package dev.netrox.quickslot;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

final class ProfileContextStore {
    private static final ProfileContextStore INSTANCE = new ProfileContextStore();
    private static final int MAX_CONTEXTS = 64;

    private final LinkedHashMap<String, Profile> profiles = new LinkedHashMap<>();
    private boolean loaded;
    private boolean enabled = true;
    private String activeContext;
    private Profile observedProfile;

    private ProfileContextStore() {}

    static ProfileContextStore get() {
        INSTANCE.ensureLoaded();
        return INSTANCE;
    }

    boolean enabled() {
        ensureLoaded();
        return enabled;
    }

    void toggleEnabled(QuickSlotConfig config) {
        ensureLoaded();
        enabled = !enabled;
        if (enabled && activeContext != null && config != null) {
            remember(activeContext, config.profile());
            observedProfile = config.profile();
        }
        save();
    }

    boolean sync(String context, QuickSlotConfig config) {
        ensureLoaded();
        if (config == null) return false;

        Profile current = config.profile();
        if (Objects.equals(activeContext, context)) {
            if (enabled && context != null && current != observedProfile) {
                remember(context, current);
                save();
            }
            observedProfile = current;
            return false;
        }

        if (enabled && activeContext != null) {
            remember(activeContext, current);
        }

        activeContext = context;
        if (!enabled || context == null) {
            observedProfile = current;
            save();
            return false;
        }

        Profile remembered = profiles.get(context);
        if (remembered == null) {
            remember(context, current);
            observedProfile = current;
            save();
            return false;
        }

        boolean restored = remembered != current;
        if (restored) config.setProfile(remembered);
        observedProfile = remembered;
        remember(context, remembered);
        save();
        return restored;
    }

    private void remember(String context, Profile profile) {
        if (context == null || profile == null) return;
        profiles.remove(context);
        profiles.put(context, profile);

        while (profiles.size() > MAX_CONTEXTS) {
            Iterator<String> iterator = profiles.keySet().iterator();
            if (!iterator.hasNext()) break;
            iterator.next();
            iterator.remove();
        }
    }

    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        Path file = file();
        if (!Files.isRegularFile(file)) return;

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
            enabled = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
            int count = parseInt(properties.getProperty("count"), 0);
            for (int index = 0; index < Math.min(count, MAX_CONTEXTS); index++) {
                String context = properties.getProperty("context." + index + ".key");
                String rawProfile = properties.getProperty("context." + index + ".profile");
                if (context == null || context.isBlank() || rawProfile == null) continue;
                try {
                    profiles.put(context, Profile.valueOf(rawProfile));
                } catch (IllegalArgumentException ignored) {
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void save() {
        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(enabled));
        properties.setProperty("count", Integer.toString(profiles.size()));

        int index = 0;
        for (Map.Entry<String, Profile> entry : profiles.entrySet()) {
            properties.setProperty("context." + index + ".key", entry.getKey());
            properties.setProperty("context." + index + ".profile", entry.getValue().name());
            index++;
        }

        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                properties.store(output, "QuickSlot 1.9.0 server profile memory");
            }
        } catch (IOException ignored) {
        }
    }

    private Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("quickslot-contexts.properties");
    }

    private static int parseInt(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
