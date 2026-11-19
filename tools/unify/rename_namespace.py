#!/usr/bin/env python3
"""Stage 2a of the SparkDLC unification: the scripted namespace rename.

Collapses the five legacy namespaces (`sparkfactionapi`, `sparkstrength`, `sparktraits`, `sparkwitch`,
`sparkassist`) into `sparkdlc`: registry ids, packet ids, component keys, role / faction / trait / skill ids,
shop-entry string ids, commands, permission nodes, lang keys, asset and data paths, config file names. It moves the
resource files, merges the lang files and the three `sounds.json`, rewrites every textual reference in sources,
resources, build tooling, docs and the local-only tests, and writes `legacy-ids.tsv` (kind, old id, new id) for the
compatibility layer of stage 2b. It never renames a Java class, a package or a `sparkwitch$...` mixin member.
See README.md next to this file.

SparkDLC 统一化第二阶段 a：脚本化的命名空间改名。把五个旧命名空间合并为 `sparkdlc`：注册 id、数据包 id、组件键、
职业 / 阵营 / 天赋 / 技能 id、商店条目字符串 id、命令、权限节点、语言键、资源与数据路径、配置文件名。脚本会移动资源文件，
合并语言文件和三个 `sounds.json`，改写源码、资源、构建工具、文档和本地测试里的全部文本引用，并生成 `legacy-ids.tsv`
（种类、旧 id、新 id）供第二阶段 b 的兼容层使用。它从不重命名 Java 类、包或 `sparkwitch$...` 形式的 mixin 成员。
见同目录 README.md。

Usage / 用法:
    python3 tools/unify/rename_namespace.py plan                  # checks + legacy-ids.tsv, nothing else / 只检查并生成表
    python3 tools/unify/rename_namespace.py apply --phase main     # resources + production and gametest sources / 资源与源码
    python3 tools/unify/rename_namespace.py apply --phase tooling  # gradle/verify, config, tools, docs / 构建工具与文档
    python3 tools/unify/rename_namespace.py apply --phase tests    # local-only src/test / 本地测试
An applied phase is recorded (applied-phases.txt, `ns-` prefix) and skipped on the next run; `--force` overrides.
已执行的阶段会被记录（applied-phases.txt，前缀 `ns-`），再次运行时跳过；`--force` 可强制执行。
"""
import argparse
import collections
import fnmatch
import json
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
NEW = 'sparkdlc'
NAMESPACES = ('sparkfactionapi', 'sparkstrength', 'sparktraits', 'sparkwitch', 'sparkassist')
NS_ALT = '(?:' + '|'.join(NAMESPACES) + ')'
# The `id(String)` helper of each legacy initializer and the namespace it used to add.
# 每个旧初始化类的 `id(String)` 辅助方法及其原先添加的命名空间。
HELPERS = {'SparkFactionApiMod': 'sparkfactionapi', 'SparkStrength': 'sparkstrength', 'SparkTraits': 'sparktraits',
           'SparkWitch': 'sparkwitch', 'SparkAssist': 'sparkassist'}
OVERRIDES_FILE = os.path.join(HERE, 'namespace-overrides.tsv')
KEEP_FILE = os.path.join(HERE, 'namespace-keep.tsv')
INVENTORY_FILE = os.path.join(HERE, 'id-inventory.tsv')
LEGACY_IDS_FILE = os.path.join(HERE, 'legacy-ids.tsv')
RESOURCE_MOVES_FILE = os.path.join(HERE, 'resource-moves.tsv')
APPLIED_FILE = os.path.join(HERE, 'applied-phases.txt')
APPLIED_TEST_FILE_REL = 'src/test/.unify-applied-phases.txt'

RESOURCE_ROOTS = ('src/main/resources', 'src/client/resources')
MERGED_ROOT = 'src/main/resources'  # the one source set that holds the merged lang files and sounds.json
LANG_LOCALES = ('en_us', 'zh_cn')
# Order of the former lang files inside the merged one (the existing sparkdlc file first).
# 合并后的语言文件里各旧文件的顺序（已有的 sparkdlc 文件在最前）。
LANG_ORDER = ('sparkdlc',) + NAMESPACES
TEXT_EXTS = ('.json', '.mcmeta', '.fsh', '.vsh', '.glsl', '.txt', '.properties', '.md', '.tsv', '.gradle', '.yml',
             '.yaml', '.py', '.java', '.toml')

