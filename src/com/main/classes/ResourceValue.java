package com.main.classes;

import lombok.Getter;
import lombok.Setter;

/*
 * 资源值类，用于表示游戏或应用中的资源（例如基础血量上限、当前血量上限、当前血量）。
 */
@Setter
@Getter
public class ResourceValue {
    private ValueModifier baseMaxValue; // 基础最大值（可修改），如基础血量上限。
    private ValueModifier.ModifiedValue currentMaxValue; // 计算后的当前最大值，受modifier影响。
    private ObservableDouble value; // 当前实际资源值，可能低于或等于最大值。

    // 用于修改器标签的过滤方式枚举
    enum tagFilter{
        INCLUDE, EXCLUDE
    }

    //空构造器
    public ResourceValue(){};

    //fromDTO() 构造器
    public ResourceValue(ValueModifier baseModifier, double currentValue) {
        this.baseMaxValue = baseModifier;
        this.currentMaxValue = baseModifier.createModifiedValue();
        this.value = new ObservableDouble(currentValue);
    }


    // 构造函数，传入基础值（例如基础血量），初始化所有数值。
    public ResourceValue(double baseValue) {
        baseMaxValue = new ValueModifier(baseValue);
        currentMaxValue = baseMaxValue.createModifiedValue();
        value = new ObservableDouble(currentMaxValue.getValue());
    }

    // 在基础最大值上增加指定值，并附带原因说明。
    public void addBaseMaxValue(double value,String reason) {
        baseMaxValue.modifyBaseValue(value,reason);
    }

    // 设置基础最大值为指定值，并附带原因说明。
    public void setBaseMaxValue(double value,String reason) {
        baseMaxValue.setBaseValue(value,reason);
    }

    // 为当前最大值添加一个加法修正（modifier），带id、标签和优先级。
    public void addCurrentMaxValueAddModifier(double value,String id,String tag,int priority) {
        baseMaxValue.addAdditiveModifier(id,value,tag,priority);
    }

    // 为当前最大值添加一个乘法修正，带id、标签和优先级。
    public void addCurrentMaxValueMultiplyModifier(double value,String id,String tag,int priority) {
        baseMaxValue.addMultiplicativeModifier(id,value,tag,priority);
    }

    // 为基础最大值添加基础乘法修正，带id、标签和优先级。
    public void addCurrentMaxValueBaseMultiplyModifier(double value,String id,String tag,int priority) {
        baseMaxValue.addBaseMultiplicativeModifier(id,value,tag,priority);
    }

    // 将当前值恢复到最大值（如满血状态）。
    public void refill(){
        value.set(currentMaxValue.getValue());
    }

    // 增加资源值，不超过当前最大值上限，若达到上限则返回false。
    public boolean addValue(double addValue){
        double newValue = value.getValue() + addValue < currentMaxValue.getValue() ? (value.doubleValue() + addValue):currentMaxValue.getValue();
        value.set(newValue);
        return value.getValue() + addValue < currentMaxValue.getValue();
    }

    // 减少资源值，不低于0，若减少后值为0则返回false。
    public boolean removeValue(double removeValue){
        double newValue = value.getValue() - removeValue > 0 ? (value.doubleValue() - removeValue):0;
        value.set(newValue);
        return value.getValue() - removeValue > 0;
    }

    // 检查资源是否足够减少指定的值。
    public boolean canRemoveValue(double removeValue){
        return value.getValue() - removeValue >= 0;
    }

    // 根据tagFilter和tag获取对应的ModifiedValue实例。
    public ValueModifier.ModifiedValue getModifiedValue(tagFilter tf,String tag){
        return switch (tf) {
            case INCLUDE -> baseMaxValue.createModifiedValueForTag(tag);
            case EXCLUDE -> baseMaxValue.createModifiedValueExcludingTag(tag);
            default -> throw new RuntimeException("Unknown tag filter");
        };
    }

    // 根据修正器ID删除修正器，返回删除是否成功。
    public boolean deleteModifierByID(String id){
        return baseMaxValue.removeModifier(id);
    }

    // 根据标签删除所有匹配的修正器，返回删除的数量。
    public int deleteModifierByTag(String tag){
        return baseMaxValue.removeModifiersByTag(tag);
    }

    // 清除所有修正器并同步当前值到最新的最大值。
    public void unifiedAll(){
        baseMaxValue.clearAllModifiers();
        value.set(currentMaxValue.getValue());
    }

    // 获取当前资源的ObservableDouble对象。
    public ObservableDouble getValue() {
        return value;
    }

    // 获取基础最大值（不含任何修正器影响）。
    public double getBaseValue() {
        return baseMaxValue.getCurrentBaseValue();
    }

    // 获取当前的最大值（包含所有修正器影响后的值）。
    public double getCurrentMaxValue() {
        return currentMaxValue.getValue();
    }

}
