package dev.caecorthus.sparkdlc.mixin.record;

import dev.caecorthus.sparkdlc.role.civilian.junker.JunkerLootSpend;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaReplayService;
import dev.caecorthus.sparkdlc.role.special.wraith.conversion.WraithSwapperFallAttribution;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Spends used 拾荒者 loot first, then keeps Vendetta terminal replay exclusive. (Until 2026-11-28 the remaining
 * confirmed item-use records also re-triggered Saint Karma; Hellfire is inflicted once now and nothing re-triggers it.)
 * 先消耗被使用的拾荒者战利品，再保证仇杀终止回放唯一。（2026-11-28 之前，其余已确认成功的物品使用记录还会再次触发圣徒业障；
 * 如今业火只施加一次，不再被任何行为再次触发。）
 */
@Mixin(GameRecordManager.class)
public abstract class RoleItemUseRecordHooksMixin {
    @Inject(method = "recordItemUse", at = @At("HEAD"), cancellable = true)
    private static void sparkdlc$spendLootAndHideVendettaRecord(
            ServerPlayerEntity actor,
            Identifier itemId,
            ServerPlayerEntity target,
            NbtCompound extra,
            CallbackInfo ci
    ) {
        // Ahead of the suppression: a hidden Vendetta record is still a real use. / 先于屏蔽：被隐藏的仇杀记录仍是一次真实使用。
        JunkerLootSpend.onRecordedItemUse(actor, itemId, extra);
        if (VendettaReplayService.shouldSuppressItemRecord(actor, target)) {
            ci.cancel();
        }
    }

    @Inject(method = "recordSkillUse", at = @At("HEAD"), cancellable = true)
    private static void sparkdlc$hideVendettaTerminalSkillRecord(
            ServerPlayerEntity actor,
            Identifier skillId,
            ServerPlayerEntity target,
            NbtCompound extra,
            CallbackInfo ci
    ) {
        if (VendettaReplayService.shouldSuppressSkillRecord(actor, target)) {
            ci.cancel();
        }
    }

    /** Observes only NoellesRoles' already-confirmed successful Swapper replay. */
    @Inject(method = "recordSkillUse", at = @At("TAIL"))
    private static void sparkdlc$captureSuccessfulSwapper(
            ServerPlayerEntity actor,
            Identifier skillId,
            ServerPlayerEntity target,
            NbtCompound extra,
            CallbackInfo ci
    ) {
        WraithSwapperFallAttribution.onRecordedSkillUse(actor, skillId, target, extra);
    }
}
