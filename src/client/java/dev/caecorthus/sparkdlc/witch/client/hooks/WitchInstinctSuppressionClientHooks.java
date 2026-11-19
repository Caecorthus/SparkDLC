package dev.caecorthus.sparkdlc.witch.client.hooks;

import dev.caecorthus.sparkdlc.traits.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.witch.SparkWitchRoles;
import dev.caecorthus.sparkdlc.witch.compat.NoellesTaotieSeekerBridge;
import dev.caecorthus.sparkdlc.witch.component.WitchWorldComponent;
import dev.caecorthus.sparkdlc.witch.roles.civilian.apprentice.ApprenticeFearExemption;
import dev.caecorthus.sparkdlc.witch.roles.witch.WitchFactionRules;
import dev.caecorthus.sparkdlc.witch.net.SparkWitchServerConnection;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.event.GetInstinctHighlight;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/**
 * High-priority client gate for Grand Witch area spells that must beat always-on role highlights.
 * 大魔女范围法术的高优先级客户端拦截，用来压过常驻角色高亮。
 */
public final class WitchInstinctSuppressionClientHooks {
    public static final int SUPPRESSION_PRIORITY = GetInstinctHighlight.HighlightResult.PRIORITY_HIGH + 2;

    private WitchInstinctSuppressionClientHooks() {
    }

    public static void register() {
        GetInstinctHighlight.EVENT.register(WitchInstinctSuppressionClientHooks::suppressAffectedInstinctHighlight);
    }

    private static GetInstinctHighlight.HighlightResult suppressAffectedInstinctHighlight(Entity target) {
        if (!shouldSuppressInstinctHighlight()) {
            return null;
        }
        return new GetInstinctHighlight.HighlightResult(-1, false, SUPPRESSION_PRIORITY);
    }

    public static boolean shouldSuppressInstinctHighlight() {
        if (!SparkWitchServerConnection.isConfirmedServer()) {
            return false;
        }
        PlayerEntity viewer = MinecraftClient.getInstance().player;
        if (viewer == null) {
            return false;
        }

        World world = viewer.getWorld();
        GameWorldComponent gameComponent = GameWorldComponent.KEY.get(world);
        WitchWorldComponent witchWorld = WitchWorldComponent.KEY.get(world);
        Role viewerRole = gameComponent.getRole(viewer);
        // The Apprentice Witch is spell-immune and a Healing ward lifts Fear (owner 2026-10-06 D4/D8); her own skill
        // outlines then answer before Obscure inside WitchInstinctPolicy.
        // 预备魔女不受法术影响，疗愈庇护解除恐惧（所有者 2026-10-06 D4/D8）；她自己的技能描边随后在 WitchInstinctPolicy
        // 中先于障眼作答。
        boolean apprentice = viewerRole == SparkWitchRoles.apprenticeWitch();
        // An active Final Moment only ever lifts a suppression (the rule returns false first), so it is passed as
        // false and the SparkTraits read runs only when Fear or Obscure would suppress: same answer.
        // 激活的最终时刻只会解除压制（规则会先返回 false），因此此处传入 false，仅当恐惧或障眼会生效时才读取
        // SparkTraits：结果不变。
        return WitchFactionRules.shouldSuppressAffectedInstinctHighlight(
                witchWorld.getFearTicks() > 0 && !ApprenticeFearExemption.isExempt(viewer),
                witchWorld.isInstinctObscured() && !apprentice,
                viewerRole,
                GameFunctions.isPlayerPlayingAndAlive(viewer),
                false
        ) && !isSparkTraitsFinalMomentActive(world);
    }

    public static boolean shouldSuppressSwallowedInstinctHighlight(Entity target) {
        if (!SparkWitchServerConnection.isConfirmedServer()) {
            return false;
        }
        PlayerEntity viewer = MinecraftClient.getInstance().player;
        // NoellesRoles is a hard dependency: read its synced swallowed flag directly (a missing component reads false).
        // NoellesRoles 是硬依赖：直接读取其同步的被吞标记（组件缺失时视为 false）。
        if (NoellesTaotieSeekerBridge.isSwallowed(viewer)) {
            return true;
        }
        return target instanceof PlayerEntity targetPlayer && NoellesTaotieSeekerBridge.isSwallowed(targetPlayer);
    }

    // SparkTraits is fused into SparkDLC: a direct call to its stable public facade; a null world is not active.
    // SparkTraits 已融合进 SparkDLC：直接调用其稳定公共门面；世界为空时视为未激活。
    private static boolean isSparkTraitsFinalMomentActive(World world) {
        return world != null && SparkTraitsApi.isFinalMomentActive(world);
    }
}
