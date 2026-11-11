package com.tequilacat.kaleidoscope_startale.data;

import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.tequilacat.kaleidoscope_startale.init.KSBlocks;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.HashSet;
import java.util.Set;

public class ModBlockLootTablesProvider extends BlockLootSubProvider {

    /** 已登记战利品表的方块。 */
    private final Set<Block> knownBlocks = new HashSet<>();

    public ModBlockLootTablesProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {

        // ==================== 作物 ====================
        /* 大黄 */
        LootItemCondition.Builder mature = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(KSBlocks.RHUBARB_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(CropBlock.AGE, 5));
        add(KSBlocks.RHUBARB_CROP.get(), LootTable.lootTable()
                .apply(ApplyExplosionDecay.explosionDecay())
                // 池 1：总是掉落 1 个种子
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(KSItems.RHUBARB_SEEDS.get())))
                // 池 2：成熟时 50% 再掉 1 个种子
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(mature)
                        .add(LootItem.lootTableItem(KSItems.RHUBARB_SEEDS.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.5f))))
                // 池 3：成熟时掉 1 个大黄
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(mature)
                        .add(LootItem.lootTableItem(KSItems.RHUBARB.get())))
                // 池 4：成熟 + 夜晚(13000~23000) 时 10% 掉附魔夜行者
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(mature)
                        .when(TimeCheck.time(IntRange.range(13000, 23000)).setPeriod(24000L))
                        .add(LootItem.lootTableItem(KSItems.ENCHANTED_NIGHTCRAWLER.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.1f)))));

        /* 苋菜 */
        LootItemCondition.Builder amaranthMature = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(KSBlocks.AMARANTH_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties()
                        .hasProperty(CropBlock.AGE, 5));
        add(KSBlocks.AMARANTH_CROP.get(), LootTable.lootTable()
                // 顶层 explosion_decay
                .apply(ApplyExplosionDecay.explosionDecay())

                // 池 1：成熟时掉 1~3 个种子，否则固定掉 1 个
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(KSItems.AMARANTH_SEEDS.get())
                                .when(amaranthMature)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                                .otherwise(LootItem.lootTableItem(KSItems.AMARANTH_SEEDS.get()))))
                // 池 2：成熟时掉 1 个苋菜
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(amaranthMature)
                        .add(LootItem.lootTableItem(KSItems.AMARANTH.get())))
                // 池 3：成熟 + 夜晚(13000~23000) 时 10% 掉附魔夜行者
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(amaranthMature)
                        .when(TimeCheck.time(IntRange.range(13000, 23000)).setPeriod(24000L))
                        .add(LootItem.lootTableItem(KSItems.ENCHANTED_NIGHTCRAWLER.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.1f)))));

        /* 上古水果：需要长在上古水果藤（熟）下方才能存活，剪刀采集。 */
        add(KSBlocks.ANCIENT_FRUIT_CROP.get(), this.createItemWithCountTable(KSItems.ANCIENT_FRUIT.get(), UniformGenerator.between(1, 2)));
        /* 上古水果藤：可从藤架向上 / 向水平蔓延，并在下方结果。 */
        add(KSBlocks.ANCIENT_FRUIT_VINE.get(), this.createMultiItemTable(ModItems.TRELLIS.get(), KSItems.ANCIENT_FRUIT.get()));


        // ==================== 工具 ====================
        dropSelf(KSBlocks.COLD_STOVE.get());
        dropSelf(KSBlocks.CREAM_OIL.get());

    }

    /**
     * 添加方块及其战利品表到注册器
     * <p>
     * 将方块添加到已知方块集合中，以便后续追踪和管理
     * 然后调用父类方法完成战利品表的实际注册
     *
     * @param block   要注册的方块
     * @param builder 战利品表构建器
     */
    @Override
    public void add(Block block, LootTable.Builder builder) {
        // 记录已知的方块
        this.knownBlocks.add(block);
        super.add(block, builder);
    }

    @Override
    public Iterable<Block> getKnownBlocks() {
        return this.knownBlocks;
    }

    /**
     * 创建包含多个物品的战利品表构建器
     * <p>
     * 为每个物品创建一个独立的战利品池，每个池固定掉落 1 个物品
     * 自动应用爆炸破坏条件（被爆炸破坏时不掉落）
     *
     * @param items 要掉落的物品列表（可变参数）
     * @return 包含所有物品池的战利品表构建器
     */
    public LootTable.Builder createMultiItemTable(ItemLike... items) {
        LootTable.Builder builder = LootTable.lootTable();

        // 为每个物品创建独立的战利品池
        for (ItemLike item : items) {
            LootPool.Builder pool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(item));
            builder.withPool(this.applyExplosionCondition(item, pool));
        }
        return builder;
    }

    /**
     * 创建带有自定义数量掉落物品的战利品表构建器
     * <p>
     * 创建一个固定 1 次投掷的战利品池，使用提供的数量生成器决定掉落数量
     * 自动应用爆炸破坏条件（被爆炸破坏时不掉落）
     *
     * @param item          要掉落的物品
     * @param countProvider 数量生成器（用于控制掉落数量范围）
     * @return 包含该物品的战利品表构建器
     */
    protected LootTable.Builder createItemWithCountTable(ItemLike item, NumberProvider countProvider) {
        LootTable.Builder builder = LootTable.lootTable();

        // 创建战利品池并设置数量函数
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .apply(SetItemCountFunction.setCount(countProvider))
                .add(LootItem.lootTableItem(item));
        builder.withPool(this.applyExplosionCondition(item, pool));
        return builder;
    }

    /**
     * 为结果藤蔓方块添加战利品表
     * <p>
     * 仅当藤蔓的指定布尔属性为 true（已结果）时才掉落浆果物品
     * 用于迷梦果藤等需要条件掉落的藤蔓植物
     *
     * @param block      藤蔓方块
     * @param berryItem  浆果物品（掉落物）
     * @param property   判断是否结果的布尔属性（如 HAS_FRUIT）
     */
    private void vinesWithBerries(Block block, ItemLike berryItem, BooleanProperty property) {
        this.add(block, blockState ->
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, true)))
                                .add(LootItem.lootTableItem(berryItem)))
        );
    }
}
