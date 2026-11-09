package tequila.cat.pigpig.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import tequila.cat.pigpig.KaleidoscopeStartale;

/**
 * 嗜血：造成伤害后回复相当于该伤害 80% 的生命（逻辑在 BloodthirstEvents），
 * 期间无法自然回血（逻辑在 FoodDataMixin），攻击速度 +20%。
 */
public class BloodthirstEffect extends MobEffect {
    public static final int COLOR = 0x00B3232A;
    /** 攻击速度加成（按总攻速的百分比叠加）。 */
    public static final double ATTACK_SPEED_BONUS = 0.2D;

    private static final ResourceLocation ATTACK_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(KaleidoscopeStartale.MODID, "bloodthirst_attack_speed");

    public BloodthirstEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, ATTACK_SPEED_ID, ATTACK_SPEED_BONUS,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
