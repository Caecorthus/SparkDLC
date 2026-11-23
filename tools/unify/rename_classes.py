#!/usr/bin/env python3
"""Stage 3 "one vocabulary": rename the classes and mixin members that still carry the name of a former mod.

阶段三“统一词汇”：改掉仍然带着旧模组名字的类名与 mixin 成员名。

The five former mods (SparkFactionAPI, SparkStrength, SparkTraits, SparkWitch, SparkAssist) are one mod now, but
about 220 classes and about 2800 mixin members were still named after them. This script

* renames classes (`class-renames.tsv`, kind `rename`): the file is moved with a plain rename and every whole-word
  occurrence of the old simple name is rewritten, in code, comments, strings, mixin configs, `fabric.mod.json`,
  verify scripts, tools, docs and the local tests. The package never changes here (`move_packages.py` moves);
* merges the registries that were split by former mod into one class per kind (kind `merge`: the class bodies are
  concatenated in row order, each part keeps its own registration method; kind `stored`: the merged class is the
  hand-written file in `merged/`);
* deletes the transitional holder classes and points their call sites at `SparkDLC` / `SparkDLCClient` (kind
  `absorb`);
* gives every mixin member the prefix `sparkdlc$` (`mixin-member-renames.tsv` holds the names that would clash).

`plan` checks the tables against the tree and changes nothing; it fails on a class that matches a former-mod name
pattern and is in neither table, on two classes with one name in one package, on a file that would import one simple
name twice, on a member clash inside a merged class, on a changed source of a hand-merged class and on two mixin
members that would get the same name. `apply --phase main|tooling|tests` rewrites one part of the tree. Rows whose
class is gone (another change removed it) are skipped with a note, so the script can be run again on a tree that has
changed. See README.md, third part.

`plan` 只检查，不修改；`apply --phase main|tooling|tests` 改写代码树的一部分。类已不存在的行会被跳过并给出说明，
因此脚本可以在已发生变化的代码树上重新运行。详见 README.md 第三部分。
"""
import argparse
import fnmatch
import hashlib
import os
import re
import subprocess
import sys
from collections import OrderedDict, defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
BASE_PKG = 'dev.caecorthus.sparkdlc'
BASE_DIR = BASE_PKG.replace('.', '/')

RENAMES_FILE = os.path.join(HERE, 'class-renames.tsv')
KEEPS_FILE = os.path.join(HERE, 'class-keeps.tsv')
MEMBERS_FILE = os.path.join(HERE, 'member-renames.tsv')
MIXIN_MEMBERS_FILE = os.path.join(HERE, 'mixin-member-renames.tsv')
EDITS_FILE = os.path.join(HERE, 'source-edits.tsv')
MERGED_DIR = os.path.join(HERE, 'merged')
STORED_SOURCES_FILE = os.path.join(MERGED_DIR, 'sources.tsv')
FILE_MOVES = 'tools/unify/file-moves.tsv'
TEST_FILE_MOVES = 'src/test/.unify-file-moves.tsv'
APPLIED_FILE = os.path.join(HERE, 'applied-phases.txt')
APPLIED_TEST_FILE_REL = 'src/test/.unify-applied-phases.txt'
PHASE_NAMES = {'main': 'names-main', 'tooling': 'names-tooling', 'tests': 'names-tests'}

PROD_ROOTS = ('src/main/java', 'src/client/java', 'src/gametest/java')
TEST_ROOTS = ('src/test/java', 'src/test/strength/java')
MAIN_TREES = PROD_ROOTS + ('src/main/resources', 'src/client/resources', 'src/gametest/resources')
# The two id tables are data about old ids; their note column names Java classes, and only those names follow.
# 两张 id 表记录的是旧 id；其备注列提到 Java 类，只有这些类名随之改写。
TOOLING_FILES = ('build.gradle', 'tools/payload_policy.py', 'CONTEXT.md', 'README.md', 'THIRD_PARTY_NOTICES.md',
                 'tools/unify/id-inventory.tsv', 'tools/unify/legacy-ids.tsv')
TOOLING_TREES = ('gradle/verify', 'config', '.github', 'docs/assist', 'docs/factionapi', 'docs/witch')
# History: these describe the separate mods or the first fusion and keep the names of that time.
# 历史文档：描述独立模组时期或第一次融合，保留当时的名字。
TOOLING_SKIP = ('docs/*/DOWNSTREAM_MIGRATION_NOTES.md', 'docs/factionapi/SPARK_MOD_FEATURES.md')
TEST_TREES = ('src/test/java', 'src/test/strength', 'src/test/resources')
TEXT_EXTS = ('.java', '.json', '.mcmeta', '.txt', '.properties', '.md', '.tsv', '.gradle', '.yml', '.yaml', '.py',
             '.accesswidener', '.toml', '.cfg', '.groovy', '.kts', '.xml', '.Extension')

# A class is "named after a former mod" when its simple name matches one of these. Faction* is not here: the faction
# is a concept of the game. 名字符合以下任一模式的类视为“以旧模组命名”。Faction* 不在其中：阵营是游戏概念。
NAME_PATTERNS = (
    re.compile(r'Spark(Witch|Strength|Traits|Faction|Assist)'),
    re.compile(r'^(Witch|Witches|Strength|Traits|Assist)(?=[A-Z])'),
    re.compile(r'(Witch|Strength|Traits|Assist)(Client)?(Bridge|Compat)$'),
)
OLD_PREFIXES = ('sparkfactionapi', 'sparkstrength', 'sparktraits', 'sparkwitch', 'sparkassist')
NEW_PREFIX = 'sparkdlc'
TOKEN_RE = re.compile(r'(?<![A-Za-z0-9_])(' + '|'.join(OLD_PREFIXES + (NEW_PREFIX,)) + r')((?:\\)*\$)([A-Za-z0-9_]*)')
# Sub-logger names: "SparkWitch/MiniWitherSkin" -> "SparkDLC/MiniWitherSkin". Only the string-literal form is touched
# (a CamelCase word after the slash, then the closing quote), so prose and resource paths stay as they are.
# 子日志器名字：只改写字符串字面量形式（斜杠后是驼峰词，随后是结束引号），普通文字与资源路径保持不变。
LOGGER_RE = re.compile(r'(?<![A-Za-z0-9_])(?:SparkAssist|SparkWitch|SparkStrength|SparkTraits|SparkFactionAPI)/'
                       r'(?=[A-Z][A-Za-z0-9]*\\?")')
NEW_LOGGER_ROOT = 'SparkDLC/'
IDENT_L = r'(?<![A-Za-z0-9_$])'
IDENT_R = r'(?![A-Za-z0-9_])'
SECTION_RULE = '    // ' + '-' * 111


