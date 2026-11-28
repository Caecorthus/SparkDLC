package dev.caecorthus.sparkdlc.role.civilian.orthopedist;

import dev.caecorthus.sparkdlc.SparkDlcRoles;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

/**
 * Independent Orthopedist state: owner-private cooldown plus viewer-safe Bone Setting visibility. Every player has
 * this component, so it also holds the player's Exoskeleton layers (the wearer can be anyone). The layers are
 * server-only: they are saved but never synced, and reach the client only as the tracked stamina modifier
 * ({@link ExoskeletonService#STAMINA_MODIFIER_ID}).
 * 骨科大夫的独立状态：本人私有冷却，以及可安全同步给骨科大夫观察者的正骨标记。每名玩家都有本组件，因此它也保存该玩家的
 * 外骨骼层数（穿戴者可以是任何人）。层数仅存在于服务端：会存档但从不同步，客户端只能通过被追踪的体力修饰符
 * （{@link ExoskeletonService#STAMINA_MODIFIER_ID}）间接得知。
 */
public final class OrthopedistPlayerComponent
        implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<OrthopedistPlayerComponent> KEY = ComponentRegistry.getOrCreate(
            OrthopedistRules.ROLE_ID,
            OrthopedistPlayerComponent.class
    );

    private final PlayerEntity player;
    private int cooldownTicks;
    private boolean boneSettingActive;
    private int exoskeletonLayers;
    /**
     * Layer count the stamina modifier was last written for; -1 = not written yet (a new entity, or loaded data),
     * so the next server tick writes it.
     * 上一次写入体力修饰符时对应的层数；-1 表示尚未写入（新实体或刚读档），下一个服务端刻会写入。
     */
    private int appliedExoskeletonLayers = -1;

    public OrthopedistPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    /**
     * Exoskeleton layers on this player. Server-authoritative; always 0 on a client.
     * 该玩家身上的外骨骼层数。以服务端为准；客户端恒为 0。
     */
    public int getExoskeletonLayers() {
        return exoskeletonLayers;
    }

    /**
     * Server only: fits one more layer and updates the stamina modifier at once. False at the cap, nothing changed.
     * 仅服务端：再装一层并立即更新体力修饰符。已达上限时返回 false，不做任何改动。
     */
    public boolean addExoskeletonLayer() {
        if (player.getWorld().isClient || exoskeletonLayers >= ExoskeletonRules.MAX_LAYERS) {
            return false;
        }
        exoskeletonLayers++;
        writeExoskeletonModifier();
        return true;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public void setCooldownTicks(int ticks) {
        cooldownTicks = Math.max(0, ticks);
        sync();
    }

    public boolean hasBoneSettingActive() {
        return boneSettingActive;
    }

    public void assignForRole(Role role) {
        setCooldownTicks(isOrthopedist(role) ? OrthopedistRules.INITIAL_COOLDOWN_TICKS : 0);
    }

    public void refreshBoneSettingState() {
        updateBoneSettingState(player.hasStatusEffect(OrthopedistEffects.boneSetting()));
    }

    /**
     * Death, reset and round end, for every player whatever the role: also takes the Exoskeleton layers off, which
     * last exactly as long as this per-round state.
     * 死亡、重置与对局结束时对每名玩家执行，与职业无关：同时卸下外骨骼层数，其持续时间与这份每局状态完全一致。
     */
    public void clear() {
        clear(false);
    }

    /**
     * {@link #clear} for a death the Last Stand trait intercepted: the survivor is not dead, so the Exoskeleton layers
     * stay on; the cooldown and Bone Setting are cleared as before.
     * 被背水一战词条拦截的死亡所用的 {@link #clear}：存活者并未死亡，外骨骼层数保留；冷却与正骨照旧清除。
     */
    public void clearKeepingExoskeleton() {
        clear(true);
    }

    private void clear(boolean keepExoskeleton) {
        cooldownTicks = 0;
        boneSettingActive = false;
        player.removeStatusEffect(OrthopedistEffects.boneSetting());
        if (!keepExoskeleton && !player.getWorld().isClient) {
            exoskeletonLayers = 0;
            writeExoskeletonModifier();
        }
        sync();
    }

    /**
     * Client-only: forgets observer data synced while the local player acted as an Orthopedist. No sync and no
     * status-effect change; the server copy is untouched and a later sync overwrites this again.
     * 仅客户端：清除本地玩家扮演骨科大夫期间同步来的观察数据。不触发同步、不改动状态效果；服务端数据不受影响，后续同步会再次覆盖。
     */
    public void clearObserverViewOnClient() {
        if (!player.getWorld().isClient) {
            return;
        }
        cooldownTicks = 0;
        boneSettingActive = false;
    }

    @Override
    public void serverTick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            if ((cooldownTicks == 0 || cooldownTicks % 20 == 0) && player instanceof ServerPlayerEntity owner
                    && owner.networkHandler != null) {
                // Observers only read the Bone Setting bit, which every write syncs to all (and every new observer gets a
                // full resend); the countdown checkpoint goes to the owner alone.
                // 观察者只读取正骨标记，而每次写入都会同步给所有人（新观察者也会收到补发）；倒计时校准只发给本人。
                KEY.syncWith(owner, (ComponentProvider) owner);
            }
        }
        refreshBoneSettingState();
        tickExoskeleton();
    }

    /**
     * Drops layers that no longer belong to a running round, then rewrites the stamina modifier when it is out of
     * step (after a load, where the temporary modifier is gone but the saved layers are back).
     * 先丢弃已不属于进行中对局的层数，再在体力修饰符与层数不一致时重写（读档后临时修饰符已消失，而存档的层数已恢复）。
     */
    private void tickExoskeleton() {
        if (exoskeletonLayers > 0 && !ExoskeletonService.keepsLayers(player)) {
            exoskeletonLayers = 0;
        }
        if (appliedExoskeletonLayers != exoskeletonLayers) {
            writeExoskeletonModifier();
        }
    }

    private void writeExoskeletonModifier() {
        ExoskeletonService.writeStaminaModifier(player, exoskeletonLayers);
        appliedExoskeletonLayers = exoskeletonLayers;
    }

    @Override
    public void clientTick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity recipient) {
        if (recipient == player) {
            return true;
        }
        // Widened by the Black Raven acting overlay; getRole stays raw. / 黑羽鸦扮演覆盖层会放宽此判定；getRole 仍为真实身份。
        return GameWorldComponent.KEY.get(recipient.getWorld()).isRole(recipient, SparkDlcRoles.orthopedist());
    }

    /**
     * Only the component owner receives cooldown data; Orthopedist observers receive the public effect bit only.
     * 只有组件本人会收到冷却；其他骨科大夫观察者仅收到公开的正骨标记。
     */
    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        boolean includeCooldown = recipient == player;
        buf.writeBoolean(includeCooldown);
        if (includeCooldown) {
            buf.writeVarInt(cooldownTicks);
        }
        buf.writeBoolean(boneSettingActive);
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        boolean includesCooldown = buf.readBoolean();
        cooldownTicks = includesCooldown ? Math.max(0, buf.readVarInt()) : 0;
        boneSettingActive = buf.readBoolean();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (cooldownTicks > 0) {
            tag.putInt("CooldownTicks", cooldownTicks);
        }
        if (exoskeletonLayers > 0) {
            tag.putInt("ExoskeletonLayers", exoskeletonLayers);
        }
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        cooldownTicks = tag.contains("CooldownTicks", NbtElement.NUMBER_TYPE)
                ? Math.max(0, tag.getInt("CooldownTicks"))
                : 0;
        boneSettingActive = false;
        // Absent in data saved before the Exoskeleton existed: no layers. / 外骨骼出现之前的存档没有此键：视为 0 层。
        exoskeletonLayers = tag.contains("ExoskeletonLayers", NbtElement.NUMBER_TYPE)
                ? ExoskeletonRules.clampLayers(tag.getInt("ExoskeletonLayers"))
                : 0;
        appliedExoskeletonLayers = -1;
    }

    private void updateBoneSettingState(boolean active) {
        if (active == boneSettingActive) {
            return;
        }
        boneSettingActive = active;
        sync();
    }

    private void sync() {
        KEY.sync(player);
    }

    private static boolean isOrthopedist(Role role) {
        return role != null && OrthopedistRules.ROLE_ID.equals(role.identifier());
    }
}
