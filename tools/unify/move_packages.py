#!/usr/bin/env python3
"""Stage 1 of the SparkDLC unification: the scripted package move.

Moves every Java class from the five legacy module packages (`dev.caecorthus.sparkdlc.{factionapi,strength,traits,
witch,assist}`) to the function-first layout described by `package-map.tsv`, merges the nine mixin configs into two,
and rewrites every textual reference (package/import lines, fully-qualified names, slashed names, source paths,
module-relative path pieces, config-relative mixin names) in sources, resources, build tooling, docs and the
local-only tests. It never renames a class, an id or a namespace. See README.md next to this file.

SparkDLC 统一化第一阶段：脚本化的包搬迁。按 `package-map.tsv` 把五个旧模块包里的所有 Java 类搬到按功能组织的新布局，
把九个 mixin 配置合并为两个，并改写源码、资源、构建工具、文档和本地测试里的全部文本引用（package/import 行、全限定名、
斜杠形式类名、源码路径、模块相对路径片段、相对配置包的 mixin 名）。它从不重命名类、id 或命名空间。见同目录 README.md。

Usage / 用法:
    python3 tools/unify/move_packages.py plan                 # write file-moves.tsv, print checks / 生成清单并打印检查
    python3 tools/unify/move_packages.py apply --phase code    # non-mixin classes / 非 mixin 类
    python3 tools/unify/move_packages.py apply --phase mixins  # mixin classes + the two merged configs / mixin 类与合并配置
    python3 tools/unify/move_packages.py apply --phase tooling # gradle/verify, tools, config, docs / 构建工具与文档
    python3 tools/unify/move_packages.py apply --phase tests   # local-only src/test / 本地测试
Every phase is idempotent, and a phase that was applied is recorded (applied-phases.txt) and skipped on the next
run. / 每个阶段都是幂等的；已执行的阶段会被记录（applied-phases.txt），再次运行时跳过。
"""
import argparse
import collections
import json
import os
import posixpath
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(os.path.dirname(HERE))
ROOT_PKG = 'dev.caecorthus.sparkdlc'
ROOT_DIR = ROOT_PKG.replace('.', '/')
MODULES = ('factionapi', 'strength', 'traits', 'witch', 'assist')
PROD_ROOTS = ('src/main/java', 'src/client/java')
TEST_ROOTS = ('src/test/java', 'src/test/strength/java')
JAVA_ROOTS = PROD_ROOTS + ('src/gametest/java',) + TEST_ROOTS
RESOURCE_ROOTS = ('src/main/resources', 'src/client/resources', 'src/test/resources')
MAP_FILE = os.path.join(HERE, 'package-map.tsv')
MOVES_FILE = os.path.join(HERE, 'file-moves.tsv')
MOVES_TEST_FILE_REL = 'src/test/.unify-file-moves.tsv'  # local-only half of the manifest / 清单的本地测试部分
# Phases already applied. After the move a few verify scripts and tests name the LEGACY modules on purpose (they
# look files up in the manifest), so a finished phase must not rewrite text a second time.
# 已执行的阶段。搬迁之后，少数校验脚本和测试有意使用旧模块名（它们用清单查找文件），因此已完成的阶段不能再次改写文本。
APPLIED_FILE = os.path.join(HERE, 'applied-phases.txt')
APPLIED_TEST_FILE_REL = 'src/test/.unify-applied-phases.txt'

# The nine legacy configs in today's fabric.mod.json order; the merged lists keep this order per side.
# 九个旧配置，按当前 fabric.mod.json 的顺序；合并后的列表在每一侧都保持该顺序。
LEGACY_CONFIGS = [
    ('src/client/resources/sparkassist.client.mixins.json', 'assist.client.mixin', 'client'),
    ('src/main/resources/sparkfactionapi.mixins.json', 'factionapi.mixin', 'mixins'),
    ('src/client/resources/sparkfactionapi.client.mixins.json', 'factionapi.client.mixin', 'client'),
    ('src/main/resources/sparkstrength.mixins.json', 'strength.mixin', 'mixins'),
    ('src/client/resources/sparkstrength.client.mixins.json', 'strength.client.mixin', 'client'),
    ('src/main/resources/sparktraits.mixins.json', 'traits.mixin', 'mixins'),
    ('src/client/resources/sparktraits.client.mixins.json', 'traits.client.mixin', 'client'),
    ('src/main/resources/sparkwitch.mixins.json', 'witch.mixin', 'mixins'),
    ('src/client/resources/sparkwitch.client.mixins.json', 'witch.client.mixin', 'client'),
]
MERGED = {
    'mixins': ('src/main/resources/sparkdlc.mixins.json', 'mixin'),
    'client': ('src/client/resources/sparkdlc.client.mixins.json', 'client.mixin'),
}
COMPOSITE_PLUGIN = 'client.SparkDlcClientMixinPlugin'
LEGACY_CONFIG_PKGS = {pkg: MERGED[key][1] for _, pkg, key in LEGACY_CONFIGS}
CONFIG_NAME_RE = re.compile(r'(?<![\w.])spark(?:(?:factionapi|strength|traits|witch)|(?:factionapi|strength|traits|witch|assist)(\.client))\.mixins\.json')

IDENT = r'[A-Za-z_$][\w$]*'


def read(path):
    with open(path, encoding='utf-8', newline='') as f:
        return f.read()


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='') as f:
        f.write(text)


# ---------------------------------------------------------------------------------------------------------------
# Rules / 规则
# ---------------------------------------------------------------------------------------------------------------
class Rules:
    def __init__(self, path=MAP_FILE):
        self.rules = []
        for line in read(path).splitlines():
            if not line or line.startswith('#') or line.startswith('old_prefix\t'):
                continue
            cols = line.split('\t')
            cols += [''] * (4 - len(cols))
            old, flt, new = cols[0], cols[1], cols[2]
            self.rules.append((old, re.compile(flt) if flt else None, new))
        self.plain = {old: new for old, flt, new in self.rules if flt is None}
        self.filtered = collections.defaultdict(list)
        for old, flt, new in self.rules:
            if flt is not None:
                self.filtered[old].append((flt, new))

    def resolve(self, pkg, cls=None):
        """New package for (old package relative to the root, simple class name). / 旧包与类名对应的新包。"""
        if cls is not None:
            for flt, new in self.filtered.get(pkg, ()):
                if flt.search(cls):
                    return new
        parts = pkg.split('.') if pkg else []
        for k in range(len(parts), 0, -1):
            pre = '.'.join(parts[:k])
            if pre in self.plain:
                new = self.plain[pre]
                rest = parts[k:]
                return '.'.join(([new] if new else []) + rest)
        return pkg


def is_old(pkg):
    return pkg.split('.', 1)[0] in MODULES


