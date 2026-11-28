package dev.caecorthus.sparkdlc.item.fruit;

import dev.caecorthus.sparkdlc.SparkDlcEntities;
import dev.caecorthus.sparkdlc.entity.BananaPeelEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

/**
 * Drops a peel onto the ground as an entity. Uses {@code use} (not {@code useOnBlock}) with our own raycast, the way
 * the hunter's trap does: in a round you often look straight down at the floor, and a missing block hit would make
 * {@code useOnBlock} never fire. 把果皮作为实体放到地上。用 {@code use}（不是 {@code useOnBlock}）自己做射线，和猎人
 * 捕兽夹同一做法：局内常常低头看地板，方块命中失败会导致 useOnBlock 根本不触发。
 */
public class BananaPeelItem extends Item {

    private static final double MAX_SURFACE_SEARCH_DEPTH = 2.0D;

    public BananaPeelItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        BlockHitResult hit = Item.raycast(world, user, RaycastContext.FluidHandling.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return TypedActionResult.pass(stack);
        }
        Placement placement = findPlacement(world, hit.getPos());
        if (placement == null) {
            return TypedActionResult.pass(stack);
        }
        if (world.isClient) {
            return TypedActionResult.success(stack, true);
        }
        if (!(world instanceof ServerWorld serverWorld)) {
            return TypedActionResult.pass(stack);
        }
        BananaPeelEntity peel = SparkDlcEntities.bananaPeel().create(serverWorld);
        if (peel == null) {
            return TypedActionResult.pass(stack);
        }
        peel.refreshPositionAndAngles(placement.spawn().x, placement.spawn().y, placement.spawn().z, 0.0F, 0.0F);
        serverWorld.spawnEntity(peel);
        serverWorld.playSound(null, placement.support(), SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.PLAYERS,
                0.8F, 1.2F);
        stack.decrement(1);
        return TypedActionResult.success(stack, false);
    }

    /** Same shape as HunterTrapItem#findPlacement: scan down for the nearest solid top under the cursor. / 与猎人捕兽夹相同的找面逻辑。 */
    private static Placement findPlacement(World world, Vec3d hitPos) {
        int minBlockY = MathHelper.floor(hitPos.y - MAX_SURFACE_SEARCH_DEPTH);
        int maxBlockY = MathHelper.floor(hitPos.y);
        double bestTopY = Double.NEGATIVE_INFINITY;
        BlockPos bestSupport = null;
        for (int y = maxBlockY; y >= minBlockY; y--) {
            BlockPos support = BlockPos.ofFloored(hitPos.x, y, hitPos.z);
            VoxelShape shape = world.getBlockState(support).getCollisionShape(world, support);
            if (shape.isEmpty()) {
                continue;
            }
            double topY = support.getY() + shape.getMax(Direction.Axis.Y);
            if (topY <= bestTopY || hitPos.y < topY - 1.0E-4D) {
                continue;
            }
            Vec3d spawn = new Vec3d(support.getX() + 0.5D, topY + 0.01D, support.getZ() + 0.5D);
            Box box = SparkDlcEntities.bananaPeel().getDimensions().getBoxAt(spawn.x, spawn.y, spawn.z);
            if (!world.isSpaceEmpty(null, box)) {
                continue;
            }
            bestTopY = topY;
            bestSupport = support.toImmutable();
        }
        return bestSupport == null ? null
                : new Placement(bestSupport, new Vec3d(bestSupport.getX() + 0.5D, bestTopY + 0.01D,
                        bestSupport.getZ() + 0.5D));
    }

    private record Placement(BlockPos support, Vec3d spawn) {
    }
}