# A namespace word that is a whole token: not glued to an identifier on the left, not continued by a lower-case
# letter or digit on the right (so `sparkwitch$x`, `sparkwitch_x`, `sparkwitch:x`, `sparkwitchFoo` all start here).
# 作为完整记号出现的命名空间词：左侧不与标识符相连，右侧不接小写字母或数字。
TOKEN_RE = re.compile(r'(?<![A-Za-z0-9_$])' + '(' + '|'.join(NAMESPACES) + ')' + r'(?![a-z0-9])')
PATH_RE = re.compile(r'[A-Za-z0-9_/\-]+(?:\.[A-Za-z0-9_/\-]+)*')
# Dotted words that start with a namespace but are not keys: pre-fusion package fragments quoted by tests, the
# legacy mixin config names, and developer-only JVM system properties.
# 以命名空间开头但不是键的点分词：测试引用的融合前包片段、旧 mixin 配置名，以及仅供开发者使用的 JVM 系统属性。
KEEP_FIRST_SEGMENTS = {'impl', 'component', 'client', 'role', 'api', 'mixin', 'mixins', '*',
                       'debugCautiousSound', 'decorDebug', 'noellesrolesJar'}
PLACEHOLDER = '%d'
PLACEHOLDER_RE = re.compile('(\\d+)')


def read(path):
    with open(path, encoding='utf-8', newline='') as f:
        return f.read()


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='') as f:
        f.write(text)


def read_tsv(path):
    rows = []
    for line in read(path).split('\n'):
        if not line.strip() or line.startswith('#'):
            continue
        rows.append(line.split('\t'))
    return rows[1:]  # header


def git(*args, cwd=REPO, env=None):
    return subprocess.run(('git',) + args, cwd=cwd, env=env, capture_output=True, text=True)


# --------------------------------------------------------------------------------------------------------------------
# Rules / 规则
# --------------------------------------------------------------------------------------------------------------------
class Rules:
    """The two hand-written tables: per-id exceptions to "same path under sparkdlc", and sites that keep a legacy
    name on purpose. / 两张手写表：“同路径改到 sparkdlc 下”的逐 id 例外，以及有意保留旧名字的位置。"""

    def __init__(self):
        self.id_new = {}        # (ns, path) -> new full id (equal to the old one = keep)
        self.key_new = {}       # full dotted key -> new key
        self.prefixed_new = {}  # ns_x -> new string
        self.literal_keep = []  # exact text kept everywhere
        self.path_new = {}      # exact path text -> new path text (a file that changes its source set)
        self.pre = []           # (compiled regex, replacement function) applied before the token pass
        for form, old, new, _note in (r[:4] for r in read_tsv(OVERRIDES_FILE)):
            if form == 'id':
                ns, path = old.split(':', 1)
                self.id_new[(ns, path)] = new
            elif form == 'key':
                self.key_new[old] = new
            elif form == 'prefixed':
                self.prefixed_new[old] = new
            elif form == 'literal':
                self.literal_keep.append(old)
            elif form == 'path':
                self.path_new[old] = new
            elif form.startswith('lang-'):
                pass  # used by the lang merge only / 仅用于语言文件合并
            else:
                raise SystemExit('unknown form in namespace-overrides.tsv: ' + form)
        self._compile()
        self.site_keep = []  # (path glob, compiled line regex or None)
        for row in read_tsv(KEEP_FILE):
            glob, regex = row[0], row[1]
            self.site_keep.append((glob, None if regex in ('', '*') else re.compile(regex)))

    def _compile(self):
        idc = r'[A-Za-z0-9_/\-]'
        for (ns, path), new in sorted(self.id_new.items()):
            new_ns, new_path = new.split(':', 1)
            p = re.escape(path)
            # "ns:path"
            self.pre.append((re.compile(r'(?<![A-Za-z0-9_$])%s:%s(?!%s|\.[A-Za-z0-9_])' % (ns, p, idc)),
                             lambda m, new=new: new))
            # Identifier.of("ns", "path"), also with the escaped quotes of a string that quotes source text
            self.pre.append((re.compile(r'(\\*")%s\1(\s*,\s*)\1%s\1' % (ns, p)),
                             lambda m, a=new_ns, b=new_path: '%s%s%s%s%s%s%s' % (
                                 m.group(1), a, m.group(1), m.group(2), m.group(1), b, m.group(1))))
            # SparkX.id("path"): the helper itself is renamed by its MOD_ID constant, so only a changed path matters
            if new_ns == NEW and new_path != path:
                for helper, helper_ns in HELPERS.items():
                    if helper_ns == ns:
                        self.pre.append((re.compile(r'\b%s\.id\(\s*(\\*")%s\1\s*\)' % (helper, p)),
                                         lambda m, h=helper, b=new_path: '%s.id(%s%s%s)' % (
                                             h, m.group(1), b, m.group(1))))
        for old, new in sorted(self.key_new.items()):
            self.pre.append((re.compile(r'(?<![A-Za-z0-9_.\-])%s(?![A-Za-z0-9_]|\.[A-Za-z0-9_])' % re.escape(old)),
                             lambda m, new=new: new))
        for old, new in sorted(self.prefixed_new.items()):
            self.pre.append((re.compile(r'(?<![A-Za-z0-9_$])%s(?![a-z0-9_])' % re.escape(old)),
                             lambda m, new=new: new))
        for old in self.literal_keep:
            self.pre.append((re.compile(re.escape(old)), lambda m: m.group(0)))
        for old, new in sorted(self.path_new.items()):
            self.pre.append((re.compile(re.escape(old)), lambda m, new=new: new))

    def keep_regexes(self, rel):
        """Line regexes of the keep rules that apply to `rel`; [None] = the whole file is kept."""
        found = []
        for glob, regex in self.site_keep:
            if fnmatch.fnmatch(rel, glob):
                if regex is None:
                    return [None]
                found.append(regex)
        return found


