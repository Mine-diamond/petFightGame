package com.main.classes;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.lang.ref.WeakReference;

/**
 * ValueModifier 类用于管理游戏中的数值属性，支持永久修改和临时修改
 */
public class ValueModifier {
    // 优先级常量
    public static final int PRIORITY_HIGHEST = 100;
    public static final int PRIORITY_HIGH = 200;
    public static final int PRIORITY_NORMAL = 500; // 默认值
    public static final int PRIORITY_LOW = 800;
    public static final int PRIORITY_LOWEST = 1000;

    // 核心数据
    private final double initialBaseValue;
    private double currentBaseValue;
    private final List<PermanentModification> modificationHistory = new ArrayList<>();

    // 按类型分组存储修改器
    private final Map<ModifierType, Map<String, TemporaryModifier>> modifiersByType = new EnumMap<>(ModifierType.class);

    // 用于自动更新的ModifiedValue集合
    private final Set<WeakReference<ModifiedValue>> modifiedValues = new HashSet<>();

    // 精度控制属性
    private int calculationPrecision = -1;  // -1表示不限制精度
    private int displayPrecision = -1;      // -1表示不限制精度
    private boolean roundingForCalculation = true;  // true=四舍五入，false=直接截断
    private boolean roundingForDisplay = false;     // true=四舍五入，false=直接截断
    private String formatPattern = null;    // 自定义格式化模式

    /**
     * 创建一个新的ValueModifier实例
     * @param initialValue 初始基础值
     */
    public ValueModifier(double initialValue) {
        this.initialBaseValue = initialValue;
        this.currentBaseValue = initialValue;

        // 初始化所有修改器类型的映射
        for (ModifierType type : ModifierType.values()) {
            modifiersByType.put(type, new HashMap<>());
        }
    }

    /**
     * 创建一个新的ValueModifier实例
     * @param initialValue 初始基础值
     */
    public ValueModifier(int initialValue) {
        this((double) initialValue);
    }

    // ==================== 精度控制方法 ====================

    /**
     * 设置计算精度（小数位数）
     * @param precision 小数位数，-1表示不限制精度
     * @param useRounding 是否使用四舍五入（true）或直接截断（false）
     */
    public void setCalculationPrecision(int precision, boolean useRounding) {
        this.calculationPrecision = precision;
        this.roundingForCalculation = useRounding;
        notifyModifiedValues();
    }

    /**
     * 设置显示精度（小数位数）
     * @param precision 小数位数，-1表示不限制精度
     * @param useRounding 是否使用四舍五入（true）或直接截断（false）
     */
    public void setDisplayPrecision(int precision, boolean useRounding) {
        this.displayPrecision = precision;
        this.roundingForDisplay = useRounding;
    }

    /**
     * 设置自定义格式化模式
     * @param pattern 格式化模式，如"#.##"或"0.00%"
     */
    public void setFormatPattern(String pattern) {
        if (pattern == null) {
            this.formatPattern = null;
            return;
        }

        try {
            // 验证格式是否有效
            new DecimalFormat(pattern).format(1.0);
            this.formatPattern = pattern;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的格式模式: " + pattern, e);
        }
    }

    /**
     * 应用计算精度
     * @param value 原始值
     * @return 应用精度后的值
     */
    private double applyCalculationPrecision(double value) {
        if (calculationPrecision < 0) {
            return value;  // 不限制精度
        }

        if (roundingForCalculation) {
            // 四舍五入
            double factor = Math.pow(10, calculationPrecision);
            return Math.round(value * factor) / factor;
        } else {
            // 直接截断（对正负数都适用）
            double factor = Math.pow(10, calculationPrecision);
            return Math.signum(value) * Math.floor(Math.abs(value) * factor) / factor;
        }
    }

