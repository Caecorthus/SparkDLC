package dev.caecorthus.sparkdlc.bridge.strength;

import dev.caecorthus.sparkdlc.skill.api.SparkWitchApi;
import dev.caecorthus.sparkdlc.bridge.witch.SparkWitchKidnapperCompat;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * SparkWitch 兼容桥。
 *
 * <p>各模块已融合进 SparkDLC：SparkWitch 始终存在于同一个 jar 中，因此这里直接调用它暴露的稳定门面
 * （{@link SparkWitchApi} 与 {@link SparkWitchKidnapperCompat}），不再按名称反射，也不再检测 sparkwitch 是否加载。
 * The modules are fused in SparkDLC: SparkWitch always ships in the same jar, so this calls its stable facades
 * ({@link SparkWitchApi} and {@link SparkWitchKidnapperCompat}) directly, with no lookup by name and no
 * mod-loaded gate.</p>
 */
public final class SparkWitchCompat {
    private SparkWitchCompat() {
    }

    /** Always true: SparkWitch is a module of the same SparkDLC jar. Kept for its callers.
     *  恒为 true：SparkWitch 是同一个 SparkDLC jar 中的模块；为调用方保留。 */
    public static boolean isLoaded() {
        return true;
    }

    public static @Nullable ItemStack createKidnapperDrugStack() {
        return SparkWitchKidnapperCompat.createKnockoutDrugStack();
    }

    public static @Nullable ShopEntry createKidnapperDrugShopEntry() {
        return SparkWitchKidnapperCompat.createKnockoutDrugShopEntry();
    }

    public static @Nullable ShopEntry createKidnapperDrugShopEntry(PlayerEntity player) {
        return SparkWitchKidnapperCompat.createKnockoutDrugShopEntry(player);
    }

    /**
     * SparkWitch Control Expert stun, read through its public facade {@code SparkWitchApi.isControlExpertStunned}
     * (a direct call since the SparkDLC fusion). False for a null player.
     * SparkWitch 控场专家眩晕，经其公开门面 SparkWitchApi.isControlExpertStunned 读取（融合进 SparkDLC 后为直接调用）。
     * 玩家为 null 时返回 false。
     */
    public static boolean isControlExpertStunned(@Nullable PlayerEntity player) {
        return player != null && SparkWitchApi.isControlExpertStunned(player);
    }

    /**
     * Render thread, client presentation only: whether SparkWitch's Blind view on this client strips every feature from
     * {@code player}'s body right now ({@code SparkWitchApi#hidesFeaturesFromBlind}). A SparkStrength extra drawn
     * outside the feature loop skips itself while this holds; today that is the skateboard under a replaced (Pig) body.
     * Asked every frame; a direct call since the SparkDLC fusion, so there is no lookup to cache. A null player answers
     * false, so everything draws as before.
     * 仅限渲染线程、仅用于客户端展示：此客户端上 SparkWitch 的盲人视图此刻是否去掉 {@code player} 身体上的全部附加层
     * （{@code SparkWitchApi#hidesFeaturesFromBlind}）。在附加层循环之外绘制的 SparkStrength 附加物在此为真时跳过自身，
     * 目前即被替换的（猪）身体下方的滑板。每帧都会询问；融合进 SparkDLC 后为直接调用，无需缓存查找。玩家为 null 时返回
     * false，一切照旧绘制。
     */
    public static boolean hidesFeaturesFromBlind(PlayerEntity player) {
        return player != null && SparkWitchApi.hidesFeaturesFromBlind(player);
    }

    /**
     * Client pick for the Serial Killer pistols, through SparkWitch's frozen
     * {@code SparkWitchApi.preferNearerGunWorldTarget(PlayerEntity, HitResult, double)}: the Seeker device step SparkWitch
     * wraps inside Wathe's {@code RevolverItem#use}, which the pistols' own {@code use} never runs. A direct call since
     * the SparkDLC fusion; returns {@code gunTarget} unchanged for a null argument or a null answer.
     * 连环杀手手枪的客户端选靶，经 SparkWitch 冻结的 {@code SparkWitchApi.preferNearerGunWorldTarget} 补上 SparkWitch 包装在
     * Wathe {@code RevolverItem#use} 内、而手枪自己的 {@code use} 不会执行的搜寻者设备一步。融合进 SparkDLC 后为直接调用；
     * 参数为 null 或结果为 null 时原样返回 {@code gunTarget}。
     */
    public static HitResult preferNearerGunWorldTarget(PlayerEntity shooter, HitResult gunTarget, double range) {
        if (shooter == null || gunTarget == null) {
            return gunTarget;
        }
        HitResult result = SparkWitchApi.preferNearerGunWorldTarget(shooter, gunTarget, range);
        return result != null ? result : gunTarget;
    }

