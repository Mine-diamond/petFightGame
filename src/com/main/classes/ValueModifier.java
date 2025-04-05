package com.main.classes;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ValueModifier - 一个灵活、强大的值修改系统
 * 明确分离基值和修改值的职责，支持修改器优先级和多种修改类型。
 */
public class ValueModifier {

    /**
     * 修改器类型枚举
     */
    public enum ModifierType {
        ADD,           // 加法修改器
        MULTIPLY,      // 乘法修改器
        BASE_MULTIPLY, // 只乘以基值的修改器 (特殊处理，优先级最低，基于原始值计算效果)
        SET_MAX,       // 设置上限修改器
        SET_MIN        // 设置下限修改器
    }

    // --- 默认优先级常量 ---
    public static final int PRIORITY_BASE_MULTIPLY = -100; // 特殊处理，实际在最前计算效果
    public static final int PRIORITY_ADD = 0;
    public static final int PRIORITY_MULTIPLY = 10;
    public static final int PRIORITY_SET_MIN_MAX = 100; // 通常限制类修改器优先级较高

    /**
     * 基值类 - 存储原始值和修改器
     * 注意: T 现在需要实现 Comparable 用于 SET_MAX/SET_MIN
     */
    public static class BaseValue<T extends Number & Comparable<T>> {
        private final T originalValue;
        // 使用 Map 存储所有修改器，方便按 ID 移除
        private final Map<String, Modifier<T>> modifiers = new LinkedHashMap<>();
        private T cachedResult; // 存储计算后的结果
        private boolean autoUpdate = true; // 是否自动更新
        private boolean dirty = true; // 标记是否需要重新计算

        /**
         * 创建一个基值
         */
        public BaseValue(T originalValue) {
            if (originalValue == null) {
                throw new IllegalArgumentException("Original value cannot be null");
            }
            this.originalValue = originalValue;
            this.cachedResult = originalValue;
        }

        /**
         * 获取原始值
         */
        public T getOriginalValue() {
            return originalValue;
        }

        /**
         * 创建一个关联的修改值对象
         */
        public ModifiedValue<T> createModifiedValue() {
            // 不再需要维护 modifiedValues 列表
            return new ModifiedValue<>(this);
        }

        /**
         * 设置是否自动更新
         */
        public BaseValue<T> setAutoUpdate(boolean autoUpdate) {
            this.autoUpdate = autoUpdate;
            if (autoUpdate && dirty) {
                updateCachedResult(); // 如果设为自动更新且当前是脏状态，立即更新
            }
            return this;
        }

        /**
         * 添加修改器 - 对象版本
         */
        public BaseValue<T> addModifier(Modifier<T> modifier) {
            if (modifier == null) {
                throw new IllegalArgumentException("Modifier cannot be null");
            }
            modifiers.put(modifier.getId(), modifier);
            setDirty();
            return this;
        }

        // --- 重载 addModifier 方法以支持优先级和直接参数 ---

        /**
         * 添加修改器 - 指定 ID、值、类型和优先级
         */
        public BaseValue<T> addModifier(String id, T value, ModifierType type, int priority) {
            switch (type) {
                case ADD:
                    return addModifier(new AddModifier<>(id, value, priority));
                case MULTIPLY:
                    return addModifier(new MultiplyModifier<>(id, value, priority));
                case BASE_MULTIPLY:
                    // BaseMultiply 的优先级是固定的，用于计算逻辑，传入的 priority 被忽略
                    return addModifier(new BaseMultiplyModifier<>(id, value));
                case SET_MAX:
                    return addModifier(new SetMaxModifier<>(id, value, priority));
                case SET_MIN:
                    return addModifier(new SetMinModifier<>(id, value, priority));
                default:
                    throw new IllegalArgumentException("未知的修改器类型: " + type);
            }
        }

        /**
         * 添加修改器 - 指定 ID、值、类型（使用默认优先级）
         */
        public BaseValue<T> addModifier(String id, T value, ModifierType type) {
            int defaultPriority;
            switch (type) {
                case ADD:         defaultPriority = PRIORITY_ADD; break;
                case MULTIPLY:    defaultPriority = PRIORITY_MULTIPLY; break;
                case SET_MAX:
                case SET_MIN:     defaultPriority = PRIORITY_SET_MIN_MAX; break;
                case BASE_MULTIPLY: // BaseMultiply 特殊处理
                    return addModifier(new BaseMultiplyModifier<>(id, value));
                default: throw new IllegalArgumentException("未知的修改器类型: " + type);
            }
            return addModifier(id, value, type, defaultPriority);
        }

