package dev.caecorthus.sparkdlc.cooldown.provider;

import dev.caecorthus.sparkdlc.role.killer.witchmaiden.wither.MiniWitherRules;
import dev.caecorthus.sparkdlc.cooldown.api.ItemCooldownNominalProvider;
import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesRoleIds;
import dev.caecorthus.sparkdlc.item.ceremonialsword.CeremonialSwordItem;
import dev.caecorthus.sparkdlc.item.defense.DefenseVialRules;
import dev.caecorthus.sparkdlc.item.firepoker.FirePokerRules;
import dev.caecorthus.sparkdlc.item.m1903a4.M1903A4Rules;
import dev.caecorthus.sparkdlc.item.ninja.NinjaKnifeItem;
import dev.caecorthus.sparkdlc.item.ninja.NinjaShurikenItem;
import dev.caecorthus.sparkdlc.item.rope.RopeRules;
import dev.caecorthus.sparkdlc.item.sparekey.SpareKeyRules;
import dev.caecorthus.sparkdlc.role.civilian.bear.BearRules;
import dev.caecorthus.sparkdlc.role.civilian.blind.kit.BlindKitRules;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.ControlExpertRules;
import dev.caecorthus.sparkdlc.role.civilian.fisher.FisherRules;
import dev.caecorthus.sparkdlc.role.civilian.hanna.HannaRules;
import dev.caecorthus.sparkdlc.role.civilian.saint.flash.HolyFlashRules;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorRules;
import dev.caecorthus.sparkdlc.role.civilian.usec.UsecRules;
import dev.caecorthus.sparkdlc.role.killer.bellringer.BellRingerRules;
import dev.caecorthus.sparkdlc.role.killer.blackraven.BlackRavenRules;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterRules;
import dev.caecorthus.sparkdlc.role.killer.kidnapper.KidnapperRules;
import dev.caecorthus.sparkdlc.role.killer.ninja.NinjaRules;
import dev.caecorthus.sparkdlc.role.killer.timestealer.TimeStealerRules;
import dev.caecorthus.sparkdlc.role.jiahao.insider.InsiderRules;
import dev.caecorthus.sparkdlc.role.neutral.koko.KokoRules;
import dev.caecorthus.sparkdlc.role.jiahao.sphinxhao.SphinxHaoRules;
import dev.caecorthus.sparkdlc.role.witch.abysslistener.AbyssListenerRules;
import dev.caecorthus.sparkdlc.role.witch.potiongunner.PotionGunnerRules;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.gate.RiftGatePlacementService;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.item.AntidoteItem;
import org.agmas.noellesroles.item.PoisonNeedleItem;
import org.agmas.noellesroles.item.RepairToolItem;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Nominal (full post-use) cooldowns of the items whose cooldown the mod's own roles write, plus the public NoellesRoles
 * item constants, keyed by registry id so the provider works before and after item registration. Initial round-start
 * cooldowns are not nominals. Items without a cooldown (traps, shells, stamps, camera, Swordfish) are absent, the
 * Seeker car is exempt instead, and Wathe items fall through to Wathe's own table.
 * 本模组职业写入其冷却的物品与 NoellesRoles 公开物品常量的标准（使用后完整）冷却，按注册 id 索引，物品注册前后均可用。
 * 开局冷却不算标准冷却。没有冷却的物品（陷阱、弹壳、邮票、摄像头、剑鱼）不在表内，搜寻者小车改为豁免，Wathe 物品回落到
 * Wathe 自身的表。
 */
final class RoleItemCooldownNominals implements ItemCooldownNominalProvider {
    @Override
    public OptionalInt nominalTicks(Item item) {
        return nominalTicks(Registries.ITEM.getId(item));
    }

    static OptionalInt nominalTicks(Identifier itemId) {
        Integer ticks = itemId == null ? null : table().get(itemId);
        return ticks == null ? OptionalInt.empty() : OptionalInt.of(ticks);
    }

    static Map<Identifier, Integer> table() {
        return Table.ENTRIES;
    }

    private static Identifier noelles(String path) {
        return Identifier.of(NoellesRoleIds.NAMESPACE, path);
    }

    /** Lazy holder: the constant owners initialize on first lookup, not at registration. / 惰性持有者：首次查询时才初始化常量所有者。 */
    private static final class Table {
        static final Map<Identifier, Integer> ENTRIES = build();

