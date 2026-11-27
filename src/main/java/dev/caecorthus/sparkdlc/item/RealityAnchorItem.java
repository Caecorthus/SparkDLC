package dev.caecorthus.sparkdlc.item;

import dev.caecorthus.sparkdlc.SparkDlcEntities;
import dev.caecorthus.sparkdlc.entity.RealityAnchorEntity;
import dev.doctor4t.wathe.util.AdventureUsable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Reality stabilization anchor item: right-click a block to place the anchor entity where the click landed. Usable in
 * adventure mode ({@link AdventureUsable} lets Wathe's adventure players use it on blocks), which is how rounds run.
 * 现实稳定锚物品：右键方块，在点击位置生成锚实体。冒险模式下也可用（{@link AdventureUsable} 让 Wathe 的冒险模式玩家可以
 * 对方块使用），对局内正是冒险模式。
 */
public final class RealityAnchorItem extends Item implements AdventureUsable {

    public RealityAnchorItem(Settings settings) {
        super(settings);
    }

    public static Settings createSettings() {
        return new Settings().maxCount(16);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (player == null) {
            return ActionResult.PASS;
        }
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        RealityAnchorEntity anchor = SparkDlcEntities.realityAnchor().create(world);
        if (anchor == null) {
            return ActionResult.FAIL;
        }
        Vec3d hit = context.getHitPos();
        BlockPos placed = context.getBlockPos();
        anchor.setPosition(hit.x, Math.max(hit.y, placed.getY() + 0.05D), hit.z);
        anchor.setYaw(player.getHeadYaw());
        world.spawnEntity(anchor);
        // Placing sounds like activating a beacon; the entity itself keeps pulsing and deactivates on destruction.
        // 放下时如同激活信标；实体自身持续播放环境音，并在被摧毁时播放熄灭音。
        world.playSound(null, anchor.getBlockPos(), net.minecraft.sound.SoundEvents.BLOCK_BEACON_ACTIVATE,
                net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 1.0F);

        ItemStack stack = context.getStack();
        if (!player.isCreative()) {
            stack.decrement(1);
        }
        return ActionResult.CONSUME;
    }
}
