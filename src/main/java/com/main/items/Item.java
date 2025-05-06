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
        FOOD
    }

    //稀有度
    public enum Rarity{
        COMMON,RARE,EPIC
    }

    //基本属性(不可变)
    protected String name;
    protected Type type;
    protected String description;
    protected Rarity rarity;

    //实例数据(可变)
    //这里有0个:),需子类定义

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
                .append("类型：").append(type)
                .append("描述：").append(description)
                .append("稀有度：").append(rarity);
        return sb.toString();
    }

}



