package com.main.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public abstract class Pet {
    String name;//姓名
    String type;//种类

    int level;//等级
    int experience;//目前经验值

    int baseMaxHP;//最大血量
    int maxHP;//目前最大血量（技能或道具影响）
    int currentHP;//现在血量

    int baseMaxEnergy;//最大能量
    int maxEnergy;//目前最大能量（技能或道具影响）
    int currentEnergy;//现在能量

    int baseAttack;//基础攻击力
    int currentAttack;//现在攻击力（技能或道具影响）

    int baseDefense;//基础防御力
    int currentDefense;//现在防御力（技能或道具影响）

    Attributes attributes;//天赋
    Element[] elements;//元素
    ArrayList<Skill> skills;//技能集合
    GrowthAttribute growth;//能力随等级成长曲线

    HashMap<Integer,Integer> levelExpRequirements = new HashMap<>();//升级需要的经验值

    public Pet(int level,Attributes attributes,ArrayList<Skill> skills) {
        this.level = level;
        this.attributes = attributes;
        this.skills = skills != null ? skills : new ArrayList<>();
        this.experience = 0;
        this.growth = createGrowthAttribute(); // 工厂方法创建成长属性
        //其他变量初始化
        setBaseValue();
        unifiedValue();
    }

    // 抽象方法：子类必须实现成长属性逻辑
    protected abstract GrowthAttribute createGrowthAttribute();

    public static abstract class GrowthAttribute {
        public abstract int getHpGrowth();
        public abstract int getAttackGrowth();
        public abstract int getDefenseGrowth();
        public abstract int getEnergyGrowth();
    }
    protected void setBaseValue() {
        baseMaxHP = growth.getHpGrowth() * level;
        baseMaxEnergy = growth.getEnergyGrowth() * level;
        baseAttack = growth.getAttackGrowth() * level;
        baseDefense = growth.getDefenseGrowth() * level;
    }


    public void unifiedValue(){
        maxHP = baseMaxHP;
        currentHP = baseMaxHP;

        maxEnergy = baseMaxEnergy;
        currentEnergy = baseMaxEnergy;

        currentDefense = baseDefense;
        currentAttack = baseAttack;

    }


    @Override
    public String toString() {
        return String.format(
                "%s [name=%s, type=%s, level=%d, HP=%d/%d, ATK=%d, DEF=%d]",
                getClass().getSimpleName(), name, type, level, currentHP, maxHP, currentAttack, currentDefense
        );
    }
}





