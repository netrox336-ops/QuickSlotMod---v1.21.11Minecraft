package dev.netrox.quickslot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ProfileScreen extends Screen {
    private final Screen parent;

    public ProfileScreen(Screen parent) {
        super(Component.literal("Профили QuickSlot"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        QuickSlotConfig config = QuickSlotConfig.get();
        int x = width / 2 - 100;
        int y = height / 2 - 82;

        Profile[] profiles = Profile.values();
        for (int i = 0; i < profiles.length; i++) {
            Profile profile = profiles[i];
            addRenderableWidget(Button.builder(
                profileMessage(config, profile),
                button -> {
                    config.setProfile(profile);
                    minecraft.setScreen(new ProfileScreen(parent));
                }
            ).bounds(x, y + i * 24, 200, 20).build());
        }

        addRenderableWidget(Button.builder(
            Component.literal("Копировать активный профиль"),
            button -> minecraft.setScreen(new ProfileCopyScreen(this))
        ).bounds(x, y + 82, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Сбросить активный профиль"),
            button -> minecraft.setScreen(new ProfileResetScreen(this))
        ).bounds(x, y + 106, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Назад"),
            button -> minecraft.setScreen(parent)
        ).bounds(x, y + 138, 200, 20).build());
    }

    private Component profileMessage(QuickSlotConfig config, Profile profile) {
        String prefix = config.profile() == profile ? "> " : "";
        return Component.literal(prefix + profile.displayName());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 110, 0xFFFFFF);
        graphics.drawCenteredString(
            font,
            Component.literal("Выберите активный профиль или управляйте его настройками"),
            width / 2,
            height / 2 - 96,
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
