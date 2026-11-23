package dev.caecorthus.sparkdlc.mixin.role.civilian.judge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.caecorthus.sparkdlc.legacy.LegacyDataNames;
import dev.caecorthus.sparkdlc.role.civilian.judge.JudgeKillAttribution;
import dev.caecorthus.sparkdlc.role.civilian.judge.JudgeVoodooCause;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.voodoo.VoodooPlayerComponent;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/** The existing pending countdown owns the saved UUID and reset/NBT lifecycle. / 保存责任 UUID 随现有倒计时重置与持久化。 */
@Mixin(VoodooPlayerComponent.class)
public abstract class JudgeVoodooPendingMixin implements JudgeVoodooCause, AutoSyncedComponent {
    /**
     * Renamed from its former-mod name on 2026-11-20 (owner: "就数据可以改"); data saved under the old name is still
     * read (current name first) and written back under this one (legacy.LegacyDataNames).
     * 2026-11-20 起不再使用旧模组名（所有者：“就数据可以改”）；旧名字下保存的数据仍可读取（先读当前名字），并以本名字写回
     * （legacy.LegacyDataNames）。
     */
    @Unique private static final String sparkdlc$ACTOR_KEY = "sparkdlc:judge_voodoo_actor";
    @Unique private UUID sparkdlc$pendingVoodooActor;

    @Override
    public void sparkdlc$setPendingVoodooActor(UUID actor) {
        sparkdlc$pendingVoodooActor = actor;
    }

    @Inject(method = {"reset", "startPendingDeath"}, at = @At("HEAD"))
    private void sparkdlc$clearVoodooCause(CallbackInfo ci) {
        sparkdlc$pendingVoodooActor = null;
    }

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
            target = "Ldev/doctor4t/wathe/game/GameFunctions;killPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;ZLnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/Identifier;)V"))
    private void sparkdlc$scopeVoodoo(ServerPlayerEntity victim, boolean body, ServerPlayerEntity killer,
                                       Identifier reason, Operation<Void> original) {
        UUID actor = sparkdlc$pendingVoodooActor;
        sparkdlc$pendingVoodooActor = null;
        JudgeKillAttribution.runWith(victim.getServerWorld(), actor,
                () -> original.call(victim, body, killer, reason));
    }

    /** The default CCA transport serializes NBT; never reveal the hidden curse owner to its victim.
     * CCA 默认传输会序列化 NBT，不得把隐藏的诅咒施加者同步给受害者。 */
    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        NbtCompound publicState = new NbtCompound();
        ((VoodooPlayerComponent) (Object) this).writeToNbt(publicState, buf.getRegistryManager());
        publicState.remove(sparkdlc$ACTOR_KEY);
        buf.writeNbt(publicState);
    }

    @Inject(method = "writeToNbt", at = @At("TAIL"))
    private void sparkdlc$writeVoodooCause(NbtCompound tag, RegistryWrapper.WrapperLookup registries, CallbackInfo ci) {
        if (sparkdlc$pendingVoodooActor != null) tag.putUuid(sparkdlc$ACTOR_KEY, sparkdlc$pendingVoodooActor);
        else tag.remove(sparkdlc$ACTOR_KEY);
    }

    @Inject(method = "readFromNbt", at = @At("TAIL"))
    private void sparkdlc$readVoodooCause(NbtCompound tag, RegistryWrapper.WrapperLookup registries, CallbackInfo ci) {
        String actorKey = LegacyDataNames.keyIn(tag, sparkdlc$ACTOR_KEY);
        sparkdlc$pendingVoodooActor = tag.containsUuid(actorKey) ? tag.getUuid(actorKey) : null;
    }
}