# --------------------------------------------------------------------------------------------------------------------
# The text rule / 文本规则
# --------------------------------------------------------------------------------------------------------------------
def effective_next(right):
    """First character after the token, looking through the backslashes of an escaped quote, dot or dollar."""
    i = 0
    while i < len(right) and right[i] == '\\':
        i += 1
    if i and i < len(right) and right[i] in '."$\'':
        return right[i], i
    return (right[0] if right else ''), 0


def classify(text, m):
    """Decides what one namespace token is. Returns (rule name, rewrite?).
    判断一个命名空间记号是什么。返回（规则名，是否改写）。"""
    left, right = text[max(0, m.start() - 160):m.start()], text[m.end():m.end() + 40]
    prev = left[-1] if left else ''
    nxt, skipped = effective_next(right)
    after = right[skipped + 1:] if nxt else ''
    if nxt == '$':
        return 'keep: mixin member prefix', False
    # A segment of a Java package or class path (`dev.caecorthus.sparkwitch.x`, `annina.sparkstrength`,
    # `dev/caecorthus/sparkdlc/factionapi/mixin/compat/sparkstrength/X.class`): packages are not renamed.
    # Java 包名或类路径中的一段：包不改名。
    if re.search(r'(?:caecorthus|annina)\\*[./](?:[A-Za-z0-9_$]+\\*[./])*$', left):
        return 'keep: package or class path', False
    if nxt == ':':
        return ('id' if PATH_RE.match(after) else 'id prefix'), True
    if nxt == '.':
        if re.match(r'(?:client\\*\.)?mixins\\*\.json', after):
            return 'keep: legacy mixin config name', False
        if prev != '.':
            seg = re.match(r'[A-Za-z_]+|\*', after)
            if not seg:
                return 'keep: prose', False
            if seg.group(0) in KEEP_FIRST_SEGMENTS:
                return 'keep: package fragment or system property', False
            return 'key or file starting with the namespace', True
        return 'key segment', True
    if nxt == '_':
        return 'prefixed string id', True
    if nxt == '/':
        return 'path segment', True
    if nxt == '-':
        if right.startswith('-client'):
            return 'config file name', True
        return 'keep: prose', False
    if nxt in ('"', "'"):
        if prev == '.':
            return 'key ending with the namespace', True
        if prev == '/':
            return 'path ending with the namespace', True
        if prev == nxt or (prev == '"' and nxt == '"'):
            return 'bare literal', True
        return 'keep: prose', False
    if prev == '.' and re.search(r'[A-Za-z0-9_]\\*\.$', left) and not (nxt.isalnum() or nxt == '_'):
        # `key.categories.sparkfactionapi` at the end of a backticked or bracketed key
        return 'key ending with the namespace', True
    if prev == '/' and nxt in ('`', ')', ' ', '\n', ',', ''):
        return 'path ending with the namespace', True
    return 'keep: prose', False


