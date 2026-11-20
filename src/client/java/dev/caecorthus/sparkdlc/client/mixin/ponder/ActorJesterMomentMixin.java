package dev.caecorthus.sparkdlc.client.mixin.ponder;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.caecorthus.sparkdlc.client.ponder.ActorPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * During a Jester Moment (NoellesRoles) every other player is drawn with the Jester's bat, raised arms and cape; not
 * while a demo actor is being drawn (NoellesRoles is a required dependency, so the target always exists). The check
 * runs several times per player
 * drawn and only reads state, so the answer is changed on return rather than through a cancellable callback allocated
 * per call.
 * 小丑时刻（NoellesRoles）期间，其他所有玩家都画成拿着小丑的球棒、举起双臂并披着小丑的披风；绘制演示演员时不这样画。
 * NoellesRoles 是必需依赖，目标类总是存在。
 * 每绘制一名玩家都会多次调用该判断，且它只读取状态，因此在返回时修改结果，而不是通过
 * 每次调用都要分配的可取消回调。
 */
@Mixin(targets = "org.agmas.noellesroles.client.jester.JesterMomentClient", remap = false)
public abstract class ActorJesterMomentMixin {
    @ModifyReturnValue(method = "isActiveForLocalViewer", at = @At("RETURN"), require = 0)
    private static boolean sparkdlc$notForActors(boolean active) {
        return active && !ActorPlayer.isDrawing();
    }
}
