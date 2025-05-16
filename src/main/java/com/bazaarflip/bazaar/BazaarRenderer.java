package com.bazaarflip.bazaar;

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
    private int guiX = 5;
    private int guiY = 5;
    private boolean isDragging = false;
    private int dragOffsetX, dragOffsetY;

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
            
            // Draw background
            int width = 300;
            int height = Math.min(results.size() * 12 + 25, 150);
            drawRect(guiX, guiY, guiX + width, guiY + height, 0x80000000);
            
            // Draw header
            fr.drawStringWithShadow("Bazaar Flip Finder", guiX + 5, guiY + 5, 0xFFFFFF);
            
            // Draw results
            int yPos = guiY + 15;
            for (BazaarTracker.FlipResult result : results) {
                if (yPos > guiY + height - 12) break;
                
                String text = String.format("%s - §6%.1f§f | §a%.1f%%§f | §b%.1f hrs§f",
                    result.itemName, result.profit, result.margin, result.restockHours);
                
                fr.drawStringWithShadow(text, guiX + 5, yPos, 0xFFFFFF);
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
        if (mouseX >= guiX && mouseX <= guiX + 200 &&
            mouseY >= guiY && mouseY <= guiY + 150) {
            isDragging = true;
            dragOffsetX = mouseX - guiX;
            dragOffsetY = mouseY - guiY;
        }
    }
    
    @SubscribeEvent
    public void onMouseDrag(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (isDragging && showingFlips) {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            int mouseX = Mouse.getX() * sr.getScaledWidth() / Minecraft.getMinecraft().displayWidth;
            int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / Minecraft.getMinecraft().displayHeight;
            
            guiX = mouseX - dragOffsetX;
            guiY = mouseY - dragOffsetY;
            
            // Keep GUI on screen
            guiX = Math.max(0, Math.min(sr.getScaledWidth() - 200, guiX));
            guiY = Math.max(0, Math.min(sr.getScaledHeight() - 150, guiY));
        }
    }
    
    @SubscribeEvent
    public void onMouseRelease(GuiScreenEvent.MouseInputEvent.Post event) {
        isDragging = false;
    }
}