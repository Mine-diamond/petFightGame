package com.main.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.main.classes.Player;

import java.io.*;

public class Storage {

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static String saveFolder = "save" + File.separator;

    static {
        File saveFolder = new File("save" + File.separator);
        if(!saveFolder.exists()){
            saveFolder.mkdir();
        }
    }

    public static GameData createNewGameData(String saveName, Player player){
        PlayerDTO playerDTO = new PlayerDTO(player);
        GameData gameData = new GameData(playerDTO, saveName);
        return gameData;
    }

    public static void saveGame(String saveName, Player player) {
        GameData game;
        if (ifGameDataExists(saveName)) {
            game = loadGameData(saveName);
        }else {
            game = createNewGameData(saveName, player);
        }
        game.updateTimestamp();
        game.addSavedTime();
        PlayerDTO playerDTO = new PlayerDTO(player);
        game.setPlayerDTO(playerDTO);
        try(FileWriter fw = new FileWriter(saveFolder + game.getSaveName() + ".json")) {
            gson.toJson(game, fw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveGame(GameData game) {
        game.updateTimestamp();
        game.addSavedTime();
        try(FileWriter fw = new FileWriter(saveFolder + game.getSaveName() + ".json")) {
            gson.toJson(game, fw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static GameData loadGameData(String saveName) {

        if(!ifGameDataExists(saveName)) {
            throw new RuntimeException("Could not find save: " + saveName);
        }

        try(FileReader fr = new FileReader(saveFolder + saveName + ".json")){
            return gson.fromJson(fr, GameData.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Player loadGame(String saveName){
        GameData game = loadGameData(saveName);
        return game.toPlayer();
    }

    public static String[] getAllSavedDataName() {
        File dir = new File(saveFolder);
        File[] files = null;
        try {
            files = dir.listFiles();
        } catch (Exception e) {
            return new String[0];
        }

        if (files == null) {
            return new String[0];
        }

        String[] games = new String[files.length];
        for(int i = 0; i < files.length; i++) {
            games[i] = files[i].getName().replace(".json", "");
        }
        return games;
    }

    public static boolean ifGameDataExists(String saveName) {
        return new File(saveFolder + saveName + ".json").exists();
    }

    public static void copyGameData(String currentSaveName, String mergedSaveName, Player player) {
        GameData gameData = loadGameData(currentSaveName);
        gameData.setPlayerDTO(new PlayerDTO(player));
        saveGame(gameData);
        GameData mergedGameData = gameData.toNewGameData(mergedSaveName);
        saveGame(mergedGameData);
    }

}