    /**
     * Server thread: lets SparkWitch end a Magician puppet or break a Seeker device that a pistol shot's non-player
     * target id names, exactly as for a Wathe revolver shot, through its frozen
     * {@code SparkWitchApi.hitGunWorldTarget(ServerPlayerEntity, Entity, ItemStack, double)}. Call where Wathe's receiver
     * records the shot (after the click, before the record and cooldown); the caller then finishes the shot as a miss.
     * A direct call since the SparkDLC fusion; false (nothing happens) for a null argument.
     * 服务端线程：经 SparkWitch 冻结的 {@code SparkWitchApi.hitGunWorldTarget}，让 SparkWitch 像处理 Wathe 左轮射击一样结束
     * 手枪射击中非玩家目标 id 所指的魔术师皮套或打坏搜寻者设备。在 Wathe 接收器记录这一枪的位置调用（扳机声之后、记录与冷却
     * 之前）；调用方随后按未命中收尾。融合进 SparkDLC 后为直接调用；参数为 null 时返回 false（无事发生）。
     */
    public static boolean hitGunWorldTarget(ServerPlayerEntity shooter, Entity target, ItemStack gun,
                                            double maxDistance) {
        return shooter != null && target != null && gun != null
                && SparkWitchApi.hitGunWorldTarget(shooter, target, gun, maxDistance);
    }

    /**
     * Server authority: whether {@code target} is the active SparkWitch Vendetta bound to {@code actor}, through the
     * frozen {@code SparkWitchApi.isBoundKillerTargetingVendetta(PlayerEntity, PlayerEntity)}. Wathe counts that
     * Vendetta as dead, yet a revolver shot by its bound killer still reaches killPlayer, which resolves its terminal
     * death. A direct call since the SparkDLC fusion; false for a null argument.
     * 以服务端为准：{@code target} 是否为与 {@code actor} 绑定的激活 SparkWitch 仇杀客，经冻结的
     * {@code SparkWitchApi.isBoundKillerTargetingVendetta} 查询。Wathe 将该仇杀客视为已死亡，但其绑定凶手的左轮射击仍会进入
     * killPlayer 并结算其终局死亡。融合进 SparkDLC 后为直接调用；参数为 null 时返回 false。
     */
    public static boolean isBoundKillerTargetingVendetta(PlayerEntity actor, PlayerEntity target) {
        return actor != null && target != null && SparkWitchApi.isBoundKillerTargetingVendetta(actor, target);
    }

    /**
     * Server thread, Bodyguard vest: asks SparkWitch's frozen
     * {@code SparkWitchApi.tryPierceShieldLayer(PlayerEntity, PlayerEntity, Identifier)} whether the shot being settled
     * against exactly this victim, killer and death reason pierces one more shield layer (today only the USEC AXMC's
     * {@code wathe:gun_shot}: FMJ 2 layers, AP 5). True has already spent that pierce: the vest is then spent and the
     * same kill goes on. A direct call since the SparkDLC fusion; false for a null argument and for every other kill,
     * so the vest then stops it as before.
     * 服务端线程，保镖防弹衣：经 SparkWitch 冻结的 {@code SparkWitchApi.tryPierceShieldLayer} 询问正对确切的受害者、击杀者与
     * 死因结算的这一枪能否再击穿一层护盾（目前只有 USEC AXMC 的 {@code wathe:gun_shot}：FMJ 2 层，AP 5 层）。返回 true 时该次
     * 穿透已被花费：防弹衣随之报废，同一次击杀继续。融合进 SparkDLC 后为直接调用；参数为 null 以及其他任何击杀时返回 false，
     * 防弹衣照旧挡下。
     */
    public static boolean tryPierceShieldLayer(@Nullable ServerPlayerEntity victim, @Nullable ServerPlayerEntity killer,
                                               @Nullable Identifier deathReason) {
        return victim != null && killer != null && deathReason != null
                && SparkWitchApi.tryPierceShieldLayer(victim, killer, deathReason);
    }

