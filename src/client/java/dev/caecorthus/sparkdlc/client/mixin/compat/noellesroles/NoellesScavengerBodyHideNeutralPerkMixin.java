package dev.caecorthus.sparkdlc.client.mixin.compat.noellesroles;

import dev.doctor4t.wathe.client.render.entity.PlayerBodyEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Empty anchor on Wathe's {@code PlayerBodyEntityRenderer}: it only makes Mixin call
 * {@code SparkDlcClientMixinPlugin.postApply} for that class, which runs {@code NoellesScavengerBodyHideRewrite}
 * on NoellesRoles' merged Scavenger body-hide handler (contract D3; see that class for why an injector cannot reach
 * the handler). It adds nothing to the class and is always applied: NoellesRoles is a required dependency.
 * Wathe {@code PlayerBodyEntityRenderer} 上的空锚点：只为让 Mixin 对该类调用 {@code SparkDlcClientMixinPlugin.postApply}，
 * 由其对 NoellesRoles 合并后的清道夫尸体隐藏处理器执行 {@code NoellesScavengerBodyHideRewrite}（契约 D3；为何注入器无法
 * 触及该处理器见该类说明）。它不向类添加任何内容，且始终应用：NoellesRoles 是必需依赖。
 */
@Mixin(value = PlayerBodyEntityRenderer.class, remap = false)
public abstract class NoellesScavengerBodyHideNeutralPerkMixin {
}