    /**
     * 应用显示精度并格式化
     * @param value 原始值
     * @return 格式化后的字符串
     */
    private String applyDisplayFormat(double value) {
        // 先应用显示精度
        double displayValue = value;
        if (displayPrecision >= 0) {
            if (roundingForDisplay) {
                // 四舍五入
                double factor = Math.pow(10, displayPrecision);
                displayValue = Math.round(value * factor) / factor;
            } else {
                // 直接截断（对正负数都适用）
                double factor = Math.pow(10, displayPrecision);
                displayValue = Math.signum(value) * Math.floor(Math.abs(value) * factor) / factor;
            }
        }

        // 然后应用格式化
        if (formatPattern != null) {
            try {
                DecimalFormat df = new DecimalFormat(formatPattern);
                return df.format(displayValue);
            } catch (IllegalArgumentException e) {
                // 格式无效，回退到默认格式
                return Double.toString(displayValue);
            }
        } else if (displayPrecision >= 0) {
            return String.format("%." + displayPrecision + "f", displayValue);
        } else {
            return Double.toString(displayValue);
        }
    }

    /**
     * 获取格式化的值
     * @return 格式化后的字符串
     */
    public String getFormattedValue() {
        return applyDisplayFormat(getFinalValue());
    }

    // ==================== 基础值管理 ====================

    /**
     * 获取初始基础值
     * @return 初始基础值
     */
    public double getInitialBaseValue() {
        return initialBaseValue;
    }

    /**
     * 获取当前基础值
     * @return 当前基础值
     */
    public double getCurrentBaseValue() {
        return currentBaseValue;
    }

    /**
     * 修改基础值
     * @param delta 变化量
     * @param reason 修改原因
     * @return 修改后的基础值
     */
    public double modifyBaseValue(double delta, String reason) {
        double oldValue = currentBaseValue;
        currentBaseValue += delta;
        modificationHistory.add(new PermanentModification(oldValue, currentBaseValue, reason));
        notifyModifiedValues();
        return currentBaseValue;
    }

    /**
     * 设置基础值
     * @param newValue 新的基础值
     * @param reason 修改原因
     * @return 修改后的基础值
     */
    public double setBaseValue(double newValue, String reason) {
        double oldValue = currentBaseValue;
        currentBaseValue = newValue;
        modificationHistory.add(new PermanentModification(oldValue, currentBaseValue, reason));
        notifyModifiedValues();
        return currentBaseValue;
    }

    // ==================== 永久修改历史管理 ====================

    /**
     * 获取修改历史
     * @return 修改历史列表
     */
    public List<PermanentModification> getModificationHistory() {
        return Collections.unmodifiableList(modificationHistory);
    }

    /**
     * 撤销最后一次修改
     * @return 撤销后的基础值，如果没有历史记录则返回当前值
     */
    public double undoLastModification() {
        if (modificationHistory.isEmpty()) {
            return currentBaseValue;
        }

        PermanentModification lastMod = modificationHistory.remove(modificationHistory.size() - 1);
        currentBaseValue = lastMod.getOldValue();
        notifyModifiedValues();
        return currentBaseValue;
    }

    /**
     * 撤销到特定历史点
     * @param index 历史索引
     * @return 撤销后的基础值
     * @throws IndexOutOfBoundsException 如果索引无效
     */
    public double undoToHistoryPoint(int index) {
        if (index < 0 || index >= modificationHistory.size()) {
            throw new IndexOutOfBoundsException("Invalid history index: " + index);
        }

        PermanentModification targetMod = modificationHistory.get(index);
        currentBaseValue = targetMod.getOldValue();

        // 移除该索引及之后的所有历史
        modificationHistory.subList(index, modificationHistory.size()).clear();

        notifyModifiedValues();
        return currentBaseValue;
    }

    /**
     * 清除所有历史
     */
    public void clearHistory() {
        modificationHistory.clear();
    }

    // ==================== 临时修改器管理 ====================

    /**
     * 添加临时加法修改器
     * @param id 修改器ID
     * @param value 修改值
     * @param tag 标签
     * @return 添加的修改器
     */
    public TemporaryModifier addAdditiveModifier(String id, double value, String tag) {
        return addAdditiveModifier(id, value, tag, PRIORITY_NORMAL);
    }

    /**
     * 添加临时加法修改器（带优先级）
     * @param id 修改器ID
     * @param value 修改值
     * @param tag 标签
     * @param priority 优先级（数字越小优先级越高）
     * @return 添加的修改器
     */
    public TemporaryModifier addAdditiveModifier(String id, double value, String tag, int priority) {
        return addModifier(id, ModifierType.ADDITIVE, value, tag, priority);
    }

