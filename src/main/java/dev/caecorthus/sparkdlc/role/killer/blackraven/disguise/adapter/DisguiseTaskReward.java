package dev.caecorthus.sparkdlc.role.killer.blackraven.disguise.adapter;

/**
 * Which real payer a disguise's task income mirrors; the amount rule lives in BlackRavenDisguiseEconomy.
 * 伪装任务收入所镜像的真实付款方；金额规则位于 BlackRavenDisguiseEconomy。
 */
public enum DisguiseTaskReward {
    /**
     * NoellesRoles +50, skipped when the Conscience/Impostor trait already pays. / 诺艾尔职业 +50；良知/内鬼词条已支付时跳过。
     */
    NOELLES_NATIVE,
    /**
     * {@code RoleEconomyService} good-role +50, skipped like {@link #NOELLES_NATIVE} when the Conscience/Impostor trait
     * already pays. / 好人职业 +50；与 {@link #NOELLES_NATIVE} 相同，良知/内鬼词条已支付时跳过。
     */
    ROLE_ECONOMY_GOOD_ROLE,
    /** The mod's own civilian roles' economy +50, unconditional. / 本模组自有平民职业经济 +50，无条件。 */
    OWN_ROLE,
    NONE
}