def read(path):
    with open(path, encoding='utf-8', newline='') as f:
        return f.read()


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='') as f:
        f.write(text)


def read_tsv(path):
    """Rows of a TSV as dicts; `#` lines and blank lines are comments. / 读取 TSV，`#` 行与空行为注释。"""
    rows, header = [], None
    if not os.path.exists(path):
        return rows
    for n, line in enumerate(read(path).split('\n'), 1):
        if not line.strip() or line.startswith('#'):
            continue
        cells = line.split('\t')
        if header is None:
            header = cells
            continue
        if len(cells) != len(header):
            sys.exit('%s:%d: %d columns, expected %d' % (os.path.relpath(path, REPO), n, len(cells), len(header)))
        rows.append(dict(zip(header, cells)))
    return rows


def git(*args):
    return subprocess.run(('git',) + args, cwd=REPO, check=True, capture_output=True, text=True).stdout


def tracked(rel):
    return subprocess.run(('git', 'ls-files', '--error-unmatch', rel), cwd=REPO, capture_output=True).returncode == 0


def move_file(old_rel, new_rel):
    """Plain rename; through git for a tracked file so the history follows. / 普通重命名；已跟踪的文件走 git。"""
    os.makedirs(os.path.dirname(os.path.join(REPO, new_rel)), exist_ok=True)
    if tracked(old_rel):
        git('mv', '-f', old_rel, new_rel)
    else:
        os.replace(os.path.join(REPO, old_rel), os.path.join(REPO, new_rel))


def remove_file(rel):
    if tracked(rel):
        git('rm', '-q', '-f', rel)
    else:
        os.remove(os.path.join(REPO, rel))


def walk(rel_dir):
    out = []
    for d, dirs, files in os.walk(os.path.join(REPO, rel_dir)):
        dirs.sort()
        for f in sorted(files):
            out.append(os.path.relpath(os.path.join(d, f), REPO))
    return out


# --------------------------------------------------------------------------------------------------------------------
# Java text helpers / Java 文本工具
# --------------------------------------------------------------------------------------------------------------------

def mask_java(text):
    """Same length as `text`; comments and the contents of string and char literals are blanked (newlines stay).

    与 `text` 等长；注释以及字符串、字符字面量的内容被替换为空格（保留换行）。"""
    out, i, n = [], 0, len(text)
    while i < n:
        c = text[i]
        if text.startswith('//', i):
            j = text.find('\n', i)
            j = n if j < 0 else j
            out.append(' ' * (j - i))
        elif text.startswith('/*', i):
            j = text.find('*/', i + 2)
            j = n if j < 0 else j + 2
            out.append(re.sub(r'[^\n]', ' ', text[i:j]))
        elif text.startswith('"""', i):
            j = text.find('"""', i + 3)
            j = n if j < 0 else j + 3
            out.append(re.sub(r'[^\n]', ' ', text[i:j]))
        elif c == '"' or c == "'":
            j = i + 1
            while j < n and text[j] != c and text[j] != '\n':
                j += 2 if text[j] == '\\' else 1
            j = min(j + 1, n)
            out.append(c + ' ' * (j - i - 2) + c if j - i >= 2 else c)
        else:
            out.append(c)
            j = i + 1
        i = j
    return ''.join(out)


IMPORT_RE = re.compile(r'^import\s+(static\s+)?([\w.]+(?:\.\*)?)\s*;[ \t]*$', re.M)


def strip_annotations(masked):
    """Removes `@Name` and `@Name(...)` from masked code. / 去掉注解。"""
    out, i, n = [], 0, len(masked)
    while i < n:
        if masked[i] == '@' and not masked.startswith('@interface', i):
            j = i + 1
            while j < n and (masked[j].isalnum() or masked[j] in '._'):
                j += 1
            k = j
            while k < n and masked[k] in ' \t\n':
                k += 1
            if k < n and masked[k] == '(':
                depth, k = 1, k + 1
                while k < n and depth:
                    depth += (masked[k] == '(') - (masked[k] == ')')
                    k += 1
                j = k
            out.append(' ' * (j - i))
            i = j
        else:
            out.append(masked[i])
            i += 1
    return ''.join(out)


class Member:
    def __init__(self, name, kind, text):
        self.name, self.kind, self.text = name, kind, text  # kind: field, method, type, ctor, init


class JavaClass:
    """A top-level class split into header, class comment and depth-1 members. / 拆成头部、类注释与一级成员的顶层类。"""

    def __init__(self, text, simple):
        self.text, self.simple = text, simple
        masked = mask_java(text)
        m = re.search(r'^(?:(?:public|final|abstract)\s+)*(?:class|interface|enum|record)\s+' + re.escape(simple)
                      + r'\b[^{;]*\{', masked, re.M)
        if not m:
            raise ValueError('no top-level type %s' % simple)
        self.imports = [(bool(s), name) for s, name in IMPORT_RE.findall(text[:m.start()])]
        last_import = 0
        for im in IMPORT_RE.finditer(text[:m.start()]):
            last_import = im.end()
        if not last_import:
            pm = re.search(r'^package\s+[\w.]+\s*;[ \t]*$', text, re.M)
            last_import = pm.end() if pm else 0
        self.doc = text[last_import:m.start()].strip('\n')
        self.declaration = text[m.start():m.end()]
        body_start = m.end()
        body_end = masked.rindex('}')
        self.members, self.tail = self._split(text, masked, body_start, body_end)

    def _split(self, text, masked, start, end):
        members, seg, i = [], start, start
        while i < end:
            # skip to the first code character of the next member
            j = i
            while j < end and masked[j] in ' \t\n\r':
                j += 1
            if j >= end:
                break
            # head: up to the first `{`, `;` or `=` outside parentheses
            k, paren, has_assign = j, 0, False
            while k < end:
                c = masked[k]
                if c == '(':
                    paren += 1
                elif c == ')':
                    paren -= 1
                elif paren == 0 and c in '{;':
                    break
                elif paren == 0 and c == '=':
                    has_assign = True
                k += 1
            head = strip_annotations(masked[j:k])
            head = head.split('=')[0] if has_assign else head
            # body: to the `;` at brace depth 0 (field) or to the closing brace (method, type, initialiser)
            depth, p = 0, k
            while p < end:
                c = masked[p]
                if c == '{':
                    depth += 1
                elif c == '}':
                    depth -= 1
                    if depth == 0 and not has_assign:
                        p += 1
                        break
                elif c == ';' and depth == 0:
                    p += 1
                    break
                p += 1
            name, kind = self._name(head)
            members.append(Member(name, kind, text[seg:p]))
            seg = i = p
        return members, text[seg:end]

    def _name(self, head):
        head = ' '.join(head.split())
        tm = re.search(r'\b(class|interface|enum|record)\s+(\w+)', head)
        if tm:
            return tm.group(2), 'type'
        if '(' in head:
            name = re.search(r'(\w+)\s*\($', head[:head.index('(') + 1]).group(1)
            return name, 'ctor' if name == self.simple else 'method'
        if head in ('', 'static'):
            return None, 'init'
        return re.search(r'(\w+)(?:\s*\[\s*\])*$', head).group(1), 'field'

    def member_names(self):
        return [m.name for m in self.members if m.kind in ('field', 'method', 'type')]


