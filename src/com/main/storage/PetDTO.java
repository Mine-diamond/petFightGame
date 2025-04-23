package com.main.storage;

import com.main.classes.Element;
import com.main.pets.Attributes;
import com.main.pets.Pet;
import com.main.skills.Skill;
import static com.main.skills.SkillLibrary.getSkillByName;

import com.main.skills.SkillLibrary;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;

@Setter
@Getter
public class PetDTO {

    private String name;//名称
    private String type;//种类
    private PetStatDTO stat;//宠物各个属性
    private String statue;//状态

    private int level;//等级
    private int experience;//目前经验值

    private Attributes attributes;//天赋
    private LinkedHashSet<String> skills;//技能集合

    public PetDTO() {}

    public PetDTO(Pet pet) {
        name = pet.getName();
        type = pet.getType();
        stat = new PetStatDTO(pet.getStat()) ;
        statue = pet.getStatue().toString();
        level = pet.getLevel();
        experience = pet.getExperience();
        attributes = pet.getAttributes();
        LinkedHashSet<Skill> petSkills = pet.getSkills();
        skills = new LinkedHashSet<>();
        for (Skill skill : petSkills) {
            skills.add(skill.getName());
        }
    }

    public Pet toPet(){

        LinkedHashSet<Skill> petSkills = new LinkedHashSet<>();
        for (String skill : skills) {
            Skill s;
            try {
                s = (Skill) getSkillByName(skill, SkillLibrary.Type.Chinese);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            } catch (ClassCastException e) {
                e.printStackTrace();
                continue;
            }
            petSkills.add(s);
        }

        Pet pet;
        try {
            Class<?> clazz = Class.forName("com.main.pets." + getType());
            Constructor<?> constructor = clazz.getConstructor(int.class, Attributes.class, LinkedHashSet.class);
            pet = (Pet) constructor.newInstance(getLevel(), getAttributes(), petSkills);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        pet.setLevel(level);
        pet.setExperience(experience);
        pet.setStatue(Pet.Statue.valueOf(statue));
        pet.setStat(stat.toPetStat());

        return pet;
    }


}
