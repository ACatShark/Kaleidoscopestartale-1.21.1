package tequila.cat.pigpig.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 聪明：持续期间实体身上物品不损耗耐久（逻辑在 ItemStackMixin）。 */
public class SmartEffect extends MobEffect {
    /** 取拍拍器贴图的平均色。 */
    public static final int COLOR = 0x0069735D;

    public SmartEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
