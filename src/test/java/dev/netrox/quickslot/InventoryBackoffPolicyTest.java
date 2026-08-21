package dev.netrox.quickslot;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class InventoryBackoffPolicyTest {
    @Test
    public void failureCooldownGrowsAndStopsAtMaximum() {
        InventoryBackoffPolicy policy = new InventoryBackoffPolicy();

        assertEquals(4, policy.onFailure());
        assertEquals(8, policy.onFailure());
        assertEquals(16, policy.onFailure());
        assertEquals(32, policy.onFailure());
        assertEquals(32, policy.onFailure());
    }

    @Test
    public void threeSuccessfulActionsReduceFailureLevel() {
        InventoryBackoffPolicy policy = new InventoryBackoffPolicy();
        policy.onFailure();
        policy.onFailure();

        policy.onSuccess();
        policy.onSuccess();
        assertEquals(2, policy.getConsecutiveFailures());

        policy.onSuccess();
        assertEquals(1, policy.getConsecutiveFailures());
        assertEquals(4, policy.getFailureCooldownCycles());
    }

    @Test
    public void resetClearsFailureHistory() {
        InventoryBackoffPolicy policy = new InventoryBackoffPolicy();
        policy.onFailure();
        policy.onFailure();
        policy.reset();

        assertEquals(0, policy.getConsecutiveFailures());
        assertEquals(0, policy.getFailureCooldownCycles());
        assertEquals(4, policy.onFailure());
    }
}
