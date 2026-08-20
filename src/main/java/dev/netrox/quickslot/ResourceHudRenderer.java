package dev.netrox.quickslot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

public final class ResourceHudRenderer {
    private ResourceHudRenderer() {}

    public static void render(GuiGraphics graphics, Inventory inventory, int x, int y, float scale) {
        render(
            graphics,
            x,
            y,
            scale,
            InventoryManager.count(inventory, Items.IRON_INGOT),
            InventoryManager.count(inventory, Items.GOLD_INGOT),
            InventoryManager.count(inventory, Items.DIAMOND),
            InventoryManager.count(inventory, Items.EMERALD)
        );
    }

    public static void render(GuiGraphics graphics, int x, int y, float scale, int iron, int gold, int diamond, int emerald) {
        Minecraft minecraft = Minecraft.getInstance();
        int line = minecraft.font.lineHeight + 2;

        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.drawString(minecraft.font, "Iron: " + iron, 0, 0, 0xFFFFFF, true);
        graphics.drawString(minecraft.font, "Gold: " + gold, 0, line, 0xFFFFFF, true);
        graphics.drawString(minecraft.font, "Diamond: " + diamond, 0, line * 2, 0xFFFFFF, true);
        graphics.drawString(minecraft.font, "Emerald: " + emerald, 0, line * 3, 0xFFFFFF, true);
        graphics.pose().popMatrix();
    }

    public static int previewWidth(float scale) {
        return Math.max(1, Math.round(100 * scale));
    }

    public static int previewHeight(float scale) {
        return Math.max(1, Math.round((Minecraft.getInstance().font.lineHeight + 2) * 4 * scale));
    }
}
