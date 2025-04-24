package com.main.game;

import com.main.pets.Pet;
import com.main.skills.Skill;

import java.util.Scanner;

public class BattleSystem {
    enum State {//战斗状态
        ABOUT_TO_START, FIGHT, END
    };

    enum BattleResult {//战斗结果
        A_WIN, B_WIN, DRAW, TIMEOUT
    }

    private static final Scanner SCANNER = new Scanner(System.in);

    private int roundNumber;
    private int maxRoundNumber;

    private State state;

    private Pet pet1;
    private Pet pet2;

    public BattleSystem(Pet pet1,Pet pet2,int maxRoundNumber){
        this.pet1 = pet1;
        this.pet2 = pet2;

        this.maxRoundNumber = maxRoundNumber;
        roundNumber = 0;
        this.state = State.ABOUT_TO_START;
    }

    public BattleSystem(Pet pet1,Pet pet2){
        this(pet1,pet2,30);
    }


    public void battleFlow() {
        state = State.FIGHT;
        // 循环直到达到最大回合或战斗提前结束
        while (state == State.FIGHT && roundNumber < maxRoundNumber) {
            System.out.println("第 " + (roundNumber + 1) + " 回合开始：");
            // 宠物1先出手
            if (executeTurn(pet1, pet2)) break;
            // 宠物2出手
            if (executeTurn(pet2, pet1)) break;
            roundNumber++;
        }
        // 超时判定
        if (state != State.END) {
            endBattle(BattleResult.TIMEOUT);
        }
    }

    /**
     * 执行单个宠物的回合：选择技能并持续尝试直至技能成功施放。
     * 输出双方状态，并在战斗结束时返回 true。
     */
    private boolean executeTurn(Pet attacker, Pet defender) {
        Skill chosenSkill;
        boolean success;
        do {
            chosenSkill = getSkill(attacker);
            success = chosenSkill.applyEffect(attacker, defender);
            if (!success) {
                System.out.println("技能未能成功施放，请重新选择技能。");
            }
        } while (!success);

        System.out.println("施放技能后状态：");
        System.out.println(attacker);
        System.out.println(defender);

        if (isBattleEnded()) {
            endBattle(determineWinner());
            return true;
        }
        return false;
    }

    public boolean isBattleEnded(){
        if(pet2.getStat().getHP().getValue().getValue() == 0 || pet1.getStat().getHP().getValue().getValue() == 0){
            return true;
        }
        return false;
    }

    public BattleResult determineWinner(){
        if(pet2.getStat().getHP().getValue().getValue() <= 0 && pet1.getStat().getHP().getValue().getValue() <= 0){
            return BattleResult.DRAW;
        } else if(pet2.getStat().getHP().getValue().getValue() <= 0){
            return BattleResult.A_WIN;
        } else if (pet1.getStat().getHP().getValue().getValue() <= 0){
            return BattleResult.B_WIN;
        } else {
            return BattleResult.DRAW;
        }
    }

    public void endBattle(BattleResult result){
        System.out.println("结果："+result);
        state = State.END;
    }

    public Skill getSkill(Pet pet){
        System.out.println("现在"+pet.getName()+"出招");
        System.out.println("请选择技能，目前所有技能:");
        System.out.println(pet.getAllSkills());
        Skill skill = null;
        while (true){
            int choice = 0;
            try {
                choice = Integer.parseInt(SCANNER.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("输入的不是数字");
                continue;
            }
            if(choice > 0 && choice <=pet.getSkillsArray().length){
                skill = pet.getSkillsArray()[choice - 1];
                break;
            }else {
                System.out.println("输入的数有误");
                continue;
            }

        }
        return skill;
    }

}
