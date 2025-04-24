package com.main.classes;

import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;

@Setter
@Getter
public class Wallet {

    public enum CurrencyType {
        Coin("硬币"),Diamond("钻石");


        private final String NAME;
        CurrencyType(String NAME) {
            this.NAME = NAME;
        }

        public String getName() { // 添加getter方法
            return NAME;
        }
    }

    Map<CurrencyType,Integer> currencies = new EnumMap<>(CurrencyType.class);

    public Wallet() {
        for (CurrencyType type : CurrencyType.values()) {
            currencies.put(type,0);
        }
    }

    public void addCurrency(CurrencyType type,int amount) {//增加货币

        if(amount < 0) throw new IllegalArgumentException("amount must be positive");

        currencies.put(type,currencies.get(type)+amount);
    }

    public boolean removeCurrency(CurrencyType type, int amount) {//减少货币，货币不足返回false

        if(amount < 0) throw new IllegalArgumentException("amount must be positive");

        if(currencies.get(type) - amount < 0) {
            return false;
        }else {
            currencies.put(type,currencies.get(type)-amount);
            return true;
        }
    }

    public int getAmount(CurrencyType type) {//获取货币数量

        return currencies.get(type);
    }

    public void setCurrencies(CurrencyType type,int amount) {//直接设置货币数量


        if(amount < 0) throw new IllegalArgumentException("amount must be positive");

        currencies.put(type,amount);
    }

    public boolean hasSufficientAmount(CurrencyType type, int amount) {//是否有足够多的货币
        return currencies.get(type) >= amount;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("货币:").append(System.lineSeparator());
        for (CurrencyType type : currencies.keySet()) {
            sb.append(type.NAME).append(":").append(currencies.get(type)).append(System.lineSeparator());
        }
        return sb.toString();
    }

}
