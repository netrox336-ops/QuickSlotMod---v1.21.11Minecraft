package dev.netrox.quickslot;

final class InventoryBackoffPolicy {
    static final int BASE_FAILURE_COOLDOWN_CYCLES = 4;
    static final int MAX_FAILURE_COOLDOWN_CYCLES = 32;
    static final int SUCCESSES_TO_RECOVER = 3;

    private int consecutiveFailures;
    private int successfulActions;

    int onFailure() {
        consecutiveFailures = Math.min(consecutiveFailures + 1, 8);
        successfulActions = 0;
        return getFailureCooldownCycles();
    }

    void onSuccess() {
        if (consecutiveFailures <= 0) return;

        successfulActions++;
        if (successfulActions >= SUCCESSES_TO_RECOVER) {
            consecutiveFailures--;
            successfulActions = 0;
        }
    }

    int getFailureCooldownCycles() {
        if (consecutiveFailures <= 0) return 0;

        int shift = Math.min(consecutiveFailures - 1, 3);
        return Math.min(MAX_FAILURE_COOLDOWN_CYCLES, BASE_FAILURE_COOLDOWN_CYCLES << shift);
    }

    int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    void reset() {
        consecutiveFailures = 0;
        successfulActions = 0;
    }
}
