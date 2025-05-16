#!/usr/bin/env python3
"""
Hypixel Bazaar NPC Flipping Tool
Finds profitable items between NPC sell prices and Bazaar buy orders
"""

import requests
import time
import os
from typing import List, Tuple, Optional

# ---------------------------
# Configuration Settings
# ---------------------------
class Config:
    MIN_BAZAAR_PRICE = 1555.0       # Minimum acceptable Bazaar price
    MIN_HOURLY_SALES = 640          # Minimum hourly sales volume required
    TARGET_STOCK = 640              # Desired quantity to acquire
    UPDATE_INTERVAL = 5             # Seconds between market refreshes
    ITEM_CACHE_DURATION = 300       # Seconds between item data refreshes
    MAX_RESULTS = 25                # Number of results to save in file
    CONSOLE_DISPLAY = 3             # Number of results to show in terminal

# ---------------------------
# Global State
# ---------------------------
item_data_cache = None
last_item_refresh = 0

def clear_terminal() -> None:
    """Clears console screen based on OS"""
    os.system('cls' if os.name == 'nt' else 'clear')

def get_market_data() -> Tuple[Optional[dict], Optional[dict]]:
    """
    Fetches latest market data from Hypixel API
    Returns tuple of (bazaar_data, item_registry)
    """
    global item_data_cache, last_item_refresh
    
    # Refresh item registry periodically
    if time.time() - last_item_refresh > Config.ITEM_CACHE_DURATION:
        try:
            response = requests.get(
                "https://api.hypixel.net/resources/skyblock/items",
                timeout=5
            )
            response.raise_for_status()
            item_data_cache = response.json()
            last_item_refresh = time.time()
        except requests.RequestException:
            pass  # Use cached data if update fails
    
    # Get current bazaar prices
    try:
        bz_response = requests.get(
            "https://api.hypixel.net/skyblock/bazaar",
            timeout=5
        )
        bz_response.raise_for_status()
        return bz_response.json(), item_data_cache
    except requests.RequestException:
        return None, item_data_cache

def calculate_profits(bazaar_data: dict, item_registry: dict) -> List[Tuple[str, dict]]:
    """
    Analyzes market data to find profitable items
    Returns sorted list of (item_id, item_stats) tuples
    """
    if not bazaar_data or not item_registry:
        return []

    profitable_items = {}
    
    for item in item_registry.get("items", []):
        item_id = item.get("id")
        if not item_id:
            continue
        
        # Get market information
        market_info = bazaar_data["products"].get(item_id, {})
        npc_price = item.get("npc_sell_price", 0)
        
        # Basic item viability checks
        if not all([
            npc_price > 0,
            market_info.get("sell_summary"),
            market_info.get("quick_status")
        ]):
            continue
        
        # Market statistics
        sell_orders = market_info["sell_summary"]
        weekly_volume = market_info["quick_status"].get("sellMovingWeek", 0)
        hourly_volume = weekly_volume // 168  # Convert weekly to hourly
        
        # Volume requirements
        if hourly_volume < Config.MIN_HOURLY_SALES:
            continue
        
        # Price calculations
        bazaar_price = round(sell_orders[0]["pricePerUnit"] + 0.1, 1)
        if bazaar_price < Config.MIN_BAZAAR_PRICE:
            continue
        
        profit = npc_price - bazaar_price
        margin_percent = (profit / bazaar_price) * 100 if bazaar_price else 0
        restock_time = Config.TARGET_STOCK / hourly_volume if hourly_volume else 999
        
        # Final validation
        if profit <= 0 or restock_time > 1:
            continue
        
        profitable_items[item_id] = {
            "name": item.get("name", "Unknown Item"),
            "npc_price": npc_price,
            "bazaar_price": bazaar_price,
            "profit": round(profit, 1),
            "margin": round(margin_percent, 1),
            "hourly_volume": hourly_volume,
            "restock_hours": round(restock_time, 2)
        }
    
    # Sort by fastest restock time then highest profit
    return sorted(
        profitable_items.items(),
        key=lambda x: (x[1]["restock_hours"], -x[1]["profit"])
    )

def save_analysis(results: List[Tuple[str, dict]]) -> None:
    """Saves top results to timestamped text file"""
    filename = "bazaar_flips.txt"
    
    header = (
        f"{'Item Name':<35}  |  {'NPC Price':>8}  |  "
        f"{'Margin %':>8}  |  {'Hourly Vol':>11}\n"
        + "-"*80
    )
    
    with open(filename, 'w', encoding='utf-8') as output_file:
        output_file.write(header + "\n")
        
        for item_id, stats in results[:Config.MAX_RESULTS]:
            line = (
                f"{stats['name']:<35}  |  "
                f"{stats['npc_price']:>8.1f}  |  "
                f"{stats['margin']:>7.1f}%  |  "
                f"{stats['hourly_volume']:>11,.0f}"
            )
            output_file.write(line + "\n")
        
        output_file.write("-"*80 + "\n")

def display_live_results(results: List[Tuple[str, dict]]) -> None:
    """Prints formatted results to console"""
    print(f"\nFound {len(results)} profitable items\n")
    print(
        f"{'Item Name':<35}  |  {'Profit':>7}  |  "
        f"{'Margin %':>8}  |  {'Restock Time':>9}"
    )
    print("-"*75)
    
    for item_id, stats in results[:Config.CONSOLE_DISPLAY]:
        print(
            f"{stats['name']:<35}  |  "
            f"{stats['profit']:>7.1f}  |  "
            f"{stats['margin']:>7.1f}%  |  "
            f"{stats['restock_hours']:>7.2f}h"
        )

def countdown_timer(seconds: int) -> None:
    """Displays refresh countdown"""
    for remaining in range(seconds, 0, -1):
        print(f"Next update in {remaining} seconds...", end='\r')
        time.sleep(1)

def main():
    """Main application loop"""
    while True:
        try:
            clear_terminal()
            print("Hypixel Bazaar Flipper - RealTime Flip Finder\n")
            
            bazaar_data, item_registry = get_market_data()
            
            if analysis_results := calculate_profits(bazaar_data, item_registry):
                save_analysis(analysis_results)
                display_live_results(analysis_results)
                print(f"\nTop {Config.MAX_RESULTS} results saved to bazaar_flips.txt")
            else:
                print("No profitable opportunities found this cycle")
            
            countdown_timer(Config.UPDATE_INTERVAL)
            
        except KeyboardInterrupt:
            print("\nGoodbye! Happy trading!")
            break
        except Exception as error:
            print(f"Error occurred: {str(error)}")
            time.sleep(Config.UPDATE_INTERVAL)

if __name__ == "__main__":
    main()