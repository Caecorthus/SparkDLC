package dev.caecorthus.sparkdlc.client.compat.iris;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.client.render.ClientFrame;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/**
 * The one place that asks Iris whether a shader pack is in use: {@code IrisApi.getInstance().isShaderPackInUse()},
 * by reflection, because Iris is a real optional mod and not a compile dependency. A pack renders the world into
 * its own G-buffers and its final pass overwrites the main target, so every effect that reads or draws on the
 * vanilla targets has to know. The features differ in what they do when Iris is installed but cannot be asked, and
 * in how often they ask, so the variants are separate, named entry points (client thread only):
 * <ul>
 *   <li>{@link #inUseOrUnknown()}: asks Iris now, on every call; a failure counts as "in use" (fail closed) and is
 *   asked again the next time. The Blind echo view (its fairness rule C13), BEAR smoke and the Specter otherworld
 *   effects: a pack switched at run time is seen at once.</li>
 *   <li>{@link #inUseOrUnknownThisFrame()}: the same answer, asked once per {@link ClientFrame#epoch()} (client tick
 *   and world render start). Undercover thermal, whose outline fallback asks for every player.</li>
 *   <li>{@link #failOpen(String)}: a failure counts as "not in use", with one warning when the API is unavailable.
 *   The Attendant flashlight, which stays on.</li>
 *   <li>{@link #failClosedForGood(String)}: fail closed, and the first failure is final for that feature: one
 *   warning, then "in use" without asking again. The scope lens and the Riftwalker grey view (HUD fallbacks).</li>
 *   <li>{@link #installed()}: only whether the Iris mod is installed, pack or not (BEAR smoke's march point).</li>
 * </ul>
 * Whether the mod is installed is read once (the mod list cannot change), and so is the reflective lookup, including
 * a failed one (a missing class or method stays missing). The call itself is made anew for every answer.
 * 询问 Iris 是否正在使用光影包的唯一位置：通过反射调用 {@code IrisApi.getInstance().isShaderPackInUse()}，因为 Iris 是
 * 真正的可选模组，不是编译依赖。光影包把世界渲染进自己的 G 缓冲，最终处理会覆盖主目标，所以每个读取或绘制原版目标的效果都
 * 需要知道。各功能在“装了 Iris 却无法询问”时的做法以及询问频率并不相同，因此各变体是分开命名的入口（仅客户端线程）：
 * <ul>
 *   <li>{@link #inUseOrUnknown()}：每次调用都立即询问；失败视为“正在使用”（失败即关闭），下次再问。盲人回声视图（公平性规则
 *   C13）、BEAR 烟雾与幽灵异界效果：运行时切换光影包会立即生效。</li>
 *   <li>{@link #inUseOrUnknownThisFrame()}：同样的答案，但每个 {@link ClientFrame#epoch()}（客户端 tick 与世界渲染开始）
 *   只询问一次。卧底热成像的描边回退会对每个玩家询问。</li>
 *   <li>{@link #failOpen(String)}：失败视为“未使用”，API 不可用时警告一次。乘务员手电筒保持开启。</li>
 *   <li>{@link #failClosedForGood(String)}：失败即关闭，且该功能的第一次失败即为定论：警告一次，此后不再询问并一直视为
 *   “正在使用”。开镜镜片与裂隙行者灰色视图（HUD 回退）。</li>
 *   <li>{@link #installed()}：只问是否安装了 Iris 模组，不论有没有光影包（BEAR 烟雾的步进时机）。</li>
 * </ul>
 * 是否安装只读取一次（模组列表不会变化），反射查找同样只做一次，失败的查找也一样（缺失的类或方法不会再出现）；
 * 而每次回答都会重新发起调用。
 */
public final class IrisShaderPacks {
    public static final String MOD_ID = "iris";
    static final String API_CLASS = "net.irisshaders.iris.api.v0.IrisApi";

    // Lambdas, so that neither the loader nor the logger is touched before Iris is first asked about.
    // 使用 lambda：在第一次询问 Iris 之前不触碰加载器与日志器。
    private static final Probe IRIS = new Probe(
            () -> FabricLoader.getInstance().isModLoaded(MOD_ID),
            IrisShaderPacks::resolveApi,
            () -> ClientFrame.epoch(),
            (message, cause) -> SparkDLC.LOGGER.warn(message, cause));

    private IrisShaderPacks() {
    }

    /** True when the Iris mod is installed, with or without a pack. / 安装了 Iris 模组时为 true，不论是否使用光影包。 */
    public static boolean installed() {
        return IRIS.installed();
    }

    /**
     * Asks now; true when a pack is in use or Iris is installed but cannot be asked (fail closed).
     * 立即询问；正在使用光影包，或装了 Iris 却无法询问时为 true（失败即关闭）。
     */
    public static boolean inUseOrUnknown() {
        return IRIS.inUseOrUnknown();
    }

    /**
     * {@link #inUseOrUnknown()}, asked once per {@link ClientFrame#epoch()}: for hooks that ask per entity.
     * 即 {@link #inUseOrUnknown()}，但每个 {@link ClientFrame#epoch()} 只询问一次：供逐实体询问的钩子使用。
     */
    public static boolean inUseOrUnknownThisFrame() {
        return IRIS.inUseOrUnknownThisFrame();
    }

