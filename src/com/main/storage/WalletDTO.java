package com.main.storage;

import com.main.classes.Wallet;

import java.util.EnumMap;
import java.util.Map;

public class WalletDTO {
    private Map<Wallet.CurrencyType,Integer> currencies;

    public WalletDTO() {}

    public WalletDTO(Wallet wallet) {
        currencies = wallet.getCurrencies();
    }

    public Wallet toWallet() {
        Wallet wallet = new Wallet();
        wallet.setCurrencies(currencies);
        return wallet;
    }
}
