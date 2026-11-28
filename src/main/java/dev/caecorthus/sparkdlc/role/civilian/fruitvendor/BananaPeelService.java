package dev.caecorthus.sparkdlc.role.civilian.fruitvendor;

import dev.doctor4t.wathe.api.event.ResetPlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.math.random.Random;
import java.util.UUID;

/**
 * The slip itself: one random direction, a solid shove, Slowness 255 for the whole duration so the player cannot steer,
 * and the shove decays a little every tick. Any damage taken, death or round reset ends it early.
 * 滑倒本身：一个随机方向、一记推力、整段期间施加缓慢 255 让玩家无法转向，推力每刻轻微衰减。受伤、死亡或回合重置都会提前结束。
 */
public final class BananaPeelService {

    private record Slip(Vec3d velocity, long untilTick) {
    }

    private static final Map<UUID, Slip> SLIPS = new HashMap<>();
    private static boolean registered;

    private BananaPeelService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = server.getOverworld().getTime();
            if (SLIPS.isEmpty()) {
                return;
            }
            for (ServerWorld world : server.getWorlds()) {
                for (ServerPlayerEntity player : world.getPlayers()) {
                    tickPlayer(player, now);
                }
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SLIPS.clear());
        ResetPlayer.EVENT.register(player -> SLIPS.remove(player.getUuid()));
        // Peels are world entities, so a new round has to sweep the old ones or they accumulate across matches.
        // 果皮是世界实体，新一局必须清掉旧的，否则会跨局堆积。
        dev.doctor4t.wathe.api.event.GameEvents.ON_FINISH_INITIALIZE.register((world, game) -> {
            if (world instanceof ServerWorld serverWorld) {
                for (net.minecraft.entity.Entity peel
                        : serverWorld.getEntitiesByType(dev.caecorthus.sparkdlc.SparkDlcEntities.bananaPeel(),
                        entity -> true)) {
                    peel.discard();
                }
            }
        });
    }

    /** Steps on a peel: returns false when this player is already slipping. / 踩到果皮：该玩家已在滑倒时返回 false。 */
    public static boolean slip(ServerPlayerEntity player, Random random) {
        if (SLIPS.containsKey(player.getUuid())) {
            return false;
        }
        long now = player.getServerWorld().getTime();
        int ticks = FruitVendorRules.SLIP_MIN_TICKS
                + random.nextInt((FruitVendorRules.SLIP_MAX_TICKS - FruitVendorRules.SLIP_MIN_TICKS) / 10 + 1) * 10;
        double angle = random.nextDouble() * Math.PI * 2.0D;
        Vec3d velocity = new Vec3d(Math.cos(angle) * FruitVendorRules.SLIP_SPEED, 0.0D,
                Math.sin(angle) * FruitVendorRules.SLIP_SPEED);
        SLIPS.put(player.getUuid(), new Slip(velocity, now + ticks));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, ticks, 255, true, false, false));
        player.setVelocity(velocity);
        player.velocityModified = true;
        player.sendMessage(Text.translatable("message.sparkdlc.fruit_vendor.slipped"), true);
        ServerWorld world = player.getServerWorld();
        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_BREAK, SoundCategory.PLAYERS,
                1.0F, 0.7F);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS,
                0.8F, 1.4F);
        for (int i = 0; i < 12; i++) {
            double a = random.nextDouble() * Math.PI * 2.0D;
            double r = 0.2D + random.nextDouble() * 0.5D;
            world.spawnParticles(ParticleTypes.CRIT,
                    player.getX() + Math.cos(a) * r, player.getY() + 0.1D, player.getZ() + Math.sin(a) * r,
                    1, Math.cos(a) * 0.2D, 0.05D, Math.sin(a) * 0.2D, 0.0D);
        }
        return true;
    }

    private static void tickPlayer(ServerPlayerEntity player, long now) {
        Slip slip = SLIPS.get(player.getUuid());
        if (slip == null) {
            return;
        }
        if (player.getServerWorld().getTime() % 4L == 0L) {
            player.getServerWorld().spawnParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 0.05D, player.getZ(), 2, 0.15D, 0.0D, 0.15D, 0.01D);
        }
        if (now >= slip.untilTick() || !player.isAlive()) {
            SLIPS.remove(player.getUuid());
            return;
        }
        // Keep pushing along the same direction and damp it slowly, so it reads as a slide rather than a launch.
        // 沿同一方向持续推动并缓慢衰减，看起来像打滑而不是被弹射。
        Vec3d next = slip.velocity().multiply(FruitVendorRules.SLIP_DAMPING);
        if (next.horizontalLengthSquared() < 0.0025D) {
            next = Vec3d.ZERO;
        }
        SLIPS.put(player.getUuid(), new Slip(next, slip.untilTick()));
        player.setVelocity(next.x, player.getVelocity().y, next.z);
        player.velocityModified = true;
    }
}