# ---------------------------------------------------------------------------------------------------------------
# Lexer: splits Java text into code / comment / string regions. / 词法：把 Java 文本切成代码、注释、字符串区域。
# ---------------------------------------------------------------------------------------------------------------
def java_regions(text):
    out = []
    i, n, start = 0, len(text), 0

    def flush(end):
        if end > start:
            out.append(('code', start, end))

    while i < n:
        c = text[i]
        if c == '/' and text.startswith('//', i):
            flush(i)
            j = text.find('\n', i)
            j = n if j < 0 else j
            out.append(('comment', i, j))
            i = start = j
        elif c == '/' and text.startswith('/*', i):
            flush(i)
            j = text.find('*/', i + 2)
            j = n if j < 0 else j + 2
            out.append(('comment', i, j))
            i = start = j
        elif c == '"' and text.startswith('"""', i):
            flush(i)
            j = i + 3
            while j < n and not (text.startswith('"""', j) and text[j - 1] != '\\'):
                j += 1
            j = min(n, j + 3)
            out.append(('string', i, j))
            i = start = j
        elif c == '"':
            flush(i)
            j = i + 1
            while j < n and text[j] != '"' and text[j] != '\n':
                j += 2 if text[j] == '\\' else 1
            j = min(n, j + 1)
            out.append(('string', i, j))
            i = start = j
        elif c == "'":
            flush(i)
            j = i + 1
            while j < n and text[j] != "'" and text[j] != '\n':
                j += 2 if text[j] == '\\' else 1
            j = min(n, j + 1)
            out.append(('char', i, j))
            i = start = j
        else:
            i += 1
    flush(n)
    return out


PKG_RE = re.compile(r'^\s*package\s+([\w.]+)\s*;', re.M)


def declared_package(text):
    for kind, a, b in java_regions(text):
        if kind == 'code':
            m = PKG_RE.search(text, a, b)
            if m:
                return m.group(1), m
            if text[a:b].strip():
                # first code that is not a package line: default package / 没有 package 行
                m2 = re.search(r'\b(import|class|interface|enum|record|@interface)\b', text[a:b])
                if m2:
                    return None, None
    return None, None


# ---------------------------------------------------------------------------------------------------------------
# Plan / 清单
# ---------------------------------------------------------------------------------------------------------------
class Entry:
    __slots__ = ('root', 'old_pkg', 'cls', 'new_pkg', 'old_path', 'phase')

    def __init__(self, root, old_pkg, cls, new_pkg, old_path, phase):
        self.root, self.old_pkg, self.cls, self.new_pkg, self.old_path, self.phase = \
            root, old_pkg, cls, new_pkg, old_path, phase

    @property
    def old_fq(self):
        return (self.old_pkg + '.' if self.old_pkg else '') + self.cls

    @property
    def new_fq(self):
        return (self.new_pkg + '.' if self.new_pkg else '') + self.cls

    @property
    def new_path(self):
        return posixpath.join(self.root, ROOT_DIR, self.new_pkg.replace('.', '/'), self.cls + '.java') \
            if self.new_pkg else posixpath.join(self.root, ROOT_DIR, self.cls + '.java')


def phase_of(root, old_pkg):
    if root in TEST_ROOTS:
        return 'tests'
    return 'mixins' if 'mixin' in old_pkg.split('.') else 'code'


def scan(rules):
    """Entries for every Java file that still sits in a legacy module package. / 仍在旧模块包里的每个 Java 文件。"""
    entries = []
    for root in JAVA_ROOTS:
        base = os.path.join(REPO, root)
        for d, dirs, fs in os.walk(base):
            dirs.sort()
            for f in sorted(fs):
                if not f.endswith('.java') or f in ('package-info.java', 'module-info.java'):
                    continue
                path = os.path.join(d, f)
                pkg, _ = declared_package(read(path))
                if pkg is None or not (pkg == ROOT_PKG or pkg.startswith(ROOT_PKG + '.')):
                    continue
                rel = pkg[len(ROOT_PKG):].lstrip('.')
                if not is_old(rel):
                    continue
                cls = f[:-5]
                entries.append(Entry(root, rel, cls, rules.resolve(rel, cls), os.path.relpath(path, REPO),
                                     phase_of(root, rel)))
    return entries


def load_manifest():
    entries = []
    for path in (MOVES_FILE, os.path.join(REPO, MOVES_TEST_FILE_REL)):
        if not os.path.isfile(path):
            continue
        for line in read(path).splitlines()[1:]:
            old_path, new_path, old_fq, new_fq, root, phase = line.split('\t')
            old_pkg, _, cls = old_fq.rpartition('.')
            new_pkg = new_fq.rpartition('.')[0]
            entries.append(Entry(root, old_pkg, cls, new_pkg, old_path, phase))
    return entries


def save_manifest(entries):
    head = 'old_path\tnew_path\told_fqcn\tnew_fqcn\tsource_root\tphase\n'
    for path, keep in ((MOVES_FILE, lambda e: e.root not in TEST_ROOTS),
                       (os.path.join(REPO, MOVES_TEST_FILE_REL), lambda e: e.root in TEST_ROOTS)):
        rows = sorted((e.old_path, e.new_path, e.old_fq, e.new_fq, e.root, e.phase) for e in entries if keep(e))
        if rows or os.path.isfile(path):
            write(path, head + ''.join('\t'.join(r) + '\n' for r in rows))


CODE_IDENT_RE = re.compile(r'(?<![\w$.])([A-Z][\w$]*)')


def code_identifiers(text):
    """Capitalised identifiers used in code and not qualified by a dot. / 代码中未被点号限定的大写开头标识符。"""
    names = set()
    for kind, a, b in java_regions(text):
        if kind == 'code':
            names.update(CODE_IDENT_RE.findall(text[a:b]))
    return names


def place_tests(entries, rules):
    """A test in a package that the map splits follows the production classes it names without an import.

    映射拆分了某个包时，测试跟随它不经 import 直接使用的那些生产类所在的新包。
    """
    by_pkg = collections.defaultdict(dict)
    for e in entries:
        if e.root in PROD_ROOTS:
            by_pkg[e.old_pkg][e.cls] = e.new_pkg
    notes = []
    for e in entries:
        if e.root != 'src/test/java':
            continue
        siblings = by_pkg.get(e.old_pkg, {})
        targets = set(siblings.values())
        if len(targets) <= 1:
            if len(targets) == 1 and e.old_pkg not in rules.filtered:
                continue
            if len(targets) == 1:
                e.new_pkg = next(iter(targets))
                continue
            continue
        path = os.path.join(REPO, e.old_path)
        if not os.path.isfile(path):
            continue
        text = read(path)
        used = code_identifiers(text)
        imported = set(re.findall(r'^\s*import\s+(?:static\s+)?[\w.]*?\.(\w+)\s*;', text, re.M))
        votes = collections.Counter()
        for name in used - imported:
            if name in siblings:
                votes[siblings[name]] += 1
        # the class under test is usually the test's own name minus a suffix / 被测类通常是测试名去掉后缀
        for name in siblings:
            if e.cls.startswith(name):
                votes[siblings[name]] += len(name) / 10.0
        if votes:
            best = max(sorted(votes), key=lambda k: votes[k])
            if best != e.new_pkg:
                notes.append((e.old_fq, e.new_pkg, best))
            e.new_pkg = best
    return notes