    /**
     * 添加临时乘法修改器
     * @param id 修改器ID
     * @param value 修改值（百分比，如0.2表示增加20%）
     * @param tag 标签
     * @return 添加的修改器
     */
    public TemporaryModifier addMultiplicativeModifier(String id, double value, String tag) {
        return addMultiplicativeModifier(id, value, tag, PRIORITY_NORMAL);
    }

    /**
     * 添加临时乘法修改器（带优先级）
     * @param id 修改器ID
     * @param value 修改值（百分比，如0.2表示增加20%）
     * @param tag 标签
     * @param priority 优先级（数字越小优先级越高）
     * @return 添加的修改器
     */
    public TemporaryModifier addMultiplicativeModifier(String id, double value, String tag, int priority) {
        return addModifier(id, ModifierType.MULTIPLICATIVE, value, tag, priority);
    }

    /**
     * 添加基础值乘法修改器
     * @param id 修改器ID
     * @param value 修改值（百分比，如0.2表示增加20%基础值）
     * @param tag 标签
     * @return 添加的修改器
     */
    public TemporaryModifier addBaseMultiplicativeModifier(String id, double value, String tag) {
        return addBaseMultiplicativeModifier(id, value, tag, PRIORITY_NORMAL);
    }

    /**
     * 添加基础值乘法修改器（带优先级）
     * @param id 修改器ID
     * @param value 修改值（百分比，如0.2表示增加20%基础值）
     * @param tag 标签
     * @param priority 优先级（数字越小优先级越高）
     * @return 添加的修改器
     */
    public TemporaryModifier addBaseMultiplicativeModifier(String id, double value, String tag, int priority) {
        return addModifier(id, ModifierType.BASE_MULTIPLICATIVE, value, tag, priority);
    }

    /**
     * 添加最小值限制修改器
     * @param id 修改器ID
     * @param minValue 最小值
     * @param tag 标签
     * @return 添加的修改器
     */
    public TemporaryModifier addMinLimitModifier(String id, double minValue, String tag) {
        return addMinLimitModifier(id, minValue, tag, PRIORITY_NORMAL);
    }

    /**
     * 添加最小值限制修改器（带优先级）
     * @param id 修改器ID
     * @param minValue 最小值
     * @param tag 标签
     * @param priority 优先级（数字越小优先级越高）
     * @return 添加的修改器
     */
    public TemporaryModifier addMinLimitModifier(String id, double minValue, String tag, int priority) {
        return addModifier(id, ModifierType.MIN_LIMIT, minValue, tag, priority);
    }

    /**
     * 添加最大值限制修改器
     * @param id 修改器ID
     * @param maxValue 最大值
     * @param tag 标签
     * @return 添加的修改器
     */
    public TemporaryModifier addMaxLimitModifier(String id, double maxValue, String tag) {
        return addMaxLimitModifier(id, maxValue, tag, PRIORITY_NORMAL);
    }

    /**
     * 添加最大值限制修改器（带优先级）
     * @param id 修改器ID
     * @param maxValue 最大值
     * @param tag 标签
     * @param priority 优先级（数字越小优先级越高）
     * @return 添加的修改器
     */
    public TemporaryModifier addMaxLimitModifier(String id, double maxValue, String tag, int priority) {
        return addModifier(id, ModifierType.MAX_LIMIT, maxValue, tag, priority);
    }

    /**
     * 添加任意类型的修改器
     * @param id 修改器ID
     * @param type 修改器类型
     * @param value 修改值
     * @param tag 标签
     * @param priority 优先级
     * @return 添加的修改器
     */
    private TemporaryModifier addModifier(String id, ModifierType type, double value, String tag, int priority) {
        TemporaryModifier modifier = new TemporaryModifier(id, type, value, tag, priority);
        modifiersByType.get(type).put(id, modifier);
        notifyModifiedValues();
        return modifier;
    }

    /**
     * 设置修改器的优先级
     * @param id 修改器ID
     * @param newPriority 新的优先级
     * @return 是否成功设置
     */
    public boolean setModifierPriority(String id, int newPriority) {
        for (Map<String, TemporaryModifier> modifiers : modifiersByType.values()) {
            if (modifiers.containsKey(id)) {
                TemporaryModifier modifier = modifiers.get(id);
                modifier.setPriority(newPriority);
                notifyModifiedValues();
                return true;
            }
        }
        return false;
    }

