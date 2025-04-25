package com.main.test;

import com.main.classes.Player;
import com.main.classes.Wallet;
import com.main.pets.Attributes;
import com.main.pets.BlackTaurus;
import com.main.pets.FireFox;
import com.main.pets.Pet;
import com.main.skills.SkillLibrary;

import java.util.LinkedHashSet;

import static com.main.storage.Storage.saveGame;

public class StorageTest {
    public static void main(String[] args) {
        Player p = createPlayer();
        saveGame("testSave",p);
    }

    public static Player createPlayer() {
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
        pet2.removeHP(100);

        System.out.println(pet1.getAllSkills());


        player.setName("DiamondMine");

        player.wallet.addCurrency(Wallet.CurrencyType.Coin,100);
        player.wallet.addCurrency(Wallet.CurrencyType.Diamond,20);

        System.out.println(player.getName());
        System.out.println(player.wallet);

        SkillLibrary.getAllSkills().forEach(System.out::println);
        return player;
    }
}
