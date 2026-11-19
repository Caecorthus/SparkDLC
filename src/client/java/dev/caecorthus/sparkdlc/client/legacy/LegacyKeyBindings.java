package dev.caecorthus.sparkdlc.client.legacy;

import dev.caecorthus.sparkdlc.legacy.LegacyFiles;
import dev.caecorthus.sparkdlc.legacy.LegacyIds;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps a player's custom keys across the rename of the key bindings ({@code key_binding} rows of
 * {@link LegacyIds}, e.g. {@code key.sparkwitch.secondary_skill} is now {@code key.sparkdlc.role_skill_2}).
 *
 * <p>Vanilla stores a binding in {@code options.txt} as the line {@code key_<translation key>:<bound key>}. On load
 * ({@code GameOptions#load}) every line is split at its first colon into a map, and each binding that exists asks
 * for its own line only; a line of a name no binding has is never read and is dropped by the next
 * {@code GameOptions#write}, which rewrites the file from the bindings that exist. So after the rename the custom
 * key would reset to the default and the old line would be gone for good at the first save.</p>
 *
 * <p>{@link #carryOver(Path)} therefore appends, for each renamed binding, the line {@code key_<new>:<value>} when
 * the file has a {@code key_<old>} line and no {@code key_<new>} line yet. It runs in the client entrypoint, which
 * Fabric Loader calls inside {@code MinecraftClient.<init>} just before {@code new GameOptions(...)} reads the
 * file, so vanilla finds the line under the new name by itself; no mixin is involved. Nothing else in the file is
 * touched: the existing bytes are written back unchanged with the new lines after them. Once vanilla has saved the
 * options the new line exists (so nothing is appended again) and the old one is gone; until then the old line is
 * still there but a new-name line always wins, so a key the player changes afterwards is never overridden. A value
 * vanilla could not parse is not carried over, because under a name that IS read it would make the whole options
 * load fail.</p>
 *
 * 在按键绑定改名（{@link LegacyIds} 的 {@code key_binding} 行，例如 {@code key.sparkwitch.secondary_skill} 现在是
 * {@code key.sparkdlc.role_skill_2}）之后保留玩家自定义的按键。
 *
 * <p>原版把绑定保存在 {@code options.txt} 中，形如 {@code key_<翻译键>:<绑定的键>}。读取时（{@code GameOptions#load}）每一行在
 * 第一个冒号处拆开放进映射，每个存在的绑定只查询自己的那一行；没有任何绑定使用的名字所在的行不会被读取，并会在下一次
 * {@code GameOptions#write}（按现有绑定重写整个文件）时被丢弃。因此改名之后自定义按键会回到默认值，旧行也会在第一次保存时永久消失。</p>
 *
 * <p>所以 {@link #carryOver(Path)} 对每个改名的绑定，在文件里有 {@code key_<旧>} 行而还没有 {@code key_<新>} 行时追加一行
 * {@code key_<新>:<值>}。它在客户端入口中执行，而 Fabric Loader 在 {@code MinecraftClient.<init>} 内、紧接在
 * {@code new GameOptions(...)} 读取文件之前调用客户端入口，于是原版自己就能在新名字下找到这一行，不需要任何 mixin。文件中的其他
 * 内容不会改动：原有字节原样写回，新行追加在其后。原版保存过选项之后新行已经存在（不会再次追加）、旧行消失；在那之前旧行仍在，
 * 但新名字的行始终优先，所以玩家之后修改的按键不会被覆盖。原版无法解析的值不会带过去，因为放在一个“会被读取”的名字下，
 * 它会让整个选项读取失败。</p>
 */
public final class LegacyKeyBindings {
    /** The table kind this class works from. / 本类使用的对照表种类。 */
    public static final String KIND = "key_binding";

    private static final Logger LOGGER = LoggerFactory.getLogger("SparkDLC/Legacy");
    private static final String LINE_PREFIX = "key_";

    private LegacyKeyBindings() {
    }

    /**
     * Carries the old key lines of {@code optionsFile} over; a missing file needs nothing. Never throws: on a
     * failure the file is left as it was and the binding starts from its default key.
     * 把 {@code optionsFile} 里的旧按键行带过去；文件不存在时无事可做。绝不抛出异常：失败时文件保持原样，绑定从默认键开始。
     */
    public static void carryOver(Path optionsFile) {
        try {
            List<String> added = carryOver(optionsFile, LegacyIds.renamed(KIND), LegacyKeyBindings::isBoundKey);
            if (!added.isEmpty()) {
                LOGGER.info("Carried {} key binding(s) over to their new names in {}: {}", added.size(), optionsFile,
                        added);
            }
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Could not carry the old key bindings over in {}; they start from their default keys",
                    optionsFile, exception);
        }
    }

    /**
     * {@link #carryOver(Path)} for the given renames (old translation key to new); {@code boundKey} says whether
     * vanilla can parse a stored value. Returns the appended lines.
     * 针对给定改名表（旧翻译键到新翻译键）的 {@link #carryOver(Path)}；{@code boundKey} 判断原版能否解析保存的值。
     * 返回追加的行。
     */
    static List<String> carryOver(Path optionsFile, Map<String, String> renamed, Predicate<String> boundKey)
            throws IOException {
        if (!Files.isRegularFile(optionsFile)) {
            return List.of();
        }
        byte[] bytes = Files.readAllBytes(optionsFile);
        // ISO-8859-1 maps every byte to one char, so a line that is not valid UTF-8 cannot disturb the parsing, and
        // the lines looked at here are ASCII. The bytes themselves are written back, not this text.
        // ISO-8859-1 把每个字节映射为一个字符，不是合法 UTF-8 的行不会影响解析，而这里关心的行都是 ASCII。写回的是原始字节，
        // 不是这段文本。
        String text = new String(bytes, StandardCharsets.ISO_8859_1);
        List<String> added = linesToAdd(text, renamed, boundKey);
        if (added.isEmpty()) {
            return added;
        }
        String separator = text.contains("\r\n") ? "\r\n" : "\n";
        ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length + 128);
        out.writeBytes(bytes);
        if (bytes.length > 0 && bytes[bytes.length - 1] != '\n' && bytes[bytes.length - 1] != '\r') {
            out.writeBytes(separator.getBytes(StandardCharsets.ISO_8859_1));
        }
        for (String line : added) {
            out.writeBytes((line + separator).getBytes(StandardCharsets.ISO_8859_1));
        }
        LegacyFiles.writeAtomically(optionsFile, out.toByteArray());
        return added;
    }

    /**
     * The {@code key_<new>:<value>} lines {@code optionsText} lacks. Read as vanilla reads the file: a line is split
     * at its first colon, a line without one is skipped, and of two lines with one name the later one counts.
     * {@code optionsText} 缺少的 {@code key_<新>:<值>} 行。按原版读取文件的方式解析：每行在第一个冒号处拆开，没有冒号的行跳过，
     * 同名的两行以后出现的为准。
     */
    static List<String> linesToAdd(String optionsText, Map<String, String> renamed, Predicate<String> boundKey) {
        Map<String, String> options = new HashMap<>();
        for (String line : optionsText.split("\r\n|\r|\n")) {
            int colon = line.indexOf(':');
            if (colon >= 0) {
                options.put(line.substring(0, colon), line.substring(colon + 1));
            }
        }
        // Table order decides when two old names lead to one new name. 两个旧名字指向同一个新名字时按表内顺序决定。
        Map<String, String> added = new LinkedHashMap<>();
        renamed.forEach((oldName, newName) -> {
            String newLine = LINE_PREFIX + newName;
            String value = options.get(LINE_PREFIX + oldName);
            if (value != null && !options.containsKey(newLine) && !added.containsKey(newLine)
                    && boundKey.test(value)) {
                added.put(newLine, value);
            }
        });
        List<String> lines = new ArrayList<>();
        added.forEach((name, value) -> lines.add(name + ":" + value));
        return lines;
    }

    /**
     * Whether vanilla can turn {@code value} back into a key ({@code key.keyboard.x}, {@code key.mouse.4},
     * {@code scancode.42}, {@code key.keyboard.unknown} for "not bound").
     * 原版能否把 {@code value} 还原成按键（{@code key.keyboard.x}、{@code key.mouse.4}、{@code scancode.42}，以及表示“未绑定”的
     * {@code key.keyboard.unknown}）。
     */
    private static boolean isBoundKey(String value) {
        try {
            InputUtil.fromTranslationKey(value);
            return true;
        } catch (RuntimeException unknown) {
            return false;
        }
    }
}
