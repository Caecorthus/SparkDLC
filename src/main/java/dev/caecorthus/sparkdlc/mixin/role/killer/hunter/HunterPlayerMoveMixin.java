package dev.caecorthus.sparkdlc.mixin.role.killer.hunter;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterPlayerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;

/** Server and owning client both reject travel input during the trap's hard root. / 定身期间服务端与本人客户端都拒绝移动输入。 */
@Mixin(PlayerEntity.class)
public abstract class HunterPlayerMoveMixin {
    @WrapMethod(method = "travel")
    private void sparkdlc$blockHunterTrapTravel(Vec3d movementInput, Operation<Void> original) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        original.call(HunterPlayerComponent.KEY.get(player).isRooted() ? Vec3d.ZERO : movementInput);
    }
}