# --------------------------------------------------------------------------------------------------------------------
# The tree / 代码树
# --------------------------------------------------------------------------------------------------------------------

def scan_classes(roots):
    """Relative class name -> repository path, for the top-level classes below `roots`. / 相对类名 -> 仓库路径。"""
    index = {}
    for root in roots:
        base = os.path.join(root, BASE_DIR)
        if not os.path.isdir(os.path.join(REPO, base)):
            continue
        for rel in walk(base):
            if rel.endswith('.java'):
                index[os.path.relpath(rel, base)[:-5].replace(os.sep, '.')] = rel
    return index


def simple_of(fqcn):
    return fqcn.rsplit('.', 1)[-1]


def package_of(fqcn):
    return fqcn.rsplit('.', 1)[0] if '.' in fqcn else ''


def glob_to_re(glob):
    out = ''
    for part in re.split(r'(\*\*|\*)', glob):
        out += '.*' if part == '**' else '[^.]*' if part == '*' else re.escape(part)
    return re.compile(out + r'\Z')


class Plan:
    """The tables resolved against the current tree. / 对照当前代码树解析后的各张表。"""

    def __init__(self, everything=False):
        self.problems, self.notes = [], []
        self.prod = scan_classes(PROD_ROOTS)
        self.tests = scan_classes(TEST_ROOTS)
        self.rows = read_tsv(RENAMES_FILE)
        self.keeps = [(glob_to_re(r['fqcn_glob']), r) for r in read_tsv(KEEPS_FILE)]
        self.member_rows = read_tsv(MEMBERS_FILE)
        self.token_rows = read_tsv(MIXIN_MEMBERS_FILE)
        self.edits = read_tsv(EDITS_FILE)
        self.everything = everything  # `translate`: every row is active, whatever the tree looks like
        self.groups = OrderedDict()   # new fqcn -> rows of kind merge / stored
        self.simple_map = {}          # old simple name -> new simple name (whole-word rewrite)
        self.absorbed = {}            # old simple name -> (new simple name, new fqcn)
        self.file_renames = []        # (old fqcn, new fqcn) of pending plain renames
        self.done_renames = []        # (old fqcn, new fqcn) of renames that are in the tree already
        self._resolve_rows()
        self._resolve_members()
        self._resolve_tokens()

    # -- class rows ---------------------------------------------------------------------------------------------
    def _resolve_rows(self):
        seen = set()
        for row in self.rows:
            old, new, kind = row['old_fqcn'], row['new_fqcn'], row['kind']
            if old in seen:
                self.problems.append('class-renames.tsv: %s is listed twice' % old)
            seen.add(old)
            if kind not in ('rename', 'merge', 'stored', 'absorb'):
                self.problems.append('class-renames.tsv: %s: unknown kind %s' % (old, kind))
                continue
            if package_of(old) != package_of(new):
                self.problems.append('class-renames.tsv: %s -> %s changes the package; this script only renames'
                                     % (old, new))
            row['old_exists'] = old in self.prod
            if kind in ('merge', 'stored'):
                self.groups.setdefault(new, []).append(row)
        for row in self.rows:
            old, new, kind = row['old_fqcn'], row['new_fqcn'], row['kind']
            if kind == 'rename':
                if row['old_exists'] and new in self.prod:
                    self.problems.append('%s -> %s: both classes exist' % (old, new))
                    row['state'] = 'pending'
                elif row['old_exists']:
                    row['state'] = 'pending'
                    self.file_renames.append((old, new))
                elif new in self.prod:
                    row['state'] = 'done'
                    self.done_renames.append((old, new))
                else:
                    row['state'] = 'gone'
                    self.notes.append('skipped %s -> %s: the class no longer exists' % (old, new))
            elif kind == 'absorb':
                row['state'] = 'pending' if row['old_exists'] else 'done'
                if new not in self.prod:
                    self.problems.append('%s: the absorbing class %s does not exist' % (old, new))
            if self.everything and row.get('state') == 'gone':
                row['state'] = 'done'
        for new, rows in self.groups.items():
            sources = [r for r in rows if r['old_fqcn'] != new]
            live = [r for r in sources if r['old_exists']]
            state = 'pending' if live else 'done' if new in self.prod or self.everything else 'gone'
            for r in rows:
                r['state'] = state
            if state == 'gone':
                self.notes.append('skipped the merge into %s: none of its classes exists any more' % new)
            elif state == 'pending':
                for r in sources:
                    if not r['old_exists']:
                        self.notes.append('merge into %s: %s no longer exists, merging the rest' % (new, r['old_fqcn']))
                if new in self.prod and not any(r['old_fqcn'] == new for r in rows):
                    self.problems.append('merge into %s: the class exists already and is not a row of the merge' % new)
        for row in self.rows:
            if row.get('state') in (None, 'gone'):
                continue
            old, new = simple_of(row['old_fqcn']), simple_of(row['new_fqcn'])
            if old == new:
                continue
            if row['kind'] == 'absorb':
                self.absorbed[old] = (new, row['new_fqcn'])
            elif self.simple_map.setdefault(old, new) != new:
                self.problems.append('the simple name %s is renamed to both %s and %s: give the classes one new name'
                                     % (old, self.simple_map[old], new))

    def active(self, row):
        return row.get('state') in ('pending', 'done')

    # -- members of merged classes ------------------------------------------------------------------------------
    def _resolve_members(self):
        self.member_map = []  # (old simple, member, new class simple, new member, import fqcn or None)
        self.member_actions = defaultdict(dict)  # old fqcn -> member -> (action, new member)
        by_old = {r['old_fqcn']: r for r in self.rows}
        for r in self.member_rows:
            owner = by_old.get(r['class'])
            if owner is None:
                self.problems.append('member-renames.tsv: %s is not a row of class-renames.tsv' % r['class'])
                continue
            if not self.active(owner):
                continue
            action, new = r['action'], r['new']
            self.member_actions[r['class']][r['member']] = (action, new)
            if action == 'rename':
                self.member_map.append((simple_of(r['class']), r['member'], simple_of(owner['new_fqcn']), new, None))
            elif action == 'move':
                target, member = new.split('#')
                self.member_map.append((simple_of(r['class']), r['member'], simple_of(target), member,
                                        BASE_PKG + '.' + target))
            elif action != 'drop':
                self.problems.append('member-renames.tsv: %s.%s: unknown action %s' % (r['class'], r['member'], action))

    # -- mixin member prefixes ----------------------------------------------------------------------------------
    def _resolve_tokens(self):
        self.token_map = {}
        for r in self.token_rows:
            old, new = r['old_member'], r['new_member']
            if '$' not in old or not new.startswith(NEW_PREFIX + '$'):
                self.problems.append('mixin-member-renames.tsv: %s -> %s: the new name must start with %s$'
                                     % (old, new, NEW_PREFIX))
                continue
            self.token_map[tuple(old.split('$', 1))] = new.split('$', 1)[1]

    def new_token_name(self, prefix, name):
        if prefix == NEW_PREFIX:
            return name
        return self.token_map.get((prefix, name), name)


