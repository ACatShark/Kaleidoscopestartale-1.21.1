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
 * 分组饲料只喂对应分组的生物；通用饲料可喂给通用饲料标签内的生物，
 * 以及任意通过自身食物判定的动物。
 * 通用饲料黑名单由配置项提供，改完即时生效。
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
     * 判断实体类型是否被通用饲料拉黑。
     * <p>
     * ID 写进了配置黑名单即视为拉黑，配置项每次现读，
     * 保证改完配置无需重载数据。
     *
     * @param type 实体类型
     * @return 是否被拉黑
     */
    private static boolean isBlacklisted(EntityType<?> type) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        for (String raw : StartaleConfig.UNIVERSAL_FEED_BLACKLIST.get()) {
            if (key.equals(ResourceLocation.tryParse(raw))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断实体是否认可该物品为其自身食物。
     * <p>
     * 仅动物类且非敌对生物，并且动物自身认可该物品为食物时返回 true。
     *
     * @param entity 目标实体
     * @param stack  物品堆叠
     * @return 是否认可为食物
     */
    private static boolean acceptsByFood(Entity entity, ItemStack stack) {
        return entity instanceof Animal animal && !(entity instanceof Enemy) && animal.isFood(stack);
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
     * 判断物品堆叠能否喂给目标实体。
     * <p>
     * 先排除空物品与黑名单实体；分组饲料只喂对应分组的生物，
     * 喂错分组直接拒绝且不回退；否则需为通用饲料，
     * 且生物属于通用饲料标签或自身认可该食物。
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

        return isUniversalFeed(stack) && (type.is(KSEntityTags.UNIVERSAL_FEED_MOBS) || acceptsByFood(entity, stack));
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
     * 属于通用饲料标签的实体返回通用饲料标签下的物品；
     * 其余实体从通用饲料中筛选出自身认可的食物。
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
        if (type.is(KSEntityTags.UNIVERSAL_FEED_MOBS)) {
            return itemsIn(KSItemTags.UNIVERSAL_FEED);
        }
        Set<Item> accepted = new HashSet<>();
        for (Item item : itemsIn(KSItemTags.UNIVERSAL_FEED)) {
            if (acceptsByFood(entity, new ItemStack(item))) {
                accepted.add(item);
            }
        }
        return Set.copyOf(accepted);
    }
}