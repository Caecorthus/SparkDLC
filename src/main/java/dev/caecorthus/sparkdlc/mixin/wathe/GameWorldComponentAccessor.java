package dev.caecorthus.sparkdlc.mixin.wathe;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashSet;
import java.util.UUID;

/**
 * The mod's one accessor on Wathe's {@code GameWorldComponent} (it was four, one per former mod and purpose). Names
 * carry the {@code sparkdlc$} prefix so another mod's accessor on the same class cannot collide.
 * 本 mod 在 Wathe {@code GameWorldComponent} 上唯一的访问器（此前有四个，按旧 mod 与用途各一个）。方法名带 {@code sparkdlc$}
 * 前缀，避免与其他模组在同一类上声明的访问器冲突。
 */
@Mixin(value = GameWorldComponent.class, remap = false)
public interface GameWorldComponentAccessor {
    /**
     * The world the component belongs to, which Wathe keeps private. Read-only. Conditional settlement exclusions need
     * it to turn the {@code (game, uuid)} pair every settlement point already passes into the online player their
     * condition is asked about; the traits' round hooks use it the same way.
     * 组件所属的世界（Wathe 将其设为私有），只读。带条件的结算排除需要它，把各结算点已经传入的 {@code (game, uuid)} 换成被询问
     * 条件的在线玩家；特质的回合钩子也以同样方式使用。
     */
    @Accessor("world")
    World sparkdlc$getWorld();

    /**
     * Exact Adapter for Wathe's private {@code deadPlayers}: Wathe has no "un-dead" method, and a revive must take a
     * player back out of the dead set. Writers: the Pathogen's T-Virus revive, the traits' Last Stand, the Ferryman's
     * moment (Lost Soul revival), and the admin {@code setPlayerAlive}/{@code setPlayerDead} debug commands.
     * Wathe 私有 {@code deadPlayers} 的精确 Adapter：Wathe 没有“复活”方法，复活需要把玩家移出死亡名单。写入方：病原体的
     * T病毒复活、特质的背水一战、摆渡人的亡魂时刻（迷失的灵魂复活），以及管理员调试命令 setPlayerAlive / setPlayerDead。
     */
    @Accessor("deadPlayers")
    HashSet<UUID> sparkdlc$getDeadPlayers();

    /**
     * Exact Adapter for Wathe's train-copy retry counter: positive while a copy is queued or retrying, negative once it
     * succeeded or when none was queued. The sabotage map scan waits for a pending copy instead of guessing with a
     * timeout.
     * Wathe 车厢复制重试计数的精确 Adapter：复制排队或重试中为正数，成功后或从未排队时为负数。破坏系统的地图扫描会等待进行中的
     * 复制，而不是靠超时猜测。
     */
    @Accessor("ticksUntilNextResetAttempt")
    int sparkdlc$getTicksUntilNextResetAttempt();

    /** Raw write without {@code setGameStatus}'s sync; only {@code PlanningRoundView} uses it. 不经同步的原始写入，仅供 PlanningRoundView 使用。 */
    @Accessor("gameStatus")
    void sparkdlc$setGameStatusField(GameWorldComponent.GameStatus status);
}