class Rewriter:
    def __init__(self, rules, report=None):
        self.rules = rules
        self.log = collections.Counter()
        self.report = report  # optional list of (file, rule, context) for kept tokens

    def protect(self, text, rel):
        """Replaces everything a table decides with placeholders so the token pass leaves it alone."""
        saved = []

        def stash(value):
            saved.append(value)
            return PLACEHOLDER % (len(saved) - 1)

        keeps = self.rules.keep_regexes(rel)
        if keeps:
            lines = text.split('\n')
            for i, line in enumerate(lines):
                if TOKEN_RE.search(line) and (keeps == [None] or any(r.search(line) for r in keeps)):
                    def keep_token(m):
                        self.log['keep: namespace-keep.tsv'] += 1
                        return stash(m.group(0))
                    lines[i] = TOKEN_RE.sub(keep_token, line)
            text = '\n'.join(lines)
        for regex, repl in self.rules.pre:
            def apply(m, repl=repl):
                new = repl(m)
                self.log['override: namespace-overrides.tsv' if new != m.group(0)
                         else 'keep: namespace-overrides.tsv'] += 1
                return stash(new)
            text = regex.sub(apply, text)
        return text, saved

    @staticmethod
    def restore(text, saved):
        while PLACEHOLDER_RE.search(text):
            text = PLACEHOLDER_RE.sub(lambda m: saved[int(m.group(1))], text)
        return text

    def tokens(self, text, rel):
        def one(m):
            rule, rewrite = classify(text, m)
            self.log[rule] += 1
            if not rewrite and self.report is not None:
                self.report.append((rel, rule, text[max(0, m.start() - 60):m.end() + 60].replace('\n', '\\n')))
            return NEW if rewrite else m.group(0)
        return TOKEN_RE.sub(one, text)

    def rewrite(self, text, rel, kind):
        """kind: 'text' = the whole file is data or prose; 'java' / 'python' = only strings and comments are text,
        code is left alone (it holds identifiers, not ids)."""
        if not TOKEN_RE.search(text) and not any(h + '.id(' in text for h in HELPERS):
            return dedupe_json_lists(text) if rel.endswith('.json') and '"%s"' % NEW in text else text
        text, saved = self.protect(text, rel)
        if kind == 'text':
            text = self.tokens(text, rel)
        else:
            out = []
            for region, chunk in regions(text, kind):
                if region == 'code':
                    for m in TOKEN_RE.finditer(chunk):
                        rule, rewrite = classify(chunk, m)
                        self.log['code: ' + ('WOULD REWRITE ' + rule if rewrite else rule.replace('keep: ', ''))] += 1
                        if rewrite and self.report is not None:
                            self.report.append((rel, 'code region left alone: ' + rule,
                                                chunk[max(0, m.start() - 60):m.end() + 60].replace('\n', '\\n')))
                    out.append(chunk)
                else:
                    out.append(self.tokens(chunk, rel))
            text = ''.join(out)
        text = self.restore(text, saved)
        if kind == 'java':
            text = dedupe_mod_lists(text)
        elif rel.endswith('.json'):
            text = dedupe_json_lists(text)
        return text


MOD_LIST_RE = re.compile(r'List\.of\(((?:\\*"[a-z_0-9]+\\*"\s*,\s*)+\\*"[a-z_0-9]+\\*")\)')


