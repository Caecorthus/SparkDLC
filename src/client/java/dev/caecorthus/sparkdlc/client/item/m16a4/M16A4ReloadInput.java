package dev.caecorthus.sparkdlc.client.item.m16a4;

import dev.caecorthus.sparkdlc.SparkDlcItems;
import dev.caecorthus.sparkdlc.client.input.MacControlClickRules;
import dev.caecorthus.sparkdlc.client.mixin.input.SelectedSlotFlushInvoker;
import dev.caecorthus.sparkdlc.net.M16A4ReloadC2SPayload;
import dev.caecorthus.sparkdlc.net.version.SparkDlcServerConnection;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * M16A4 left-click, client side (the Wither Gohei's and the launcher's input layer, with a reload in place of a shot):
 * while the main hand holds the rifle the vanilla attack and block breaking are swallowed, and each fresh attack press
 * that vanilla's own {@code handleInputEvents} drains sends one {@code sparkdlc:m16a4_reload}. The press is never read
 * anywhere else, so whoever drains the attack key first (a piloted drone, a key lock) keeps it. A keyboard-bound
 * attack key held down never repeats the intent. Client intent only: the server decides whether a reload starts.
 * M16A4 左键的客户端部分（沿用凋灵御币与炮筒的输入层，只是把射击换成换弹）：主手持步枪时吞掉原版攻击与方块破坏，原版自己的
 * {@code handleInputEvents} 每取出一次新的攻击按下，就发送一次 {@code sparkdlc:m16a4_reload}。按键不在别处读取，因此谁先
 * 取走攻击键（驾驶中的无人机、按键锁）谁就保有它。按住绑定在键盘上的攻击键不会重复发送意图。仅为客户端意图：是否开始换弹由
 * 服务端决定。
 */
public final class M16A4ReloadInput {
    /** Armed at every tick end unless a repeating keyboard attack key is held. / 每刻末尾解除闸门，除非按住会重复的键盘攻击键。 */
    private static boolean armed;

    private M16A4ReloadInput() {
    }

    /**
     * Whether the local player's left click belongs to the rifle: it is the main-hand stack and the player is no
     * spectator (a spectator's left click still picks a player to watch).
     * 本地玩家的左键是否归步枪所有：步枪是主手物品，且玩家不是旁观者（旁观者的左键仍用于选择观察对象）。
     */
    public static boolean ownsAttackKey(ClientPlayerEntity player) {
        return !player.isSpectator() && player.getMainHandStack().isOf(SparkDlcItems.m16a4());
    }

    /** {@code doAttack} HEAD: true swallows the attack. Never reloads. / {@code doAttack} HEAD：返回 true 表示吞掉攻击，从不换弹。 */
    public static boolean onAttack(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        return player != null && ownsAttackKey(player);
    }

    /** A drained attack press: the only reload path. / 被取出的攻击按键：唯一的换弹路径。 */
    public static void onPressEdge(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || !ownsAttackKey(player) || !armed) {
            return;
        }
        armed = false;
        requestReload(client, player);
    }

    /** {@code handleBlockBreaking} HEAD: true cancels it. / {@code handleBlockBreaking} HEAD：返回 true 表示取消。 */
    public static boolean onBlockBreaking(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        return player != null && ownsAttackKey(player);
    }

    /**
     * macOS only: keeps Ctrl + left-click (sprint is Left Ctrl) a left press while the rifle is held, so it reloads
     * instead of pulling the trigger. / 仅 macOS：手持步枪时 Ctrl + 左键（疾跑是左 Ctrl）仍是左键，因此是换弹而不是扣扳机。
     */
    public static boolean keepsMacLeftPress(MinecraftClient client, int button, int action,
                                            int pendingRemappedReleases) {
        ClientPlayerEntity player = client.player;
        return MacControlClickRules.keepsLeftPress(
                button == GLFW.GLFW_MOUSE_BUTTON_LEFT,
                action == GLFW.GLFW_PRESS,
                client.currentScreen != null,
                player != null && ownsAttackKey(player),
                pendingRemappedReleases);
    }

    /** End of every client tick on a confirmed server. / 已确认服务器上的每个客户端刻末尾。 */
    public static void tick(MinecraftClient client) {
        armed = !repeatingAttackKeyHeld(client);
    }

    public static void reset() {
        armed = false;
    }

    private static boolean repeatingAttackKeyHeld(MinecraftClient client) {
        InputUtil.Key key = KeyBindingHelper.getBoundKeyOf(client.options.attackKey);
        return switch (key.getCategory()) {
            case KEYSYM -> key.getCode() != InputUtil.UNKNOWN_KEY.getCode()
                    && InputUtil.isKeyPressed(client.getWindow().getHandle(), key.getCode());
            case SCANCODE -> client.options.attackKey.isPressed();
            case MOUSE -> false;
        };
    }

    private static void requestReload(MinecraftClient client, ClientPlayerEntity player) {
        if (client.currentScreen != null || client.getCameraEntity() != player
                || !SparkDlcServerConnection.isConfirmedServer() || client.getNetworkHandler() == null
                || !ClientPlayNetworking.canSend(M16A4ReloadC2SPayload.ID)) {
            return;
        }
        // The server reloads only the rifle in its main hand: flush a hotbar change made earlier this tick first.
        // 服务端只给其主手中的步枪换弹：先同步本刻早先的快捷栏变化。
        if (client.interactionManager != null) {
            ((SelectedSlotFlushInvoker) client.interactionManager).sparkdlc$flushSelectedSlot();
        }
        ClientPlayNetworking.send(M16A4ReloadC2SPayload.INSTANCE);
    }
}
