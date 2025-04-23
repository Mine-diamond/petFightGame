package com.main.skills;

import com.main.classes.Element;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkillLibrary {

    public static List<Skill> getAllSkills() {
        List<Skill> skills = new ArrayList<>();
        // 通过反射获取所有字段
        Field[] fields = SkillLibrary.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())
                    && field.getType().equals(Skill.class)) {
                try {
                    Skill skill = (Skill) field.get(null);
                    if (skill != null) {
                        skills.add(skill);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return Collections.unmodifiableList(skills);
    }

    public enum Type{
        Origen,Chinese
    }

    public static Object getSkillByName(String name,Type type) throws ClassNotFoundException {
        switch (type) {
            case Origen:
            try {
                Field field = SkillLibrary.class.getField(name);
                return field.get(null); // 静态字段用 null
            } catch (Exception e) {
                throw new RuntimeException("找不到静态对象：" + name + "." + name, e);
            }
            case Chinese:
            List<Skill> skills = getAllSkills();
            for (Skill skill : skills) {
                if (name.equals(skill.getName())) {
                    return skill;
                }
            }
            throw new RuntimeException("找不到静态对象：" + name + "." + name);
        }
        return null;
    }

    public static final Skill skillBasicAttack = new Skill.SkillBuilder()
            .setName("基础攻击")
            .setElement(Element.noElement)
            .setType(Skill.SkillType.ATTACK)
            .setTargetType(Skill.TargetType.ENEMY)
            .setEnergyCost(0,true)
            .setDescription("普通攻击，不消耗能量")
            .setEffect(
                    (caster, target) -> {
                        double damage = caster.getCurrentAttack();
                        target.removeHP(damage);
                    }

            ).build();

    public static final Skill skillFireBall = new Skill.SkillBuilder()
            .setName("火球术")
            .setElement(Element.fire)
            .setType(Skill.SkillType.ATTACK)
            .setTargetType(Skill.TargetType.ENEMY)
            .setEnergyCost(20,true)
            .setDescription("释放火球产生伤害")
            .setEffect(
                    (caster, target) -> {
                        double damage = caster.getCurrentAttack();
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
                        double damage = caster.getCurrentAttack();
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
                        caster.addHP(caster.getMaxHP() * 0.3);
                        caster.addEnergy(caster.getMaxEnergy() * 0.5);
                    }

            ).build();

}

