package com.main.classes;

import java.util.ArrayList;

public class FireFox extends Pet {

    public FireFox(int level, Attributes attributes, ArrayList<Skill> skills) {
        super(level, attributes, skills);
        this.name = "FireFox";
        this.type = "Fire Element Pet";
        this.elements = new Element[]{Element.fire}; // 明确元素属性
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
