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
 * Server side of the M16A4's trigger state, reload and recoil feedback. The trigger state is per shooter, not per
 * stack: the deadline of the next round and the stack whose trigger is held, so carrying several rifles buys neither a
 * higher rate of fire nor a second burst from a button that stayed down. A reload is a per-player timer on one stack,
 * started by the left-click intent: tactical (rounds left) is 2.2 s and fills the magazine to 31; empty is 3 s and
 * chambers 30. Starting one spends what is left of the burst window, and the magazine is only written when the timer
 * ends, so an interrupted reload gives nothing. It is dropped silently when that stack is no longer the main-hand stack
 * (slot change, drop, swap to another rifle), when the player dies, resets, disconnects or the server stops. Each
 * accepted round sends its shooter one {@code sparkdlc:m16a4_shot} so the client can kick the view like the revolver.
 * M16A4 扳机状态、换弹与后坐反馈的服务端。扳机状态按射手而不是按物品堆保存：下一发的截止时刻，以及扳机被扣住的那个物品堆，
 * 因此携带多把步枪既换不来更高的射速，也无法让一直按住的按钮打出第二轮点射。换弹是由左键意图启动、针对某一个物品堆的按玩家
 * 计时：战术换弹（还有子弹）2.2 秒装满 31 发；空仓换弹 3 秒装 30 发。开始换弹会花掉点射窗口的剩余部分，弹匣只在计时结束时
 * 写入，因此被打断的换弹什么也得不到。该物品堆不再是主手物品（切换栏位、丢弃、换成另一把步枪）、玩家死亡、重置、断线或
 * 服务器停止时静默丢弃换弹。每发被接受的子弹给射手发一个 {@code sparkdlc:m16a4_shot}，让客户端像左轮一样抬起视角。
 */
public final class M16A4Service {
    /** Server thread only. / 仅在服务端线程访问。 */
    private static final Map<UUID, Reload> RELOADS = new HashMap<>();
    /** Server thread only. / 仅在服务端线程访问。 */
    private static final Map<UUID, Trigger> TRIGGERS = new HashMap<>();
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
        ServerDisconnectCleanup.register((player, server) -> {
            RELOADS.remove(player.getUuid());
            TRIGGERS.remove(player.getUuid());
        });
        KillPlayer.AFTER.register((victim, killer, deathReason) -> RELOADS.remove(victim.getUuid()));
        ResetPlayer.EVENT.register(player -> {
            if (player != null) {
                RELOADS.remove(player.getUuid());
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            RELOADS.clear();
            TRIGGERS.clear();
        });
        M16A4Replay.register();
    }

    /** Whether this server player is in the middle of a reload. / 该服务端玩家是否正在换弹。 */
    public static boolean isReloading(PlayerEntity player) {
        if (player == null || player.getWorld().isClient) {
            return false;
        }
        return RELOADS.containsKey(player.getUuid());
    }

    /**
     * A trigger pull on {@code rifle}: from now on only this stack fires while the button stays down.
     * 对 {@code rifle} 的一次扣扳机：此后按钮按住期间只有这个物品堆会开火。
     */
    static void pull(ServerPlayerEntity shooter, ItemStack rifle) {
        TRIGGERS.computeIfAbsent(shooter.getUuid(), uuid -> new Trigger()).pulled = rifle;
    }

    /** Whether the running use state is the pull that was made on {@code rifle}. / 进行中的使用状态是否就是对 {@code rifle} 的那次扣扳机。 */
    static boolean holdsTrigger(ServerPlayerEntity shooter, ItemStack rifle) {
        Trigger trigger = TRIGGERS.get(shooter.getUuid());
        return trigger != null && trigger.pulled == rifle;
    }

    /** The deadline of the shooter's next round, 0 before the first. / 射手下一发的截止时刻，第一发之前为 0。 */
    static double nextRoundAt(ServerPlayerEntity shooter) {
        Trigger trigger = TRIGGERS.get(shooter.getUuid());
        return trigger == null ? 0.0D : trigger.nextRoundAt;
    }