def dedupe_mod_lists(text):
    """`List.of("sparkwitch", "sparkstrength")` (the required mods of a Ponder demo) became
    `List.of("sparkdlc", "sparkdlc")`; one entry says the same.
    Ponder 演示的“所需模组”列表改写后会出现重复的 "sparkdlc"，保留一个即可。"""
    def one(m):
        items = re.findall(r'\\*"[a-z_0-9]+\\*"', m.group(1))
        if sum(1 for i in items if i.strip('\\"') == NEW) < 2:
            return m.group(0)
        seen, kept = set(), []
        for item in items:
            if item not in seen:
                seen.add(item)
                kept.append(item)
        return 'List.of(' + ', '.join(kept) + ')'
    return MOD_LIST_RE.sub(one, text)


JSON_LIST_RE = re.compile(r'\[(\s*)("[a-z_0-9]+"(?:\s*,\s*"[a-z_0-9]+")+)(\s*)\]')


def dedupe_json_lists(text):
    """The same for a JSON list of mod ids (`requiredModIds` of a guidebook page): a repeated "sparkdlc" is dropped,
    the layout of the list is kept. / JSON 的模组 id 列表同理：去掉重复的 "sparkdlc"，保留列表的排版。"""
    def one(m):
        parts = re.split(r'(\s*,\s*)', m.group(2))
        items, seps = parts[0::2], parts[1::2]
        if items.count('"%s"' % NEW) < 2:
            return m.group(0)
        out, seen = '', set()
        for i, item in enumerate(items):
            if item in seen:
                continue
            out += (seps[i - 1] if out else '') + item
            seen.add(item)
        return '[' + m.group(1) + out + m.group(3) + ']'
    return JSON_LIST_RE.sub(one, text)


def regions(text, kind):
    """Splits source text into ('code' | 'text', chunk) pieces; 'text' = string literals and comments."""
    i, n, start = 0, len(text), 0
    out = []

    def flush(end, region):
        nonlocal start
        if end > start:
            out.append((region, text[start:end]))
        start = end

    while i < n:
        c = text[i]
        two = text[i:i + 2]
        three = text[i:i + 3]
        if kind == 'java' and two == '//' or kind == 'python' and c == '#':
            flush(i, 'code')
            j = text.find('\n', i)
            i = n if j < 0 else j
            flush(i, 'text')
        elif kind == 'java' and two == '/*':
            flush(i, 'code')
            j = text.find('*/', i + 2)
            i = n if j < 0 else j + 2
            flush(i, 'text')
        elif three in ('"""', "'''") and (kind == 'python' or three == '"""'):
            flush(i, 'code')
            j = i + 3
            while j < n and text[j:j + 3] != three:
                j += 2 if text[j] == '\\' else 1
            i = min(n, j + 3)
            flush(i, 'text')
        elif c == '"' or c == "'":
            flush(i, 'code')
            j = i + 1
            while j < n and text[j] != c and text[j] != '\n':
                j += 2 if text[j] == '\\' else 1
            i = min(n, j + 1)
            flush(i, 'text')
        else:
            i += 1
    flush(n, 'code')
    return out


# --------------------------------------------------------------------------------------------------------------------
# Resource files / 资源文件
# --------------------------------------------------------------------------------------------------------------------
def walk(rel_dir):
    base = os.path.join(REPO, rel_dir)
    for d, dirs, files in os.walk(base):
        dirs[:] = sorted(x for x in dirs if x != '__pycache__')
        for f in sorted(files):
            if f != '.DS_Store':
                yield os.path.relpath(os.path.join(d, f), REPO).replace(os.sep, '/')


def map_resource_path(rel):
    """New path of a resource file: a path segment that IS a legacy namespace becomes `sparkdlc` (namespace folders
    under assets/ and data/, and the per-namespace folders inside the guidebook and Ponder trees), and a post
    shader program named `<ns>_x` under assets/minecraft becomes `sparkdlc_x`.
    资源文件的新路径：等于旧命名空间的路径段改为 `sparkdlc`；assets/minecraft 下名为 `<ns>_x` 的后处理着色器改为 `sparkdlc_x`。"""
    parts = rel.split('/')
    parts = [NEW if p in NAMESPACES else p for p in parts]
    name = parts[-1]
    for ns in NAMESPACES:
        if name.startswith(ns + '_') and '/shaders/' in rel:
            parts[-1] = NEW + name[len(ns):]
    return '/'.join(parts)


