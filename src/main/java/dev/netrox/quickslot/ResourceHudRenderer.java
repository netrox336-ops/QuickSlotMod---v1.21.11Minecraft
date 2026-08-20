package dev.netrox.quickslot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

public final class ResourceHudRenderer {
    private ResourceHudRenderer() {}

    public static void render(GuiGraphics graphics, Inventory inventory, QuickSlotConfig config) {
        renderInternal(
            graphics,
            config.hudX(),
            config.hudY(),
            config.hudScale(),
            config.resourceHud(),
            config.statusHud(),
            config.profile(),
            config.autoSortEnabled(),
            InventoryManager.count(inventory, Items.IRON_INGOT),
            InventoryManager.count(inventory, Items.GOLD_INGOT),
            InventoryManager.count(inventory, Items.DIAMOND),
            InventoryManager.count(inventory, Items.EMERALD)
        );
    }

    public static void renderPreview(GuiGraphics graphics, QuickSlotConfig config) {
        renderInternal(
            graphics,
            config.hudX(),
            config.hudY(),
            config.hudScale(),
            config.resourceHud(),
            config.statusHud(),
            config.profile(),
            config.autoSortEnabled(),
            32,
            8,
            4,
            2
        );
    }

    private static void renderInternal(
        GuiGraphics graphics,
        int x,
        int y,
        float scale,
        boolean showResources,
        boolean showStatus,
        Profile profile,
        boolean autoSort,
        int iron,
        int gold,
        int diamond,
        int emerald
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        int line = minecraft.font.lineHeight + 2;

        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);

        int cursorY = 0;
        if (showResources) {
            graphics.drawString(minecraft.font, "Iron: " + iron, 0, cursorY, 0xFFFFFF, true);
            graphics.drawString(minecraft.font, "Gold: " + gold, 0, cursorY + line, 0xFFFFFF, true);
            graphics.drawString(minecraft.font, "Diamond: " + diamond, 0, cursorY + line * 2, 0xFFFFFF, true);
            graphics.drawString(minecraft.font, "Emerald: " + emerald, 0, cursorY + line * 3, 0xFFFFFF, true);
            cursorY += line * 4 + 2;
        }

        if (showStatus) {
            graphics.drawString(minecraft.font, "QuickSlot • " + profile.displayName(), 0, cursorY, 0xFFFFFF, true);
            graphics.drawString(
                minecraft.font,
                "Сортировка: " + (autoSort ? "ВКЛ" : "ВЫКЛ"),
                0,
                cursorY + line,
                autoSort ? 0x55FF55 : 0xFF5555,
                true
            );
        }

        if (!showResources && !showStatus) {
            graphics.drawString(minecraft.font, "HUD выключен", 0, 0, 0xAAAAAA, true);
        }

        graphics.pose().popMatrix();
    }

    public static int previewWidth(QuickSlotConfig config) {
        Minecraft minecraft = Minecraft.getInstance();
        int width = minecraft.font.width("HUD выключен");
        if (config.resourceHud()) {
            width = Math.max(width, minecraft.font.width("Diamond: 999"));
            width = Math.max(width, minecraft.font.width("Emerald: 999"));
        }
        if (config.statusHud()) {
            width = Math.max(width, minecraft.font.width("QuickSlot • " + config.profile().displayName()));
            width = Math.max(width, minecraft.font.width("Сортировка: ВЫКЛ"));
        }
        return Math.max(1, Math.round((width + 4) * config.hudScale()));
    }

    public static int previewHeight(QuickSlotConfig config) {
        int line = Minecraft.getInstance().font.lineHeight + 2;
        int height = 0;
        if (config.resourceHud()) height += line * 4 + 2;
        if (config.statusHud()) height += line * 2;
        if (height == 0) height = line;
        return Math.max(1, Math.round(height * config.hudScale()));
    }
}
