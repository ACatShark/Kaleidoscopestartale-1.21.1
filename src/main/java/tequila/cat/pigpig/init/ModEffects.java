package tequila.cat.pigpig.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.effect.BloodthirstEffect;
import tequila.cat.pigpig.effect.BlueHourEffect;
import tequila.cat.pigpig.effect.InspirationDroughtEffect;
import tequila.cat.pigpig.effect.InspirationEffect;
import tequila.cat.pigpig.effect.JunimoBlessingEffect;
import tequila.cat.pigpig.effect.MealEffect;
import tequila.cat.pigpig.effect.RanchHeroEffect;
import tequila.cat.pigpig.effect.SmartEffect;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, KaleidoscopeStartale.MODID);

    /** 灵感充沛：受伤时消耗经验抵消伤害 */
    public static final DeferredHolder<MobEffect, MobEffect> INSPIRATION =
            MOB_EFFECTS.register("inspiration", InspirationEffect::new);
    /** 灵感枯：灵感充沛结束后的负面状态，期间无法再获得灵感 */
    public static final DeferredHolder<MobEffect, MobEffect> INSPIRATION_DROUGHT =
            MOB_EFFECTS.register("inspiration_drought", InspirationDroughtEffect::new);
    /** 嗜血：回复造成伤害 80% 的生命、禁止自然回血、攻击速度 +20% */
    public static final DeferredHolder<MobEffect, MobEffect> BLOODTHIRST =
            MOB_EFFECTS.register("bloodthirst", BloodthirstEffect::new);
    /** 聪明：持续期间身上物品不损耗耐久 */
    public static final DeferredHolder<MobEffect, MobEffect> SMART =
            MOB_EFFECTS.register("smart", SmartEffect::new);
    /** 祝尼魔的祝福：额外收获、镰刀范围收割、拾取范围扩大 */
    public static final DeferredHolder<MobEffect, MobEffect> JUNIMO_BLESSING =
            MOB_EFFECTS.register("junimo_blessing", JunimoBlessingEffect::new);
    /** 蓝调时刻：获得的其它状态效果持续时间 ×1.5 */
    public static final DeferredHolder<MobEffect, MobEffect> BLUE_HOUR =
            MOB_EFFECTS.register("blue_hour", BlueHourEffect::new);
    /** 膳食：进食速度翻倍 */
    public static final DeferredHolder<MobEffect, MobEffect> MEAL =
            MOB_EFFECTS.register("meal", MealEffect::new);
    /** 牧场英雄：拍拍器产出与拍击幅度提升 */
    public static final DeferredHolder<MobEffect, MobEffect> RANCH_HERO =
            MOB_EFFECTS.register("ranch_hero", RanchHeroEffect::new);

    private ModEffects() {
    }
}
