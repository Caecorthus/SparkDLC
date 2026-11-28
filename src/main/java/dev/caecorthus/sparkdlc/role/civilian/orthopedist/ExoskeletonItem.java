package dev.caecorthus.sparkdlc.role.civilian.orthopedist;

import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * The Exoskeleton: one item fits one layer onto a player. Right-click another player to fit it on them; sneak and
 * right-click to fit it on yourself. The self-fit needs the sneak on purpose: a plain right-click that misses its
 * target (out of reach, a wall, a corpse) also ends in {@link #use}, and must not spend the item on the user. Any
 * holder may use it; the server decides in {@link ExoskeletonService#fit}.
 * 外骨骼：一件道具给一名玩家装上一层。右键其他玩家为其装配；潜行并右键则给自己装配。给自己装配特意要求潜行：没点中目标的普通
 * 右键（够不着、点到墙或尸体）同样会落到 {@link #use}，不能因此把道具消耗在使用者自己身上。任何持有者都能使用；由服务端在
 * {@link ExoskeletonService#fit} 中判定。
 */
public final class ExoskeletonItem extends Item {
    public ExoskeletonItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!user.isSneaking()) {
            // A click that hit no player: nothing is fitted or spent, only the reminder. / 没点中玩家的右键：不装配、不消耗，只给提示。
            if (!world.isClient) {
                user.sendMessage(Text.translatable("message.sparkdlc.exoskeleton.self_hint"), true);
            }
            return TypedActionResult.pass(stack);
        }
        if (world.isClient) {
            return TypedActionResult.success(stack);
        }
        if (!(user instanceof ServerPlayerEntity serverUser)
                || !ExoskeletonService.fit(serverUser, serverUser, stack)) {
            return TypedActionResult.fail(stack);
        }
        return TypedActionResult.consume(stack);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof PlayerEntity target)) {
            return ActionResult.PASS;
        }
        if (user.getWorld().isClient) {
            return ActionResult.SUCCESS;
        }
        if (!(user instanceof ServerPlayerEntity serverUser)
                || !(target instanceof ServerPlayerEntity serverTarget)) {
            return ActionResult.PASS;
        }
        return ExoskeletonService.fit(serverUser, serverTarget, stack) ? ActionResult.SUCCESS : ActionResult.FAIL;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(getTranslationKey() + ".tooltip.line1")
                .styled(style -> style.withColor(0x808080).withItalic(false)));
        tooltip.add(Text.translatable(getTranslationKey() + ".tooltip.line2")
                .styled(style -> style.withColor(0x808080).withItalic(false)));
    }
}