# --------------------------------------------------------------------------------------------------------------------
# Rewriting text / 改写文本
# --------------------------------------------------------------------------------------------------------------------

class Rewriter:
    def __init__(self, plan):
        self.plan = plan
        self.member_res = []
        for old_simple, member, new_simple, new_member, imp in plan.member_map:
            # the separator may be regex-escaped in a test: `SparkWitchItems\.register` / 测试中的分隔符可能带正则转义
            rx = re.compile(IDENT_L + re.escape(old_simple) + r'(\s*(?:\\*\.|::|#)\s*)' + re.escape(member) + IDENT_R)
            self.member_res.append((rx, new_simple, new_member, imp, old_simple))
        # old simple name -> the packages (as segment lists) of the classes that had it
        self.packages = defaultdict(list)
        for row in plan.rows:
            if plan.active(row):
                self.packages[simple_of(row['old_fqcn'])].append(
                    (BASE_PKG + '.' + package_of(row['old_fqcn'])).rstrip('.').split('.'))
        self.simple_re = None
        if plan.simple_map:
            names = sorted(plan.simple_map, key=len, reverse=True)
            self.simple_re = re.compile(IDENT_L + '(' + '|'.join(map(re.escape, names)) + ')' + IDENT_R)
        self.absorb = {}
        for old, (new, new_fqcn) in plan.absorbed.items():
            members = set(['class'])
            path = plan.prod.get(new_fqcn)
            if path:
                members.update(JavaClass(read(os.path.join(REPO, path)), simple_of(new_fqcn)).member_names())
            self.absorb[old] = (new, members)
        self.absorb_re = None
        if self.absorb:
            names = sorted(self.absorb, key=len, reverse=True)
            self.absorb_re = re.compile(IDENT_L + '(' + '|'.join(map(re.escape, names)) + ')' + IDENT_R)
        self.leftovers = []  # (path, line number, line) of absorbed names that stayed in prose
        self.foreign = []    # (path, line number, line) of names behind a path that is not the class's package
        self.rel = None

    def rewrite(self, rel, text):
        java = rel.endswith('.java')
        self.rel = rel
        imports_needed = set()
        for rx, new_simple, new_member, imp, old_simple in self.member_res:
            def repl(m, new_simple=new_simple, new_member=new_member, imp=imp, old_simple=old_simple):
                if not self._qualifier_fits(text, m.start(), old_simple):
                    return m.group(0)
                if imp:
                    imports_needed.add(imp)
                return new_simple + m.group(1) + new_member
            if old_simple in text:
                text = rx.sub(repl, text)
        if self.absorb_re is not None:
            text = self._rewrite_absorbed(rel, text, java)
        if self.simple_re is not None:
            source = text
            text = self.simple_re.sub(
                lambda m: self.plan.simple_map[m.group(1)] if self._qualifier_fits(source, m.start(), m.group(1))
                else m.group(1), source)
        text = TOKEN_RE.sub(self._token, text)
        text = LOGGER_RE.sub(NEW_LOGGER_ROOT, text)
        if java:
            text = fix_imports(text, imports_needed)
        return text

    QUALIFIER_RE = re.compile(r'((?:[A-Za-z_][A-Za-z0-9_\-]*[./])+)$')

    def _qualifier_fits(self, text, start, old_simple):
        """A name behind a package or directory path that is not the class's own package may be a name of the past:
        `dev/caecorthus/sparkdlc/factionapi/util/SparkFactionPermissions.class` names a path of the past (a check that
        a stale class is not in the jar) and stays.

        带有包路径或目录路径的名字，只有当该路径确实是这个类所在的包时才改写：指向过去路径的名字（例如检查某个过期类不在 jar
        中）保持不变。"""
        m = self.QUALIFIER_RE.search(text, max(0, start - 200), start)
        if not m:
            return True
        segments = [s for s in re.split(r'[./]', m.group(1)) if s]
        if not segments or not segments[-1][0].islower():
            return True  # Outer.Inner, or a receiver expression / 外部类限定或表达式
        tail = []
        for s in reversed(segments):
            if not s[0].islower():
                break
            tail.insert(0, s)
        for real in self.packages.get(old_simple, ()):
            n = min(len(tail), len(real))
            if tail[len(tail) - n:] == real[len(real) - n:]:
                return True
        # A mismatch is history only when it names a file (`.class`, `.java`) or a whole name from the root package;
        # a loose `compat/SparkStrengthHudBridge` in a comment still means the class, and follows it.
        # 只有指向文件（`.class`、`.java`）或从根包写起的全名才算历史；注释里零散的相对路径仍然指这个类，随之改写。
        after = re.match(r'[A-Za-z0-9_]*(?:\$\w+)*\.(?:class|java)\b', text[start:start + 200])
        if not after and 'caecorthus' not in tail:
            return True
        line_no = text.count('\n', 0, start) + 1
        self.foreign.append((self.rel, line_no, text.split('\n')[line_no - 1].strip()))
        return False

    def _token(self, m):
        prefix, sep, name = m.group(1), m.group(2), m.group(3)
        return NEW_PREFIX + sep + self.plan.new_token_name(prefix, name)

    def _rewrite_absorbed(self, rel, text, java):
        """A holder name is also a word of prose ("SparkWitch adds ..."), so it is rewritten only where it is code:
        in Java code, as a qualifier of a member of the absorbing class, and in a fully-qualified name or a link.

        持有类的名字同时是普通词语（“SparkWitch 新增……”），因此只在它确实是代码的地方改写：Java 代码中、作为吸收类
        成员的限定符、全限定名或文档链接中。"""
        masked = mask_java(text) if java else None

        def repl(m):
            old = m.group(1)
            new, members = self.absorb[old]
            if masked is not None and masked[m.start():m.end()] == old:
                return new
            after = re.match(r'\s*(?:\\*\.|::|#)\s*(\w+)', text[m.end():m.end() + 80])
            before = text[max(0, m.start() - 80):m.start()]
            if after and after.group(1) in members:
                return new
            # `SparkWitch.java`, `SparkWitch.class`, and a declaration inside a test double: `class SparkWitch {`
            # 文件名形式，以及测试替身里的声明
            if re.match(r'\.(?:java|class)\b', text[m.end():m.end() + 8]) or re.search(r'\bclass\s+$', before):
                return new
            if re.search(r'(?:sparkdlc[./](?:client[./])?|\{@link(?:plain)?\s+|@see\s+|\bimport\s+(?:static\s+)?)$',
                         before):
                return new
            line_no = text.count('\n', 0, m.start()) + 1
            self.leftovers.append((rel, line_no, text.split('\n')[line_no - 1].strip()))
            return old

        return self.absorb_re.sub(repl, text)


