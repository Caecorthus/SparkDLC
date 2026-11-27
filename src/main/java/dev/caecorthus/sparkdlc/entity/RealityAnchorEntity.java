package dev.caecorthus.sparkdlc.entity;

import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Placed reality stabilization anchor: a static, non-colliding decoration that only disappears when a player hits it
 * or a shot/grenade reaches it (owner 2026-11-26, destruction sources added 2026-11-27). No gravity. It borrows the
 * beacon's sound set (activation on placement, ambient pulse every 80 ticks, deactivation on destruction), burns like
 * a torch through a level-15 light block at its position, gives nearby players (5 blocks) night vision, and denies the
 * Alt instinct look within 15 blocks (client side).
 * 放置后的现实稳定锚：静止、不参与碰撞的装饰实体，只有被玩家左键或枪械/手雷命中才会消失（所有者 2026-11-26，
 * 2026-11-27 增加摧毁途径）。不受重力。沿用信标音效（放下激活、每 80 刻环境音、被摧毁熄灭），通过所在位置的 15 级
 * 光源方块像火把一样照亮，给 5 格内的玩家夜视，并在 15 格内禁止按 Alt 使用本能透视（客户端判断）。
 */
public final class RealityAnchorEntity extends Entity {
    /** Beacon ambient interval, the same 80 ticks an active beacon uses. / 信标环境音间隔，与激活信标相同的 80 刻。 */
    private static final int AMBIENT_INTERVAL_TICKS = 80;
    /** Light refresh interval; night vision is refreshed by {@link RealityAnchorAuraService}. / 光源刷新间隔；夜视由服务刷新。 */
    private static final int AURA_INTERVAL_TICKS = 20;

    public RealityAnchorEntity(EntityType<? extends RealityAnchorEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            return;
        }
        if (age > 0 && age % AMBIENT_INTERVAL_TICKS == 0) {
            getWorld().playSound(null, getBlockPos(), SoundEvents.BLOCK_BEACON_AMBIENT, SoundCategory.BLOCKS,
                    1.0F, 1.0F);
        }
        if (age % AURA_INTERVAL_TICKS == 0) {
            ensureLight();
            applyNightVision();
        }
    }

    /** Keeps a level-15 light block under the anchor, without ever overwriting a real block. / 维持锚位置的 15 级光源。 */
    private void ensureLight() {
        BlockPos pos = getBlockPos();
        if (getWorld().getBlockState(pos).isAir()) {
            getWorld().setBlockState(pos, Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, 15));
        }
    }

    /** Night vision for players within {@link RealityAnchorAuraService#NIGHT_VISION_RADIUS}. / 给半径内的玩家夜视。 */
    private void applyNightVision() {
        if (!(getWorld() instanceof ServerWorld world)) {
            return;
        }
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.squaredDistanceTo(this)
                    <= RealityAnchorAuraService.NIGHT_VISION_RADIUS * RealityAnchorAuraService.NIGHT_VISION_RADIUS) {
                RealityAnchorAuraService.grant(player);
            }
        }
    }

    /** Any player hit clears the anchor. / 任何玩家的一击都会清掉锚。 */
    @Override
    public boolean damage(DamageSource source, float amount) {
        if (!getWorld().isClient) {
            discard();
        }
        return true;
    }

    /**
     * Beacon deactivation (and the light block) when the anchor is destroyed. Chunk unloading and other removals stay
     * silent and leave the light block alone, so the sound only marks an actual destruction.
     * 锚被摧毁时播放信标熄灭音并移除光源方块。区块卸载等其他移除保持安静，也不动光源方块，因此该音效只代表真正的摧毁。
     */
    @Override
    public void remove(RemovalReason reason) {
        if (!getWorld().isClient && reason == RemovalReason.DISCARDED) {
            getWorld().playSound(null, getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS,
                    1.0F, 1.0F);
            BlockPos pos = getBlockPos();
            if (getWorld().getBlockState(pos).isOf(Blocks.LIGHT)) {
                getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
        super.remove(reason);
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }
}
