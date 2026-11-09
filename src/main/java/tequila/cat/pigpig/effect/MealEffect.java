package tequila.cat.pigpig.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 膳食：进食速度翻倍（逻辑在 ItemStackMixin）。 */
public class MealEffect extends MobEffect {
    public static final int COLOR = 0x00D9A441;

    public MealEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
