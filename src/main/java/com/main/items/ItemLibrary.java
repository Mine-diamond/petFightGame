package com.main.items;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.UUID;

public class ItemLibrary {
    private static HashMap<String,Item> items = new HashMap();

    private static void registerItem(Item item) {
        items.put(item.getTypeId(),item);
    }

    public static Item getItem(String itemID) {
        return items.getOrDefault(itemID,items.get("apple"));
    }

    public static Item getItemInstance(String itemID) {
        Item item = items.getOrDefault(itemID,items.get("apple"));
        Item itemInstant = item.newInstant();
        return itemInstant;
    }

    public static Item getItemInstance(String itemID, UUID uuid) {
        Item item = items.getOrDefault(itemID,items.get("apple"));
        Item itemInstant = item.newInstant(uuid);
        return itemInstant;
    }

    public static boolean hasItem(String itemID) {
        return items.containsKey(itemID);
    }

    static {
        registerItem(new FoodItem.FoodItemBuilder()
                .setHealthRecovered(10)
                .setName("Apple")
                .setChineseName("苹果")
                .setType(Item.Type.FOOD)
                .setRarity(Item.Rarity.COMMON)
                .setCanStack(true)
                .setDescription("普通的苹果，吃下后恢复十点生命值")
                .setTypeId("apple")
                .build()
        );

        registerItem(new FoodItem.FoodItemBuilder()
                .setHealthRecovered(30)
                .setName("goldenApple")
                .setChineseName("金苹果")
                .setType(Item.Type.FOOD)
                .setRarity(Item.Rarity.RARE)
                .setCanStack(true)
                .setDescription("金苹果，吃下后恢复三十点生命值")
                .setTypeId("goldenApple")
                .build()
        );
    }

}


