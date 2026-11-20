package dev.caecorthus.sparkdlc.mixin.cooldown.provider;

import dev.caecorthus.sparkdlc.cooldown.provider.RoleItemCooldownReleases;
import dev.caecorthus.sparkdlc.mixin.cooldown.ServerItemCooldownManagerAccessor;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerItemCooldownManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Server only: an explicit {@code remove} (the cooldown service's {@code clearCooldown}, Wathe's round reset) also
 * releases the role timers that gate the same item. Expiry never calls {@code remove}, and a {@code remove} another
 * mixin cancels at HEAD (the traits' forced melee floor) never reaches TAIL, so neither releases anything.
 * 仅服务端：显式 {@code remove}（冷却服务的 {@code clearCooldown}、Wathe 开局重置）同时释放门控同一物品的
 * 职业计时。到期不会调用 {@code remove}；被其他 mixin 在 HEAD 取消的 {@code remove}（天赋的强制近战下限）
 * 到不了 TAIL，两者都不释放任何计时。
 */
@Mixin(ItemCooldownManager.class)
public abstract class RoleItemTimerReleaseMixin {
    @Inject(method = "remove", at = @At("TAIL"))
    private void sparkdlc$releaseOwnTimers(Item item, CallbackInfo ci) {
        if ((Object) this instanceof ServerItemCooldownManager manager) {
            RoleItemCooldownReleases.onRemoved(((ServerItemCooldownManagerAccessor) manager).sparkdlc$getPlayer(), item);
        }
    }
}
