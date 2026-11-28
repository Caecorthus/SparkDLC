package dev.caecorthus.sparkdlc.entity;

import dev.caecorthus.sparkdlc.role.civilian.fruitvendor.BananaPeelService;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.List;

/**
 * A peel lying on the floor, as an entity rather than a block (owner 2026-11-28: 「类似便签那种实体触发的」). It has no
 * collision of its own; every tick it looks for a player whose box overlaps it, and the first one slips.
 * 躺在地上的果皮，做成实体而不是方块（所有者 2026-11-28：「类似便签那种实体触发的」）。它自身没有碰撞；每刻寻找
 * 碰撞箱与它重叠的玩家，第一个踩中的人滑倒。
 */
public class BananaPeelEntity extends Entity {

    /** Cleaned up after five minutes so old peels do not pile up forever. / 五分钟后清理，避免旧果皮无限堆积。 */
    public static final int LIFETIME_TICKS = 5 * 60 * 20;

    public BananaPeelEntity(EntityType<? extends BananaPeelEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient()) {
            return;
        }
        if (this.age > LIFETIME_TICKS) {
            this.discard();
            return;
        }
        if (!(this.getWorld() instanceof ServerWorld world)) {
            return;
        }
        List<ServerPlayerEntity> touching = world.getEntitiesByClass(ServerPlayerEntity.class,
                this.getBoundingBox().expand(0.05D), player -> player.isAlive() && !player.isSpectator());
        for (ServerPlayerEntity player : touching) {
            if (BananaPeelService.slip(player, world.getRandom())) {
                this.discard();
                return;
            }
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }

    /** Nothing to collide with: the peel is a decal, the slip is decided in {@link #tick}. / 没有碰撞：果皮是贴地图层，滑倒判定在 tick 里。 */
    @Override
    public boolean isCollidable() {
        return false;
    }
}
