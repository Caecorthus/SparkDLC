package dev.caecorthus.sparkdlc.role.civilian.discoverer;

import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesTaotieSeekerBridge;
import dev.caecorthus.sparkdlc.net.DiscovererNightVisionC2SPacket;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.ControlExpertStun;
import dev.caecorthus.sparkdlc.role.witch.grandwitch.GrandWitchFearService;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.util.TickTime;
import dev.doctor4t.wathe.api.event.ResetPlayer;
import dev.doctor4t.wathe.api.event.RoleAssigned;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/**
 * 探索者's Night Vision key (PR #12, owner 2026-11-26), outside the witch skill dispatcher: registers the empty
 * {@code sparkdlc:discoverer_night_vision} payload (common initializer, so both sides know the type; only the server
 * receiver acts) and the cooldown lifecycle, and gates each request ({@link DiscovererRules#nightVisionVerdict}). A
 * granted request gives 15 s of plain Night Vision and starts the cooldown in {@link DiscovererComponent}: 60 s after
 * the first activation of a round, 75 s after every later one. A refused request changes nothing and costs nothing.
 * Blackouts are not immunised against: Wathe still blinds the Discoverer for the blackout, and removes every player's
 * Night Vision when the blackout ends (so does the Engineer's repair), whatever is left of this one included.
 * 探索者的夜视键（PR #12，所有者 2026-11-26），位于魔女技能分发路径之外：注册空的 {@code sparkdlc:discoverer_night_vision}
 * 数据包（在通用初始化器中注册，两端都知道该类型；只有服务端接收器会执行）与冷却生命周期，并判定每个请求
 * （{@link DiscovererRules#nightVisionVerdict}）。请求通过时发放 15 秒普通夜视，并在 {@link DiscovererComponent} 中开始冷却：
 * 本局第一次激活后 60 秒，之后每次 75 秒。被拒绝的请求不改变任何东西，也不消耗冷却。不免疫停电：停电期间 Wathe 照样让探索者
 * 失明，并在停电结束时移除所有玩家的夜视（工程师的修复同样如此），包括本技能剩余的夜视。
 */
public final class DiscovererNightVisionService {
    private static boolean registered;

    private DiscovererNightVisionService() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        PayloadTypeRegistry.playC2S().register(DiscovererNightVisionC2SPacket.ID, DiscovererNightVisionC2SPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DiscovererNightVisionC2SPacket.ID,
                (payload, context) -> tryUse(context.player()));
        // Every role assignment and every Wathe reset starts from "ready, unused": the state never follows a player
        // into another round or another role. There is no opening cooldown.
        // 每次身份分配与每次 Wathe 重置都回到“就绪、未使用”：状态不会跟着玩家进入下一局或另一个职业。没有开局冷却。
        RoleAssigned.EVENT.register((player, role) -> DiscovererComponent.KEY.get(player).clear());
        ResetPlayer.EVENT.register(player -> DiscovererComponent.KEY.get(player).clear());
    }

    /**
     * Called on the server thread for every request. The safe-time, stun, daze, Seeker-session, Riftwalker-session,
     * Knight duel and Fear payload guards already drop most refused requests; this re-checks every gate against
     * server state, so the client can only ask.
     * 每个请求都在服务端线程调用。安全时间、眩晕、饕餮眩晕、搜寻者会话、隙行者会话、骑士决斗与恐惧的数据包拦截已丢弃大多数
     * 被拒请求；此处按服务端状态重新检查每个门槛，客户端只能“请求”。
     */
    public static void tryUse(ServerPlayerEntity player) {
        if (player == null) {
            return;
        }
        ServerWorld world = player.getServerWorld();
        long now = world.getTime();
        GameWorldComponent game = GameWorldComponent.KEY.get(world);
        DiscovererComponent state = DiscovererComponent.KEY.get(player);
        int cooldown = state.nightVisionCooldownRemaining(now);
        StatusEffectInstance current = player.getStatusEffect(StatusEffects.NIGHT_VISION);
        DiscovererRules.NightVisionVerdict verdict = DiscovererRules.nightVisionVerdict(
                DiscovererRules.isDiscoverer(game.getRole(player)),
                game.getGameStatus() == GameWorldComponent.GameStatus.ACTIVE,
                GameFunctions.isPlayerPlayingAndAlive(player) && !player.isSpectator(),
                NoellesTaotieSeekerBridge.isSwallowed(player),
                ControlExpertStun.isStunned(player),
                SparkTraitsApi.isRoleSkillBlocked(player),
                cooldown <= 0,
                DiscovererRules.isDarkEnoughForNightVision(lightLevel(player)),
                DiscovererRules.nightVisionAlreadyCovers(current != null, current != null && current.isInfinite(),
                        current == null ? 0 : current.getDuration()),
                GrandWitchFearService.isPlayerFeared(player));
        switch (verdict) {
            case USE -> grant(player, state, now);
            case COOLING_DOWN -> tell(player, Text.translatable(
                    "message.sparkdlc.discoverer.night_vision.cooldown", TickTime.ceilSeconds(cooldown)));
            case TOO_BRIGHT -> tell(player, Text.translatable("message.sparkdlc.discoverer.night_vision.too_bright"));
            case ALREADY_ACTIVE -> tell(player,
                    Text.translatable("message.sparkdlc.discoverer.night_vision.already_active"));
            case FEARED -> GrandWitchFearService.sendSkillBlocked(player);
            case REFUSE -> {
            }
        }
    }

    private static void grant(ServerPlayerEntity player, DiscovererComponent state, long now) {
        state.setNightVisionReadyTick(now + DiscovererRules.nightVisionCooldownTicks(state.nightVisionUsed()));
        state.markNightVisionUsed();
        // The flags of every other Night Vision in the mod (not ambient, no particles, icon shown). Vanilla merges
        // it into a weaker or shorter one already running and never shortens that.
        // 与本模组其他夜视相同的标志（非环境效果、无粒子、显示图标）。原版会把它并入已有的较弱或较短的夜视，绝不缩短后者。
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION,
                DiscovererRules.NIGHT_VISION_TICKS, 0, false, false, true));
        tell(player, Text.translatable("message.sparkdlc.discoverer.night_vision.granted"));
    }

    /**
     * The server's effective light at the player's feet: block light, or sky light less the ambient darkness, whichever
     * is higher. Raw sky light would not do: it is 15 under open sky at any hour, so the skill could never be used
     * outdoors at night.
     * 玩家脚下的服务端有效亮度：方块光，或天光减去环境暗度，取较高者。不能用原始天光：露天处无论几点都是 15，那样夜里在室外
     * 永远用不了技能。
     */
    private static int lightLevel(ServerPlayerEntity player) {
        return player.getServerWorld().getLightLevel(player.getBlockPos());
    }

    private static void tell(ServerPlayerEntity player, Text message) {
        player.sendMessage(message, true);
    }
}
