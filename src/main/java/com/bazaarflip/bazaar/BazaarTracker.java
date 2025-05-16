package com.bazaarflip.bazaar;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BazaarTracker {
    private static final double MIN_BAZAAR_PRICE = 1555.0;
    private static final int MIN_HOURLY_SALES = 640;
    private static final int UPDATE_INTERVAL = 5;
    
    private JsonObject itemRegistry;
    private JsonObject bazaarData;
    private long lastUpdate = 0;
    private List<FlipResult> currentFlips = new ArrayList<>();

    public static class FlipResult {
        public final String itemName;
        public final double profit;
        public final double margin;
        public final int hourlyVolume;
        public final double restockHours;

        public FlipResult(String itemName, double profit, double margin, int hourlyVolume, double restockHours) {
            this.itemName = itemName;
            this.profit = profit;
            this.margin = margin;
            this.hourlyVolume = hourlyVolume;
            this.restockHours = restockHours;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        
        long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime - lastUpdate < UPDATE_INTERVAL) return;
        
        lastUpdate = currentTime;
        updateMarketData();
    }

    private void updateMarketData() {
        CompletableFuture.runAsync(() -> {
            try {
                // Update item registry if needed
                if (itemRegistry == null) {
                    String itemData = EntityUtils.toString(HttpClients.createDefault()
                            .execute(new HttpGet("https://api.hypixel.net/resources/skyblock/items"))
                            .getEntity());
                    itemRegistry = new JsonParser().parse(itemData).getAsJsonObject();
                }

                // Get current bazaar data
                String bazaarResponse = EntityUtils.toString(HttpClients.createDefault()
                        .execute(new HttpGet("https://api.hypixel.net/skyblock/bazaar"))
                        .getEntity());
                bazaarData = new JsonParser().parse(bazaarResponse).getAsJsonObject();

                calculateProfits();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void calculateProfits() {
        List<FlipResult> newFlips = new ArrayList<>();

        if (itemRegistry == null || bazaarData == null) return;

        for (JsonElement item : itemRegistry.getAsJsonArray("items")) {
            JsonObject itemObj = item.getAsJsonObject();
            String itemId = itemObj.get("id").getAsString();
            
            if (!bazaarData.getAsJsonObject("products").has(itemId)) continue;
            
            JsonObject marketInfo = bazaarData.getAsJsonObject("products").getAsJsonObject(itemId);
            double npcPrice = itemObj.has("npc_sell_price") ? itemObj.get("npc_sell_price").getAsDouble() : 0;
            
            if (npcPrice <= 0 || !marketInfo.has("sell_summary") || marketInfo.getAsJsonArray("sell_summary").size() == 0) continue;
            
            double bazaarPrice = marketInfo.getAsJsonArray("sell_summary").get(0).getAsJsonObject().get("pricePerUnit").getAsDouble() + 0.1;
            if (bazaarPrice < MIN_BAZAAR_PRICE) continue;
            
            int weeklyVolume = marketInfo.getAsJsonObject("quick_status").get("sellMovingWeek").getAsInt();
            int hourlyVolume = weeklyVolume / 168;
            
            if (hourlyVolume < MIN_HOURLY_SALES) continue;
            
            double profit = npcPrice - bazaarPrice;
            double margin = (profit / bazaarPrice) * 100;
            
            if (profit > 0) {
                double restockHours = marketInfo.getAsJsonObject("quick_status").get("buyMovingWeek").getAsDouble() / (hourlyVolume * 168);
                newFlips.add(new FlipResult(
                    itemObj.get("name").getAsString(),
                    profit,
                    margin,
                    hourlyVolume,
                    restockHours
                ));
            }
        }

        newFlips.sort((a, b) -> {
            int restockCompare = Double.compare(a.restockHours, b.restockHours);
            if (restockCompare != 0) return restockCompare;
            return -Double.compare(a.profit, b.profit);
        });

        currentFlips = newFlips;
    }

    public List<FlipResult> getFlipResults() {
        return new ArrayList<>(currentFlips);
    }
}