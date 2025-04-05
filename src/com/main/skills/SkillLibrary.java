package com.main.skills;

import com.main.classes.Element;

public class SkillLibrary {

    public static final Skill skillFireBall = new Skill.SkillBuilder()
            .setName("火球术")
            .setElement(Element.fire)
            .setType(Skill.SkillType.ATTACK)
            .setTargetType(Skill.TargetType.ENEMY)
            .setEnergyCost(20,true)
            .setDescription("释放火球产生伤害")
            .setEffect(
                    (caster, target) -> {
                        int damage = caster.getBaseAttack();
                        target.removeHP(damage);
                        }

            ).build();

    public static final Skill skillEarthShattering = new Skill.SkillBuilder()
            .setName("震地术")
            .setElement(Element.earth)
            .setType(Skill.SkillType.ATTACK)
            .setTargetType(Skill.TargetType.ENEMY)
            .setEnergyCost(30,true)
            .setDescription("用力震地，产生伤害")
            .setEffect(
                    (caster, target) -> {
                        int damage = caster.getBaseAttack();
                        target.removeHP(damage);
                    }

            ).build();

    public static final Skill skillNull = new Skill.SkillBuilder()
            .setName("无效果的技能")
            .setElement(Element.noElement)
            .setType(Skill.SkillType.ATTACK)
            .setTargetType(Skill.TargetType.ENEMY)
            .setEnergyCost(0,true)
            .setDescription("什么效果都没有的技能")
            .setEffect(
                    (caster, target) -> {

                    }

            ).build();

    public static final Skill skillRecover = new Skill.SkillBuilder()
            .setName("恢复术")
            .setElement(Element.noElement)
            .setType(Skill.SkillType.HEAL)
            .setTargetType(Skill.TargetType.SELF)
            .setEnergyCost(0,true)
            .setDescription("恢复25%血量和50%能量")
            .setEffect(
                    (caster, target) -> {
                        caster.addHP((int) (caster.getMaxHP() * 0.3));
                        caster.addEnergy((int) (caster.getMaxEnergy() * 0.5));
                    }

            ).build();

}

