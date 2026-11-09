package tequila.cat.pigpig.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 牧场英雄：拍拍器产出提升、拍击幅度更大（逻辑在 PetterItem）。 */
public class RanchHeroEffect extends MobEffect {
    public static final int COLOR = 0x00E0A63C;

    public RanchHeroEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
