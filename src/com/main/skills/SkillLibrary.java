package com.main.skills;

import com.main.classes.Element;

public class SkillLibrary {

    public static final Skill skillFireBall = new Skill.SkillBuilder()
            .setName("火球术")
            .setElement(Element.fire)
            .setType(Skill.SkillType.ATTACK)
            .setTargetType(Skill.TargetType.ENEMY)
            .setEnergyCost(20)
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
            .setEnergyCost(30)
            .setEffect(
                    (caster, target) -> {
                        int damage = caster.getBaseAttack();
                        target.removeHP(damage);
                    }

            ).build();


}

