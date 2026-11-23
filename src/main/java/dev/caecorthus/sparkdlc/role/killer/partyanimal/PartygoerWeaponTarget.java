package dev.caecorthus.sparkdlc.role.killer.partyanimal;

import dev.caecorthus.sparkdlc.entity.PartygoerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * The Partygoer as a target of other features' weapons (owner decision 2026-10-21): the Party Animal's summoned zombie
 * ({@code sparkdlc:partygoer}) dies in one hit to anything that can kill a player. A role feature whose weapon resolves
 * its own hit (the Savior's lightsaber and pistol, the swordfish, Mighty Force, the Hunter's trap, the Time Stealer's
 * clock, a Magician puppet's replayed weapon...) asks here whether the entity it met is a Partygoer it may hit and ends
 * it through {@link #killByWeapon}. The weapons this feature hooks itself go through {@link PartygoerWeaponHits}, which
 * targets with {@link PartygoerHitGeometry}.
 * 作为其他功能武器目标的派对客（所有者 2026-10-21 决定）：派对狂召唤的僵尸（{@code sparkdlc:partygoer}）会被任何能杀死玩家的
 * 东西一击打死。自行结算命中的职业武器（救世主的光剑与手枪、剑鱼、大力、猎人的陷阱、窃时者的怀表、魔术师皮套回放的武器……）
 * 在这里询问遇到的实体是否是可命中的派对客，并通过 {@link #killByWeapon} 结束它。本功能自己挂钩的武器走
 * {@link PartygoerWeaponHits}，其选靶使用 {@link PartygoerHitGeometry}。
 */
public final class PartygoerWeaponTarget {
    private PartygoerWeaponTarget() {
    }

    /**
     * Cheap type check, any side. {@code sparkdlc:partygoer} is the only entity type built from {@link PartygoerEntity}
     * and nothing subclasses it, so the class test is the type-id test.
     * 廉价的类型判断，双端可用。{@code sparkdlc:partygoer} 是唯一由 {@link PartygoerEntity} 构建的实体类型，且它没有子类，
     * 因此按类判断即按类型 id 判断。
     */
    public static boolean isPartygoer(@Nullable Entity entity) {
        return entity instanceof PartygoerEntity;
    }

    /**
     * Server: a Partygoer a weapon may hit right now (visible, not inside a Taotie, not already dead).
     * 服务端：此刻可被武器命中的派对客（可见、不在饕餮肚里、尚未死亡）。
     *
     * <p>Not the same rule as {@link PartygoerHitGeometry#isTargetable}, and deliberately not merged with it. This one
     * always asks the server brain ({@link PartygoerService#isKillable}), so on a client, where no brain exists, it
     * answers false for every Partygoer; {@code isTargetable} answers there from the synced entity state instead (it
     * feeds client aim hints). {@code isTargetable} also reads the entity itself ({@code isAlive()}, the synced
     * {@code partyState()}), which this one leaves to the brain. On the server the two agree for every state the
     * Partygoer can reach (it takes no damage, and the brain writes the synced state), which is why other features'
     * weapons may ask either door.
     * 与 {@link PartygoerHitGeometry#isTargetable} 不是同一条规则，刻意不合并。本规则始终询问服务端大脑
     * （{@link PartygoerService#isKillable}），因此在没有大脑的客户端对任何派对客都回答 false；{@code isTargetable} 在客户端
     * 改由同步的实体状态回答（供客户端瞄准提示使用）。{@code isTargetable} 还会读取实体自身（{@code isAlive()}、同步的
     * {@code partyState()}），本规则把这些交给大脑判断。在服务端，对派对客能达到的每种状态二者结论一致（它不受伤害，
     * 同步状态也由大脑写入），所以其他功能的武器询问任一入口皆可。</p>
     */
    public static boolean isKillable(@Nullable Entity entity) {
        if (!isPartygoer(entity) || entity.isRemoved() || entity.isInvisible()) {
            return false;
        }
        return PartygoerService.isKillable(entity);
    }

    /**
     * Server: kills the Partygoer as {@code attacker}'s hit with the weapon's own death reason; the Partygoer pays the
     * reward, drops whoever it holds and leaves the corpse ({@link PartygoerService#killByWeapon}). The caller has
     * already validated its weapon (charge, cooldown, reach, aim); only the target is checked here. True iff it died.
     * 服务端：把这次命中当作 {@code attacker} 的武器击杀派对客，死因为该武器自己的死因；派对客发放奖励、放下手中的人并留下尸体
     * （{@link PartygoerService#killByWeapon}）。调用方已校验武器（蓄力、冷却、距离、瞄准）；这里只检查目标。
     * 仅当它确实死亡时返回 true。
     */
    public static boolean killByWeapon(@Nullable ServerPlayerEntity attacker, @Nullable Entity target,
                                       @Nullable Identifier deathReason) {
        if (!isKillable(target)) {
            return false;
        }
        return PartygoerService.killByWeapon(attacker, target, deathReason);
    }
}