def build_plan(rules):
    merged = {}
    for e in load_manifest():
        merged[(e.root, e.old_fq)] = e
    for e in scan(rules):
        merged[(e.root, e.old_fq)] = e
    entries = sorted(merged.values(), key=lambda e: (e.root, e.old_fq))
    return entries, place_tests(entries, rules)


# ---------------------------------------------------------------------------------------------------------------
# Mapper: old names -> new names for a set of active phases. / 映射器：在给定阶段集合下把旧名映射到新名。
# ---------------------------------------------------------------------------------------------------------------
def lca(paths):
    """Longest common ancestor of dotted or slashed names given as segment lists. / 段列表的最长公共祖先。"""
    it = iter(paths)
    try:
        cur = list(next(it))
    except StopIteration:
        return None
    for p in it:
        k = 0
        while k < len(cur) and k < len(p) and cur[k] == p[k]:
            k += 1
        cur = cur[:k]
    return cur


class Mapper:
    def __init__(self, entries, rules, phases):
        self.rules = rules
        self.phases = set(phases)
        self.entries = entries
        self.cls = {}          # old fqcn (relative) -> entry (production wins over tests) / 旧全名 -> 条目
        for e in entries:
            if e.old_fq not in self.cls or e.root in PROD_ROOTS:
                self.cls[e.old_fq] = e
        self.by_pkg = collections.defaultdict(list)   # old pkg -> entries directly in it / 旧包 -> 直接成员
        for e in entries:
            self.by_pkg[e.old_pkg].append(e)
        self.known_pkgs = set()
        for pkg in self.by_pkg:
            parts = pkg.split('.')
            for k in range(1, len(parts) + 1):
                self.known_pkgs.add('.'.join(parts[:k]))
        self.log = collections.Counter()
        self._lca_cache = {}
        # suffix indexes over production files for module-relative pieces / 生产文件的后缀索引
        self.file_suffix = collections.defaultdict(list)
        self.dir_suffix = collections.defaultdict(set)
        for e in entries:
            if e.root not in PROD_ROOTS:
                continue
            segs = e.old_pkg.split('.') + [e.cls]
            for k in range(len(segs)):
                self.file_suffix['/'.join(segs[k:])].append(e)
            dsegs = e.old_pkg.split('.')
            for end in range(1, len(dsegs) + 1):
                for k in range(end):
                    self.dir_suffix['/'.join(dsegs[k:end])].add((e.root, '.'.join(dsegs[:end])))

    def active(self, e):
        return e.phase in self.phases

    def new_pkg(self, e):
        return e.new_pkg if self.active(e) else e.old_pkg

    def new_fq(self, e):
        return e.new_fq if self.active(e) else e.old_fq

    def subtree_lca(self, pkg, roots=PROD_ROOTS):
        if pkg in MODULES:
            # a module root has no single successor: its files are spread over the whole tree
            # 模块根目录没有唯一的后继：其文件散布在整棵树中
            return ''
        key = (pkg, roots)
        if key not in self._lca_cache:
            pre = pkg + '.'
            news = [self.new_pkg(e).split('.') if self.new_pkg(e) else []
                    for e in self.entries
                    if e.root in roots and (e.old_pkg == pkg or e.old_pkg.startswith(pre))]
            got = lca(news)
            self._lca_cache[key] = None if got is None else '.'.join(got)
        return self._lca_cache[key]

    def map_dir(self, pkg, roots=PROD_ROOTS):
        """New directory for an old directory: the common ancestor of everything that was below it.

        旧目录对应的新目录：原先位于其下的所有文件的新位置的公共祖先。
        """
        if pkg in LEGACY_CONFIG_PKGS:
            # the plugins leave these packages, the mixins keep a common root / 插件离开这些包，mixin 仍有共同的根
            return LEGACY_CONFIG_PKGS[pkg] if 'mixins' in self.phases else pkg
        got = self.subtree_lca(pkg, roots)
        if got is None:
            got = self.subtree_lca(pkg, JAVA_ROOTS)
        if got is None:
            got = self.map_unknown_pkg(pkg)
        return got

    def map_unknown_pkg(self, pkg):
        e_phase = phase_of('src/main/java', pkg)
        return self.rules.resolve(pkg) if e_phase in self.phases else pkg

    def map_pkg(self, pkg):
        """New package for a bare old package name in dotted form. / 点号形式的旧包名对应的新包。"""
        if pkg in LEGACY_CONFIG_PKGS:
            return LEGACY_CONFIG_PKGS[pkg] if 'mixins' in self.phases else pkg
        direct = {self.new_pkg(e) for e in self.by_pkg.get(pkg, ()) if e.root in PROD_ROOTS}
        if not direct:
            direct = {self.new_pkg(e) for e in self.by_pkg.get(pkg, ())}
        if len(direct) == 1:
            return next(iter(direct))
        if len(direct) > 1:
            self.log['split-package token: ' + pkg] += 1
            return self.map_unknown_pkg(pkg)
        return self.map_dir(pkg)

    def resolve_segs(self, segs, as_dir=False, roots=PROD_ROOTS):
        """Map a name given as segments after the root package; None when nothing changes.

        把根包之后的名字段映射为新名字段；没有变化时返回 None。
        """
        if not segs or segs[0] not in MODULES:
            return None
        for k in range(len(segs), 0, -1):
            last, dollar, inner = segs[k - 1].partition('$')
            key = '.'.join(segs[:k - 1] + [last])
            e = self.cls.get(key)
            if e is not None:
                new = self.new_fq(e).split('.')
                new[-1] += dollar + inner
                return new + segs[k:]
        for k in range(len(segs), 0, -1):
            pkg = '.'.join(segs[:k])
            if pkg in self.known_pkgs:
                rest = segs[k:]
                if rest and rest[0][:1].isupper():
                    cls = rest[0].split('$')[0]
                    new = self.rules.resolve(pkg, cls) if phase_of('src/main/java', pkg) in self.phases else pkg
                elif as_dir or rest:
                    new = self.map_dir(pkg, roots)
                else:
                    new = self.map_pkg(pkg)
                return (new.split('.') if new else []) + rest
        return None

    # -- full names ------------------------------------------------------------------------------------------
    def rewrite_full(self, text):
        """Rewrite every `dev.caecorthus.sparkdlc.<module>...` name in dotted, slashed and regex-escaped form.

        改写所有点号、斜杠及正则转义形式的 `dev.caecorthus.sparkdlc.<module>...` 名字。
        """
        mod = '(?:' + '|'.join(MODULES) + ')'
        for sep_re, sep in ((r'\.', '.'), (r'/', '/'), (r'\\\\\.', '\\\\.'), (r'\\\.', '\\.')):
            root = sep_re.join(('dev', 'caecorthus', 'sparkdlc'))
            pre = r'(?P<pre>src/(?:main|client|test|gametest)/java/|src/test/strength/java/)?' if sep == '/' else ''
            rx = re.compile(pre + r'(?<![\w$])' + root + '(?P<segs>(?:' + sep_re + IDENT + ')+)')
            first = re.compile(sep_re + mod + r'(?![\w$])')

            def sub(m, sep=sep, sep_re=sep_re, first=first):
                if not first.match(m.group('segs')):
                    return m.group(0)
                segs = re.split(sep_re, m.group('segs'))[1:]
                roots = PROD_ROOTS
                as_dir = sep == '/'
                if sep == '/' and m.group('pre'):
                    roots = (m.group('pre').rstrip('/'),)
                    if roots[0] not in JAVA_ROOTS:
                        roots = PROD_ROOTS
                new = self.resolve_segs(segs, as_dir=as_dir, roots=roots)
                if new is None or new == segs:
                    return m.group(0)
                head = m.group(0)[:m.start('segs') - m.start(0)]
                return head + ''.join(sep + s for s in new)

            text = rx.sub(sub, text)
        return text

    # -- module-relative pieces -------------------------------------------------------------------------------
    def piece_candidates(self, segs, ctx_modules, ctx_roots):
        """Production files whose old path ends with the given segments. / 旧路径以这些段结尾的生产文件。"""
        cands = self.file_suffix.get('/'.join(segs), [])
        if len(cands) > 1:
            exact = [e for e in cands if len(e.old_pkg.split('.')) + 1 == len(segs)]
            if exact:
                cands = exact
        if len(cands) > 1 and ctx_modules:
            f = [e for e in cands if e.old_pkg.split('.')[0] in ctx_modules]
            cands = f or cands
        if len(cands) > 1 and ctx_roots:
            f = [e for e in cands if e.root in ctx_roots]
            cands = f or cands
        return cands

    def rewrite_piece(self, segs, sep, ctx_modules, ctx_roots, allow_dirs):
        """Map a path or name piece that is relative to some old directory; None when it is not one.

        映射相对于某个旧目录的路径或名字片段；不是这类片段时返回 None。
        """
        last, dollar, inner = segs[-1].partition('$')
        cands = self.piece_candidates(segs[:-1] + [last], ctx_modules, ctx_roots)
        if len(cands) == 1:
            e = cands[0]
            old = e.old_pkg.split('.') + [e.cls]
            base_old = old[:len(old) - len(segs)]
            new = (self.new_pkg(e).split('.') if self.new_pkg(e) else []) + [e.cls + dollar + inner]
            return self._relative(base_old, new, sep, e.root)
        if len(cands) > 1:
            news = {tuple(self._relative((e.old_pkg.split('.') + [e.cls])[:len(e.old_pkg.split('.')) + 1 - len(segs)],
                                         (self.new_pkg(e).split('.') if self.new_pkg(e) else []) + [e.cls],
                                         sep, e.root) or ()) for e in cands}
            if len(news) == 1 and () not in news:
                return list(next(iter(news)))
            self.log['ambiguous piece: ' + sep.join(segs)] += 1
            return None
        if not allow_dirs or len(segs) < 2:
            return None
        if any(os.path.isdir(os.path.join(REPO, r, *segs)) for r in RESOURCE_ROOTS):
            return None  # a classpath resource directory, not a source directory / 这是类路径资源目录
        dirs = self.dir_suffix.get('/'.join(segs), set())
        if len(dirs) > 1 and ctx_modules:
            f = {d for d in dirs if d[1].split('.')[0] in ctx_modules}
            dirs = f or dirs
        if len(dirs) > 1 and ctx_roots:
            f = {d for d in dirs if d[0] in ctx_roots}
            dirs = f or dirs
        results = set()
        for root, pkg in dirs:
            old = pkg.split('.')
            base_old = old[:len(old) - len(segs)]
            new = self.map_dir(pkg, (root,))
            rel = self._relative(base_old, new.split('.') if new else [], sep, root)
            results.add(tuple(rel) if rel is not None else None)
        if len(results) == 1 and None not in results:
            return list(next(iter(results)))
        if results:
            self.log['ambiguous directory piece: ' + sep.join(segs)] += 1
        return None

    def _relative(self, base_old, new, sep, root):
        """`new` relative to the new location of the old base directory. / `new` 相对旧基目录新位置的写法。"""
        base_pkg = '.'.join(base_old)
        if not base_old:
            base_new = []
        elif base_pkg in LEGACY_CONFIG_PKGS and 'mixins' in self.phases:
            base_new = LEGACY_CONFIG_PKGS[base_pkg].split('.')
        else:
            got = self.map_dir(base_pkg, (root,))
            base_new = got.split('.') if got else []
        if new[:len(base_new)] == base_new:
            return new[len(base_new):]
        if sep != '/':
            return None
        k = 0
        while k < len(base_new) and k < len(new) and base_new[k] == new[k]:
            k += 1
        return ['..'] * (len(base_new) - k) + new[k:]

    PIECE_SLASH = re.compile(r'(?:(?<=["\'`]/)|(?<![\w$./\\-]))(' + IDENT + r'(?:/' + IDENT + r')+)(?P<ext>\.java\b|\.class\b)?(?![\w$])')
    PIECE_DOT = re.compile(r'(?<![\w$./\\-])((?:[a-z][\w]*\.)+[A-Z][\w$]*)')

    PIECE_FILE = re.compile(r'(?<=["\'`])([A-Z][\w$]*)(\.java)(?=["\'`])')

    def rewrite_file_names(self, text, ctx_modules, ctx_roots):
        """A bare "X.java" literal is resolved against X's old directory. When the map takes X out of the new
        location of that directory (a flat mixin package, a split package), the literal gains the missing
        sub-directories.

        单独的 "X.java" 字面量是相对 X 的旧目录解析的。若映射把 X 移出了该目录的新位置（平铺的 mixin 包、被拆分的包），
        则给字面量补上缺少的子目录。
        """
        def sub(m):
            if text[max(0, m.start() - 24):m.start()].rstrip('"\'` \n').endswith('resolveSibling('):
                return m.group(0)  # relative to one file's own directory / 相对某个文件自身所在目录
            cands = [e for e in self.file_suffix.get(m.group(1), ())]
            if len(cands) > 1 and ctx_modules:
                cands = [e for e in cands if e.old_pkg.split('.')[0] in ctx_modules] or cands
            if len(cands) > 1 and ctx_roots:
                cands = [e for e in cands if e.root in ctx_roots] or cands
            if len(cands) != 1:
                return m.group(0)
            e = cands[0]
            new = (self.new_pkg(e).split('.') if self.new_pkg(e) else []) + [e.cls]
            rel = self._relative(e.old_pkg.split('.'), new, '/', e.root)
            if rel is None or rel == [e.cls]:
                return m.group(0)
            self.log['file name'] += 1
            return '/'.join(rel) + m.group(2)

        return self.PIECE_FILE.sub(sub, text)

    def rewrite_pieces(self, text, ctx_modules, ctx_roots, allow_dirs=True, dotted=True):
        def sub_slash(m):
            segs = m.group(1).split('/')
            if segs[0] in ('src', 'dev', 'caecorthus', 'sparkdlc', 'assets', 'data', 'build', 'docs', 'gradle'):
                return m.group(0)
            ext = m.group('ext') or ''
            if not ext and not allow_dirs:
                return m.group(0)
            new = self.rewrite_piece(segs, '/', ctx_modules, ctx_roots, allow_dirs and not ext)
            if new is None or new == segs:
                return m.group(0)
            self.log['piece'] += 1
            return '/'.join(new) + ext

        text = self.PIECE_SLASH.sub(sub_slash, text)
        if dotted:
            def sub_dot(m):
                segs = m.group(1).split('.')
                new = self.rewrite_piece(segs, '.', ctx_modules, ctx_roots, False)
                if new is None or new == segs:
                    return m.group(0)
                self.log['dotted piece'] += 1
                return '.'.join(new)

            text = self.PIECE_DOT.sub(sub_dot, text)
        return text

    STUB_PACKAGE = re.compile(r'(package\s+)dev\.caecorthus\.sparkdlc\.((?:' + '|'.join(MODULES)
                              + r')(?:\.\w+)*)(\s*;)')
    STUB_TYPE = re.compile(r'\b(?:class|interface|enum|record)\s+([A-Z]\w*)')

    def rewrite_stub_packages(self, text):
        """A `package` line inside a stub source kept in a string follows the class that the stub declares, so
        that the stub of a class from a split package lands where the real class went.

        字符串里保存的桩源码中的 `package` 行跟随该桩声明的类，这样被拆分包里的类的桩会落在真实类所在的新包。
        """
        def sub(m):
            nxt = self.STUB_PACKAGE.search(text, m.end())
            end = nxt.start() if nxt else min(len(text), m.end() + 4000)
            decl = self.STUB_TYPE.search(text, m.end(), end)
            if decl is None:
                return m.group(0)
            pkg, cls = m.group(2), decl.group(1)
            e = self.cls.get(pkg + '.' + cls)
            if e is not None:
                new = self.new_pkg(e)
            elif phase_of('src/main/java', pkg) in self.phases:
                new = self.rules.resolve(pkg, cls)
            else:
                return m.group(0)
            return m.group(1) + ROOT_PKG + ('.' + new if new else '') + m.group(3)

        return self.STUB_PACKAGE.sub(sub, text)

    RESOLVE_ONE = re.compile(r'(\.resolve\(\s*")([a-z][a-z0-9_]*)("\s*\))')

    def rewrite_anchored_dirs(self, text, anchors):
        """`.resolve("x")` where x is a directory directly below a root the same file names in full.

        `.resolve("x")`：x 是同一文件里以完整路径写出的某个根目录的直接子目录。
        """
        def sub(m):
            seg = m.group(2)
            found = set()
            for root, base in anchors:
                pkg = base + '.' + seg
                if pkg in self.known_pkgs and any(e.root == root for e in self.entries
                                                  if e.old_pkg == pkg or e.old_pkg.startswith(pkg + '.')):
                    new = self.map_dir(pkg, (root,))
                    rel = self._relative(base.split('.'), new.split('.') if new else [], '/', root)
                    found.add('/'.join(rel) if rel is not None else None)
            if len(found) != 1 or None in found:
                return m.group(0)
            new = next(iter(found))
            if new != seg:
                self.log['anchored directory'] += 1
            return m.group(1) + new + m.group(3)

        return self.RESOLVE_ONE.sub(sub, text) if anchors else text

    def old_dir_anchors(self, text):
        """Old directories a text names in full, as (source root, old package). / 文本中完整写出的旧目录。"""
        out = set()
        rx = re.compile(r'(src/(?:main|client)/java)/dev/caecorthus/sparkdlc((?:/' + IDENT + r')+)(?![\w$/.])')
        for m in rx.finditer(text):
            pkg = m.group(2)[1:].replace('/', '.')
            if pkg.split('.')[0] in MODULES and pkg in self.known_pkgs:
                out.add((m.group(1), pkg))
        return out

    QUOTED_ENTRY = re.compile(r'(\\")([A-Z][\w$]*)(\\")')
    PREFIX_LITERAL = re.compile(r'(?<![\\\w])"((?:[a-z][a-z0-9_]*\.)+)"')

    def build_config_index(self):
        self.flat_entries = collections.defaultdict(set)   # simple name -> new config-relative names
        self.config_dirs = collections.defaultdict(set)    # old config-relative package -> new one
        for e in self.entries:
            if e.root not in PROD_ROOTS:
                continue
            for cfg, merged in LEGACY_CONFIG_PKGS.items():
                if e.old_pkg == cfg and e.new_pkg.startswith(merged + '.'):
                    self.flat_entries[e.cls].add((cfg.split('.')[0], e.new_fq[len(merged) + 1:]))
                elif e.old_pkg.startswith(cfg + '.'):
                    new = self.map_dir(e.old_pkg, (e.root,))
                    if new.startswith(merged + '.'):
                        self.config_dirs[e.old_pkg[len(cfg) + 1:] + '.'].add(new[len(merged) + 1:] + '.')

    WHOLE_ENTRY = re.compile(r'(?<![\\\w])(")([A-Z][\w$]*)(")')

    def rewrite_config_entries(self, text, ctx_modules=(), whole_literals=False):
        """Config-relative names that only a JSON-quoted or prefix literal identifies as such.

        A top-level entry such as "FooMixin" looks like any class-name mention, so it is rewritten only where the
        literal carries JSON quotes, or (verify scripts) where every whole-literal mixin name is a config entry.
        只有带 JSON 引号的字面量或前缀字面量才能确认是“相对配置包的名字”，这里改写这两类。顶层条目（如 "FooMixin"）
        与普通的类名提及无法区分，因此只在字面量带 JSON 引号时改写；校验脚本中整串的 mixin 名都是配置条目，也会改写。
        """
        if 'mixins' not in self.phases:
            return text
        if not hasattr(self, 'flat_entries'):
            self.build_config_index()

        def sub_entry(m):
            found = self.flat_entries.get(m.group(2))
            if not found:
                return m.group(0)
            new = {n for mod, n in found if mod in ctx_modules} or {n for mod, n in found}
            if len(new) != 1:
                self.log['ambiguous config entry: ' + m.group(2)] += 1
                return m.group(0)
            self.log['quoted config entry'] += 1
            return m.group(1) + next(iter(new)) + m.group(3)

        if whole_literals:
            text = self.WHOLE_ENTRY.sub(sub_entry, text)

        def sub_prefix(m):
            new = self.config_dirs.get(m.group(1))
            if not new or len(new) != 1:
                return m.group(0)
            self.log['config package prefix'] += 1
            return '"' + next(iter(new)) + '"'

        return self.PREFIX_LITERAL.sub(sub_prefix, self.QUOTED_ENTRY.sub(sub_entry, text))

    def rewrite_config_names(self, text):
        if 'mixins' not in self.phases:
            return text
        return CONFIG_NAME_RE.sub(lambda m: 'sparkdlc' + (m.group(1) or '') + '.mixins.json', text)


