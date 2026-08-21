package dev.netrox.quickslot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class InventorySettingsScreen extends Screen {
    private final Screen parent;

    public InventorySettingsScreen(Screen parent) {
        super(Component.literal("Инвентарь"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        QuickSlotConfig config = QuickSlotConfig.get();
        int x = width / 2 - 100;
        int y = height / 2 - 44;

        addRenderableWidget(Button.builder(
            Component.literal(consolidationText(config)),
            button -> {
                config.toggleStackConsolidation();
                button.setMessage(Component.literal(consolidationText(config)));
            }
        ).bounds(x, y, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal(graceText(config)),
            button -> {
                config.toggleManualGrace();
                button.setMessage(Component.literal(graceText(config)));
            }
        ).bounds(x, y + 28, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Назад"),
            button -> minecraft.setScreen(parent)
        ).bounds(x, y + 64, 200, 20).build());
    }

    private String consolidationText(QuickSlotConfig config) {
        return "Объединять стаки: " + state(config.stackConsolidation());
    }

    private String graceText(QuickSlotConfig config) {
        return "Пауза после инвентаря: " + state(config.manualGrace());
    }

    private String state(boolean enabled) {
        return enabled ? "ВКЛ" : "ВЫКЛ";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 78, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Дополнительное управление предметами"), width / 2, height / 2 - 64, 0xA0A0A0);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
