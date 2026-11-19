package com.tequilacat.kaleidoscope_startale.data.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tequilacat.kaleidoscope_startale.config.KSServerConfig;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * 箱子战利品修饰器。
 * <p>
 * 在符合条件的箱子战利品生成后，按配置概率额外添加一定数量的远古果实藤蔓；
 * 概率为 0 或未命中时保持原掉落不变。
 */
public class ChestLootModifier extends LootModifier {

    /**
     * 修饰器编解码器。
     * <p>
     * 仅包含通用战利品修饰器条件字段。
     */
    public static final MapCodec<ChestLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, ChestLootModifier::new));

    /**
     * 构造箱子战利品修饰器。
     *
     * @param conditions 生效条件
     */
    public ChestLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    /**
     * 应用战利品修饰。
     * <p>
     * 读取配置中的概率与数量：概率为 0 或随机未命中时不修改；
     * 数量大于 0 时向战利品追加对应数量的远古果实藤蔓。
     *
     * @param generatedLoot 已生成的战利品列表
     * @param context       战利品上下文
     * @return 修改后的战利品列表
     */
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        float chance = KSServerConfig.ANCIENT_FRUIT_VINE_CHEST_CHANCE.get().floatValue();
        if (chance <= 0.0F || context.getRandom().nextFloat() >= chance) {
            return generatedLoot;
        }

        int count = KSServerConfig.ANCIENT_FRUIT_VINE_CHEST_COUNT.get();
        if (count > 0) {
            generatedLoot.add(new ItemStack(KSItems.ANCIENT_FRUIT_VINE.get(), count));
        }
        return generatedLoot;
    }

    /**
     * 获取修饰器编解码器。
     *
     * @return 修饰器编解码器
     */
    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
