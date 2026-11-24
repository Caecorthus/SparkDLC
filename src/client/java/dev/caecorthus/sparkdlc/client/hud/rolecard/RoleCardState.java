package dev.caecorthus.sparkdlc.client.hud.rolecard;

import net.minecraft.client.MinecraftClient;

/**
 * Client-thread timer of the round-start role card: started by every Wathe welcome
 * ({@code RoundTextRendererRoleCardMixin}, so a role reassigned and re-announced restarts it), advanced once per
 * client tick and dropped when it has run out, the world is gone or the client disconnects.
 * 开局身份卡的客户端线程计时器：每次 Wathe 欢迎词都会启动它（{@code RoundTextRendererRoleCardMixin}；职业被重新分配并再次
 * 宣告时随之重启），每个客户端刻推进一次，到时、世界消失或断开连接时清除。
 */
public final class RoleCardState {
    private static final int IDLE = -1;
    private static int age = IDLE;

    private RoleCardState() {
    }

    /** A Wathe welcome began. / Wathe 欢迎词开始。 */
    public static void onWelcome() {
        age = 0;
        RoleCardHud.invalidate();
    }

    /** END_CLIENT_TICK. / 客户端刻末。 */
    public static void tick(MinecraftClient client) {
        if (age == IDLE) {
            return;
        }
        if (client.world == null || client.player == null || ++age > RoleCardRules.END_TICKS) {
            clear();
        }
    }

    public static void clear() {
        age = IDLE;
        RoleCardHud.invalidate();
    }

    /** Ticks since the welcome, partial tick included, or -1 when idle. / 欢迎词之后的刻数（含不足一刻），空闲时为 -1。 */
    static float age(float tickDelta) {
        return age == IDLE ? -1.0F : age + tickDelta;
    }
}