        /**
         * 添加修改器 - 自动生成 ID，指定值、类型和优先级
         */
        public BaseValue<T> addModifier(T value, ModifierType type, int priority) {
            return addModifier(UUID.randomUUID().toString(), value, type, priority);
        }

        /**
         * 添加修改器 - 自动生成 ID，指定值、类型（使用默认优先级）
         */
        public BaseValue<T> addModifier(T value, ModifierType type) {
            return addModifier(UUID.randomUUID().toString(), value, type);
        }


        /**
         * 移除修改器
         */
        public BaseValue<T> removeModifier(String modifierId) {
            if (modifiers.remove(modifierId) != null) {
                setDirty();
            }
            return this;
        }

        /**
         * 获取所有修改器 (不可变映射)
         */
        public Map<String, Modifier<T>> getAllModifiers() {
            return Collections.unmodifiableMap(modifiers);
        }

        /**
         * 应用所有修改器，计算修改后的值
         */
        public T calculateModifiedValue() {
            // 如果没有修改且不需要强制计算，直接返回缓存结果
            if (!dirty) {
                return cachedResult;
            }

            // 1. 分离 BASE_MULTIPLY 和其他修改器
            List<BaseMultiplyModifier<T>> baseMultipliers = new ArrayList<>();
            List<Modifier<T>> otherModifiers = new ArrayList<>();

            for (Modifier<T> mod : modifiers.values()) {
                if (mod.getType() == ModifierType.BASE_MULTIPLY) {
                    baseMultipliers.add((BaseMultiplyModifier<T>) mod);
                } else {
                    otherModifiers.add(mod);
                }
            }

            // 2. 计算基值乘法的总效果 (基于原始值)
            //    这部分逻辑在所有其他优先级修改器之前计算其 *效果*
            Number baseMultiplierEffect = calculateBaseMultiplierEffect(baseMultipliers);

            // 3. 应用基值乘法效果得到初始计算值
            Number currentValue = applyBaseMultiplierEffect(originalValue, baseMultiplierEffect);


            // 4. 对其他修改器按优先级排序 (数值小的优先)
            otherModifiers.sort(Comparator.comparingInt(Modifier::getPriority));

            // 5. 按优先级顺序应用其他修改器
            for (Modifier<T> modifier : otherModifiers) {
                // BASE_MULTIPLY 不在这里应用，因为它已经被处理了
                if (modifier.getType() != ModifierType.BASE_MULTIPLY) {
                    currentValue = applyModifier(modifier, currentValue);
                }
            }

            // 6. 更新缓存结果
            // 需要确保 currentValue 是正确的 T 类型
            cachedResult = castResult(currentValue, originalValue);
            dirty = false;

            return cachedResult;
        }

        /**
         * 计算所有 BaseMultiplyModifier 的累加效果 (返回一个加数值)
         */
        private Number calculateBaseMultiplierEffect(List<BaseMultiplyModifier<T>> baseMultipliers) {
            if (baseMultipliers.isEmpty()) {
                // 根据原始值类型返回 0
                if (originalValue instanceof Double || originalValue instanceof Float) return 0.0;
                if (originalValue instanceof Long) return 0L;
                return 0; // Default to Integer 0
            }

            // 使用 BigDecimal 进行中间计算以提高精度
            java.math.BigDecimal totalBaseMultiplierEffect = java.math.BigDecimal.ZERO;
            java.math.BigDecimal originalValueDecimal = new java.math.BigDecimal(originalValue.toString());

            for (BaseMultiplyModifier<T> multiplier : baseMultipliers) {
                java.math.BigDecimal factorDecimal = new java.math.BigDecimal(multiplier.getFactor().toString());
                // effect = (factor - 1) * originalValue
                java.math.BigDecimal effect = factorDecimal.subtract(java.math.BigDecimal.ONE).multiply(originalValueDecimal);
                totalBaseMultiplierEffect = totalBaseMultiplierEffect.add(effect);
            }

            // 根据原始类型转换回 Number
            if (originalValue instanceof Double) return totalBaseMultiplierEffect.doubleValue();
            if (originalValue instanceof Float) return totalBaseMultiplierEffect.floatValue();
            if (originalValue instanceof Long) return totalBaseMultiplierEffect.longValueExact(); // May throw ArithmeticException if lossy
            if (originalValue instanceof Integer) return totalBaseMultiplierEffect.intValueExact(); // May throw ArithmeticException if lossy
            // Fallback for other Number types might be needed or throw exception
            // For now, assume Double for unknown precise types
            return totalBaseMultiplierEffect.doubleValue();
        }

