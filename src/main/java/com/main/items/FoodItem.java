package com.main.items;

import com.main.classes.Player;
import com.main.pets.Pet;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public abstract class FoodItem extends Item{

//    //基本属性(不可变)
//    protected String name = "食物";
//    protected Type type = Type.FOOD;
//    protected String description = "这是食物";
//    protected Rarity rarity = Rarity.COMMON;
//    protected boolean canStack = true;

    //基本属性(FOOD)
    protected double healthRecovered = 0;

    protected FoodItem() {
        super("food","食物",Type.FOOD,"这是食物",Rarity.COMMON,true);
    }

    protected FoodItem(String name, Type type, String description, Rarity rarity, boolean canStack) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.rarity = rarity;
        this.canStack = canStack;
    }

    @Override
    public void use(Player player, Pet pet1, Pet pet2) {
        eat(pet1);
    }

    public void eat(Pet pet) {
        pet.addHP(healthRecovered);
    }

    public static class FoodItemBuilder extends ItemBuilder {
        private String name = "item";
        private String chineseName = "物品";
        private Type type = Type.NONE;
        private String description = "这是一个物品";
        private Rarity rarity = Rarity.COMMON;
        private boolean canStack = false;

        private double healthRecovered;

        public FoodItemBuilder setHealthRecovered(double healthRecovered) {this.healthRecovered = healthRecovered;return this;}

        public FoodItem build(){
            FoodItem item = new FoodItem(name, type, description, rarity, canStack){};
            item.setHealthRecovered(healthRecovered);
            return item;
        }
    }
}
