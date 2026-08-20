package dev.netrox.quickslot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RefillSettingsScreen extends Screen {
    private final Screen rootParent;

    public RefillSettingsScreen(Screen rootParent) {
        super(Component.literal("Автопополнение"));
        this.rootParent = rootParent;
    }

    @Override
    protected void init() {
        QuickSlotConfig config = QuickSlotConfig.get();
        int x = width / 2 - 100;
        int y = height / 2 - 48;

        addRenderableWidget(Button.builder(
            Component.literal("Режим: " + config.refillMode().displayName()),
            button -> {
                config.nextRefillMode();
                minecraft.setScreen(new RefillSettingsScreen(rootParent));
            }
        ).bounds(x, y, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("-8"),
            button -> {
                config.setRefillThreshold(config.refillThreshold() - 8);
                minecraft.setScreen(new RefillSettingsScreen(rootParent));
            }
        ).bounds(x, y + 28, 48, 20).build());

        Button threshold = Button.builder(
            Component.literal("Порог: " + config.refillThreshold()),
            button -> {}
        ).bounds(x + 52, y + 28, 96, 20).build();
        threshold.active = false;
        addRenderableWidget(threshold);

        addRenderableWidget(Button.builder(
            Component.literal("+8"),
            button -> {
                config.setRefillThreshold(config.refillThreshold() + 8);
                minecraft.setScreen(new RefillSettingsScreen(rootParent));
            }
        ).bounds(x + 152, y + 28, 48, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Назад"),
            button -> minecraft.setScreen(new QuickSlotScreen(rootParent))
        ).bounds(x, y + 64, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 82, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Порог используется только в режиме «Ниже порога»"), width / 2, height / 2 - 68, 0xA0A0A0);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(new QuickSlotScreen(rootParent));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