        /**
         * 将 BaseMultiplier 的效果应用到原始值上
         */
        private Number applyBaseMultiplierEffect(T original, Number effect) {
            // 使用 BigDecimal 进行加法
            java.math.BigDecimal originalDecimal = new java.math.BigDecimal(original.toString());
            java.math.BigDecimal effectDecimal = new java.math.BigDecimal(effect.toString());
            java.math.BigDecimal resultDecimal = originalDecimal.add(effectDecimal);

            // 转换回原始类型对应的 Number
            if (originalValue instanceof Double) return resultDecimal.doubleValue();
            if (originalValue instanceof Float) return resultDecimal.floatValue();
            if (originalValue instanceof Long) return resultDecimal.longValueExact();
            if (originalValue instanceof Integer) return resultDecimal.intValueExact();
            // Fallback
            return resultDecimal.doubleValue();
        }


        /**
         * 应用单个常规修改器 (ADD, MULTIPLY, SET_MAX, SET_MIN)
         */
        @SuppressWarnings("unchecked")
        private Number applyModifier(Modifier<T> modifier, Number currentNumberValue) {
            T currentValue = castResult(currentNumberValue, originalValue); // Ensure current value is of type T for comparison/operations

            switch (modifier.getType()) {
                case ADD:
                    AddModifier<T> addMod = (AddModifier<T>) modifier;
                    // 使用 BigDecimal 保证精度
                    return new java.math.BigDecimal(currentValue.toString())
                            .add(new java.math.BigDecimal(addMod.getAmount().toString()));
                case MULTIPLY:
                    MultiplyModifier<T> mulMod = (MultiplyModifier<T>) modifier;
                    // 使用 BigDecimal 保证精度
                    return new java.math.BigDecimal(currentValue.toString())
                            .multiply(new java.math.BigDecimal(mulMod.getFactor().toString()));
                case SET_MAX:
                    SetMaxModifier<T> maxMod = (SetMaxModifier<T>) modifier;
                    T maxValue = maxMod.getMaxValue();
                    // currentValue > maxValue ? maxValue : currentValue
                    return currentValue.compareTo(maxValue) > 0 ? maxValue : currentValue;
                case SET_MIN:
                    SetMinModifier<T> minMod = (SetMinModifier<T>) modifier;
                    T minValue = minMod.getMinValue();
                    // currentValue < minValue ? minValue : currentValue
                    return currentValue.compareTo(minValue) < 0 ? minValue : currentValue;
                case BASE_MULTIPLY:
                    // Base multiply effect is pre-calculated and applied, should not reach here in normal flow
                    System.err.println("Warning: BASE_MULTIPLY modifier encountered during prioritized application phase. Ignored.");
                    return currentValue; // No change
                default:
                    // Should not happen if enum is exhaustive
                    return currentValue;
            }
        }

