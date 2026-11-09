package tequila.cat.pigpig.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.config.StartaleConfig;
import tequila.cat.pigpig.init.ModItems;

/**
 * 饲料 ↔ 生物 映射表。
 * 通用饲料（universal_feed）默认只对可繁殖的 Animal 生效，敌对生物与原版可驯服生物由
 * {@link #acceptsDefaultFeed} 直接排除；黑名单可再按实体 ID 精确排除（如女仆模组的实体）。
 * 显式列出的饲料目标（livestock_feed / poultry_feed 等）不受上面两条默认规则限制。
 */
public final class FeedRegistry {
    private static final List<String> FEED_TARGETS = List.of(
            "kaleidoscope_startale:livestock_feed=minecraft:cow,minecraft:mooshroom,minecraft:pig,minecraft:sheep,minecraft:goat,minecraft:sniffer",
            "kaleidoscope_startale:poultry_feed=minecraft:armadillo,minecraft:rabbit,minecraft:chicken");

    private static boolean built;
    private static Map<Item, Set<EntityType<?>>> byFeed = Map.of();
    private static Map<ResourceLocation, Set<Item>> byEntity = Map.of();
    private static Set<ResourceLocation> listedEntities = Set.of();
    private static volatile Set<ResourceLocation> blacklistCache;

    public static void invalidateBlacklistCache() {
        blacklistCache = null;
    }

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

    private static boolean isBlacklisted(ResourceLocation key) {
        return key != null && blacklist().contains(key);
    }

    private static void buildIfNeeded() {
        if (built) {
            return;
        }
        built = true;

        Map<Item, Set<EntityType<?>>> feedMap = new HashMap<>();
        Map<ResourceLocation, Set<Item>> entityMap = new HashMap<>();
        Set<ResourceLocation> listed = new HashSet<>();

        for (String entry : FEED_TARGETS) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            int eq = entry.indexOf('=');
            if (eq <= 0 || eq == entry.length() - 1) {
                KaleidoscopeStartale.LOGGER.warn("[feedTargets] 格式错误，应为 <饲料物品ID>=<生物实体ID>,... ：{}", entry);
                continue;
            }

            Item feed = resolveItem(entry.substring(0, eq).trim());
            if (feed == null) {
                continue;
            }
            for (String part : entry.substring(eq + 1).split(",")) {
                ResourceLocation typeId = resolveEntityId(part.trim());
                if (typeId == null) {
                    continue;
                }
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(typeId);
                feedMap.computeIfAbsent(feed, k -> new HashSet<>()).add(type);
                entityMap.computeIfAbsent(typeId, k -> new HashSet<>()).add(feed);
                listed.add(typeId);
            }
        }

        byFeed = Map.copyOf(feedMap);
        byEntity = Map.copyOf(entityMap);
        listedEntities = Set.copyOf(listed);
    }

    private static Item resolveItem(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id.toLowerCase(Locale.ROOT));
        if (rl != null && BuiltInRegistries.ITEM.containsKey(rl)) {
            return BuiltInRegistries.ITEM.get(rl);
        }
        KaleidoscopeStartale.LOGGER.warn("[feedTargets] 未知的物品 ID，已忽略：{}", id);
        return null;
    }

    private static ResourceLocation resolveEntityId(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id.toLowerCase(Locale.ROOT));
        if (rl != null && BuiltInRegistries.ENTITY_TYPE.containsKey(rl)) {
            return rl;
        }
        KaleidoscopeStartale.LOGGER.warn("[feedTargets] 未知的实体 ID，已忽略：{}", id);
        return null;
    }

    private static Item defaultFeed() {
        return ModItems.UNIVERSAL_FEED.get();
    }

    /** 是否是通用饲料（对所有可繁殖 Animal 都生效的那种）。 */
    public static boolean isUniversalFeed(Item item) {
        return item == defaultFeed();
    }

    /**
     * 通用饲料的默认适用范围：可繁殖的 Animal。
     * 两类生物不参与，避免和别的东西抢活：
     *  - Enemy（Hoglin、Zoglin 等）：敌对生物不该被喂食、也不该被饲料吸引；
     *  - 原版自带驯养体系的 TamableAnimal（狼、猫、鹦鹉）：驯服交回原版的骨头/鱼，
     *    否则通用饲料会把它记成本模组驯养，和原版状态对不上。
     * 第三方模组继承 TamableAnimal 的生物不在此列，仍按 TamingCompat 的 owner 判定处理。
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

    private static boolean isVanilla(ResourceLocation key) {
        return key != null && "minecraft".equals(key.getNamespace());
    }

    /** 该物品能否喂该生物 */
    public static boolean canFeed(Item item, Entity entity) {
        buildIfNeeded();
        EntityType<?> type = entity.getType();
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (isBlacklisted(key)) {
            return false;
        }
        if (key != null && listedEntities.contains(key)) {
            Set<EntityType<?>> set = byFeed.get(item);
            return set != null && set.contains(type);
        }
        return item == defaultFeed() && acceptsDefaultFeed(entity, key);
    }

    /** 该生物可接受的饲料（用于挂载饲料引诱 AI） */
    public static Set<Item> feedsFor(Entity entity) {
        buildIfNeeded();
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (isBlacklisted(key)) {
            return Set.of();
        }
        if (key != null && listedEntities.contains(key)) {
            Set<Item> set = byEntity.get(key);
            return set == null ? Set.of() : set;
        }
        return acceptsDefaultFeed(entity, key) ? Set.of(defaultFeed()) : Set.of();
    }

    private FeedRegistry() {
    }
}
