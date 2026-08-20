package dev.netrox.quickslot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class HudEditorScreen extends Screen {
    private final Screen parent;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("Настройка HUD QuickSlot"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = width / 2;
        int bottom = height - 28;

        addRenderableWidget(Button.builder(Component.literal("Масштаб -"), button -> changeScale(-0.1F))
            .bounds(center - 154, bottom, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Сбросить"), button -> QuickSlotConfig.get().resetHud())
            .bounds(center - 50, bottom, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Масштаб +"), button -> changeScale(0.1F))
            .bounds(center + 56, bottom, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Готово"), button -> onClose())
            .bounds(center - 75, bottom - 26, 150, 20).build());
    }

    private void changeScale(float delta) {
        QuickSlotConfig config = QuickSlotConfig.get();
        config.setHudScale(config.hudScale() + delta);
        clampHudPosition();
        config.save();
    }

    private void clampHudPosition() {
        QuickSlotConfig config = QuickSlotConfig.get();
        int maxX = Math.max(0, width - ResourceHudRenderer.previewWidth(config.hudScale()));
        int maxY = Math.max(0, height - ResourceHudRenderer.previewHeight(config.hudScale()) - 58);
        config.setHudPosition(Math.min(config.hudX(), maxX), Math.min(config.hudY(), maxY));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent info, boolean recent) {
        if (super.mouseClicked(info, recent)) return true;
        if (info.button() != 0) return false;

        QuickSlotConfig config = QuickSlotConfig.get();
        int hudWidth = ResourceHudRenderer.previewWidth(config.hudScale());
        int hudHeight = ResourceHudRenderer.previewHeight(config.hudScale());
        if (info.x() >= config.hudX() && info.x() <= config.hudX() + hudWidth
            && info.y() >= config.hudY() && info.y() <= config.hudY() + hudHeight) {
            dragging = true;
            dragOffsetX = (int) info.x() - config.hudX();
            dragOffsetY = (int) info.y() - config.hudY();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent info, double deltaX, double deltaY) {
        if (!dragging) return super.mouseDragged(info, deltaX, deltaY);

        QuickSlotConfig config = QuickSlotConfig.get();
        int hudWidth = ResourceHudRenderer.previewWidth(config.hudScale());
        int hudHeight = ResourceHudRenderer.previewHeight(config.hudScale());
        int maxX = Math.max(0, width - hudWidth);
        int maxY = Math.max(0, height - hudHeight - 58);
        int x = Math.max(0, Math.min(maxX, (int) info.x() - dragOffsetX));
        int y = Math.max(0, Math.min(maxY, (int) info.y() - dragOffsetY));
        config.setHudPosition(x, y);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent info) {
        if (dragging && info.button() == 0) {
            dragging = false;
            QuickSlotConfig.get().save();
            return true;
        }
        return super.mouseReleased(info);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        QuickSlotConfig config = QuickSlotConfig.get();

        graphics.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Перетащите счётчик мышкой. Масштаб меняется кнопками снизу."), width / 2, 22, 0xA0A0A0);
        graphics.drawCenteredString(font, Component.literal("Масштаб: " + String.format(java.util.Locale.ROOT, "%.1fx", config.hudScale())), width / 2, 34, 0xD0D0D0);

        int hudWidth = ResourceHudRenderer.previewWidth(config.hudScale());
        int hudHeight = ResourceHudRenderer.previewHeight(config.hudScale());
        graphics.fill(config.hudX() - 3, config.hudY() - 3, config.hudX() + hudWidth + 3, config.hudY() + hudHeight + 3, 0x66000000);

        if (minecraft.player != null) {
            ResourceHudRenderer.render(graphics, minecraft.player.getInventory(), config.hudX(), config.hudY(), config.hudScale());
        } else {
            ResourceHudRenderer.render(graphics, config.hudX(), config.hudY(), config.hudScale(), 32, 8, 4, 2);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        QuickSlotConfig.get().save();
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
