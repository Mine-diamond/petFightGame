package com.main.game;


import com.main.classes.*;

import java.util.ArrayList;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        FireFox fireFox = new FireFox(2,Attributes.Offensive,new ArrayList<>());
        System.out.println(fireFox.toString());

        while (true){
            System.out.println("请输入增加的经验：");
            int n = 0;
            try {
                n = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                continue;
            }
            fireFox.addExperience(n);
            System.out.println(fireFox.toString());
        }
    }
}
