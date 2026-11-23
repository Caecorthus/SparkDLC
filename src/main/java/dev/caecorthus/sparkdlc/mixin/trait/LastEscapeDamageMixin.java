package dev.caecorthus.sparkdlc.mixin.trait;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.caecorthus.sparkdlc.trait.killer.escape.LastEscapeService;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ServerPlayerEntity.class, priority = 2000)
public abstract class LastEscapeDamageMixin {
    @WrapMethod(method = "damage")
    private boolean sparkdlc$phaseImmunity(DamageSource source, float amount, Operation<Boolean> original) {
        return !LastEscapeService.isActive((ServerPlayerEntity) (Object) this) && original.call(source, amount);
    }
}