    /**
     * Server thread, Democracy Shield: the stable round id ({@code "fmj"} / {@code "ap"}) of the shield-piercing shot
     * SparkWitch is settling against exactly this victim, killer and death reason, through its frozen
     * {@code SparkWitchApi.piercingShotAmmoId(PlayerEntity, PlayerEntity, Identifier)}. Spends nothing; the shield is
     * never pierced and only prices its block by this round ({@code BodyguardProtectionRules.shieldPoints}). A direct
     * call since the SparkDLC fusion; null for a null argument and for every other kill (revolvers included), so the
     * block then keeps its usual cost.
     * 服务端线程，民主盾牌：经 SparkWitch 冻结的 {@code SparkWitchApi.piercingShotAmmoId} 读取 SparkWitch 正对确切的受害者、
     * 击杀者与死因结算的穿盾射击的稳定弹种 id（{@code "fmj"} / {@code "ap"}）。不花费任何预算；盾牌从不被击穿，只按弹种为格挡
     * 定价（{@code BodyguardProtectionRules.shieldPoints}）。融合进 SparkDLC 后为直接调用；参数为 null 以及其他任何击杀
     * （含左轮）时返回 null，格挡保持原有消耗。
     */
    public static @Nullable String piercingShotAmmoId(@Nullable ServerPlayerEntity victim,
                                                      @Nullable ServerPlayerEntity killer,
                                                      @Nullable Identifier deathReason) {
        if (victim == null || killer == null || deathReason == null) {
            return null;
        }
        return SparkWitchApi.piercingShotAmmoId(victim, killer, deathReason);
    }

    /**
     * Whether {@code player} is an active SparkWitch Wraith, promoted forms included, read through the public facade
     * {@code SparkWitchApi.isWraithActive}. The Wraith component syncs to every client, so this answers on both sides.
     * A direct call since the SparkDLC fusion; false for a null player.
     * {@code player} 是否为激活中的 SparkWitch 冤魂（含晋升形态），经公开门面 SparkWitchApi.isWraithActive 读取。冤魂组件同步给
     * 所有客户端，因此两端都能回答。融合进 SparkDLC 后为直接调用；玩家为 null 时返回 false。
     */
    public static boolean isWraithActive(@Nullable PlayerEntity player) {
        return player != null && SparkWitchApi.isWraithActive(player);
    }

    /**
     * Side-neutral: whether {@code entity} is a SparkWitch Black Raven Raven Form crow (化鸦), through the frozen
     * {@code SparkWitchApi.isBlackRavenCrow(Entity)}. Lets a client gate that accepts only players (the Undercover
     * tomahawk) still send the attack on a crow; the server decides through {@link #crowMeleeStandIn}. A direct call
     * since the SparkDLC fusion; false for null and for players.
     * 两端通用：{@code entity} 是否为 SparkWitch 黑羽鸦化鸦乌鸦，经冻结的 {@code SparkWitchApi.isBlackRavenCrow} 查询。让只接受
     * 玩家的客户端门槛（卧底战术斧）对乌鸦也发送攻击；由服务端经 {@link #crowMeleeStandIn} 判定。融合进 SparkDLC 后为直接调用；
     * null 与玩家返回 false。
     */
    public static boolean isBlackRavenCrow(@Nullable Entity entity) {
        return entity != null && !(entity instanceof PlayerEntity) && SparkWitchApi.isBlackRavenCrow(entity);
    }

