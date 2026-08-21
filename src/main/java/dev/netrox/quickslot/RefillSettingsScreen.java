package dev.netrox.quickslot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RefillSettingsScreen extends Screen {
    private final Screen parent;
    private final int selectedSlot;

    public RefillSettingsScreen(Screen parent) {
        this(parent, 0);
    }

    private RefillSettingsScreen(Screen parent, int selectedSlot) {
        super(Component.literal("Автопополнение"));
        this.parent = parent;
        this.selectedSlot = Math.max(0, Math.min(8, selectedSlot));
    }

    @Override
    protected void init() {
        QuickSlotConfig config = QuickSlotConfig.get();
        int center = width / 2;
        int top = Math.max(34, height / 2 - 104);

        for (int slot = 0; slot < 9; slot++) {
            int currentSlot = slot;
            int column = slot % 3;
            int row = slot / 3;
            addRenderableWidget(Button.builder(
                slotMessage(config, slot),
                button -> minecraft.setScreen(new RefillSettingsScreen(parent, currentSlot))
            ).bounds(center - 103 + column * 69, top + row * 24, 66, 20).build());
        }

        int controlsTop = top + 86;
        addRenderableWidget(Button.builder(
            Component.literal("Auto Refill: " + state(config.isRefillEnabled(selectedSlot))),
            button -> {
                config.toggleRefillEnabled(selectedSlot);
                minecraft.setScreen(new RefillSettingsScreen(parent, selectedSlot));
            }
        ).bounds(center - 100, controlsTop, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Режим: " + config.refillMode(selectedSlot).displayName()),
            button -> {
                config.nextRefillMode(selectedSlot);
                minecraft.setScreen(new RefillSettingsScreen(parent, selectedSlot));
            }
        ).bounds(center - 100, controlsTop + 24, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("-8"),
            button -> {
                config.setRefillThreshold(selectedSlot, config.refillThreshold(selectedSlot) - 8);
                minecraft.setScreen(new RefillSettingsScreen(parent, selectedSlot));
            }
        ).bounds(center - 100, controlsTop + 48, 48, 20).build());

        Button threshold = Button.builder(
            Component.literal("Порог: " + config.refillThreshold(selectedSlot)),
            button -> {}
        ).bounds(center - 48, controlsTop + 48, 96, 20).build();
        threshold.active = false;
        addRenderableWidget(threshold);

        addRenderableWidget(Button.builder(
            Component.literal("+8"),
            button -> {
                config.setRefillThreshold(selectedSlot, config.refillThreshold(selectedSlot) + 8);
                minecraft.setScreen(new RefillSettingsScreen(parent, selectedSlot));
            }
        ).bounds(center + 52, controlsTop + 48, 48, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Назад"),
            button -> minecraft.setScreen(parent)
        ).bounds(center - 100, controlsTop + 80, 200, 20).build());
    }

    private Component slotMessage(QuickSlotConfig config, int slot) {
        String number = slot == selectedSlot ? "[" + (slot + 1) + "]" : Integer.toString(slot + 1);
        return Component.literal(number + " " + state(config.isRefillEnabled(slot)));
    }

    private String state(boolean enabled) {
        return enabled ? "ВКЛ" : "ВЫКЛ";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        QuickSlotConfig config = QuickSlotConfig.get();
        int top = Math.max(34, height / 2 - 104);
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Профиль: " + config.profile().displayName()), width / 2, 20, 0xD0D0D0);
        graphics.drawCenteredString(
            font,
            Component.literal("Слот " + (selectedSlot + 1) + ": " + config.rule(selectedSlot).displayName()),
            width / 2,
            top + 72,
            0xA0A0A0
        );
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
