package dev.caecorthus.sparkdlc.role.civilian.orthopedist;

/**
 * Pure tuning and decisions of the Exoskeleton (owner 2026-11-28): the Orthopedist's shop item that is fitted onto a
 * player in layers. Each layer adds the base maximum stamina once more, and a wearer's fractures run down twice as
 * fast. No Minecraft runtime here, so the item, the component and the Hunter's injury tick share one set of numbers.
 * 外骨骼（所有者 2026-11-28）的纯数值与判定：骨科大夫商店出售、按层装到玩家身上的道具。每层再增加一份基础体力上限，
 * 穿戴者的骨折以两倍速度消退。这里不依赖 Minecraft 运行时，物品、组件与猎人的伤势计时共用同一组数值。
 */
public final class ExoskeletonRules {
    /** Bare shop-entry id, like the mod's other role shops. / 不带命名空间的商店条目 id，与本模组其他职业商店一致。 */
    public static final String SHOP_ENTRY_ID = "orthopedist_exoskeleton";
    public static final int PRICE = 75;
    public static final int MAX_LAYERS = 2;
    /** Same reach as the Cologne. / 与古龙水相同的距离。 */
    public static final double FIT_RANGE_BLOCKS = 3.0D;
    /**
     * Share of the base maximum stamina one layer adds (1 layer = 2x, 2 layers = 3x).
     * 每层增加的基础体力上限比例（1 层 = 2 倍，2 层 = 3 倍）。
     */
    public static final double STAMINA_BONUS_PER_LAYER = 1.0D;
    /**
     * Ticks a wearer's fracture layer loses per server tick: a 60 s layer lasts 30 s, and layers gained before the
     * fitting run down at this speed from then on.
     * 穿戴者的每层骨折每个服务端刻减少的刻数：60 秒的一层只持续 30 秒，装配前已有的骨折此后也按此速度消退。
     */
    public static final int WEARER_FRACTURE_TICK_STEP = 2;

    private ExoskeletonRules() {
    }

    /**
     * Amount of the one stamina modifier for this many layers ({@code ADD_MULTIPLIED_BASE}); 0 means no modifier.
     * 该层数对应的唯一体力修饰符数值（{@code ADD_MULTIPLIED_BASE}）；0 表示不加修饰符。
     */
    public static double staminaBonus(int layers) {
        return STAMINA_BONUS_PER_LAYER * clampLayers(layers);
    }

    /** Fracture ticks taken off per server tick. / 每个服务端刻扣除的骨折刻数。 */
    public static int fractureTickStep(int layers) {
        return layers > 0 ? WEARER_FRACTURE_TICK_STEP : 1;
    }

    public static int clampLayers(int layers) {
        return Math.clamp(layers, 0, MAX_LAYERS);
    }

    /**
     * Layers belong to one round: they are kept only while a round runs and the wearer is one of its players. Death,
     * reset and round end clear them through the role's own cleanup; this covers data that outlived them (a wearer
     * who was offline when the round ended).
     * 层数只属于一局：仅在对局进行中且穿戴者是本局玩家时保留。死亡、重置与对局结束由职业自身的清理负责；这里兜住
     * 在清理之外残留的数据（对局结束时不在线的穿戴者）。
     */
    public static boolean keepsLayers(boolean roundRunning, boolean hasRoundRole) {
        return roundRunning && hasRoundRole;
    }

    /**
     * Server verdict of one use, in the order the messages are chosen. Any holder may use the item, so no role is
     * asked; fitting oneself needs neither range nor line of sight.
     * 一次使用的服务端判定，顺序即提示的选择顺序。任何持有者都能使用，因此不问职业；给自己装配不需要距离与视线。
     */
    public static FitResult fitResult(
            boolean userActive,
            boolean targetActive,
            boolean samePlayer,
            double squaredDistance,
            boolean hasLineOfSight,
            int targetLayers
    ) {
        if (!userActive || !targetActive) {
            return FitResult.UNAVAILABLE;
        }
        if (!samePlayer && !(hasLineOfSight && squaredDistance <= FIT_RANGE_BLOCKS * FIT_RANGE_BLOCKS)) {
            return FitResult.OUT_OF_RANGE;
        }
        if (targetLayers >= MAX_LAYERS) {
            return FitResult.AT_MAX_LAYERS;
        }
        return FitResult.FIT;
    }

    public enum FitResult {
        FIT,
        UNAVAILABLE,
        OUT_OF_RANGE,
        AT_MAX_LAYERS
    }
}
