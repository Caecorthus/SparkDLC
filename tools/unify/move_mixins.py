#!/usr/bin/env python3
"""Moves and renames mixins by `mixin-moves.tsv` (slice "No twins").
按 `mixin-moves.tsv` 搬迁并改名 mixin（"没有双胞胎"一节）。

Each row names a mixin by its config-relative entry before and after. The script moves the file (`git mv`, so the
history follows), rewrites its `package` line and class name, rewrites every reference to it (fully qualified names
in dotted and slashed form, config entries, mixin-root-relative paths) in production, gametest, tooling, docs and the
local tests, and follows the move in `file-moves.tsv` (columns `new_path` / `new_fqcn` only). The entry keeps its
position in its mixin config: only the name in the entry changes, so the application order stays the same.
A bare simple name that several classes share is never rewritten outside the moved file; `plan` lists such leftovers
for review.

每行以配置相对条目名给出 mixin 搬迁前后的名字。脚本移动文件（`git mv`，历史随之迁移），改写其 `package` 行与类名，改写
生产代码、gametest、工具、文档与本地测试中对它的全部引用（点分与斜杠形式的全限定名、配置条目、相对 mixin 根目录的路径），并在
`file-moves.tsv` 中跟随搬迁（只改 `new_path` / `new_fqcn` 两列）。条目在 mixin 配置中的位置不变，只改条目中的名字，因此应用
顺序不变。多个类共用的简单名在被搬迁文件之外从不改写；`plan` 列出这些遗留供人工检查。

    python3 tools/unify/move_mixins.py plan     # checks and leftovers, changes nothing
    python3 tools/unify/move_mixins.py apply    # moves and rewrites; rows already applied are skipped
"""
import argparse
import os
import re
import subprocess
import sys

REPO = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
TABLE = os.path.join(REPO, 'tools/unify/mixin-moves.tsv')
FILE_MOVES = os.path.join(REPO, 'tools/unify/file-moves.tsv')
CONFIGS = {
    'common': ('src/main/resources/sparkdlc.mixins.json', 'src/main/java', 'dev.caecorthus.sparkdlc.mixin'),
    'client': ('src/client/resources/sparkdlc.client.mixins.json', 'src/client/java',
               'dev.caecorthus.sparkdlc.client.mixin'),
}
# Trees whose text files are rewritten. docs/local and tools/fusion stay as they were (history and reviews), and so
# does file-moves.tsv except for its two "after" columns.
# 改写其中文本文件的目录树。docs/local 与 tools/fusion 保持原样（历史与评审），file-moves.tsv 只改两列“之后”。
TREES = ['src/main', 'src/client', 'src/gametest', 'src/test', 'gradle', 'docs', 'tools', 'config']
FILES = ['build.gradle', 'CONTEXT.md', 'README.md']
SKIP_DIRS = {'docs/local', 'tools/fusion', '.git', 'build', '__pycache__', '.gradle'}
SKIP_FILES = {'tools/unify/file-moves.tsv', 'tools/unify/class-renames.tsv', 'tools/unify/member-renames.tsv',
              'tools/unify/mixin-member-renames.tsv', 'tools/unify/mixin-moves.tsv', 'tools/unify/move_mixins.py',
              'src/test/.unify-file-moves.tsv'}
TEXT_EXT = ('.java', '.json', '.gradle', '.md', '.py', '.tsv', '.txt', '.properties', '.toml', '.yml', '.yaml',
            '.accesswidener', '.kts')


class Row:
    def __init__(self, config, old, new, reason):
        self.config, self.old, self.new, self.reason = config, old, new, reason
        _, src, pkg = CONFIGS[config]
        self.src = src
        self.root_pkg = pkg
        self.root_dir = pkg.replace('.', '/')

    def fq(self, entry):
        return self.root_pkg + '.' + entry

    def path(self, entry):
        return os.path.join(self.src, self.root_dir, entry.replace('.', '/') + '.java')

    def simple(self, entry):
        return entry.rsplit('.', 1)[1]

    def package(self, entry):
        return self.fq(entry).rsplit('.', 1)[0]