        /**
         * 尝试将计算结果 (可能是 BigDecimal 或其他 Number) 转换回原始类型 T。
         * 这对于保持类型一致性很重要，但可能涉及精度损失或溢出。
         */
        @SuppressWarnings("unchecked")
        private T castResult(Number result, T originalTypeIndicator) {
            if (result instanceof java.math.BigDecimal) {
                java.math.BigDecimal bdResult = (java.math.BigDecimal) result;
                if (originalTypeIndicator instanceof Double) return (T) Double.valueOf(bdResult.doubleValue());
                if (originalTypeIndicator instanceof Float) return (T) Float.valueOf(bdResult.floatValue());
                if (originalTypeIndicator instanceof Long) {
                    try { return (T) Long.valueOf(bdResult.longValueExact()); } catch (ArithmeticException e) { /* Handle overflow/lossy conversion */ return (T) Long.valueOf(bdResult.longValue()); } // Or throw?
                }
                if (originalTypeIndicator instanceof Integer) {
                    try { return (T) Integer.valueOf(bdResult.intValueExact()); } catch (ArithmeticException e) { /* Handle overflow/lossy conversion */ return (T) Integer.valueOf(bdResult.intValue()); } // Or throw?
                }
                // Add more types if needed (e.g., Short, Byte)
                // Fallback or throw exception for unsupported T
                return (T) result; // Or throw?
            } else if (result.getClass() == originalTypeIndicator.getClass()) {
                // Already the correct type (e.g., from SET_MAX/MIN)
                return (T) result;
            } else {
                // Handle cases where result is Double/Integer etc. but not BigDecimal
                // This might happen if only SET_MAX/MIN modifiers were applied
                if (originalTypeIndicator instanceof Double) return (T) Double.valueOf(result.doubleValue());
                if (originalTypeIndicator instanceof Float) return (T) Float.valueOf(result.floatValue());
                if (originalTypeIndicator instanceof Long) return (T) Long.valueOf(result.longValue());
                if (originalTypeIndicator instanceof Integer) return (T) Integer.valueOf(result.intValue());
                // Fallback
                return (T) result; // Best effort, might be wrong type
            }
        }


        /**
         * 标记状态为 dirty 并根据 autoUpdate 更新缓存
         */
        private void setDirty() {
            if (!dirty) {
                dirty = true;
                if (autoUpdate) {
                    updateCachedResult();
                }
            }
        }

        /**
         * 更新缓存的结果 (如果需要)
         */
        private void updateCachedResult() {
            if (dirty) {
                calculateModifiedValue();
            }
        }

        /**
         * 获取计算后的值 (最终值)
         */
        public T getModifiedValue() {
            if (dirty && autoUpdate) {
                updateCachedResult();
            } else if (dirty && !autoUpdate) {
                // 如果是 dirty 且非自动更新，需要手动计算才能获取最新值
                return calculateModifiedValue();
            }
            return cachedResult;
        }

        /**
         * 清除所有修改器
         */
        public void clearModifiers() {
            if (!modifiers.isEmpty()) {
                modifiers.clear();
                setDirty();
            }
        }

        // --- 易用性方法 ---

        /**
         * 获取计算后的 Double 值
         */
        public double getAsDouble() {
            return getModifiedValue().doubleValue();
        }

        /**
         * 获取计算后的 Integer 值
         */
        public int getAsInt() {
            return getModifiedValue().intValue();
        }

        /**
         * 获取计算后的 Long 值
         */
        public long getAsLong() {
            return getModifiedValue().longValue();
        }

        /**
         * 获取计算后的 Float 值
         */
        public float getAsFloat() {
            return getModifiedValue().floatValue();
        }

        // --- 快捷方法添加修改器 (使用默认优先级) ---

        /**
         * 添加加法修改器 (默认优先级)
         */
        public BaseValue<T> add(T value) {
            return addModifier(value, ModifierType.ADD);
        }

        /**
         * 添加乘法修改器 (默认优先级)
         */
        public BaseValue<T> multiply(T value) {
            return addModifier(value, ModifierType.MULTIPLY);
        }

        /**
         * 添加基值乘法修改器
         */
        public BaseValue<T> baseMultiply(T value) {
            return addModifier(value, ModifierType.BASE_MULTIPLY);
        }

        /**
         * 添加设置上限修改器 (默认优先级)
         */
        public BaseValue<T> setMax(T maxValue) {
            return addModifier(maxValue, ModifierType.SET_MAX);
        }

        /**
         * 添加设置下限修改器 (默认优先级)
         */
        public BaseValue<T> setMin(T minValue) {
            return addModifier(minValue, ModifierType.SET_MIN);
        }


        @Override
        public String toString() {
            // 在 toString 中调用 getModifiedValue 会触发计算（如果 dirty）
            return String.format("BaseValue[original=%s, modified=%s, modifiers=%d]",
                    originalValue, getModifiedValue(), modifiers.size());
        }
    }

