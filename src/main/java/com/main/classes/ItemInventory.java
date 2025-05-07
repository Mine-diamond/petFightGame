package com.main.classes;

import com.main.items.FoodItem;
import com.main.items.Item;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Setter
@Getter
public class ItemInventory {

    private Map<Item, Integer> items = new LinkedHashMap<>();
    private Map<Item, Integer> Potions = new LinkedHashMap<>();

    public ItemInventory() {}
    public ItemInventory(Map<Item, Integer> items) {
        this.items = items;
    }


    public void itemClassificationwhenAdd(Item item) {
        int amount = items.getOrDefault(item, 0);
        switch (item.getType()){
            case POTION:
                Potions.put(item, amount);
        }
    }

    public void addItem(Item item, int amount) {
        items.put(item, items.getOrDefault(item, 0) + amount);
        itemClassificationwhenAdd(item);
    }

    public void addItem(String itemName, int amount) {
        items.put(this.getItemByName(itemName), items.getOrDefault(this.getItemByName(itemName), 0) + amount);
    }

    public boolean containsItem(Item item) {
        return items.containsKey(item);
    }

    public boolean containsItem(String itemName) {
        return items.containsKey(this.getItemByName(itemName));
    }

    public boolean containEnoughItem(Item item, int amount) {
        return items.containsKey(item) && items.get(item) >= amount;
    }

    public boolean containEnoughItem(String itemName, int amount) {
        return items.containsKey(this.getItemByName(itemName)) && items.get(this.getItemByName(itemName)) >= amount;
    }

    public void removeAllThisItem(Item item) {
        items.remove(item);
    }

    public void removeAllThisItem(String itemName) {
        items.remove(this.getItemByName(itemName));
    }

    public void removeSpecifiedNumberOfItems(Item item, int amount) {
        items.remove(item, amount);
    }

    public void removeSpecifiedNumberOfItem(String itemName, int amount) {
        items.remove(this.getItemByName(itemName), amount);
    }

    public Item getItemByName(String itemName) {
        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            if (entry.getKey().getName().equals(itemName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        ItemInventory itemInventory = new ItemInventory();
        Item a = new FoodItem.FoodItemBuilder()
                .setHealthRecovered(10)
                .build();
        itemInventory.addItem(a, 1);
        System.out.println(itemInventory.getItems());
    }


}