    /**
     * 按ID移除修改器
     * @param id 修改器ID
     * @return 是否成功移除
     */
    public boolean removeModifier(String id) {
        boolean removed = false;
        for (Map<String, TemporaryModifier> modifiers : modifiersByType.values()) {
            if (modifiers.remove(id) != null) {
                removed = true;
                break;
            }
        }

        if (removed) {
            notifyModifiedValues();
        }
        return removed;
    }

    /**
     * 按标签移除修改器
     * @param tag 标签
     * @return 移除的修改器数量
     */
    public int removeModifiersByTag(String tag) {
        int count = 0;
        for (Map<String, TemporaryModifier> modifiers : modifiersByType.values()) {
            List<String> toRemove = modifiers.values().stream()
                    .filter(mod -> mod.getTag().equals(tag))
                    .map(TemporaryModifier::getId)
                    .collect(Collectors.toList());

            for (String id : toRemove) {
                modifiers.remove(id);
                count++;
            }
        }

        if (count > 0) {
            notifyModifiedValues();
        }

        return count;
    }

    /**
     * 按标签前缀移除修改器
     * @param tagPrefix 标签前缀
     * @return 移除的修改器数量
     */
    public int removeModifiersByTagPrefix(String tagPrefix) {
        int count = 0;
        for (Map<String, TemporaryModifier> modifiers : modifiersByType.values()) {
            List<String> toRemove = modifiers.values().stream()
                    .filter(mod -> mod.getTag().startsWith(tagPrefix))
                    .map(TemporaryModifier::getId)
                    .collect(Collectors.toList());

            for (String id : toRemove) {
                modifiers.remove(id);
                count++;
            }
        }

        if (count > 0) {
            notifyModifiedValues();
        }

        return count;
    }

    /**
     * 清除所有临时修改器
     */
    public void clearAllModifiers() {
        boolean hadModifiers = modifiersByType.values().stream()
                .anyMatch(map -> !map.isEmpty());

        if (hadModifiers) {
            for (Map<String, TemporaryModifier> modifiers : modifiersByType.values()) {
                modifiers.clear();
            }
            notifyModifiedValues();
        }
    }

    /**
     * 获取所有临时修改器
     * @return 所有修改器的集合
     */
    public Collection<TemporaryModifier> getAllModifiers() {
        List<TemporaryModifier> allModifiers = new ArrayList<>();
        for (Map<String, TemporaryModifier> modifiers : modifiersByType.values()) {
            allModifiers.addAll(modifiers.values());
        }
        return allModifiers;
    }

    /**
     * 获取特定类型的所有修改器，按优先级排序
     * @param type 修改器类型
     * @return 排序后的修改器列表
     */
    private List<TemporaryModifier> getSortedModifiers(ModifierType type) {
        return modifiersByType.get(type).values().stream()
                .sorted(Comparator.comparingInt(TemporaryModifier::getPriority)
                        .thenComparingLong(TemporaryModifier::getCreationTime))
                .collect(Collectors.toList());
    }

    // ==================== 值计算功能 ====================

    /**
     * 获取最终值
     * @return 应用所有修改器后的最终值
     */
    public double getFinalValue() {
        double rawValue = calculateValue(mod -> true);
        return applyCalculationPrecision(rawValue);
    }

    /**
     * 获取不含特定标签的值
     * @param excludeTag 要排除的标签
     * @return 排除特定标签后的值
     */
    public double getValueExcludingTag(String excludeTag) {
        double rawValue = calculateValue(mod -> !mod.getTag().equals(excludeTag));
        return applyCalculationPrecision(rawValue);
    }

    /**
     * 获取只含特定标签的值
     * @param includeTag 要包含的标签
     * @return 只包含特定标签的值
     */
    public double getValueForTag(String includeTag) {
        double rawValue = calculateValueWithBaseAndTag(includeTag);
        return applyCalculationPrecision(rawValue);
    }

