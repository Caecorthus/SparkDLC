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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * Skips {@code /wathe:forcerole} targets whose next-round locks conflict with the forced role: a pending trait lock
 * ({@code TraitLockValidationService}) or a forced witch skill that the forced role cannot have
 * ({@code WitchSkillLockValidationService}, the skill locked by {@code /sparkdlc:forceAbility}).
 * 跳过下局锁定与强制身份冲突的 {@code /wathe:forcerole} 目标：已锁定的待生效词条（{@code TraitLockValidationService}），
 * 或该身份不能拥有的已锁定魔女技能（{@code WitchSkillLockValidationService}，即 {@code /sparkdlc:forceAbility} 锁定的技能）。
 *
 * <p>The handler replaces Wathe's loop for a forceable role name and checks each target in this order: trait lock,
 * then forced skill; the first conflict skips the target with its message, others are forced as Wathe does. Owner
 * decision 2026-11-20 ("要，也检查"): {@code /sparkdlc:forceAbility} already refused a skill that conflicts with a
 * locked role, and now {@code /wathe:forcerole} refuses a role that conflicts with a locked skill. Before that the
 * skill check sat in a second handler that never ran, because this one always returned first. This is the only mixin
 * on {@code ForceRoleCommand} (of this mod, Wathe or NoellesRoles).
 * 对可锁定的身份名，此处理器代替 Wathe 的循环，按以下顺序检查每个目标：先词条锁定，再强制技能；遇到第一个冲突就带提示跳过该目标，
 * 其余目标照 Wathe 的方式锁定。所有者决定 2026-11-20（“要，也检查”）：{@code /sparkdlc:forceAbility} 原本就会拒绝与已锁定身份冲突的
 * 技能，现在 {@code /wathe:forcerole} 也会拒绝与已锁定技能冲突的身份。此前技能检查位于第二个处理器中，因为本处理器总是先返回，
 * 它从未运行过。这是 {@code ForceRoleCommand} 上唯一的 mixin（本模组、Wathe 与 NoellesRoles 中）。
 */
@Mixin(value = ForceRoleCommand.class, remap = false)
public abstract class ForceRoleCommandLockConflictMixin {
    /**
     * Blocks forced roles that would conflict with an already locked trait or witch skill.
     * 阻止与已锁定词条或魔女技能冲突的 Wathe 强制身份。
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
                WitchSkillLockValidationService.RoleConflict skillConflict =
                        WitchSkillLockValidationService.findForcedSkillRoleConflict(targetPlayer, role);
                if (skillConflict != null) {
                    source.sendFeedback(() -> Text.literal("Skipped " + targetPlayer.getGameProfile().getName()
                            + ": " + WitchSkillLockValidationService.forceAbilityConflictMessage(skillConflict)), false);
                    continue;
                }

                // Keep Wathe's forced-role storage semantics while adding the lock validation.
                // 保留 Wathe 的锁定身份存储语义，只在写入前加入锁定校验。
                component.addForcedRole(role, targetPlayer.getUuid());
                source.sendFeedback(() -> Text.translatable("commands.wathe.forcerole.success", roleText, targetPlayer.getDisplayName()), true);
            }
        }));
    }
}
