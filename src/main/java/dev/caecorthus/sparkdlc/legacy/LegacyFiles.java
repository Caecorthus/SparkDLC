package dev.caecorthus.sparkdlc.legacy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Carries the config and data files the five former mods wrote over to the names the fused mod reads. The rows are
 * the {@code file} kind of {@link LegacyIds} (old path, new path, both relative to the game directory and inside
 * {@code config/}). The rule is the same for every row and is checked again on every start:
 * <ul>
 *     <li>the new path does not exist and the old one does: the old content is copied to the new path;</li>
 *     <li>the new path exists (a file, a folder, anything): it wins and the old one is ignored;</li>
 *     <li>the old path is never changed or deleted, so it stays behind as the backup of the last state the old
 *     jars saw (and a start with those jars still finds its files).</li>
 * </ul>
 * A row is one of three shapes: a single file, a folder (the row ends with {@code /}: the whole tree is copied when
 * the new folder does not exist), or one file per something (the file name holds a {@code <placeholder>}: every
 * matching file of the old folder is carried over on its own, so one account's achievement save never blocks
 * another's). Every copy goes to a temporary sibling first and is then moved into place, so a crash mid-copy can
 * never leave a half-written file or half-filled folder that would count as "the new one exists".
 *
 * <p>The files are copied byte for byte. Ids stored inside one (only the achievement save has any) are converted
 * where that file is read, see {@link LegacyAchievementSave}; a broken old file is carried over as it is and then
 * handled by its reader exactly like a broken file of the new name.</p>
 *
 * <p>It must run before the first reader of the new paths: {@link #carryOverCommon} at the top of
 * {@code SparkDLC#onInitialize} (both sides), {@link #carryOverAll} at the top of the client entrypoint. A failure
 * is logged and never stops the game: the reader then starts from its defaults, and since the new path was not
 * created the next start tries again.</p>
 *
 * 把五个旧模组写出的配置与数据文件带到融合后模组读取的名字下。数据行是 {@link LegacyIds} 的 {@code file} 种类（旧路径、新路径，
 * 均相对游戏目录且位于 {@code config/} 内）。每一行的规则相同，并且每次启动都会重新检查：新路径不存在而旧路径存在时，把旧内容
 * 复制到新路径；新路径存在（文件、文件夹或其他）时以新的为准，忽略旧的；旧路径绝不修改或删除，留作旧 jar 最后状态的备份
 * （换回旧 jar 启动时也仍能找到自己的文件）。
 *
 * <p>一行有三种形态：单个文件；文件夹（以 {@code /} 结尾：新文件夹不存在时复制整棵目录树）；按某个维度一份的文件（文件名含
 * {@code <占位符>}：旧文件夹中每个匹配的文件各自带过去，因此一个账号的成就存档不会挡住另一个账号的）。每次复制都先写到同目录的
 * 临时文件再移动到位，复制到一半崩溃也不会留下会被当成“新文件已存在”的半截文件或半满文件夹。</p>
 *
 * <p>文件按字节原样复制。文件内保存的 id（只有成就存档有）在读取该文件的地方转换，见 {@link LegacyAchievementSave}；损坏的旧文件
 * 原样带过去，随后由读取方按新名字下的损坏文件同样处理。</p>
 *
 * <p>必须在新路径的第一个读取方之前执行：{@link #carryOverCommon} 位于 {@code SparkDLC#onInitialize} 开头（两端都执行），
 * {@link #carryOverAll} 位于客户端入口开头。失败只记录日志，绝不中断游戏：读取方随后从默认值开始，而新路径没有被创建，
 * 下次启动会再试。</p>
 */
public final class LegacyFiles {
    /** The table kind this class works from. / 本类使用的对照表种类。 */
    public static final String KIND = "file";

    private static final Logger LOGGER = LoggerFactory.getLogger("SparkDLC/Legacy");
    /** Table paths are relative to the game directory; all of them are config files. / 表内路径相对游戏目录，且都是配置文件。 */
    private static final String CONFIG_PREFIX = "config/";
    private static final String TEMPORARY_SUFFIX = ".legacy-tmp";
    /**
     * The one file both sides read (the cached Caecorthus team list, {@code CaecorthusTeamSync}); every other row is
     * read by the client only.
     * 唯一两端都会读取的文件（缓存的 Caecorthus 团队名单，{@code CaecorthusTeamSync}）；其余各行都只由客户端读取。
     */
    private static final String COMMON_FILE = "config/sparkdlc/caecorthus_team.json";

    private LegacyFiles() {
    }

    /**
     * The files a dedicated server reads too. Returns the new paths that were created.
     * 专用服务器也会读取的文件。返回新创建的新路径。
     */
    public static List<Path> carryOverCommon(Path configDirectory) {
        List<Path> carried = new ArrayList<>();
        LegacyIds.renamed(KIND).forEach((oldPath, newPath) -> {
            if (COMMON_FILE.equals(newPath)) {
                carryOver(configDirectory, oldPath, newPath, carried);
            }
        });
        return carried;
    }

    /**
     * Every row of the table: what a client reads. Returns the new paths that were created.
     * 对照表中的每一行：客户端读取的全部文件。返回新创建的新路径。
     */
    public static List<Path> carryOverAll(Path configDirectory) {
        List<Path> carried = new ArrayList<>();
        LegacyIds.renamed(KIND).forEach((oldPath, newPath) -> carryOver(configDirectory, oldPath, newPath, carried));
        return carried;
    }

    /**
     * Replaces {@code file} with {@code bytes} through a temporary sibling that is forced to disk and then moved
     * over it (atomically where the file system allows), so a crash mid-write keeps the previous file. A symbolic
     * link is followed, so a linked file stays a link.
     * 经由同目录的临时文件用 {@code bytes} 替换 {@code file}：先刷到磁盘再移动覆盖（文件系统支持时为原子操作），写到一半崩溃时
     * 保留原文件。符号链接会被跟随，因此被链接的文件仍然是链接。
     */
    public static void writeAtomically(Path file, byte[] bytes) throws IOException {
        Path target = Files.exists(file) ? file.toRealPath() : file;
        Path temporary = temporaryFor(target);
        try {
            try (FileChannel channel = FileChannel.open(temporary,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            moveIntoPlace(temporary, target);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void carryOver(Path configDirectory, String oldPath, String newPath, List<Path> carried) {
        String oldRelative = insideConfig(oldPath);
        String newRelative = insideConfig(newPath);
        try {
            if (oldRelative.endsWith("/")) {
                Path newDirectory = configDirectory.resolve(newRelative);
                if (carryDirectory(configDirectory.resolve(oldRelative), newDirectory)) {
                    carried.add(newDirectory);
                }
                return;
            }
            String name = oldRelative.substring(oldRelative.lastIndexOf('/') + 1);
            int open = name.indexOf('<');
            int close = name.lastIndexOf('>');
            if (open < 0 || close < open) {
                Path newFile = configDirectory.resolve(newRelative);
                if (carryFile(configDirectory.resolve(oldRelative), newFile)) {
                    carried.add(newFile);
                }
                return;
            }
            // "<account uuid>.json": whatever stands for the placeholder, the prefix and suffix around it must match;
            // a file keeps its name in the new folder. The placeholder itself never becomes a Path ('<' and '>' are
            // not valid in a Windows file name).
            // “<account uuid>.json”：占位符可以是任何内容，但其前后的前缀与后缀必须匹配；文件在新文件夹中保持原名。
            // 占位符本身绝不会被构造成 Path（“<”和“>”不是合法的 Windows 文件名字符）。
            carryMatchingFiles(configDirectory.resolve(parentOf(oldRelative)),
                    configDirectory.resolve(parentOf(newRelative)), name.substring(0, open),
                    name.substring(close + 1), carried);
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Could not carry {} over to {}; it is tried again on the next start", oldPath, newPath,
                    exception);
        }
    }

    private static String insideConfig(String tablePath) {
        if (!tablePath.startsWith(CONFIG_PREFIX) || tablePath.contains("..")) {
            throw new IllegalStateException("legacy-ids.tsv: a file row must stay inside config/: " + tablePath);
        }
        return tablePath.substring(CONFIG_PREFIX.length());
    }

    private static String parentOf(String relativePath) {
        int slash = relativePath.lastIndexOf('/');
        return slash < 0 ? "" : relativePath.substring(0, slash);
    }

    private static boolean carryFile(Path oldFile, Path newFile) throws IOException {
        // Anything at the new path wins, a dangling link included. 新路径上有任何东西都以它为准，包括悬空的链接。
        if (Files.exists(newFile, LinkOption.NOFOLLOW_LINKS) || !Files.isRegularFile(oldFile)) {
            return false;
        }
        Files.createDirectories(newFile.getParent());
        Path temporary = temporaryFor(newFile);
        try {
            Files.copy(oldFile, temporary, StandardCopyOption.REPLACE_EXISTING);
            moveIntoPlace(temporary, newFile);
        } finally {
            Files.deleteIfExists(temporary);
        }
        LOGGER.info("Carried {} over to {}; the old file is kept as a backup", oldFile, newFile);
        return true;
    }

    private static void carryMatchingFiles(Path oldDirectory, Path newDirectory, String prefix, String suffix,
                                           List<Path> carried) throws IOException {
        if (!Files.isDirectory(oldDirectory)) {
            return;
        }
        List<Path> oldFiles;
        try (Stream<Path> children = Files.list(oldDirectory)) {
            oldFiles = children.sorted().toList();
        }
        for (Path oldFile : oldFiles) {
            String name = oldFile.getFileName().toString();
            if (name.length() <= prefix.length() + suffix.length() || !name.startsWith(prefix)
                    || !name.endsWith(suffix)) {
                continue;
            }
            Path newFile = newDirectory.resolve(name);
            try {
                if (carryFile(oldFile, newFile)) {
                    carried.add(newFile);
                }
            } catch (IOException exception) {
                // One unreadable save must not keep the other accounts' saves back. 一个读不出来的存档不能连累其他账号的存档。
                LOGGER.warn("Could not carry {} over to {}; it is tried again on the next start", oldFile, newFile,
                        exception);
            }
        }
    }

    private static boolean carryDirectory(Path oldDirectory, Path newDirectory) throws IOException {
        if (Files.exists(newDirectory, LinkOption.NOFOLLOW_LINKS) || !Files.isDirectory(oldDirectory)) {
            return false;
        }
        Files.createDirectories(newDirectory.getParent());
        Path temporary = temporaryFor(newDirectory);
        try {
            // A leftover of an interrupted earlier attempt. 之前被中断的尝试留下的残余。
            deleteTree(temporary);
            copyTree(oldDirectory, temporary);
            moveIntoPlace(temporary, newDirectory);
        } finally {
            deleteTree(temporary);
        }
        LOGGER.info("Carried the folder {} over to {}; the old folder is kept as a backup", oldDirectory, newDirectory);
        return true;
    }

    private static void copyTree(Path from, Path to) throws IOException {
        Files.walkFileTree(from, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes)
                    throws IOException {
                Files.createDirectories(to.resolve(from.relativize(directory).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                Path target = to.resolve(from.relativize(file).toString());
                if (!attributes.isSymbolicLink()) {
                    Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES);
                    return FileVisitResult.CONTINUE;
                }
                // The tree is not followed out of the folder; a link is carried over as a link where the platform
                // lets us create one, and otherwise named in the log (it is still in the old folder).
                // 遍历不会跟随链接离开该文件夹；平台允许时链接按链接带过去，否则在日志中指出（它仍在旧文件夹里）。
                try {
                    Files.copy(file, target, LinkOption.NOFOLLOW_LINKS);
                } catch (IOException | UnsupportedOperationException exception) {
                    LOGGER.warn("Could not carry the link {} over; it stays in the old folder", file, exception);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException failure) throws IOException {
                if (failure != null) {
                    throw failure;
                }
                Files.delete(directory);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static Path temporaryFor(Path target) {
        return target.resolveSibling(target.getFileName() + TEMPORARY_SUFFIX);
    }

    private static void moveIntoPlace(Path temporary, Path target) throws IOException {
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