    static void setNextRoundAt(ServerPlayerEntity shooter, double deadline) {
        TRIGGERS.computeIfAbsent(shooter.getUuid(), uuid -> new Trigger()).nextRoundAt = deadline;
    }

    /** One accepted round: tell its shooter's client to kick the view. / 一发被接受的子弹：通知射手客户端抬起视角。 */
    public static void notifyShot(ServerPlayerEntity shooter) {
        ServerPlayNetworking.send(shooter, M16A4ShotS2CPayload.INSTANCE);
    }

    /**
     * Left click: start a reload unless one runs, the reload gate refuses ({@link M16A4Combat#mayReload}: the main-hand
     * rifle of a holder no control state holds) or the magazine is already full. An empty magazine takes the long
     * reload; anything else is tactical. The payload carries nothing, so everything here is the server's own state.
     * 左键：已在换弹、换弹门槛拒绝（{@link M16A4Combat#mayReload}：未被任何控制状态约束的持有者的主手步枪）或弹匣已满时
     * 不开始。空弹匣走长换弹，其余为战术换弹。数据包不携带任何内容，这里的一切都是服务端自己的状态。
     */
    private static void startReload(ServerPlayerEntity player) {
        if (player == null || RELOADS.containsKey(player.getUuid())) {
            return;
        }
        ItemStack stack = player.getMainHandStack();
        if (!M16A4Combat.mayReload(player, stack)) {
            return;
        }
        int ammo = M16A4Item.getAmmo(stack);
        if (ammo >= M16A4Rules.MAGAZINE_SIZE) {
            return;
        }
        boolean empty = ammo <= 0;
        long duration = empty ? M16A4Rules.EMPTY_RELOAD_TICKS : M16A4Rules.TACTICAL_RELOAD_TICKS;
        RELOADS.put(player.getUuid(), new Reload(stack, player.getServerWorld().getTime() + duration, empty));
        // The reload sound is the whole sequence and plays as the reload starts; the pull stays alive underneath it
        // (ending it would let a held right button re-trigger the rifle). A reload also spends the rest of the window,
        // so a held button cannot keep firing when the magazine comes back.
        // 换弹音效本身就是整段动作，在换弹开始时播放；扣扳机的使用状态在它下面保留（结束使用会让按住的右键重新触发步枪）。
        // 换弹同时清空剩余点射窗口，因此弹匣回来时按住的按钮也不会继续开火。
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                empty ? SparkDlcSounds.M16A4_RELOAD_EMPTY : SparkDlcSounds.M16A4_RELOAD_TACTICAL,
                SoundCategory.PLAYERS, 1.0F, 1.0F);
        M16A4Item.setBurst(stack, 0);
    }

    private static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Reload>> iterator = RELOADS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Reload> entry = iterator.next();
            Reload reload = entry.getValue();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            // The same stack object must still be in the main hand: a second rifle swapped in does not inherit the
            // reload, and a rifle that left the hand loses it.
            // 主手里必须仍是同一个物品堆对象：换上来的另一把步枪不会继承这次换弹，离开手的步枪则失去它。
            if (player == null || !player.isAlive() || player.isSpectator()
                    || player.getMainHandStack() != reload.stack()
                    || !(reload.stack().getItem() instanceof M16A4Item)) {
                iterator.remove();
                continue;
            }
            if (player.getServerWorld().getTime() < reload.endTick()) {
                continue;
            }
            M16A4Item.setAmmo(reload.stack(),
                    reload.empty() ? M16A4Rules.EMPTY_RELOAD_AMMO : M16A4Rules.MAGAZINE_SIZE);
            iterator.remove();
        }
    }

    /**
     * One shooter's trigger: kept until the player leaves, so a death or a round reset never shortens the wait for the
     * next round. / 某名射手的扳机状态：保留到玩家离开，因此死亡或对局重置都不会缩短下一发的等待。
     */
    private static final class Trigger {
        private double nextRoundAt;
        private ItemStack pulled;
    }

    /** One running reload of one stack. / 某一个物品堆上进行中的一次换弹。 */
    private record Reload(ItemStack stack, long endTick, boolean empty) {
    }
}