# ---------------------------------------------------------------------------------------------------------------
# Java file rewriting / Java 文件改写
# ---------------------------------------------------------------------------------------------------------------
IMPORT_RE = re.compile(r'^[ \t]*import\s+(static\s+)?([\w.]+?)(\.\*)?\s*;[ \t]*$', re.M)
TYPE_DECL_RE = re.compile(r'\b(?:class|interface|enum|record)\s+([A-Z][\w$]*)')


def visible_roots(root):
    if root == 'src/main/java':
        return ('src/main/java',)
    if root == 'src/client/java':
        return PROD_ROOTS
    if root == 'src/test/java':
        return PROD_ROOTS + ('src/test/java',)
    if root == 'src/test/strength/java':
        return PROD_ROOTS + ('src/test/strength/java',)
    return ()


def add_split_imports(mapper, entry, text, problems):
    """Classes that shared the old package and now live elsewhere must be imported. / 原同包、现已分开的类需要 import。"""
    my_new = mapper.new_pkg(entry)
    roots = visible_roots(entry.root)
    siblings = {}
    for e in mapper.by_pkg.get(entry.old_pkg, ()):
        if e is entry or e.root not in roots:
            continue
        if mapper.new_pkg(e) != my_new:
            siblings.setdefault(e.cls, e)
    if not siblings:
        return text
    used = code_identifiers(text)
    imports = list(IMPORT_RE.finditer(text))
    imported = {m.group(2).rpartition('.')[2] for m in imports if not m.group(3) and not m.group(1)}
    declared = set(TYPE_DECL_RE.findall(text))
    need = sorted(n for n in used if n in siblings and n not in imported and n not in declared)
    if not need:
        return text
    lines = ''.join('import %s.%s;\n' % (ROOT_PKG, mapper.new_fq(siblings[n])) for n in need)
    existing = {m.group(2) for m in imports}
    lines = ''.join(l for l in lines.splitlines(True) if l[7:-2] not in existing)
    if not lines:
        return text
    if imports:
        pos = imports[-1].end()
        pos = text.find('\n', pos) + 1 if '\n' in text[pos:] else len(text)
        return text[:pos] + lines + text[pos:]
    pkg, m = declared_package(text)
    if m is None:
        problems.append('no package line: ' + entry.old_path)
        return text
    pos = text.find('\n', m.end()) + 1
    return text[:pos] + '\n' + lines + text[pos:]


