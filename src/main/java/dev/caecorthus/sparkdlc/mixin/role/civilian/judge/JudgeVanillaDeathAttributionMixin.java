package dev.caecorthus.sparkdlc.mixin.role.civilian.judge;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.caecorthus.sparkdlc.role.civilian.judge.JudgeKillAttribution;
import dev.caecorthus.sparkdlc.role.civilian.judge.JudgeVanillaDamage;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

/** Preserve source around Wathe's forced/null VANILLA_DEATH callback; prevention occurs earlier in applyDamage.
 * 给 Wathe 强制空凶手回调传递来源；禁杀已在 applyDamage 归零前完成。 */
@Mixin(value = ServerPlayerEntity.class, priority = 500)
public abstract class JudgeVanillaDeathAttributionMixin {
    @WrapMethod(method = "onDeath")
    private void sparkdlc$scopeVanillaDeath(DamageSource source, Operation<Void> original) {
        ServerPlayerEntity victim = (ServerPlayerEntity) (Object) this;
        JudgeKillAttribution.runWith(victim.getServerWorld(), JudgeVanillaDamage.actor(victim, source),
                () -> original.call(source));
    }
}
