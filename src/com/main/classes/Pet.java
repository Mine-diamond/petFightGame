package com.main.classes;

import java.util.ArrayList;
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
        double attackMultiplier = attributes.getAttackMultiplier();
        double defenseMultiplier = attributes.getDefenseMultiplier();
        double hpMultiplier = attributes.getHPMultiplier();
        double energyMultiplier = attributes.getEnergyMultiplier();

        baseMaxHP = (int) (growth.getHpGrowth() * level * hpMultiplier);
        baseMaxEnergy = (int) (growth.getEnergyGrowth() * level * energyMultiplier);
        baseAttack = (int) (growth.getAttackGrowth() * level * attackMultiplier);
        baseDefense = (int) (growth.getDefenseGrowth() * level * defenseMultiplier);
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
                "%s [name=%s, type=%s, level=%d, exp=%d, HP=%d/%d, ATK=%d, DEF=%d]",
                getClass().getSimpleName(), name, type, level, experience, currentHP, maxHP, currentAttack, currentDefense
        );
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public int getLevel(){
        return level;
    }

    public void setExperience(int experience){
        this.experience = experience;
    }

    public int getExperience(){
        return experience;
    }

    public boolean addExperience(int experience){//增加经验，升级时返回true

        if(experience < 0){
            throw new IllegalArgumentException("experience must be a positive integer");
        }

        if (level >= levelExpRequirements.size()) {return false;}

        if(this.experience + experience >= levelExpRequirements.get(level)){
            experience -= levelExpRequirements.get(level) - this.experience;
            level++;
            setBaseValue();//更新属性变量值
            unifiedValue();//更新其他变量值
            this.experience = 0;
            addExperience(experience);
            return true;//升级
        }else {
            this.experience += experience;
            return false;//未升级
        }
    }

    public void addHP(int hp){//增加血量
        if(hp < 0){
            throw new IllegalArgumentException("hp must be a positive integer");
        }

        if (this.currentHP + hp >= maxHP) {
            currentHP = maxHP;
        }else {
            currentHP += hp;
        }
    }

    public boolean removeHP(int hp){//减少血量，为0（死亡）时返回true
        if(hp < 0){
            throw new IllegalArgumentException("hp must be a positive integer");
        }

        if (this.currentHP - hp >= 0) {
            currentHP -= hp;
            return false;
        }else {
            this.currentHP = 0;
            return true;
        }
    }

    public void addEnergy(int energy){//增加能量
        if(energy < 0){
            throw new IllegalArgumentException("energy must be a positive integer");
        }

        if (this.currentEnergy + energy >= maxEnergy) {
            currentEnergy = maxEnergy;
        }else {
            currentEnergy += energy;
        }
    }

    public boolean removeEnergy(int energy){//减少能量，为0时返回true
        if(energy < 0){
            throw new IllegalArgumentException("energy must be a positive integer");
        }

        if (this.currentEnergy - energy >= 0) {
            currentEnergy -= energy;
            return false;
        }else {
            this.currentEnergy = 0;
            return true;
        }
    }
}