def fix_imports(text, needed):
    """Drops repeated import lines and adds the imports a moved member needs. / 去掉重复的 import，补上成员搬迁所需的 import。"""
    pm = re.search(r'^package\s+([\w.]+)\s*;', text, re.M)
    package = pm.group(1) if pm else ''
    seen, out, changed = set(), [], False
    lines = text.split('\n')
    for line in lines:
        m = IMPORT_RE.match(line)
        if m:
            key = (bool(m.group(1)), m.group(2))
            if key in seen:
                changed = True
                continue
            seen.add(key)
        out.append(line)
    for fqcn in sorted(needed):
        if package_of(fqcn) == package or (False, fqcn) in seen:
            continue
        line = 'import %s;' % fqcn
        idx = [i for i, l in enumerate(out) if IMPORT_RE.match(l) and not IMPORT_RE.match(l).group(1)]
        if not idx:
            at = next(i for i, l in enumerate(out) if l.startswith('package ')) + 1
            out[at:at] = ['', line]
        else:
            after = [i for i in idx if out[i] > line and out[i].startswith('import dev.')]
            out.insert(after[0] if after else idx[0], line)
        changed = True
    return '\n'.join(out) if changed else text


# --------------------------------------------------------------------------------------------------------------------
# Merged classes / 合并类
# --------------------------------------------------------------------------------------------------------------------

def rename_unqualified(body, renames):
    """Renames a class's own members where the class itself uses them without a qualifier.

    改写类自身不带限定符使用的成员名。"""
    if not renames:
        return body
    masked = mask_java(body)
    names = '|'.join(re.escape(n) for n in sorted(renames, key=len, reverse=True))
    code_re = re.compile(r'(?<![A-Za-z0-9_$.])(' + names + r')' + IDENT_R)
    link_re = re.compile(r'(?<=#)(' + names + r')' + IDENT_R)
    out, last = [], 0
    for m in re.finditer(r'(?<![A-Za-z0-9_$])(?:' + names + r')' + IDENT_R, body):
        in_code = masked[m.start():m.end()] == m.group(0)
        rx = code_re if in_code else link_re
        hit = rx.match(body, m.start())
        if not hit and not in_code:
            continue
        if in_code and not hit:
            continue
        out.append(body[last:m.start()])
        out.append(renames[m.group(0)])
        last = m.end()
    out.append(body[last:])
    return ''.join(out)


def import_sort_key(entry):
    static, name = entry
    return (2 if static else 1 if name.startswith(('java.', 'javax.')) else 0, name)


def build_merged(plan, new_fqcn, rows):
    """The text of a merged class: the bodies of the row classes in row order, one section each.

    合并类的文本：各行的类体按行序拼接，每个类一个部分。"""
    simple = simple_of(new_fqcn)
    package = (BASE_PKG + '.' + package_of(new_fqcn)).rstrip('.')
    imports, sections = set(), []
    header_path = os.path.join(MERGED_DIR, simple + '.header.txt')
    has_header = os.path.exists(header_path)
    group_fqcns = {BASE_PKG + '.' + r['old_fqcn'] for r in rows} | {BASE_PKG + '.' + new_fqcn}
    for row in rows:
        if not row['old_exists']:
            continue
        old = row['old_fqcn']
        cls = JavaClass(read(os.path.join(REPO, plan.prod[old])), simple_of(old))
        imports.update(i for i in cls.imports if i[1] not in group_fqcns)
        actions = plan.member_actions.get(old, {})
        renames = {m: new for m, (action, new) in actions.items() if action == 'rename'}
        for m, (action, new) in actions.items():
            if action == 'move':  # the part still uses the member it gave away / 该部分仍在使用被搬走的成员
                target, member = new.split('#')
                renames[m] = simple_of(target) + '.' + member
                if package_of(target) != package_of(new_fqcn):
                    imports.add((False, BASE_PKG + '.' + target))
        parts = []
        for member in cls.members:
            if member.kind == 'ctor':
                continue
            if member.name in actions and actions[member.name][0] in ('drop', 'move'):
                continue
            parts.append(member.text)
        body = rename_unqualified(''.join(parts) + cls.tail.rstrip(), renames).strip('\n')
        doc = '' if old == new_fqcn and has_header else cls.doc.strip()
        if doc.startswith('/**'):
            doc = '/*' + doc[3:]
        doc = '\n'.join('    ' + l if l.strip() else l for l in doc.split('\n')) if doc else ''
        section = '%s\n    // %s\n%s\n' % (SECTION_RULE, row['section'], SECTION_RULE)
        sections.append(section + (doc + '\n\n' if doc else '\n') + body + '\n')
    header = read(header_path).rstrip('\n') + '\n' if has_header else ''
    lines = ['package %s;' % package, '']
    previous = None
    for static, name in sorted(imports, key=import_sort_key):
        block = import_sort_key((static, name))[0]
        if previous is not None and block != previous:
            lines.append('')
        previous = block
        lines.append('import %s%s;' % ('static ' if static else '', name))
    text = '\n'.join(lines) + '\n\n' + header + 'public final class %s {\n    private %s() {\n    }\n\n' % (simple, simple)
    return text + '\n'.join(sections) + '}\n'


def stored_sources():
    return {r['old_fqcn']: r['sha256'] for r in read_tsv(STORED_SOURCES_FILE)}


