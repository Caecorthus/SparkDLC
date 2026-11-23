package dev.caecorthus.sparkdlc.round.dualselect;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.doctor4t.wathe.api.GameMode;
import dev.doctor4t.wathe.api.WatheGameModes;
import dev.doctor4t.wathe.api.event.GameEvents;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * Registers the {@code sparkdlc:dual_role_murder} game mode and runs the opening pick: right after a round of that mode
 * initialises, every player in a pickable faction is offered one alternative role from the same faction.
 * <p>
 * 注册 {@code sparkdlc:dual_role_murder} 模式，并负责开局那次抉择：该模式的回合初始化结束后，每个可参与抽签的阵营玩家都会
 * 拿到同阵营的另一个身份作为备选。
 */
public final class DualSelectService {

    /** The mode id this service owns; the mode vote looks it up by the same id. / 本服务负责的模式 id，模式投票按同一 id 查找。 */
    public static final Identifier MODE_ID = SparkDLC.id("dual_role_murder");

    private static GameMode mode;

    private DualSelectService() {
    }

    public static void register() {
        mode = WatheGameModes.registerGameMode(MODE_ID, new DualRoleGameMode(MODE_ID));
        PayloadTypeRegistry.playC2S().register(DualRoleChoiceC2SPayload.ID, DualRoleChoiceC2SPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DualRoleChoiceC2SPayload.ID, new DualRoleChoiceC2SPayload.Receiver());
        ServerTickEvents.END_SERVER_TICK.register(DualSelectService::tick);
        GameEvents.ON_FINISH_INITIALIZE.register((world, gameWorldComponent) -> {
            if (gameWorldComponent.getGameMode() != mode) {
                return;
            }
            if (!(world instanceof ServerWorld serverWorld)) {
                return;
            }
            DualRoleSelectionComponent.KEY.get(serverWorld.getScoreboard()).startVote(serverWorld, gameWorldComponent);
        });
        SparkDLC.LOGGER.info("[dualselect] registered game mode {}", MODE_ID);
    }

    public static GameMode mode() {
        return mode;
    }

    /**
     * Drives the selection countdown from the overworld only: {@code getScoreboard()} is backed by one server-wide
     * scoreboard, so ticking every dimension would run the same component three times per tick.
     * <p>
     * 只由主世界驱动抉择倒计时：{@code getScoreboard()} 背后是同一个服务器级记分板，遍历每个维度会让同一个组件每 tick 走三帧。
     */
    private static void tick(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        DualRoleSelectionComponent component = DualRoleSelectionComponent.KEY.get(world.getScoreboard());
        if (component.phase() != DualRoleSelectionComponent.Phase.SELECTING) {
            return;
        }
        component.tick(world, GameWorldComponent.KEY.get(world));
    }
}