def rewrite_package_line(mapper, entry, text):
    pkg, m = declared_package(text)
    if m is None:
        return text
    new = ROOT_PKG + ('.' + mapper.new_pkg(entry) if mapper.new_pkg(entry) else '')
    return text[:m.start(1)] + new + text[m.end(1):]


def rewrite_non_code(mapper, text, fn):
    """Apply fn to string literals and comments of Java text only. / 只对 Java 文本的字符串和注释应用 fn。"""
    out = []
    for kind, a, b in java_regions(text):
        seg = text[a:b]
        out.append(fn(seg) if kind in ('string', 'comment') else seg)
    return ''.join(out)


def module_context(text, own_module):
    mods = set(re.findall(r'sparkdlc[/.](' + '|'.join(MODULES) + r')\b', text))
    roots = set()
    if re.search(r'src/main/java', text) or re.search(r'"src",\s*"main"', text):
        roots.add('src/main/java')
    if re.search(r'src/client/java', text) or re.search(r'"src",\s*"client"', text):
        roots.add('src/client/java')
    if not mods and own_module:
        mods = {own_module}
    return mods, tuple(sorted(roots))


# Lines the tests keep on purpose because they name a path at an old commit. / 测试有意保留的旧提交路径所在行。
KEEP_LINE = re.compile(r'git[ "\',]+show|HISTORICAL|historic(al)?_path', re.I)


