package com.tequilacat.kaleidoscope_startale.util;

import com.tequilacat.kaleidoscope_startale.config.StartaleConfig;
import com.tequilacat.kaleidoscope_startale.init.KSEntityTags;
import com.tequilacat.kaleidoscope_startale.init.KSItemTags;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 饲料注册与匹配工具类。
 * <p>
 * 基于物品标签与实体标签构成的分组判断饲料归属：
 * 分组饲料只喂对应分组的生物，通用饲料则喂未分组的可接受生物。
 * 支持通过实体标签与配置项设置通用饲料黑名单。
 */
public class FeedRegistry {

    /**
     * 饲料分组，将饲料物品标签与其对应的生物实体标签配对。
     *
     * @param feedTag   饲料物品标签
     * @param entityTag 生物实体标签
     */
    private record FeedGroup(TagKey<Item> feedTag, TagKey<EntityType<?>> entityTag) {
    }

    /**
     * 内置饲料分组列表。
     */
    private static final List<FeedGroup> GROUPS = List.of(
            new FeedGroup(KSItemTags.LIVESTOCK_FEED, KSEntityTags.LIVESTOCK_FEED_MOBS),
            new FeedGroup(KSItemTags.POULTRY_FEED, KSEntityTags.POULTRY_FEED_MOBS));

    /** 通用饲料黑名单缓存。 */
    private static volatile Set<ResourceLocation> blacklistCache;

    /**
     * 使黑名单缓存失效。
     * <p>
     * 配置变更后调用，下次查询时重新解析黑名单。
     */
    public static void invalidateBlacklistCache() {
        blacklistCache = null;
    }

    /** 标签代次，标签每次重新绑定后自增，供依赖标签的缓存判断是否失效。 */
    private static volatile int tagGeneration;

    /**
     * 获取当前标签代次。
     *
     * @return 标签代次
     */
    public static int tagGeneration() {
        return tagGeneration;
    }

    /**
     * 标记标签已重新绑定。
     * <p>
     * 数据包重载后调用，持有标签快照的 AI 据此重新读取饲料集合。
     */
    public static void onTagsUpdated() {
        tagGeneration++;
    }

    /**
     * 获取配置项中的通用饲料黑名单。
     * <p>
     * 从配置项解析实体 ID，解析成功的加入集合；结果会被缓存。
     *
     * @return 黑名单实体 ID 集合
     */
    private static Set<ResourceLocation> blacklist() {
        Set<ResourceLocation> cached = blacklistCache;
        if (cached != null) {
            return cached;
        }
        Set<ResourceLocation> set = new HashSet<>();
        for (String raw : StartaleConfig.universalFeedBlacklist()) {
            ResourceLocation rl = ResourceLocation.tryParse(raw);
            if (rl != null) {
                set.add(rl);
            }
        }
        Set<ResourceLocation> frozen = Set.copyOf(set);
        blacklistCache = frozen;
        return frozen;
    }

    /**
     * 判断实体类型是否被通用饲料拉黑。
     * <p>
     * 实体带有黑名单标签或位于配置黑名单中即视为拉黑。
     *
     * @param type 实体类型
     * @return 是否被拉黑
     */
    private static boolean isBlacklisted(EntityType<?> type) {
        if (type.is(KSEntityTags.UNIVERSAL_FEED_BLACKLIST)) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return blacklist().contains(key);
    }