    /**
     * Server thread: the Black Raven that a melee stab or strike on {@code target} reaches when {@code target} is its
     * Raven Form crow, through SparkWitch's frozen
     * {@code SparkWitchApi.crowMeleeStandIn(ServerPlayerEntity, Entity, double)}: a crow {@code attacker} may hit within
     * {@code reach} (+0.5 latency, lag-compensated, clear line) returns its owner and arms SparkWitch's owner
     * substitution, so the caller runs its own player path on that owner, skipping only its own distance check on the
     * body; its {@code killPlayer} then ends the Raven Form session first. A direct call since the SparkDLC fusion; null
     * (nothing armed) for players, nulls and other entities, so the caller keeps today's non-player handling.
     * 服务端线程：{@code target} 是黑羽鸦化鸦乌鸦时，对其的近战刺击或劈砍所命中的黑羽鸦，经 SparkWitch 冻结的
     * {@code SparkWitchApi.crowMeleeStandIn} 查询：{@code attacker} 可命中、位于 {@code reach}（+0.5 延迟、延迟补偿、视线畅通）内的
     * 乌鸦返回其主人并装好 SparkWitch 的主人替身，调用方随后对该主人执行自身的玩家路径，只跳过自身对本体的距离检查；其
     * {@code killPlayer} 会先结束化鸦会话。融合进 SparkDLC 后为直接调用；玩家、null 与其他实体返回 null（不装入替身），
     * 调用方保持现有的非玩家处理。
     */
    public static @Nullable ServerPlayerEntity crowMeleeStandIn(@Nullable ServerPlayerEntity attacker,
                                                                @Nullable Entity target, double reach) {
        if (attacker == null || target == null || target instanceof PlayerEntity) {
            return null;
        }
        return SparkWitchApi.crowMeleeStandIn(attacker, target, reach);
    }

    /**
     * Server thread, after a SparkStrength blast killed its players: kills the Black Raven behind every Raven Form crow
     * within {@code radius} of {@code center} (box centre, clear line from the centre) through SparkWitch's frozen
     * {@code SparkWitchApi.killCrowsInBlast(ServerWorld, Vec3d, double, ServerPlayerEntity, Identifier)}, credited to
     * {@code killer} with {@code deathReason} passed through as the same instance. Returns how many crows were hit; a
     * direct call since the SparkDLC fusion, 0 for nulls (no crow is touched).
     * 服务端线程，在 SparkStrength 爆炸击杀其范围内玩家之后：经 SparkWitch 冻结的 {@code SparkWitchApi.killCrowsInBlast}，处死
     * {@code center} 周围 {@code radius} 内（箱体中心、与爆心视线畅通）每只化鸦乌鸦背后的黑羽鸦，记在 {@code killer} 名下，
     * {@code deathReason} 按同一实例原样传递。返回被命中的乌鸦数量；融合进 SparkDLC 后为直接调用，参数为 null 时返回 0
     * （不触碰任何乌鸦）。
     */
    public static int killCrowsInBlast(@Nullable ServerWorld world, @Nullable Vec3d center, double radius,
                                       @Nullable ServerPlayerEntity killer, @Nullable Identifier deathReason) {
        if (world == null || center == null || deathReason == null) {
            return 0;
        }
        return SparkWitchApi.killCrowsInBlast(world, center, radius, killer, deathReason);
    }

    /**
     * Server thread, purely visual: a real SparkStrength blast (M67, Bomber drone) punches a temporary hole of
     * {@code radius} blocks into the SparkWitch BEAR smoke near {@code center}, through the frozen
     * {@code SparkWitchApi.carveSmoke(ServerWorld, Vec3d, double)} (owner S3). Smoke never feeds a server rule, so
     * nothing happens for nulls or when the call fails (never logged: it would repeat on every blast). A direct call
     * since the SparkDLC fusion; the catch only keeps a failing visual from breaking the blast.
     * 服务端线程，纯视觉：SparkStrength 的真实爆炸（M67、炸弹客无人机）经冻结的 {@code SparkWitchApi.carveSmoke} 在
     * {@code center} 附近的 SparkWitch BEAR 烟雾中炸开半径 {@code radius} 格的临时空洞（所有者 S3）。烟雾从不影响任何服务端
     * 规则，因此参数为 null 或调用失败时什么也不做（不记录日志：每次爆炸都会重复）。融合进 SparkDLC 后为直接调用；
     * 保留的 catch 只用于避免视觉效果出错时破坏爆炸本身。
     */
    public static void carveSmoke(@Nullable ServerWorld world, @Nullable Vec3d center, double radius) {
        if (world == null || center == null) {
            return;
        }
        try {
            SparkWitchApi.carveSmoke(world, center, radius);
        } catch (RuntimeException | LinkageError ignored) {
            // Visual only: the blast itself is unaffected. / 仅视觉：爆炸本身不受影响。
        }
    }
}
