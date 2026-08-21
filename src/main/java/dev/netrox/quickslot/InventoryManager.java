package dev.netrox.quickslot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;

public final class InventoryManager {
    private static final ItemStack[] LAST_PREFERRED_STACKS = new ItemStack[9];
    private static final InventoryActionQueue ACTION_QUEUE = new InventoryActionQueue();
    private static final int MANUAL_GRACE_CYCLES = 3;
    private static final int CURSOR_RELEASE_GRACE_CYCLES = 2;

    private static int manualGraceCycles;
    private static boolean wasContainerOpen;
    private static boolean cursorWasOccupied;
    private static Profile rememberedProfile;
    private static Object rememberedPlayer;
    private static Object rememberedLevel;
    private static int rememberedContainerId = -1;

    private InventoryManager() {}

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.gameMode == null || minecraft.level == null) {
            resetRuntimeState();
            return;
        }

        QuickSlotConfig config = QuickSlotConfig.get();
        int containerId = minecraft.player.inventoryMenu.containerId;
        resetOnContextChange(minecraft, config, containerId);

        if (!config.autoSortEnabled() && !config.removeResourcesFromHotbar() && !config.stackConsolidation()) {
            ACTION_QUEUE.clear();
            return;
        }

        boolean containerOpen = minecraft.screen instanceof AbstractContainerScreen<?>;
        if (containerOpen) wasContainerOpen = true;

        boolean cursorOccupied = !minecraft.player.containerMenu.getCarried().isEmpty();
        if (cursorOccupied) {
            cursorWasOccupied = true;
            ACTION_QUEUE.cancelActions();
            return;
        }

        if (cursorWasOccupied) {
            cursorWasOccupied = false;
            ACTION_QUEUE.cancelActions();
            int grace = config.manualGrace() ? MANUAL_GRACE_CYCLES : CURSOR_RELEASE_GRACE_CYCLES;
            manualGraceCycles = Math.max(manualGraceCycles, grace);
        }

        if (containerOpen) {
            ACTION_QUEUE.cancelActions();
            return;
        }
        if (minecraft.screen != null) {
            ACTION_QUEUE.cancelActions();
            return;
        }

        if (wasContainerOpen) {
            wasContainerOpen = false;
            ACTION_QUEUE.cancelActions();
            manualGraceCycles = Math.max(manualGraceCycles, config.manualGrace() ? MANUAL_GRACE_CYCLES : 0);
        }
        if (manualGraceCycles > 0) {
            manualGraceCycles--;
            return;
        }

        Inventory inventory = minecraft.player.getInventory();
        ACTION_QUEUE.tick(minecraft, inventory, containerId);
        if (ACTION_QUEUE.isBusy()) return;

        rememberPreferredStacks(inventory, config);
        int protectedSlot = config.protectSelectedSlot() ? inventory.getSelectedSlot() : -1;

        InventoryAction planned = null;
        if (config.removeResourcesFromHotbar()) {
            planned = planResourceMove(inventory, containerId, protectedSlot);
        }
        if (planned == null && config.autoSortEnabled()) {
            planned = planOrganizeOneSlot(inventory, config, containerId, protectedSlot);
        }
        if (planned == null && config.stackConsolidation()) {
            planned = planConsolidation(inventory, containerId);
        }

        if (planned != null && ACTION_QUEUE.enqueue(planned)) {
            ACTION_QUEUE.tick(minecraft, inventory, containerId);
        }
    }

    private static void resetOnContextChange(Minecraft minecraft, QuickSlotConfig config, int containerId) {
        boolean changed = rememberedPlayer != minecraft.player
            || rememberedLevel != minecraft.level
            || rememberedContainerId != containerId;

        if (rememberedProfile != config.profile()) {
            rememberedProfile = config.profile();
            Arrays.fill(LAST_PREFERRED_STACKS, ItemStack.EMPTY);
            changed = true;
        }

        if (changed) {
            ACTION_QUEUE.clear();
            manualGraceCycles = 0;
            wasContainerOpen = false;
            cursorWasOccupied = false;
            rememberedPlayer = minecraft.player;
            rememberedLevel = minecraft.level;
            rememberedContainerId = containerId;
        }
    }

    private static void resetRuntimeState() {
        ACTION_QUEUE.clear();
        Arrays.fill(LAST_PREFERRED_STACKS, ItemStack.EMPTY);
        manualGraceCycles = 0;
        wasContainerOpen = false;
        cursorWasOccupied = false;
        rememberedProfile = null;
        rememberedPlayer = null;
        rememberedLevel = null;
        rememberedContainerId = -1;
    }

    private static InventoryAction planResourceMove(Inventory inventory, int containerId, int protectedSlot) {
        for (int hotbar = 0; hotbar < 9; hotbar++) {
            if (hotbar == protectedSlot) continue;
            ItemStack stack = inventory.getItem(hotbar);
            if (!isResource(stack) || !canMoveToMain(inventory, stack)) continue;
            return InventoryAction.quickMove(inventory, containerId, hotbar);
        }
        return null;
    }

    private static InventoryAction planOrganizeOneSlot(
        Inventory inventory,
        QuickSlotConfig config,
        int containerId,
        int protectedSlot
    ) {
        for (int target = 0; target < 9; target++) {
            if (target == protectedSlot) continue;

            ItemRule rule = config.rule(target);
            ItemStack current = inventory.getItem(target);

            if (rule == ItemRule.FREE) continue;

            if (rule == ItemRule.EMPTY) {
                if (!current.isEmpty() && canMoveToMain(inventory, current)) {
                    return InventoryAction.quickMove(inventory, containerId, target);
                }
                continue;
            }

            if (rule.matches(current)) {
                if (rule == ItemRule.BLOCKS && !config.preferSameBlock()) {
                    Source preferredSource = findBestSource(inventory, config, rule, target, -1, protectedSlot);
                    if (preferredSource != null) {
                        ItemStack preferredBlock = sourceStack(inventory, preferredSource);
                        if (blockRank(preferredBlock, config) < blockRank(current, config)) {
                            return InventoryAction.swap(inventory, containerId, preferredSource.containerSlot(), target);
                        }
                    }
                }

                if (isUpgradeable(rule) && config.autoUpgrade(rule)) {
                    int currentPriority = rule.priority(current);
                    Source source = findBestSource(inventory, config, rule, target, currentPriority, protectedSlot);
                    if (source != null) {
                        return InventoryAction.swap(inventory, containerId, source.containerSlot(), target);
                    }
                } else if (!isUpgradeable(rule) && config.isRefillEnabled(target) && shouldRefill(current, config, target)) {
                    int mergeSource = findMergeSource(inventory, current);
                    if (mergeSource >= 0) {
                        return InventoryAction.merge(inventory, containerId, mergeSource, 36 + target);
                    }
                }
                continue;
            }

            if (current.isEmpty() && !config.isRefillEnabled(target)) continue;

            Source source = findBestSource(inventory, config, rule, target, -1, protectedSlot);
            if (source != null) {
                return InventoryAction.swap(inventory, containerId, source.containerSlot(), target);
            }
        }
        return null;
    }

    private static InventoryAction planConsolidation(Inventory inventory, int containerId) {
        int bestTarget = -1;
        int bestSource = -1;
        int bestTargetCount = -1;

        for (int target = 9; target <= 35; target++) {
            ItemStack targetStack = inventory.getItem(target);
            if (targetStack.isEmpty() || !targetStack.isStackable() || targetStack.getCount() >= targetStack.getMaxStackSize()) continue;

            for (int source = 9; source <= 35; source++) {
                if (source == target) continue;
                ItemStack sourceStack = inventory.getItem(source);
                if (sourceStack.isEmpty() || !ItemStack.isSameItemSameComponents(sourceStack, targetStack)) continue;

                if (targetStack.getCount() > bestTargetCount) {
                    bestTargetCount = targetStack.getCount();
                    bestTarget = target;
                    bestSource = source;
                }
            }
        }

        if (bestTarget < 0 || bestSource < 0) return null;
        return InventoryAction.merge(inventory, containerId, bestSource, bestTarget);
    }

    private static ItemStack sourceStack(Inventory inventory, Source source) {
        int containerSlot = source.containerSlot();
        return containerSlot >= 36 ? inventory.getItem(containerSlot - 36) : inventory.getItem(containerSlot);
    }

    private static void rememberPreferredStacks(Inventory inventory, QuickSlotConfig config) {
        for (int slot = 0; slot < 9; slot++) {
            ItemRule rule = config.rule(slot);
            ItemStack current = inventory.getItem(slot);
            if (!rule.matches(current)) continue;

            ItemStack remembered = current.copy();
            remembered.setCount(1);
            LAST_PREFERRED_STACKS[slot] = remembered;
        }
    }

    private static boolean shouldRefill(ItemStack current, QuickSlotConfig config, int hotbarSlot) {
        if (current.isEmpty() || !current.isStackable() || current.getCount() >= current.getMaxStackSize()) return false;
        return switch (config.refillMode(hotbarSlot)) {
            case EMPTY_ONLY -> false;
            case BELOW_THRESHOLD -> current.getCount() < Math.min(config.refillThreshold(hotbarSlot), current.getMaxStackSize());
            case ALWAYS_MAX -> true;
        };
    }

    private static boolean isUpgradeable(ItemRule rule) {
        return rule == ItemRule.SWORD || rule == ItemRule.PICKAXE || rule == ItemRule.AXE;
    }

    private static Source findBestSource(
        Inventory inventory,
        QuickSlotConfig config,
        ItemRule rule,
        int target,
        int currentPriority,
        int protectedSlot
    ) {
        Source best = null;
        int bestPriority = currentPriority;
        ItemStack preferred = rule == ItemRule.BLOCKS ? LAST_PREFERRED_STACKS[target] : ItemStack.EMPTY;

        for (int slot = 9; slot <= 35; slot++) {
            ItemStack stack = inventory.getItem(slot);
            int priority = adjustedPriority(config, rule, stack, preferred);
            if (priority > bestPriority) {
                bestPriority = priority;
                best = new Source(slot);
            }
        }

        for (int slot = 0; slot < 9; slot++) {
            if (slot == target || slot == protectedSlot) continue;

            ItemStack stack = inventory.getItem(slot);
            int priority = adjustedPriority(config, rule, stack, preferred);
            if (priority <= bestPriority) continue;

            ItemRule sourceRule = config.rule(slot);
            if (sourceRule != ItemRule.FREE && sourceRule != ItemRule.EMPTY && sourceRule.matches(stack)) continue;

            bestPriority = priority;
            best = new Source(36 + slot);
        }

        return best;
    }

    private static int adjustedPriority(QuickSlotConfig config, ItemRule rule, ItemStack stack, ItemStack preferred) {
        int priority = rule.priority(stack);
        if (priority < 0) return priority;
        if (rule != ItemRule.BLOCKS) return priority;

        int rank = blockRank(stack, config);
        int blockPriority = (BlockType.values().length - rank) * 10_000 + stack.getCount();
        if (config.preferSameBlock() && !preferred.isEmpty() && ItemStack.isSameItemSameComponents(stack, preferred)) {
            blockPriority += 1_000_000;
        }
        return blockPriority;
    }

    private static int blockRank(ItemStack stack, QuickSlotConfig config) {
        return config.blockPriorityRank(BlockType.fromStack(stack));
    }

    private static int findMergeSource(Inventory inventory, ItemStack target) {
        int bestSlot = -1;
        int bestCount = -1;
        for (int slot = 9; slot <= 35; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, target)) continue;
            if (stack.getCount() > bestCount) {
                bestCount = stack.getCount();
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    private static boolean canMoveToMain(Inventory inventory, ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (int slot = 9; slot <= 35; slot++) {
            ItemStack target = inventory.getItem(slot);
            if (target.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(target, stack) && target.getCount() < target.getMaxStackSize()) return true;
        }
        return false;
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

    private record Source(int containerSlot) {}
}
