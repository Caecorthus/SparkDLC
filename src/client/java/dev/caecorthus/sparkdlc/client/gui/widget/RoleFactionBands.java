package dev.caecorthus.sparkdlc.client.gui.widget;

import dev.caecorthus.sparkdlc.faction.api.SparkFactionApi;
import dev.caecorthus.sparkdlc.SparkDlcFactions;
import dev.caecorthus.sparkdlc.role.civilian.detective.DetectiveCaseRules;
import dev.caecorthus.sparkdlc.role.civilian.detective.DetectiveCaseRules.RolePickerBand;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Faction bands shared by the role pickers (the Detective's killer pick, the Assassin's guess): killers, neutrals,
 * the witch faction, Team Jiahao and innocents, plus each band's accent colour and the role display name.
 * 身份选择器共用的阵营分组（侦探指认凶手、刺客猜身份）：杀手、中立、魔女阵营、嘉豪阵营与平民，
 * 以及各组的强调色与身份显示名。
 */
public final class RoleFactionBands {
    private static final int KILLERS_ACCENT = 0xFFC8372D;
    private static final int NEUTRALS_ACCENT = 0xFFB08A2E;
    // The witch faction colour. / 魔女阵营颜色。
    private static final int WITCHES_ACCENT = 0xFFE9D5F0;
    // The Team Jiahao colour. / 嘉豪阵营颜色。
    private static final int JIAHAO_ACCENT = 0xFFFFC125;
    private static final int INNOCENTS_ACCENT = 0xFF3E8E4E;

    private RoleFactionBands() {
    }

    /**
     * Witch faction and Team Jiahao come from the faction contract's role-only base faction (deterministic on client
     * and server): as native Wathe factions they no longer read {@code isNeutral()} and would otherwise fall into the
     * innocent band. The role must not be null.
     * 魔女阵营与嘉豪阵营取自阵营契约仅由身份决定的基础阵营（客户端与服务端结果一致）：作为原生 Wathe 阵营它们的 isNeutral()
     * 已为 false，否则会落入平民组。身份不得为 null。
     */
    public static RolePickerBand bandOf(Role role) {
        Identifier faction = SparkFactionApi.resolveBaseFaction(role);
        return DetectiveCaseRules.rolePickerBand(
                role.canUseKiller(),
                SparkDlcFactions.WITCH.equals(faction),
                SparkDlcFactions.JIAHAO.equals(faction),
                SparkFactionApi.isNeutralAligned(role));
    }

    /** Opaque ARGB accent of a band. / 分组的不透明 ARGB 强调色。 */
    public static int accent(RolePickerBand band) {
        return switch (band) {
            case KILLERS -> KILLERS_ACCENT;
            case NEUTRALS -> NEUTRALS_ACCENT;
            case WITCHES -> WITCHES_ACCENT;
            case JIAHAO -> JIAHAO_ACCENT;
            case INNOCENTS -> INNOCENTS_ACCENT;
        };
    }

    /** Role display name; mods without the lang key fall back to the id path. / 角色显示名，缺少语言键时回退为 ID 路径。 */
    public static Text roleName(Role role) {
        String path = role.identifier().getPath();
        return Text.translatableWithFallback("announcement.role." + path, path);
    }
}
