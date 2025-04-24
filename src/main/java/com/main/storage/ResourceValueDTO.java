package com.main.storage;

import com.main.classes.ObservableDouble;
import com.main.classes.ResourceValue;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResourceValueDTO {
    private ValueModifierDTO baseMaxValue;
    private ObservableDoubleDTO value;

    public ResourceValueDTO() {}

    public ResourceValueDTO(ResourceValue rv) {
        baseMaxValue = new ValueModifierDTO(rv.getBaseMaxValue());
        value = new ObservableDoubleDTO(rv.getValue());
    }

    public ResourceValue toResourceValue(){
        return new ResourceValue(baseMaxValue.toValueModifier(), value.getValue());
    }
}
