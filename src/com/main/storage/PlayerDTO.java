package com.main.storage;

import com.main.classes.Player;
import com.main.pets.Pet;

import java.util.ArrayList;

public class PlayerDTO {
    private String name;
    private ArrayList<PetDTO> pets = new ArrayList<>();
    private WalletDTO wallet;

    public PlayerDTO() {}

    public PlayerDTO(Player player) {
        this.name = player.getName();
        for (Pet pet : player.pets) {
            pets.add(new PetDTO(pet));
        }
        wallet = new WalletDTO(player.wallet);
    }

    public Player toPlayer() {
        Player player = new Player();
        player.setName(name);
        for (PetDTO pet : pets) {
            player.pets.add(pet.toPet());
        }
        player.wallet = wallet.toWallet();
        return player;
    }
}