def sha256_of(rel):
    return hashlib.sha256(read(os.path.join(REPO, rel)).encode('utf-8')).hexdigest()


# --------------------------------------------------------------------------------------------------------------------
# Checks / 检查
# --------------------------------------------------------------------------------------------------------------------

def check_unlisted(plan):
    listed = {r['old_fqcn'] for r in plan.rows} | {r['new_fqcn'] for r in plan.rows}
    for fqcn in sorted(plan.prod):
        simple = simple_of(fqcn)
        if not any(p.search(simple) for p in NAME_PATTERNS) or fqcn in listed:
            continue
        if any(rx.match(fqcn) for rx, _ in plan.keeps):
            continue
        plan.problems.append('unlisted class named after a former mod: %s (add a row to class-renames.tsv, or to '
                             'class-keeps.tsv with the reason)' % fqcn)


def renamed_index(plan):
    """The production classes as they will be: new name -> list of old names. / 改名后的生产类：新名 -> 旧名列表。"""
    target = {}
    for row in plan.rows:
        if row.get('state') == 'pending' and row['old_exists']:
            target[row['old_fqcn']] = row['new_fqcn']
    result = defaultdict(list)
    for fqcn in plan.prod:
        result[target.get(fqcn, fqcn)].append(fqcn)
    return result, target


def check_name_collisions(plan):
    result, target = renamed_index(plan)
    merged = set(plan.groups) | {r['new_fqcn'] for r in plan.rows if r['kind'] == 'absorb'}
    for new, olds in sorted(result.items()):
        if len(olds) > 1 and new not in merged:
            plan.problems.append('name collision in one package: %s <- %s' % (new, ', '.join(sorted(olds))))
        if new in plan.tests and new not in plan.prod:
            plan.problems.append('name collision with a test class: %s' % new)
    # one old simple name, several classes, different outcomes: the whole-word rewrite could not tell them apart
    by_simple = defaultdict(set)
    for index in (plan.prod, plan.tests):
        for fqcn in index:
            by_simple[simple_of(fqcn)].add(fqcn)
    for old_simple in list(plan.simple_map) + list(plan.absorbed):
        outcomes = {simple_of(target.get(f, f)) for f in by_simple.get(old_simple, ())}
        if len(outcomes) > 1:
            plan.problems.append('the simple name %s belongs to several classes with different new names (%s): '
                                 'rename all of them alike' % (old_simple, ', '.join(sorted(by_simple[old_simple]))))


def check_imports(plan):
    """No file may end up importing two classes with one simple name, or one that shadows its own class.

    任何文件都不能导入两个同名的类，也不能导入与自身同名的类。"""
    rewriter_map = dict(plan.simple_map)
    rewriter_map.update({old: new for old, (new, _) in plan.absorbed.items()})
    if not rewriter_map:
        return
    new_simples = set(rewriter_map.values())
    for index in (plan.prod, plan.tests):
        for fqcn, rel in index.items():
            text = read(os.path.join(REPO, rel))
            own = rewriter_map.get(simple_of(fqcn), simple_of(fqcn))
            seen = {}
            for static, name in IMPORT_RE.findall(text):
                if static or name.endswith('.*'):
                    continue
                simple = simple_of(name)
                new_simple = rewriter_map.get(simple, simple) if name.startswith(BASE_PKG + '.') else simple
                new_name = name[:len(name) - len(simple)] + new_simple
                if new_simple not in new_simples:
                    continue
                if seen.setdefault(new_simple, new_name) != new_name:
                    plan.problems.append('%s would import two classes named %s: %s and %s'
                                         % (rel, new_simple, seen[new_simple], new_name))
                if new_simple == own and new_name != BASE_PKG + '.' + package_of(fqcn) + '.' + own \
                        and package_of(fqcn):
                    plan.problems.append('%s would import %s, which has the name of the class itself' % (rel, new_name))


def check_merges(plan):
    hashes = stored_sources()
    for new, rows in plan.groups.items():
        if rows[0]['state'] != 'pending':
            continue
        if rows[0]['kind'] == 'stored':
            stored = os.path.join(MERGED_DIR, simple_of(new) + '.java')
            if not os.path.exists(stored):
                plan.problems.append('%s: the hand-written class tools/unify/merged/%s.java is missing'
                                     % (new, simple_of(new)))
            for r in rows:
                if not r['old_exists']:
                    plan.problems.append('%s: %s no longer exists; the hand-written merge has to be redone'
                                         % (new, r['old_fqcn']))
                elif hashes.get(r['old_fqcn']) != sha256_of(plan.prod[r['old_fqcn']]):
                    plan.problems.append('%s: %s changed since the class was merged by hand; update '
                                         'tools/unify/merged/%s.java and the hash in merged/sources.tsv (sha256 %s)'
                                         % (new, r['old_fqcn'], simple_of(new), sha256_of(plan.prod[r['old_fqcn']])))
            continue
        names = {}
        for r in rows:
            if not r['old_exists']:
                continue
            old = r['old_fqcn']
            cls = JavaClass(read(os.path.join(REPO, plan.prod[old])), simple_of(old))
            actions = plan.member_actions.get(old, {})
            declared = set(cls.member_names())
            for member in actions:
                if member not in declared:
                    plan.problems.append('member-renames.tsv: %s has no member %s' % (old, member))
            for name in sorted(declared):
                action, renamed = actions.get(name, ('keep', name))
                if action in ('drop', 'move'):
                    continue
                final = renamed if action == 'rename' else name
                if names.setdefault(final, old) != old:
                    plan.problems.append('member collision in %s: %s is declared by %s and %s (add a row to '
                                         'member-renames.tsv)' % (new, final, names[final], old))
    for row in plan.rows:
        if row['kind'] == 'absorb' and row['old_exists'] and row['new_fqcn'] in plan.prod:
            source = JavaClass(read(os.path.join(REPO, plan.prod[row['old_fqcn']])), simple_of(row['old_fqcn']))
            target = JavaClass(read(os.path.join(REPO, plan.prod[row['new_fqcn']])), simple_of(row['new_fqcn']))
            missing = set(source.member_names()) - set(target.member_names())
            if missing:
                plan.problems.append('%s cannot be absorbed by %s, which lacks %s'
                                     % (row['old_fqcn'], row['new_fqcn'], ', '.join(sorted(missing))))


MIXIN_RE = re.compile(r'@Mixin\s*\(')
DECL_KEYWORDS = {'return', 'new', 'else', 'throw', 'case', 'yield', 'assert', 'instanceof'}


