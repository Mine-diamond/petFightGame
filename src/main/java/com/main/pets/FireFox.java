package com.main.pets;

import com.main.classes.Element;
import com.main.skills.Skill;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class FireFox extends Pet {

    public FireFox(int level, Attributes attributes, LinkedHashSet<Skill> skills) {
        super(level, attributes, skills);
        this.name = "火绒狐";
        this.type = "FireFox";
        this.elements = new Element[]{Element.fire}; // 明确元素属性
        for (int i = 0;i < 50;i++){
            levelExpRequirements.put(i+1,50 + 30*i);
        }
        setBaseValue();
        unifiedValue();
    }
    @Override
    protected GrowthAttribute createGrowthAttribute() {
        return new FireFoxGrowth();
    }


    class FireFoxGrowth extends Pet.GrowthAttribute {


        public int getHpGrowth() {
            return 250;
        }


        public int getAttackGrowth() {
            return 70;
        }


        public int getDefenseGrowth() {
            return 30;
        }


        public int getEnergyGrowth() {
            return 50;
        }
    }
}