    /**
     * A feature's own check that fails open: true only when Iris said so. {@code apiUnavailableWarning} is logged
     * once, the first time this check finds Iris installed without a usable API.
     * 某个功能自己的“失败即开放”检查：只有 Iris 明确回答时才为 true。该检查第一次发现装了 Iris 却没有可用 API 时，记录一次
     * {@code apiUnavailableWarning}。
     */
    public static Check failOpen(String apiUnavailableWarning) {
        return IRIS.failOpen(apiUnavailableWarning);
    }

    /**
     * A feature's own check that fails closed for good: the first failure is logged with {@code warning} and from
     * then on this check answers true without asking Iris again.
     * 某个功能自己的“失败即永久关闭”检查：第一次失败时以 {@code warning} 记录日志，此后不再询问 Iris 并一直回答 true。
     */
    public static Check failClosedForGood(String warning) {
        return IRIS.failClosedForGood(warning);
    }

    /** One feature's check, holding that feature's own failure state. / 某个功能的检查，保存该功能自己的失败状态。 */
    public interface Check {
        boolean inUse();
    }

    /**
     * Finds the Iris API; a failure here is permanent. The returned call asks Iris each time it runs.
     * 查找 Iris API；这里的失败是永久的。返回的调用每次执行都会询问 Iris。
     */
    interface Api {
        Callable<Boolean> resolve() throws Exception;
    }

    private static Callable<Boolean> resolveApi() throws ReflectiveOperationException {
        Class<?> api = Class.forName(API_CLASS);
        Method getInstance = api.getMethod("getInstance");
        Method isShaderPackInUse = api.getMethod("isShaderPackInUse");
        return () -> (Boolean) isShaderPackInUse.invoke(getInstance.invoke(null));
    }

    /**
     * The logic, with its four inputs passed in: whether Iris is installed, how its API is found, the frame clock and
     * the warning sink. Production has the one {@link #IRIS} above; the unit test builds its own.
     * 实际逻辑，四个输入由外部传入：是否安装 Iris、如何找到其 API、帧时钟以及警告输出。生产代码使用上面唯一的 {@link #IRIS}；
     * 单元测试自行构造。
     */
    static final class Probe {
        enum Reply {
            NOT_INSTALLED,
            NOT_IN_USE,
            IN_USE,
            /** The API class or one of its methods is missing: permanent. / API 类或其方法缺失：永久性的。 */
            API_UNAVAILABLE,
            /** The call threw or returned no boolean; the next call may work. / 调用抛出异常或没有返回布尔值；下次可能成功。 */
            CALL_FAILED
        }

        private final BooleanSupplier installedQuery;
        private final Api api;
        private final IntSupplier frameEpoch;
        private final BiConsumer<String, Throwable> warn;
        private Boolean installed;
        private Callable<Boolean> call;
        private Throwable apiUnavailable;
        /** Why the last {@link #ask()} failed; meaningful right after a failed reply. / 最近一次询问失败的原因。 */
        private Throwable failure;
        private boolean frameAnswered;
        private int frameAnswerEpoch;
        private boolean frameAnswer;

        Probe(BooleanSupplier installedQuery, Api api, IntSupplier frameEpoch, BiConsumer<String, Throwable> warn) {
            this.installedQuery = installedQuery;
            this.api = api;
            this.frameEpoch = frameEpoch;
            this.warn = warn;
        }

        boolean installed() {
            if (installed == null) {
                installed = installedQuery.getAsBoolean();
            }
            return installed;
        }

        Reply ask() {
            if (!installed()) {
                return Reply.NOT_INSTALLED;
            }
            if (apiUnavailable != null) {
                failure = apiUnavailable;
                return Reply.API_UNAVAILABLE;
            }
            if (call == null) {
                try {
                    call = api.resolve();
                } catch (Exception | LinkageError exception) {
                    apiUnavailable = exception;
                    failure = exception;
                    return Reply.API_UNAVAILABLE;
                }
            }
            try {
                Boolean inUse = call.call();
                if (inUse == null) {
                    failure = new IllegalStateException("IrisApi#isShaderPackInUse returned no boolean");
                    return Reply.CALL_FAILED;
                }
                return inUse ? Reply.IN_USE : Reply.NOT_IN_USE;
            } catch (Exception | LinkageError exception) {
                failure = exception;
                return Reply.CALL_FAILED;
            }
        }

        boolean inUseOrUnknown() {
            Reply reply = ask();
            return reply != Reply.NOT_INSTALLED && reply != Reply.NOT_IN_USE;
        }

        boolean inUseOrUnknownThisFrame() {
            if (!installed()) {
                return false;
            }
            int epoch = frameEpoch.getAsInt();
            if (!frameAnswered || frameAnswerEpoch != epoch) {
                frameAnswer = inUseOrUnknown();
                frameAnswerEpoch = epoch;
                frameAnswered = true;
            }
            return frameAnswer;
        }

        Check failOpen(String apiUnavailableWarning) {
            return new Check() {
                private boolean warned;

                @Override
                public boolean inUse() {
                    Reply reply = ask();
                    if (reply == Reply.API_UNAVAILABLE && !warned) {
                        warned = true;
                        warn.accept(apiUnavailableWarning, failure);
                    }
                    return reply == Reply.IN_USE;
                }
            };
        }

        Check failClosedForGood(String warning) {
            return new Check() {
                private volatile boolean broken;

                @Override
                public boolean inUse() {
                    if (broken) {
                        return true;
                    }
                    Reply reply = ask();
                    if (reply == Reply.API_UNAVAILABLE || reply == Reply.CALL_FAILED) {
                        broken = true;
                        warn.accept(warning, failure);
                        return true;
                    }
                    return reply == Reply.IN_USE;
                }
            };
        }
    }
}
