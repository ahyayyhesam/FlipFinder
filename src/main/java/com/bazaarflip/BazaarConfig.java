package com.bazaarflip;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import java.io.File;

public class BazaarConfig {
    private static Configuration config;
    private static int guiX = 5;
    private static int guiY = 5;
    private static int backgroundColor = 0x80000000;
    private static int maxDisplayItems = 10;
    private static int refreshInterval = 20;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        loadConfig();
    }

    public static void loadConfig() {
        Property prop;

        prop = config.get(Configuration.CATEGORY_CLIENT, "guiX", guiX, "X position of the GUI");
        guiX = prop.getInt();

        prop = config.get(Configuration.CATEGORY_CLIENT, "guiY", guiY, "Y position of the GUI");
        guiY = prop.getInt();

        prop = config.get(Configuration.CATEGORY_CLIENT, "backgroundColor", String.format("0x%08X", backgroundColor), "Background color of the GUI (ARGB format)");
        backgroundColor = (int) Long.parseLong(prop.getString().replace("0x", ""), 16);

        prop = config.get(Configuration.CATEGORY_CLIENT, "maxDisplayItems", maxDisplayItems, "Maximum number of items to display");
        maxDisplayItems = prop.getInt();

        prop = config.get(Configuration.CATEGORY_CLIENT, "refreshInterval", refreshInterval, "Refresh interval in ticks (20 ticks = 1 second)");
        refreshInterval = prop.getInt();

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void saveConfig() {
        Property prop;

        prop = config.get(Configuration.CATEGORY_CLIENT, "guiX", guiX);
        prop.set(guiX);

        prop = config.get(Configuration.CATEGORY_CLIENT, "guiY", guiY);
        prop.set(guiY);

        prop = config.get(Configuration.CATEGORY_CLIENT, "backgroundColor", String.format("0x%08X", backgroundColor));
        prop.set(String.format("0x%08X", backgroundColor));

        prop = config.get(Configuration.CATEGORY_CLIENT, "maxDisplayItems", maxDisplayItems);
        prop.set(maxDisplayItems);

        prop = config.get(Configuration.CATEGORY_CLIENT, "refreshInterval", refreshInterval);
        prop.set(refreshInterval);

        config.save();
    }

    public static int getGuiX() { return guiX; }
    public static int getGuiY() { return guiY; }
    public static int getBackgroundColor() { return backgroundColor; }
    public static int getMaxDisplayItems() { return maxDisplayItems; }
    public static int getRefreshInterval() { return refreshInterval; }

    public static void setGuiX(int x) { guiX = x; }
    public static void setGuiY(int y) { guiY = y; }
    public static void setBackgroundColor(int color) { backgroundColor = color; }
    public static void setMaxDisplayItems(int count) { maxDisplayItems = count; }
    public static void setRefreshInterval(int interval) { refreshInterval = interval; }
}