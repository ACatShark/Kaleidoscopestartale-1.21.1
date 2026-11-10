package com.tequilacat.kaleidoscope_startale.init;

import com.github.ysbbbbbb.kaleidoscopecookery.init.registry.TeacupRegistry;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class KSTeacups {

    /** 抹茶 */
    public static ResourceLocation MATCHA_TEA;
    /** 玫瑰花茶 */
    public static ResourceLocation ROSE_TEA;

    public static void init() {
        TeacupRegistry registry = new TeacupRegistry();

        MATCHA_TEA = registry.registerTeacupData(KaleidoscopeStartale.id("matcha_tea"),
                TeacupRegistry.TeacupData.create(4).addEffect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 2 * 60 * 20)));
        ROSE_TEA = registry.registerTeacupData(KaleidoscopeStartale.id("rose_tea"),
                TeacupRegistry.TeacupData.create(4).addEffect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 2 * 60 * 20)));
    }
}
