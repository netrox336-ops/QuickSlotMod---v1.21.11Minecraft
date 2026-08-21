package dev.netrox.quickslot;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayDeque;
import java.util.Deque;

final class InventoryActionQueue {
    private static final int CONFIRM_STABLE_CYCLES = 2;
    private static final int ACTION_TIMEOUT_CYCLES = 8;
    private static final int SUCCESS_COOLDOWN_CYCLES = 1;
    private static final int MAX_PENDING = 8;

    private final Deque<InventoryAction> pending = new ArrayDeque<>();
    private final InventoryBackoffPolicy backoffPolicy = new InventoryBackoffPolicy();
    private InventoryAction active;
    private int activeCycles;
    private int stableCycles;
    private int cooldownCycles;

    boolean enqueue(InventoryAction action) {
        if (action == null) return false;
        if (active != null && active.key().equals(action.key())) return false;
        for (InventoryAction queued : pending) {
            if (queued.key().equals(action.key())) return false;
        }
        if (pending.size() >= MAX_PENDING) return false;
        pending.addLast(action);
        return true;
    }

    void tick(Minecraft minecraft, Inventory inventory, int containerId) {
        if (minecraft == null || inventory == null) {
            clear();
            return;
        }

        if (active != null) {
            if (active.containerId() != containerId) {
                failCurrent();
                return;
            }

            activeCycles++;
            if (active.isConfirmed(inventory)) {
                stableCycles++;
                if (stableCycles >= CONFIRM_STABLE_CYCLES) {
                    active = null;
                    activeCycles = 0;
                    stableCycles = 0;
                    backoffPolicy.onSuccess();
                    cooldownCycles = SUCCESS_COOLDOWN_CYCLES;
                }
            } else {
                stableCycles = 0;
            }

            if (active != null && activeCycles >= ACTION_TIMEOUT_CYCLES) {
                failCurrent();
            }
            return;
        }

        if (cooldownCycles > 0) {
            cooldownCycles--;
            return;
        }

        while (!pending.isEmpty()) {
            InventoryAction next = pending.removeFirst();
            if (next.containerId() != containerId || !next.isStillValid(inventory)) continue;

            active = next;
            activeCycles = 0;
            stableCycles = 0;
            try {
                next.execute(minecraft);
            } catch (RuntimeException ignored) {
                failCurrent();
            }
            return;
        }
    }

    boolean canPlan() {
        return active == null && pending.isEmpty() && cooldownCycles == 0;
    }

    boolean isBusy() {
        return active != null || !pending.isEmpty() || cooldownCycles > 0;
    }

    int getConsecutiveFailures() {
        return backoffPolicy.getConsecutiveFailures();
    }

    void cancelActions() {
        pending.clear();
        active = null;
        activeCycles = 0;
        stableCycles = 0;
        cooldownCycles = 0;
    }

    void clear() {
        cancelActions();
        backoffPolicy.reset();
    }

    private void failCurrent() {
        pending.clear();
        active = null;
        activeCycles = 0;
        stableCycles = 0;
        cooldownCycles = backoffPolicy.onFailure();
    }
}
