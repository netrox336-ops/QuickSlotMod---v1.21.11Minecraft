package dev.netrox.quickslot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SmartSelectionScreen extends Screen {
    private final Screen parent;

    public SmartSelectionScreen(Screen parent) {
        super(Component.literal("Выбор"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        QuickSlotConfig config = QuickSlotConfig.get();
        int x = width / 2 - 100;
        int y = Math.max(36, height / 2 - 76);

        addRenderableWidget(Button.builder(
            Component.literal("Сохранять текущий блок: " + state(config.preferSameBlock())),
            button -> {
                config.togglePreferSameBlock();
                button.setMessage(Component.literal("Сохранять текущий блок: " + state(config.preferSameBlock())));
            }
        ).bounds(x, y, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Приоритет блоков"),
            button -> minecraft.setScreen(new BlockPriorityScreen(this))
        ).bounds(x, y + 28, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Улучшать меч: " + state(config.autoUpgradeSword())),
            button -> {
                config.toggleAutoUpgradeSword();
                button.setMessage(Component.literal("Улучшать меч: " + state(config.autoUpgradeSword())));
            }
        ).bounds(x, y + 62, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Улучшать кирку: " + state(config.autoUpgradePickaxe())),
            button -> {
                config.toggleAutoUpgradePickaxe();
                button.setMessage(Component.literal("Улучшать кирку: " + state(config.autoUpgradePickaxe())));
            }
        ).bounds(x, y + 86, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Улучшать топор: " + state(config.autoUpgradeAxe())),
            button -> {
                config.toggleAutoUpgradeAxe();
                button.setMessage(Component.literal("Улучшать топор: " + state(config.autoUpgradeAxe())));
            }
        ).bounds(x, y + 110, 200, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Назад"),
            button -> minecraft.setScreen(parent)
        ).bounds(x, y + 144, 200, 20).build());
    }

    private String state(boolean enabled) {
        return enabled ? "ВКЛ" : "ВЫКЛ";
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
        graphics.drawCenteredString(
            font,
            Component.literal("Профиль: " + QuickSlotConfig.get().profile().displayName()),
            width / 2,
            22,
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
