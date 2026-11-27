package dev.caecorthus.sparkdlc.item.m16a4;

import dev.caecorthus.sparkdlc.SparkDlcSounds;
import dev.caecorthus.sparkdlc.net.M16A4ReloadC2SPayload;
import dev.caecorthus.sparkdlc.net.M16A4ShotS2CPayload;
import dev.caecorthus.sparkdlc.util.ServerDisconnectCleanup;
import dev.doctor4t.wathe.api.event.KillPlayer;
import dev.doctor4t.wathe.api.event.ResetPlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Server side of the M16A4's reload and recoil feedback. A reload is a per-player timer started by the left-click
 * intent: tactical (rounds left) is 2.2 s and refills the magazine to 31; empty is 3 s and chambers 30. Both refill the
 * burst window. The reload is dropped silently when the rifle leaves the main hand, the player dies, resets,
 * disconnects or the server stops. Each accepted round sends its shooter one {@code sparkdlc:m16a4_shot} so the client
 * can kick the view like the revolver.
 * M16A4 换弹与后坐反馈的服务端。换弹是由左键意图启动的按玩家计时：战术换弹（还有子弹）2.2 秒装满 31 发；空仓换弹 3 秒装 30
 * 发。两者都会补满点射窗口。步枪离开主手、玩家死亡、重置、断线或服务器停止时静默丢弃换弹。每发被接受的子弹给射手发一个
 * {@code sparkdlc:m16a4_shot}，让客户端像左轮一样抬起视角。
 */
public final class M16A4Service {
    private static final Map<UUID, Reload> RELOADS = new HashMap<>();
    private static boolean registered;

    private M16A4Service() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        PayloadTypeRegistry.playC2S().register(M16A4ReloadC2SPayload.ID, M16A4ReloadC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(M16A4ShotS2CPayload.ID, M16A4ShotS2CPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(M16A4ReloadC2SPayload.ID,
                (payload, context) -> startReload(context.player()));
        ServerTickEvents.END_SERVER_TICK.register(M16A4Service::tick);
        ServerDisconnectCleanup.register((player, server) -> RELOADS.remove(player.getUuid()));
        KillPlayer.AFTER.register((victim, killer, deathReason) -> RELOADS.remove(victim.getUuid()));
        ResetPlayer.EVENT.register(player -> {
            if (player != null) {
                RELOADS.remove(player.getUuid());
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> RELOADS.clear());
    }

    /** Whether this server player is in the middle of a reload. / 该服务端玩家是否正在换弹。 */
    public static boolean isReloading(PlayerEntity player) {
        if (player == null || player.getWorld().isClient) {
            return false;
        }
        return RELOADS.containsKey(player.getUuid());
    }

    /** One accepted round: tell its shooter's client to kick the view. / 一发被接受的子弹：通知射手客户端抬起视角。 */
    public static void notifyShot(ServerPlayerEntity shooter) {
        ServerPlayNetworking.send(shooter, M16A4ShotS2CPayload.INSTANCE);
    }

    /**
     * Left click: start a reload unless one runs, the main hand is not the rifle or the magazine is already full. An
     * empty magazine takes the long reload; anything else is tactical.
     * 左键：未在换弹、主手不是步枪或弹匣已满时不开始。空弹匣走长换弹，其余为战术换弹。
     */
    private static void startReload(ServerPlayerEntity player) {
        if (player == null || player.isSpectator() || RELOADS.containsKey(player.getUuid())) {
            return;
        }
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof M16A4Item)) {
            return;
        }
        int ammo = M16A4Item.getAmmo(stack);
        if (ammo >= M16A4Rules.MAGAZINE_SIZE) {
            return;
        }
        boolean empty = ammo <= 0;
        long duration = empty ? M16A4Rules.EMPTY_RELOAD_TICKS : M16A4Rules.TACTICAL_RELOAD_TICKS;
        RELOADS.put(player.getUuid(), new Reload(player.getServerWorld().getTime() + duration, empty));
        // The reload sound is the whole sequence and plays as the reload starts; the pull stays alive underneath it
        // (ending it would let a held right button re-trigger the rifle). A reload also spends the rest of the window,
        // so a held button cannot keep firing when the magazine comes back.
        // 换弹音效本身就是整段动作，在换弹开始时播放；扣扳机的使用状态在它下面保留（结束使用会让按住的右键重新触发步枪）。
        // 换弹同时清空剩余点射窗口，因此弹匣回来时按住的按钮也不会继续开火。
        dev.caecorthus.sparkdlc.SparkDLC.LOGGER.info("[m16a4] reload start empty={} ammo={}", empty, ammo);
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                empty ? SparkDlcSounds.M16A4_RELOAD_EMPTY : SparkDlcSounds.M16A4_RELOAD_TACTICAL,
                SoundCategory.PLAYERS, 1.0F, 1.0F);
        M16A4Item.setBurst(stack, 0);
    }

    private static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Reload>> iterator = RELOADS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Reload> entry = iterator.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player == null || !player.isAlive() || player.isSpectator()
                    || !(player.getMainHandStack().getItem() instanceof M16A4Item)) {
                iterator.remove();
                continue;
            }
            Reload reload = entry.getValue();
            if (player.getServerWorld().getTime() < reload.endTick()) {
                continue;
            }
            ItemStack stack = player.getMainHandStack();
            M16A4Item.setAmmo(stack, reload.empty() ? M16A4Rules.EMPTY_RELOAD_AMMO : M16A4Rules.MAGAZINE_SIZE);
            dev.caecorthus.sparkdlc.SparkDLC.LOGGER.info("[m16a4] reload done ammo={}",
                    reload.empty() ? M16A4Rules.EMPTY_RELOAD_AMMO : M16A4Rules.MAGAZINE_SIZE);
            iterator.remove();
        }
    }

    /** One running reload. / 一次进行中的换弹。 */
    private record Reload(long endTick, boolean empty) {
    }
}
