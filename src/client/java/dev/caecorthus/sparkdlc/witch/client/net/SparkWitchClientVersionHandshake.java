package dev.caecorthus.sparkdlc.witch.client.net;

import dev.caecorthus.sparkdlc.witch.net.SparkWitchVersionHandshake;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;

import java.util.concurrent.CompletableFuture;

public final class SparkWitchClientVersionHandshake {
    private static boolean clientRegistered;

    private SparkWitchClientVersionHandshake() {
    }

    public static synchronized void registerClient() {
        if (clientRegistered) {
            return;
        }
        clientRegistered = true;

        ClientLoginNetworking.registerGlobalReceiver(SparkWitchVersionHandshake.VERSION_CHECK_ID,
                (client, handler, buf, callbacks) -> {
                    SparkWitchVersionHandshake.readVersion(buf);
                    return CompletableFuture.completedFuture(
                            SparkWitchVersionHandshake.writeVersion(SparkWitchVersionHandshake.localVersion()));
                });
    }
}
