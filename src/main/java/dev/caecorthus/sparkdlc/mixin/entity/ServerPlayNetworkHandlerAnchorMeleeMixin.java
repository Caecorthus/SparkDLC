package dev.caecorthus.sparkdlc.mixin.entity;

import dev.caecorthus.sparkdlc.entity.RealityAnchorEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Left-clicking a placed reality anchor destroys it (owner 2026-11-27), in adventure mode too. Vanilla's attack path
 * only forwards attacks to {@code LivingEntity}, so a non-living decoration would never take the hit; this intercepts
 * the packet's ATTACK branch for the anchor and discards it within melee reach.
 * 左键已放置的现实稳定锚即可摧毁（所有者 2026-11-27），冒险模式同样有效。原版攻击路径只把攻击转给
 * {@code LivingEntity}，非生物装饰永远收不到这一击；这里在数据包的 ATTACK 分支上单独处理锚，并限制在近战范围内。
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerAnchorMeleeMixin {
    /** Melee reach squared, a little above vanilla's 3 blocks. / 近战距离平方，略高于原版 3 格。 */
    private static final double REACH_SQUARED = 36.0D;

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onPlayerInteractEntity", at = @At("HEAD"), cancellable = true)
    private void sparkdlc$punchRealityAnchor(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        ServerWorld world = player.getServerWorld();
        Entity entity = packet.getEntity(world);
        if (!(entity instanceof RealityAnchorEntity anchor)) {
            return;
        }
        boolean[] attack = {false};
        packet.handle(new PlayerInteractEntityC2SPacket.Handler() {
            @Override
            public void interact(Hand hand) {
            }

            @Override
            public void interactAt(Hand hand, Vec3d pos) {
            }

            @Override
            public void attack() {
                attack[0] = true;
            }
        });
        if (!attack[0] || player.squaredDistanceTo(anchor) > REACH_SQUARED) {
            return;
        }
        anchor.discard();
        ci.cancel();
    }
}
