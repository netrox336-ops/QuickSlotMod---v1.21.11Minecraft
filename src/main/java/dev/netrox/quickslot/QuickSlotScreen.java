package dev.netrox.quickslot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class QuickSlotScreen extends Screen {
    private final Screen parent;

    public QuickSlotScreen(Screen parent) {
        super(Component.literal("QuickSlot"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        QuickSlotConfig config = QuickSlotConfig.get();
        int center = width / 2;
        int top = Math.max(36, height / 2 - 112);

        addRenderableWidget(Button.builder(
            Component.literal("Автосортировка: " + (config.enabled() ? "ВКЛ" : "ВЫКЛ")),
            button -> {
                config.toggleEnabled();
                button.setMessage(Component.literal("Автосортировка: " + (config.enabled() ? "ВКЛ" : "ВЫКЛ")));
            }
        ).bounds(center - 154, top, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("HUD ресурсов: " + (config.resourceHud() ? "ВКЛ" : "ВЫКЛ")),
            button -> {
                config.toggleResourceHud();
                button.setMessage(Component.literal("HUD ресурсов: " + (config.resourceHud() ? "ВКЛ" : "ВЫКЛ")));
            }
        ).bounds(center + 4, top, 150, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Профиль: " + config.profile().displayName()),
            button -> {
                config.nextProfile();
                minecraft.setScreen(new QuickSlotScreen(parent));
            }
        ).bounds(center - 100, top + 28, 200, 20).build());

        int gridTop = top + 62;
        for (int slot = 0; slot < 9; slot++) {
            int currentSlot = slot;
            int column = slot % 3;
            int row = slot / 3;
            addRenderableWidget(Button.builder(
                slotMessage(config, slot),
                button -> {
                    config.cycleRule(currentSlot);
                    button.setMessage(slotMessage(config, currentSlot));
                }
            ).bounds(center - 154 + column * 104, gridTop + row * 26, 100, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Готово"), button -> onClose())
            .bounds(center - 100, gridTop + 86, 200, 20).build());
    }

    private Component slotMessage(QuickSlotConfig config, int slot) {
        return Component.literal((slot + 1) + ": " + config.rule(slot).displayName());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 14, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Нажмите на слот, чтобы сменить его правило"), width / 2, 25, 0xA0A0A0);
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
