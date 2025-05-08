package com.main.storage;

import com.main.classes.Player;
import com.main.items.Item;
import com.main.pets.Pet;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public abstract class ItemDTO {
    String typeID;
    boolean canStack;

    public abstract void ItemDTO(Item item);

    public abstract Item toItem();
}
