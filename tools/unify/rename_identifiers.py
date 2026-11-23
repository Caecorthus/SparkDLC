#!/usr/bin/env python3
"""Renames the identifiers that only carried a former mod's name (stage 3, "One name") from identifier-renames.tsv.

    python3 tools/unify/rename_identifiers.py plan     # checks the table against the tree, prints what would change
    python3 tools/unify/rename_identifiers.py apply    # rewrites the files (runs the plan's checks first)

Each row renames one whole Java identifier (a match never touches a longer identifier that contains it: `$` counts as
part of an identifier). `old` ending in "(" matches only where a "(" follows (calls and declarations). The plan
fails when `old` is missing from a file of `declared_in`, when `new` already occurs in one of them (a clash), or when
two rows rename one name. Scope `tree` rewrites every source file that names `old`: src/main/java, src/client/java,
src/gametest/java, the local tests and harness under src/test (Java and Python; not the Markdown records there) and
the module docs (docs/core, docs/roles, docs/client-helpers); scope `declared` rewrites the declaring files only.

按 identifier-renames.tsv 为只带有旧模组名字的标识符改名（第三阶段“统一名字”）。每行改一个完整的 Java 标识符（匹配绝不触及包含它的
更长标识符：`$` 视为标识符的一部分）；以“(”结尾的 old 只匹配后面跟着“(”的位置。old 不在 declared_in 的某个文件中、new 已出现在
其中（冲突）或两行改同一个名字时，plan 失败。scope 为 tree 时改写所有引用 old 的源码文件（两棵生产代码树、gametest、src/test 下的
本地测试与工具（Java 与 Python；不含其中的 Markdown 记录）以及模块文档），为 declared 时只改声明文件。
"""
import csv
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
TABLE = ROOT / 'tools/unify/identifier-renames.tsv'
TREES = ('src/main/java', 'src/client/java', 'src/gametest/java', 'src/test', 'docs/core', 'docs/roles',
         'docs/client-helpers')
SUFFIXES = {'.java': ('src/',), '.py': ('src/test/',), '.md': ('docs/',)}


def rows():
    with TABLE.open(encoding='utf-8') as table:
        lines = [line for line in table if not line.startswith('#')]
    return list(csv.DictReader(lines, delimiter='\t'))


def pattern(old):
    call = old.endswith('(')
    name = old[:-1] if call else old
    return re.compile(r'(?<![A-Za-z0-9_$])' + re.escape(name) + (r'(?=\()' if call else r'(?![A-Za-z0-9_$])'))


def bare(name):
    return name[:-1] if name.endswith('(') else name


def source_files():
    for tree in TREES:
        base = ROOT / tree
        if not base.exists():
            continue
        for path in sorted(base.rglob('*')):
            relative = path.relative_to(ROOT).as_posix()
            prefixes = SUFFIXES.get(path.suffix)
            if path.is_file() and prefixes and relative.startswith(prefixes) and '/build/' not in relative:
                yield relative


def plan():
    table = rows()
    problems = []
    seen = {}
    for row in table:
        if row['old'] in seen:
            problems.append('%s is renamed twice' % row['old'])
        seen[row['old']] = row['new']
        if row['scope'] not in ('tree', 'declared'):
            problems.append('%s: unknown scope %s' % (row['old'], row['scope']))
        new = pattern(row['new'])
        for declared in row['declared_in'].split(','):
            path = ROOT / declared
            if not path.is_file():
                problems.append('%s: no file %s' % (row['old'], declared))
                continue
            text = path.read_text(encoding='utf-8')
            if not pattern(row['old']).search(text):
                problems.append('%s: not in %s' % (row['old'], declared))
            # A new name already in the declaring file clashes, unless another row renames that name away there.
            # 声明文件中已有新名字即为冲突，除非另一行把该名字改掉。
            if new.search(text) and bare(row['new']) not in [bare(r['old']) for r in table]:
                problems.append('%s -> %s: %s already names %s' % (row['old'], row['new'], declared, row['new']))
    changes = {}
    files = list(source_files())
    for row in table:
        targets = row['declared_in'].split(',') if row['scope'] == 'declared' else files
        regex = pattern(row['old'])
        for relative in targets:
            text = (ROOT / relative).read_text(encoding='utf-8')
            count = len(regex.findall(text))
            if count:
                changes.setdefault(relative, []).append((row, count))
    return table, problems, changes


def main(argv):
    if len(argv) != 2 or argv[1] not in ('plan', 'apply'):
        print(__doc__)
        return 2
    table, problems, changes = plan()
    for relative in sorted(changes):
        print(relative)
        for row, count in changes[relative]:
            print('    %s -> %s (%d)' % (row['old'], row['new'], count))
    print('%d rows, %d files, %d replacements' % (len(table), len(changes),
                                                   sum(count for edits in changes.values() for _, count in edits)))
    if problems:
        print('\n'.join('PROBLEM: ' + problem for problem in problems))
        return 1
    if argv[1] == 'apply':
        for relative, edits in changes.items():
            path = ROOT / relative
            text = path.read_text(encoding='utf-8')
            for row, _ in edits:
                text = pattern(row['old']).sub(bare(row['new']).replace('\\', '\\\\'), text)
            path.write_text(text, encoding='utf-8')
        print('applied')
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv))
