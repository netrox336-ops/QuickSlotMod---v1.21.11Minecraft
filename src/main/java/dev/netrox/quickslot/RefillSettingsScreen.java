package dev.netrox.quickslot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RefillSettingsScreen extends Screen {
    private final Screen parent;

    public RefillSettingsScreen(Screen parent) {
        super(Component.literal("Автопополнение"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        QuickSlotConfig config = QuickSlotConfig.get();
        int center = width / 2;
        int top = Math.max(28, height / 2 - 126);

        addRenderableWidget(Button.builder(
            Component.literal("Режим: " + config.refillMode().displayName()),
            button -> {
                config.nextRefillMode();
                minecraft.setScreen(new RefillSettingsScreen(parent));
            }
        ).bounds(center - 100, top, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("-8"),
            button -> {
                config.setRefillThreshold(config.refillThreshold() - 8);
                minecraft.setScreen(new RefillSettingsScreen(parent));
            }
        ).bounds(center - 100, top + 28, 48, 20).build());

        Button threshold = Button.builder(
            Component.literal("Порог: " + config.refillThreshold()),
            button -> {}
        ).bounds(center - 48, top + 28, 96, 20).build();
        threshold.active = false;
        addRenderableWidget(threshold);

        addRenderableWidget(Button.builder(
            Component.literal("+8"),
            button -> {
                config.setRefillThreshold(config.refillThreshold() + 8);
                minecraft.setScreen(new RefillSettingsScreen(parent));
            }
        ).bounds(center + 52, top + 28, 48, 20).build());

        int gridTop = top + 76;
        for (int slot = 0; slot < 9; slot++) {
            int currentSlot = slot;
            int column = slot % 3;
            int row = slot / 3;
            addRenderableWidget(Button.builder(
                refillSlotMessage(config, slot),
                button -> {
                    config.toggleRefillEnabled(currentSlot);
                    button.setMessage(refillSlotMessage(config, currentSlot));
                }
            ).bounds(center - 103 + column * 69, gridTop + row * 26, 66, 20).build());
        }

        addRenderableWidget(Button.builder(
            Component.literal("Назад"),
            button -> minecraft.setScreen(parent)
        ).bounds(center - 100, gridTop + 86, 200, 20).build());
    }

    private Component refillSlotMessage(QuickSlotConfig config, int slot) {
        return Component.literal((slot + 1) + ": " + (config.isRefillEnabled(slot) ? "ВКЛ" : "ВЫКЛ"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        QuickSlotConfig config = QuickSlotConfig.get();
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Профиль: " + config.profile().displayName()), width / 2, 20, 0xD0D0D0);
        graphics.drawCenteredString(font, Component.literal("Каждый слот можно исключить из Auto Refill отдельно"), width / 2, 32, 0xA0A0A0);
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
