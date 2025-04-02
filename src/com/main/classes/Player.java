package com.main.classes;

import com.main.pets.Pet;

import java.util.ArrayList;

public class Player {
    String name;

    public ArrayList<Pet> pets = new ArrayList<>();
    public Wallet wallet;

    public Player() {
        wallet = new Wallet();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