def is_lang(rel):
    return re.search(r'/assets/[a-z]+/lang/(%s)\.json$' % '|'.join(LANG_LOCALES), rel) is not None


def is_sounds(rel):
    return re.search(r'/assets/%s/sounds\.json$' % NS_ALT, rel) is not None


def plan_resources():
    """(moves, merges, problems): moves = [(old, new)], merges = {new path: [old paths in order]}."""
    moves, merges, problems = [], collections.OrderedDict(), []
    targets = collections.defaultdict(list)
    for root in RESOURCE_ROOTS:
        for rel in walk(root):
            inside = rel[len(root) + 1:]
            if is_lang(rel) or is_sounds(rel):
                new = MERGED_ROOT + '/' + map_resource_path(inside)
                merges.setdefault(new, []).append(rel)
                continue
            new = root + '/' + map_resource_path(inside)
            targets[new[new.index('/resources/') + 11:]].append(rel)
            if new != rel:
                moves.append((rel, new))
    for inside, olds in sorted(targets.items()):
        if len(olds) > 1:
            problems.append('resource path collision: %s <- %s' % (inside, ', '.join(olds)))
    for new, olds in merges.items():
        inside = new[new.index('/resources/') + 11:]
        if inside in targets:
            problems.append('merged file collides with a plain file: ' + inside)
        order = LANG_ORDER if is_lang(new) else NAMESPACES
        olds.sort(key=lambda p: order.index(p.split('/assets/')[1].split('/')[0]))
    return moves, merges, problems


LANG_LINE_RE = re.compile(r'^(\s*)("(?:[^"\\]|\\.)*")(\s*:\s*)("(?:[^"\\]|\\.)*")\s*,?\s*$')


def merge_lang(rewriter, new_rel, olds, problems, notes):
    """Text-level merge so that values keep their exact escapes: the entries of every former file in file order,
    keys rewritten by the common text rule, collisions resolved by namespace-overrides.tsv (`lang-value`,
    `lang-drop` rows) or reported.
    文本级合并，值的转义原样保留：按文件顺序拼接各旧文件的条目，键用统一文本规则改写，冲突按表处理或报告。"""
    chosen = collections.OrderedDict()  # new key -> (value text, source)
    groups = collections.OrderedDict()
    locale = os.path.basename(new_rel)[:-5]
    forced = {r[1]: r[2] for r in read_tsv(OVERRIDES_FILE) if r[0] == 'lang-value:' + locale}
    dropped = {r[1] for r in read_tsv(OVERRIDES_FILE) if r[0] == 'lang-drop'}
    for old in olds:
        ns = old.split('/assets/')[1].split('/')[0]
        for line in read(os.path.join(REPO, old)).split('\n'):
            if line.strip() in ('{', '}', ''):
                continue
            m = LANG_LINE_RE.match(line)
            if not m:
                problems.append('%s: not a one-line lang entry: %s' % (old, line[:80]))
                continue
            old_key = json.loads(m.group(2))
            if old_key in dropped:
                notes.append('lang %s: dropped %s (%s)' % (locale, old_key, ns))
                continue
            new_key = rewriter.rewrite(m.group(2), old, 'text')
            key = json.loads(new_key)
            value = m.group(4)
            if key in forced:
                value = json.dumps(forced[key], ensure_ascii=False)
            if key in chosen:
                if chosen[key][0] == value:
                    notes.append('lang %s: %s defined again with the same text (%s, first in %s): kept once'
                                 % (locale, key, ns, chosen[key][1]))
                else:
                    problems.append('lang %s: %s has two texts (%s: %s | %s: %s)'
                                    % (locale, key, chosen[key][1], chosen[key][0][:40], ns, value[:40]))
                continue
            chosen[key] = (value, ns)
            groups.setdefault(ns, []).append('  %s: %s' % (new_key, value))
    lines = [entry for ns in groups for entry in groups[ns]]
    text = '{\n' + ',\n'.join(lines) + '\n}\n'
    json.loads(text)
    return text, len(lines)