    /**
     * 计算只包含特定标签的值
     * @param includeTag 要包含的标签
     * @return 计算结果
     */
    private double calculateValueWithBaseAndTag(String includeTag) {
        // 基础值乘法修改器（只包含特定标签）
        double baseMultSum = modifiersByType.get(ModifierType.BASE_MULTIPLICATIVE).values().stream()
                .filter(mod -> mod.getTag().equals(includeTag))
                .sorted(Comparator.comparingInt(TemporaryModifier::getPriority))
                .mapToDouble(TemporaryModifier::getValue)
                .sum();

        // 确保基础值乘法总和不低于-1（避免负值或零值）
        baseMultSum = Math.max(baseMultSum, -0.99);

        // 应用基础值乘法
        double modifiedBase = currentBaseValue * (1 + baseMultSum);

        // 加法修改器（只包含特定标签）
        double additiveSum = modifiersByType.get(ModifierType.ADDITIVE).values().stream()
                .filter(mod -> mod.getTag().equals(includeTag))
                .sorted(Comparator.comparingInt(TemporaryModifier::getPriority))
                .mapToDouble(TemporaryModifier::getValue)
                .sum();

        // 乘法修改器（只包含特定标签）
        double multSum = modifiersByType.get(ModifierType.MULTIPLICATIVE).values().stream()
                .filter(mod -> mod.getTag().equals(includeTag))
                .sorted(Comparator.comparingInt(TemporaryModifier::getPriority))
                .mapToDouble(TemporaryModifier::getValue)
                .sum();

        // 确保乘法总和不低于-1（避免负值或零值）
        multSum = Math.max(multSum, -0.99);

        // 计算结果
        double result = (modifiedBase + additiveSum) * (1 + multSum);

        // 应用限制器（只包含特定标签）
        result = applyLimitsWithTag(result, includeTag);

        return result;
    }

    /**
     * 应用特定标签的限制器
     * @param value 要限制的值
     * @param includeTag 要包含的标签
     * @return 限制后的值
     */
    private double applyLimitsWithTag(double value, String includeTag) {
        // 最小值限制（只包含特定标签）
        Optional<Double> minLimit = modifiersByType.get(ModifierType.MIN_LIMIT).values().stream()
                .filter(mod -> mod.getTag().equals(includeTag))
                .sorted(Comparator.comparingInt(TemporaryModifier::getPriority))
                .map(TemporaryModifier::getValue)
                .max(Double::compare);

        if (minLimit.isPresent() && value < minLimit.get()) {
            value = minLimit.get();
        }

        // 最大值限制（只包含特定标签）
        Optional<Double> maxLimit = modifiersByType.get(ModifierType.MAX_LIMIT).values().stream()
                .filter(mod -> mod.getTag().equals(includeTag))
                .sorted(Comparator.comparingInt(TemporaryModifier::getPriority))
                .map(TemporaryModifier::getValue)
                .min(Double::compare);

        if (maxLimit.isPresent() && value > maxLimit.get()) {
            value = maxLimit.get();
        }

        return value;
    }

