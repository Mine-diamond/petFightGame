package com.main.game;

import com.main.pets.Pet;

public class BattleSystem {

    public static void battle(){

    }

    public static void fight(Pet pet1, Pet pet2){

    }

    public static int getAttackValue(int attcak){
        return 0;
    }

    public static int getEnergyCost(int costEnergy, Pet pet){
        return pet.getCurrentEnergy() - costEnergy > 0 ? costEnergy : 0;
    }

    public int getRecover(int recover) {
        return recover;
    }

}