def rewrite_text(mapper, text, kind, own_module=None, path=''):
    """kind: java-prod | java-test | script (python, gradle, config) | doc."""
    if kind in ('java-prod', 'java-gametest'):
        text = mapper.rewrite_full(text)
        if 'mixins' in mapper.phases:
            mods, roots = ({own_module} if own_module else set()), ()
            text = rewrite_non_code(mapper, text, lambda s: mapper.rewrite_config_names(
                mapper.rewrite_pieces(s, mods, roots, allow_dirs=False, dotted=False)))
        return text
    mods, roots = module_context(text, own_module)
    if kind == 'java-test':
        anchors = mapper.old_dir_anchors(text)
        text = mapper.rewrite_anchored_dirs(text, anchors)
        text = mapper.rewrite_config_entries(text, mods)
        text = mapper.rewrite_stub_packages(text)
        text = mapper.rewrite_full(text)
        text = rewrite_non_code(mapper, text, lambda s: mapper.rewrite_config_names(
            mapper.rewrite_pieces(s, mods, roots)))
        text = mapper.rewrite_file_names(text, mods, roots)
        return text
    out = []
    for line in text.splitlines(True):
        if KEEP_LINE.search(line):
            out.append(line)
            continue
        line = mapper.rewrite_full(line)
        if kind == 'doc':
            line = re.sub(r'`[^`\n]+`', lambda m: mapper.rewrite_pieces(m.group(0), mods, roots), line)
        else:
            line = mapper.rewrite_pieces(
                mapper.rewrite_config_entries(line, mods, whole_literals=path.endswith('.gradle')), mods, roots)
        out.append(mapper.rewrite_config_names(line))
    return ''.join(out)


# ---------------------------------------------------------------------------------------------------------------
# Apply / 执行
# ---------------------------------------------------------------------------------------------------------------
def git(*args, cwd=REPO, env=None):
    return subprocess.run(('git',) + args, cwd=cwd, env=env, check=True, capture_output=True, text=True).stdout


def move_files(mapper, phase, problems):
    moved = 0
    for e in mapper.entries:
        if e.phase != phase:
            continue
        src, dst = os.path.join(REPO, e.old_path), os.path.join(REPO, e.new_path)
        if src == dst or not os.path.isfile(src):
            continue
        if os.path.exists(dst):
            problems.append('target exists: %s -> %s' % (e.old_path, e.new_path))
            continue
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        os.rename(src, dst)
        moved += 1
    for root in JAVA_ROOTS:
        base = os.path.join(REPO, root)
        for d, dirs, fs in os.walk(base, topdown=False):
            if d != base and not os.listdir(d):
                os.rmdir(d)
            elif d != base and os.listdir(d) == ['.DS_Store']:
                os.remove(os.path.join(d, '.DS_Store'))
                os.rmdir(d)
    return moved


