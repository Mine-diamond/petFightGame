package com.main.pets;

import com.main.classes.Element;
import com.main.skills.Skill;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class BlackTaurus extends Pet {

    public BlackTaurus(int level, Attributes attributes, LinkedHashSet<Skill> skills) {
        super(level, attributes, skills);
        this.name = "黑金牛";
        this.type = "黑金牛";
        this.elements = new Element[]{Element.fire}; // 明确元素属性
        for (int i = 0;i < 50;i++){
            levelExpRequirements.put(i+1,50 + 30*i);
        }
        setBaseValue();
        unifiedValue();
    }


    @Override
    protected GrowthAttribute createGrowthAttribute() {
        return new BlackTaurusGrowth();
    }

    class BlackTaurusGrowth extends Pet.GrowthAttribute {


        public int getHpGrowth() {
            return 250;
        }


        public int getAttackGrowth() {
            return 90;
        }


        public int getDefenseGrowth() {
            return 50;
        }


        public int getEnergyGrowth() {
            return 30;
        }
    }
}
