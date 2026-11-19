package dev.caecorthus.sparkdlc.client.bridge.strength;

import dev.caecorthus.sparkdlc.client.api.SecondarySkillKeyApi;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * SparkWitch “职业技能 2”键（默认 N）的客户端桥。
 * Client-only bridge to SparkWitch's Role Skill 2 key (default N).
 *
 * <p>各模块已融合进 SparkDLC，因此这里直接调用其公开门面 {@link SecondarySkillKeyApi} 的
 * {@code register(Identifier, Runnable)} 与 {@code boundKeyText()}，不再按名称反射，也不再检测 sparkwitch 是否加载。
 * The modules are fused in SparkDLC, so this calls that public facade directly, with no lookup by name and no
 * mod-loaded gate.</p>
 */
public final class SparkWitchSecondaryKeyCompat {
    private SparkWitchSecondaryKeyCompat() {
    }

    /**
     * Puts {@code onPressed} on Role Skill 2 for {@code roleId}; SparkWitch runs it on the client thread only while the
     * local player's role id matches. False when the role already has a handler.
     * 为 {@code roleId} 挂接“职业技能 2”；SparkWitch 仅在本地玩家职业匹配时于客户端线程调用。
     * 该职业已有处理器时返回 false。
     */
    public static boolean register(Identifier roleId, Runnable onPressed) {
        return SecondarySkillKeyApi.register(roleId, onPressed);
    }

    /** Localized Role Skill 2 key name. / “职业技能 2”当前按键名。 */
    public static @Nullable Text boundKeyText() {
        return SecondarySkillKeyApi.boundKeyText();
    }
}
