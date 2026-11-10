package com.tequilacat.kaleidoscope_startale.data.loot;

import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.function.BiConsumer;

/**
 * 模组钓鱼掉落表数据生成器。
 * <p>
 * 覆盖原版钓鱼的鱼类掉落表，在原有鱼种基础上追加金鱼与 Joja 可乐，
 * 并调整各类物品的权重。
 */
public class ModFishingLootTablesProvider implements LootTableSubProvider {

    /**
     * 生成钓鱼掉落表。
     * <p>
     * 以单次抽取生成钓鱼鱼类表：鳕鱼 60、鲑鱼 25、热带鱼 2、河豚 13，
     * 另追加金鱼 6 与 Joja 可乐 6。
     *
     * @param output 掉落表输出消费者
     */
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(ResourceKey.create(Registries.LOOT_TABLE,
                        ResourceLocation.withDefaultNamespace("gameplay/fishing/fish")),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(Items.COD).setWeight(60))
                                .add(LootItem.lootTableItem(Items.SALMON).setWeight(25))
                                .add(LootItem.lootTableItem(Items.TROPICAL_FISH).setWeight(2))
                                .add(LootItem.lootTableItem(Items.PUFFERFISH).setWeight(13))
                                .add(LootItem.lootTableItem(KSItems.GOLDFISH.get()).setWeight(6))
                                .add(LootItem.lootTableItem(KSItems.JOJA_COLA.get()).setWeight(6))));
    }
}