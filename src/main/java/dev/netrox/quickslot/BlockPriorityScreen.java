package dev.netrox.quickslot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class BlockPriorityScreen extends Screen {
    private final Screen parent;

    public BlockPriorityScreen(Screen parent) {
        super(Component.literal("Приоритет блоков"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        QuickSlotConfig config = QuickSlotConfig.get();
        int x = width / 2 - 100;
        int y = Math.max(38, height / 2 - 92);
        int count = BlockType.values().length;

        for (int index = 0; index < count; index++) {
            int current = index;
            int rowY = y + index * 22;

            Button label = Button.builder(
                Component.literal((index + 1) + ". " + config.blockPriority(index).displayName()),
                button -> {}
            ).bounds(x, rowY, 112, 20).build();
            label.active = false;
            addRenderableWidget(label);

            Button up = Button.builder(
                Component.literal("↑"),
                button -> {
                    config.moveBlockPriority(current, -1);
                    minecraft.setScreen(new BlockPriorityScreen(parent));
                }
            ).bounds(x + 116, rowY, 40, 20).build();
            up.active = index > 0;
            addRenderableWidget(up);

            Button down = Button.builder(
                Component.literal("↓"),
                button -> {
                    config.moveBlockPriority(current, 1);
                    minecraft.setScreen(new BlockPriorityScreen(parent));
                }
            ).bounds(x + 160, rowY, 40, 20).build();
            down.active = index < count - 1;
            addRenderableWidget(down);
        }

        int bottom = y + count * 22 + 6;
        addRenderableWidget(Button.builder(
            Component.literal("Сбросить"),
            button -> {
                config.resetBlockPriority();
                minecraft.setScreen(new BlockPriorityScreen(parent));
            }
        ).bounds(x, bottom, 98, 20).build());

        addRenderableWidget(Button.builder(
            Component.literal("Назад"),
            button -> minecraft.setScreen(parent)
        ).bounds(x + 102, bottom, 98, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Выше в списке — раньше используется"), width / 2, 22, 0xA0A0A0);
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
