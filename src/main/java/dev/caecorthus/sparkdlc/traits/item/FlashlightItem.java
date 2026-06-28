package dev.caecorthus.sparkdlc.traits.item;

import dev.caecorthus.sparkdlc.traits.impl.AttendantFlashlightService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FlashlightItem extends Item {
    public FlashlightItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!(user instanceof ServerPlayerEntity serverPlayer)) {
            return TypedActionResult.success(stack, world.isClient());
        }
        if (!AttendantFlashlightService.toggleFlashlight(serverPlayer)) {
            return TypedActionResult.pass(stack);
        }
        boolean enabled = dev.caecorthus.sparkdlc.traits.component.RoleEnhancementPlayerComponent.KEY.get(serverPlayer).isFlashlightOn();
        serverPlayer.sendMessage(Text.translatable(enabled
                ? "message.sparktraits.flashlight.on"
                : "message.sparktraits.flashlight.off"), true);
        return TypedActionResult.success(stack, false);
    }
}
