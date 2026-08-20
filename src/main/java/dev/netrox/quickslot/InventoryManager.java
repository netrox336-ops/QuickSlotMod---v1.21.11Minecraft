package dev.netrox.quickslot;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class InventoryManager {
    private InventoryManager() {}

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.gameMode == null || minecraft.screen != null) return;

        QuickSlotConfig config = QuickSlotConfig.get();
        if (!config.autoSortEnabled() && !config.removeResourcesFromHotbar()) return;

        Inventory inventory = minecraft.player.getInventory();

        if (config.removeResourcesFromHotbar()) {
            for (int hotbar = 0; hotbar < 9; hotbar++) {
                ItemStack stack = inventory.getItem(hotbar);
                if (!isResource(stack) || !canMoveToMain(inventory, stack)) continue;
                quickMove(minecraft, hotbar);
                return;
            }
        }

        if (!config.autoSortEnabled()) return;

        for (int target = 0; target < 9; target++) {
            ItemRule rule = config.rule(target);
            ItemStack current = inventory.getItem(target);

            if (rule == ItemRule.FREE) continue;

            if (rule == ItemRule.EMPTY) {
                if (current.isEmpty()) continue;
                if (canMoveToMain(inventory, current)) {
                    quickMove(minecraft, target);
                    return;
                }
                continue;
            }

            int currentPriority = rule.matches(current) ? rule.priority(current) : -1;
            int source = findBestMatchingMainSlot(inventory, rule, currentPriority);
            if (source >= 0) {
                swap(minecraft, source, target);
                return;
            }

            source = findBestMatchingHotbarSlot(inventory, config, rule, target, currentPriority);
            if (source >= 0) {
                swap(minecraft, 36 + source, target);
                return;
            }
        }
    }

    private static int findBestMatchingMainSlot(Inventory inventory, ItemRule rule, int currentPriority) {
        int bestSlot = -1;
        int bestPriority = currentPriority;
        for (int slot = 9; slot <= 35; slot++) {
            ItemStack stack = inventory.getItem(slot);
            int priority = rule.priority(stack);
            if (priority > bestPriority) {
                bestPriority = priority;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    private static int findBestMatchingHotbarSlot(Inventory inventory, QuickSlotConfig config, ItemRule rule, int target, int currentPriority) {
        int bestSlot = -1;
        int bestPriority = currentPriority;
        for (int slot = 0; slot < 9; slot++) {
            if (slot == target) continue;

            ItemStack stack = inventory.getItem(slot);
            int priority = rule.priority(stack);
            if (priority <= bestPriority) continue;

            ItemRule sourceRule = config.rule(slot);
            if (sourceRule != ItemRule.FREE && sourceRule != ItemRule.EMPTY && sourceRule.matches(stack)) continue;

            bestPriority = priority;
            bestSlot = slot;
        }
        return bestSlot;
    }

    private static boolean canMoveToMain(Inventory inventory, ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (int slot = 9; slot <= 35; slot++) {
            ItemStack target = inventory.getItem(slot);
            if (target.isEmpty()) return true;
            if (target.getItem() == stack.getItem() && target.getCount() < target.getMaxStackSize()) return true;
        }
        return false;
    }

    private static void quickMove(Minecraft minecraft, int hotbarSlot) {
        minecraft.gameMode.handleInventoryMouseClick(
            minecraft.player.inventoryMenu.containerId,
            36 + hotbarSlot,
            0,
            ClickType.QUICK_MOVE,
            minecraft.player
        );
    }

    private static void swap(Minecraft minecraft, int containerSlot, int hotbarSlot) {
        minecraft.gameMode.handleInventoryMouseClick(
            minecraft.player.inventoryMenu.containerId,
            containerSlot,
            hotbarSlot,
            ClickType.SWAP,
            minecraft.player
        );
    }

    public static boolean isResource(ItemStack stack) {
        return stack.is(Items.IRON_INGOT) || stack.is(Items.GOLD_INGOT) || stack.is(Items.DIAMOND) || stack.is(Items.EMERALD);
    }

    public static int count(Inventory inventory, net.minecraft.world.item.Item item) {
        int total = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }
}