def read_rows():
    rows = []
    with open(TABLE, encoding='utf-8') as handle:
        for number, line in enumerate(handle, 1):
            if number == 1 or not line.strip() or line.startswith('#'):
                continue
            config, old, new, reason = line.rstrip('\n').split('\t')
            rows.append(Row(config, old, new, reason))
    return rows


def text_files():
    for tree in TREES:
        base = os.path.join(REPO, tree)
        for directory, dirs, files in os.walk(base):
            rel_dir = os.path.relpath(directory, REPO)
            dirs[:] = [d for d in dirs if d not in SKIP_DIRS and os.path.join(rel_dir, d) not in SKIP_DIRS]
            for name in files:
                rel = os.path.join(rel_dir, name)
                if rel in SKIP_FILES or not name.endswith(TEXT_EXT):
                    continue
                yield rel
    for rel in FILES:
        if os.path.exists(os.path.join(REPO, rel)):
            yield rel


def patterns(row):
    """(regex, replacement) pairs for every qualified spelling of the mixin. / 该 mixin 的每种限定写法。"""
    old_fq, new_fq = row.fq(row.old), row.fq(row.new)
    old_rel_slash, new_rel_slash = row.old.replace('.', '/'), row.new.replace('.', '/')
    result = [
        # dotted fully qualified name (also before $Inner) / 点分全限定名
        (re.compile(r'(?<![\w.])' + re.escape(old_fq) + r'(?![\w])'), new_fq),
        # slashed fully qualified name (paths, class-file names, descriptors) / 斜杠全限定名
        (re.compile(r'(?<![\w])' + re.escape(old_fq.replace('.', '/')) + r'(?![\w])'), new_fq.replace('.', '/')),
        # mixin-root-relative path such as "faction/witch/Foo.java" or MIXINS / "faction/witch/Foo"
        # 相对 mixin 根目录的路径
        (re.compile(r'(?<![\w/.])' + re.escape(old_rel_slash) + r'(?![\w])'), new_rel_slash),
        (re.compile(r'(?<=mixin/)' + re.escape(old_rel_slash) + r'(?![\w])'), new_rel_slash),
        # name relative to the mod's root package, e.g. mixin.faction.witch.Foo / 相对本模组根包的名字
        (re.compile(r'(?<![\w.])' + re.escape(row.root_pkg[len('dev.caecorthus.sparkdlc.'):] + '.' + row.old)
                    + r'(?![\w])'), row.root_pkg[len('dev.caecorthus.sparkdlc.'):] + '.' + row.new),
        # config-relative entry, quoted or in prose / 配置相对条目名
        (re.compile(r'(?<![\w.$/])' + re.escape(row.old) + r'(?![\w])'), row.new),
    ]
    return result


def check(rows):
    problems = []
    entries = {}
    import json
    for config, (cfg, _, _) in CONFIGS.items():
        with open(os.path.join(REPO, cfg), encoding='utf-8') as handle:
            data = json.load(handle)
        entries[config] = [e for key in ('mixins', 'client', 'server') for e in data.get(key, [])]
    olds = [(r.config, r.old) for r in rows]
    news = [(r.config, r.new) for r in rows]
    if len(set(olds)) != len(olds) or len(set(news)) != len(news):
        problems.append('duplicate old or new entry in the table')
    for row in rows:
        other = 'client' if row.config == 'common' else 'common'
        if row.old in entries[other]:
            problems.append(f'{row.old}: the same entry exists in both configs (ambiguous prose rewrite)')
        done = not os.path.exists(os.path.join(REPO, row.path(row.old))) and \
            os.path.exists(os.path.join(REPO, row.path(row.new)))
        if done:
            continue
        if row.old not in entries[row.config]:
            problems.append(f'{row.old}: not an entry of the {row.config} config')
        if not os.path.exists(os.path.join(REPO, row.path(row.old))):
            problems.append(f'{row.old}: no source file')
        if os.path.exists(os.path.join(REPO, row.path(row.new))):
            problems.append(f'{row.new}: target file exists')
        if row.new in entries[row.config]:
            problems.append(f'{row.new}: already an entry')
    return problems


def git(*args):
    subprocess.run(['git', *args], cwd=REPO, check=True)


