package com.main.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.main.classes.ValueModifier;
import com.main.storage.ValueModifierDTO;

import java.io.FileReader;
import java.io.FileWriter;

public class GsonTest {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        ValueModifier vm = new ValueModifier(15);
        vm.addBaseMultiplicativeModifier("t1",2,"one",ValueModifier.PRIORITY_LOWEST);
        vm.setCalculationPrecision(3,true);

        ValueModifierDTO vmdto = new ValueModifierDTO(vm);

        //saveData(vmdto);

        ValueModifierDTO vmdto2 = loadData();
        System.out.println(vmdto2);
        ValueModifier vmr = vmdto2.toValueModifier();
        System.out.println(vmr);
    }

    public static void saveData(ValueModifierDTO vmdto){
        try {
            FileWriter writer = new FileWriter("VMSave.json");
            gson.toJson(vmdto, writer);
            writer.close();
            System.out.println("保存成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ValueModifierDTO loadData() {
        FileReader reader = null;
        try {
            reader = new FileReader("VMSave.json");
            return gson.fromJson(reader, ValueModifierDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
