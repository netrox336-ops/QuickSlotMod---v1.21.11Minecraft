package dev.netrox.quickslot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ProfileCopyScreen extends Screen {
    private final Screen parent;

    public ProfileCopyScreen(Screen parent) {
        super(Component.literal("Копирование профиля"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        QuickSlotConfig config = QuickSlotConfig.get();
        int x = width / 2 - 100;
        int y = height / 2 - 58;
        int row = 0;

        for (Profile profile : Profile.values()) {
            if (profile == config.profile()) continue;
            Profile target = profile;
            addRenderableWidget(Button.builder(
                Component.literal("Копировать в: " + profile.displayName()),
                button -> {
                    config.copyActiveProfileTo(target);
                    minecraft.setScreen(parent);
                }
            ).bounds(x, y + row * 24, 200, 20).build());
            row++;
        }

        addRenderableWidget(Button.builder(
            Component.literal("Назад"),
            button -> minecraft.setScreen(parent)
        ).bounds(x, y + row * 24 + 10, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        QuickSlotConfig config = QuickSlotConfig.get();
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 90, 0xFFFFFF);
        graphics.drawCenteredString(
            font,
            Component.literal("Источник: " + config.profile().displayName()),
            width / 2,
            height / 2 - 76,
            0xD0D0D0
        );
        graphics.drawCenteredString(
            font,
            Component.literal("Профиль назначения будет перезаписан"),
            width / 2,
            height / 2 - 64,
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
