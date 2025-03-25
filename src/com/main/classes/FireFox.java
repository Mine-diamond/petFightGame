package com.main.classes;

import java.util.ArrayList;
import java.util.HashMap;

public class FireFox extends Pet {

    public FireFox(int level, Attributes attributes, ArrayList<Skill> skills) {
        super(level, attributes, skills);
        this.name = "FireFox";
        this.type = "Fire Element Pet";
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
            return 50;
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
