package tequila.cat.pigpig.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 蓝调时刻：之后获得的其它状态效果持续时间 ×1.5（逻辑在 LivingEntityMixin）。 */
public class BlueHourEffect extends MobEffect {
    public static final int COLOR = 0x00324B8C;

    public BlueHourEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
