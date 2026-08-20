package dev.netrox.quickslot;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public final class QuickSlotConfig {
    private static final QuickSlotConfig INSTANCE = new QuickSlotConfig();
    private final Map<Profile, ItemRule[]> rules = new EnumMap<>(Profile.class);
    private boolean loaded;
    private boolean autoSortEnabled = true;
    private boolean removeResourcesFromHotbar = true;
    private boolean resourceHud = true;
    private int hudX = 6;
    private int hudY = 6;
    private float hudScale = 1.0F;
    private Profile profile = Profile.NORMAL;
    private RefillMode refillMode = RefillMode.EMPTY_ONLY;
    private int refillThreshold = 16;

    private QuickSlotConfig() {
        resetDefaults();
    }

    public static QuickSlotConfig get() {
        INSTANCE.ensureLoaded();
        return INSTANCE;
    }

    private void resetDefaults() {
        rules.put(Profile.NORMAL, new ItemRule[]{ItemRule.SWORD, ItemRule.BLOCKS, ItemRule.PICKAXE, ItemRule.AXE, ItemRule.BOW, ItemRule.GOLDEN_APPLE, ItemRule.SHEARS, ItemRule.FREE, ItemRule.FREE});
        rules.put(Profile.RUSH, new ItemRule[]{ItemRule.SWORD, ItemRule.BLOCKS, ItemRule.BLOCKS, ItemRule.PICKAXE, ItemRule.AXE, ItemRule.GOLDEN_APPLE, ItemRule.FIREBALL, ItemRule.ENDER_PEARL, ItemRule.FREE});
        rules.put(Profile.BRIDGE, new ItemRule[]{ItemRule.BLOCKS, ItemRule.BLOCKS, ItemRule.SWORD, ItemRule.PICKAXE, ItemRule.AXE, ItemRule.GOLDEN_APPLE, ItemRule.LADDER, ItemRule.WATER, ItemRule.FREE});
    }

    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        Path file = configFile();
        if (!Files.isRegularFile(file)) return;

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
            String legacyEnabled = properties.getProperty("enabled", "true");
            autoSortEnabled = Boolean.parseBoolean(properties.getProperty("autoSortEnabled", legacyEnabled));
            removeResourcesFromHotbar = Boolean.parseBoolean(properties.getProperty("removeResourcesFromHotbar", "true"));
            resourceHud = Boolean.parseBoolean(properties.getProperty("resourceHud", "true"));
            hudX = parseInt(properties.getProperty("hudX"), 6);
            hudY = parseInt(properties.getProperty("hudY"), 6);
            hudScale = clamp(parseFloat(properties.getProperty("hudScale"), 1.0F), 0.5F, 3.0F);
            refillThreshold = clamp(parseInt(properties.getProperty("refillThreshold"), 16), 1, 64);

            try {
                profile = Profile.valueOf(properties.getProperty("profile", Profile.NORMAL.name()));
            } catch (IllegalArgumentException ignored) {
                profile = Profile.NORMAL;
            }

            try {
                refillMode = RefillMode.valueOf(properties.getProperty("refillMode", RefillMode.EMPTY_ONLY.name()));
            } catch (IllegalArgumentException ignored) {
                refillMode = RefillMode.EMPTY_ONLY;
            }

            for (Profile p : Profile.values()) {
                ItemRule[] profileRules = rules.get(p);
                for (int slot = 0; slot < 9; slot++) {
                    String value = properties.getProperty("profile." + p.name() + ".slot." + slot);
                    if (value == null) continue;
                    try {
                        profileRules[slot] = ItemRule.valueOf(value);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    public void save() {
        Properties properties = new Properties();
        properties.setProperty("autoSortEnabled", Boolean.toString(autoSortEnabled));
        properties.setProperty("removeResourcesFromHotbar", Boolean.toString(removeResourcesFromHotbar));
        properties.setProperty("resourceHud", Boolean.toString(resourceHud));
        properties.setProperty("hudX", Integer.toString(hudX));
        properties.setProperty("hudY", Integer.toString(hudY));
        properties.setProperty("hudScale", Float.toString(hudScale));
        properties.setProperty("profile", profile.name());
        properties.setProperty("refillMode", refillMode.name());
        properties.setProperty("refillThreshold", Integer.toString(refillThreshold));

        for (Profile p : Profile.values()) {
            ItemRule[] profileRules = rules.get(p);
            for (int slot = 0; slot < 9; slot++) {
                properties.setProperty("profile." + p.name() + ".slot." + slot, profileRules[slot].name());
            }
        }

        Path file = configFile();
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                properties.store(output, "QuickSlot 1.2.0");
            }
        } catch (IOException ignored) {
        }
    }

    private Path configFile() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("quickslot.properties");
    }

    private static int parseInt(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static float parseFloat(String value, float fallback) {
        if (value == null) return fallback;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean autoSortEnabled() { return autoSortEnabled; }
    public void toggleAutoSort() { autoSortEnabled = !autoSortEnabled; save(); }
    public boolean removeResourcesFromHotbar() { return removeResourcesFromHotbar; }
    public void toggleRemoveResourcesFromHotbar() { removeResourcesFromHotbar = !removeResourcesFromHotbar; save(); }
    public boolean resourceHud() { return resourceHud; }
    public void toggleResourceHud() { resourceHud = !resourceHud; save(); }
    public int hudX() { return hudX; }
    public int hudY() { return hudY; }
    public float hudScale() { return hudScale; }
    public void setHudPosition(int x, int y) { hudX = Math.max(0, x); hudY = Math.max(0, y); }
    public void setHudScale(float scale) { hudScale = clamp(scale, 0.5F, 3.0F); }
    public void resetHud() { hudX = 6; hudY = 6; hudScale = 1.0F; save(); }
    public Profile profile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; save(); }
    public void nextProfile() { profile = profile.next(); save(); }
    public RefillMode refillMode() { return refillMode; }
    public void nextRefillMode() { refillMode = refillMode.next(); save(); }
    public int refillThreshold() { return refillThreshold; }
    public void setRefillThreshold(int threshold) { refillThreshold = clamp(threshold, 1, 64); save(); }
    public ItemRule rule(int slot) { return rules.get(profile)[slot]; }
    public void cycleRule(int slot) { rules.get(profile)[slot] = rule(slot).next(); save(); }
}
