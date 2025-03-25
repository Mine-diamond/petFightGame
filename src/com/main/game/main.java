package com.main.game;


import com.main.classes.*;

import java.util.ArrayList;

public class main {
    public static void main(String[] args) {
        FireFox fireFox = new FireFox(2,Attributes.Offensive,new ArrayList<>());
        System.out.println(fireFox.toString());
    }
}
