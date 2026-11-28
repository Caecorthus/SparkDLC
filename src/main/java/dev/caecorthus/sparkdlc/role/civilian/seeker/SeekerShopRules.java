package dev.caecorthus.sparkdlc.role.civilian.seeker;

import dev.doctor4t.wathe.api.Role;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Stable contract: the pure Seeker shop specification. Wathe builds the list on both sides and buys by index with no
 * version handshake, so the shape is a constant that depends only on the role — never on a runtime API probe. It holds
 * two entries, in this order: the camera (150, no stock limit and re-buyable any time; the owner may own any number,
 * only money limits it) and the car service (50, no stock limit, owner spec 2026-11-28: a consumable that shortens the
 * running car cooldown by 60 s and sells no item; whether it can be bought right now is the server's decision at
 * purchase time, never part of the list shape). The tablet is not sold: {@code TabletShopService} issues it free at
 * round start to every police-network role (the Seeker included), so this shop never lists it.
 * 稳定契约：纯搜寻者商店规格。Wathe 在两端各自构建列表并按下标购买，没有版本握手，因此列表形状是只取决于职业的常量，
 * 绝不取决于运行时 API 探测。按顺序有两个条目：摄像头（150，不限库存，随时可再买；拥有者可拥有任意数量，只受金钱限制）
 * 与小车整备（50，不限库存，所有者规格 2026-11-28：消耗型条目，把正在进行的小车冷却缩短 60 秒，不出售任何物品；此刻能否购买
 * 由服务端在购买时决定，绝不影响列表形状）。
 * 不出售平板：{@code TabletShopService} 在开局免费发放给所有警察网络职业（含搜寻者），因此本商店从不列出平板。
 */
public final class SeekerShopRules {
    /** Wathe's "no stock limit" (the builder's stock is left unset). / Wathe 的“不限库存”（不调用 stock）。 */
    public static final int NO_STOCK_LIMIT = -1;
    /** Shop display name of the car service; its description is this key + {@code .description}. / 小车整备的商店显示名；描述为该键加 {@code .description}。 */
    public static final String CAR_SERVICE_NAME_KEY = "shop.sparkdlc." + SeekerRules.CAR_SERVICE_ENTRY_ID;
    /** Refusal shown when the car is not cooling down (nothing is charged). / 小车不在冷却中时的拒绝提示（不扣钱）。 */
    public static final String CAR_SERVICE_NOT_COOLING_KEY = "message.sparkdlc.seeker.car.service_not_cooling";

    public static final EntrySpec CAMERA = new EntrySpec(EntryKind.CAMERA, SeekerRules.CAMERA_ENTRY_ID,
            SeekerRules.CAMERA_PRICE, NO_STOCK_LIMIT);
    public static final EntrySpec CAR_SERVICE = new EntrySpec(EntryKind.CAR_SERVICE,
            SeekerRules.CAR_SERVICE_ENTRY_ID, SeekerRules.CAR_SERVICE_PRICE, NO_STOCK_LIMIT);

    /** Appended after the camera, so the camera keeps its index. / 追加在摄像头之后，摄像头的下标不变。 */
    private static final List<EntrySpec> ENTRIES = List.of(CAMERA, CAR_SERVICE);

    private SeekerShopRules() {
    }

    /**
     * Role alone decides, never running state: Wathe builds the list on both sides, including while the round is still
     * STARTING.
     * 只按职业判定，绝不依赖运行状态：Wathe 在两端构建列表，包括对局仍处于 STARTING 时。
     */
    public static boolean rebuildsShopFor(@Nullable Role role) {
        return SeekerRules.isSeeker(role);
    }

    /** Entries in shop order. / 按商店顺序排列的条目。 */
    public static List<EntrySpec> entries() {
        return ENTRIES;
    }

    /** What an entry sells: an item, or the car service (an effect, no item). / 条目出售的内容：物品，或小车整备（效果，无物品）。 */
    public enum EntryKind {
        CAMERA,
        CAR_SERVICE
    }

    /**
     * Pure purchase rule of the car service: only while the car item cooldown is running. Returns null when it may be
     * bought, otherwise the translation key of the refusal.
     * 小车整备的纯购买规则：只有小车物品冷却正在进行时才可购买。可购买时返回 null，否则返回拒绝提示的翻译键。
     */
    @Nullable
    public static String carServiceDenial(int carCooldownTicks) {
        return carCooldownTicks > 0 ? null : CAR_SERVICE_NOT_COOLING_KEY;
    }

    /**
     * One entry's id, price and stock ({@link #NO_STOCK_LIMIT} = unlimited).
     * 单个条目的 id、价格与库存（{@link #NO_STOCK_LIMIT} 表示不限）。
     */
    public record EntrySpec(EntryKind kind, String id, int price, int stock) {
        public boolean hasStockLimit() {
            return stock > 0;
        }
    }
}
