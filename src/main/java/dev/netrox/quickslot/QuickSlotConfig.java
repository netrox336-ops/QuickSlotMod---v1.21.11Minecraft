package dev.netrox.quickslot;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public final class QuickSlotConfig {
    private static final QuickSlotConfig INSTANCE = new QuickSlotConfig();
    private static final RefillMode DEFAULT_REFILL_MODE = RefillMode.EMPTY_ONLY;
    private static final int DEFAULT_REFILL_THRESHOLD = 16;
    private static final BlockType[] DEFAULT_BLOCK_PRIORITY = {
        BlockType.WOOL,
        BlockType.PLANKS,
        BlockType.END_STONE,
        BlockType.CLAY,
        BlockType.GLASS,
        BlockType.OBSIDIAN,
        BlockType.OTHER
    };

    private final Map<Profile, ItemRule[]> rules = new EnumMap<>(Profile.class);
    private final Map<Profile, boolean[]> refillEnabled = new EnumMap<>(Profile.class);
    private final Map<Profile, RefillMode[]> refillModes = new EnumMap<>(Profile.class);
    private final Map<Profile, int[]> refillThresholds = new EnumMap<>(Profile.class);
    private final Map<Profile, BlockType[]> blockPriority = new EnumMap<>(Profile.class);
    private final Map<Profile, Boolean> preferSameBlock = new EnumMap<>(Profile.class);

    private boolean loaded;
    private boolean autoSortEnabled = true;
    private boolean removeResourcesFromHotbar = true;
    private boolean protectSelectedSlot = true;
    private boolean stackConsolidation = false;
    private boolean manualGrace = true;
    private boolean autoUpgradeSword = true;
    private boolean autoUpgradePickaxe = true;
    private boolean autoUpgradeAxe = true;
    private boolean resourceHud = true;
    private boolean statusHud = true;
    private int hudX = 6;
    private int hudY = 6;
    private float hudScale = 1.0F;
    private Profile profile = Profile.NORMAL;

    private QuickSlotConfig() {
        resetDefaults();
    }

    public static QuickSlotConfig get() {
        INSTANCE.ensureLoaded();
        return INSTANCE;
    }

    private void resetDefaults() {
        for (Profile p : Profile.values()) {
            rules.put(p, defaultRules(p));

            boolean[] enabled = new boolean[9];
            Arrays.fill(enabled, true);
            refillEnabled.put(p, enabled);

            RefillMode[] modes = new RefillMode[9];
            Arrays.fill(modes, DEFAULT_REFILL_MODE);
            refillModes.put(p, modes);

            int[] thresholds = new int[9];
            Arrays.fill(thresholds, DEFAULT_REFILL_THRESHOLD);
            refillThresholds.put(p, thresholds);

            blockPriority.put(p, Arrays.copyOf(DEFAULT_BLOCK_PRIORITY, DEFAULT_BLOCK_PRIORITY.length));
            preferSameBlock.put(p, true);
        }
    }

    private ItemRule[] defaultRules(Profile p) {
        return switch (p) {
            case NORMAL -> new ItemRule[]{ItemRule.SWORD, ItemRule.BLOCKS, ItemRule.PICKAXE, ItemRule.AXE, ItemRule.BOW, ItemRule.GOLDEN_APPLE, ItemRule.SHEARS, ItemRule.FREE, ItemRule.FREE};
            case RUSH -> new ItemRule[]{ItemRule.SWORD, ItemRule.BLOCKS, ItemRule.BLOCKS, ItemRule.PICKAXE, ItemRule.AXE, ItemRule.GOLDEN_APPLE, ItemRule.FIREBALL, ItemRule.ENDER_PEARL, ItemRule.FREE};
            case BRIDGE -> new ItemRule[]{ItemRule.BLOCKS, ItemRule.BLOCKS, ItemRule.SWORD, ItemRule.PICKAXE, ItemRule.AXE, ItemRule.GOLDEN_APPLE, ItemRule.LADDER, ItemRule.WATER, ItemRule.FREE};
        };
    }

    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        Path file = configFile();
        if (!Files.isRegularFile(file)) return;

        Properties properties = new Properties();
        boolean refillMigrationNeeded = false;
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
            String legacyEnabled = properties.getProperty("enabled", "true");
            autoSortEnabled = Boolean.parseBoolean(properties.getProperty("autoSortEnabled", legacyEnabled));
            removeResourcesFromHotbar = Boolean.parseBoolean(properties.getProperty("removeResourcesFromHotbar", "true"));
            protectSelectedSlot = Boolean.parseBoolean(properties.getProperty("protectSelectedSlot", "true"));
            stackConsolidation = Boolean.parseBoolean(properties.getProperty("stackConsolidation", "false"));
            manualGrace = Boolean.parseBoolean(properties.getProperty("manualGrace", "true"));
            autoUpgradeSword = Boolean.parseBoolean(properties.getProperty("autoUpgradeSword", "true"));
            autoUpgradePickaxe = Boolean.parseBoolean(properties.getProperty("autoUpgradePickaxe", "true"));
            autoUpgradeAxe = Boolean.parseBoolean(properties.getProperty("autoUpgradeAxe", "true"));
            resourceHud = Boolean.parseBoolean(properties.getProperty("resourceHud", "true"));
            statusHud = Boolean.parseBoolean(properties.getProperty("statusHud", "true"));
            hudX = parseInt(properties.getProperty("hudX"), 6);
            hudY = parseInt(properties.getProperty("hudY"), 6);
            hudScale = clamp(parseFloat(properties.getProperty("hudScale"), 1.0F), 0.5F, 3.0F);

            try {
                profile = Profile.valueOf(properties.getProperty("profile", Profile.NORMAL.name()));
            } catch (IllegalArgumentException ignored) {
                profile = Profile.NORMAL;
            }

            RefillMode legacyRefillMode = parseRefillMode(
                properties.getProperty("refillMode"),
                DEFAULT_REFILL_MODE
            );
            int legacyRefillThreshold = clamp(
                parseInt(properties.getProperty("refillThreshold"), DEFAULT_REFILL_THRESHOLD),
                1,
                64
            );

            for (Profile p : Profile.values()) {
                ItemRule[] profileRules = rules.get(p);
                boolean[] profileRefill = refillEnabled.get(p);
                RefillMode[] profileModes = refillModes.get(p);
                int[] profileThresholds = refillThresholds.get(p);

                for (int slot = 0; slot < 9; slot++) {
                    String value = properties.getProperty("profile." + p.name() + ".slot." + slot);
                    if (value != null) {
                        try {
                            profileRules[slot] = ItemRule.valueOf(value);
                        } catch (IllegalArgumentException ignored) {
                        }
                    }

                    profileRefill[slot] = Boolean.parseBoolean(
                        properties.getProperty("profile." + p.name() + ".refill." + slot, "true")
                    );

                    String modeKey = "profile." + p.name() + ".refillMode." + slot;
                    String thresholdKey = "profile." + p.name() + ".refillThreshold." + slot;
                    String modeValue = properties.getProperty(modeKey);
                    String thresholdValue = properties.getProperty(thresholdKey);
                    if (modeValue == null || thresholdValue == null) refillMigrationNeeded = true;

                    profileModes[slot] = parseRefillMode(modeValue, legacyRefillMode);
                    profileThresholds[slot] = clamp(
                        parseInt(thresholdValue, legacyRefillThreshold),
                        1,
                        64
                    );
                }

                preferSameBlock.put(
                    p,
                    Boolean.parseBoolean(properties.getProperty("profile." + p.name() + ".preferSameBlock", "true"))
                );
                loadBlockPriority(properties, p);
            }
        } catch (IOException ignored) {
            return;
        }

        if (refillMigrationNeeded) save();
    }

    private void loadBlockPriority(Properties properties, Profile p) {
        BlockType[] order = blockPriority.get(p);
        boolean[] used = new boolean[BlockType.values().length];

        for (int index = 0; index < DEFAULT_BLOCK_PRIORITY.length; index++) {
            String raw = properties.getProperty(
                "profile." + p.name() + ".blockPriority." + index,
                DEFAULT_BLOCK_PRIORITY[index].name()
            );
            BlockType candidate = parseBlockType(raw, DEFAULT_BLOCK_PRIORITY[index]);
            if (used[candidate.ordinal()]) candidate = firstUnusedBlockType(used);
            order[index] = candidate;
            used[candidate.ordinal()] = true;
        }
    }

    private static RefillMode parseRefillMode(String value, RefillMode fallback) {
        if (value == null) return fallback;
        try {
            return RefillMode.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static BlockType parseBlockType(String value, BlockType fallback) {
        if (value == null) return fallback;
        try {
            return BlockType.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private static BlockType firstUnusedBlockType(boolean[] used) {
        for (BlockType type : DEFAULT_BLOCK_PRIORITY) {
            if (!used[type.ordinal()]) return type;
        }
        return BlockType.OTHER;
    }

    public void save() {
        Properties properties = new Properties();
        properties.setProperty("autoSortEnabled", Boolean.toString(autoSortEnabled));
        properties.setProperty("removeResourcesFromHotbar", Boolean.toString(removeResourcesFromHotbar));
        properties.setProperty("protectSelectedSlot", Boolean.toString(protectSelectedSlot));
        properties.setProperty("stackConsolidation", Boolean.toString(stackConsolidation));
        properties.setProperty("manualGrace", Boolean.toString(manualGrace));
        properties.setProperty("autoUpgradeSword", Boolean.toString(autoUpgradeSword));
        properties.setProperty("autoUpgradePickaxe", Boolean.toString(autoUpgradePickaxe));
        properties.setProperty("autoUpgradeAxe", Boolean.toString(autoUpgradeAxe));
        properties.setProperty("resourceHud", Boolean.toString(resourceHud));
        properties.setProperty("statusHud", Boolean.toString(statusHud));
        properties.setProperty("hudX", Integer.toString(hudX));
        properties.setProperty("hudY", Integer.toString(hudY));
        properties.setProperty("hudScale", Float.toString(hudScale));
        properties.setProperty("profile", profile.name());

        properties.setProperty("refillMode", refillModes.get(Profile.NORMAL)[0].name());
        properties.setProperty("refillThreshold", Integer.toString(refillThresholds.get(Profile.NORMAL)[0]));

        for (Profile p : Profile.values()) {
            ItemRule[] profileRules = rules.get(p);
            boolean[] profileRefill = refillEnabled.get(p);
            RefillMode[] profileModes = refillModes.get(p);
            int[] profileThresholds = refillThresholds.get(p);
            BlockType[] priority = blockPriority.get(p);

            for (int slot = 0; slot < 9; slot++) {
                properties.setProperty("profile." + p.name() + ".slot." + slot, profileRules[slot].name());
                properties.setProperty("profile." + p.name() + ".refill." + slot, Boolean.toString(profileRefill[slot]));
                properties.setProperty("profile." + p.name() + ".refillMode." + slot, profileModes[slot].name());
                properties.setProperty("profile." + p.name() + ".refillThreshold." + slot, Integer.toString(profileThresholds[slot]));
            }

            properties.setProperty(
                "profile." + p.name() + ".preferSameBlock",
                Boolean.toString(preferSameBlock.getOrDefault(p, true))
            );
            for (int index = 0; index < priority.length; index++) {
                properties.setProperty(
                    "profile." + p.name() + ".blockPriority." + index,
                    priority[index].name()
                );
            }
        }

        Path file = configFile();
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                properties.store(output, "QuickSlot 1.7.0");
            }
        } catch (IOException ignored) {
        }
    }

    private Path configFile() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("quickslot.properties");
    }

    private static int parseInt(String value, int fallback) {
        if (value == null) return fallback;
        try { return Integer.parseInt(value); } catch (NumberFormatException ignored) { return fallback; }
    }

    private static float parseFloat(String value, float fallback) {
        if (value == null) return fallback;
        try { return Float.parseFloat(value); } catch (NumberFormatException ignored) { return fallback; }
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }

    public boolean autoSortEnabled() { return autoSortEnabled; }
    public void toggleAutoSort() { autoSortEnabled = !autoSortEnabled; save(); }
    public boolean removeResourcesFromHotbar() { return removeResourcesFromHotbar; }
    public void toggleRemoveResourcesFromHotbar() { removeResourcesFromHotbar = !removeResourcesFromHotbar; save(); }
    public boolean protectSelectedSlot() { return protectSelectedSlot; }
    public void toggleProtectSelectedSlot() { protectSelectedSlot = !protectSelectedSlot; save(); }
    public boolean stackConsolidation() { return stackConsolidation; }
    public void toggleStackConsolidation() { stackConsolidation = !stackConsolidation; save(); }
    public boolean manualGrace() { return manualGrace; }
    public void toggleManualGrace() { manualGrace = !manualGrace; save(); }
    public boolean resourceHud() { return resourceHud; }
    public void toggleResourceHud() { resourceHud = !resourceHud; save(); }
    public boolean statusHud() { return statusHud; }
    public void toggleStatusHud() { statusHud = !statusHud; save(); }
    public boolean autoUpgradeSword() { return autoUpgradeSword; }
    public void toggleAutoUpgradeSword() { autoUpgradeSword = !autoUpgradeSword; save(); }
    public boolean autoUpgradePickaxe() { return autoUpgradePickaxe; }
    public void toggleAutoUpgradePickaxe() { autoUpgradePickaxe = !autoUpgradePickaxe; save(); }
    public boolean autoUpgradeAxe() { return autoUpgradeAxe; }
    public void toggleAutoUpgradeAxe() { autoUpgradeAxe = !autoUpgradeAxe; save(); }
    public int hudX() { return hudX; }
    public int hudY() { return hudY; }
    public float hudScale() { return hudScale; }
    public void setHudPosition(int x, int y) { hudX = Math.max(0, x); hudY = Math.max(0, y); }
    public void setHudScale(float scale) { hudScale = clamp(scale, 0.5F, 3.0F); }
    public void resetHud() { hudX = 6; hudY = 6; hudScale = 1.0F; save(); }
    public Profile profile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; save(); }
    public void nextProfile() { profile = profile.next(); save(); }

    public boolean isRefillEnabled(int slot) {
        return isRefillEnabled(profile, slot);
    }

    public boolean isRefillEnabled(Profile profile, int slot) {
        return profile != null && slot >= 0 && slot < 9 && refillEnabled.get(profile)[slot];
    }

    public void toggleRefillEnabled(int slot) {
        if (slot < 0 || slot >= 9) return;
        boolean[] profileRefill = refillEnabled.get(profile);
        profileRefill[slot] = !profileRefill[slot];
        save();
    }

    public RefillMode refillMode(int slot) {
        return refillMode(profile, slot);
    }

    public RefillMode refillMode(Profile profile, int slot) {
        if (profile == null || slot < 0 || slot >= 9) return DEFAULT_REFILL_MODE;
        return refillModes.get(profile)[slot];
    }

    public void setRefillMode(int slot, RefillMode mode) {
        if (slot < 0 || slot >= 9 || mode == null) return;
        refillModes.get(profile)[slot] = mode;
        save();
    }

    public void nextRefillMode(int slot) {
        setRefillMode(slot, refillMode(slot).next());
    }

    public int refillThreshold(int slot) {
        return refillThreshold(profile, slot);
    }

    public int refillThreshold(Profile profile, int slot) {
        if (profile == null || slot < 0 || slot >= 9) return DEFAULT_REFILL_THRESHOLD;
        return refillThresholds.get(profile)[slot];
    }

    public void setRefillThreshold(int slot, int threshold) {
        if (slot < 0 || slot >= 9) return;
        refillThresholds.get(profile)[slot] = clamp(threshold, 1, 64);
        save();
    }

    public RefillMode refillMode() { return refillMode(0); }
    public int refillThreshold() { return refillThreshold(0); }

    public void nextRefillMode() {
        RefillMode next = refillMode(0).next();
        Arrays.fill(refillModes.get(profile), next);
        save();
    }

    public void setRefillThreshold(int threshold) {
        Arrays.fill(refillThresholds.get(profile), clamp(threshold, 1, 64));
        save();
    }

    public boolean preferSameBlock() { return preferSameBlock.getOrDefault(profile, true); }
    public void togglePreferSameBlock() {
        preferSameBlock.put(profile, !preferSameBlock());
        save();
    }
    public BlockType blockPriority(int index) {
        BlockType[] order = blockPriority.get(profile);
        if (index < 0 || index >= order.length) return BlockType.OTHER;
        return order[index];
    }
    public int blockPriorityRank(BlockType type) {
        BlockType[] order = blockPriority.get(profile);
        for (int index = 0; index < order.length; index++) {
            if (order[index] == type) return index;
        }
        return order.length;
    }
    public void moveBlockPriority(int index, int direction) {
        BlockType[] order = blockPriority.get(profile);
        int target = index + direction;
        if (index < 0 || index >= order.length || target < 0 || target >= order.length) return;
        BlockType current = order[index];
        order[index] = order[target];
        order[target] = current;
        save();
    }
    public void resetBlockPriority() {
        blockPriority.put(profile, Arrays.copyOf(DEFAULT_BLOCK_PRIORITY, DEFAULT_BLOCK_PRIORITY.length));
        preferSameBlock.put(profile, true);
        save();
    }
    public boolean autoUpgrade(ItemRule rule) {
        return switch (rule) {
            case SWORD -> autoUpgradeSword;
            case PICKAXE -> autoUpgradePickaxe;
            case AXE -> autoUpgradeAxe;
            default -> false;
        };
    }

    public void copyActiveProfileTo(Profile target) {
        copyProfile(profile, target);
    }

    public void copyProfile(Profile source, Profile target) {
        if (source == null || target == null || source == target) return;
        System.arraycopy(rules.get(source), 0, rules.get(target), 0, 9);
        System.arraycopy(refillEnabled.get(source), 0, refillEnabled.get(target), 0, 9);
        System.arraycopy(refillModes.get(source), 0, refillModes.get(target), 0, 9);
        System.arraycopy(refillThresholds.get(source), 0, refillThresholds.get(target), 0, 9);
        System.arraycopy(blockPriority.get(source), 0, blockPriority.get(target), 0, DEFAULT_BLOCK_PRIORITY.length);
        preferSameBlock.put(target, preferSameBlock.getOrDefault(source, true));
        save();
    }

    public void resetActiveProfile() {
        ItemRule[] defaults = defaultRules(profile);
        System.arraycopy(defaults, 0, rules.get(profile), 0, 9);
        Arrays.fill(refillEnabled.get(profile), true);
        Arrays.fill(refillModes.get(profile), DEFAULT_REFILL_MODE);
        Arrays.fill(refillThresholds.get(profile), DEFAULT_REFILL_THRESHOLD);
        System.arraycopy(DEFAULT_BLOCK_PRIORITY, 0, blockPriority.get(profile), 0, DEFAULT_BLOCK_PRIORITY.length);
        preferSameBlock.put(profile, true);
        save();
    }

    public ItemRule rule(int slot) { return rules.get(profile)[slot]; }
    public void cycleRule(int slot) { rules.get(profile)[slot] = rule(slot).next(); save(); }
}
