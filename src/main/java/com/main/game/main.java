package com.main.game;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.main.classes.*;
import com.main.pets.Attributes;
import com.main.pets.BlackTaurus;
import com.main.pets.FireFox;
import com.main.pets.Pet;
import com.main.skills.SkillLibrary;
import com.main.storage.PlayerDTO;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Scanner;

public class main {//仅测试用

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        Player player = new Player();
        player.pets.add(new FireFox(2, Attributes.Offensive,new LinkedHashSet<>()));
        Pet pet1 = player.pets.get(0);
        pet1.addSkills(SkillLibrary.skillNull);
        pet1.addSkills(SkillLibrary.skillFireBall);
        pet1.addSkills(SkillLibrary.skillFireBall);
        pet1.addSkills(SkillLibrary.skillEarthShattering);
        pet1.addSkills(SkillLibrary.skillRecover);
        pet1.addSkills(SkillLibrary.skillRecover);

        player.pets.add(new BlackTaurus(2, Attributes.Defensive,new LinkedHashSet<>()));
        Pet pet2 = player.pets.get(1);
        pet2.addSkills(SkillLibrary.skillNull);
        pet2.addSkills(SkillLibrary.skillEarthShattering);
        pet2.addSkills(SkillLibrary.skillFireBall);
        pet2.addSkills(SkillLibrary.skillRecover);

        System.out.println(pet1.getAllSkills());


        player.setName("DiamondMine");

        player.wallet.addCurrency(Wallet.CurrencyType.Coin,100);
        player.wallet.addCurrency(Wallet.CurrencyType.Diamond,20);

        System.out.println(player.getName());
        System.out.println(player.wallet);

        SkillLibrary.getAllSkills().forEach(System.out::println);

        BattleSystem battleSystem = new BattleSystem(pet1, pet2,10);

        //battleSystem.battleFlow();

        PlayerDTO playerDTO = new PlayerDTO(player);

        try (FileWriter fw = new FileWriter("playerSave.json")){
            gson.toJson(playerDTO,fw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
