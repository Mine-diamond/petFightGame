package com.main.items;

import com.main.classes.Player;
import com.main.pets.Pet;

import lombok.Getter;
import lombok.Setter;

import java.util.function.BiFunction;
import java.util.function.Function;

@Setter
@Getter
//Item父类，需要子类定义具体物品
public abstract class Item{

    //种类
    public enum Type{
        NONE,FOOD,POTION
    }

    //稀有度
    public enum Rarity{
        COMMON,RARE,EPIC
    }

    //基本属性(不可变)
    protected String name;
    protected String chineseName;
    protected Type type;
    protected String description;
    protected Rarity rarity;
    protected boolean canStack;

    //实例数据(可变)
    //这里有0个:),需子类定义

    public Item(){}

    public Item(String name,String ChineseName, Type type, String description, Rarity rarity, boolean canStack){
        this.name = name;
        this.chineseName = ChineseName;
        this.type = type;
        this.description = description;
        this.rarity = rarity;
        this.canStack = canStack;
    }

    //基本方法
    public abstract void use(Player player,Pet pet1,Pet pet2);

    /**
     * 基础类型安全转换
     */
    public static <T extends Item> T cast(Item item, Class<T> type) {
        if (!type.isInstance(item)) {
            throw new IllegalArgumentException("类型不匹配: 需要" + type.getSimpleName()
                    + "，实际是" + item.getClass().getSimpleName());
        }
        return type.cast(item);
    }

    // ===== 无额外参数 =====
    public static <T extends Item, R> R with(
            Item item, Class<T> type,
            Function<T, R> action
    ) {
        return action.apply(cast(item, type));
    }

    // ===== 单个Pet参数 =====
    public static <T extends Item, R> R with(
            Item item, Class<T> type,
            Pet pet,
            BiFunction<T, Pet, R> action
    ) {
        return action.apply(cast(item, type), pet);
    }

    // ===== 两个Pet参数 =====
    public static <T extends Item, R> R with(
            Item item, Class<T> type,
            Pet pet1, Pet pet2,
            TriFunction<T, Pet, Pet, R> action
    ) {
        return action.apply(cast(item, type), pet1, pet2);
    }

    // ===== Player参数 =====
    public static <T extends Item, R> R with(
            Item item, Class<T> type,
            Player player,
            BiFunction<T, Player, R> action
    ) {
        return action.apply(cast(item, type), player);
    }

    // ===== Player + Pet参数 =====
    public static <T extends Item, R> R with(
            Item item, Class<T> type,
            Player player, Pet pet,
            TriFunction<T, Player, Pet, R> action
    ) {
        return action.apply(cast(item, type), player, pet);
    }

    // ===== 通用版本 =====
    /**
     * 通用参数处理版本（当上述方法不满足需求时使用）
     */
    public static <T extends Item, R> R withContext(
            Item item, Class<T> type,
            Object context,
            BiFunction<T, Object, R> action
    ) {
        return action.apply(cast(item, type), context);
    }

    // 三参数函数接口
    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    //建造者类
    public static class ItemBuilder{
        private String name = "item";
        private String chineseName = "物品";
        private Type type = Type.NONE;
        private String description = "这是一个物品";
        private Rarity rarity = Rarity.COMMON;
        private boolean canStack = false;

        public ItemBuilder(){}
        public ItemBuilder setName(String name){this.name = name; return this;}
        public ItemBuilder setChineseName(String ChineseName){this.chineseName = ChineseName;return this;}
        public ItemBuilder setType(Type type){this.type = type; return this;}
        public ItemBuilder setDescription(String description){this.description = description;return this;}
        public ItemBuilder setRarity(Rarity rarity){this.rarity = rarity; return this;}
        public ItemBuilder setCanStack(boolean canStack){this.canStack = canStack;return this;}


        public Item build(){
            return new Item(name, chineseName,type,description,rarity,canStack) {
                @Override
                public void use(Player player, Pet pet1, Pet pet2) {
                    //在子类中定义
                }
            };
        }
    }


    //仅测试用
    public String test(){
        System.out.println("Test");
        return "test";
    }

    //toString方法
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("物品信息：")
                .append("名称：").append(name)
                .append(", 类型：").append(type)
                .append(", 描述：").append(description)
                .append(", 稀有度：").append(rarity);
        return sb.toString();
    }

}



