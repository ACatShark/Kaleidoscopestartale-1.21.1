package com.tequilacat.kaleidoscope_startale.init;

import com.tequilacat.kaleidoscope_startale.effect.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;

public interface KSEffects {
    DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, KaleidoscopeStartale.MODID);

    /** 灵感充沛：受伤时消耗经验抵消伤害 */
    DeferredHolder<MobEffect, MobEffect> INSPIRATION = MOB_EFFECTS.register("inspiration", () -> new InspirationEffect(0x00F2C9CE));
    /** 灵感枯：灵感充沛结束后的负面状态，期间无法再获得灵感 */
    DeferredHolder<MobEffect, MobEffect> INSPIRATION_DROUGHT = MOB_EFFECTS.register("inspiration_drought", () -> new InspirationDroughtEffect(0x006B5F3B));
    /** 嗜血：回复造成伤害 80% 的生命、禁止自然回血、攻击速度 +20% */
    DeferredHolder<MobEffect, MobEffect> BLOODTHIRST = MOB_EFFECTS.register("bloodthirst", () -> new BloodthirstEffect(0x00B3232A));
    /** 聪明：持续期间身上物品不损耗耐久 */
    DeferredHolder<MobEffect, MobEffect> SMART = MOB_EFFECTS.register("smart", () -> new SmartEffect(0x0069735D));
    /** 祝尼魔的祝福：额外收获、镰刀范围收割、拾取范围扩大 */
    DeferredHolder<MobEffect, MobEffect> JUNIMO_BLESSING = MOB_EFFECTS.register("junimo_blessing", () -> new JunimoBlessingEffect(0x0077C043));
    /** 蓝调时刻：获得的其它状态效果持续时间 ×1.5 */
    DeferredHolder<MobEffect, MobEffect> BLUE_HOUR = MOB_EFFECTS.register("blue_hour", () -> new BlueHourEffect(0x00324B8C));
    /** 膳食：进食速度翻倍 */
    DeferredHolder<MobEffect, MobEffect> MEAL = MOB_EFFECTS.register("meal", () -> new MealEffect(0x00D9A441));
    /** 牧场英雄：拍拍器产出与拍击幅度提升 */
    DeferredHolder<MobEffect, MobEffect> RANCH_HERO = MOB_EFFECTS.register("ranch_hero", () -> new RanchHeroEffect(0x00E0A63C));
    /** 清爽：提升移动速度与挖掘速度 */
    DeferredHolder<MobEffect, MobEffect> REFRESHING = MOB_EFFECTS.register("refreshing", () -> new RefreshingEffect(0x007EC8E3));
    /** 回甘：降低饱食度消耗 */
    DeferredHolder<MobEffect, MobEffect> AFTER_TASTE = MOB_EFFECTS.register("after_taste", () -> new AftertasteEffect(0x0090EE90));
    /** 提神：免疫部分负面效果 */
    DeferredHolder<MobEffect, MobEffect> INVIGORATING = MOB_EFFECTS.register("invigorating", () -> new InvigoratingEffect(0x00FFD700));
    /** 余韵：周期性回复饱食度和生命值 */
    DeferredHolder<MobEffect, MobEffect> LINGERING = MOB_EFFECTS.register("lingering", () -> new LingeringEffect(0x00E6E6FA));

    static void register(IEventBus bus){
        MOB_EFFECTS.register(bus);
    }
}