    /**
     * 修改值类 - 可直接用于计算的修改后值
     * 关联的 BaseValue 的 T 需要实现 Comparable
     */
    public static class ModifiedValue<T extends Number & Comparable<T>> extends Number implements Comparable<Number> {
        private final BaseValue<T> baseValue;

        /**
         * 创建一个修改值，关联到指定的基值
         */
        ModifiedValue(BaseValue<T> baseValue) {
            if (baseValue == null) {
                throw new IllegalArgumentException("BaseValue cannot be null");
            }
            this.baseValue = baseValue;
        }

        /**
         * 获取关联的基值
         */
        public BaseValue<T> getBaseValue() {
            return baseValue;
        }

        /**
         * 获取修改后的值 (总是从 BaseValue 获取最新计算结果)
         */
        public T getValue() {
            return baseValue.getModifiedValue();
        }

        /**
         * 获取原始值
         */
        public T getOriginalValue() {
            return baseValue.getOriginalValue();
        }

        // --- 继承自 Number 的方法 ---
        @Override public int intValue() { return getValue().intValue(); }
        @Override public long longValue() { return getValue().longValue(); }
        @Override public float floatValue() { return getValue().floatValue(); }
        @Override public double doubleValue() { return getValue().doubleValue(); }

        // --- 实现 Comparable ---
        @Override
        public int compareTo(Number other) {
            // 使用 BigDecimal 进行比较以提高精度和避免类型问题
            return new java.math.BigDecimal(this.toString())
                    .compareTo(new java.math.BigDecimal(other.toString()));
        }

        // --- 创建新 ModifiedValue 的操作 (返回新实例，不修改原 BaseValue) ---
        // 注意：这些操作创建的是全新的、独立的 BaseValue/ModifiedValue
        // 它们的值是基于当前 ModifiedValue 的 *计算结果*，而不是原始 BaseValue
        // 这可能符合预期，也可能不符合，取决于具体用例。

        /**
         * 创建一个新的 ModifiedValue，其值为当前值的函数结果。
         * 注意：返回的 ModifiedValue 的 BaseValue 是新创建的。
         * 需要确保 R 类型也满足约束。
         */
        @SuppressWarnings("unchecked")
        public <R extends Number & Comparable<R>> ModifiedValue<R> map(Function<T, R> mapper) {
            R newValue = mapper.apply(getValue());
            return new ModifiedValue<>(new BaseValue<>(newValue)); // 创建新的 BaseValue
        }

        /**
         * 创建一个新的 ModifiedValue，其值为当前值加上指定值。
         * 返回的 ModifiedValue 基于新的 BaseValue。
         */
        public ModifiedValue<T> plus(Number value) {
            java.math.BigDecimal result = new java.math.BigDecimal(this.toString())
                    .add(new java.math.BigDecimal(value.toString()));
            return new ModifiedValue<>(new BaseValue<>(castResult(result, getValue())));
        }

        /**
         * 创建一个新的 ModifiedValue，其值为当前值减去指定值。
         * 返回的 ModifiedValue 基于新的 BaseValue。
         */
        public ModifiedValue<T> minus(Number value) {
            java.math.BigDecimal result = new java.math.BigDecimal(this.toString())
                    .subtract(new java.math.BigDecimal(value.toString()));
            return new ModifiedValue<>(new BaseValue<>(castResult(result, getValue())));
        }

        /**
         * 创建一个新的 ModifiedValue，其值为当前值乘以指定值。
         * 返回的 ModifiedValue 基于新的 BaseValue。
         */
        public ModifiedValue<T> times(Number value) {
            java.math.BigDecimal result = new java.math.BigDecimal(this.toString())
                    .multiply(new java.math.BigDecimal(value.toString()));
            // 对于乘法，可能需要指定精度和舍入模式
            // java.math.BigDecimal result = currentDecimal.multiply(new java.math.BigDecimal(value.toString()), java.math.MathContext.DECIMAL64);
            return new ModifiedValue<>(new BaseValue<>(castResult(result, getValue())));
        }

