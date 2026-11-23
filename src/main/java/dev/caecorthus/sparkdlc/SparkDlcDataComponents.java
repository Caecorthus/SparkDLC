package dev.caecorthus.sparkdlc;

import com.mojang.serialization.Codec;
import dev.caecorthus.sparkdlc.role.civilian.bear.helmet.RysTVisor;
import dev.caecorthus.sparkdlc.role.civilian.bear.launcher.M32Cylinder;
import dev.caecorthus.sparkdlc.role.killer.bandit.BanditRevolverState;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Every item data component of the mod. Ids are a stable save/network contract; components sync to clients for
 * tooltips, item bars, HUDs and model overrides.
 *
 * <p>All components are registered by the static initialiser of this class, in declaration order. That order is the
 * raw-id order the mod has always had (the role kits' drone, Bandit revolver and Timekeeper watch components, then
 * the trait component, then the components of the mod's own roles): add a new component at the end, never in
 * between.</p>
 *
 * 本模组的全部物品数据组件。id 是稳定的存档与网络契约；组件会同步到客户端，用于提示文本、物品条、HUD 与模型覆盖。
 *
 * <p>所有组件都由本类的静态初始化器按声明顺序注册。该顺序就是本模组一直以来的 raw id 顺序（职业套件的无人机、强盗左轮与
 * 守时者怀表组件，然后是词条组件，最后是本模组自有职业的组件）：新增组件请追加到末尾，不要插在中间。</p>
 */
public final class SparkDlcDataComponents {
    public static final Identifier DRONE_CHARGE_ID = SparkDLC.id("drone_charge");
    public static final Identifier DRONE_PAYLOAD_ID = SparkDLC.id("drone_payload");
    public static final Identifier BANDIT_REVOLVER_ID = SparkDLC.id("bandit_revolver");
    /** Drone battery in basis points (see DroneRules.CHARGE_MAX). / 无人机电量，万分比（见 DroneRules.CHARGE_MAX）。 */
    private static final ComponentType<Integer> droneCharge = Registry.register(
            Registries.DATA_COMPONENT_TYPE, DRONE_CHARGE_ID,
            ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).build());
    /** Grenade drone has a bound M67. / 投弹无人机已挂载 M67。 */
    private static final ComponentType<Boolean> dronePayload = Registry.register(
            Registries.DATA_COMPONENT_TYPE, DRONE_PAYLOAD_ID,
            ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build());
    /** Parts and cylinder of a Bandit revolver. / 强盗改装左轮的配件与弹巢。 */
    private static final ComponentType<BanditRevolverState> banditRevolver = Registry.register(
            Registries.DATA_COMPONENT_TYPE, BANDIT_REVOLVER_ID,
            ComponentType.<BanditRevolverState>builder().codec(BanditRevolverState.CODEC)
                    .packetCodec(BanditRevolverState.PACKET_CODEC).build());

    /** 怀表模式数据组件：0=刷新物品冷却，1=刷新技能冷却。 */
    public static final ComponentType<Integer> TIMEKEEPER_WATCH_MODE = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            SparkDLC.id("timekeeper_watch_mode"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.INTEGER)
                    .build()
    );

    public static final ComponentType<String> CONSCIENCE_POISONER = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            SparkDLC.id("conscience_poisoner"),
            ComponentType.<String>builder().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build()
    );

    /**
     * Per-stack marker of the knife issued to a Knight duel target ({@code KnightDuelKnives}): only that stack is
     * restricted to stabbing the Knight, and every marked stack is confiscated when the duel ends.
     * 发给骑士决斗对手的小刀的逐物品堆标记（{@code KnightDuelKnives}）：只有该物品堆被限制为只能刺骑士，
     * 决斗结束时所有带标记的物品堆都会被收回。
     */
    public static final ComponentType<Boolean> DUEL_KNIFE = register("duel_knife",
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOL));

    /**
     * Per-stack marker of the revolver temporarily issued by the Dominator's forced shot: only that stack is
     * confiscated when the sequence ends.
     * 支配者强制开枪临时发放的左轮手枪逐物品堆标记：序列结束时只没收带此标记的物品堆。
     */
    public static final ComponentType<Boolean> FORCED_SHOT_GUN = register("forced_shot_gun",
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOL));

    /**
     * Frozen contract (BEAR WP0): the M32A1's six-chamber cylinder ({@link M32Cylinder}), persistent and synced to
     * clients (HUD). Every launcher stack carries it as a default component (empty).
     * 冻结契约（BEAR WP0）：M32A1 的六弹膛转轮（{@link M32Cylinder}），持久化并同步给客户端（HUD）。每个发射器物品堆都以默认组件
     * （空）携带它。
     */
    public static final ComponentType<M32Cylinder> M32_CYLINDER = register("m32_cylinder",
            ComponentType.<M32Cylinder>builder()
                    .codec(M32Cylinder.CODEC)
                    .packetCodec(M32Cylinder.PACKET_CODEC));

    /**
     * Frozen contract (BEAR WP0): the Rys-T visor state and glass cracks ({@link RysTVisor}), persistent and synced to
     * every client through equipment sync (others see the visor and cracks). Default on every helmet stack.
     * 冻结契约（BEAR WP0）：Rys-T 面罩状态与玻璃裂纹（{@link RysTVisor}），持久化并经装备同步发给所有客户端（他人也能看到面罩与
     * 裂纹）。每个头盔物品堆都带有其默认值。
     */
    public static final ComponentType<RysTVisor> RYS_T_VISOR = register("rys_t_visor",
            ComponentType.<RysTVisor>builder()
                    .codec(RysTVisor.CODEC)
                    .packetCodec(RysTVisor.PACKET_CODEC));

    /**
     * Stable contract: the Sphinx Hao "手"枪 holds one fist ({@code true}); absent or {@code false} is empty. Persistent
     * and synced, so the client model override and tooltip read it; only the server's gun use writes it.
     * 稳定契约：狮身豪“手”枪装有一发拳头（{@code true}）；缺省或 {@code false} 为空膛。持久化并同步，客户端模型覆盖与提示文本
     * 读取它；只有服务端的手枪使用会写入它。
     */
    public static final ComponentType<Boolean> SPHINX_HAO_LOADED = register("sphinx_hao_loaded",
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .packetCodec(PacketCodecs.BOOL));

    private SparkDlcDataComponents() {
    }

    private static <T> ComponentType<T> register(String path, ComponentType.Builder<T> builder) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, SparkDLC.id(path), builder.build());
    }

    /** Forces class initialization during mod init. / 在模组初始化时强制加载本类。 */
    public static void register() {
    }

    public static ComponentType<Integer> droneCharge() {
        return droneCharge;
    }

    public static ComponentType<Boolean> dronePayload() {
        return dronePayload;
    }

    public static ComponentType<BanditRevolverState> banditRevolver() {
        return banditRevolver;
    }
}
