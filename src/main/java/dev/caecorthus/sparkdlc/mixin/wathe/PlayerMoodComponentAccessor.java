package dev.caecorthus.sparkdlc.mixin.wathe;

import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Access to Wathe's pinned task cadence internals: the Wraith task runtime reads and writes them; the 规划大师 Master
 * Planner only reads {@code nextTaskTimer} (server, its own next-task HUD line) and never writes it; the trait
 * 极简主义 Minimalism only calls the private task generator to hand out a replacement task. One accessor for all of
 * them (the trait used to declare its own invoker).
 * 访问 Wathe 固定的任务生成节奏内部状态：冤魂任务运行时读写它们；规划大师只读取 {@code nextTaskTimer}（服务端，用于自身的
 * 下一任务 HUD 行），从不写入；特质“极简主义”只调用私有的任务生成器来补发任务。它们共用这一个访问器（该特质此前自带一个调用口）。
 */
@Mixin(value = PlayerMoodComponent.class, remap = false)
public interface PlayerMoodComponentAccessor {
    @Accessor("nextTaskTimer")
    int sparkdlc$getNextTaskTimer();

    @Accessor("nextTaskTimer")
    void sparkdlc$setNextTaskTimer(int ticks);

    @Invoker("generateTask")
    PlayerMoodComponent.TrainTask sparkdlc$generateTask();
}
