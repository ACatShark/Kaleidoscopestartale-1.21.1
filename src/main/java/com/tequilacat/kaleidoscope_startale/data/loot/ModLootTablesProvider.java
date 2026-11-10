package com.tequilacat.kaleidoscope_startale.data.loot;

import com.github.ysbbbbbb.kaleidoscopecookery.loot.AdvanceBlockMatchTool;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.function.BiConsumer;

/**
 * 模组掉落表数据生成器。
 * <p>
 * 生成草帽种子掉落表：在破坏短草且生物头戴草帽时，
 * 按概率掉落大黄种子与苋菜种子，并受时运加成。
 */
public class ModLootTablesProvider implements LootTableSubProvider {

    /**
     * 草帽物品标签。
     */
    private static final TagKey<Item> STRAW_HAT = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "straw_hat"));

    /** 注册表提供者。 */
    private final HolderLookup.Provider registries;

    /**
     * 构造掉落表生成器。
     *
     * @param registries 注册表提供者
     */
    public ModLootTablesProvider(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    /**
     * 生成掉落表。
     * <p>
     * 生成草帽种子掉落表：单次抽取，分别以 12.5% 概率掉落大黄种子与苋菜种子，
     * 均要求头戴草帽，并受时运加成（每级额外 +2 次机会）。
     *
     * @param output 掉落表输出消费者
     */
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        Holder<Enchantment> fortune = this.registries.lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.FORTUNE);
        // 只有头上戴着草帽的生物才会掉落（厨师模组的自定义战利品条件）
        LootItemCondition.Builder wearingStrawHat = AdvanceBlockMatchTool.toolMatches(
                EquipmentSlot.HEAD,
                ItemPredicate.Builder.item().of(STRAW_HAT).build());

        output.accept(ResourceKey.create(Registries.LOOT_TABLE, KaleidoscopeStartale.id("straw_hat_seed_drop")),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                // 大黄种子：12.5% 概率，受时运加成（每级额外 +2 次机会）
                                .add(LootItem.lootTableItem(KSItems.RHUBARB_SEEDS.get())
                                        .when(LootItemRandomChanceCondition.randomChance(0.125F))
                                        .when(wearingStrawHat)
                                        .apply(ApplyBonusCount.addUniformBonusCount(fortune, 2)))
                                // 苋菜种子：同上
                                .add(LootItem.lootTableItem(KSItems.AMARANTH_SEEDS.get())
                                        .when(LootItemRandomChanceCondition.randomChance(0.125F))
                                        .when(wearingStrawHat)
                                        .apply(ApplyBonusCount.addUniformBonusCount(fortune, 2)))));
    }
}