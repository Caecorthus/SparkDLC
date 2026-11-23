package dev.caecorthus.sparkdlc.role.killer.blackraven;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.net.OpenBlackRavenLedgerS2CPacket;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Secret-free bound stack; pages are built client-side from the owner component. */
public final class BlackRavenLedgerItem extends Item {
    public BlackRavenLedgerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) {
            return TypedActionResult.success(stack);
        }
        if (!(user instanceof ServerPlayerEntity serverUser)
                || !stack.isOf(SparkDlcItems.blackRavenLedger())
                || !GameFunctions.isPlayerPlayingAndAlive(serverUser)
                || !BlackRavenRules.isBlackRaven(GameWorldComponent.KEY.get(world).getRole(serverUser))
                || !ServerPlayNetworking.canSend(serverUser, OpenBlackRavenLedgerS2CPacket.ID)) {
            return TypedActionResult.fail(stack);
        }
        ServerPlayNetworking.send(serverUser, new OpenBlackRavenLedgerS2CPacket());
        return TypedActionResult.success(stack);
    }
}
