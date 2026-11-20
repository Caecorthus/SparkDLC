package dev.caecorthus.sparkdlc.role.killer.timestealer;

import dev.caecorthus.sparkdlc.SparkDlcDeathReasons;
import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerWeaponTarget;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.role.civilian.seeker.hit.SeekerDeviceHits;
import dev.caecorthus.sparkdlc.util.ExactItemCooldowns;
import dev.doctor4t.wathe.record.GameRecordManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

/**
 * Server-side Clock use flow: gates, target, theft, authoritative cooldown, feedback and replay. Returns CONSUME on a
 * committed theft, a Seeker device absorb (Q9) or a Partygoer kill (2026-10-21), FAIL otherwise, never
 * SUCCESS (a success would broadcast an arm swing).
 * 服务端时钟使用流程：判定、目标、窃取、权威冷却、反馈与回放。成功窃取、被搜寻者设备吸收（Q9）或打死派对客
 * （2026-10-21）时返回 CONSUME，其余返回 FAIL，绝不返回 SUCCESS（SUCCESS 会广播挥手动作）。
 *
 * <p>Only two owner-only outcomes exist, "stolen" and one generic "no target", so a free miss cannot be used to probe
 * hidden states (invisibility, Last Escape, faction vetoes, an existing theft). A refusal costs nothing; the cooldown is
 * written only after {@link TimeTheftRuntime#steal} committed, or when a nearer Seeker device shielding an eligible
 * target absorbed the Clock (owner decision Q9; unlike the Taser and the Feather Blade, a miss never breaks a device),
 * or after the Clock killed a Partygoer. The authoritative deadline is {@code ClockReadyAt}; the item cooldown is
 * display only, written exactly through {@code SparkTraitsApi}; its vanilla fallback is unreachable since the fusion.
 * 只有两种仅所有者可见的结果：“已窃取”与一条通用的“无目标”，因此免费的未命中无法用来探测隐藏状态（隐身、最后逃脱、
 * 阵营否决、已被窃）。被拒绝不付出任何代价；只有在 {@link TimeTheftRuntime#steal} 提交之后，或更近的搜寻者设备吸收了
 * 时钟时（所有者决定 Q9；与电击枪和羽刃不同，未命中绝不打坏设备），或时钟打死派对客之后，才写入冷却。权威截止为 {@code ClockReadyAt}；物品冷却只作显示，
 * 经 {@code SparkTraitsApi} 精确写入；其原版回退分支融合后不可达。
 */
public final class TimeStealerClockService {
    static final String STOLEN_KEY = "message.sparkdlc.time_stealer.stolen";
    static final String NO_TARGET_KEY = "message.sparkdlc.time_stealer.no_target";

    private TimeStealerClockService() {
    }

