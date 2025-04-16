package com.main.skills;

import com.main.classes.Element;
import com.main.classes.ValueModifier;
import com.main.pets.Pet;

public abstract class Skill {
    // 基础属性（所有技能共有）
    protected String name;
    protected Element element;
    protected ValueModifier energyCost;
    protected boolean ifEnergyCost;
    protected SkillType type;
    protected TargetType targetType;
    protected String description;

    // 禁用来源集合：记录所有让该技能失效的来源
    private final java.util.Set<String> disableSources = new java.util.HashSet<>();

    public enum SkillType { ATTACK, DEFENSE, HEAL, STATUS }
    public enum TargetType { SELF, ENEMY }

    // 策略接口：定义技能效果逻辑
    public interface SkillEffect {
        void apply(Pet caster, Pet target);
    }

    // 构造器（子类调用）
    protected Skill(String name, Element element, ValueModifier energyCost, boolean ifEnergyCost,SkillType type, TargetType targetType, String description) {
        this.name = name;
        this.element = element;
        this.energyCost = energyCost;
        this.type = type;
        this.targetType = targetType;
        this.ifEnergyCost = ifEnergyCost;
        this.description = description;
    }

    // 抽象方法：子类或策略类实现具体效果
    public abstract boolean applyEffect(Pet caster, Pet target);

    // 建造者类（用于快速配置基础技能）
    public static class SkillBuilder {
        private String name;
        private Element element;
        private ValueModifier energyCost;
        private SkillType type;
        private SkillEffect effect;
        private TargetType targetType;
        private boolean ifEnergyCost;
        private String description;

        public SkillBuilder setName(String name) { this.name = name; return this; }
        public SkillBuilder setElement(Element element) { this.element = element; return this; }
        public SkillBuilder setEnergyCost(double cost, boolean ifCost) { this.energyCost = new ValueModifier(cost);this.ifEnergyCost = ifCost; return this; }
        //public SkillBuilder setIfEnergyCost(boolean ifCost) {this.ifEnergyCost = ifCost; return this; }
        public SkillBuilder setType(SkillType type) { this.type = type; return this; }
        public SkillBuilder setEffect(SkillEffect effect) { this.effect = effect; return this; }
        public SkillBuilder setTargetType(TargetType target) { this.targetType = target; return this; }
        public SkillBuilder setDescription(String description) { this.description = description; return this; }


        public Skill build() {
            return new Skill(name, element, energyCost, ifEnergyCost, type, targetType, description) {
                @Override
                public boolean applyEffect(Pet caster, Pet target) {

                    if (!this.isEnabled()) {
                        return false;
                    }

                    if(!caster.hasEnoughEnergy(energyCost.getFinalValue())) {
                        return false;
                    }

                    if(ifEnergyCost) {
                        caster.removeEnergy(energyCost.getFinalValue());
                    }
                    effect.apply(caster, target);
                    return true;
                }
            };
        }
    }

    // 添加禁用来源
    public void disable(String source) {
        disableSources.add(source);
    }

    // 移除禁用来源
    public void enable(String source) {
        disableSources.remove(source);
    }

    // 当前技能是否可用（未被禁用）
    public boolean isEnabled() {
        return disableSources.isEmpty();
    }


    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("名称:").append(name)
                .append(" 元素:").append(element.toString())
                .append(" 消耗能量值:").append(energyCost)
                .append(" 种类:").append(type.toString())
                .append(" 对象目标:").append(targetType.toString())
                .append(" 描述:").append(description)
                .append("是否被禁用:").append(isEnabled());
        return str.toString();
    }

    public static boolean isSameElement(Skill s1, Skill s2) {
        return s1.element == s2.element;
    }

    public static boolean isAbleAddSkill(Skill skill, Element[] elements) {
        boolean result = false;
        if(skill.element == Element.noElement) {
            result = true;
        }
        for(Element element : elements) {
            if(skill.element == element) {
                result = true;
            }
        }
        return result;
    }
}

