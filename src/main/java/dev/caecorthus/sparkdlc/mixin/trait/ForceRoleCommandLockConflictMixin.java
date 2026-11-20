package dev.caecorthus.sparkdlc.mixin.trait;

import dev.caecorthus.sparkdlc.command.WatheForcedRoles;
import dev.caecorthus.sparkdlc.command.trait.TraitLockValidationService;
import dev.caecorthus.sparkdlc.skill.WitchSkillLockValidationService;
import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.ScoreboardRoleSelectorComponent;
import dev.doctor4t.wathe.command.ForceRoleCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Skips {@code /wathe:forcerole} targets whose next-round locks conflict with the forced role: pending trait locks
 * (the trait handler) and a forced witch skill (the skill handler).
 * 跳过下局锁定与强制身份冲突的 {@code /wathe:forcerole} 目标：已锁定的待生效天赋（天赋处理器）与已锁定的魔女技能（技能处理器）。
 *
 * <p>Two mixins used to do this, the trait one at the default priority 1000 and the skill one at 1500. Both inject at
 * the HEAD of {@code execute}, and no other mixin (of this mod, Wathe or NoellesRoles) targets
 * {@code ForceRoleCommand}, so the class priority only ever ordered the two against each other. Inside one class the
 * method order keeps that order: the trait {@code @Inject} is applied first and its callback runs first with the
 * original target list; the skill {@code @ModifyVariable} is inserted after it, directly in front of Wathe's code.
 * Do not reorder the two methods. Because the trait handler cancels whenever the role name is forceable and the skill
 * handler acts only then, the skill filter never takes effect while the trait handler is present (it did not before
 * the merge either; whether it should is left to the owner).
 * 原先由两个 mixin 完成：天赋的在默认优先级 1000，技能的在 1500。两者都注入 {@code execute} 的 HEAD，且没有其他 mixin（本模组、
 * Wathe 或 NoellesRoles）以 {@code ForceRoleCommand} 为目标，所以类优先级只决定这两者之间的先后。合并为一个类后由方法顺序保持该
 * 顺序：天赋的 {@code @Inject} 先应用，其回调先运行并拿到原始目标列表；技能的 {@code @ModifyVariable} 插在它之后、紧挨 Wathe
 * 原代码之前。不要调换这两个方法的顺序。由于身份名可锁定时天赋处理器总会取消，而技能处理器只在此时才起作用，只要天赋处理器存在，
 * 技能过滤就从不生效（合并前也是如此；是否应当生效留给所有者决定）。
 */
@Mixin(value = ForceRoleCommand.class, remap = false)
public abstract class ForceRoleCommandLockConflictMixin {
    /**
     * Blocks forced roles that would conflict with already locked traits.
     * 阻止与已锁定天赋冲突的 Wathe 强制身份。
     */
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, remap = false)
    private static void sparkdlc$validatePendingTraitLocks(
            ServerCommandSource source,
            Collection<ServerPlayerEntity> targetPlayers,
            String roleName,
            CallbackInfoReturnable<Integer> cir
    ) {
        Role role = WatheForcedRoles.findForceableRole(roleName);
        if (role == null) {
            return;
        }

        cir.setReturnValue(Wathe.executeSupporterCommand(source, () -> {
            ScoreboardRoleSelectorComponent component = ScoreboardRoleSelectorComponent.KEY.get(source.getServer().getScoreboard());
            Text roleText = Text.literal(role.identifier().getPath()).withColor(role.color());
            for (ServerPlayerEntity targetPlayer : targetPlayers) {
                TraitLockValidationService.RoleConflict conflict =
                        TraitLockValidationService.findPendingTraitAudienceConflict(targetPlayer, role);
                if (conflict != null) {
                    source.sendFeedback(() -> Text.literal("Skipped " + targetPlayer.getGameProfile().getName()
                            + ": " + TraitLockValidationService.forceRoleConflictMessage(conflict)), false);
                    continue;
                }

                // Keep Wathe's forced-role storage semantics while adding the trait validation.
                // 保留 Wathe 的锁定身份存储语义，只在写入前加入天赋校验。
                component.addForcedRole(role, targetPlayer.getUuid());
                source.sendFeedback(() -> Text.translatable("commands.wathe.forcerole.success", roleText, targetPlayer.getDisplayName()), true);
            }
        }));
    }

    /**
     * Filters forced roles that conflict with already locked skill-role abilities.
     * 过滤与已锁定技能职业能力冲突的 Wathe 强制身份目标。
     */
    @ModifyVariable(method = "execute", at = @At("HEAD"), argsOnly = true, index = 1, remap = false)
    private static Collection<ServerPlayerEntity> sparkdlc$filterForceRoleTargets(
            Collection<ServerPlayerEntity> targetPlayers,
            ServerCommandSource source,
            Collection<ServerPlayerEntity> originalTargetPlayers,
            String roleName
    ) {
        Role role = WatheForcedRoles.findForceableRole(roleName);
        if (role == null || targetPlayers.isEmpty()) {
            return targetPlayers;
        }

        List<ServerPlayerEntity> allowedTargets = new ArrayList<>();
        for (ServerPlayerEntity targetPlayer : targetPlayers) {
            WitchSkillLockValidationService.RoleConflict conflict =
                    WitchSkillLockValidationService.findForcedSkillRoleConflict(targetPlayer, role);
            if (conflict == null) {
                allowedTargets.add(targetPlayer);
                continue;
            }

            source.sendFeedback(() -> Text.literal("Skipped " + targetPlayer.getGameProfile().getName()
                    + ": " + WitchSkillLockValidationService.forceAbilityConflictMessage(conflict)), false);
        }
        return allowedTargets;
    }
}
