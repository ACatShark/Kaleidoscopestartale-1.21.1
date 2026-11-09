package com.tequilacat.kaleidoscope_startale.util;

import java.util.function.Supplier;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * 效果配对类。
 * <p>
 * 将状态效果实例的供应器与触发概率配对，
 * 供食物属性构建等场景延迟获取效果并指定生效概率。
 */
public class EffectPair {

    /**
     * 状态效果实例的供应器，用于延迟加载效果。
     */
    private final Supplier<MobEffectInstance> effect;

    /**
     * 效果触发概率，范围通常为 0.0 到 1.0。
     */
    private final float probability;

    /**
     * 私有构造函数，创建效果与概率的配对对象。
     *
     * @param effect      状态效果实例的供应器，用于延迟加载效果
     * @param probability 效果触发概率，范围通常为 0.0 到 1.0
     */
    private EffectPair(Supplier<MobEffectInstance> effect, float probability) {
        this.effect = effect;
        this.probability = probability;
    }

    /**
     * 工厂方法，创建 EffectPair 实例。
     *
     * @param effect      状态效果实例的供应器，用于延迟加载效果
     * @param probability 效果触发概率，范围通常为 0.0 到 1.0
     * @return 新创建的 EffectPair 对象
     */
    public static EffectPair of(Supplier<MobEffectInstance> effect, float probability) {
        return new EffectPair(effect, probability);
    }

    /**
     * 获取状态效果实例的供应器。
     *
     * @return MobEffectInstance 的 Supplier 对象
     */
    public Supplier<MobEffectInstance> getEffect() {
        return effect;
    }

    /**
     * 获取效果触发概率。
     *
     * @return 概率值，范围为 0.0 到 1.0
     */
    public float getProbability() {
        return probability;
    }
}