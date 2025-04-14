package com.main.pets;

import com.main.classes.Element;
import com.main.classes.PetStat;
import com.main.skills.Skill;

import java.util.HashMap;
import java.util.LinkedHashSet;

public abstract class Pet {
    enum Statue {
        Fight, Free
    }
    String name;//名称
    String type;//种类
    PetStat stat;//宠物各个属性
    Statue statue = Statue.Free;//状态

    int level;//等级
    int experience;//目前经验值

    Attributes attributes;//天赋
    Element[] elements;//元素
    LinkedHashSet<Skill> skills;//技能集合
    HashMap<Integer,Integer> levelExpRequirements = new HashMap<>();//升级需要的经验值
    GrowthAttribute growth;//能力随等级成长曲线


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

    public void setBaseValue(){
        double attackMultiplier = attributes.getAttackMultiplier();
        double defenseMultiplier = attributes.getDefenseMultiplier();
        double hpMultiplier = attributes.getHPMultiplier();
        double energyMultiplier = attributes.getEnergyMultiplier();

        stat.getHP().setBaseMaxValue((growth.getHpGrowth() * level * hpMultiplier),"初始化");
        stat.getEnergy().setBaseMaxValue((growth.getEnergyGrowth() * level * energyMultiplier),"初始化");
        stat.getBaseAttack().setBaseValue((growth.getAttackGrowth() * level * attackMultiplier),"初始化");
        stat.getBaseDefense().setBaseValue((growth.getDefenseGrowth() * level * defenseMultiplier),"初始化");
    }

    public void unifiedValue(){
        stat.getHP().unifiedAll();
        stat.getEnergy().unifiedAll();
        stat.getBaseAttack().clearAllModifiers();
        stat.getBaseDefense().clearAllModifiers();
    }

    public PetStat getStat() {
        return stat;
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

    //------------------------技能相关方法--------------------------
    /**
     * 添加技能
     * @param skill 要添加的技能
     * @return 是否成功添加
     */
    public boolean addSkills(Skill skill) {
        if(Skill.ifAbleAddSkill(skill,this.elements)){
            return this.skills.add(skill);
        }
        return false;
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
                getClass().getSimpleName(), name, type, level, experience, stat.getHP().getValue(), stat.getHP().getCurrentMaxValue(),stat.getEnergy().getValue(), stat.getEnergy().getCurrentMaxValue(),stat.getCurrentAttack(), stat.getCurrentDefense()
        );
    }
}
