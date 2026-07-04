package dev.caecorthus.sparkdlc.strength.client.screen.tablet;

import dev.caecorthus.sparkdlc.strength.network.tablet.TabletSnapshot;

public final class TabletClientState {
    private static TabletSnapshot snapshot = TabletSnapshot.empty();
    private static String chatDraft = "";

    private TabletClientState() {
    }

    public static TabletSnapshot snapshot() {
        return snapshot;
    }

    public static void apply(TabletSnapshot nextSnapshot) {
        snapshot = nextSnapshot == null ? TabletSnapshot.empty() : nextSnapshot;
    }

    public static String chatDraft() {
        return chatDraft;
    }

    public static void setChatDraft(String draft) {
        chatDraft = draft == null ? "" : draft;
    }

    public static void clearChatDraft() {
        chatDraft = "";
    }
}
