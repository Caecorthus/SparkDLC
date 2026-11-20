package dev.caecorthus.sparkdlc.client.hud.charge;

import dev.caecorthus.sparkdlc.client.hud.charge.ChargeMeter.Window;
import dev.caecorthus.sparkdlc.item.m67.M67Rules;
import dev.caecorthus.sparkdlc.item.ninja.NinjaShurikenItem;
import dev.caecorthus.sparkdlc.role.civilian.fisher.FisherRules;
import dev.caecorthus.sparkdlc.role.civilian.knight.CrossSwordItem;
import dev.caecorthus.sparkdlc.role.civilian.vendetta.VendettaKnifeRules;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * The held weapons that have to be wound up, and how long each one takes. The indicator works from this list and from
 * nothing else: "is using an item" is also true while aiming a rifle, raising a shield or drinking, none of which is a
 * charge.
 * 需要蓄力的手持武器，以及各自要蓄多久。指示只依据这张清单：「正在使用物品」在举枪瞄准、举盾、喝酒时同样为真，而那些都不是
 * 蓄力。
 *
 * <p>{@code getMaxUseTime} is no help either — it is how long the item may be held (100 ticks for the knife, 7200 for
 * the shuriken, 72000 for the axe), not when a release starts to count. So every item is listed with the number its
 * own release check uses: where the owning feature has a constant, that constant itself, read directly. A change in
 * that feature then reaches the indicator without touching this file.
 * {@code getMaxUseTime} 同样帮不上忙——它只是物品最多能按住多久（刀 100 刻、手里剑 7200、飞斧 72000），而不是松手从何时起
 * 有效。因此每件道具都按其自身松手判定所用的数值列出：所属功能有常量时直接读取并使用该常量。那个功能改了规则，
 * 指示会自动跟上，无需改动本文件。</p>
 */
public final class ChargeItems {
    /** Wathe's {@code KnifeItem.onStoppedUsing} needs more than 10 elapsed ticks, so tick 11 is the first that stabs. Wathe 的 {@code KnifeItem.onStoppedUsing} 要求已用刻数超过 10，因此第 11 刻是第一个能出刀的。 */
    private static final int KNIFE_WINDUP_TICKS = 11;
    /**
     * NoellesRoles' {@code ThrowingAxeItem} uses the bow curve over 20 ticks and refuses a throw under 0.25 power,
     * which is 7 ticks; the numbers are literals there, so there is nothing to read.
     * NoellesRoles 的 {@code ThrowingAxeItem} 在 20 刻上套用弓的曲线，并拒绝力度低于 0.25 的投掷，即 7 刻；这些数值在那边是
     * 字面量，没有常量可读。
     */
    private static final int AXE_MIN_TICKS = 7;
    private static final int AXE_FULL_TICKS = 20;

    private static final int VENDETTA_KNIFE_TICKS = VendettaKnifeRules.MINIMUM_HOLD_TICKS;
    private static final int SWORDFISH_TICKS = FisherRules.SWORDFISH_MIN_HOLD_TICKS;
    private static final int CROSS_SWORD_TICKS = CrossSwordItem.MIN_HOLD_TICKS;
    private static final int SHURIKEN_TICKS = NinjaShurikenItem.MIN_CHARGE_TICKS;
    private static final int M67_TICKS = M67Rules.CHARGE_TICKS;

    /** Looked up once per item instance; the map also remembers the items that are not on the list. 每个物品实例只查一次；不在清单上的物品同样被记住。 */
    private static final Map<Item, Optional<Kind>> KINDS = new IdentityHashMap<>();

    private ChargeItems() {
    }

    /** The charged weapon this item is, or {@code null} for everything else. 该物品对应的蓄力武器；其他物品返回 {@code null}。 */
    public static Kind kindOf(Item item) {
        return KINDS.computeIfAbsent(item, key -> Optional.ofNullable(Kind.byId(Registries.ITEM.getId(key))))
                .orElse(null);
    }

    /** How long {@code player} has to hold this weapon, traits included. {@code player} 需要按住这件武器多久，已计入词条。 */
    public static Window window(Kind kind, PlayerEntity player, ItemStack stack) {
        return switch (kind) {
            case KNIFE -> Window.of(TraitCharge.knifeWindup(player, stack, KNIFE_WINDUP_TICKS));
            case VENDETTA_KNIFE -> Window.of(VENDETTA_KNIFE_TICKS);
            case SWORDFISH -> Window.of(SWORDFISH_TICKS);
            case CROSS_SWORD -> Window.of(CROSS_SWORD_TICKS);
            case SHURIKEN -> Window.of(serverTimed(TraitCharge.throwCharge(player, SHURIKEN_TICKS)));
            case M67 -> Window.of(serverTimed(TraitCharge.throwCharge(player, M67_TICKS)));
            case THROWING_AXE -> new Window(
                    serverTimed(TraitCharge.throwCharge(player, AXE_MIN_TICKS)),
                    serverTimed(TraitCharge.throwCharge(player, AXE_FULL_TICKS)));
        };
    }

    /**
     * One tick of slack for the items whose hold is counted on the server alone. The client's count can be a tick ahead
     * of the server's, and a release the server finds short is not free: the M67 answers with a 40-tick cooldown.
     * 为仅由服务端计时的道具留一刻余量。客户端的计数可能比服务端快一刻，而被服务端判定为不足的松手并非没有代价：M67 会因此
     * 进入 40 刻冷却。
     */
    private static int serverTimed(int ticks) {
        return ticks + 1;
    }

    /** The weapons the indicator covers. 指示所覆盖的武器。 */
    public enum Kind {
        KNIFE("wathe", "knife"),
        /** Extends Wathe's knife but replaces the release check with its own. 继承自 Wathe 的刀，但用自己的松手判定替换了原判定。 */
        VENDETTA_KNIFE("sparkdlc", "vendetta_knife"),
        SWORDFISH("sparkdlc", "swordfish"),
        CROSS_SWORD("sparkdlc", "cross_sword"),
        SHURIKEN("sparkdlc", "ninja_shuriken"),
        M67("sparkdlc", "m67"),
        THROWING_AXE("noellesroles", "throwing_axe");

        private final Identifier id;

        Kind(String namespace, String path) {
            this.id = Identifier.of(namespace, path);
        }

        public Identifier id() {
            return id;
        }

        static Kind byId(Identifier id) {
            for (Kind kind : values()) {
                if (kind.id.equals(id)) {
                    return kind;
                }
            }
            return null;
        }
    }
}
