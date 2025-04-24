package com.main.test;

import com.main.classes.ResourceValue;
import com.main.classes.ValueModifier;

import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.main.storage.ResourceValueDTO;

public class RVTest {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        ResourceValue HP = new ResourceValue(100);
        HP.addCurrentMaxValueBaseMultiplyModifier(1.5,"test","test.id", ValueModifier.PRIORITY_NORMAL);
        HP.refill();
        HP.removeValue(15);

        //ResourceValueDTO rvdto = new ResourceValueDTO(HP);
        //saveHP(rvdto);

        ResourceValueDTO tvdto2 = loadHP();
        System.out.println(tvdto2);
        ResourceValue HP2 = tvdto2.toResourceValue();
        System.out.println(HP2);
        System.out.println("baseValue: "+HP2.getBaseMaxValue() + " currentMaxValue: " + HP2.getCurrentMaxValue() + " value: " + HP2.getValue());
    }

    public static void saveHP(ResourceValueDTO HP) {
        try {
            FileWriter writer = new FileWriter("HPSave.json");
            gson.toJson(HP, writer);
            writer.close();
            System.out.println("保存成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ResourceValueDTO loadHP() {
        FileReader reader = null;
        try {
            reader = new FileReader("HPSave.json");
            return gson.fromJson(reader, ResourceValueDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
