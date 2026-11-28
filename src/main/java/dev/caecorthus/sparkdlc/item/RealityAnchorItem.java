package dev.caecorthus.sparkdlc.item;

import dev.caecorthus.sparkdlc.SparkDlcEntities;
import dev.caecorthus.sparkdlc.entity.RealityAnchorEntity;
import dev.doctor4t.wathe.util.AdventureUsable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Reality stabilization anchor item: right-click a block to place the anchor entity where the click landed. Usable in
 * adventure mode ({@link AdventureUsable} lets Wathe's adventure players use it on blocks), which is how rounds run.
 * 现实稳定锚物品：右键方块，在点击位置生成锚实体。冒险模式下也可用（{@link AdventureUsable} 让 Wathe 的冒险模式玩家可以
 * 对方块使用），对局内正是冒险模式。
 */
public final class RealityAnchorItem extends Item implements AdventureUsable {
    private static final int TOOLTIP_LINES = 2;

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
        Vec3d pos = placement(context.getHitPos(), context.getSide(), anchor.getWidth(), anchor.getHeight(),
                context.getBlockPos().getY());
        anchor.setPosition(pos.x, pos.y, pos.z);
        anchor.setYaw(player.getHeadYaw());
        // An anchor placed during a match leaves with that match; one placed outside stays until it is broken.
        // 对局中放下的锚随该局一起消失；对局外放下的锚保留到被打掉为止。
        anchor.bindToMatch(RealityAnchorEntity.currentMatchId());
        if (!world.spawnEntity(anchor)) {
            // Nothing was placed: keep the item. / 没有放出任何东西：保留物品。
            return ActionResult.FAIL;
        }
        // Placing sounds like activating a beacon; the entity itself keeps pulsing and deactivates on destruction.
        // 放下时如同激活信标；实体自身持续播放环境音，并在被摧毁时播放熄灭音。
        world.playSound(null, anchor.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS,
                1.0F, 1.0F);

        if (!player.isCreative()) {
            context.getStack().decrement(1);
        }
        return ActionResult.CONSUME;
    }

    /**
     * Pure rule: the anchor's feet position for a click at {@code hit} on face {@code side}. The box always ends up
     * outside the clicked block, in the space the face looks into: resting on a top face, hanging below a bottom
     * face, flush against a side face at the clicked height (never lower than just above the clicked block's bottom).
     * The entity's block position, where its light goes, is therefore never inside the clicked block.
     * 纯规则：在 {@code side} 面的 {@code hit} 处点击时锚的脚底位置。包围盒始终落在被点击方块之外、该面所朝向的空间里：顶面则
     * 立在其上，底面则悬在其下，侧面则贴着该面并位于点击高度（不低于被点击方块底部略上方）。实体所在的方块位置（即放光源的
     * 位置）因此绝不会落在被点击的方块内部。
     */
    static Vec3d placement(Vec3d hit, Direction side, float width, float height, int clickedBlockY) {
        return switch (side) {
            case UP -> hit;
            case DOWN -> new Vec3d(hit.x, hit.y - height, hit.z);
            default -> new Vec3d(
                    hit.x + side.getOffsetX() * width / 2.0D,
                    Math.max(hit.y, clickedBlockY + 0.05D),
                    hit.z + side.getOffsetZ() * width / 2.0D);
        };
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        for (int line = 1; line <= TOOLTIP_LINES; line++) {
            tooltip.add(Text.translatable("item.sparkdlc.reality_anchor.tooltip.line" + line)
                    .formatted(Formatting.GRAY));
        }
    }
}
