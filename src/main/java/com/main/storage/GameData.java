package com.main.storage;


import com.main.classes.Player;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GameData {

    private PlayerDTO playerDTO;

    private long timestamp;
    private long startTimestamp;
    private int savedTime;
    private String saveName;

    public GameData(){}


    public GameData(PlayerDTO playerDTO, String saveName) {
        this.playerDTO = playerDTO;
        this.saveName = saveName;
        timestamp = System.currentTimeMillis();
        startTimestamp = timestamp;
        savedTime = 0;
    }


    public Player toPlayer(){
        return playerDTO.toPlayer();
    }

    public void updateTimestamp(){
        timestamp = System.currentTimeMillis();
    }

    public void addSavedTime(){
        savedTime++;
    }

    public GameData toNewGameData(String saveName){
        return new GameData(playerDTO, saveName);
    }

}
