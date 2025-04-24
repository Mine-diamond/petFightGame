package com.main.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.main.pets.Attributes;
import com.main.pets.BlackTaurus;
import com.main.pets.Pet;
import com.main.skills.Skill;
import com.main.skills.SkillLibrary;
import com.main.storage.PetDTO;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;

import static com.main.skills.SkillLibrary.getSkillByName;

public class PetSaveTest {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {

        BlackTaurus bt = new BlackTaurus(3, Attributes.Offensive,new LinkedHashSet<>());
        try {
            bt.addSkills((Skill) getSkillByName("skillEarthShattering", SkillLibrary.Type.Origen));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        bt.removeHP(50);

        PetDTO pet = new PetDTO(bt);
        //save(pet);
        PetDTO pd = load();
        Pet recover = pd.toPet();
        System.out.println(recover);
    }

    public static void save(PetDTO pet) {
        try(FileWriter fw = new FileWriter("PetSave.json");){
            gson.toJson(pet, fw);
        }catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("保存成功！");
    }

    public static PetDTO load() {
        FileReader reader = null;
        try {
            reader = new FileReader("PetSave.json");
            return gson.fromJson(reader, PetDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
