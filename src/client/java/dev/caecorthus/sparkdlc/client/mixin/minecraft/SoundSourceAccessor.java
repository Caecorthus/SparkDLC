package dev.caecorthus.sparkdlc.client.mixin.minecraft;

import net.minecraft.client.sound.Source;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Read-only access to a vanilla sound {@link Source}'s OpenAL source name. {@code DoubleBarrelShotAudio} switches on
 * {@code AL_SOURCE_SPATIALIZE_SOFT} for the double-barrel shot with it, and {@code BearHearingAudio} raises
 * {@code AL_MAX_GAIN} for a scaled sound. Only read on the sound executor thread, inside the setup consumer vanilla
 * already runs there; vanilla generates a fresh AL source per sound and deletes it on release, so a property set
 * through it never carries over to another sound. One accessor for both roles: it names neither, so sharing it couples
 * neither role to the other (each used to keep its own copy for that reason).
 * 只读访问原版声源 {@link Source} 的 OpenAL 声源名。{@code DoubleBarrelShotAudio} 借此为双管猎枪枪声开启
 * {@code AL_SOURCE_SPATIALIZE_SOFT}，{@code BearHearingAudio} 借此为受缩放声音提高 {@code AL_MAX_GAIN}。只在声音执行线程上、
 * 原版本就在该线程运行的设置回调内读取；原版为每个声音新建 AL 声源并在释放时删除，因此通过它设置的属性不会延续到其他声音。
 * 两个职业共用这一个访问器：它不提及任何职业，共用不会让两个职业互相耦合（此前正是为此各留一份）。
 */
@Mixin(Source.class)
public interface SoundSourceAccessor {
    @Accessor("pointer")
    int sparkdlc$getSourcePointer();
}
