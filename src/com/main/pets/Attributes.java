package com.main.pets;

public enum Attributes {
    Offensive(1.15, 1.0, 0.9, 1.0),    // 攻击+15%，防御-10%
    Defensive(0.9, 1.15, 1.0, 1.0),   // 防御+15%，攻击-10%
    Balanced(1.05, 1.05, 1.05, 1.05), // 全属性+5%
    Support(1.0, 1.0, 1.1, 1.2);      // 血量+10%，能量+20%

    private final double attackMultiplier;
    private final double defenseMultiplier;
    private final double hpMultiplier;
    private final double energyMultiplier;

    Attributes(double attack, double defense, double hp, double energy) {
        this.attackMultiplier = attack;
        this.defenseMultiplier = defense;
        this.hpMultiplier = hp;
        this.energyMultiplier = energy;
    }

    public double getAttackMultiplier() {
        return attackMultiplier;
    }

    public double getDefenseMultiplier() {
        return defenseMultiplier;
    }

    public double getHPMultiplier() {
        return hpMultiplier;
    }

    public double getEnergyMultiplier() {
        return energyMultiplier;
    }
}
