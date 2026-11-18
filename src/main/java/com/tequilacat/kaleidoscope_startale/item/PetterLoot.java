package com.tequilacat.kaleidoscope_startale.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 拍拍器掉落生成工具类。
 * <p>
 * 按生物类型登记自定义掉落生成器，未登记的生物使用其自身击杀掉落表。
 * 内置骆驼、猫、狼与嗅探兽的特殊掉落逻辑，
 * 并提供带玩家伤害来源与幸运值的实体掉落表滚取、礼物表与挖掘表滚取等方法。
 */
public final class PetterLoot {

    /** 骆驼掉落表的选项数量，均匀分布即每项 20%。 */
    private static final int CAMEL_GIFT_OPTIONS = 5;

    /**
     * 自定义掉落表生成器。
     */
    @FunctionalInterface
    public interface Roller {

        /**
         * 生成一次掉落。
         *
         * @param player 玩家
         * @param pet    被拍的生物
         * @param level  服务端世界
         * @return 掉落物列表
         */
        List<ItemStack> roll(Player player, LivingEntity pet, ServerLevel level);
    }

    /** 特殊生物的掉落生成器，按生物类型登记。 */
    private static final Map<EntityType<?>, Roller> SPECIAL = new LinkedHashMap<>();

    static {
        register(EntityType.CAMEL, (player, pet, level) -> randomCamelGift(level));
        register(EntityType.CAT, (player, pet, level) -> rollCatGift(pet, level));
        register(EntityType.WOLF, (player, pet, level) -> randomRawMeat(level));
        register(EntityType.SNIFFER, (player, pet, level) -> {
            List<ItemStack> drops = new ArrayList<>(rollEntityLoot(player, pet, level));
            drops.addAll(rollDiggingLoot(pet, level));
            return drops;
        });
    }

    /**
     * 登记某个生物的自定义掉落。
     *
     * @param type   生物类型
     * @param roller 掉落生成器
     */
    public static void register(EntityType<?> type, Roller roller) {
        SPECIAL.put(type, roller);
    }

    /**
     * 滚取一次掉落。
     * <p>
     * 有自定义掉落表的生物走自定义逻辑，其余生物使用自身击杀掉落表。
     *
     * @param player 玩家
     * @param pet    被拍的生物
     * @param level  服务端世界
     * @return 掉落物列表
     */
    public static List<ItemStack> roll(Player player, LivingEntity pet, ServerLevel level) {
        Roller roller = SPECIAL.get(pet.getType());
        if (roller != null) {
            return roller.roll(player, pet, level);
        }
        return rollEntityLoot(player, pet, level);
    }

    /**
     * 滚取实体的击杀掉落表。
     * <p>
     * 以玩家攻击作为伤害来源构造掉落参数，并计入玩家幸运值。
     *
     * @param player 玩家
     * @param pet    被拍的生物
     * @param level  服务端世界
     * @return 掉落物列表
     */
    private static List<ItemStack> rollEntityLoot(Player player, LivingEntity pet, ServerLevel level) {
        ResourceKey<LootTable> lootTable = pet.getLootTable();
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, pet)
                .withParameter(LootContextParams.ORIGIN, pet.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(player))
                .withParameter(LootContextParams.ATTACKING_ENTITY, player)
                .withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.ENTITY);
        return randomItems(level, lootTable, params);
    }

    /**
     * 猫的早晨礼物表。
     *
     * @param pet   被拍的猫
     * @param level 服务端世界
     * @return 掉落物列表
     */
    private static List<ItemStack> rollCatGift(LivingEntity pet, ServerLevel level) {
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, pet)
                .withParameter(LootContextParams.ORIGIN, pet.position())
                .create(LootContextParamSets.GIFT);
        return randomItems(level, BuiltInLootTables.CAT_MORNING_GIFT, params);
    }

    /**
     * 嗅探兽的挖掘掉落表。
     *
     * @param pet   被拍的生物
     * @param level 服务端世界
     * @return 掉落物列表
     */
    private static List<ItemStack> rollDiggingLoot(LivingEntity pet, ServerLevel level) {
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, pet)
                .withParameter(LootContextParams.ORIGIN, pet.position())
                .create(LootContextParamSets.GIFT);
        return randomItems(level, BuiltInLootTables.SNIFFER_DIGGING, params);
    }

    /**
     * 骆驼：发光地衣 / 矮草 / 蕨 / 仙人掌 / 随机药水，五项各 20%。
     *
     * @param level 服务端世界
     * @return 掉落物列表
     */
    private static List<ItemStack> randomCamelGift(ServerLevel level) {
        return switch (level.getRandom().nextInt(CAMEL_GIFT_OPTIONS)) {
            case 0 -> List.of(new ItemStack(Items.GLOW_LICHEN));
            case 1 -> List.of(new ItemStack(Items.SHORT_GRASS));
            case 2 -> List.of(new ItemStack(Items.FERN));
            case 3 -> List.of(new ItemStack(Items.CACTUS));
            default -> List.of(randomPotion(level));
        };
    }

    /**
     * 从药水注册表里随机挑一瓶。
     *
     * @param level 服务端世界
     * @return 药水物品堆叠
     */
    private static ItemStack randomPotion(ServerLevel level) {
        Optional<Holder.Reference<Potion>> pick = BuiltInRegistries.POTION.getRandom(level.getRandom());
        if (pick.isEmpty()) {
            return new ItemStack(Items.POTION);
        }

        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(pick.get()));
        return stack;
    }

    /**
     * 随机获取一种生肉。
     *
     * @param level 服务端世界
     * @return 掉落物列表
     */
    private static List<ItemStack> randomRawMeat(ServerLevel level) {
        Optional<Holder<Item>> pick = BuiltInRegistries.ITEM.getRandomElementOf(Tags.Items.FOODS_RAW_MEAT, level.getRandom());
        return pick.map(itemHolder -> List.of(new ItemStack(itemHolder.value()))).orElseGet(List::of);
    }

    /**
     * 从指定掉落表随机获取物品。
     *
     * @param level  服务端世界
     * @param key    掉落表键
     * @param params 掉落参数
     * @return 掉落物列表
     */
    private static List<ItemStack> randomItems(ServerLevel level, ResourceKey<LootTable> key, LootParams params) {
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
        return List.copyOf(table.getRandomItems(params));
    }
}