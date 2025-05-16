package com.bazaarflip.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import java.io.File;

public class BazaarConfig {
    private static Configuration config;
    
    // GUI Settings
    private static int guiX = 5;
    private static int guiY = 5;
    private static int maxDisplayItems = 10;
    private static int backgroundColor = 0x80000000;
    private static int updateInterval = 20;
    
    public static void init(File configFile) {
        config = new Configuration(configFile);
        loadConfig();
    }
    
    public static void loadConfig() {
        config.load();
        
        Property guiXProperty = config.get("gui", "x", 5, "GUI X position");
        Property guiYProperty = config.get("gui", "y", 5, "GUI Y position");
        Property maxItemsProperty = config.get("gui", "maxItems", 10, "Maximum number of items to display");
        Property bgColorProperty = config.get("gui", "backgroundColor", "80000000", "Background color in hex format");
        Property updateIntervalProperty = config.get("performance", "updateInterval", 20, "API update interval in ticks");
        
        guiX = guiXProperty.getInt();
        guiY = guiYProperty.getInt();
        maxDisplayItems = maxItemsProperty.getInt();
        backgroundColor = Integer.parseInt(bgColorProperty.getString(), 16);
        updateInterval = updateIntervalProperty.getInt();
        
        if (config.hasChanged()) {
            config.save();
        }
    }
    
    public static void saveConfig() {
        Property guiXProperty = config.get("gui", "x", 5);
        Property guiYProperty = config.get("gui", "y", 5);
        
        guiXProperty.set(guiX);
        guiYProperty.set(guiY);
        
        config.save();
    }
    
    // Getters and setters
    public static int getGuiX() { return guiX; }
    public static int getGuiY() { return guiY; }
    public static void setGuiX(int x) { guiX = x; }
    public static void setGuiY(int y) { guiY = y; }
    public static int getMaxDisplayItems() { return maxDisplayItems; }
    public static int getBackgroundColor() { return backgroundColor; }
    public static int getUpdateInterval() { return updateInterval; }
}