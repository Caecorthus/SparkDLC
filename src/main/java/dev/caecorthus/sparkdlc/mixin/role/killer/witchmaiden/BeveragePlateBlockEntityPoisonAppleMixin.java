package dev.caecorthus.sparkdlc.mixin.role.killer.witchmaiden;

import dev.caecorthus.sparkdlc.role.killer.witchmaiden.PoisonApplePlateAccess;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.PoisonApplePlateNbt;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.PoisonApplePlateState;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.PoisonApplePlateService;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.PoisonApplePlateTracker;
import dev.doctor4t.wathe.block_entity.BeveragePlateBlockEntity;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds private server authority and an armed-only client view without touching Wathe's native poisoner.
 * 增加服务端私密权威状态与客户端 armed 位，不写入 Wathe 的原生 poisoner。
 */
@Mixin(BeveragePlateBlockEntity.class)
public abstract class BeveragePlateBlockEntityPoisonAppleMixin implements PoisonApplePlateAccess {
    @Unique
    private @Nullable PoisonApplePlateState sparkdlc$poisonAppleState;
    @Unique
    private boolean sparkdlc$clientPoisonAppleArmed;

    @Override
    public boolean sparkdlc$isPoisonAppleArmed() {
        return sparkdlc$poisonAppleState != null || sparkdlc$clientPoisonAppleArmed;
    }

    @Override
    public boolean sparkdlc$armPoisonApple(UUID placerUuid, UUID matchUuid) {
        if (sparkdlc$isPoisonAppleArmed()) {
            return false;
        }
        sparkdlc$poisonAppleState = PoisonApplePlateState.armed(placerUuid, matchUuid);
        sparkdlc$clientPoisonAppleArmed = true;
        PoisonApplePlateTracker.track(this);
        sparkdlc$syncVisibleState();
        return true;
    }

    @Override
    public @Nullable UUID sparkdlc$recordSuccessfulTake(UUID matchUuid) {
        if (sparkdlc$poisonAppleState == null) {
            return null;
        }
        PoisonApplePlateState.TakeResult result = sparkdlc$poisonAppleState.onSuccessfulTake(matchUuid);
        sparkdlc$poisonAppleState = result.nextState();
        sparkdlc$clientPoisonAppleArmed = sparkdlc$poisonAppleState != null;
        if (sparkdlc$poisonAppleState == null) {
            PoisonApplePlateTracker.untrack(this);
        }
        sparkdlc$syncVisibleState();
        return result.poisonerUuid();
    }

    @Override
    public @Nullable UUID sparkdlc$poisonApplePlacer() {
        return sparkdlc$poisonAppleState == null ? null : sparkdlc$poisonAppleState.placerUuid();
    }

    @Override
    public void sparkdlc$clearPoisonApple() {
        if (!sparkdlc$isPoisonAppleArmed()) {
            PoisonApplePlateTracker.untrack(this);
            return;
        }
        sparkdlc$poisonAppleState = null;
        sparkdlc$clientPoisonAppleArmed = false;
        PoisonApplePlateTracker.untrack(this);
        sparkdlc$syncVisibleState();
    }

    @Override
    public void sparkdlc$clearIfMatchChanged(@Nullable UUID matchUuid) {
        if (sparkdlc$poisonAppleState != null
                && !Objects.equals(sparkdlc$poisonAppleState.matchUuid(), matchUuid)) {
            sparkdlc$clearPoisonApple();
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void sparkdlc$writePoisonApple(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registryLookup,
            CallbackInfo ci
    ) {
        PoisonApplePlateNbt.writePersistent(nbt, sparkdlc$poisonAppleState);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void sparkdlc$readPoisonApple(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registryLookup,
            CallbackInfo ci
    ) {
        PoisonApplePlateState decodedState = PoisonApplePlateNbt.readPersistent(nbt);
        boolean rejectedStaleState = decodedState != null
                && !decodedState.belongsTo(PoisonApplePlateService.activeMatchId());
        sparkdlc$poisonAppleState = rejectedStaleState ? null : decodedState;
        sparkdlc$clientPoisonAppleArmed = sparkdlc$poisonAppleState != null
                || PoisonApplePlateNbt.readArmed(nbt);
        if (rejectedStaleState) {
            ((BlockEntity) (Object) this).markDirty();
        }
        if (sparkdlc$poisonAppleState != null) {
            PoisonApplePlateTracker.track(this);
        } else {
            PoisonApplePlateTracker.untrack(this);
        }
    }

    @Inject(method = "toInitialChunkDataNbt", at = @At("RETURN"))
    private void sparkdlc$stripSensitivePoisonAppleData(
            RegistryWrapper.WrapperLookup registryLookup,
            CallbackInfoReturnable<NbtCompound> cir
    ) {
        PoisonApplePlateNbt.stripForClient(cir.getReturnValue(), sparkdlc$isPoisonAppleArmed());
    }

    @Unique
    private void sparkdlc$syncVisibleState() {
        BlockEntity plate = (BlockEntity) (Object) this;
        plate.markDirty();
        World world = plate.getWorld();
        if (world != null && !world.isClient) {
            world.updateListeners(plate.getPos(), plate.getCachedState(), plate.getCachedState(), 3);
        }
    }
}
