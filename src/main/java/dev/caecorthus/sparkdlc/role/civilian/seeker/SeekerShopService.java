package dev.caecorthus.sparkdlc.role.civilian.seeker;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerCooldowns;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.trait.api.TraitShopEntries;
import dev.caecorthus.sparkdlc.util.ShopStacks;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.event.BuildShopEntries;
import dev.doctor4t.wathe.api.event.ShopPurchase;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Replaces the Seeker's shop on both sides with the camera (150, re-buyable any time with no ownership limit) and the
 * car service (50, no stock limit, owner spec 2026-11-28). The camera keeps Wathe's default buy handler. The car
 * service sells no item: its {@code onBuy} shortens the running car cooldown by 60 s on the server and returns false
 * (so Wathe charges nothing) when the car is not cooling down; a deny-only {@code ShopPurchase.BEFORE} explains that
 * refusal and never allows. Both entries pass through the Charisma trait's discount (applied only to a player who owns
 * that trait). The tablet is issued by {@code TabletShopService} at round start, never sold here.
 * 在两端把搜寻者商店替换为摄像头（150，随时可再买、不限拥有数量）与小车整备（50，不限库存，所有者规格 2026-11-28）。
 * 摄像头保留 Wathe 默认购买处理。小车整备不出售物品：其 {@code onBuy} 在服务端把正在进行的小车冷却缩短 60 秒，小车不在冷却中时
 * 返回 false（Wathe 因此不扣钱）；只拒绝的 {@code ShopPurchase.BEFORE} 负责说明该拒绝原因、从不允许。两个条目都经过魅力词条折扣
 * （仅对拥有该词条的玩家生效）。平板由 {@code TabletShopService} 开局发放，此处不出售。
 */
public final class SeekerShopService {
    private static boolean registered;

    private SeekerShopService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        BuildShopEntries.EVENT.register(SeekerShopService::buildEntries);
        ShopPurchase.BEFORE.register(SeekerShopService::beforePurchase);
    }

    private static void buildEntries(PlayerEntity player, BuildShopEntries.ShopContext context) {
        Role role = GameWorldComponent.KEY.get(player.getWorld()).getRole(player);
        if (!SeekerShopRules.rebuildsShopFor(role)) {
            return;
        }
        // Trait shop entries (e.g. the Impostor revolver) may already be present; keep them across the rebuild.
        // 词条商店条目（如内鬼左轮）可能已先加入；重建时保留它们。
        List<ShopEntry> preservedTraitEntries = TraitShopEntries.capture(context);
        context.clearEntries();
        for (SeekerShopRules.EntrySpec spec : SeekerShopRules.entries()) {
            ShopEntry entry = switch (spec.kind()) {
                case CAMERA -> cameraEntry(spec);
                case CAR_SERVICE -> carServiceEntry(spec);
            };
            context.addEntry(SparkTraitsApi.discountShopEntryForCharisma(player, entry));
        }
        TraitShopEntries.restore(context, preservedTraitEntries);
    }

    private static ShopEntry cameraEntry(SeekerShopRules.EntrySpec spec) {
        // No stock and no ownership limit: any number of cameras, only money limits it.
        // 不设库存也不限拥有数量：摄像头数量不限，只受金钱限制。
        return new ShopEntry.Builder(spec.id(), SparkDlcItems.seekerCamera().getDefaultStack(), spec.price(),
                ShopEntry.Type.TOOL).build();
    }

    private static ShopEntry carServiceEntry(SeekerShopRules.EntrySpec spec) {
        // The car item is only the icon (a display-only stack with its own name and description); nothing is handed
        // out, so the "one car item per Seeker" rule is untouched.
        // 小车物品只作图标（仅用于展示的物品堆，带自己的名称与描述）；不发放任何物品，因此“每名搜寻者只有一个小车物品”的规则不受影响。
        return new ShopEntry.Builder(spec.id(), carServiceDisplayStack(), spec.price(), ShopEntry.Type.TOOL)
                .onBuy(buyer -> buyer instanceof ServerPlayerEntity serverBuyer && buyCarService(serverBuyer))
                .build();
    }

    private static ItemStack carServiceDisplayStack() {
        ItemStack display = ShopStacks.display(SparkDlcItems.seekerCar().getDefaultStack(),
                SeekerShopRules.CAR_SERVICE_NAME_KEY);
        // Without this the shop tooltip would also list the car item's own "how to deploy" lines above the description.
        // 否则商店提示会在描述上方再列出小车物品自身的“如何部署”说明行。
        display.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        return display;
    }

    /**
     * Deny-only: the car-service entry and the exact role first (everything else returns null untouched), then the
     * readable refusal; never allow. {@code onBuy} stays the authoritative backstop.
     * 只拒绝：先判小车整备条目与精确职业（其余一律原样返回 null），再给出可读的拒绝原因；从不允许。{@code onBuy} 仍是权威的
     * 最后防线。
     */
    private static @Nullable ShopPurchase.PurchaseResult beforePurchase(ServerPlayerEntity player, ShopEntry entry,
                                                                        int index) {
        if (!SeekerRules.CAR_SERVICE_ENTRY_ID.equals(entry.id()) || !isSeeker(player)) {
            return null;
        }
        String denial = SeekerShopRules.carServiceDenial(SeekerCooldowns.remainingTicks(player));
        return denial == null ? null : ShopPurchase.PurchaseResult.deny(denial);
    }

    /**
     * Server purchase on the server thread: true charges the price, false charges nothing. The cooldown is shortened
     * here, before Wathe takes the coins; Wathe has already checked the balance, so a true result is always paid for.
     * 服务端线程上的购买：返回 true 扣费，false 不扣费。冷却在这里缩短，先于 Wathe 扣除金币；Wathe 此前已检查余额，因此返回 true
     * 的购买一定会被扣费。
     */
    static boolean buyCarService(ServerPlayerEntity buyer) {
        return isSeeker(buyer) && SeekerCooldowns.shortenExact(buyer, SeekerRules.CAR_SERVICE_TICKS);
    }

    private static boolean isSeeker(PlayerEntity player) {
        return SeekerRules.isSeeker(GameWorldComponent.KEY.get(player.getWorld()).getRole(player));
    }
}
