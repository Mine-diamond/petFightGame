package com.main.pets;

import com.main.classes.Element;
import com.main.skills.Skill;

import java.util.*;
import java.util.function.Consumer;

/**
 * 宠物抽象基类，定义了所有宠物的共有属性和行为
 */
public abstract class Pet {

    enum Statue {
        Fight, Free
    }

    //------------------------基本属性--------------------------
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
    LinkedHashSet<Skill> skills;//技能集合
    GrowthAttribute growth;//能力随等级成长曲线

    Statue statue = Statue.Free;//状态

    HashMap<Integer,Integer> levelExpRequirements = new HashMap<>();//升级需要的经验值

    //------------------------构造方法--------------------------
    /**
     * 宠物构造函数
     * @param level 初始等级
     * @param attributes 天赋属性
     * @param skills 初始技能集
     */
    public Pet(int level, Attributes attributes, LinkedHashSet<Skill> skills) {
        this.level = level;
        this.attributes = attributes;
        this.skills = skills != null ? skills : new LinkedHashSet<>();
        this.experience = 0;
        this.growth = createGrowthAttribute(); // 工厂方法创建成长属性
        //其他变量初始化
        setBaseValue();
        unifiedValue();
    }

    //------------------------抽象方法与内部类--------------------------
    /**
     * 创建宠物成长属性的抽象方法，由子类实现
     */
    protected abstract GrowthAttribute createGrowthAttribute();

    /**
     * 宠物成长属性抽象类，定义了成长相关的抽象方法
     */
    public static abstract class GrowthAttribute {
        public abstract int getHpGrowth();
        public abstract int getAttackGrowth();
        public abstract int getDefenseGrowth();
        public abstract int getEnergyGrowth();
    }

    //------------------------属性计算方法--------------------------
    /**
     * 根据等级和天赋设置基础属性值
     */
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

    /**
     * 统一更新当前属性为基础属性值
     */
    public void unifiedValue() {
        maxHP = baseMaxHP;
        currentHP = baseMaxHP;

        maxEnergy = baseMaxEnergy;
        currentEnergy = baseMaxEnergy;

        currentDefense = baseDefense;
        currentAttack = baseAttack;
    }

    //------------------------基本信息相关方法--------------------------
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    //------------------------经验与升级相关方法--------------------------
    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getExperience() {
        return experience;
    }

    /**
     * 增加经验值，并处理可能的升级
     * @param experience 要增加的经验值
     * @return 是否发生升级
     */
    public boolean addExperience(int experience) {
        if(experience < 0) {
            throw new IllegalArgumentException("experience must be a positive integer");
        }

        if (level >= levelExpRequirements.size()) {
            return false;
        }

        if(this.experience + experience >= levelExpRequirements.get(level)) {
            experience -= levelExpRequirements.get(level) - this.experience;
            level++;
            setBaseValue();//更新属性变量值
            unifiedValue();//更新其他变量值
            this.experience = 0;
            addExperience(experience);
            return true;//升级
        } else {
            this.experience += experience;
            return false;//未升级
        }
    }

    //------------------------血量相关方法--------------------------
    public int getBaseMaxHP() {
        return baseMaxHP;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    /**
     * 增加血量
     * @param hp 要增加的血量
     */
    public void addHP(int hp) {
        if(hp < 0) {
            throw new IllegalArgumentException("hp must be a positive integer");
        }

        if (this.currentHP + hp >= maxHP) {
            currentHP = maxHP;
        } else {
            currentHP += hp;
        }
    }

    /**
     * 减少血量
     * @param hp 要减少的血量
     * @return 是否死亡（HP降为0）
     */
    public boolean removeHP(int hp) {
        if(hp < 0) {
            throw new IllegalArgumentException("hp must be a positive integer");
        }

        if (this.currentHP - hp >= 0) {
            currentHP -= hp;
            return false;
        } else {
            this.currentHP = 0;
            return true;
        }
    }

    public boolean canRemoveHP(int hp) {
        if(hp < 0) {
            throw new IllegalArgumentException("hp must be a positive integer");
        }

        if (this.currentHP - hp >= 0) {
            return true;
        }

        return false;
    }

    //------------------------能量相关方法--------------------------
    public int getMaxEnergy() {
        return maxEnergy;
    }

    public int getBaseMaxEnergy() {
        return baseMaxEnergy;
    }

    public int getCurrentEnergy() {
        return currentEnergy;
    }

    /**
     * 增加能量
     * @param energy 要增加的能量
     */
    public void addEnergy(int energy) {
        if(energy < 0) {
            throw new IllegalArgumentException("energy must be a positive integer");
        }

        if (this.currentEnergy + energy >= maxEnergy) {
            currentEnergy = maxEnergy;
        } else {
            currentEnergy += energy;
        }
    }

    /**
     * 减少能量
     * @param energy 要减少的能量
     * @return 能量是否耗尽
     */
    public boolean removeEnergy(int energy) {
        if(energy < 0) {
            throw new IllegalArgumentException("energy must be a positive integer");
        }

        if (this.currentEnergy - energy >= 0) {
            currentEnergy -= energy;
            return false;
        } else {
            this.currentEnergy = 0;
            return true;
        }
    }

    public boolean canRemoveEnergy(int energy) {
        if(energy < 0) {
            throw new IllegalArgumentException("energy must be a positive integer");
        }
        if (this.currentEnergy - energy >= 0) {
            return true;
        }
        return false;
    }

    //------------------------攻击与防御相关方法--------------------------
    public int getBaseAttack() {
        return baseAttack;
    }

    public int getCurrentAttack() {
        return currentAttack;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public int getCurrentDefense() {
        return currentDefense;
    }


    //------------------------技能相关方法--------------------------
    /**
     * 添加技能
     * @param skill 要添加的技能
     * @return 是否成功添加
     */
    public boolean addSkills(Skill skill) {
        return this.skills.add(skill);
    }

    /**
     * 获取所有技能的字符串表示
     * @return 格式化的技能列表字符串
     */
    public String getAllSkills() {
        StringBuilder skillsStr = new StringBuilder();
        int i = 0;
        for (Skill skill : skills) {
            i++;
            skillsStr.append(i).append(". ").append(skill.toString()).append("\n");
        }
        return skillsStr.toString();
    }

    /**
     * 获取技能数组
     * @return 所有技能的数组
     */
    public Skill[] getSkillsArray() {
        return this.skills.toArray(new Skill[0]);
    }

    //------------------------状态相关方法--------------------------
    /**
     * 设置宠物状态
     * @param statue 新状态
     */
    public void setStatue(Statue statue) {
        this.statue = statue;
        if(statue == Statue.Free) {
            unifiedValue();
        }
    }

    //------------------------辅助方法--------------------------
    @Override
    public String toString() {
        return String.format(
                "%s [name=%s, type=%s, level=%d, exp=%d, HP=%d/%d,Energy=%d/%d, ATK=%d, DEF=%d]",
                getClass().getSimpleName(), name, type, level, experience, currentHP, maxHP,currentEnergy, maxEnergy,currentAttack, currentDefense
        );
    }
}