def current_path(e, mapper):
    for p in (e.new_path, e.old_path):
        if os.path.isfile(os.path.join(REPO, p)):
            return p
    return None


def rewrite_java_tree(mapper, roots, problems):
    """Rewrite every Java file of the given source roots (moved or not). / 改写给定源码根下的全部 Java 文件。"""
    by_path = {}
    for e in mapper.entries:
        p = current_path(e, mapper)
        if p:
            by_path[p] = e
    changed = 0
    for root in roots:
        base = os.path.join(REPO, root)
        for d, dirs, fs in os.walk(base):
            for f in fs:
                if not f.endswith('.java'):
                    continue
                path = os.path.join(d, f)
                rel = os.path.relpath(path, REPO)
                text = read(path)
                e = by_path.get(rel)
                kind = 'java-test' if root in TEST_ROOTS else 'java-prod'
                new = text
                if e is not None and mapper.active(e):
                    new = add_split_imports(mapper, e, new, problems)
                    new = rewrite_package_line(mapper, e, new)
                own = e.old_pkg.split('.')[0] if e is not None else None
                new = rewrite_text(mapper, new, kind, own, rel)
                if new != text:
                    write(path, new)
                    changed += 1
    return changed


def rewrite_files(mapper, paths, kind_of):
    changed = 0
    for rel in paths:
        path = os.path.join(REPO, rel)
        if not os.path.isfile(path):
            continue
        try:
            text = read(path)
        except UnicodeDecodeError:
            continue
        kind, own = kind_of(rel)
        new = rewrite_text(mapper, text, kind, own, rel)
        if new != text:
            write(path, new)
            changed += 1
    return changed


def walk_files(rel_dir, exts=None):
    base = os.path.join(REPO, rel_dir)
    for d, dirs, fs in os.walk(base):
        dirs[:] = sorted(x for x in dirs if x not in ('__pycache__', '.git'))
        for f in sorted(fs):
            if exts is None or f.endswith(exts):
                yield os.path.relpath(os.path.join(d, f), REPO)


def merge_mixin_configs(mapper, problems):
    """Nine configs become two; each side's list is the concatenation of the legacy lists in fabric.mod.json order.

    九个配置合并为两个；每一侧的列表是旧列表按 fabric.mod.json 顺序的拼接。
    """
    if not all(os.path.isfile(os.path.join(REPO, p)) for p, _, _ in LEGACY_CONFIGS):
        return False
    plugin_path = os.path.join(REPO, 'src/client/java', ROOT_DIR, COMPOSITE_PLUGIN.replace('.', '/') + '.java')
    if not os.path.isfile(plugin_path):
        problems.append('composite mixin plugin source is missing: ' + os.path.relpath(plugin_path, REPO))
        return False
    lists = {'mixins': [], 'client': []}
    for path, pkg, key in LEGACY_CONFIGS:
        data = json.loads(read(os.path.join(REPO, path)))
        if data['package'] != ROOT_PKG + '.' + pkg or set(data) - {'required', 'minVersion', 'package',
                                                                 'compatibilityLevel', 'plugin', 'injectors', key}:
            problems.append('unexpected shape of ' + path)
            return False
        for name in data[key]:
            e = mapper.cls.get(pkg + '.' + name)
            if e is None:
                problems.append('%s lists %s, which has no source file' % (path, name))
                continue
            base = MERGED[key][1] + '.'
            if not e.new_fq.startswith(base):
                problems.append('%s leaves the merged config package: %s' % (e.old_fq, e.new_fq))
                continue
            lists[key].append(e.new_fq[len(base):])
    for key, (path, pkg) in MERGED.items():
        if len(set(lists[key])) != len(lists[key]):
            problems.append('duplicate entries in merged ' + key)
        out = ['{', '  "required": true,', '  "minVersion": "0.8",', '  "package": "%s.%s",' % (ROOT_PKG, pkg),
               '  "compatibilityLevel": "JAVA_21",']
        if key == 'client':
            out.append('  "plugin": "%s.%s",' % (ROOT_PKG, COMPOSITE_PLUGIN))
        out.append('  "%s": [' % key)
        out += ['    "%s"%s' % (n, ',' if i + 1 < len(lists[key]) else '') for i, n in enumerate(lists[key])]
        out += ['  ],', '  "injectors": {', '    "defaultRequire": 1', '  }', '}', '']
        # keep the history of the largest legacy list / 沿用最大旧列表的历史
        biggest = 'src/main/resources/sparkwitch.mixins.json' if key == 'mixins' \
            else 'src/client/resources/sparkwitch.client.mixins.json'
        os.rename(os.path.join(REPO, biggest), os.path.join(REPO, path))
        write(os.path.join(REPO, path), '\n'.join(out))
    for path, _, _ in LEGACY_CONFIGS:
        if os.path.isfile(os.path.join(REPO, path)):
            os.remove(os.path.join(REPO, path))
    fmj = os.path.join(REPO, 'src/main/resources/fabric.mod.json')
    text = read(fmj)
    m = re.search(r'"mixins": \[\n.*?\n  \]', text, re.S)
    new = ('"mixins": [\n    "sparkdlc.mixins.json",\n    {\n      "config": "sparkdlc.client.mixins.json",\n'
           '      "environment": "client"\n    }\n  ]')
    write(fmj, text[:m.start()] + new + text[m.end():])
    return True


# AGENTS.md is the project's instruction file and is edited by its owner, not by this script.
# AGENTS.md 是项目的指令文件，由其所有者修改，本脚本不改动它。
TOOLING_FILES = ['build.gradle', 'tools/payload_policy.py', 'CONTEXT.md', 'README.md', 'THIRD_PARTY_NOTICES.md']
TOOLING_DIRS = ['gradle/verify', 'config', '.github', 'docs/assist', 'docs/factionapi', 'docs/witch']


def tooling_kind(rel):
    own = None
    m = re.match(r'(?:gradle/verify|docs)/(\w+)', rel)
    if m and m.group(1) in MODULES:
        own = m.group(1)
    return ('doc' if rel.endswith('.md') else 'script'), own


def test_kind(rel):
    return 'script', 'strength' if '/strength/' in rel else None


