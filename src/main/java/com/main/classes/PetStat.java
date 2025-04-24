package com.main.classes;

public class PetStat {
    ResourceValue HP;
    ResourceValue energy;
    ValueModifier baseAttack;
    ValueModifier.ModifiedValue currentAttack;
    ValueModifier baseDefense;
    ValueModifier.ModifiedValue currentDefense;

    public PetStat() {}

    public PetStat(ResourceValue HP,ResourceValue energy,ValueModifier baseAttack,ValueModifier baseDefense) {
        this.HP = HP;
        this.energy = energy;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        currentAttack = baseAttack.createModifiedValue();
        currentDefense = baseDefense.createModifiedValue();
    }

    public PetStat(double hp, double energy,double attack,double defense) {
        this.HP = new ResourceValue(hp);
        this.energy = new ResourceValue(energy);
        this.baseAttack = new ValueModifier(attack);
        currentAttack = baseAttack.createModifiedValue();
        this.baseDefense = new ValueModifier(defense);
        currentDefense = baseDefense.createModifiedValue();
    }

    public ResourceValue getHP() {
        return HP;
    }

    public ResourceValue getEnergy() {
        return energy;
    }

    public ValueModifier getBaseAttack(){
        return baseAttack;
    }

    public ValueModifier getBaseDefense(){
        return baseDefense;
    }

    public ValueModifier.ModifiedValue getCurrentAttack(){
        return currentAttack;
    }

    public ValueModifier.ModifiedValue getCurrentDefense(){
        return currentDefense;
    }
}