        /**
         * 创建一个新的 ModifiedValue，其值为当前值除以指定值。
         * 返回的 ModifiedValue 基于新的 BaseValue。
         * 注意：除法需要处理精度和除零错误。
         */
        public ModifiedValue<T> dividedBy(Number value) {
            java.math.BigDecimal divisor = new java.math.BigDecimal(value.toString());
            if (divisor.compareTo(java.math.BigDecimal.ZERO) == 0) {
                throw new ArithmeticException("Division by zero");
            }
            // 可能需要指定 scale 和 RoundingMode
            java.math.BigDecimal result = new java.math.BigDecimal(this.toString())
                    .divide(divisor, java.math.MathContext.DECIMAL64); // 使用标准精度
            return new ModifiedValue<>(new BaseValue<>(castResult(result, getValue())));
        }

        // --- 类型转换方法 (返回新的 ModifiedValue) ---

        /**
         * 转换为 Double 类型的 ModifiedValue (基于新 BaseValue)
         */
        public ModifiedValue<Double> asDouble() {
            // 确保 Double 也实现了 Comparable (它确实实现了)
            return new ModifiedValue<>(new BaseValue<>(getValue().doubleValue()));
        }

        /**
         * 转换为 Integer 类型的 ModifiedValue (基于新 BaseValue)
         */
        public ModifiedValue<Integer> asInt() {
            // 确保 Integer 也实现了 Comparable (它确实实现了)
            return new ModifiedValue<>(new BaseValue<>(getValue().intValue()));
        }


        @Override
        public String toString() {
            return getValue().toString(); // 直接返回计算后的值字符串
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            // 比较数值，使用 compareTo 保证精度和类型兼容性
            if (obj instanceof Number) {
                // return Double.compare(this.doubleValue(), ((Number) obj).doubleValue()) == 0; // 可能有精度问题
                return this.compareTo((Number) obj) == 0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            // HashCode 基于计算后的值
            // 注意：如果值是 Double/Float，其 hashCode 行为可能需要注意
            return getValue().hashCode();
        }

        // Helper method from BaseValue needed here too for casting in arithmetic operations
        @SuppressWarnings("unchecked")
        private T castResult(Number result, T originalTypeIndicator) {
            return baseValue.castResult(result, originalTypeIndicator); // Delegate to BaseValue's caster
        }
    }

    /**
     * 修改器接口
     */
    public interface Modifier<T extends Number & Comparable<T>> {
        String getId();
        ModifierType getType();
        int getPriority(); // 获取优先级

        @Override
        String toString();
    }

    // --- 具体修改器实现 ---

    /**
     * 加法修改器
     */
    public static class AddModifier<T extends Number & Comparable<T>> implements Modifier<T> {
        private final String id;
        private final T amount;
        private final int priority;

        public AddModifier(String id, T amount, int priority) {
            this.id = id;
            this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
            this.priority = priority;
        }

        @Override public String getId() { return id; }
        @Override public ModifierType getType() { return ModifierType.ADD; }
        @Override public int getPriority() { return priority; }
        public T getAmount() { return amount; }

        @Override public String toString() {
            return String.format("AddModifier[id=%s, amount=%s, priority=%d]", id, amount, priority);
        }
    }

    /**
     * 乘法修改器
     */
    public static class MultiplyModifier<T extends Number & Comparable<T>> implements Modifier<T> {
        private final String id;
        private final T factor;
        private final int priority;

        public MultiplyModifier(String id, T factor, int priority) {
            this.id = id;
            this.factor = Objects.requireNonNull(factor, "Factor cannot be null");
            this.priority = priority;
        }

        @Override public String getId() { return id; }
        @Override public ModifierType getType() { return ModifierType.MULTIPLY; }
        @Override public int getPriority() { return priority; }
        public T getFactor() { return factor; }

        @Override public String toString() {
            return String.format("MultiplyModifier[id=%s, factor=%s, priority=%d]", id, factor, priority);
        }
    }

    /**
     * 基值乘法修改器 - 只乘以原始基值
     * 优先级固定且特殊，在计算流程中优先处理其效果。
     */
    public static class BaseMultiplyModifier<T extends Number & Comparable<T>> implements Modifier<T> {
        private final String id;
        private final T factor;

        public BaseMultiplyModifier(String id, T factor) {
            this.id = id;
            this.factor = Objects.requireNonNull(factor, "Factor cannot be null");
        }

        @Override public String getId() { return id; }
        @Override public ModifierType getType() { return ModifierType.BASE_MULTIPLY; }
        // 优先级固定，象征性返回一个低值，实际计算逻辑不同
        @Override public int getPriority() { return PRIORITY_BASE_MULTIPLY; }
        public T getFactor() { return factor; }