    /**
     * 根据自定义条件计算值
     * @param filter 修改器过滤条件
     * @return 计算后的值
     */
    public double calculateValue(Predicate<TemporaryModifier> filter) {
        // 1. 应用基础值乘法修改器
        double baseMultSum = modifiersByType.get(ModifierType.BASE_MULTIPLICATIVE).values().stream()
                .filter(filter)
                .sorted(Comparator.comparingInt(TemporaryModifier::getPriority))
                .mapToDouble(TemporaryModifier::getValue)
                .sum();

        // 确保基础值乘法总和不低于-1（避免负值或零值）
        baseMultSum = Math.max(baseMultSum, -0.99);

        // 应用基础值乘法
        double modifiedBase = currentBaseValue * (1 + baseMultSum);

        // 2. 应用加法修改器
        double additiveSum = modifiersByType.get(ModifierType.ADDITIVE).values().stream()
                .filter(filter)
                .sorted(Comparator.comparingInt(TemporaryModifier::getPriority))
                .mapToDouble(TemporaryModifier::getValue)
                .sum();

        // 3. 应用普通乘法修改器
        double multSum = modifiersByType.get(ModifierType.MULTIPLICATIVE).values().stream()
                .filter(filter)
                .sorted(Comparator.comparingInt(TemporaryModifier::getPriority))
                .mapToDouble(TemporaryModifier::getValue)
                .sum();

        // 确保乘法总和不低于-1（避免负值或零值）
        multSum = Math.max(multSum, -0.99);

        // 计算中间结果
        double result = (modifiedBase + additiveSum) * (1 + multSum);

        // 4. 应用最小值限制
        List<TemporaryModifier> minLimits = modifiersByType.get(ModifierType.MIN_LIMIT).values().stream()
                .filter(filter)
                .sorted(Comparator.comparingInt(TemporaryModifier::getPriority))
                .collect(Collectors.toList());

        if (!minLimits.isEmpty()) {
            // 取所有最小值限制中的最大值（最严格的下限）
            double minLimit = minLimits.stream()
                    .mapToDouble(TemporaryModifier::getValue)
                    .max()
                    .orElse(Double.NEGATIVE_INFINITY);

            if (result < minLimit) {
                result = minLimit;
            }
        }

        // 5. 应用最大值限制
        List<TemporaryModifier> maxLimits = modifiersByType.get(ModifierType.MAX_LIMIT).values().stream()
                .filter(filter)
                .sorted(Comparator.comparingInt(TemporaryModifier::getPriority))
                .collect(Collectors.toList());

        if (!maxLimits.isEmpty()) {
            // 取所有最大值限制中的最小值（最严格的上限）
            double maxLimit = maxLimits.stream()
                    .mapToDouble(TemporaryModifier::getValue)
                    .min()
                    .orElse(Double.POSITIVE_INFINITY);

            if (result > maxLimit) {
                result = maxLimit;
            }
        }

        // 6. 处理限制器冲突（如果最小值大于最大值）
        if (!minLimits.isEmpty() && !maxLimits.isEmpty()) {
            double minLimit = minLimits.stream()
                    .mapToDouble(TemporaryModifier::getValue)
                    .max()
                    .orElse(Double.NEGATIVE_INFINITY);

            double maxLimit = maxLimits.stream()
                    .mapToDouble(TemporaryModifier::getValue)
                    .min()
                    .orElse(Double.POSITIVE_INFINITY);

            if (minLimit > maxLimit) {
                // 冲突解决策略：使用优先级更高的限制器
                TemporaryModifier highestPriorityMin = minLimits.get(0);
                TemporaryModifier highestPriorityMax = maxLimits.get(0);

                if (highestPriorityMin.getPriority() <= highestPriorityMax.getPriority()) {
                    // 最小值限制优先级更高
                    result = minLimit;
                } else {
                    // 最大值限制优先级更高
                    result = maxLimit;
                }
            }
        }

        return result;
    }

    /**
     * 创建一个ModifiedValue对象，自动跟踪值的变化
     * @return ModifiedValue对象
     */
    public ModifiedValue createModifiedValue() {
        ModifiedValue value = new ModifiedValue(mod -> true);
        modifiedValues.add(new WeakReference<>(value));
        return value;
    }

    /**
     * 创建一个包含特定标签的ModifiedValue对象
     * @param includeTag 要包含的标签
     * @return ModifiedValue对象
     */
    public ModifiedValue createModifiedValueForTag(String includeTag) {
        ModifiedValue value = new ModifiedValue(mod -> mod.getTag().equals(includeTag));
        modifiedValues.add(new WeakReference<>(value));
        return value;
    }

    /**
     * 创建一个排除特定标签的ModifiedValue对象
     * @param excludeTag 要排除的标签
     * @return ModifiedValue对象
     */
    public ModifiedValue createModifiedValueExcludingTag(String excludeTag) {
        ModifiedValue value = new ModifiedValue(mod -> !mod.getTag().equals(excludeTag));
        modifiedValues.add(new WeakReference<>(value));
        return value;
    }

    /**
     * 通知所有ModifiedValue对象更新
     */
    private void notifyModifiedValues() {
        // 移除已被垃圾回收的引用
        modifiedValues.removeIf(ref -> ref.get() == null);

        // 通知所有ModifiedValue更新
        for (WeakReference<ModifiedValue> ref : modifiedValues) {
            ModifiedValue value = ref.get();
            if (value != null) {
                value.update();
            }
        }
    }

    // ==================== 内部类 ====================

