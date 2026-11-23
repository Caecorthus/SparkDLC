package dev.caecorthus.sparkdlc.client.hooks;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.agmas.noellesroles.client.NoellesrolesClient;

/**
 * Reuses the shared Wathe-role ability key instead of registering a keybind of the mod's own.
 * 复用列车职业体系已有的技能控制键，不为本模组新增独立按键项。
 */
public final class AbilityKeyBridge {
    static final String SHARED_ABILITY_TRANSLATION_KEY = "key.noellesroles.ability";

    private static final SharedAbilityPressState PRESS_STATE = new SharedAbilityPressState();

    private AbilityKeyBridge() {
    }

    /**
     * Consumes a press observed from NoellesRoles' own {@link KeyBinding#wasPressed()} call.
     * This preserves short taps even after NoellesRoles consumes the shared key's press counter.
     */
    public static boolean wasPressed() {
        return PRESS_STATE.consume();
    }

    public static void captureSharedAbilityPress(KeyBinding keyBinding, boolean pressed) {
        if (pressed
                && keyBinding != null
                && keyBinding == NoellesrolesClient.abilityBind) {
            PRESS_STATE.record();
        }
    }

    public static void reset() {
        PRESS_STATE.reset();
    }

    /** Whether the shared ability key is physically held now. / 共享技能键当前是否被按住。 */
    public static boolean isHeld() {
        KeyBinding keyBinding = sharedAbilityKeyBinding();
        return keyBinding != null && keyBinding.isPressed();
    }

    public static Text keyText() {
        KeyBinding keyBinding = sharedAbilityKeyBinding();
        return keyBinding == null
                ? Text.translatable(SHARED_ABILITY_TRANSLATION_KEY)
                : keyBinding.getBoundKeyLocalizedText();
    }

    private static KeyBinding sharedAbilityKeyBinding() {
        return NoellesrolesClient.abilityBind;
    }
}