        @Override public String toString() {
            return String.format("BaseMultiplyModifier[id=%s, factor=%s]", id, factor);
        }
    }

    /**
     * 设置上限修改器
     */
    public static class SetMaxModifier<T extends Number & Comparable<T>> implements Modifier<T> {
        private final String id;
        private final T maxValue;
        private final int priority;

        public SetMaxModifier(String id, T maxValue, int priority) {
            this.id = id;
            this.maxValue = Objects.requireNonNull(maxValue, "Max value cannot be null");
            this.priority = priority;
        }

        @Override public String getId() { return id; }
        @Override public ModifierType getType() { return ModifierType.SET_MAX; }
        @Override public int getPriority() { return priority; }
        public T getMaxValue() { return maxValue; }

        @Override public String toString() {
            return String.format("SetMaxModifier[id=%s, maxValue=%s, priority=%d]", id, maxValue, priority);
        }
    }

    /**
     * 设置下限修改器
     */
    public static class SetMinModifier<T extends Number & Comparable<T>> implements Modifier<T> {
        private final String id;
        private final T minValue;
        private final int priority;

        public SetMinModifier(String id, T minValue, int priority) {
            this.id = id;
            this.minValue = Objects.requireNonNull(minValue, "Min value cannot be null");
            this.priority = priority;
        }

        @Override public String getId() { return id; }
        @Override public ModifierType getType() { return ModifierType.SET_MIN; }
        @Override public int getPriority() { return priority; }
        public T getMinValue() { return minValue; }

        @Override public String toString() {
            return String.format("SetMinModifier[id=%s, minValue=%s, priority=%d]", id, minValue, priority);
        }
    }


    // --- 工厂方法 (保持不变，但注意泛型约束变化) ---

    /**
     * 工厂方法 - 创建 Double 类型的基值
     */
    public static BaseValue<Double> createBaseDouble(double initialValue) {
        return new BaseValue<>(initialValue);
    }

    /**
     * 工厂方法 - 创建 Integer 类型的基值
     */
    public static BaseValue<Integer> createBaseInt(int initialValue) {
        return new BaseValue<>(initialValue);
    }

    /**
     * 工厂方法 - 创建 Long 类型的基值
     */
    public static BaseValue<Long> createBaseLong(long initialValue) {
        return new BaseValue<>(initialValue);
    }

    /**
     * 工厂方法 - 创建 Double 类型的修改值
     */
    public static ModifiedValue<Double> createModifiedDouble(double initialValue) {
        return new BaseValue<>(initialValue).createModifiedValue();
    }

    /**
     * 工厂方法 - 创建 Integer 类型的修改值
     */
    public static ModifiedValue<Integer> createModifiedInt(int initialValue) {
        return new BaseValue<>(initialValue).createModifiedValue();
    }

    /**
     * 工厂方法 - 创建 Long 类型的修改值
     */
    public static ModifiedValue<Long> createModifiedLong(long initialValue) {
        return new BaseValue<>(initialValue).createModifiedValue();
    }