    /**
     * 永久修改记录类
     */
    public static class PermanentModification {
        private final double oldValue;
        private final double newValue;
        private final String reason;
        private final LocalDateTime timestamp;

        public PermanentModification(double oldValue, double newValue, String reason) {
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.reason = reason;
            this.timestamp = LocalDateTime.now();
        }

        public double getOldValue() {
            return oldValue;
        }

        public double getNewValue() {
            return newValue;
        }

        public String getReason() {
            return reason;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public double getDelta() {
            return newValue - oldValue;
        }

        @Override
        public String toString() {
            return String.format("修改: %s, 从 %.2f 到 %.2f (变化: %+.2f), 时间: %s",
                    reason, oldValue, newValue, getDelta(), timestamp);
        }
    }

    /**
     * 临时修改器类型枚举
     */
    public enum ModifierType {
        ADDITIVE("加法"),
        MULTIPLICATIVE("乘法"),
        BASE_MULTIPLICATIVE("基础值乘法"),
        MIN_LIMIT("最小值限制"),
        MAX_LIMIT("最大值限制");

        private final String description;

        ModifierType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 临时修改器类
     */
    public static class TemporaryModifier {
        private final String id;
        private final ModifierType type;
        private final double value;
        private final String tag;
        private int priority;
        private final long creationTime;

        public TemporaryModifier(String id, ModifierType type, double value, String tag, int priority) {
            this.id = id;
            this.type = type;
            this.value = value;
            this.tag = tag;
            this.priority = priority;
            this.creationTime = System.nanoTime();
        }

        public String getId() {
            return id;
        }

        public ModifierType getType() {
            return type;
        }

        public double getValue() {
            return value;
        }

        public String getTag() {
            return tag;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public long getCreationTime() {
            return creationTime;
        }

        /**
         * 检查标签是否以指定前缀开头
         * @param prefix 前缀
         * @return 是否匹配
         */
        public boolean tagStartsWith(String prefix) {
            return tag.startsWith(prefix);
        }

        /**
         * 获取标签在特定层级的值
         * @param level 层级（从0开始）
         * @return 层级值，如果层级不存在则返回null
         */
        public String getTagLevel(int level) {
            String[] parts = tag.split("\\.");
            if (level >= 0 && level < parts.length) {
                return parts[level];
            }
            return null;
        }

        @Override
        public String toString() {
            String valueStr;
            switch (type) {
                case MULTIPLICATIVE:
                case BASE_MULTIPLICATIVE:
                    valueStr = String.format("%+.1f%%", value * 100);
                    break;
                case MIN_LIMIT:
                    valueStr = String.format("最小值: %.2f", value);
                    break;
                case MAX_LIMIT:
                    valueStr = String.format("最大值: %.2f", value);
                    break;
                default:
                    valueStr = String.format("%+.2f", value);
            }
            return String.format("%s: %s (%s, %s, 优先级: %d)", id, valueStr, type.getDescription(), tag, priority);
        }
    }

    /**
     * 修改后的值类，实现Number接口以便于与Java数值系统集成
     */
    public class ModifiedValue extends Number implements Comparable<Number> {
        private static final long serialVersionUID = 1L;
        private final Predicate<TemporaryModifier> filter;
        private double cachedValue;

        private ModifiedValue(Predicate<TemporaryModifier> filter) {
            this.filter = filter;
            update();
        }

        /**
         * 更新缓存的值
         */
        void update() {
            double rawValue = calculateValue(filter);
            this.cachedValue = applyCalculationPrecision(rawValue);
        }

        /**
         * 获取当前值
         * @return 当前值
         */
        public double getValue() {
            return cachedValue;
        }

        /**
         * 获取格式化的值
         * @return 格式化后的字符串
         */
        public String getFormattedValue() {
            return applyDisplayFormat(cachedValue);
        }

        /**
         * 获取基础值
         * @return 基础值
         */
        public double getBaseValue() {
            return currentBaseValue;
        }

        /**
         * 获取修改百分比
         * @return 相对于基础值的变化百分比
         */
        public double getModificationPercentage() {
            return (cachedValue / currentBaseValue) - 1.0;
        }

