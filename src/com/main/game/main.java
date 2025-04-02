package com.main.game;


import com.main.classes.*;
import com.main.pets.Attributes;
import com.main.pets.BlackTaurus;
import com.main.pets.FireFox;
import com.main.pets.Pet;
import com.main.skills.SkillLibrary;

import java.util.LinkedHashSet;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        Player player = new Player();
        player.pets.add(new FireFox(2, Attributes.Offensive,new LinkedHashSet<>()));
        Pet pet1 = player.pets.get(0);
        pet1.addSkills(SkillLibrary.skillFireBall);
        pet1.addSkills(SkillLibrary.skillFireBall);
        pet1.addSkills(SkillLibrary.skillEarthShattering);

        player.pets.add(new BlackTaurus(2, Attributes.Defensive,new LinkedHashSet<>()));
        Pet pet2 = player.pets.get(1);
        pet2.addSkills(SkillLibrary.skillFireBall);

        System.out.println(pet1.getAllSkills());

        pet1.getSkillsArray()[0].applyEffect(pet1,pet2);
        System.out.println(pet2);
        System.out.println(pet1);

        player.setName("DiamondMine");

        player.wallet.addCurrency(Wallet.CurrencyType.Coin,100);
        player.wallet.addCurrency(Wallet.CurrencyType.Diamond,20);

        System.out.println(player.getName());
        System.out.println(player.wallet);
    }
}
