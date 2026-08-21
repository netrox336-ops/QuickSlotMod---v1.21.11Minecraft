package dev.netrox.quickslot;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

final class InventoryAction {
    enum Kind {
        QUICK_MOVE,
        SWAP,
        MERGE
    }

    private final Kind kind;
    private final int containerId;
    private final int sourceSlot;
    private final int targetSlot;
    private final int hotbarButton;
    private final ItemStack sourceBefore;
    private final ItemStack targetBefore;
    private final int expectedSourceCount;
    private final int expectedTargetCount;

    private InventoryAction(
        Kind kind,
        int containerId,
        int sourceSlot,
        int targetSlot,
        int hotbarButton,
        ItemStack sourceBefore,
        ItemStack targetBefore,
        int expectedSourceCount,
        int expectedTargetCount
    ) {
        this.kind = kind;
        this.containerId = containerId;
        this.sourceSlot = sourceSlot;
        this.targetSlot = targetSlot;
        this.hotbarButton = hotbarButton;
        this.sourceBefore = sourceBefore.copy();
        this.targetBefore = targetBefore.copy();
        this.expectedSourceCount = expectedSourceCount;
        this.expectedTargetCount = expectedTargetCount;
    }

    static InventoryAction quickMove(Inventory inventory, int containerId, int hotbarSlot) {
        int source = 36 + hotbarSlot;
        return new InventoryAction(
            Kind.QUICK_MOVE,
            containerId,
            source,
            -1,
            0,
            stackAt(inventory, source),
            ItemStack.EMPTY,
            -1,
            -1
        );
    }

    static InventoryAction swap(Inventory inventory, int containerId, int sourceSlot, int hotbarSlot) {
        int target = 36 + hotbarSlot;
        return new InventoryAction(
            Kind.SWAP,
            containerId,
            sourceSlot,
            target,
            hotbarSlot,
            stackAt(inventory, sourceSlot),
            stackAt(inventory, target),
            -1,
            -1
        );
    }

    static InventoryAction merge(Inventory inventory, int containerId, int sourceSlot, int targetSlot) {
        ItemStack source = stackAt(inventory, sourceSlot);
        ItemStack target = stackAt(inventory, targetSlot);
        int total = source.getCount() + target.getCount();
        int targetCount = Math.min(target.getMaxStackSize(), total);
        int sourceCount = Math.max(0, total - targetCount);
        return new InventoryAction(
            Kind.MERGE,
            containerId,
            sourceSlot,
            targetSlot,
            0,
            source,
            target,
            sourceCount,
            targetCount
        );
    }

    int containerId() {
        return containerId;
    }

    String key() {
        return kind.name() + ':' + sourceSlot + ':' + targetSlot + ':' + hotbarButton;
    }

    boolean isStillValid(Inventory inventory) {
        if (!snapshotMatches(sourceBefore, stackAt(inventory, sourceSlot))) return false;
        return targetSlot < 0 || snapshotMatches(targetBefore, stackAt(inventory, targetSlot));
    }

    void execute(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.gameMode == null) return;

        switch (kind) {
            case QUICK_MOVE -> minecraft.gameMode.handleInventoryMouseClick(
                containerId, sourceSlot, 0, ClickType.QUICK_MOVE, minecraft.player
            );
            case SWAP -> minecraft.gameMode.handleInventoryMouseClick(
                containerId, sourceSlot, hotbarButton, ClickType.SWAP, minecraft.player
            );
            case MERGE -> {
                minecraft.gameMode.handleInventoryMouseClick(containerId, sourceSlot, 0, ClickType.PICKUP, minecraft.player);
                minecraft.gameMode.handleInventoryMouseClick(containerId, targetSlot, 0, ClickType.PICKUP, minecraft.player);
                minecraft.gameMode.handleInventoryMouseClick(containerId, sourceSlot, 0, ClickType.PICKUP, minecraft.player);
            }
        }
    }

    boolean isConfirmed(Inventory inventory) {
        return switch (kind) {
            case QUICK_MOVE -> !snapshotMatches(sourceBefore, stackAt(inventory, sourceSlot));
            case SWAP -> snapshotMatches(sourceBefore, stackAt(inventory, targetSlot))
                && snapshotMatches(targetBefore, stackAt(inventory, sourceSlot));
            case MERGE -> mergeConfirmed(inventory);
        };
    }

    private boolean mergeConfirmed(Inventory inventory) {
        ItemStack source = stackAt(inventory, sourceSlot);
        ItemStack target = stackAt(inventory, targetSlot);

        boolean targetOk = expectedTargetCount == 0
            ? target.isEmpty()
            : sameKind(targetBefore, target) && target.getCount() == expectedTargetCount;
        boolean sourceOk = expectedSourceCount == 0
            ? source.isEmpty()
            : sameKind(sourceBefore, source) && source.getCount() == expectedSourceCount;
        return sourceOk && targetOk;
    }

    private static ItemStack stackAt(Inventory inventory, int containerSlot) {
        int inventorySlot = inventorySlot(containerSlot);
        if (inventorySlot < 0 || inventorySlot >= inventory.getContainerSize()) return ItemStack.EMPTY;
        return inventory.getItem(inventorySlot);
    }

    private static int inventorySlot(int containerSlot) {
        if (containerSlot >= 36 && containerSlot <= 44) return containerSlot - 36;
        if (containerSlot >= 9 && containerSlot <= 35) return containerSlot;
        return -1;
    }

    private static boolean snapshotMatches(ItemStack expected, ItemStack actual) {
        if (expected.isEmpty()) return actual.isEmpty();
        return !actual.isEmpty()
            && ItemStack.isSameItemSameComponents(expected, actual)
            && expected.getCount() == actual.getCount();
    }

    private static boolean sameKind(ItemStack expected, ItemStack actual) {
        return !expected.isEmpty()
            && !actual.isEmpty()
            && ItemStack.isSameItemSameComponents(expected, actual);
    }
}
