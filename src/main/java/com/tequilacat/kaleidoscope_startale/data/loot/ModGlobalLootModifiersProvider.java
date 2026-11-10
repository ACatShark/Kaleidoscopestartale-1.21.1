package com.tequilacat.kaleidoscope_startale.data.loot;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 全局掉落修饰器数据生成器。
 * <p>
 * 生成全局掉落修饰器：破坏短草时额外掉落一次草帽种子表，
 * 通过掉落表 ID 条件限定仅对原版短草掉落生效。
 */
public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {

    /**
     * 构造全局掉落修饰器生成器。
     *
     * @param output     数据包输出
     * @param registries 注册表提供者
     */
    public ModGlobalLootModifiersProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, KaleidoscopeStartale.MODID);
    }

    /**
     * 生成全局掉落修饰器条目。
     * <p>
     * 破坏短草时，额外掉落一次草帽种子表。
     */
    @Override
    protected void start() {
        // 破坏短草时，额外掉落一次草帽种子表
        this.add("straw_hat_seed_drop", new AddTableLootModifier(
                new LootItemCondition[]{
                        LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace("blocks/short_grass")).build()
                },
                ResourceKey.create(Registries.LOOT_TABLE, KaleidoscopeStartale.id("straw_hat_seed_drop"))
        ), List.of());
    }
}