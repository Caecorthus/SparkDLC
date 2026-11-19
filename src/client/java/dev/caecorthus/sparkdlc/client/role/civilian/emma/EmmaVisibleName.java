package dev.caecorthus.sparkdlc.client.role.civilian.emma;

import dev.caecorthus.sparkdlc.client.role.killer.morphling.MorphlingAppearanceClientHelper;
import dev.doctor4t.wathe.client.WatheClient;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.noellesroles.client.jester.JesterMomentClient;
import org.agmas.noellesroles.morphling.MorphlingPlayerComponent;

/** Resolve displayed identities without recursively following another player's disguise.
 * 解析可见身份，不递归追踪被模仿玩家当前的伪装。 */
public final class EmmaVisibleName {
    private EmmaVisibleName() { }

    public static Text of(PlayerEntity target) {
        if (target.hasStatusEffect(StatusEffects.INVISIBILITY) || target.isInvisible() || JesterMomentClient.isActive()) {
            return hidden();
        }
        MorphlingPlayerComponent morph = MorphlingPlayerComponent.KEY.get(target);
        if (morph.corpseMode) return hidden();
        if (morph.getMorphTicks() > 0) return morph.disguise == null ? hidden() : profileName(morph.disguise);
        // SparkStrength's own disguises (Coroner, morph potion); a direct call since the SparkDLC fusion.
        // SparkStrength 自身的伪装（验尸官、变形试剂）；融合进 SparkDLC 后为直接调用。
        Text strengthName = MorphlingAppearanceClientHelper.resolveActiveDisplayName(target);
        if (strengthName != null) return strengthName.copy();
        return Text.literal(target.getGameProfile().getName());
    }

    private static Text profileName(UUID identity) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerListEntry entry = client.getNetworkHandler() == null ? null : client.getNetworkHandler().getPlayerListEntry(identity);
        if (entry == null && WatheClient.PLAYER_ENTRIES_CACHE != null) entry = WatheClient.PLAYER_ENTRIES_CACHE.get(identity);
        if (entry != null) return Text.literal(entry.getProfile().getName());
        PlayerEntity player = client.world == null ? null : client.world.getPlayerByUuid(identity);
        return player == null ? hidden() : Text.literal(player.getGameProfile().getName());
    }
    private static Text hidden() { return Text.literal("????????").formatted(Formatting.OBFUSCATED); }
}
