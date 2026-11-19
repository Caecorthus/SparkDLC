package dev.caecorthus.sparkdlc.bridge.witch;

import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerBodySnapshotComponent;
import dev.caecorthus.sparkdlc.role.civilian.coroner.CoronerService;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * SparkStrength 兼容桥。
 *
 * <p>各模块已融合进 SparkDLC，SparkStrength 始终随同一个 jar 发布，因此这里直接调用（不再反射、不再检查模组是否加载）
 * “验尸官是否正伪装为 SparkWitch 绑架者”，使迷药对伪装成绑架者的验尸官同样生效；
 * 并为魔人假死的假尸体与魔术师诱饵尸体写入验尸官读取的身份快照。</p>
 *
 * <p>The modules are fused in SparkDLC and SparkStrength always ships in the same jar, so this calls
 * {@link CoronerService} and {@link CoronerBodySnapshotComponent} directly: no reflection, no mod-presence gate.</p>
 */
public final class SparkStrengthCoronerCompat {
    private SparkStrengthCoronerCompat() {
    }

    /**
     * Writes the role SparkStrength's Coroner reads from a body SparkWitch spawned itself (the Fiend's feign-death
     * corpse, the Magician decoy); SparkStrength only snapshots bodies made inside Wathe's kill and otherwise falls back
     * to the owner's live role. A direct call since the SparkDLC fusion: the snapshot component
     * ({@code sparkdlc:coroner_body_snapshot}) is read through its own key and its public setter is called; an
     * entity without the component (not a Wathe body) is left as is.
     * 为 SparkWitch 自行生成的尸体（魔人假死的假尸体、魔术师诱饵尸体）写入 SparkStrength 验尸官读取的身份；SparkStrength 只会快照 Wathe
     * 击杀流程中生成的尸体，否则回落到尸体主人的实时身份。融合进 SparkDLC 后为直接调用：经快照组件
     * （{@code sparkdlc:coroner_body_snapshot}）自身的键读取并调用其 public setter；没有该组件的实体（不是 Wathe 尸体）
     * 保持不变。
     */
    public static void recordBodyRole(Entity body, Identifier roleId) {
        if (body == null || roleId == null) {
            return;
        }
        CoronerBodySnapshotComponent snapshot = CoronerBodySnapshotComponent.KEY.getNullable(body);
        if (snapshot != null) {
            snapshot.setRoleId(roleId);
        }
    }

    public static boolean hasKidnapperDisguise(PlayerEntity player) {
        return player != null && CoronerService.hasKidnapperDisguise(player);
    }
}