    public static TypedActionResult<ItemStack> use(ServerPlayerEntity user, ItemStack stack, Hand hand) {
        if (user == null || user.getWorld().isClient()) {
            // The item already returns consume on the client; this service is server-only.
            // 物品在客户端已直接返回 consume；本服务仅在服务端运行。
            return TypedActionResult.consume(stack);
        }
        Item clock = stack.getItem();
        ServerWorld world = user.getServerWorld();
        TimeStealerPlayerComponent state = TimeStealerPlayerComponent.KEY.get(user);
        if (!TimeStealerTargeting.canUse(user, stack)) {
            restoreDisplayedCooldown(user, stack, state);
            return refuse(user, stack);
        }

        // Players the Time Stealer may not affect are filtered before the nearest pick, so they are transparent.
        // 窃时者无法影响的玩家在选取最近者之前就被过滤，因此对射线透明。
        ServerPlayerEntity aimed = ClockGeometry.findTarget(user, TimeStealerRules.CLOCK_RANGE, world.getPlayers(),
                candidate -> TimeStealerTargeting.canAffect(user, candidate));
        if (aimed == null) {
            return killPartygoerOrRefuse(user, stack, clock, state);
        }
        // A Partygoer strictly nearer on the same ray is decided here, with the Clock's own aim tolerance, before
        // the Partygoer hook in the Seeker device seam could judge it with another one; it dies and the player behind
        // keeps their time.
        // 同一射线上更近的派对客在此处判定（使用时钟自身的瞄准容差），先于搜寻者设备接缝中的派对客钩子以另一套容差判定；
        // 派对客死亡，其后的玩家保住时间。
        Entity shield = ClockGeometry.findEntity(user, TimeStealerRules.CLOCK_RANGE,
                PartygoerWeaponTarget::isKillable);
        if (shield != null
                && ClockGeometry.aimEntrySquared(user, shield, TimeStealerRules.CLOCK_RANGE)
                < ClockGeometry.aimEntrySquared(user, aimed, TimeStealerRules.CLOCK_RANGE)
                && PartygoerWeaponTarget.killByWeapon(user, shield, SparkDlcDeathReasons.TIME_STOLEN)) {
            startCooldown(user, clock, state);
            return TypedActionResult.consume(stack);
        }
        // Seeker seam (Q9): a nearer Seeker device absorbs the Clock and breaks; nothing is stolen, cooldown applies.
        // Called only with a picked target: a Clock miss is free, so a device with nobody eligible behind it is left
        // intact rather than becoming a free device breaker.
        // 搜寻者接缝（Q9）：更近的搜寻者设备吸收时钟并被打坏；不窃取任何人，但照常进入冷却。仅在已选中目标时调用：
        // 时钟未命中是免费的，因此其后没有合格目标的设备保持完好，而不会变成免费的拆设备手段。
        ServerPlayerEntity target = SeekerDeviceHits.onClockFired(user, aimed, TimeStealerRules.CLOCK_RANGE);
        if (target == null) {
            startCooldown(user, clock, state);
            user.sendMessage(Text.translatable(NO_TARGET_KEY), true);
            return TypedActionResult.consume(stack);
        }
        if (!TimeStealerTargeting.canAffect(user, target) || !TimeTheftRuntime.steal(target, user)) {
            return refuse(user, stack);
        }

        // Committed: only now is the cooldown written. / 已提交：此时才写入冷却。
        startCooldown(user, clock, state);
        user.sendMessage(Text.translatable(STOLEN_KEY), true);
        GameRecordManager.recordItemUse(user, TimeStealerRules.CLOCK_ID, target, new NbtCompound());
        return TypedActionResult.consume(stack);
    }

    /**
     * Partygoer seam (owner rule 2026-10-21: whatever kills a player kills a Partygoer in one hit), reached only when
     * the Clock picked no eligible player: a killable Partygoer on the same Clock ray dies at once
     * ({@code TIME_STOLEN}, the curse's own kill) and the Clock cools down as after a theft; the Partygoer's reward
     * line is the feedback (no record: the Partygoer feature records the kill). Otherwise the usual free refusal. When
     * a player was picked, a strictly nearer Partygoer is decided above, before the device seam, so one use kills at
     * most once and starts one cooldown.
     * 派对客接缝（所有者规则 2026-10-21：能杀死玩家的东西一击打死派对客），仅在时钟没有选中合格玩家时进入：同一条
     * 时钟射线上的可击杀派对客立即死亡（{@code TIME_STOLEN}，即诅咒自身的击杀死因），时钟与窃取后一样进入冷却；以派对客
     * 的奖励提示作为反馈（不写记录：由派对客功能记录击杀）。否则照常免费拒绝。选中玩家时，更近的派对客已在上方、设备接缝之前
     * 判定，因此一次使用至多击杀一次、只开始一次冷却。
     */
    private static TypedActionResult<ItemStack> killPartygoerOrRefuse(ServerPlayerEntity user, ItemStack stack,
                                                                      Item clock, TimeStealerPlayerComponent state) {
        Entity partygoer = ClockGeometry.findEntity(user, TimeStealerRules.CLOCK_RANGE,
                PartygoerWeaponTarget::isKillable);
        if (partygoer == null
                || !PartygoerWeaponTarget.killByWeapon(user, partygoer, SparkDlcDeathReasons.TIME_STOLEN)) {
            return refuse(user, stack);
        }
        startCooldown(user, clock, state);
        return TypedActionResult.consume(stack);
    }

