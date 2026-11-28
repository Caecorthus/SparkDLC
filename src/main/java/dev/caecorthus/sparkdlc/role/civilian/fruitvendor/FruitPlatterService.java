package dev.caecorthus.sparkdlc.role.civilian.fruitvendor;

import dev.caecorthus.sparkdlc.item.fruit.FruitItems;
import dev.doctor4t.wathe.block.FoodPlatterBlock;
import dev.doctor4t.wathe.block_entity.BeveragePlateBlockEntity;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Puts the vendor's fruit onto Wathe's food platter and drink tray. Implemented as a Fabric use-block callback rather
 * than a mixin on {@code FoodPlatterBlock#onUse}: the callback fires before the block handles the click, so it works
 * the same on the platter and on the tray (which only extends it), and it does not depend on the block still calling
 * into {@code onUse} in a round.
 * 把水果贩的水果放上 Wathe 的餐盘与饮料托盘。用 Fabric 的方块使用回调而不是在 {@code FoodPlatterBlock#onUse} 上做
 * mixin：回调在方块处理点击之前触发，因此餐盘与（仅继承它的）托盘行为一致，也不依赖局内方块仍会调用 {@code onUse}。
 */
public final class FruitPlatterService {

    private static final int PLATTER_CAPACITY = 6;
    private static boolean registered;

    private FruitPlatterService() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        UseBlockCallback.EVENT.register(FruitPlatterService::onUseBlock);
    }

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand,
                                          net.minecraft.util.hit.BlockHitResult hitResult) {
        if (world.isClient() || hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }
        ItemStack held = player.getStackInHand(hand);
        if (!isFruit(held)) {
            return ActionResult.PASS;
        }
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof FoodPlatterBlock)) {
            return ActionResult.PASS;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof BeveragePlateBlockEntity platter)) {
            return ActionResult.PASS;
        }
        if (platter.getStoredItems().size() >= PLATTER_CAPACITY) {
            return ActionResult.PASS;
        }
        platter.addItem(held.copy());
        held.decrement(1);
        // The plate keeps its contents in memory only: markDirty so a save does not drop them, updateListeners so
        // every client redraws the plate with the fruit on it.
        // 餐盘内容只在内存里：markDirty 防止存盘丢失，updateListeners 让所有客户端把水果画出来。
        platter.markDirty();
        world.updateListeners(pos, state, state, 3);
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.playSoundToPlayer(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        return ActionResult.SUCCESS;
    }

    private static boolean isFruit(ItemStack stack) {
        return stack.isOf(FruitItems.banana()) || stack.isOf(FruitItems.orange())
                || stack.isOf(FruitItems.enlightenmentBerry());
    }
}