def checks(entries, rules):
    problems, notes = [], []
    seen = {}
    for e in entries:
        group = 'prod' if e.root in PROD_ROOTS else e.root
        key = (group, e.new_fq)
        if key in seen:
            problems.append('collision: %s <- %s and %s' % (e.new_fq, seen[key].old_fq, e.old_fq))
        seen[key] = e
        if e.root in TEST_ROOTS and ('prod', e.new_fq) in seen:
            problems.append('test collides with production class: ' + e.new_fq)
    listed = set()
    for path, pkg, key in LEGACY_CONFIGS:
        full = os.path.join(REPO, path)
        if os.path.isfile(full):
            listed.update(pkg + '.' + n for n in json.loads(read(full))[key])
    for path, pkg in MERGED.values():
        full = os.path.join(REPO, path)
        if os.path.isfile(full):
            data = json.loads(read(full))
            listed_new = {pkg + '.' + n for n in data.get('mixins', []) + data.get('client', [])}
            listed.update(e.old_fq for e in entries if e.new_fq in listed_new)
    for e in entries:
        if e.root not in PROD_ROOTS:
            continue
        in_mixin_tree = e.new_pkg == 'mixin' or e.new_pkg.startswith('mixin.') or \
            e.new_pkg == 'client.mixin' or e.new_pkg.startswith('client.mixin.')
        if (e.old_fq in listed) != in_mixin_tree:
            problems.append('%s: %s the merged mixin package but %s a mixin config' % (
                e.new_fq, 'inside' if in_mixin_tree else 'outside', 'listed in' if e.old_fq in listed else 'not in'))
        if e.root == 'src/client/java' and not (e.new_pkg == 'client' or e.new_pkg.startswith('client.')):
            problems.append('client class outside the client package: ' + e.new_fq)
        if e.root == 'src/main/java' and (e.new_pkg == 'client' or e.new_pkg.startswith('client.')):
            problems.append('common class inside the client package: ' + e.new_fq)
        if is_old(e.new_pkg):
            notes.append('still in a legacy package: ' + e.new_fq)
    # The composite client plugin forwards every callback to the three legacy plugins. That is only equivalent to
    # the legacy per-config plugins while each of them names (by FQCN string) mixins of its own former config only.
    # 组合客户端插件把每个回调转发给三个旧插件。只有当每个旧插件（以全限定名字符串）点名的都是它自己原配置里的 mixin 时，
    # 这才与原先每个配置各自的插件等价。
    governed = {'SparkFactionClientMixinPlugin': 'factionapi.client.mixin',
                'SparkStrengthClientMixinPlugin': 'strength.client.mixin',
                'SparkWitchClientMixinPlugin': 'witch.client.mixin'}
    by_any = {}
    for e in entries:
        by_any[e.old_fq] = e
        by_any[e.new_fq] = e
    for e in entries:
        if e.root == 'src/client/java' and e.cls in governed:
            path = next((os.path.join(REPO, q) for q in (e.new_path, e.old_path)
                         if os.path.isfile(os.path.join(REPO, q))), None)
            if path is None:
                problems.append('legacy mixin plugin source is missing: ' + e.cls)
                continue
            named = re.findall(r'"dev\.caecorthus\.sparkdlc\.([\w.]+Mixin)"', read(path))
            if not named:
                problems.append(e.cls + ' names no mixin; the composite plugin comment is out of date')
            for name in named:
                target = by_any.get(name)
                if target is None or not (target.old_pkg + '.').startswith(governed[e.cls] + '.') \
                        or target.old_fq not in listed:
                    problems.append('%s names %s, which its former config did not list' % (e.cls, name))
    return problems, notes


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('command', choices=('plan', 'apply'))
    ap.add_argument('--phase', choices=('code', 'mixins', 'tooling', 'tests'))
    ap.add_argument('--force', action='store_true',
                    help='apply a phase again although it is recorded as applied / 即使已记录为执行过也再次执行')
    args = ap.parse_args()
    applied_path = os.path.join(REPO, APPLIED_TEST_FILE_REL) if args.phase == 'tests' else APPLIED_FILE
    applied = read(applied_path).split() if os.path.isfile(applied_path) else []
    if args.command == 'apply' and args.phase in applied and not args.force:
        print('phase %s is recorded as applied in %s: nothing to do' % (args.phase, os.path.relpath(applied_path, REPO)))
        return 0
    rules = Rules()
    entries, test_notes = build_plan(rules)
    problems, notes = checks(entries, rules)
    if args.command == 'plan':
        save_manifest(entries)
        count = collections.Counter((e.root, e.phase) for e in entries)
        for k in sorted(count):
            print('%-28s %-8s %5d files' % (k[0], k[1], count[k]))
        print('files that change package:', sum(1 for e in entries if e.old_pkg != e.new_pkg))
        for old, default, chosen in test_notes:
            print('test placed by the classes it uses: %s -> %s (plain rule: %s)' % (old, chosen, default))
        for n in notes:
            print('note:', n)
        for p in problems:
            print('PROBLEM:', p)
        return 1 if problems else 0
    if not args.phase:
        ap.error('apply needs --phase')
    if problems:
        for p in problems:
            print('PROBLEM:', p)
        return 1
    save_manifest(entries)
    # verify scripts name local test files, so the tooling phase maps test paths as well
    # 校验脚本会引用本地测试文件，因此 tooling 阶段同样映射测试路径
    done = {'code': ['code'], 'mixins': ['code', 'mixins'], 'tooling': ['code', 'mixins', 'tests'],
            'tests': ['code', 'mixins', 'tests']}[args.phase]
    mapper = Mapper(entries, rules, done)
    if args.phase in ('code', 'mixins'):
        moved = move_files(mapper, args.phase, problems)
        merged = merge_mixin_configs(mapper, problems) if args.phase == 'mixins' else False
        changed = rewrite_java_tree(mapper, PROD_ROOTS + ('src/gametest/java',), problems)
        res = [p for d in ('src/main/resources', 'src/client/resources', 'src/gametest/resources')
               for p in walk_files(d, ('.json', '.glsl', '.vsh', '.fsh', '.txt', '.mcmeta', '.properties'))
               if '/assets/' not in p or p.endswith('.glsl')]
        res = [p for p in res if not p.endswith('.mixins.json')]
        changed += rewrite_files(mapper, res, lambda rel: ('script', None))
        print('moved %d files, rewrote %d files%s' % (moved, changed, ', merged the mixin configs' if merged else ''))
    elif args.phase == 'tooling':
        paths = list(TOOLING_FILES)
        for d in TOOLING_DIRS:
            paths += list(walk_files(d))
        print('rewrote %d tooling and doc files' % rewrite_files(mapper, paths, tooling_kind))
    else:
        moved = move_files(mapper, 'tests', problems)
        changed = rewrite_java_tree(mapper, TEST_ROOTS, problems)
        paths = list(walk_files('src/test/strength/python', ('.py',))) + list(walk_files('src/test/resources/META-INF'))
        changed += rewrite_files(mapper, paths, test_kind)
        print('moved %d test files, rewrote %d files' % (moved, changed))
    for k, v in sorted(mapper.log.items()):
        if not k.startswith('piece') and not k.startswith('dotted piece') and ':' in k:
            print('log: %s (x%d)' % (k, v))
    print('rewritten: ' + ', '.join('%d %s' % (v, k) for k, v in sorted(mapper.log.items()) if ':' not in k))
    for p in problems:
        print('PROBLEM:', p)
    if not problems and args.phase not in applied:
        write(applied_path, ''.join(x + '\n' for x in applied + [args.phase]))
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())
