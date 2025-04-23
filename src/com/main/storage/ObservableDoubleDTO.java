package com.main.storage;

import com.main.classes.ObservableDouble;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ObservableDoubleDTO {
    private double value;
    public ObservableDoubleDTO() {}

    public ObservableDoubleDTO(ObservableDouble od) {
        this.value = od.getValue();
    }

    public ObservableDouble toObservableDouble() {
        return new ObservableDouble(value);
    }
}
