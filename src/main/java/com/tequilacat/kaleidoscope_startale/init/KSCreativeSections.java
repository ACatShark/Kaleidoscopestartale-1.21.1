package com.tequilacat.kaleidoscope_startale.init;

import com.github.ysbbbbbb.kaleidoscopecookery.init.registry.TeacupRegistry;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.block.CreamOilBlock;
import com.tequilacat.kaleidoscope_startale.blockentity.CreamOilBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 模组创造模式物品栏分区定义类。
 * <p>
 * 定义本模组物品栏的各分区，
 * 每个分区包含名称、图标与物品填充逻辑；
 * 维护当前激活的分区集合，未激活分区的物品仅进入搜索结果而不显示在物品栏中。
 */
public final class KSCreativeSections {

    /**
     * 分区定义。
     *
     * @param key     名称本地化键后缀
     * @param icon    分区图标
     * @param content 分区物品填充逻辑
     */
    public record Section(String key, Supplier<ItemStack> icon, Consumer<CreativeModeTab.Output> content) {

        /**
         * 取得分区名称。
         *
         * @return 本地化名称
         */
        public Component name() {
            return Component.translatable("creative_tab." + KaleidoscopeStartale.MODID + "." + this.key);
        }
    }

    /**
     * 全部分区定义，顺序即按钮与物品显示顺序。
     */
    private static final List<Section> SECTIONS = List.of(
            /* 材料分区 */
            new Section("materials", () -> new ItemStack(KSItems.ANCIENT_FRUIT_JUICE_BUCKET.get()), output -> {
                output.accept(KSItems.ANCIENT_FRUIT_JUICE_BUCKET.get());
                output.accept(KSItems.LIVESTOCK_FEED.get());
                output.accept(KSItems.POULTRY_FEED.get());
                output.accept(KSItems.UNIVERSAL_FEED.get());
                output.accept(KSItems.MATCHA_POWDER.get());
                output.accept(KSItems.CREAM.get());
                output.accept(KSItems.GREEN_TEA_LEAVES);
                output.accept(KSItems.ROSE_TEA_BAG);
            }),
            /* 工具分区 */
            new Section("tools", () -> new ItemStack(KSItems.PETTER.get()), output -> {
                output.accept(KSItems.PETTER.get());
                output.accept(KSItems.COLD_STOVE.get());
                output.accept(KSItems.YUNOMI.get());
                output.accept(KSItems.CREAM_OIL.get());
                CreamOilBlock.writeCreamCount(new ItemStack(KSItems.CREAM_OIL.get()),
                        CreamOilBlockEntity.MAX_CREAM_COUNT);
            }),
            /* 食物分区 */
            new Section("foods", () -> new ItemStack(KSItems.SLIME_KING_FLAVORED_JELLY.get()), output -> {
                // ==================== 作物 ====================
                output.accept(KSItems.RHUBARB.get());
                output.accept(KSItems.RHUBARB_SEEDS.get());
                output.accept(KSItems.AMARANTH.get());
                output.accept(KSItems.AMARANTH_SEEDS.get());
                output.accept(KSItems.ANCIENT_FRUIT.get());
                output.accept(KSItems.ANCIENT_FRUIT_VINE.get());

                // ==================== 食物 ====================
                output.accept(KSItems.GOLDFISH.get());
                output.accept(KSItems.ENCHANTED_NIGHTCRAWLER.get());
                output.accept(KSItems.ANCIENT_FRUIT_PLATTER.get());
                output.accept(KSItems.AMARANTH_SOUP.get());
                output.accept(KSItems.FISH_MUSHROOM_SOUP.get());
                output.accept(KSItems.GLOWING_CATERPILLAR_SOUP.get());
                output.accept(KSItems.RHUBARB_FONDANT_PIE.get());
                output.accept(KSItems.OMELETTE_RICE.get());
                output.accept(KSItems.ANCIENT_FRUIT_SKEWER.get());
                output.accept(KSItems.EGG_BAKED_CAKE.get());
                output.accept(KSItems.IDEA_COOKIE.get());
                output.accept(KSItems.INSPIRATION_MUSHROOM.get());
                output.accept(KSItems.SLIME_KING_FLAVORED_JELLY.get());
                output.accept(KSItems.MATCHA_BB);
                output.accept(KSItems.MATCHA_PARFAIT);
                output.accept(KSItems.MATCHA_CAKE_ROLL);
                output.accept(KSItems.MATCHA_CAKE_SLICE);
                output.accept(KSItems.MATCHA_CAKE);
            }),
            /* 饮品分区 */
            new Section("drinks", () -> new ItemStack(KSItems.ICE_ELF_COCKTAIL.get()), output -> {
                // ==================== 酒水 ====================
                output.accept(KSItems.RHUBARB_JAM.get());
                output.accept(KSItems.ICE_ELF_COCKTAIL.get());
                output.accept(KSItems.BUBBLE_ECHO_COCKTAIL.get());
                output.accept(KSItems.SPRING_DRINK_COCKTAIL.get());
                output.accept(KSItems.SPRING_DRINK_1_COCKTAIL.get());
                output.accept(brewed(KSItems.ANCIENT_FRUIT_WINE.get()));
                output.accept(brewed(KSItems.REFRESHING_BEER.get()));
                output.accept(brewed(KSItems.YANJING_U7.get()));
                output.accept(brewed(KSItems.GRAPE_FLAVORED_WINE.get()));
                output.accept(brewed(KSItems.JOJA_COLA.get()));
                output.accept(brewed(KSItems.RED_EYE_WINE.get()));

                // ==================== 茶水 ====================
                output.accept(TeacupRegistry.getItem(KSTeacups.MATCHA_TEA));
                output.accept(TeacupRegistry.getItem(KSTeacups.ROSE_TEA));

            }));

