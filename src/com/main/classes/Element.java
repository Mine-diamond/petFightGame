package com.main.classes;

public enum Element {
    gold,
    wood,
    earth,
    water,
    fire,
    noElement;

    private Element counteredElement;

    // 使用静态初始化块设置相克关系
    static {
        gold.counteredElement = wood;
        wood.counteredElement = earth;
        earth.counteredElement = water;
        water.counteredElement = fire;
        fire.counteredElement = gold;
        noElement.counteredElement = null;
    }

    public boolean counters(Element other) {return this.counteredElement == other;}
    public Element getCounteredElement() {return this.counteredElement;}
}
