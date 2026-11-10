package com.tequilacat.kaleidoscope_startale.data.registries;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSItemTags;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 模组数据包注册表数据生成器。
 * <p>
 * 通过注册表构建器生成自定义数据包条目，目前用于重定义抢夺附魔：
 * 扩展可附魔物品标签，并为其添加对玩家攻击者生效的装备掉落概率加成效果。
 */
public class ModDatapackRegistriesProvider extends DatapackBuiltinEntriesProvider {

    /** 注册表构建器，注册附魔条目。 */
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.ENCHANTMENT, ModDatapackRegistriesProvider::bootstrapEnchantments);

    /**
     * 构造数据包注册表生成器。
     * <p>
     * 输出命名空间包含本模组与原版，以便覆盖原版附魔定义。
     *
     * @param output     数据包输出
     * @param registries 注册表提供者
     */
    public ModDatapackRegistriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(KaleidoscopeStartale.MODID, "minecraft"));
    }

    /**
     * 引导附魔注册。
     * <p>
     * 重新定义抢夺附魔：使用自定义可附魔物品标签、指定权重与等级消耗，
     * 并添加装备掉落概率加成效果（每级 +1%），仅对玩家攻击者生效。
     *
     * @param context 引导上下文
     */
    private static void bootstrapEnchantments(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);

        context.register(Enchantments.LOOTING, Enchantment.enchantment(Enchantment.definition(
                        // 可附魔物品
                        items.getOrThrow(KSItemTags.LOOTING_APPLICABLE),
                        // 权重
                        2,
                        // 最大等级
                        3,
                        // 最低等级消耗
                        Enchantment.dynamicCost(15, 9),
                        // 最高等级消耗
                        Enchantment.dynamicCost(65, 9),
                        // 铁砧消耗
                        4,
                        EquipmentSlotGroup.MAINHAND))
                .withEffect(EnchantmentEffectComponents.EQUIPMENT_DROPS,
                        EnchantmentTarget.ATTACKER,
                        EnchantmentTarget.VICTIM,
                        // 每级 +1% 装备掉落概率
                        new AddValue(new LevelBasedValue.Linear(0.01F, 0.01F)),
                        // 只对玩家生效
                        LootItemEntityPropertyCondition.hasProperties(
                                LootContext.EntityTarget.ATTACKER,
                                EntityPredicate.Builder.entity()
                                        .entityType(EntityTypePredicate.of(EntityType.PLAYER))))
                .build(Enchantments.LOOTING.location()));
    }
}