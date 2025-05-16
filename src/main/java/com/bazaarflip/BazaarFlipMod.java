package com.bazaarflip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import com.bazaarflip.bazaar.BazaarRenderer;
import com.bazaarflip.bazaar.BazaarTracker;

@Mod(modid = "bazaarflip", version = "1.1", clientSideOnly = true)
public class BazaarFlipMod {
    private static BazaarTracker bazaarTracker;
    private static BazaarRenderer bazaarRenderer;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        bazaarTracker = new BazaarTracker();
        bazaarRenderer = new BazaarRenderer(bazaarTracker);

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(bazaarTracker);
        MinecraftForge.EVENT_BUS.register(bazaarRenderer);
    }

    public static BazaarTracker getTracker() {
        return bazaarTracker;
    }

    public static BazaarRenderer getRenderer() {
        return bazaarRenderer;
    }
}