        /**
         * 获取参与计算的修改器
         * @return 修改器列表
         */
        public List<TemporaryModifier> getContributingModifiers() {
            return getAllModifiers().stream()
                    .filter(filter)
                    .collect(Collectors.toList());
        }

        /**
         * 获取特定类型的修改器
         * @param type 修改器类型
         * @return 指定类型的修改器列表
         */
        public List<TemporaryModifier> getModifiersByType(ModifierType type) {
            return getContributingModifiers().stream()
                    .filter(mod -> mod.getType() == type)
                    .collect(Collectors.toList());
        }

        /**
         * 分析值的组成部分
         * @return 值分析结果
         */
        public ValueBreakdown getValueBreakdown() {
            return new ValueBreakdown(this);
        }

        // Number接口实现
        @Override
        public int intValue() {
            return (int) cachedValue;
        }

        @Override
        public long longValue() {
            return (long) cachedValue;
        }

        @Override
        public float floatValue() {
            return (float) cachedValue;
        }

        @Override
        public double doubleValue() {
            return cachedValue;
        }

        // Comparable接口实现
        @Override
        public int compareTo(Number other) {
            return Double.compare(cachedValue, other.doubleValue());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Number) {
                return Double.compare(cachedValue, ((Number) obj).doubleValue()) == 0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Double.hashCode(cachedValue);
        }

        @Override
        public String toString() {
            return applyDisplayFormat(cachedValue);
        }
    }

    /**
     * 值分析结果类，用于详细分析值的组成部分
     */
    public class ValueBreakdown {
        private final double baseValue;
        private final double baseMultiplierEffect;
        private final double additiveEffect;
        private final double multiplicativeEffect;
        private final double limitEffect;
        private final double finalValue;

        private ValueBreakdown(ModifiedValue value) {
            // 基础值
            this.baseValue = currentBaseValue;

            // 基础值乘法效果
            double baseMultSum = value.getModifiersByType(ModifierType.BASE_MULTIPLICATIVE).stream()
                    .mapToDouble(TemporaryModifier::getValue)
                    .sum();
            // 确保基础值乘法总和不低于-1（避免负值或零值）
            baseMultSum = Math.max(baseMultSum, -0.99);
            this.baseMultiplierEffect = currentBaseValue * baseMultSum;

            // 加法效果
            this.additiveEffect = value.getModifiersByType(ModifierType.ADDITIVE).stream()
                    .mapToDouble(TemporaryModifier::getValue)
                    .sum();

            // 乘法效果
            double multSum = value.getModifiersByType(ModifierType.MULTIPLICATIVE).stream()
                    .mapToDouble(TemporaryModifier::getValue)
                    .sum();
            // 确保乘法总和不低于-1（避免负值或零值）
            multSum = Math.max(multSum, -0.99);
            double afterAdditive = currentBaseValue * (1 + baseMultSum) + additiveEffect;
            this.multiplicativeEffect = afterAdditive * multSum;

            // 计算不含限制的值
            double unlimitedValue = afterAdditive * (1 + multSum);

            // 最终值
            this.finalValue = value.getValue();

            // 限制效果
            this.limitEffect = finalValue - unlimitedValue;
        }

        public double getBaseValue() {
            return baseValue;
        }

        public double getBaseMultiplierEffect() {
            return baseMultiplierEffect;
        }

        public double getAdditiveEffect() {
            return additiveEffect;
        }

        public double getMultiplicativeEffect() {
            return multiplicativeEffect;
        }

        public double getLimitEffect() {
            return limitEffect;
        }

        public double getFinalValue() {
            return finalValue;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("值分析:\n");
            sb.append(String.format("基础值: %.2f\n", baseValue));

            if (baseMultiplierEffect != 0) {
                sb.append(String.format("基础值乘法效果: %+.2f\n", baseMultiplierEffect));
            }

            if (additiveEffect != 0) {
                sb.append(String.format("加法效果: %+.2f\n", additiveEffect));
            }

            if (multiplicativeEffect != 0) {
                sb.append(String.format("乘法效果: %+.2f\n", multiplicativeEffect));
            }

            if (limitEffect != 0) {
                sb.append(String.format("限制效果: %+.2f\n", limitEffect));
            }

            sb.append(String.format("最终值: %.2f", finalValue));
            return sb.toString();
        }
    }
}