        private static Map<Identifier, Integer> build() {
            Map<Identifier, Integer> entries = new LinkedHashMap<>();
            // Control Expert. / 控制专家。
            entries.put(SparkDlcItems.TASER_ID, ControlExpertRules.TASER_COOLDOWN);
            entries.put(SparkDlcItems.DISRUPTOR_ID, ControlExpertRules.DISRUPTOR_COOLDOWN);
            entries.put(SparkDlcItems.SHOCK_DEVICE_ID, ControlExpertRules.SHOCK_DEVICE_COOLDOWN);
            // Hunter: the empty-gun reload cooldown, not the 4-tick follow-up. / 猎人：打空后的装填冷却，而非 4 tick 连发间隔。
            entries.put(SparkDlcItems.DOUBLE_BARREL_SHOTGUN_ID, HunterRules.EMPTY_COOLDOWN_TICKS);
            // Time Stealer: display of the authoritative ClockReadyAt. / 窃时者：权威 ClockReadyAt 的显示冷却。
            entries.put(SparkDlcItems.TIME_STEALER_CLOCK_ID, TimeStealerRules.CLOCK_COOLDOWN_TICKS);
            // Conscience Time Stealer: display of the authoritative GiftReadyAt. / 善良窃时者：权威 GiftReadyAt 的显示冷却。
            entries.put(SparkDlcItems.TIME_STEALER_GIFT_WATCH_ID, TimeStealerRules.GIFT_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.TOLL_BELL_ID, BellRingerRules.TOLL_COOLDOWN_TICKS);
            // Angler: rod and the shared edible-fish use cooldown. / 钓鱼佬：鱼竿与共享的食鱼冷却。
            entries.put(SparkDlcItems.FISHING_ROD_ID, FisherRules.ROD_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.SALMON_ID, FisherRules.FISH_USE_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.COD_ID, FisherRules.FISH_USE_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.CLOWNFISH_ID, FisherRules.FISH_USE_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.GOLDFISH_ID, FisherRules.FISH_USE_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.GLIMMERFISH_ID, FisherRules.FISH_USE_COOLDOWN_TICKS);
            // Saint's Holy Flash; the Blind's White Cane write covers its 5 s window plus the 10 s cooldown, not the
            // round-start one.
            // 圣徒圣光弹；盲人盲杖的写入值为 5 秒窗口加 10 秒冷却，而非开局冷却。
            entries.put(SparkDlcItems.HOLY_FLASH_ID, HolyFlashRules.USE_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.WHITE_CANE_ID, BlindKitRules.CANE_USE_ITEM_COOLDOWN_TICKS);
            // Ninja, Black Raven, Kidnapper. / 忍者、黑羽鸦、绑架者。
            entries.put(SparkDlcItems.NINJA_SHURIKEN_ID, NinjaShurikenItem.SHURIKEN_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.NINJA_KNIFE_ID, NinjaKnifeItem.KNIFE_COOLDOWN_TICKS);
            // Grappling Hook: written when a hook cycle ends, not the 90 s round-start lock. / 钩爪：循环结束时写入，而非开局 90 秒锁定。
            entries.put(SparkDlcItems.NINJA_GRAPPLING_HOOK_ID, NinjaRules.GRAPPLING_HOOK_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.FEATHER_BLADE_ID, BlackRavenRules.FEATHER_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.KNOCKOUT_DRUG_ID, KidnapperRules.KNOCKOUT_DRUG_COOLDOWN_TICKS);
            // Grand Witch sword dash and the Murderous Witch Fire Poker. / 大魔女仪式剑冲刺与杀意魔女火钳。
            entries.put(SparkDlcItems.CEREMONIAL_SWORD_ID, CeremonialSwordItem.DASH_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.FIRE_POKER_ID, FirePokerRules.COOLDOWN_TICKS);
            // Abyss Listener Shriek Gun: the post-shot cooldown, not the 60 s initial one. / 聆渊者啸音铳：开火后冷却，而非首次 60 秒。
            entries.put(SparkDlcItems.SHRIEK_GUN_ID, AbyssListenerRules.GUN_COOLDOWN_TICKS);
            // Potion launcher and Rift Gate: 1 s anti-repeat writes, listed so every own-role item write has a nominal.
            // 药炮筒与裂隙门：1 秒防连按写入，列入以保证本模组职业的每个物品写入都有标准冷却。
            entries.put(SparkDlcItems.POTION_LAUNCHER_ID, PotionGunnerRules.FIRE_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.RIFT_GATE_ID, RiftGatePlacementService.PLACE_COOLDOWN_TICKS);
            // USEC rifle: the 2 s bolt (Fast Reload x0.7 is per player), not the 60 s round-start lock; UsecCooldowns is
            // its only writer.
            // USEC 步枪：2 秒拉栓（快速装填 x0.7 因人而异），而非开局 60 秒锁定；UsecCooldowns 是唯一写入方。
            entries.put(SparkDlcItems.USEC_RIFLE_ID, UsecRules.BOLT_TICKS);
            // BEAR M32A1: the 1 s between shots (D12: the admin clear releases it like any vanilla cooldown).
            // BEAR M32A1：两次射击之间的 1 秒（D12：管理员清除冷却时与其他原版冷却一样释放）。
            entries.put(BearRules.M32A1_ITEM_ID, BearRules.M32_COOLDOWN_TICKS);
            // Witch Maiden Wither Gohei: the crimson skull cooldown. / 巫女凋灵御币：赤首冷却。
            entries.put(SparkDlcItems.WITHER_GOHEI_ID, MiniWitherRules.CRIMSON_COOLDOWN_TICKS);
            // Spare Key: the 5 s cooldown after each opening. / 备用钥匙：每次开门后的 5 秒冷却。
            entries.put(SparkDlcItems.SPARE_KEY_ID, SpareKeyRules.COOLDOWN_TICKS);
            // Tono Hanna's shop items (PR #212): the parasol's 10 s after it is opened and the handkerchief's 15 s
            // between uses.
            // 远野汉娜的商店物品（PR #212）：洋伞撑开后的 10 秒，以及手帕两次使用之间的 15 秒。
            entries.put(SparkDlcItems.HANNA_UMBRELLA_ID, HannaRules.UMBRELLA_OPEN_TICKS);
            entries.put(SparkDlcItems.HANNA_HANDKERCHIEF_ID, HannaRules.HANDKERCHIEF_COOLDOWN_TICKS);
            // Savior (PR #200): the pistol's 10 s after every shot and the lightsaber's 30 s after a kill, not the
            // 60 s / 45 s round-start locks.
            // 救世主（PR #200）：手枪每次射击后 10 秒、光剑击杀后 30 秒，而非开局的 60 秒 / 45 秒锁定。
            entries.put(SparkDlcItems.SAVIOR_PISTOL_ID, SaviorRules.PISTOL_COOLDOWN_TICKS);
            entries.put(SparkDlcItems.SAVIOR_LIGHTSABER_ID, SaviorRules.LIGHTSABER_KILL_COOLDOWN_TICKS);
            // 二阶堂希罗's Rope: the 10 s cooldown after an interrupted strangle. / 二阶堂希罗的绳索：勒杀被打断后的 10 秒冷却。
            entries.put(SparkDlcItems.ROPE_ID, RopeRules.INTERRUPT_COOLDOWN_TICKS);
            // 泽渡可可's lightstick (PR #223): the 30 s between uses. / 泽渡可可的应援棒（PR #223）：两次使用之间的 30 秒。
            entries.put(SparkDlcItems.KOKO_LIGHTSTICK_ID, KokoRules.LIGHTSTICK_COOLDOWN_TICKS);
            // 狮身豪's "手"枪: 5 s after a shot, 1 s after a load; the longer of the two is the nominal.
            // 狮身豪的“手”枪：射击后 5 秒、装填后 1 秒；取两者较长者为标准冷却。
            entries.put(SparkDlcItems.SPHINX_HAO_HAND_GUN_ID,
                    Math.max(SphinxHaoRules.GUN_FIRE_COOLDOWN_TICKS, SphinxHaoRules.GUN_LOAD_COOLDOWN_TICKS));
            // Defense Vial (PR #240): the 30 s after a finished drink. / 防御药剂（PR #240）：喝完之后的 30 秒。
            entries.put(SparkDlcItems.DEFENSE_VIAL_ID, DefenseVialRules.COOLDOWN_TICKS);
            // Sniper M1903A4: the 10 s after a shot, not the 2 s reload floor or the 60 s round-start lock;
            // M1903A4Cooldowns is its only writer.
            // 狙击手 M1903A4：开火后 10 秒，而非装填后的 2 秒下限或开局 60 秒锁定；M1903A4Cooldowns 是唯一写入方。
            entries.put(SparkDlcItems.M1903A4_ID, M1903A4Rules.SHOT_COOLDOWN_TICKS);
            // NoellesRoles items: public constants of the pinned 1.7.6 jar, plus the master key, which both
            // NoellesRoles (literal) and the Insider door path write as 200 ticks. The timed bomb stays exempt in
            // the cooldown core itself (ItemCooldownCatalog; Bomber pass gate).
            // NoellesRoles 物品：锁定 1.7.6 jar 的公开常量；万能钥匙由 NoellesRoles（字面量）与内应开门路径都写为 200 tick。
            // 定时炸弹由冷却核心（ItemCooldownCatalog）自身豁免（炸弹客转手门槛）。
            entries.put(noelles("antidote"), AntidoteItem.COOLDOWN_TICKS);
            entries.put(noelles("repair_tool"), RepairToolItem.COOLDOWN_TICKS);
            entries.put(noelles("poison_needle"), PoisonNeedleItem.USE_COOLDOWN_TICKS);
            entries.put(noelles("neutral_master_key"), InsiderRules.MASTER_KEY_COOLDOWN_TICKS);
            return Collections.unmodifiableMap(entries);
        }
    }
}
