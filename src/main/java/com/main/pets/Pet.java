package com.main.pets;

import com.main.classes.Element;
import com.main.classes.PetStat;
import com.main.skills.Skill;
import com.main.storage.PetDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashSet;

@Setter
@Getter
public abstract class Pet {
    public enum Statue {
        Fight, Free
    }
    protected String name;//名称
    protected String type;//种类
    protected PetStat stat;//宠物各个属性
    protected Statue statue = Statue.Free;//状态

    protected int level;//等级
    protected int experience;//目前经验值

    protected Attributes attributes;//天赋
    protected Element[] elements;//元素
    protected LinkedHashSet<Skill> skills;//技能集合
    protected HashMap<Integer,Integer> levelExpRequirements = new HashMap<>();//升级需要的经验值
    protected GrowthAttribute growth;//能力随等级成长曲线


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

    public Pet(PetDTO petDTO) {

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

        if(this.stat == null ){
            stat = new PetStat((growth.getHpGrowth() * level * hpMultiplier),(growth.getEnergyGrowth() * level * energyMultiplier),(growth.getAttackGrowth() * level * attackMultiplier),(growth.getDefenseGrowth() * level * defenseMultiplier));
        } else {
            stat.getHP().setBaseMaxValue((growth.getHpGrowth() * level * hpMultiplier),"更新基值");
            stat.getEnergy().setBaseMaxValue((growth.getEnergyGrowth() * level * energyMultiplier),"更新基值");
            stat.getBaseAttack().setBaseValue((growth.getAttackGrowth() * level * attackMultiplier),"更新基值");
            stat.getBaseDefense().setBaseValue((growth.getDefenseGrowth() * level * defenseMultiplier),"更新基值");
        }


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
        if(Skill.isAbleAddSkill(skill,this.elements)){
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

    //------------------------元素相关方法--------------------------
    public Element[] getElementArray() {
        return elements;
    }

    public Element[] getCounteredElement(){
        Element[] counteredElements = new Element[elements.length];
        for (int i = 0; i < elements.length; i++) {
            counteredElements[i] = elements[i].getCounteredElement();
        }
        return counteredElements;
    }

    public boolean isCounteredBy(Element other) {
        boolean result = false;
        for (Element element : elements) {
            result = element.counters(other);
            if(result) {
                break;
            }
        }
        return result;
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


    //------------------------HP相关方法--------------------------
    /**
     * 在基础HP最大值上增加指定值
     * @param value 增加的值
     * @param reason 原因说明
     */
    public void addBaseMaxHP(double value, String reason) {
        stat.getHP().addBaseMaxValue(value, reason);
    }
    /**
     * 设置基础HP最大值
     * @param value 设置的值
     * @param reason 原因说明
     */
    public void setBaseMaxHP(double value, String reason) {
        stat.getHP().setBaseMaxValue(value, reason);
    }
    /**
     * 为当前HP最大值添加一个加法修正
     * @param value 修正值
     * @param id 修正器ID
     * @param tag 标签
     * @param priority 优先级
     */
    public void addHPAddModifier(double value, String id, String tag, int priority) {
        stat.getHP().addCurrentMaxValueAddModifier(value, id, tag, priority);
    }
    /**
     * 为当前HP最大值添加一个乘法修正
     * @param value 修正值
     * @param id 修正器ID
     * @param tag 标签
     * @param priority 优先级
     */
    public void addHPMultiplyModifier(double value, String id, String tag, int priority) {
        stat.getHP().addCurrentMaxValueMultiplyModifier(value, id, tag, priority);
    }

    /**
     * 为当前HP最大值添加一个乘法修正
     * @param value 修正值
     * @param id 修正器ID
     * @param tag 标签
     * @param priority 优先级
     */
    public void addHPBaseMultiplyModifier(double value, String id, String tag, int priority) {
        stat.getHP().addCurrentMaxValueBaseMultiplyModifier(value, id, tag, priority);
    }
    /**
     * 恢复HP到最大值
     */
    public void refillHP() {
        stat.getHP().refill();
    }
    /**
     * 增加HP值
     * @param addValue 增加的值
     * @return 是否未达到上限
     */
    public boolean addHP(double addValue) {
        return stat.getHP().addValue(addValue);
    }
    /**
     * 减少HP值
     * @param removeValue 减少的值
     * @return 是否未降至0
     */
    public boolean removeHP(double removeValue) {
        return stat.getHP().removeValue(removeValue);
    }
    /**
     * 获取当前HP值
     * @return 当前HP
     */
    public double getCurrentHP() {
        return stat.getHP().getValue().doubleValue();
    }
    /**
     * 获取最大HP值
     * @return 最大HP
     */
    public double getMaxHP() {
        return stat.getHP().getCurrentMaxValue();
    }
    //------------------------能量相关方法--------------------------
    /**
     * 在基础能量最大值上增加指定值
     * @param value 增加的值
     * @param reason 原因说明
     */
    public void addBaseMaxEnergy(double value, String reason) {
        stat.getEnergy().addBaseMaxValue(value, reason);
    }
    /**
     * 设置基础能量最大值
     * @param value 设置的值
     * @param reason 原因说明
     */
    public void setBaseMaxEnergy(double value, String reason) {
        stat.getEnergy().setBaseMaxValue(value, reason);
    }
    /**
     * 为当前能量最大值添加一个加法修正
     * @param value 修正值
     * @param id 修正器ID
     * @param tag 标签
     * @param priority 优先级
     */
    public void addEnergyAddModifier(double value, String id, String tag, int priority) {
        stat.getEnergy().addCurrentMaxValueAddModifier(value, id, tag, priority);
    }
    /**
     * 为当前能量最大值添加一个乘法修正
     * @param value 修正值
     * @param id 修正器ID
     * @param tag 标签
     * @param priority 优先级
     */
    public void addEnergyMultiplyModifier(double value, String id, String tag, int priority) {
        stat.getEnergy().addCurrentMaxValueMultiplyModifier(value, id, tag, priority);
    }

    /**
     * 为当前能量最大值添加一个乘法修正
     * @param value 修正值
     * @param id 修正器ID
     * @param tag 标签
     * @param priority 优先级
     */
    public void addEnergyBaseMultiplyModifier(double value, String id, String tag, int priority) {
        stat.getEnergy().addCurrentMaxValueBaseMultiplyModifier(value, id, tag, priority);
    }

    /**
     * 恢复能量到最大值
     */
    public void refillEnergy() {
        stat.getEnergy().refill();
    }
    /**
     * 增加能量值
     * @param addValue 增加的值
     * @return 是否未达到上限
     */
    public boolean addEnergy(double addValue) {
        return stat.getEnergy().addValue(addValue);
    }
    /**
     * 减少能量值
     * @param removeValue 减少的值
     * @return 是否未降至0
     */
    public boolean removeEnergy(double removeValue) {
        return stat.getEnergy().removeValue(removeValue);
    }
    /**
     * 检查能量是否足够
     * @param value 需要检查的值
     * @return 是否足够
     */
    public boolean hasEnoughEnergy(double value) {
        return stat.getEnergy().canRemoveValue(value);
    }
    /**
     * 获取当前能量值
     * @return 当前能量
     */
    public double getCurrentEnergy() {
        return stat.getEnergy().getValue().doubleValue();
    }
    /**
     * 获取最大能量值
     * @return 最大能量
     */
    public double getMaxEnergy() {
        return stat.getEnergy().getCurrentMaxValue();
    }
    //------------------------攻击相关方法--------------------------
    /**
     * 设置基础攻击力
     * @param value 设置的值
     * @param reason 原因说明
     */
    public void setBaseAttack(double value, String reason) {
        stat.getBaseAttack().setBaseValue(value, reason);
    }
    /**
     * 增加基础攻击力
     * @param value 增加的值
     * @param reason 原因说明
     */
    public void addBaseAttack(double value, String reason) {
        stat.getBaseAttack().modifyBaseValue(value, reason);
    }
    /**
     * 为攻击力添加加法修正
     * @param value 修正值
     * @param id 修正器ID
     * @param tag 标签
     * @param priority 优先级
     */
    public void addAttackAddModifier(double value, String id, String tag, int priority) {
        stat.getBaseAttack().addAdditiveModifier(id, value, tag, priority);
    }
    /**
     * 为攻击力添加乘法修正
     * @param value 修正值
     * @param id 修正器ID
     * @param tag 标签
     * @param priority 优先级
     */
    public void addAttackMultiplyModifier(double value, String id, String tag, int priority) {
        stat.getBaseAttack().addMultiplicativeModifier(id, value, tag, priority);
    }
    /**
     * 为攻击力添加乘法修正
     * @param value 修正值
     * @param id 修正器ID
     * @param tag 标签
     * @param priority 优先级
     */
    public void addAttackBaseMultiplyModifier(double value, String id, String tag, int priority) {
        stat.getBaseAttack().addBaseMultiplicativeModifier(id, value, tag, priority);
    }
    /**
     * 获取当前攻击力
     * @return 当前攻击力
     */
    public double getCurrentAttack() {
        return stat.getCurrentAttack().getValue();
    }
    /**
     * 获取基础攻击力
     * @return 基础攻击力
     */
    public double getBaseAttack() {
        return stat.getBaseAttack().getCurrentBaseValue();
    }
    //------------------------防御相关方法--------------------------
    /**
     * 设置基础防御力
     * @param value 设置的值
     * @param reason 原因说明
     */
    public void setBaseDefense(double value, String reason) {
        stat.getBaseDefense().setBaseValue(value, reason);
    }
    /**
     * 增加基础防御力
     * @param value 增加的值
     * @param reason 原因说明
     */
    public void addBaseDefense(double value, String reason) {
        stat.getBaseDefense().modifyBaseValue(value, reason);
    }
    /**
     * 为防御力添加加法修正
     * @param value 修正值
     * @param id 修正器ID
     * @param tag 标签
     * @param priority 优先级
     */
    public void addDefenseAddModifier(double value, String id, String tag, int priority) {
        stat.getBaseDefense().addAdditiveModifier(id, value, tag, priority);
    }
    /**
     * 为防御力添加乘法修正
     * @param value 修正值
     * @param id 修正器ID
     * @param tag 标签
     * @param priority 优先级
     */
    public void addDefenseMultiplyModifier(double value, String id, String tag, int priority) {
        stat.getBaseDefense().addMultiplicativeModifier(id, value, tag, priority);
    }

    /**
     * 为防御力添加基础乘法修正
     * @param value 修正值
     * @param id 修正器ID
     * @param tag 标签
     * @param priority 优先级
     */
    public void addDefenseBAseMultiplyModifier(double value, String id, String tag, int priority) {
        stat.getBaseDefense().addBaseMultiplicativeModifier(id, value, tag, priority);
    }
    /**
     * 获取当前防御力
     * @return 当前防御力
     */
    public double getCurrentDefense() {
        return stat.getCurrentDefense().getValue();
    }
    /**
     * 获取基础防御力
     * @return 基础防御力
     */
    public double getBaseDefense() {
        return stat.getBaseDefense().getCurrentBaseValue();
    }


    //------------------------辅助方法--------------------------
    @Override
    public String toString() {
        return String.format(
                "%s [name=%s, type=%s, level=%d, exp=%d, HP=%.4f/%.4f,Energy=%.4f/%.4f, ATK=%.4f, DEF=%.4f]",
                getClass().getSimpleName(), name, type, level, experience, getCurrentHP(), getMaxHP(),getCurrentEnergy(), getMaxEnergy(),getCurrentAttack(), getCurrentDefense()
        );
    }
}
