package com.main.storage;


import com.main.classes.Player;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GameData {

    private PlayerDTO playerDTO;

    private long Timestamp;
    private int slot;

    public GameData(){}
    public GameData(PlayerDTO playerDTO, int slot) {
        this.playerDTO = playerDTO;
        this.slot = slot;
        Timestamp = System.currentTimeMillis();
    }

    public Player toPlayer(int slot){
        return playerDTO.toPlayer();
    }

    public void updateTimestamp(){
        Timestamp = System.currentTimeMillis();
    }

}
