package com.main.storage;

import com.main.classes.ValueModifier;
import com.main.classes.ValueModifier.ModifierType;
import com.main.classes.ValueModifier.TemporaryModifier;

import java.util.ArrayList;
import java.util.List;

public class ValueModifierDTO {
    public double baseValue;

    public List<TempModifierDTO> tempModifiers = new ArrayList<>();

    public int calculationPrecision = -1;
    public boolean roundingForCalculation = true;

    public int displayPrecision = -1;
    public boolean roundingForDisplay = false;

    public String formatPattern = null;

    public ValueModifierDTO() {}

    public ValueModifierDTO(ValueModifier vm) {
        this.baseValue = vm.getCurrentBaseValue();

        for (TemporaryModifier mod : vm.getAllModifiers()) {
            tempModifiers.add(new TempModifierDTO(mod));
        }

        // 精度和格式设置
        this.calculationPrecision = vm.getCalculationPrecision();
        this.roundingForCalculation = vm.isRoundingForCalculation();
        this.displayPrecision = vm.getDisplayPrecision();
        this.roundingForDisplay = vm.isRoundingForDisplay();
        this.formatPattern = vm.getFormatPattern();
    }

    public ValueModifier toValueModifier() {
        ValueModifier vm = new ValueModifier(baseValue);

        // 应用精度设置
        vm.setCalculationPrecision(calculationPrecision, roundingForCalculation);
        vm.setDisplayPrecision(displayPrecision, roundingForDisplay);
        vm.setFormatPattern(formatPattern);

        for (TempModifierDTO mod : tempModifiers) {
            mod.applyTo(vm);
        }

        return vm;
    }

    public static class TempModifierDTO {
        public String id;
        public String tag;
        public String type; // "ADDITIVE", "MULTIPLICATIVE", etc.
        public double value;
        public int priority;

        public TempModifierDTO() {}

        public TempModifierDTO(TemporaryModifier mod) {
            this.id = mod.getId();
            this.tag = mod.getTag();
            this.type = mod.getType().name();
            this.value = mod.getValue();
            this.priority = mod.getPriority();
        }

        public void applyTo(ValueModifier vm) {
            switch (type) {
                case "ADDITIVE" -> vm.addAdditiveModifier(id, value, tag, priority);
                case "MULTIPLICATIVE" -> vm.addMultiplicativeModifier(id, value, tag, priority);
                case "BASE_MULTIPLICATIVE" -> vm.addBaseMultiplicativeModifier(id, value, tag, priority);
                case "MIN_LIMIT" -> vm.addMinLimitModifier(id, value, tag, priority);
                case "MAX_LIMIT" -> vm.addMaxLimitModifier(id, value, tag, priority);
                default -> throw new IllegalArgumentException("未知的类型: " + type);
            }
        }
    }
}

