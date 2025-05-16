package com.bazaarflip;

import com.bazaarflip.commands.BazaarFlipCommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import com.bazaarflip.bazaar.BazaarRenderer;
import com.bazaarflip.bazaar.BazaarTracker;

@Mod(modid = "bazaarflip", version = "1.0", clientSideOnly = true)
public class BazaarFlipMod {
    private static BazaarTracker bazaarTracker;
    private static BazaarRenderer bazaarRenderer;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize config
        BazaarConfig.init(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        bazaarTracker = new BazaarTracker();
        bazaarRenderer = new BazaarRenderer(bazaarTracker);

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(bazaarTracker);
        MinecraftForge.EVENT_BUS.register(bazaarRenderer);
        
        // Register commands
        ClientCommandHandler.instance.registerCommand(new BazaarFlipCommand());
    }

    public static BazaarTracker getTracker() {
        return bazaarTracker;
    }

    public static BazaarRenderer getRenderer() {
        return bazaarRenderer;
    }
}