def mixin_targets(text, masked):
    """The targets of a mixin class: class literals and `targets` strings. / mixin 的目标：类字面量与 `targets` 字符串。"""
    m = MIXIN_RE.search(masked)
    if not m:
        return None
    i, depth = m.end(), 1
    while depth and i < len(masked):
        depth += (masked[i] == '(') - (masked[i] == ')')
        i += 1
    arg = text[m.end():i - 1]
    imports = {simple_of(name): name for static, name in IMPORT_RE.findall(text) if not static}
    targets = []
    for literal in re.findall(r'([\w.]+)\.class', arg):
        head = literal.split('.')[0]
        targets.append((imports.get(head, head) + literal[len(head):]).replace('$', '.'))
    for string in re.findall(r'"([^"]+)"', arg):
        targets.append(string.replace('/', '.').replace('$', '.'))
    return targets


def check_mixin_members(plan):
    """Two members that are different today must stay different once every prefix is `sparkdlc$`.

    Every prefixed name of the production tree is mapped; two different old names that become one new name are a
    collision. The report names the mixins that declare them and their real targets (class literals, several targets
    and string targets). A clash on one target is certain; one on different targets is still refused, because a
    mixin on a superclass and one on its subclass share members by inheritance.

    今天不同的两个成员，在所有前缀变成 `sparkdlc$` 之后也必须保持不同。对生产代码中每个带前缀的名字做映射；两个不同的旧名字
    变成同一个新名字即为冲突。报告会列出声明它们的 mixin 及其真实目标（类字面量、多目标与字符串目标）。同一目标上的冲突是确定的；
    不同目标上的同名也会被拒绝，因为父类与子类上的 mixin 会通过继承共享成员。"""
    users = defaultdict(lambda: defaultdict(set))    # new name -> old token -> files that use it
    declared = defaultdict(lambda: defaultdict(set))  # new name -> old token -> (mixin, targets)
    for fqcn, rel in plan.prod.items():
        text = read(os.path.join(REPO, rel))
        if '$' not in text:
            continue
        masked = mask_java(text)
        targets = mixin_targets(text, masked)
        for m in TOKEN_RE.finditer(masked):
            prefix, name = m.group(1), m.group(3)
            if not name:
                continue
            new = plan.new_token_name(prefix, name)
            old = prefix + '$' + name
            users[new][old].add(fqcn)
            before = masked[:m.start()].rstrip()
            prev = re.search(r'([A-Za-z_][\w.]*|[>\]])$', before)
            nxt = masked[m.end():].lstrip()[:1]
            if targets is not None and prev and prev.group(1) not in DECL_KEYWORDS and nxt and nxt in '(=;,':
                declared[new][old].add((fqcn, tuple(targets)))
    count = 0
    for new in sorted(users):
        olds = users[new]
        if len(olds) < 2:
            continue
        count += 1
        lines = []
        target_sets = []
        for old in sorted(olds):
            sites = sorted(declared[new].get(old, ()))
            if not sites:
                lines.append('    %s: used in %s' % (old, ', '.join(sorted(olds[old])[:3])))
            for fqcn, targets in sites:
                target_sets.append((old, set(targets)))
                lines.append('    %s: %s -> %s' % (old, fqcn, ', '.join(targets) or '?'))
        shared = any(a[1] & b[1] for a in target_sets for b in target_sets if a[0] != b[0])
        where = 'on the same target' if shared else 'on different targets (possible through inheritance)'
        plan.problems.append('mixin member collision %s: %s$%s would be declared %d times; add rows to '
                             'mixin-member-renames.tsv\n%s' % (where, NEW_PREFIX, new, len(olds), '\n'.join(lines)))
    live = {old for olds in users.values() for old in olds}
    missing = ['%s$%s' % key for key in plan.token_map if '%s$%s' % key not in live]
    if missing and len(missing) == len(plan.token_map):
        plan.notes.append('mixin-member-renames.tsv: none of its %d members is in the tree (renamed already)'
                          % len(missing))
    else:
        for old in missing:
            plan.notes.append('mixin-member-renames.tsv: %s is not in the tree (renamed already, or gone)' % old)
    return count


def run_checks(plan):
    check_unlisted(plan)
    check_name_collisions(plan)
    check_imports(plan)
    check_merges(plan)
    check_mixin_members(plan)
    for e in plan.edits:
        if not os.path.exists(os.path.join(REPO, e['path'])):
            plan.notes.append('source-edits.tsv: %s does not exist, edit skipped' % e['path'])


# --------------------------------------------------------------------------------------------------------------------
# Phases / 阶段
# --------------------------------------------------------------------------------------------------------------------

def apply_edits(plan):
    """Exact one-line edits in the OLD vocabulary, before anything is renamed (so a second run finds nothing).

    在改名之前按旧词汇做的整行精确替换（因此再次运行时不会再命中）。"""
    done = 0
    for e in plan.edits:
        path = os.path.join(REPO, e['path'])
        if not os.path.exists(path):
            continue
        lines = read(path).split('\n')
        hits = [i for i, l in enumerate(lines) if l.strip() == e['old_line'].strip()]
        if not hits:
            plan.notes.append('source-edits.tsv: `%s` is not in %s (applied already, or changed)'
                              % (e['old_line'].strip(), e['path']))
            continue
        if len(hits) > 1:
            sys.exit('source-edits.tsv: `%s` matches %d lines of %s' % (e['old_line'].strip(), len(hits), e['path']))
        i = hits[0]
        if e['new_line'].strip():
            indent = lines[i][:len(lines[i]) - len(lines[i].lstrip())]
            lines[i] = indent + e['new_line'].strip()
        else:
            del lines[i]
        write(path, '\n'.join(lines))
        done += 1
    return done


def apply_main(plan, rewriter):
    edits = apply_edits(plan)
    merged = 0
    for new, rows in plan.groups.items():
        if rows[0]['state'] != 'pending':
            continue
        new_rel = os.path.join(os.path.dirname(plan.prod[next(r for r in rows if r['old_exists'])['old_fqcn']]),
                               simple_of(new) + '.java')
        if rows[0]['kind'] == 'stored':
            text = read(os.path.join(MERGED_DIR, simple_of(new) + '.java'))
        else:
            text = build_merged(plan, new, rows)
        sources = [plan.prod[r['old_fqcn']] for r in rows if r['old_exists'] and r['old_fqcn'] != new]
        if not os.path.exists(os.path.join(REPO, new_rel)):
            # the largest part carries the history / 由最大的部分承载历史
            biggest = max(sources, key=lambda rel: os.path.getsize(os.path.join(REPO, rel)))
            move_file(biggest, new_rel)
            sources.remove(biggest)
        for rel in sources:
            remove_file(rel)
        write(os.path.join(REPO, new_rel), text)
        merged += 1
    absorbed = 0
    for row in plan.rows:
        if row['kind'] == 'absorb' and row['old_exists']:
            remove_file(plan.prod[row['old_fqcn']])
            absorbed += 1
    for old, new in plan.file_renames:
        old_rel = plan.prod[old]
        move_file(old_rel, os.path.join(os.path.dirname(old_rel), simple_of(new) + '.java'))
    changed = rewrite_files(rewriter, [p for d in MAIN_TREES for p in walk(d)])
    print('%d source edits, %d merged classes, %d absorbed classes, %d renamed classes, %d files rewritten'
          % (edits, merged, absorbed, len(plan.file_renames), changed))


