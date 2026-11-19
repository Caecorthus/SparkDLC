package dev.caecorthus.sparkdlc.client;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Plugin of {@code sparkdlc.client.mixins.json}. A mixin config can name one plugin only, so when the five legacy
 * client configs became one, the three legacy plugins were put behind this composite. It adds no rule of its own:
 * every callback is forwarded to the legacy plugins in their former config order (factionapi, strength, witch), and
 * a mixin applies only when all of them agree.
 *
 * <p>That is the same decision each legacy config used to take, because a legacy plugin answers "apply" and does
 * nothing for every mixin other than the ones it names by fully-qualified class name, and those names are mixins
 * of its own former config ({@code tools/unify/move_packages.py plan} checks this). The former assist and traits
 * client configs had no plugin; their mixins therefore still always apply.</p>
 *
 * <p>Like the legacy plugins, this class must stay outside the mixin package: Mixin refuses to load an ordinary
 * class from a config's package.</p>
 *
 * {@code sparkdlc.client.mixins.json} 的插件。一个 mixin 配置只能指定一个插件，因此五个旧客户端配置合并后，三个旧插件
 * 被放在这个组合插件之后。它自身不增加任何规则：每个回调都按旧配置顺序（factionapi、strength、witch）转发给旧插件，
 * 只有它们全部同意时 mixin 才会应用。
 *
 * <p>这与各旧配置原先的判定完全一致：旧插件只对它以全限定类名点名的 mixin 有动作，对其余 mixin 一律回答“应用”且不做
 * 任何事，而被点名的都是它自己原配置里的 mixin（{@code tools/unify/move_packages.py plan} 会检查这一点）。原 assist 与
 * traits 客户端配置没有插件，它们的 mixin 仍然总是应用。</p>
 *
 * <p>与旧插件一样，本类必须位于 mixin 包之外：Mixin 拒绝从配置的包里加载普通类。</p>
 */
public final class SparkDlcClientMixinPlugin implements IMixinConfigPlugin {
    private final List<IMixinConfigPlugin> legacyPlugins = List.of(
            new SparkFactionClientMixinPlugin(),
            new SparkStrengthClientMixinPlugin(),
            new SparkWitchClientMixinPlugin());

    @Override
    public void onLoad(String mixinPackage) {
        for (IMixinConfigPlugin plugin : legacyPlugins) {
            plugin.onLoad(mixinPackage);
        }
    }

    /** None of the legacy configs used a refmap. / 旧配置都不使用 refmap。 */
    @Override
    public String getRefMapperConfig() {
        for (IMixinConfigPlugin plugin : legacyPlugins) {
            String refMap = plugin.getRefMapperConfig();
            if (refMap != null) {
                return refMap;
            }
        }
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        for (IMixinConfigPlugin plugin : legacyPlugins) {
            if (!plugin.shouldApplyMixin(targetClassName, mixinClassName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        for (IMixinConfigPlugin plugin : legacyPlugins) {
            plugin.acceptTargets(myTargets, otherTargets);
        }
    }

    /** No legacy plugin contributes extra mixins; the lists are joined in case one ever does. / 旧插件都不追加 mixin；若将来有，则按顺序拼接。 */
    @Override
    public List<String> getMixins() {
        List<String> extra = null;
        for (IMixinConfigPlugin plugin : legacyPlugins) {
            List<String> mixins = plugin.getMixins();
            if (mixins != null) {
                if (extra == null) {
                    extra = new ArrayList<>();
                }
                extra.addAll(mixins);
            }
        }
        return extra;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        for (IMixinConfigPlugin plugin : legacyPlugins) {
            plugin.preApply(targetClassName, targetClass, mixinClassName, mixinInfo);
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        for (IMixinConfigPlugin plugin : legacyPlugins) {
            plugin.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);
        }
    }
}
