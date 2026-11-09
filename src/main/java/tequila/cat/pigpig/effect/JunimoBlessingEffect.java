package tequila.cat.pigpig.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 祝尼魔的祝福：破坏作物时额外收获、手持镰刀范围收割、拾取范围扩大
 * （逻辑在 JunimoBlessingEvents）。
 */
public class JunimoBlessingEffect extends MobEffect {
    public static final int COLOR = 0x0077C043;

    public JunimoBlessingEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }
}
