package com.tequilacat.kaleidoscope_startale.event;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.config.KSClientConfig;
import com.tequilacat.kaleidoscope_startale.init.KSDataComponents;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

/**
 * 附魔强化事件类。
 * <p>
 * 处理铁砧上的附魔强化：用附魔夜行者随机提升左侧物品一个附魔的等级，
 * 或把强化过的附魔书合并到物品上并保留超出原版上限的等级；
 * 强化次数记录在数据组件中并有总量上限。
 * 已强化附魔的名称染成紫色。
 */
@EventBusSubscriber(modid = KaleidoscopeStartale.MODID)
public final class EnchantUpgradeEvents {

    /** 同一件物品最多可以强化的次数。 */
    public static final int MAX_UPGRADES = 1;
    /** 强化一次消耗的经验等级基础值。 */
    private static final long BASE_COST = 5L;
    /** 每强化过一次后额外增加的经验等级。 */
    private static final long COST_PER_UPGRADE = 5L;

    private EnchantUpgradeEvents() {
    }

    /**
     * 铁砧合成：附魔夜行者强化附魔，强化过的附魔书给装备附魔时保留超出上限的等级。
     *
     * @param event 铁砧更新事件
     */
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (left.isEmpty() || right.isEmpty()) {
            return;
        }
        if (right.is(KSItems.ENCHANTED_NIGHTCRAWLER.get())) {
            upgradeWithNightcrawler(event, left);
            return;
        }
        if (right.is(Items.ENCHANTED_BOOK)) {
            applyUpgradedBook(event, left, right);
        }
    }

    /**
     * 用附魔夜行者随机把左侧物品上的一个附魔等级 +1。
     * <p>
     * 已达到强化上限或没有可提升附魔时不做处理；
     * 随机源以物品本身与已用次数为种子，保证预览与实际结果一致。
     *
     * @param event 铁砧更新事件
     * @param left  左侧物品
     */
    private static void upgradeWithNightcrawler(AnvilUpdateEvent event, ItemStack left) {
        ItemEnchantments upgrades = upgradesOf(left);
        int used = totalUpgrades(upgrades);
        if (used >= MAX_UPGRADES) {
            return;
        }

        ItemEnchantments current = enchantmentsOf(left);
        if (current.isEmpty()) {
            return;
        }

        // 每次只随机提升其中一个附魔
        List<Holder<Enchantment>> candidates = new ArrayList<>();
        for (Holder<Enchantment> holder : current.keySet()) {
            if (current.getLevel(holder) < holder.value().getMaxLevel() + MAX_UPGRADES) {
                candidates.add(holder);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        // 用物品本身与已用次数做种子，保证两端算出的结果一致，避免预览与实际产出不同
        RandomSource random = RandomSource.create(Objects.hash(ItemStack.hashItemAndComponents(left), used));
        Holder<Enchantment> chosen = candidates.get(random.nextInt(candidates.size()));

        ItemEnchantments.Mutable upgraded = new ItemEnchantments.Mutable(current);
        upgraded.set(chosen, current.getLevel(chosen) + 1);
        ItemEnchantments.Mutable counters = new ItemEnchantments.Mutable(upgrades);
        counters.set(chosen, upgrades.getLevel(chosen) + 1);

        ItemStack output = left.copy();
        setEnchantments(output, upgraded.toImmutable());
        output.set(KSDataComponents.ENCHANT_UPGRADES.get(), counters.toImmutable());

        event.setOutput(output);
        event.setCost(BASE_COST + used * COST_PER_UPGRADE);
        event.setMaterialCost(1);
    }

    /**
     * 把强化过的附魔书合并到左侧物品上，保留超过原版上限的等级。
     * <p>
     * 只提升书上比物品更高的附魔等级；强化次数取两者较大值，
     * 使之后的强化总量上限保持一致。
     *
     * @param event 铁砧更新事件
     * @param left  左侧物品
     * @param book  右侧的附魔书
     */
    private static void applyUpgradedBook(AnvilUpdateEvent event, ItemStack left, ItemStack book) {
        ItemEnchantments marks = upgradesOf(book);
        if (marks.isEmpty() || !EnchantmentHelper.canStoreEnchantments(left)) {
            return;
        }

        ItemEnchantments bookEnchantments = enchantmentsOf(book);
        ItemEnchantments current = enchantmentsOf(left);
        ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(current);
        int addedLevels = 0;
        for (Holder<Enchantment> holder : bookEnchantments.keySet()) {
            int bookLevel = bookEnchantments.getLevel(holder);
            int currentLevel = current.getLevel(holder);
            if (bookLevel <= currentLevel) {
                continue;
            }
            merged.set(holder, bookLevel);
            addedLevels += bookLevel - currentLevel;
        }
        if (addedLevels <= 0) {
            return;
        }

        ItemStack output = left.copy();
        setEnchantments(output, merged.toImmutable());
        // 强化次数取两者较大值，让之后的强化总量上限保持一致
        ItemEnchantments.Mutable counters = new ItemEnchantments.Mutable(upgradesOf(left));
        for (Holder<Enchantment> holder : marks.keySet()) {
            counters.set(holder, Math.max(counters.getLevel(holder), marks.getLevel(holder)));
        }
        output.set(KSDataComponents.ENCHANT_UPGRADES.get(), counters.toImmutable());

        event.setOutput(output);
        event.setCost(BASE_COST + addedLevels * 2L);
        event.setMaterialCost(1);
    }

    /**
     * 物品提示：把升级过的附魔名整行显示为紫色。
     *
     * @param event 物品提示事件
     */
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!KSClientConfig.SPEC.isLoaded() || !KSClientConfig.COLOR_UPGRADED_ENCHANT_NAME.get()) {
            return;
        }
        ItemEnchantments upgrades = upgradesOf(event.getItemStack());
        if (upgrades.isEmpty()) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        for (Holder<Enchantment> holder : upgrades.keySet()) {
            if (upgrades.getLevel(holder) > 0) {
                colorizeName(tooltip, holder.value().description());
            }
        }
    }

    /**
     * 把提示里该附魔所在的整行（含等级）改为紫色。
     *
     * @param tooltip 提示行列表
     * @param name    附魔名称
     */
    private static void colorizeName(List<Component> tooltip, Component name) {
        String plainName = name.getString();
        for (int i = 0; i < tooltip.size(); i++) {
            String line = tooltip.get(i).getString();
            if (!line.startsWith(plainName)) {
                continue;
            }
            String suffix = line.substring(plainName.length());
            MutableComponent rebuilt = Component.empty()
                    .append(name.copy())
                    .append(Component.literal(suffix))
                    .withStyle(ChatFormatting.LIGHT_PURPLE);
            tooltip.set(i, rebuilt);
        }
    }

    /**
     * 读取物品上的强化次数表。
     *
     * @param stack 物品
     * @return 强化次数表，没有则为空表
     */
    private static ItemEnchantments upgradesOf(ItemStack stack) {
        return stack.getOrDefault(KSDataComponents.ENCHANT_UPGRADES.get(), ItemEnchantments.EMPTY);
    }

    /**
     * 统计一件物品上所有附魔被强化的总次数。
     *
     * @param upgrades 强化次数表
     * @return 总次数
     */
    private static int totalUpgrades(ItemEnchantments upgrades) {
        int total = 0;
        for (Holder<Enchantment> holder : upgrades.keySet()) {
            total += upgrades.getLevel(holder);
        }
        return total;
    }

    /**
     * 读取物品上的附魔表，附魔书读取存储附魔，其余物品读取普通附魔。
     *
     * @param stack 物品
     * @return 附魔表
     */
    private static ItemEnchantments enchantmentsOf(ItemStack stack) {
        if (stack.is(Items.ENCHANTED_BOOK)) {
            return stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    /**
     * 写回物品上的附魔表，附魔书写入存储附魔，其余物品写入普通附魔。
     *
     * @param stack        物品
     * @param enchantments 附魔表
     */
    private static void setEnchantments(ItemStack stack, ItemEnchantments enchantments) {
        if (stack.is(Items.ENCHANTED_BOOK)) {
            stack.set(DataComponents.STORED_ENCHANTMENTS, enchantments);
        } else {
            stack.set(DataComponents.ENCHANTMENTS, enchantments);
        }
    }
}