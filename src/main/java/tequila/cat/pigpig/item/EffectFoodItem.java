package tequila.cat.pigpig.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * 带药水效果的普通食物：把必定生效的那部分效果按药水格式列出，
 * 概率触发的效果则以「效果名（概率%）」单列一行。
 * 每个效果下面再补一行说明，内容取自 effect.<命名空间>.<id>.desc，没有翻译就不显示。
 */
public class EffectFoodItem extends Item {
    private static final String DESC_SUFFIX = ".desc";

    public EffectFoodItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null || food.effects().isEmpty()) {
            return;
        }

        List<MobEffectInstance> guaranteed = food.effects().stream()
                .filter(effect -> effect.probability() >= 1.0F)
                .map(FoodProperties.PossibleEffect::effect)
                .toList();
        if (!guaranteed.isEmpty()) {
            PotionContents.addPotionTooltip(guaranteed, tooltipComponents::add, 1.0F, context.tickRate());
            guaranteed.forEach(instance -> addDescription(tooltipComponents, instance.getEffect().value()));
        }

        for (FoodProperties.PossibleEffect effect : food.effects()) {
            if (effect.probability() >= 1.0F) {
                continue;
            }
            MobEffectInstance instance = effect.effect();
            tooltipComponents.add(Component.translatable("item.kaleidoscope_startale.effect_chance",
                    instance.getEffect().value().getDisplayName(),
                    Math.round(effect.probability() * 100.0F)).withStyle(ChatFormatting.GRAY));
            addDescription(tooltipComponents, instance.getEffect().value());
        }
    }

    /** 效果有翻译好的说明时补一行灰字；翻译缺失时 getString() 会退回键名，直接跳过。 */
    private static void addDescription(List<Component> tooltipComponents, MobEffect effect) {
        String key = effect.getDescriptionId() + DESC_SUFFIX;
        MutableComponent description = Component.translatable(key);
        if (!key.equals(description.getString())) {
            tooltipComponents.add(Component.literal("  ").append(description).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
