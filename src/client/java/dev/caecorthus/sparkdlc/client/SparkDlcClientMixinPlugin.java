package dev.caecorthus.sparkdlc.client;

import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesScavengerBodyHideRewrite;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * The one plugin of {@code sparkdlc.client.mixins.json}. Every client mixin applies as configured, with two
 * exceptions:
 *
 * <ul>
 * <li><b>Sodium.</b> Sodium (mod id {@code sodium}, shipped by the modpack) {@code @Overwrite}s
 * {@code WorldRenderer.setupTerrain}. Mixin rejects any injector into an overwritten method while applying, and
 * {@code require = 0} does not prevent that, so with Sodium installed the game crashed on launch. The three mixins
 * that only inject into {@code setupTerrain} ({@link #SODIUM_SKIPPED_MIXINS}: the remote viewpoint of drone piloting
 * and Wraith possession, the Seeker's remote devices, the Black Raven crow view) are therefore not applied when
 * Sodium is loaded; Sodium's renderer already centres terrain on the camera, so far terrain still draws.</li>
 * <li><b>The Scavenger anchor.</b> {@code NoellesScavengerBodyHideNeutralPerkMixin} is an empty anchor on Wathe's
 * {@code PlayerBodyEntityRenderer}: it is skipped without NoellesRoles, and after all mixins were applied to that
 * class its {@code postApply} rewrites NoellesRoles' merged Scavenger body-hide handler
 * ({@link NoellesScavengerBodyHideRewrite}).</li>
 * </ul>
 *
 * <p>Mixin classes are named by string here because loading one directly is illegal; this class lives outside the
 * mixin package for the same reason (Mixin refuses to load an ordinary class from a config's package). It replaces
 * three plugins that each named mixins of one former config; the decisions are the same for the same mixins.</p>
 *
 * {@code sparkdlc.client.mixins.json} 唯一的插件。所有客户端 mixin 照常应用，只有两处例外：
 *
 * <ul>
 * <li><b>Sodium。</b>Sodium（模组 id {@code sodium}，整合包自带）以 {@code @Overwrite} 替换
 * {@code WorldRenderer.setupTerrain}。Mixin 在应用阶段拒绝向被覆盖的方法注入，{@code require = 0} 也无法避免，因此安装 Sodium 时
 * 游戏启动即崩溃。故 Sodium 存在时不应用只向 {@code setupTerrain} 注入的三个 mixin（{@link #SODIUM_SKIPPED_MIXINS}：
 * 无人机驾驶与附身冤魂的远程视角、搜寻者的遥控设备、黑羽鸦乌鸦视角）；Sodium 的渲染器本就以镜头为中心渲染地形，远处地形仍会绘制。</li>
 * <li><b>清道夫锚点。</b>{@code NoellesScavengerBodyHideNeutralPerkMixin} 是 Wathe {@code PlayerBodyEntityRenderer} 上的空锚点：
 * 未安装 NoellesRoles 时跳过它；在该类应用完所有 mixin 之后，于其 {@code postApply} 改写 NoellesRoles 合并后的清道夫尸体隐藏处理器
 * （{@link NoellesScavengerBodyHideRewrite}）。</li>
 * </ul>
 *
 * <p>这里以字符串指代 mixin 类，因为直接加载 mixin 类是非法的；出于同样原因本类位于 mixin 包之外（Mixin 拒绝从配置的包里加载
 * 普通类）。它取代了三个各自点名某个旧配置中 mixin 的插件；对同一个 mixin 的判定保持不变。</p>
 */
public final class SparkDlcClientMixinPlugin implements IMixinConfigPlugin {
    public static final String SCAVENGER_BODY_HIDE_ANCHOR =
            "dev.caecorthus.sparkdlc.client.mixin.compat.noellesroles.NoellesScavengerBodyHideNeutralPerkMixin";
    static final String SODIUM_MOD_ID = "sodium";
    /** Camera-centred render grid of drone piloting and Wraith possession. / 无人机驾驶与附身冤魂的以镜头为中心的渲染网格。 */
    static final String REMOTE_VIEWPOINT_TERRAIN_GRID_MIXIN =
            "dev.caecorthus.sparkdlc.client.mixin.viewpoint.RemoteViewpointTerrainGridMixin";
    /** The Seeker's remote devices. / 搜寻者的遥控设备。 */
    static final String TERRAIN_GRID_MIXIN =
            "dev.caecorthus.sparkdlc.client.mixin.role.civilian.seeker.SeekerRemoteTerrainGridMixin";
    /** The Black Raven crow view's grid hook, skipped under Sodium for the same reason. / 黑羽鸦乌鸦视角的网格钩子，同理跳过。 */
    static final String CROW_TERRAIN_GRID_MIXIN =
            "dev.caecorthus.sparkdlc.client.mixin.role.killer.blackraven.crow.BlackRavenCrowTerrainGridMixin";
    /** {@code WorldRenderer.setupTerrain} injectors (camera-centred render grid). / setupTerrain 注入（以镜头为中心的渲染网格）。 */
    static final Set<String> SODIUM_SKIPPED_MIXINS = Set.of(
            REMOTE_VIEWPOINT_TERRAIN_GRID_MIXIN, TERRAIN_GRID_MIXIN, CROW_TERRAIN_GRID_MIXIN);
    private static final String NOELLES_ROLES = "noellesroles";
    /** Logger name kept from before the fusion so existing log filters still match. / 保留融合前的日志名，已有的日志过滤规则仍然有效。 */
    private static final Logger LOGGER = LoggerFactory.getLogger("SparkDLC/ClientMixinPlugin");

    private boolean sodiumLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        sodiumLoaded = FabricLoader.getInstance().isModLoaded(SODIUM_MOD_ID);
    }

    /** The config uses no refmap. / 该配置不使用 refmap。 */
    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // NoellesRoles is only looked up for the anchor itself, as before. / 与此前一样，只在判定锚点本身时才查询 NoellesRoles。
        return applies(mixinClassName, sodiumLoaded,
                !SCAVENGER_BODY_HIDE_ANCHOR.equals(mixinClassName)
                        || FabricLoader.getInstance().isModLoaded(NOELLES_ROLES));
    }

    /** Pure decision, unit-tested without a loader. / 纯判定，可脱离加载器做单元测试。 */
    static boolean applies(String mixinClassName, boolean sodiumLoaded, boolean noellesRolesLoaded) {
        if (SCAVENGER_BODY_HIDE_ANCHOR.equals(mixinClassName)) {
            return noellesRolesLoaded;
        }
        return !(sodiumLoaded && SODIUM_SKIPPED_MIXINS.contains(mixinClassName));
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    /** No extra mixins. / 不追加 mixin。 */
    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (!SCAVENGER_BODY_HIDE_ANCHOR.equals(mixinClassName)) {
            return;
        }
        // Throws on an unexpected NoellesRoles shape, failing like a stale require = 1 selector.
        // NoellesRoles 形状不符时抛出异常，与过期的 require = 1 选择器一样失败。
        int rewritten = NoellesScavengerBodyHideRewrite.rewrite(targetClass);
        LOGGER.info("Kept the NoellesRoles Scavenger hidden-body view for neutral-aligned native factions "
                + "({} faction reads in {}). 已为中立同盟原生阵营保留清道夫隐藏尸体可见性。", rewritten, targetClassName);
    }
}
