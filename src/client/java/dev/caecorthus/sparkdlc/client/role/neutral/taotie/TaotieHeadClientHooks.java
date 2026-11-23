package dev.caecorthus.sparkdlc.client.role.neutral.taotie;

import dev.caecorthus.sparkdlc.SparkDlcEntities;
import dev.caecorthus.sparkdlc.client.render.entity.TaotieHeadEntityRenderer;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadPlayerComponent;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadFireC2SPacket;
import dev.caecorthus.sparkdlc.role.neutral.taotie.TaotieHeadDaze;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.taotie.SwallowedPlayerComponent;
import org.agmas.noellesroles.taotie.TaotiePlayerComponent;
import org.jetbrains.annotations.Nullable;

/**
 * Client key hook for the Taotie head launch.
 * 饕餮“发射头颅”的客户端按键挂钩。
 *
 * <p>The key is the shared "second skill key" {@code key.sparkdlc.role_skill_2}, found at runtime by translation
 * key; it is always registered, so this hook registers no binding of its own. The secondary skill controller drains
 * {@code wasPressed()}, so this hook reads the rising edge of {@code isPressed()} and never consumes the press queue.
 * The client gate only avoids pointless packets; the server re-validates every condition.
 * 按键复用“第二技能键” key.sparkdlc.role_skill_2，运行时按翻译键查找；该键始终已注册，因此这里不注册自己的按键。
 * 第二技能控制器会清空 wasPressed() 队列，所以这里只对 isPressed() 做上升沿检测，绝不消耗按键队列。
 * 客户端条件只用于减少无效发包，服务端会重新校验全部条件。</p>
 */
public final class TaotieHeadClientHooks {
    private static final String SHARED_KEY = "key.sparkdlc.role_skill_2";
    private static final Text DEFAULT_KEY_TEXT = Text.literal("N");

    private static boolean registered;
    private static @Nullable KeyBinding sharedBinding;
    private static boolean keyWasDown;

    private TaotieHeadClientHooks() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientTickEvents.END_CLIENT_TICK.register(TaotieHeadClientHooks::tick);
        EntityRendererRegistry.register(SparkDlcEntities.taotieHead(), TaotieHeadEntityRenderer::new);
    }

    /** Localized name of the bound second skill key for HUD text. / HUD 文本使用的第二技能键本地化名称。 */
    public static Text secondarySkillKeyText() {
        KeyBinding binding = secondarySkillKey();
        return binding != null ? binding.getBoundKeyLocalizedText() : DEFAULT_KEY_TEXT;
    }

    private static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        KeyBinding binding = secondarySkillKey();
        if (player == null || binding == null) {
            keyWasDown = false;
            return;
        }

        boolean keyDown = binding.isPressed();
        if (keyDown && !keyWasDown && client.currentScreen == null && canRequestFire(player)) {
            ClientPlayNetworking.send(new TaotieHeadFireC2SPacket());
        }
        keyWasDown = keyDown;
    }

    private static boolean canRequestFire(ClientPlayerEntity player) {
        if (!GameFunctions.isPlayerPlayingAndAlive(player)
                || !GameWorldComponent.KEY.get(player.getWorld()).isRole(player, Noellesroles.TAOTIE)
                || SwallowedPlayerComponent.isPlayerSwallowed(player)) {
            return false;
        }
        return TaotiePlayerComponent.KEY.get(player).getSwallowedCount() > 0
                && TaotieHeadPlayerComponent.KEY.get(player).getCooldownTicks() <= 0
                && !TaotieHeadDaze.isDazed(player);
    }

    /**
     * {@code SecondaryAbilityController} registers the binding during client init, so it is already in
     * {@code options.allKeys} by the first tick; the lookup is retried while it misses and cached once found.
     * {@code SecondaryAbilityController} 在客户端初始化时注册该键，首个 tick 时已在 options.allKeys 中；未找到时下次重试，
     * 找到后缓存。
     */
    private static @Nullable KeyBinding secondarySkillKey() {
        if (sharedBinding == null) {
            sharedBinding = findBinding(SHARED_KEY);
        }
        return sharedBinding;
    }

    private static @Nullable KeyBinding findBinding(String translationKey) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) {
            return null;
        }
        for (KeyBinding binding : client.options.allKeys) {
            if (translationKey.equals(binding.getTranslationKey())) {
                return binding;
            }
        }
        return null;
    }
}
