package com.main.classes;

import com.main.pets.Pet;

import java.util.Random;

/**
 * 战斗工具类，用于计算宠物间的伤害值与克制关系。
 */
public class CombatUtil {

    // 随机数生成器，用于生成伤害波动
    private static final Random RAND = new Random();

    // 防御影响系数（影响减伤程度）
    private static final double DEFENSE_MULTIPLIER = 1.5;

    // 随机伤害波动的下限和上限（±5%）
    private static final double RANDOM_MIN_FACTOR = 0.95;
    private static final double RANDOM_MAX_FACTOR = 1.05;

    // 克制关系伤害倍率设定
    private static final double COUNTER_MULTIPLIER = 1.5;    // 克制对方时：增伤
    private static final double COUNTERED_MULTIPLIER = 0.75; // 被克制时：减伤
    private static final double NEUTRAL_MULTIPLIER = 1.0;    // 无克制：正常伤害
    private static final double MUTUAL_MULTIPLIER = 1.0;     // 互相克制：不修改伤害

    /**
     * 计算伤害值（考虑攻击、防御、属性克制、伤害波动）
     * @param caster 攻击方宠物
     * @param target 防御方宠物
     * @return 最终伤害值（最低为1）
     */
    public static double calcDamageValue(Pet caster, Pet target) {

        double damage = 0;

        // 根据属性克制关系，获取对应伤害倍率
        double counterMultiplier = 1;
        CounterRelation counterRelation = getCounterRelation(caster, target);
        switch (counterRelation) {
            case COUNTER -> counterMultiplier = COUNTER_MULTIPLIER;
            case COUNTERED -> counterMultiplier = COUNTERED_MULTIPLIER;
            case NEUTRAL -> counterMultiplier = NEUTRAL_MULTIPLIER;
            case MUTUAL -> counterMultiplier = MUTUAL_MULTIPLIER;
        }

        // 若目标防御为0，则直接按攻击力计算伤害（乘以克制系数）
        if (target.getCurrentDefense() == 0) {
            damage = caster.getCurrentAttack() * counterMultiplier;
        } else {
            // 否则按照攻击力与防御力的比例公式计算（考虑防御乘数）
            damage = caster.getCurrentAttack() *
                    (caster.getCurrentAttack() / (caster.getCurrentAttack() + target.getCurrentDefense() * DEFENSE_MULTIPLIER))
                    * counterMultiplier;
        }

        // 引入随机波动因子（±5%）
        double randomFluctuation = RAND.nextDouble(RANDOM_MIN_FACTOR, RANDOM_MAX_FACTOR);
        damage *= randomFluctuation;

        // 保证最小伤害为1
        if (damage < 1) {
            damage = 1;
        }

        return damage;
    }

    /**
     * 表示属性克制关系的枚举
     */
    public enum CounterRelation {
        COUNTER,    // 克制：造成更多伤害
        COUNTERED,  // 被克制：伤害减少
        NEUTRAL,    // 中性：不受影响
        MUTUAL      // 互克：无修改
    }

    /**
     * 判断攻击方与防御方的属性克制关系
     * @param caster 攻击方
     * @param target 防御方
     * @return 双方之间的属性克制关系
     */
    public static CounterRelation getCounterRelation(Pet caster, Pet target) {
        boolean countered = false; // 是否被对方克制
        boolean counter = false;   // 是否克制对方

        Element[] targetElements = target.getElementArray();
        Element[] casterElements = caster.getElementArray();

        // 判断是否被目标的某个属性克制
        for (Element targetElement : targetElements) {
            countered = caster.isCounteredBy(targetElement);
            if (countered) {
                break;
            }
        }

        // 判断是否克制目标的某个属性
        for (Element casterElement : casterElements) {
            counter = target.isCounteredBy(casterElement);
            if (counter) {
                break;
            }
        }

        // 返回对应的克制关系枚举
        if (counter && countered) return CounterRelation.MUTUAL;
        if (counter) return CounterRelation.COUNTER;
        if (countered) return CounterRelation.COUNTERED;
        return CounterRelation.NEUTRAL;
    }
}
