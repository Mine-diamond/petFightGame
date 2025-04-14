package com.main.classes;

/**
 * ObservableDouble 是一个可监听的 double 值容器，当值发生变化时可以通知所有监听者。
 * 适合用于 UI 绑定、状态观察等场景，例如当前 HP/能量值。
 */
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ObservableDouble extends Number implements Comparable<Number> {
    private double value;
    private final List<Consumer<Double>> listeners = new ArrayList<>();

    public ObservableDouble(double initialValue) {
        this.value = initialValue;
    }

    public double getValue() {
        return value;
    }

    public void set(double newValue) {
        if (Double.compare(this.value, newValue) != 0) {
            this.value = newValue;
            notifyListeners();
        }
    }

    public void add(double delta) {
        set(this.value + delta);
    }

    public void subtract(double delta) {
        set(this.value - delta);
    }

    public void onChanged(Consumer<Double> listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (Consumer<Double> listener : listeners) {
            listener.accept(value);
        }
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    // Number 接口实现
    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public float floatValue() {
        return (float) value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    // Comparable 接口实现
    @Override
    public int compareTo(Number other) {
        return Double.compare(this.value, other.doubleValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Number) {
            return Double.compare(this.value, ((Number) obj).doubleValue()) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }
}
