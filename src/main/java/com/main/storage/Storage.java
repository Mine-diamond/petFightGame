package com.main.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class Storage {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveGame(GameData game) {
        game.updateTimestamp();
        try(FileWriter fw = new FileWriter("save" + game.getSlot() + ".json")) {
            gson.toJson(game, fw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static GameData loadGame(int slot) {

        if(!new File("save" + slot + ".json").exists()) {
            throw new RuntimeException("Could not find slot: " + slot);
        }

        try(FileReader fr = new FileReader("save" + slot + ".json")){
            return gson.fromJson(fr, GameData.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