def merge_sounds(rewriter, olds, problems):
    """Concatenates the bodies of the three sounds.json objects (no event name occurs twice)."""
    bodies, seen = [], {}
    for old in olds:
        text = rewriter.rewrite(read(os.path.join(REPO, old)), old, 'text')
        for name in json.loads(text):
            if name in seen:
                problems.append('sound event %s is in %s and %s' % (name, seen[name], old))
            seen[name] = old
        body = text.strip()
        assert body.startswith('{') and body.endswith('}')
        bodies.append(body[1:-1].strip('\n').rstrip())
    text = '{\n' + ',\n'.join(bodies) + '\n}\n'
    json.loads(text)
    return text, len(seen)


def apply_resources(rewriter, problems, notes):
    moves, merges, more = plan_resources()
    problems.extend(more)
    if problems:
        return 0
    write(RESOURCE_MOVES_FILE, 'old_path\tnew_path\n' + ''.join(
        '%s\t%s\n' % mv for mv in moves + [(o, n) for n, olds in merges.items() for o in olds if o != n]))
    merged = {}
    for new, olds in merges.items():
        if is_lang(new):
            merged[new], count = merge_lang(rewriter, new, olds, problems, notes)
            notes.append('merged %d lang files into %s: %d keys' % (len(olds), new, count))
        else:
            merged[new], count = merge_sounds(rewriter, olds, problems)
            notes.append('merged %d sounds.json into %s: %d events' % (len(olds), new, count))
    if problems:
        return 0
    for old, new in moves:
        os.makedirs(os.path.dirname(os.path.join(REPO, new)), exist_ok=True)
        r = git('mv', old, new)
        if r.returncode:
            problems.append('git mv %s: %s' % (old, r.stderr.strip()))
    for new, olds in merges.items():
        for old in olds:
            if old != new:
                git('rm', '-q', old)
        write(os.path.join(REPO, new), merged[new])
        git('add', new)
    for root in RESOURCE_ROOTS:  # drop the emptied namespace folders
        for d, _dirs, _files in os.walk(os.path.join(REPO, root), topdown=False):
            if not os.listdir(d):
                os.rmdir(d)
    return len(moves)


# --------------------------------------------------------------------------------------------------------------------
# Text rewriting per tree / 各目录的文本改写
# --------------------------------------------------------------------------------------------------------------------
def kind_of(rel):
    if rel.endswith('.java'):
        return 'java'
    if rel.endswith('.py'):
        return 'python'
    return 'text'


def rewrite_files(rewriter, paths, skip=()):
    changed = 0
    for rel in paths:
        if not rel.endswith(TEXT_EXTS) or any(fnmatch.fnmatch(rel, g) for g in skip):
            continue
        path = os.path.join(REPO, rel)
        try:
            text = read(path)
        except UnicodeDecodeError:
            continue
        new = rewriter.rewrite(text, rel, kind_of(rel))
        if rel == POLICY_TABLE and new != text:
            # the table must stay sorted by payload id / 该表必须保持按数据包 id 排序
            lines = new.rstrip('\n').split('\n')
            new = '\n'.join([lines[0]] + sorted(lines[1:], key=lambda line: line.split('\t')[0])) + '\n'
        if new != text:
            write(path, new)
            changed += 1
    return changed


MAIN_TREES = ('src/main/java', 'src/client/java', 'src/gametest/java', 'src/main/resources', 'src/client/resources',
              'src/gametest/resources')
TOOLING_FILES = ('build.gradle', 'tools/payload_policy.py', 'CONTEXT.md', 'README.md', 'THIRD_PARTY_NOTICES.md')
TOOLING_TREES = ('gradle/verify', 'config', '.github', 'docs/assist', 'docs/factionapi', 'docs/witch')
# History: these describe the separate mods or the first fusion and keep the names of that time.
# 历史文档：描述独立模组时期或第一次融合，保留当时的名字。
TOOLING_SKIP = ('docs/*/DOWNSTREAM_MIGRATION_NOTES.md', 'docs/factionapi/SPARK_MOD_FEATURES.md')
POLICY_TABLE = 'config/payload-policy/actions.tsv'
TEST_TREES = ('src/test/java', 'src/test/strength', 'src/test/resources')