    /**
     * 判断实体类型是否属于任一分组。
     *
     * @param type 实体类型
     * @return 是否属于分组
     */
    private static boolean isGrouped(EntityType<?> type) {
        for (FeedGroup group : GROUPS) {
            if (type.is(group.entityTag())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取物品标签下的所有物品。
     *
     * @param tag 物品标签
     * @return 物品集合，标签不存在时返回空集合
     */
    private static Set<Item> itemsIn(TagKey<Item> tag) {
        Optional<HolderSet.Named<Item>> holders = BuiltInRegistries.ITEM.getTag(tag);
        if (holders.isEmpty()) {
            return Set.of();
        }
        Set<Item> items = new HashSet<>();
        holders.get().forEach(holder -> items.add(holder.value()));
        return Set.copyOf(items);
    }

    /**
     * 判断物品堆叠是否为通用饲料。
     *
     * @param stack 物品堆叠
     * @return 是否为通用饲料
     */
    public static boolean isUniversalFeed(ItemStack stack) {
        return stack.is(KSItemTags.UNIVERSAL_FEED);
    }

    /**
     * 判断物品是否为通用饲料。
     *
     * @param item 物品
     * @return 是否为通用饲料
     */
    public static boolean isUniversalFeed(Item item) {
        return new ItemStack(item).is(KSItemTags.UNIVERSAL_FEED);
    }

    /**
     * 判断实体是否接受通用饲料。
     * <p>
     * 仅动物类且非敌对生物可接受；原版可驯服动物不接受通用饲料。
     *
     * @param entity 目标实体
     * @param key    实体类型 ID
     * @return 是否接受通用饲料
     */
    private static boolean acceptsDefaultFeed(Entity entity, ResourceLocation key) {
        if (!(entity instanceof Animal)) {
            return false;
        }
        if (entity instanceof Enemy) {
            return false;
        }
        return !(entity instanceof TamableAnimal) || !isVanilla(key);
    }

    /**
     * 判断实体类型 ID 是否属于原版命名空间。
     *
     * @param key 实体类型 ID
     * @return 是否属于原版
     */
    private static boolean isVanilla(ResourceLocation key) {
        return key != null && "minecraft".equals(key.getNamespace());
    }

    /**
     * 判断物品堆叠能否喂给目标实体。
     * <p>
     * 先排除黑名单；分组饲料只喂对应分组生物，喂错分组直接拒绝且不回退；
     * 否则只有通用饲料且生物未分组并接受通用饲料时才可喂。
     *
     * @param stack  物品堆叠
     * @param entity 目标实体
     * @return 是否可喂
     */
    public static boolean canFeed(ItemStack stack, Entity entity) {
        if (stack.isEmpty()) {
            return false;
        }
        EntityType<?> type = entity.getType();
        if (isBlacklisted(type)) {
            return false;
        }

        boolean matchedGroupFeed = false;
        for (FeedGroup group : GROUPS) {
            if (!stack.is(group.feedTag())) {
                continue;
            }
            matchedGroupFeed = true;
            if (type.is(group.entityTag())) {
                return true;
            }
        }
        // 是分组饲料但不属于对应分组：不接受（也不会退回到通用饲料）
        if (matchedGroupFeed) {
            return false;
        }

        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return isUniversalFeed(stack) && !isGrouped(type) && acceptsDefaultFeed(entity, key);
    }

    /**
     * 判断物品能否喂给目标实体。
     *
     * @param item   物品
     * @param entity 目标实体
     * @return 是否可喂
     */
    public static boolean canFeed(Item item, Entity entity) {
        return canFeed(new ItemStack(item), entity);
    }

    /**
     * 获取实体可接受的饲料集合。
     * <p>
     * 黑名单实体返回空集合；属于分组的实体返回该分组饲料标签下的物品；
     * 否则在接受通用饲料时返回通用饲料标签下的物品。
     *
     * @param entity 目标实体
     * @return 可接受的饲料集合
     */
    public static Set<Item> feedsFor(Entity entity) {
        EntityType<?> type = entity.getType();
        if (isBlacklisted(type)) {
            return Set.of();
        }
        for (FeedGroup group : GROUPS) {
            if (type.is(group.entityTag())) {
                return itemsIn(group.feedTag());
            }
        }
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return acceptsDefaultFeed(entity, key) ? itemsIn(KSItemTags.UNIVERSAL_FEED) : Set.of();
    }
}