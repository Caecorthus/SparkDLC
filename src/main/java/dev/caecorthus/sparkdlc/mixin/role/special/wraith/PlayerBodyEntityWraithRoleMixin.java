package dev.caecorthus.sparkdlc.mixin.role.special.wraith;

import dev.caecorthus.sparkdlc.role.special.wraith.conversion.WraithBodyRoleAccess;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the original corpse role as tracked and persisted data owned by this mod. */
@Mixin(PlayerBodyEntity.class)
public abstract class PlayerBodyEntityWraithRoleMixin implements WraithBodyRoleAccess {
    @Unique
    private static final TrackedData<String> SPARKWITCH_DEATH_ROLE = DataTracker.registerData(
            PlayerBodyEntity.class,
            TrackedDataHandlerRegistry.STRING
    );

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void sparkdlc$trackDeathRole(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(SPARKWITCH_DEATH_ROLE, "");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void sparkdlc$writeDeathRole(NbtCompound nbt, CallbackInfo ci) {
        Identifier roleId = sparkdlc$getDeathRole();
        if (roleId != null) {
            nbt.putString("SparkWitchDeathRole", roleId.toString());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void sparkdlc$readDeathRole(NbtCompound nbt, CallbackInfo ci) {
        sparkdlc$setDeathRole(Identifier.tryParse(nbt.getString("SparkWitchDeathRole")));
    }

    @Override
    public void sparkdlc$setDeathRole(@Nullable Identifier roleId) {
        ((PlayerBodyEntity) (Object) this).getDataTracker().set(
                SPARKWITCH_DEATH_ROLE,
                roleId == null ? "" : roleId.toString()
        );
    }

    @Override
    public @Nullable Identifier sparkdlc$getDeathRole() {
        return Identifier.tryParse(((PlayerBodyEntity) (Object) this)
                .getDataTracker().get(SPARKWITCH_DEATH_ROLE));
    }
}
