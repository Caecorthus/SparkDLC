package dev.caecorthus.sparkdlc.mixin.role.special.wraith;

import dev.caecorthus.sparkdlc.legacy.LegacyDataNames;
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
    /**
     * Renamed from its former-mod name on 2026-11-20 (owner: "就数据可以改"); data saved under the old name is still
     * read (current name first) and written back under this one (legacy.LegacyDataNames).
     * 2026-11-20 起不再使用旧模组名（所有者：“就数据可以改”）；旧名字下保存的数据仍可读取（先读当前名字），并以本名字写回
     * （legacy.LegacyDataNames）。
     */
    @Unique
    private static final String sparkdlc$DEATH_ROLE_KEY = "SparkDlcDeathRole";

    @Unique
    private static final TrackedData<String> sparkdlc$DEATH_ROLE = DataTracker.registerData(
            PlayerBodyEntity.class,
            TrackedDataHandlerRegistry.STRING
    );

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void sparkdlc$trackDeathRole(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(sparkdlc$DEATH_ROLE, "");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void sparkdlc$writeDeathRole(NbtCompound nbt, CallbackInfo ci) {
        Identifier roleId = sparkdlc$getDeathRole();
        if (roleId != null) {
            nbt.putString(sparkdlc$DEATH_ROLE_KEY, roleId.toString());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void sparkdlc$readDeathRole(NbtCompound nbt, CallbackInfo ci) {
        sparkdlc$setDeathRole(Identifier.tryParse(nbt.getString(LegacyDataNames.keyIn(nbt, sparkdlc$DEATH_ROLE_KEY))));
    }

    @Override
    public void sparkdlc$setDeathRole(@Nullable Identifier roleId) {
        ((PlayerBodyEntity) (Object) this).getDataTracker().set(
                sparkdlc$DEATH_ROLE,
                roleId == null ? "" : roleId.toString()
        );
    }

    @Override
    public @Nullable Identifier sparkdlc$getDeathRole() {
        return Identifier.tryParse(((PlayerBodyEntity) (Object) this)
                .getDataTracker().get(sparkdlc$DEATH_ROLE));
    }
}
