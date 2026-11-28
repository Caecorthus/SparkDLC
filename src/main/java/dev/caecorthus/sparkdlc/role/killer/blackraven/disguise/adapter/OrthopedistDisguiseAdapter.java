package dev.caecorthus.sparkdlc.role.killer.blackraven.disguise.adapter;

import dev.caecorthus.sparkdlc.role.civilian.orthopedist.ExoskeletonRules;
import dev.caecorthus.sparkdlc.role.civilian.orthopedist.OrthopedistPlayerComponent;
import dev.caecorthus.sparkdlc.role.civilian.orthopedist.OrthopedistRules;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Disguise parity surface of the Orthopedist: the skill, recipient sync, HUD, outline, key dispatch and the shop read
 * isRole. The component cooldown keeps ticking across switches; only the first entry sets it, aligned to the round
 * clock. The role's economy (owner 2026-11-28) reads the raw role, so money visibility, the Exoskeleton shop entry
 * and the task income are declared here, like the Tarot Reader's.
 * 骨科医生的伪装对等面：技能、接收方同步、HUD、描边、按键分发与商店均读取 isRole。组件冷却跨切换持续计时；
 * 仅首次进入时按本局时钟对齐设置。该职业的经济（所有者 2026-11-28）读取真实身份，因此金钱可见性、外骨骼商店条目与
 * 任务收入像塔罗师一样在此声明。
 */
public final class OrthopedistDisguiseAdapter implements BlackRavenDisguiseAdapter {
    public static final Identifier ROLE_ID = Identifier.of("sparkdlc", "orthopedist");
    /** Built by {@code OrthopedistShopService}, whose gate the overlay widens. / 由 OrthopedistShopService 构建，其门控被覆盖层放宽。 */
    private static final DisguiseShopSpec SHOP = DisguiseShopSpec.of(
            DisguiseShopSpec.Entry.nativeOnly(ExoskeletonRules.SHOP_ENTRY_ID)
    );

    @Override
    public Identifier roleId() {
        return ROLE_ID;
    }

    @Override
    public void onFirstEntry(ServerPlayerEntity player, FirstEntry entry) {
        OrthopedistPlayerComponent.KEY.get(player).setCooldownTicks(entry.aligned(OrthopedistRules.INITIAL_COOLDOWN_TICKS));
    }

    /**
     * The recipient gate now passes, so resend every player's public Bone Setting bit (as OrthopedistSkillService does
     * on assignment).
     * 接收门控现已放行，按 OrthopedistSkillService 分配时的做法补发所有玩家的公开正骨标记。
     */
    @Override
    public void onEnter(ServerPlayerEntity player, boolean firstEntry) {
        for (ServerPlayerEntity target : player.getServerWorld().getPlayers()) {
            OrthopedistPlayerComponent.KEY.sync(target);
        }
    }

    @Override
    public DisguiseShopSpec shopSpec() {
        return SHOP;
    }

    @Override
    public boolean moneyVisible() {
        return true;
    }

    @Override
    public DisguiseTaskReward taskReward() {
        return DisguiseTaskReward.OWN_ROLE;
    }
}
