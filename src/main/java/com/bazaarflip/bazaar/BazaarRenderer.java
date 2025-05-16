package com.bazaarflip.bazaar;

import com.bazaarflip.BazaarConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.util.List;

public class BazaarRenderer extends Gui {
    private final BazaarTracker tracker;
    private boolean showingFlips = false;
    private boolean isDragging = false;
    private int dragOffsetX, dragOffsetY;
    private int maxWidth = 200;

    public BazaarRenderer(BazaarTracker tracker) {
        this.tracker = tracker;
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        showingFlips = event.gui instanceof GuiChest;
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (showingFlips && event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            List<BazaarTracker.FlipResult> results = tracker.getFlipResults();
            
            // Calculate dynamic width based on item names
            maxWidth = 200;
            for (BazaarTracker.FlipResult result : results) {
                int itemWidth = fr.getStringWidth(result.itemName) + 100; // Extra space for profit and margin
                maxWidth = Math.max(maxWidth, itemWidth);
            }
            
            int height = Math.min(results.size() * 12 + 15, 150);
            drawRect(BazaarConfig.getGuiX(), BazaarConfig.getGuiY(), 
                     BazaarConfig.getGuiX() + maxWidth, BazaarConfig.getGuiY() + height, 
                     BazaarConfig.getBackgroundColor());
            
            // Draw header
            fr.drawStringWithShadow("Bazaar Flip Finder", BazaarConfig.getGuiX() + 5, BazaarConfig.getGuiY() + 5, 0xFFFFFF);
            
            // Draw results
            int yPos = BazaarConfig.getGuiY() + 15;
            for (BazaarTracker.FlipResult result : results) {
                if (yPos > BazaarConfig.getGuiY() + height - 12) break;
                
                String text = String.format("%s - §6%.1f§f | §a%.1f%%§f",
                    result.itemName, result.profit, result.margin);
                
                fr.drawStringWithShadow(text, BazaarConfig.getGuiX() + 5, yPos, 0xFFFFFF);
                yPos += 12;
            }
        }
    }
    
    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!showingFlips || !(event.gui instanceof GuiChest)) return;
        
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int mouseX = Mouse.getX() * sr.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / Minecraft.getMinecraft().displayHeight;
        
        // Check if mouse is over GUI
        if (mouseX >= BazaarConfig.getGuiX() && mouseX <= BazaarConfig.getGuiX() + maxWidth &&
            mouseY >= BazaarConfig.getGuiY() && mouseY <= BazaarConfig.getGuiY() + 150) {
            isDragging = true;
            dragOffsetX = mouseX - BazaarConfig.getGuiX();
            dragOffsetY = mouseY - BazaarConfig.getGuiY();
        }
    }
    
    @SubscribeEvent
    public void onMouseDrag(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (isDragging && showingFlips) {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            int mouseX = Mouse.getX() * sr.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
            int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / Minecraft.getMinecraft().displayHeight;
            
            int newX = mouseX - dragOffsetX;
            int newY = mouseY - dragOffsetY;
            
            // Keep GUI on screen
            newX = Math.max(0, Math.min(sr.getScaledWidth() - maxWidth, newX));
            newY = Math.max(0, Math.min(sr.getScaledHeight() - 150, newY));
            
            BazaarConfig.setGuiX(newX);
            BazaarConfig.setGuiY(newY);
            BazaarConfig.saveConfig();
        }
    }
    
    @SubscribeEvent
    public void onMouseRelease(GuiScreenEvent.MouseInputEvent.Post event) {
        isDragging = false;
    }
}