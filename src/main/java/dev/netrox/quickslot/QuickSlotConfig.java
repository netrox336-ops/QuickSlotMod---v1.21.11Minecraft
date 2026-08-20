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
    private boolean enabled = true;
    private boolean resourceHud = true;
    private Profile profile = Profile.NORMAL;

    private QuickSlotConfig() {
        resetDefaults();
    }

    public static QuickSlotConfig get() {
        INSTANCE.ensureLoaded();
        return INSTANCE;
    }

    private void resetDefaults() {
        rules.put(Profile.NORMAL, new ItemRule[]{ItemRule.SWORD, ItemRule.BLOCKS, ItemRule.PICKAXE, ItemRule.AXE, ItemRule.BOW, ItemRule.CONSUMABLE, ItemRule.UTILITY, ItemRule.FREE, ItemRule.FREE});
        rules.put(Profile.RUSH, new ItemRule[]{ItemRule.SWORD, ItemRule.BLOCKS, ItemRule.BLOCKS, ItemRule.PICKAXE, ItemRule.AXE, ItemRule.CONSUMABLE, ItemRule.UTILITY, ItemRule.FREE, ItemRule.FREE});
        rules.put(Profile.BRIDGE, new ItemRule[]{ItemRule.BLOCKS, ItemRule.BLOCKS, ItemRule.SWORD, ItemRule.PICKAXE, ItemRule.AXE, ItemRule.CONSUMABLE, ItemRule.UTILITY, ItemRule.FREE, ItemRule.FREE});
    }

    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        Path file = configFile();
        if (!Files.isRegularFile(file)) return;

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
            enabled = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
            resourceHud = Boolean.parseBoolean(properties.getProperty("resourceHud", "true"));
            try {
                profile = Profile.valueOf(properties.getProperty("profile", Profile.NORMAL.name()));
            } catch (IllegalArgumentException ignored) {
                profile = Profile.NORMAL;
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
        properties.setProperty("enabled", Boolean.toString(enabled));
        properties.setProperty("resourceHud", Boolean.toString(resourceHud));
        properties.setProperty("profile", profile.name());
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
                properties.store(output, "QuickSlot 1.0.0");
            }
        } catch (IOException ignored) {
        }
    }

    private Path configFile() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("quickslot.properties");
    }

    public boolean enabled() { return enabled; }
    public void toggleEnabled() { enabled = !enabled; save(); }
    public boolean resourceHud() { return resourceHud; }
    public void toggleResourceHud() { resourceHud = !resourceHud; save(); }
    public Profile profile() { return profile; }
    public void nextProfile() { profile = profile.next(); save(); }
    public ItemRule rule(int slot) { return rules.get(profile)[slot]; }
    public void cycleRule(int slot) { rules.get(profile)[slot] = rule(slot).next(); save(); }
}
