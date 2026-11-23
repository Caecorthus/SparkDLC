package dev.caecorthus.sparkdlc.mixin.compat.noellesroles;

import org.agmas.noellesroles.taotie.TaotiePlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * External seam (pinned NoellesRoles 1.7.6): read-only access to the Taotie's private dynamic swallow cooldown
 * {@code calculatedSwallowCooldown} ({@code clamp(60 - players, 20..50)} seconds, set per round by
 * {@code initializeForGame}; the value every swallow writes). The field is a NoellesRoles member, hence
 * {@code remap = false}; the field name and {@code I} descriptor are pinned by {@code SeekerNoellesTaotieContractTest}.
 * Server only. Three readers share this one accessor (each used to declare its own): swallowing a Seeker car
 * ({@code NoellesTaotieSeekerBridge}) and swallowing a Partygoer ({@code PartygoerTaotieService}) cost exactly what a
 * player swallow costs without duplicating NoellesRoles' formula, and
 * {@code cooldown.provider.NoellesTaotieSwallowCooldownStore} uses it as the forced-cooldown nominal.
 * 外部接缝（固定 NoellesRoles 1.7.6）：只读访问饕餮私有的动态吞噬冷却 {@code calculatedSwallowCooldown}
 * （{@code initializeForGame} 每局设置，{@code clamp(60 - 人数, 20..50)} 秒；每次吞噬写入的值）。该字段属于 NoellesRoles，
 * 因此使用 {@code remap = false}；字段名与 {@code I} 描述符由 {@code SeekerNoellesTaotieContractTest} 固定。仅服务端。
 * 三个读取方共用这一个访问器（此前各自声明一个）：吞搜寻者的车（{@code NoellesTaotieSeekerBridge}）与吞派对客
 * （{@code PartygoerTaotieService}）的代价与吞人完全一致，而无需复制 NoellesRoles 的公式；
 * {@code cooldown.provider.NoellesTaotieSwallowCooldownStore} 则把它作为强制冷却的标准冷却。
 */
@Mixin(value = TaotiePlayerComponent.class, remap = false)
public interface TaotiePlayerComponentAccessor {
    @Accessor(value = "calculatedSwallowCooldown", remap = false)
    int sparkdlc$getCalculatedSwallowCooldown();
}
