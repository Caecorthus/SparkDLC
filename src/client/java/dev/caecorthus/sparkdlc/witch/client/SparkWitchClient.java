package dev.caecorthus.sparkdlc.witch.client;

import dev.caecorthus.sparkdlc.witch.SparkWitch;
import dev.caecorthus.sparkdlc.witch.SparkWitchSounds;
import dev.caecorthus.sparkdlc.witch.client.screen.CriminologistScreen;
import dev.caecorthus.sparkdlc.witch.client.net.SparkWitchClientVersionHandshake;
import dev.caecorthus.sparkdlc.witch.component.WitchPlayerComponent;
import dev.caecorthus.sparkdlc.witch.component.WitchWorldComponent;
import dev.caecorthus.sparkdlc.witch.net.OpenCriminologistScreenS2CPacket;
import dev.caecorthus.sparkdlc.witch.net.SparkWitchServerConnection;
import dev.caecorthus.sparkdlc.witch.net.UseWitchSkillC2SPacket;
import dev.doctor4t.ratatouille.client.util.ambience.AmbienceUtil;
import dev.doctor4t.ratatouille.client.util.ambience.BackgroundAmbience;
import dev.doctor4t.wathe.api.event.CanSeePoison;
import dev.doctor4t.wathe.api.event.ShouldShowCohort;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

public final class SparkWitchClient implements ClientModInitializer {
    private static final String LAMBDYNAMICLIGHTS_MOD_ID = "lambdynlights";

    @Override
    public void onInitializeClient() {
        SparkWitch.LOGGER.info("Initializing SparkWitch client hooks.");
        SparkWitchServerConnection.reset();
        SparkWitchClientVersionHandshake.registerClient();
        requireLambDynamicLights();

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SparkWitchServerConnection.reset());
        NoellesRoleEnhancementClientHooks.register();
        WitchInstinctSuppressionClientHooks.register();
        registerGrandWitchCeremonialSwordBgm();

        ClientPlayNetworking.registerGlobalReceiver(OpenCriminologistScreenS2CPacket.ID,
                (payload, context) -> context.client().execute(() -> {
                    if (SparkWitchServerConnection.isConfirmedServer()) {
                        context.client().setScreen(new CriminologistScreen(payload.victimUuid()));
                    }
                }
                ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!SparkWitchServerConnection.isConfirmedServer()) {
                DeathRayClientHooks.reset();
                return;
            }
            GrandWitchFearClientHooks.tick();
            DeathRayClientHooks.tick(client);
            if (client.player != null
                    && client.getNetworkHandler() != null
                    && WitchPlayerComponent.KEY.get(client.player).hasSkill()
                    && WitchAbilityKeyBridge.wasPressed()) {
                ClientPlayNetworking.send(new UseWitchSkillC2SPacket());
            }
        });

        ShouldShowCohort.EVENT.register((viewer, target) -> {
            if (!SparkWitchServerConnection.isConfirmedServer()) {
                return null;
            }
            if (WitchCohortClientHooks.isGrandWitchCohortPair(viewer, target)) {
                return ShouldShowCohort.CohortResult.hide(110);
            }
            return null;
        });
        CanSeePoison.EVENT.register(WitchPoisonVisionClientHooks::canSeeHiddenPoison);
    }

    public static Text abilityKeyText() {
        return WitchAbilityKeyBridge.keyText();
    }

    private static void registerGrandWitchCeremonialSwordBgm() {
        AmbienceUtil.registerBackgroundAmbience(new BackgroundAmbience(
                SparkWitchSounds.GRAND_WITCH_CEREMONIAL_SWORD_BGM,
                player -> SparkWitchServerConnection.isConfirmedServer()
                        && WitchWorldComponent.KEY.get(player.getWorld()).hasGrandWitchCeremonialSwordBgm(),
                20
        ));
    }

    private static void requireLambDynamicLights() {
        if (!FabricLoader.getInstance().isModLoaded(LAMBDYNAMICLIGHTS_MOD_ID)) {
            throw new IllegalStateException(
                    "SparkWitch requires LambDynamicLights on the client for flashlight lighting. "
                            + "Install LambDynamicLights for Minecraft 1.21.1."
            );
        }
    }
}