def tracked(rel):
    return subprocess.run(['git', 'ls-files', '--error-unmatch', rel], cwd=REPO,
                          capture_output=True).returncode == 0


def apply_row(row):
    old_path, new_path = row.path(row.old), row.path(row.new)
    if not os.path.exists(os.path.join(REPO, old_path)):
        return False
    os.makedirs(os.path.dirname(os.path.join(REPO, new_path)), exist_ok=True)
    if tracked(old_path):
        git('mv', old_path, new_path)
    else:
        os.rename(os.path.join(REPO, old_path), os.path.join(REPO, new_path))
    full = os.path.join(REPO, new_path)
    with open(full, encoding='utf-8') as handle:
        text = handle.read()
    text = re.sub(r'^package ' + re.escape(row.package(row.old)) + r';', 'package ' + row.package(row.new) + ';',
                  text, count=1, flags=re.M)
    old_simple, new_simple = row.simple(row.old), row.simple(row.new)
    if old_simple != new_simple:
        text = re.sub(r'\b' + re.escape(old_simple) + r'\b', new_simple, text)
    with open(full, 'w', encoding='utf-8') as handle:
        handle.write(text)
    return True


def rewrite_references(rows):
    compiled = [(row, patterns(row)) for row in rows]
    changed = []
    for rel in text_files():
        path = os.path.join(REPO, rel)
        try:
            with open(path, encoding='utf-8') as handle:
                text = handle.read()
        except (UnicodeDecodeError, FileNotFoundError):
            continue
        new_text = text
        for row, pats in compiled:
            for regex, replacement in pats:
                new_text = regex.sub(replacement, new_text)
        if new_text != text:
            with open(path, 'w', encoding='utf-8') as handle:
                handle.write(new_text)
            changed.append(rel)
    return changed


def follow_file_moves(rows):
    with open(FILE_MOVES, encoding='utf-8') as handle:
        lines = handle.read().split('\n')
    header = lines[0].split('\t')
    i_path, i_fqcn = header.index('new_path'), header.index('new_fqcn')
    by_fqcn = {}
    for row in rows:
        prefix = row.root_pkg[len('dev.caecorthus.sparkdlc.'):]
        by_fqcn[prefix + '.' + row.old] = (row, prefix + '.' + row.new)
    updated = 0
    for index, line in enumerate(lines[1:], 1):
        if not line:
            continue
        cols = line.split('\t')
        hit = by_fqcn.get(cols[i_fqcn])
        if hit:
            row, new_fqcn = hit
            cols[i_path] = row.path(row.new)
            cols[i_fqcn] = new_fqcn
            lines[index] = '\t'.join(cols)
            updated += 1
    with open(FILE_MOVES, 'w', encoding='utf-8') as handle:
        handle.write('\n'.join(lines))
    return updated


def leftovers(rows):
    """Bare old simple names still present outside the moved files. / 被搬迁文件之外仍出现的旧简单名。"""
    report = []
    for row in rows:
        old_simple, new_simple = row.simple(row.old), row.simple(row.new)
        if old_simple == new_simple:
            continue
        regex = re.compile(r'\b' + re.escape(old_simple) + r'\b')
        for rel in text_files():
            try:
                with open(os.path.join(REPO, rel), encoding='utf-8') as handle:
                    text = handle.read()
            except (UnicodeDecodeError, FileNotFoundError):
                continue
            for number, line in enumerate(text.split('\n'), 1):
                if regex.search(line):
                    report.append(f'{rel}:{number}: [{row.config} {row.old}] {line.strip()[:160]}')
    return report


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('command', choices=('plan', 'apply'))
    args = parser.parse_args()
    rows = read_rows()
    problems = check(rows)
    if problems:
        print('\n'.join(problems))
        sys.exit(1)
    if args.command == 'apply':
        moved = [row for row in rows if apply_row(row)]
        changed = rewrite_references(rows)
        updated = follow_file_moves(rows)
        print(f'moved {len(moved)} mixins, rewrote {len(changed)} files, updated {updated} file-moves.tsv rows')
    for line in leftovers(rows):
        print('leftover', line)


if __name__ == '__main__':
    main()
