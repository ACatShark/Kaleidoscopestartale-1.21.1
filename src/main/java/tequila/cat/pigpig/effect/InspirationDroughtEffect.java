package tequila.cat.pigpig.effect;

import java.util.Set;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;

/** 灵感枯：只能用不死图腾解除，期间无法再获得灵感充沛。 */
public class InspirationDroughtEffect extends MobEffect {
    public static final int COLOR = 0x006B5F3B;

    private static final Set<EffectCure> ALLOWED_CURES = Set.of(EffectCures.PROTECTED_BY_TOTEM);

    public InspirationDroughtEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        cures.clear();
        cures.addAll(ALLOWED_CURES);
    }
}
