package dev.netrox.quickslot;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

public final class QuickSlotClient {
    private static final KeyMapping OPEN_SETTINGS = new KeyMapping(
        "key.quickslot.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, KeyMapping.Category.MISC
    );
    private static final KeyMapping NEXT_PROFILE = new KeyMapping(
        "key.quickslot.next_profile", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, KeyMapping.Category.MISC
    );
    private static int tickCounter;

    private QuickSlotClient() {}

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_SETTINGS);
        event.register(NEXT_PROFILE);
    }

    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        QuickSlotConfig config = QuickSlotConfig.get();

        while (OPEN_SETTINGS.consumeClick()) {
            minecraft.setScreen(new QuickSlotScreen(minecraft.screen));
        }
        while (NEXT_PROFILE.consumeClick()) {
            config.nextProfile();
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("QuickSlot: профиль " + config.profile().displayName()),
                    true
                );
            }
        }

        tickCounter++;
        if (tickCounter >= 4) {
            tickCounter = 0;
            InventoryManager.tick(minecraft);
        }
    }

    public static void addHudLayer(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(
            Identifier.fromNamespaceAndPath(QuickSlot.MODID, "resource_hud"),
            QuickSlotClient::renderResourceHud
        );
    }

    private static void renderResourceHud(GuiGraphics graphics, DeltaTracker tracker) {
        Minecraft minecraft = Minecraft.getInstance();
        QuickSlotConfig config = QuickSlotConfig.get();
        if (minecraft.player == null || minecraft.options.hideGui || !config.resourceHud()) return;

        ResourceHudRenderer.render(
            graphics,
            minecraft.player.getInventory(),
            config.hudX(),
            config.hudY(),
            config.hudScale()
        );
    }
}
