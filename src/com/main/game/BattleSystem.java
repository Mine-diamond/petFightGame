package com.main.game;

import com.main.pets.Pet;
import com.main.skills.Skill;

import java.util.Scanner;

public class BattleSystem {
    enum Statue{//战斗状态
        AboutToStart,Fight,End
    };

    enum BattleResult {//战斗结果
        A_WIN, B_WIN, DRAW, TIMEOUT
    }

    int roundNumber;
    int maxRoundNumber;

    Statue statue;

    Pet pet1;
    Pet pet2;

    public BattleSystem(Pet pet1,Pet pet2,int maxRoundNumber){
        this.pet1 = pet1;
        this.pet2 = pet2;

        this.maxRoundNumber = maxRoundNumber;
        roundNumber = 0;

    }

    public BattleSystem(Pet pet1,Pet pet2){
        this.pet1 = pet1;
        this.pet2 = pet2;

        roundNumber = 0;
        maxRoundNumber = 30;
        statue = Statue.AboutToStart;
    }


    public void battleFlow(){
        statue = Statue.Fight;
        while (statue == Statue.Fight){
            if(roundNumber > maxRoundNumber){
                endBattle(BattleResult.TIMEOUT);
            }else {
                fightRound();
            }
            roundNumber++;
        }

    }

    public  void fightRound(){
        Skill skill1;
        Skill skill2;
        boolean success1 = false;
        do{
            skill1 = getSkill(pet1);
            success1 =  skill1.applyEffect(pet1,pet2);
        }while (!success1);

        System.out.println(pet1+"\n"+pet2);
        if(ifhasEndBattle()){
            endBattle(determineWinner());
            return;
        }

        boolean success2 = false;
        do{
            skill2 = getSkill(pet2);
            success2 =  skill2.applyEffect(pet2,pet1);
        }while (!success2);

        System.out.println(pet2+"\n"+pet1);
        if(ifhasEndBattle()){
            endBattle(determineWinner());
            return;
        }
    }

    public boolean ifhasEndBattle(){
        if(pet2.getCurrentHP() == 0 || pet1.getCurrentHP() == 0){
            return true;
        }
        return false;
    }

    public BattleResult determineWinner(){
        if(pet2.getCurrentHP() == 0 && pet1.getCurrentHP() == 0){
            return BattleResult.DRAW;
        } else if(pet2.getCurrentHP() == 0){
            return BattleResult.A_WIN;
        } else if (pet1.getCurrentHP() == 0){
            return BattleResult.B_WIN;
        } else {
            return BattleResult.DRAW;
        }
    }

    public void endBattle(BattleResult result){
        System.out.println(result);
        statue = Statue.End;
    }

    public Skill getSkill(Pet pet){
        System.out.println("请选择技能，目前所有技能:");
        System.out.println(pet.getAllSkills());
        Scanner sc = new Scanner(System.in);
        Skill skill = null;
        while (true){
            int choice = 0;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("输入的不是数字");
                continue;
            }
            if(choice > 0 && choice <=pet.getAllSkills().length()){
                skill = pet.getSkillsArray()[choice - 1];
                break;
            }else {
                System.out.println("输入的数有误");
                continue;
            }

        }
        return skill;
    }



    public static int getAttackValue(int attcak){
        return 0;
    }

    public static int getEnergyCost(int costEnergy, Pet pet){
        return pet.getCurrentEnergy() - costEnergy > 0 ? costEnergy : 0;
    }

    public static int getRecover(int recover) {
        return recover;
    }

}