def rewrite_files(rewriter, paths, skip=()):
    changed = 0
    for rel in paths:
        if not rel.endswith(TEXT_EXTS) or any(fnmatch.fnmatch(rel, g) for g in skip):
            continue
        path = os.path.join(REPO, rel)
        if not os.path.isfile(path):
            continue
        try:
            text = read(path)
        except UnicodeDecodeError:
            continue
        new = rewriter.rewrite(rel, text)
        if new != text:
            write(path, new)
            changed += 1
    return changed


def rename_test_classes(plan):
    """Nothing to do: test classes keep their names (see README). / 无需处理：测试类保留原名（见 README）。"""
    return 0


def update_file_moves(plan, rel):
    """`file-moves.tsv` says which former module a file came from; consumers look a file up by its current path, so
    the current-path and current-name columns follow a plain rename. The rows of merged and absorbed classes stay as
    they are: their file is gone, and consumers take only the rows whose file exists.

    `file-moves.tsv` 记录文件来自哪个旧模块；使用方按文件当前路径查找，因此普通改名后“当前路径”“当前类名”两列随之更新。
    被合并、被吸收的类所在的行保持不变：文件已不存在，使用方只取文件仍然存在的行。"""
    path = os.path.join(REPO, rel)
    if not os.path.exists(path):
        return 0
    renames = {old: new for old, new in plan.file_renames + plan.done_renames}
    lines, changed = read(path).split('\n'), 0
    header = lines[0].split('\t')
    i_path, i_fqcn = header.index('new_path'), header.index('new_fqcn')
    for n, line in enumerate(lines[1:], 1):
        cells = line.split('\t')
        if len(cells) != len(header) or cells[i_fqcn] not in renames:
            continue
        new = renames[cells[i_fqcn]]
        cells[i_path] = os.path.join(os.path.dirname(cells[i_path]), simple_of(new) + '.java')
        cells[i_fqcn] = new
        lines[n] = '\t'.join(cells)
        changed += 1
    if changed:
        write(path, '\n'.join(lines))
    return changed


def applied(phase):
    path = os.path.join(REPO, APPLIED_TEST_FILE_REL) if phase == 'tests' else APPLIED_FILE
    return os.path.exists(path) and PHASE_NAMES[phase] in read(path).split()


def record(phase):
    path = os.path.join(REPO, APPLIED_TEST_FILE_REL) if phase == 'tests' else APPLIED_FILE
    done = read(path).split() if os.path.exists(path) else []
    if PHASE_NAMES[phase] not in done:
        write(path, '\n'.join(done + [PHASE_NAMES[phase]]) + '\n')


def finish(plan, quiet_notes=False):
    for n in plan.notes:
        print('note:', n)
    if plan.problems:
        for p in plan.problems:
            print('PROBLEM:', p)
        print('%d problem(s)' % len(plan.problems))
        return 1
    return 0


def main():
    parser = argparse.ArgumentParser(description=__doc__.split('\n')[0])
    sub = parser.add_subparsers(dest='command', required=True)
    sub.add_parser('plan', help='check the tables against the tree; changes nothing')
    ap = sub.add_parser('apply', help='rewrite one part of the tree')
    ap.add_argument('--phase', required=True, choices=sorted(PHASE_NAMES))
    ap.add_argument('--force', action='store_true', help='run a phase that is recorded as applied')
    ap.add_argument('--report', help='write the holder names that were left in prose to this file')
    tp = sub.add_parser('translate', help='rewrite one text file (for example a start-up order dump taken before '
                                          'the rename) with every row of the tables')
    tp.add_argument('file')
    tp.add_argument('--out', required=True)
    args = parser.parse_args()

    if args.command == 'translate':
        plan = Plan(everything=True)
        write(args.out, Rewriter(plan).rewrite(args.file, read(args.file)))
        return 0

    plan = Plan()
    run_checks(plan)
    if args.command == 'plan':
        states = defaultdict(int)
        for row in plan.rows:
            states[(row['kind'], row.get('state'))] += 1
        for (kind, state), n in sorted(states.items()):
            print('%-7s %-8s %d' % (kind, state, n))
        tokens = set()
        for rel in plan.prod.values():
            for m in TOKEN_RE.finditer(read(os.path.join(REPO, rel))):
                if m.group(1) != NEW_PREFIX:
                    tokens.add(m.group(0))
        print('mixin member names with a former prefix: %d' % len(tokens))
        return finish(plan)

    if finish(plan):
        return 1
    if applied(args.phase) and not args.force:
        print('phase %s is recorded as applied; --force runs it again' % args.phase)
        return 0
    rewriter = Rewriter(plan)
    if args.phase == 'main':
        apply_main(plan, rewriter)
    elif args.phase == 'tooling':
        paths = list(TOOLING_FILES) + [p for d in TOOLING_TREES if os.path.isdir(os.path.join(REPO, d))
                                       for p in walk(d)]
        changed = rewrite_files(rewriter, paths, TOOLING_SKIP)
        print('%d tooling and doc files rewritten, %d rows of %s updated'
              % (changed, update_file_moves(plan, FILE_MOVES), FILE_MOVES))
    else:
        changed = rewrite_files(rewriter, [p for d in TEST_TREES for p in walk(d)])
        print('%d test files rewritten, %d rows of %s updated'
              % (changed, update_file_moves(plan, TEST_FILE_MOVES), TEST_FILE_MOVES))
    if args.report:
        write(args.report, ''.join('prose\t%s:%d: %s\n' % l for l in rewriter.leftovers)
              + ''.join('foreign-path\t%s:%d: %s\n' % l for l in rewriter.foreign))
    print('left alone: %d mention(s) of a holder class in prose, %d name(s) behind a path that is not the class\'s '
          'package%s' % (len(rewriter.leftovers), len(rewriter.foreign),
                         ' (see %s)' % args.report if args.report else ''))
    record(args.phase)
    return 0


if __name__ == '__main__':
    sys.exit(main())
