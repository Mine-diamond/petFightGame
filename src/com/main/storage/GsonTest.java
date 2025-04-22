package com.main.storage;

import com.main.classes.ValueModifier;

import java.io.FileWriter;
import java.io.IOException;

public class GsonTest {


    public static void main(String[] args) {
        ValueModifier vm = new ValueModifier(15);
        vm.addBaseMultiplicativeModifier("t1",2,"one",ValueModifier.PRIORITY_LOWEST);
        vm.setCalculationPrecision(3,true);
    }

    public void saveData(ValueModifier vm) throws IOException {
        FileWriter writer = new FileWriter("save");

    }
}
