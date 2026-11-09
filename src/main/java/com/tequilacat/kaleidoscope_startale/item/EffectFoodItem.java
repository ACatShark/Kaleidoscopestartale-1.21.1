package com.tequilacat.kaleidoscope_startale.item;

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

import java.util.List;

/**
 * 效果食物物品类。
 * <p>
 * 在物品提示中展示食物附带的状态效果：
 * 必定生效的效果按药水提示格式显示，概率生效的效果显示触发概率，
 * 并在每个效果后追加其描述文本（若存在）。
 */
public class EffectFoodItem extends Item {

    /**
     * 效果描述文本键的后缀。
     */
    private static final String DESC_SUFFIX = ".desc";

    /**
     * 构造效果食物物品。
     *
     * @param properties 物品属性
     */
    public EffectFoodItem(Item.Properties properties) {
        super(properties);
    }

    /**
     * 追加物品提示文本。
     * <p>
     * 若物品具有食物属性且附带效果，则先列出必定生效的效果，
     * 再列出概率生效的效果及其触发概率，并为每个效果追加描述。
     *
     * @param stack             物品堆叠
     * @param context           提示上下文
     * @param tooltipComponents 提示组件列表
     * @param tooltipFlag       提示标志
     */
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
            tooltipComponents.add(Component.translatable("tooltip.kaleidoscope_startale.effect_chance",
                    instance.getEffect().value().getDisplayName(),
                    Math.round(effect.probability() * 100.0F)).withStyle(ChatFormatting.GRAY));
            addDescription(tooltipComponents, instance.getEffect().value());
        }
    }

    /**
     * 追加效果描述文本。
     * <p>
     * 根据效果描述键加后缀查找翻译文本，
     * 若翻译存在则以深灰色缩进形式添加到提示中。
     *
     * @param tooltipComponents 提示组件列表
     * @param effect            状态效果
     */
    private static void addDescription(List<Component> tooltipComponents, MobEffect effect) {
        String key = effect.getDescriptionId() + DESC_SUFFIX;
        MutableComponent description = Component.translatable(key);
        if (!key.equals(description.getString())) {
            tooltipComponents.add(Component.literal("  ").append(description).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}