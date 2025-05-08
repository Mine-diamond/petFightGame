package com.main.items;

import com.main.classes.Player;
import com.main.pets.Pet;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


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
        super("food","食物",Type.FOOD,"这是食物",Rarity.COMMON,true,"food");
    }

    protected FoodItem(String name,String chineseName, Type type, String description, Rarity rarity, boolean canStack,String typeID) {
        super(name,chineseName,type,description,rarity,canStack,typeID);
    }

    @Override
    public void use(Player player, Pet pet1, Pet pet2) {
        eat(pet1);
    }

    public void eat(Pet pet) {
        pet.addHP(healthRecovered);
    }

    public void eat(int amount, Pet pet) {
        pet.addHP(amount * healthRecovered);
    }

    public static class FoodItemBuilder extends ItemBuilder {

        private double healthRecovered;

        public FoodItemBuilder setHealthRecovered(double healthRecovered) {this.healthRecovered = healthRecovered;return this;}

        public FoodItem build(){
            FoodItem item = new FoodItem(super.name,super.chineseName, super.type, super.description, super.rarity, super.canStack,super.typeId){};
            item.setHealthRecovered(healthRecovered);
            return item;
        }
    }

    //深拷贝构造器
    public FoodItem(FoodItem other){
        super(other);
        this.healthRecovered = other.healthRecovered;
    }

    //深拷贝构造器,指定UUID
    public FoodItem(FoodItem other,UUID uuid){
        super(other,uuid);
        this.healthRecovered = other.healthRecovered;
    }

    //创建新实例
    public FoodItem newInstant(){
        return new FoodItem(this){};
    }

    public FoodItem newInstant(UUID uuid){return new FoodItem(this,uuid){};}
}
