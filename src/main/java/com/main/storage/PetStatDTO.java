package com.main.storage;

import com.main.classes.PetStat;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class PetStatDTO {
    private ResourceValueDTO HP;
    private ResourceValueDTO energy;
    private ValueModifierDTO baseAttack;
    private ValueModifierDTO baseDefense;

    public PetStatDTO() {}

    public PetStatDTO(PetStat petStat) {
        this.HP = new ResourceValueDTO(petStat.getHP());
        this.energy = new ResourceValueDTO(petStat.getEnergy());
        this.baseAttack = new ValueModifierDTO(petStat.getBaseAttack());
        this.baseDefense = new ValueModifierDTO(petStat.getBaseDefense());
    }

    public PetStat toPetStat() {
        return new PetStat(HP.toResourceValue(),energy.toResourceValue(),baseAttack.toValueModifier(),baseDefense.toValueModifier());
    }
}