    /** 当前激活的分区下标，默认全选。 */
    private static final Set<Integer> ACTIVE_SECTIONS = new TreeSet<>();

    static {
        for (int i = 0; i < SECTIONS.size(); i++) {
            ACTIVE_SECTIONS.add(i);
        }
    }

    private KSCreativeSections() {
    }

    /**
     * 取得全部分区（按按钮与物品顺序）。
     *
     * @return 不可变分区列表
     */
    public static List<Section> sections() {
        return SECTIONS;
    }

    /**
     * 判断指定分区是否处于激活状态。
     *
     * @param index 分区下标
     * @return 是否激活
     */
    public static boolean isActive(int index) {
        return ACTIVE_SECTIONS.contains(index);
    }

    /**
     * 只保留一个分区。
     *
     * @param index 分区下标
     */
    public static void selectSection(int index) {
        ACTIVE_SECTIONS.clear();
        ACTIVE_SECTIONS.add(index);
    }

    /**
     * 切换一个分区的激活状态（至少保留一个）。
     *
     * @param index 分区下标
     */
    public static void toggleSection(int index) {
        if (ACTIVE_SECTIONS.contains(index)) {
            if (ACTIVE_SECTIONS.size() > 1) {
                ACTIVE_SECTIONS.remove(index);
            }
        } else {
            ACTIVE_SECTIONS.add(index);
        }
    }

    /**
     * 把当前激活的分区写入物品栏。
     * <p>
     * 未激活的分区以「仅搜索」可见性注册，避免它们在创造栏里出现，但搜索仍然可用。
     *
     * @param output 物品栏输出
     */
    public static void addItems(CreativeModeTab.Output output) {
        List<Section> sections = new ArrayList<>(SECTIONS);
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            if (ACTIVE_SECTIONS.contains(i)) {
                section.content().accept(output);
            } else {
                section.content().accept(new SearchOnlyOutput(output));
            }
        }
    }

    /**
     * 把物品转成酒馆的满瓶形态。
     *
     * @param item 物品
     * @return 物品堆叠
     */
    private static ItemStack brewed(Item item) {
        if (item instanceof BottleBlockItem bottle) {
            return bottle.getFilledStack(6);
        }
        return new ItemStack(item);
    }

    /**
     * 只把物品放进搜索结果的输出包装。
     *
     * @param delegate 真实输出
     */
    private record SearchOnlyOutput(CreativeModeTab.Output delegate) implements CreativeModeTab.Output {

        @Override
        public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
            this.delegate.accept(stack, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
        }
    }
}