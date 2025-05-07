package com.main.items;

import java.util.HashMap;
import java.util.LinkedHashSet;

public class ItemLibrary {
    private static HashMap<String,Item> items = new HashMap();

    private static void registerItem(String itemID, Item item) {
        items.put(itemID,item);
    }

    static {
        registerItem("apple", new FoodItem.FoodItemBuilder()
                .setHealthRecovered(10)
                .setName("Apple")
                .setChineseName("苹果")
                .setType(Item.Type.FOOD)
                .setRarity(Item.Rarity.COMMON)
                .setCanStack(true)
                .setDescription("普通的苹果，吃下后恢复十点生命值")
                .build()
        );
    }

}


