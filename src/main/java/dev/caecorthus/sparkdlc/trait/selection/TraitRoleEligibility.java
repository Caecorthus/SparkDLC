package dev.caecorthus.sparkdlc.trait.selection;

import dev.caecorthus.sparkdlc.trait.api.Trait;
import dev.caecorthus.sparkdlc.trait.api.TraitAudience;
import dev.doctor4t.wathe.api.Role;
import net.minecraft.util.Identifier;

import java.util.Set;

/**
 * Candidate-level role restrictions. The roles are matched by full role id because they used to live in another jar.
 * 候选天赋的身份限制：按完整身份 ID 识别这些身份，因为它们曾位于另一个 jar。
 */
public final class TraitRoleEligibility {
    private static final String MOD_ID = "sparkdlc";
    // Owner decision 2026-10-04: these roles may hold only traits that declare UNIVERSAL audience.
    // 所有者 2026-10-04 决定：这些身份只能获得声明为通用受众（UNIVERSAL）的天赋。
    private static final Set<Identifier> UNIVERSAL_ONLY_ROLE_IDS = Set.of(
            Identifier.of(MOD_ID, "grand_witch"),
            Identifier.of(MOD_ID, "accomplice"),
            // The special accomplices share every Accomplice rule. / 特殊共犯沿用共犯的全部规则。
            Identifier.of(MOD_ID, "abyss_listener"),
            Identifier.of(MOD_ID, "potion_gunner"),
            Identifier.of(MOD_ID, "riftwalker"),
            // The Bewitched (魔化使) is an accomplice before promotion. / 魔化使是晋升前的共犯。
            Identifier.of(MOD_ID, "bewitched"),
            Identifier.of(MOD_ID, "apprentice_witch"),
            Identifier.of(MOD_ID, "murderous_witch"),
            // The Fiend follows the same rule by owner decision. / 魔人按所有者决定沿用同一规则。
            Identifier.of(MOD_ID, "fiend")
    );

    // Owner request 2026-11-05: the Savior's sanity bar is pinned full, so sanity-related good traits are never
    // offered to that role. Matched by full trait id; other roles keep their pools unchanged.
    // 所有者 2026-11-05：救世主理智恒满，因此该身份永远不获得与理智相关的好人词条；按完整词条 id 匹配，
    // 其他身份的天赋池不变。
    private static final Identifier SAVIOR_ROLE_ID = Identifier.of(MOD_ID, "savior");
    private static final Set<Identifier> SAVIOR_EXCLUDED_SANITY_TRAITS = Set.of(
            Identifier.of("sparkdlc", "task_master"),
            Identifier.of("sparkdlc", "steady"),
            Identifier.of("sparkdlc", "extroverted"),
            Identifier.of("sparkdlc", "introverted"),
            Identifier.of("sparkdlc", "focus"),
            Identifier.of("sparkdlc", "depression"),
            Identifier.of("sparkdlc", "well_trained"),
            Identifier.of("sparkdlc", "oppressive")
    );

    private TraitRoleEligibility() {
    }

    /**
     * Additional role restriction only; callers must still apply their existing predicate/audience checks.
     * Reads the declared {@link Trait#audience()}, not {@code canApply}, so overridden predicates (Impostor,
     * Conscience) cannot widen it.
     * 仅附加身份限制；调用方仍须执行原有的天赋谓词或阵营校验。这里读取声明的受众而非 {@code canApply}，
     * 因此重写了谓词的天赋（内鬼、善良）无法绕过该限制。
     */
    public static boolean canReceiveTrait(Role role, Trait candidate) {
        if (role != null && SAVIOR_ROLE_ID.equals(role.identifier())
                && SAVIOR_EXCLUDED_SANITY_TRAITS.contains(candidate.id())) {
            return false;
        }
        return role == null || !UNIVERSAL_ONLY_ROLE_IDS.contains(role.identifier())
                || candidate.audience() == TraitAudience.UNIVERSAL;
    }
}
