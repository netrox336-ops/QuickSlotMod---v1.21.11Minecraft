package dev.netrox.quickslot;

public enum RefillMode {
    EMPTY_ONLY("Когда пусто"),
    BELOW_THRESHOLD("Ниже порога"),
    ALWAYS_MAX("Держать максимум");

    private final String displayName;

    RefillMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public RefillMode next() {
        RefillMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
