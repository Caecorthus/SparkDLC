package dev.caecorthus.sparkdlc.mixin.role.civilian.perfumer;

import dev.caecorthus.sparkdlc.role.civilian.perfumer.PerfumerScentComponent;
import dev.caecorthus.sparkdlc.role.civilian.perfumer.PerfumerKitRules;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.wathe.cca.PlayerMoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Zephyr Perfume halves Wathe's per-task mood drain by scaling the {@code MOOD_DRAIN} read itself, so it
 * multiplies with the traits' and the Professor sedative's {@code setMood} argument modifiers and the Bell
 * Echo drain modifier on the same read instead of replacing them. Pinned Wathe 1.5.6 bytecode: serverTick and
 * clientTick each read GETSTATIC {@code MOOD_DRAIN} exactly once. The client reads the owner-synced Zephyr flag
 * for its local drain prediction. Selectors use only Wathe types, so {@code remap = false} holds everywhere.
 * 晨风香水直接缩放 {@code MOOD_DRAIN} 的读取，使理智下降减半；因此与天赋、教授镇静剂对
 * {@code setMood} 参数的修改以及钟声回响对同一读取的修改相乘叠加，而不是覆盖。
 * 锁定 Wathe 1.5.6 字节码：serverTick 与 clientTick 各恰有一次 GETSTATIC {@code MOOD_DRAIN}。
 * 客户端依据仅同步给本人的晨风标记做本地预测。选择器只含 Wathe 类型，{@code remap = false} 在任何环境都成立。
 */
@Mixin(value = PlayerMoodComponent.class, remap = false)
public abstract class PerfumerZephyrMoodDrainMixin {
    @Shadow
    @Final
    private PlayerEntity player;

    @ModifyExpressionValue(
            method = {"serverTick", "clientTick"},
            at = @At(
                    value = "FIELD",
                    target = "Ldev/doctor4t/wathe/game/GameConstants;MOOD_DRAIN:F",
                    opcode = Opcodes.GETSTATIC
            ),
            require = 2,
            allow = 2
    )
    private float sparkdlc$halveDrainWithZephyr(float drain) {
        return PerfumerKitRules.zephyrMoodDrain(drain, PerfumerScentComponent.KEY.get(player).isZephyrActive());
    }
}
