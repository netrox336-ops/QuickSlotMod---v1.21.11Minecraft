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
        if (!config.enabled()) return;

        Inventory inventory = minecraft.player.getInventory();

        for (int hotbar = 0; hotbar < 9; hotbar++) {
            if (!isResource(inventory.getItem(hotbar))) continue;
            int empty = findEmptyMainSlot(inventory);
            if (empty >= 0) {
                swap(minecraft, empty, hotbar);
                return;
            }
        }

        for (int target = 0; target < 9; target++) {
            ItemRule rule = config.rule(target);
            if (rule == ItemRule.FREE || rule.matches(inventory.getItem(target))) continue;

            int source = findMatchingMainSlot(inventory, rule);
            if (source >= 0) {
                swap(minecraft, source, target);
                return;
            }

            source = findMatchingHotbarSlot(inventory, config, rule, target);
            if (source >= 0) {
                swap(minecraft, 36 + source, target);
                return;
            }
        }
    }

    private static int findEmptyMainSlot(Inventory inventory) {
        for (int slot = 9; slot <= 35; slot++) {
            if (inventory.getItem(slot).isEmpty()) return slot;
        }
        return -1;
    }

    private static int findMatchingMainSlot(Inventory inventory, ItemRule rule) {
        for (int slot = 9; slot <= 35; slot++) {
            if (rule.matches(inventory.getItem(slot))) return slot;
        }
        return -1;
    }

    private static int findMatchingHotbarSlot(Inventory inventory, QuickSlotConfig config, ItemRule rule, int target) {
        for (int slot = 0; slot < 9; slot++) {
            if (slot == target || !rule.matches(inventory.getItem(slot))) continue;
            ItemRule sourceRule = config.rule(slot);
            if (sourceRule == ItemRule.FREE || !sourceRule.matches(inventory.getItem(slot))) return slot;
        }
        return -1;
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
