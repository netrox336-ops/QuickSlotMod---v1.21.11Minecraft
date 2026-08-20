package dev.netrox.quickslot;

import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(QuickSlot.MODID)
public final class QuickSlot {
    public static final String MODID = "quickslot";

    public QuickSlot(FMLJavaModLoadingContext context) {
        RegisterKeyMappingsEvent.BUS.addListener(QuickSlotClient::registerKeys);
        TickEvent.ClientTickEvent.Post.BUS.addListener(QuickSlotClient::onClientTick);
        AddGuiOverlayLayersEvent.BUS.addListener(QuickSlotClient::addHudLayer);
    }
}