    /** No cooldown, one generic owner-only line. / 不写冷却，只给一条仅所有者可见的通用提示。 */
    private static TypedActionResult<ItemStack> refuse(ServerPlayerEntity user, ItemStack stack) {
        user.sendMessage(Text.translatable(NO_TARGET_KEY), true);
        return TypedActionResult.fail(stack);
    }

    private static void startCooldown(ServerPlayerEntity user, Item clock, TimeStealerPlayerComponent state) {
        state.setClockReadyAt(user.getWorld().getTime() + TimeStealerRules.CLOCK_COOLDOWN_TICKS);
        showCooldown(user, clock, TimeStealerRules.CLOCK_COOLDOWN_TICKS);
    }

    /**
     * Vanilla refuses a cooling item before {@code Item#use}, so reaching here inside {@code ClockReadyAt} means the
     * display cooldown was shortened elsewhere (NoellesRoles Stimulation, Last Escape halving) since the last
     * {@link #keepDisplayedCooldown}. When the refusal is due to the deadline alone, rewrite the display to the
     * authoritative remaining ticks.
     * 原版会在 {@code Item#use} 之前拒绝冷却中的物品，因此在 {@code ClockReadyAt} 之前到达这里说明显示冷却在上次
     * {@link #keepDisplayedCooldown} 之后已被其他机制缩短（NoellesRoles 兴奋剂、最后逃脱减半）。当拒绝仅由截止 tick 造成时，
     * 把显示冷却重写为权威剩余值。
     */
    private static void restoreDisplayedCooldown(ServerPlayerEntity user, ItemStack stack,
                                                 TimeStealerPlayerComponent state) {
        Item clock = stack.getItem();
        long remaining = state.clockReadyAt() - user.getWorld().getTime();
        if (remaining <= 0L || user.getItemCooldownManager().isCoolingDown(clock)
                || !TimeStealerTargeting.canUseWhenReady(user, stack)) {
            return;
        }
        showCooldown(user, clock, (int) Math.min(Integer.MAX_VALUE, remaining));
    }

    /**
     * Holder's per-tick upkeep: the slot never shows less than {@code ClockReadyAt}, so it never looks ready while the
     * Clock is refused. A removed cooldown (the admin {@code clearCooldown} command) has already released
     * {@code ClockReadyAt} ({@code RoleItemCooldownReleases}), so it stays cleared. Exact writes only: a vanilla
     * {@code set} would be shortened again (NoellesRoles Stimulation) and then rewritten every tick.
     * 持有者的每 tick 维护：栏位显示绝不少于 {@code ClockReadyAt}，因此时钟被拒绝时不会显示为就绪。被移除的冷却
     * （管理员 {@code clearCooldown} 命令）已先释放 {@code ClockReadyAt}（{@code RoleItemCooldownReleases}），
     * 因此保持清除。仅用精确写入：原版 {@code set} 会再次被缩短（NoellesRoles 兴奋剂）并每 tick 重写。
     */
    static void keepDisplayedCooldown(ServerPlayerEntity user) {
        Item clock = SparkDlcItems.timeStealerClock();
        long remaining = TimeStealerPlayerComponent.KEY.get(user).clockReadyAt() - user.getWorld().getTime();
        if (TimeStealerRules.clockDisplayLags(remaining, ExactItemCooldowns.remainingTicks(user, clock))) {
            SparkTraitsApi.setExactItemCooldownRemaining(user, clock, (int) Math.min(Integer.MAX_VALUE, remaining));
        }
    }

    /**
     * The trait API's exact write performs the vanilla {@code set} itself past cooldown modifiers, so it goes first;
     * the plain vanilla write is the fallback for a refused exact write (null player or item only), unreachable since
     * the fusion (never both: two cooldown packets).
     * 词条 API 的精确写入会越过冷却倍率自行完成原版写入，因此优先调用；原版写入是精确写入被拒（仅玩家或物品为空）时的回退，
     * 融合后不可达（二者不会同时执行，以免发送两次冷却数据包）。
     */
    private static void showCooldown(ServerPlayerEntity user, Item clock, int ticks) {
        if (!SparkTraitsApi.setExactItemCooldownRemaining(user, clock, ticks)) {
            user.getItemCooldownManager().set(clock, ticks);
        }
    }
}
