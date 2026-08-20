package dev.netrox.quickslot;

public enum Profile {
    NORMAL("Normal"),
    RUSH("Rush"),
    BRIDGE("Bridge");

    private final String displayName;

    Profile(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public Profile next() {
        Profile[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
