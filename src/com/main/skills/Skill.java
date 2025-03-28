package com.main.skills;

import com.main.classes.Element;
import com.main.pets.Pet;

public abstract class Skill {
    // 基础属性（所有技能共有）
    protected String name;
    protected Element element;
    protected int energyCost;
    protected SkillType type;
    protected TargetType targetType;

    public enum SkillType { ATTACK, DEFENSE, HEAL, STATUS }
    public enum TargetType { SELF, ENEMY }

    // 策略接口：定义技能效果逻辑
    public interface SkillEffect {
        void apply(Pet caster, Pet target);
    }

    // 构造器（子类调用）
    protected Skill(String name, Element element, int energyCost, SkillType type, TargetType targetType) {
        this.name = name;
        this.element = element;
        this.energyCost = energyCost;
        this.type = type;
        this.targetType = targetType;
    }

    // 抽象方法：子类或策略类实现具体效果
    public abstract void applyEffect(Pet caster, Pet target);

    // 建造者类（用于快速配置基础技能）
    public static class SkillBuilder {
        private String name;
        private Element element;
        private int energyCost;
        private SkillType type;
        private SkillEffect effect;
        private TargetType targetType;

        public SkillBuilder setName(String name) { this.name = name; return this; }
        public SkillBuilder setElement(Element element) { this.element = element; return this; }
        public SkillBuilder setEnergyCost(int cost) { this.energyCost = cost; return this; }
        public SkillBuilder setType(SkillType type) { this.type = type; return this; }
        public SkillBuilder setEffect(SkillEffect effect) { this.effect = effect; return this; }
        public SkillBuilder setTargetType(TargetType target) { this.targetType = target; return this; }

        public Skill build() {
            return new Skill(name, element, energyCost, type, targetType) {
                @Override
                public void applyEffect(Pet caster, Pet target) {
                    effect.apply(caster, target);
                }
            };
        }
    }


    public String toString() {
        return name + " " + element.toString() + " " + energyCost + " " + type.toString() + " " + targetType.toString();
    }
}

