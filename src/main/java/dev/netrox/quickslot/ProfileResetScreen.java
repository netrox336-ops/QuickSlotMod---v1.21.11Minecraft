package dev.netrox.quickslot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ProfileResetScreen extends Screen {
    private final Screen parent;

    public ProfileResetScreen(Screen parent) {
        super(Component.literal("Сброс профиля"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = width / 2;
        int y = height / 2 + 10;

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить"),
            button -> {
                QuickSlotConfig.get().resetActiveProfile();
                minecraft.setScreen(parent);
            }
        ).bounds(center - 100, y, 98, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Отмена"),
            button -> minecraft.setScreen(parent)
        ).bounds(center + 2, y, 98, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        QuickSlotConfig config = QuickSlotConfig.get();
        graphics.drawCenteredString(
            font,
            Component.literal("Сбросить профиль «" + config.profile().displayName() + "»?"),
            width / 2,
            height / 2 - 32,
            0xFFFFFF
        );
        graphics.drawCenteredString(
            font,
            Component.literal("Будут сброшены хотбар, Auto Refill и приоритеты блоков"),
            width / 2,
            height / 2 - 16,
            0xA0A0A0
        );
        graphics.drawCenteredString(
            font,
            Component.literal("Глобальные настройки останутся без изменений"),
            width / 2,
            height / 2 - 4,
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
