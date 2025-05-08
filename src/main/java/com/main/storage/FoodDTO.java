package com.main.storage;

import com.main.items.FoodItem;
import com.main.items.Item;
import com.main.items.ItemLibrary;

public class FoodDTO extends ItemDTO {

    @Override
    public void ItemDTO(Item item) {
        this.typeID = item.getTypeId();
        this.canStack = item.isCanStack();
        Item.with(item,FoodItem.class,food->{
            return food;
        });
    }

    @Override
    public Item toItem() {
        Item foodItem = ItemLibrary.getItem(typeID);
        return foodItem;
    }
}