# --------------------------------------------------------------------------------------------------------------------
# legacy-ids.tsv
# --------------------------------------------------------------------------------------------------------------------
def new_id_for(rules, kind, old):
    """The new name of one inventory row, by the same rules the text pass applies."""
    if old in rules.key_new:
        return rules.key_new[old]
    if old in rules.prefixed_new:
        return rules.prefixed_new[old]
    m = re.match('(%s):(.*)$' % NS_ALT, old)
    if m and (m.group(1), m.group(2)) in rules.id_new:
        return rules.id_new[(m.group(1), m.group(2))]
    probe = Rewriter(rules)
    quoted = probe.rewrite('"%s"' % old, '', 'text')
    return quoted[1:-1]


def write_legacy_ids(rules, problems):
    rows, seen_new = [], collections.defaultdict(list)
    for kind, old, note, forced in ((r + ['', '', ''])[:4] for r in read_tsv(INVENTORY_FILE)):
        new = forced or new_id_for(rules, kind, old)
        rows.append((kind, old, new, note))
        if new != old:
            seen_new[(kind, new)].append(old)
    for (kind, new), olds in sorted(seen_new.items()):
        if len(olds) > 1 and not any('merged' in r[3] or 'share' in r[3] for r in rows if r[0] == kind and r[2] == new):
            problems.append('two %s ids become %s: %s' % (kind, new, ', '.join(olds)))
    write(LEGACY_IDS_FILE, 'kind\told_id\tnew_id\tnote\n' + ''.join('%s\t%s\t%s\t%s\n' % r for r in rows))
    return collections.Counter(r[0] for r in rows)


# --------------------------------------------------------------------------------------------------------------------
def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('command', choices=('plan', 'apply'))
    ap.add_argument('--phase', choices=('main', 'tooling', 'tests'))
    ap.add_argument('--force', action='store_true',
                    help='apply a phase again although it is recorded as applied / 即使已记录为执行过也再次执行')
    ap.add_argument('--report', help='write every token that was left alone, with context, to this file / '
                                     '把所有未改写的记号及上下文写入该文件')
    args = ap.parse_args()
    rules = Rules()
    problems, notes = [], []
    counts = write_legacy_ids(rules, problems)
    if args.command == 'plan':
        moves, merges, more = plan_resources()
        problems.extend(more)
        print('resource files to move: %d; merged files: %d (from %d)'
              % (len(moves), len(merges), sum(len(v) for v in merges.values())))
        print('legacy-ids.tsv: ' + ', '.join('%s %d' % kv for kv in sorted(counts.items())))
        for p in problems:
            print('PROBLEM:', p)
        return 1 if problems else 0
    if not args.phase:
        ap.error('apply needs --phase')
    applied_path = os.path.join(REPO, APPLIED_TEST_FILE_REL) if args.phase == 'tests' else APPLIED_FILE
    applied = read(applied_path).split() if os.path.isfile(applied_path) else []
    if 'ns-' + args.phase in applied and not args.force:
        print('phase %s is recorded as applied in %s: nothing to do' % (args.phase, os.path.relpath(applied_path, REPO)))
        return 0
    if problems:
        for p in problems:
            print('PROBLEM:', p)
        return 1
    report = [] if args.report else None
    rewriter = Rewriter(rules, report)
    if args.phase == 'main':
        moved = apply_resources(rewriter, problems, notes)
        if not problems:
            changed = rewrite_files(rewriter, [p for d in MAIN_TREES for p in walk(d)])
            print('moved %d resource files, rewrote %d files' % (moved, changed))
    elif args.phase == 'tooling':
        paths = list(TOOLING_FILES) + [p for d in TOOLING_TREES if os.path.isdir(os.path.join(REPO, d)) for p in walk(d)]
        print('rewrote %d tooling and doc files' % rewrite_files(rewriter, paths, TOOLING_SKIP))
    else:
        paths = [p for d in TEST_TREES for p in walk(d)]
        print('rewrote %d test files' % rewrite_files(rewriter, paths))
    for n in notes:
        print('note:', n)
    for k, v in sorted(rewriter.log.items()):
        print('%7d  %s' % (v, k))
    if report is not None:
        write(args.report, ''.join('%s\t%s\t%s\n' % r for r in report))
    for p in problems:
        print('PROBLEM:', p)
    if not problems and 'ns-' + args.phase not in applied:
        write(applied_path, ''.join(x + '\n' for x in applied + ['ns-' + args.phase]))
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())
