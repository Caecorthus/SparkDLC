package dev.caecorthus.sparkdlc.role.civilian.fruitvendor;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.item.fruit.FruitItems;
import dev.doctor4t.wathe.api.event.BuildShopEntries;
import dev.doctor4t.wathe.api.event.TaskComplete;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.doctor4t.wathe.api.event.KillPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import net.minecraft.util.math.random.Random;

/**
 * Shop, coins and kill reward for the Fruit Vendor (owner 2026-11-28). Passengers are innocent, so the shop is only
 * ever offered to her; coins come from tasks (+50) and kills (+100), never naturally.
 * 水果贩的商店、金币与击杀奖励（所有者 2026-11-28）。乘客属于好人，商店只对她开放；金币只来自任务（+50）与击杀（+100），
 * 不会自然增长。
 */
public final class FruitVendorService {

    private static final List<Item> CRATE_FRUITS = List.of(Items.APPLE, Items.SWEET_BERRIES,
            Items.MELON_SLICE, Items.GLOW_BERRIES);

    private static boolean registered;

    private FruitVendorService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        // Both sides build the shop (the client renders it, the server charges for it), so the role test must not
        // require a ServerPlayerEntity — otherwise the client sees an empty shelf.
        // 商店是双端构建的（客户端渲染、服务端扣钱），所以职业判定不能要求 ServerPlayerEntity，否则客户端看到空货架。
        BuildShopEntries.EVENT.register((player, context) -> {
            if (!isFruitVendor(player)) {
                return;
            }
            context.addEntry(SparkTraitsApi.discountShopEntryForCharisma(player, new ShopEntry.Builder(
                    FruitVendorRules.RANDOM_FRUIT_ENTRY, new ItemStack(randomCrateFruit(player.getRandom())),
                    FruitVendorRules.RANDOM_FRUIT_PRICE, ShopEntry.Type.TOOL)
                    .cooldown(FruitVendorRules.RANDOM_FRUIT_COOLDOWN_TICKS).build()));
            context.addEntry(SparkTraitsApi.discountShopEntryForCharisma(player, new ShopEntry.Builder(
                    FruitVendorRules.BANANA_ENTRY, new ItemStack(FruitItems.banana()),
                    FruitVendorRules.BANANA_PRICE, ShopEntry.Type.TOOL).build()));
            context.addEntry(SparkTraitsApi.discountShopEntryForCharisma(player, new ShopEntry.Builder(
                    FruitVendorRules.ORANGE_ENTRY, new ItemStack(FruitItems.orange()),
                    FruitVendorRules.ORANGE_PRICE, ShopEntry.Type.TOOL).build()));
            context.addEntry(SparkTraitsApi.discountShopEntryForCharisma(player, new ShopEntry.Builder(
                    FruitVendorRules.BERRY_ENTRY, new ItemStack(FruitItems.enlightenmentBerry()),
                    FruitVendorRules.BERRY_PRICE, ShopEntry.Type.TOOL).build()));
        });
        TaskComplete.EVENT.register((player, task) -> {
            if (player instanceof ServerPlayerEntity serverPlayer && isFruitVendor(serverPlayer)) {
                PlayerShopComponent shop = PlayerShopComponent.KEY.get(serverPlayer);
                shop.setBalance(shop.getBalance() + FruitVendorRules.TASK_MONEY_REWARD);
            }
        });
        KillPlayer.AFTER.register((victim, killer, reason) -> {
            if (!(killer instanceof ServerPlayerEntity serverKiller) || killer == victim) {
                return;
            }
            if (isFruitVendor(serverKiller)) {
                PlayerShopComponent shop = PlayerShopComponent.KEY.get(serverKiller);
                shop.setBalance(shop.getBalance() + FruitVendorRules.KILL_MONEY_REWARD);
            }
        });
    }

    private static Item randomCrateFruit(Random random) {
        return CRATE_FRUITS.get(random.nextInt(CRATE_FRUITS.size()));
    }

    private static boolean isFruitVendor(PlayerEntity player) {
        return FruitVendorRules.isFruitVendor(GameWorldComponent.KEY.get(player.getWorld()).getRole(player));
    }
}
