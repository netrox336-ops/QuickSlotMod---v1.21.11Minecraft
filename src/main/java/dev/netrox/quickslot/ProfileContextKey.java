package dev.netrox.quickslot;

import java.util.Locale;

final class ProfileContextKey {
    private ProfileContextKey() {}

    static String server(String address) {
        if (address == null) return null;
        String normalized = address.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : "server:" + normalized;
    }

    static String singleplayer(String worldName) {
        if (worldName == null) return "singleplayer";
        String normalized = worldName.trim();
        return normalized.isEmpty() ? "singleplayer" : "singleplayer:" + normalized;
    }
}
