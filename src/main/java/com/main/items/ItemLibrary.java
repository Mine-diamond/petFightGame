package com.main.items;

import java.util.LinkedHashSet;

public class ItemLibrary {
    private static LinkedHashSet<Item> items;

    public static void registerItem(ItemFactory factory) {
        Item item =  factory.registerItem();
        items.add(item);
    }

    static {

    }

}

interface ItemFactory{
    Item registerItem();
}