    /**
     * 使用示例 (更新以展示新功能)
     */
    public static void main(String[] args) {
        System.out.println("--- Double 示例 ---");
        BaseValue<Double> attackDamage = createBaseDouble(100.0);
        ModifiedValue<Double> finalDamage = attackDamage.createModifiedValue();

        // 添加修改器，注意优先级
        attackDamage.addModifier("base_boost", 1.5, ModifierType.BASE_MULTIPLY); // 效果: (1.5-1)*100 = +50
        attackDamage.addModifier("flat_bonus", 20.0, ModifierType.ADD, PRIORITY_ADD); // 优先级 0
        attackDamage.addModifier("percent_increase", 1.2, ModifierType.MULTIPLY, PRIORITY_MULTIPLY); // 优先级 10
        attackDamage.addModifier("gear_cap", 200.0, ModifierType.SET_MAX, PRIORITY_SET_MIN_MAX); // 优先级 100
        attackDamage.addModifier("curse_reduction", 0.8, ModifierType.MULTIPLY, 5); // 优先级 5 (介于 ADD 和 MULTIPLY 之间)

        // 计算过程模拟:
        // 1. 原始值: 100.0
        // 2. BaseMultiply 效果: +50.0 => 初始计算值 150.0
        // 3. 按优先级排序应用其他修改器:
        //    - Prio 0: ADD +20.0 => 150.0 + 20.0 = 170.0
        //    - Prio 5: MULTIPLY *0.8 => 170.0 * 0.8 = 136.0
        //    - Prio 10: MULTIPLY *1.2 => 136.0 * 1.2 = 163.2
        //    - Prio 100: SET_MAX 200.0 => min(163.2, 200.0) = 163.2

        System.out.println("原始伤害: " + attackDamage.getOriginalValue()); // 100.0
        System.out.println("所有修改器: ");
        attackDamage.getAllModifiers().values().stream()
                .sorted(Comparator.comparingInt(Modifier::getPriority))
                .forEach(m -> System.out.println("  " + m));
        System.out.println("最终伤害 (BaseValue): " + attackDamage.getModifiedValue()); // 预期: 163.2
        System.out.println("最终伤害 (ModifiedValue): " + finalDamage); // 预期: 163.2
        System.out.println("最终伤害 (int, BaseValue): " + attackDamage.getAsInt()); // 预期: 163
        System.out.println("最终伤害 (double, ModifiedValue): " + finalDamage.doubleValue()); // 预期: 163.2

        // 添加一个 SetMin
        attackDamage.addModifier("min_guarantee", 170.0, ModifierType.SET_MIN, PRIORITY_SET_MIN_MAX + 1); // Prio 101
        // 计算过程模拟:
        // ... 上一步结果 163.2
        //    - Prio 101: SET_MIN 170.0 => max(163.2, 170.0) = 170.0
        System.out.println("添加 Min Guarantee 后: " + finalDamage); // 预期: 170.0

        // 移除一个修改器
        attackDamage.removeModifier("curse_reduction");
        System.out.println("移除 Curse Reduction 后: " + finalDamage); // 预期会重新计算
        // 计算过程模拟:
        // 1. 原始值: 100.0
        // 2. BaseMultiply 效果: +50.0 => 初始计算值 150.0
        // 3. 按优先级排序应用其他修改器:
        //    - Prio 0: ADD +20.0 => 150.0 + 20.0 = 170.0
        //    - Prio 10: MULTIPLY *1.2 => 170.0 * 1.2 = 204.0
        //    - Prio 100: SET_MAX 200.0 => min(204.0, 200.0) = 200.0
        //    - Prio 101: SET_MIN 170.0 => max(200.0, 170.0) = 200.0
        System.out.println("移除 Curse Reduction 后预期: 200.0, 实际: " + finalDamage); // 预期: 200.0

        System.out.println("\n--- Integer 示例 ---");
        BaseValue<Integer> score = createBaseInt(1000);
        ModifiedValue<Integer> finalScore = score.createModifiedValue();

        score.add(100); // Prio 0
        score.multiply(2); // Prio 10
        score.setMax(2500); // Prio 100
        score.setMin(500); // Prio 100
        score.addModifier("bonus", 50, ModifierType.ADD, -5); // Prio -5 (比默认 ADD 更早)

        // 计算模拟:
        // 1. 原始值: 1000
        // 2. BaseMultiply: 无
        // 3. 排序应用:
        //    - Prio -5: ADD +50 => 1000 + 50 = 1050
        //    - Prio 0: ADD +100 => 1050 + 100 = 1150
        //    - Prio 10: MULTIPLY *2 => 1150 * 2 = 2300
        //    - Prio 100: SET_MAX 2500 => min(2300, 2500) = 2300
        //    - Prio 100: SET_MIN 500 => max(2300, 500) = 2300 (注意: 同优先级的应用顺序取决于 LinkedHashMap 插入顺序或 sort 稳定性，但对于min/max通常结果一致)
        System.out.println("最终分数: " + finalScore); // 预期: 2300

        // ModifiedValue 的运算 (创建新实例)
        ModifiedValue<Integer> scorePlus1000 = finalScore.plus(1000);
        System.out.println("Final Score: " + finalScore); // 仍然是 2300
        System.out.println("Score + 1000: " + scorePlus1000); // 3300 (这是一个新的 ModifiedValue)
        System.out.println("Original base value still intact: " + score.getModifiedValue()); // 2300
    }
}