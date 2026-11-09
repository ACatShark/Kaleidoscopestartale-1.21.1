package com.tequilacat.kaleidoscope_startale.compat.jei.category;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import com.tequilacat.kaleidoscope_startale.util.JuiceBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 果汁灌注配方 JEI 分类。
 * <p>
 * 展示「果汁桶 + 空酒杯 → 果汁」的灌注配方，
 * 绘制箭头与每杯流体量文本，并按酒馆模组的果汁桶物品动态生成配方列表。
 */
public class JuicePouringCategory implements IRecipeCategory<JuicePouringCategory.Pouring> {

    /**
     * 灌注配方数据，包含输入果汁桶与输出果汁。
     *
     * @param bucket 果汁桶物品堆叠
     * @param juice  果汁物品堆叠
     */
    public record Pouring(ItemStack bucket, ItemStack juice) {
    }

    /** 灌注配方类型。 */
    public static final RecipeType<Pouring> TYPE = RecipeType.create(KaleidoscopeStartale.MODID, "juice_pouring", Pouring.class);

    /**
     * 分类标题。
     */
    private static final Component TITLE = Component.translatable("jei.kaleidoscope_startale.juice_pouring");

    /**
     * 酒馆模组的命名空间。
     */
    private static final String TAVERN = "kaleidoscope_tavern";

    /** 与 JEI 标准配方边框同宽。 */
    private static final int WIDTH = 116;

    /** 分类高度。 */
    private static final int HEIGHT = 58;

    /** 果汁桶槽位 X 坐标。 */
    private static final int BUCKET_X = 40;

    /** 果汁桶槽位 Y 坐标。 */
    private static final int BUCKET_Y = 5;

    /** 箭头 22 宽，居中在 40~57 与 86~103 两个槽之间。 */
    private static final int ARROW_X = 61;

    /** 箭头 16 高，垂直居中对齐两侧 18 高的槽位。 */
    private static final int ARROW_Y = 6;

    /** 输出槽位 X 坐标。 */
    private static final int OUTPUT_X = 86;

    /** 输出槽位 Y 坐标。 */
    private static final int OUTPUT_Y = 5;

    /** 空酒杯 18 宽，居中在箭头正下方。 */
    private static final int GLASS_X = 63;

    /** 空酒杯槽位 Y 坐标。 */
    private static final int GLASS_Y = 23;

    /** 流体量文本 Y 坐标。 */
    private static final int AMOUNT_Y = 43;

    /** 文本颜色。 */
    private static final int TEXT_COLOR = 0xFF404040;

    /** 箭头绘制对象。 */
    private final IDrawable arrowDraw;

    /** 分类图标绘制对象。 */
    private final IDrawable iconDraw;

    /**
     * 构造果汁灌注配方分类。
     *
     * @param guiHelper JEI 界面辅助对象
     */
    public JuicePouringCategory(IGuiHelper guiHelper) {
        this.arrowDraw = guiHelper.getRecipeArrow();
        this.iconDraw = guiHelper.createDrawableItemLike(KSItems.JUICER.get());
    }

    /**
     * 获取所有灌注配方。
     * <p>
     * 以空酒杯为输出目标，按酒馆模组的各类果汁桶构建配方；
     * 空酒杯不可用时返回空列表。
     *
     * @return 灌注配方列表
     */
    public static List<Pouring> getRecipes() {
        ItemStack glass = new ItemStack(
                com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks.EMPTY_GLASSWARE.get().asItem());
        if (glass.isEmpty()) {
            return List.of();
        }

        List<Pouring> recipes = new ArrayList<>();
        add(recipes, "sweet_berries_bucket", glass, KSItems.BERRY_JUICE);
        add(recipes, "glow_berries_bucket", glass, KSItems.GLOW_BERRY_JUICE);
        add(recipes, "grape_bucket", glass, KSItems.GRAPE_JUICE);
        add(recipes, "gold_grape_bucket", glass, KSItems.GOLD_GRAPE_JUICE);
        add(recipes, "ice_grape_bucket", glass, KSItems.ICE_GRAPE_JUICE);
        add(recipes, "green_grape_bucket", glass, KSItems.GREEN_GRAPE_JUICE);
        return recipes;
    }

    /**
     * 添加一条灌注配方。
     * <p>
     * 按酒馆命名空间解析果汁桶物品，物品不存在或为空气时跳过。
     *
     * @param recipes    配方列表
     * @param bucketPath 果汁桶物品路径
     * @param glass      空酒杯物品堆叠
     * @param juice      输出果汁物品
     */
    private static void add(List<Pouring> recipes, String bucketPath, ItemStack glass, DeferredItem<?> juice) {
        Item bucket = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(TAVERN, bucketPath));
        if (bucket == null || bucket == Items.AIR) {
            return;
        }

        recipes.add(new Pouring(new ItemStack(bucket), juice.get().getDefaultInstance()));
    }

    /**
     * 绘制分类箭头与流体量文本。
     * <p>
     * 绘制配方箭头，并在居中位置显示每杯的流体量。
     *
     * @param recipe      灌注配方
     * @param slotsView   槽位视图
     * @param guiGraphics 图形绘制对象
     * @param mouseX      鼠标 X 坐标
     * @param mouseY      鼠标 Y 坐标
     */
    @Override
    public void draw(Pouring recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        this.arrowDraw.draw(guiGraphics, ARROW_X, ARROW_Y);

        Font font = Minecraft.getInstance().font;
        Component amount = Component.translatable("jei.kaleidoscope_startale.juice_pouring.amount", JuiceBlocks.AMOUNT_PER_GLASS);
        guiGraphics.drawString(font, amount, (WIDTH - font.width(amount)) / 2, AMOUNT_Y, TEXT_COLOR, false);
    }

    /**
     * 设置配方布局。
     * <p>
     * 放置果汁桶与空酒杯两个输入槽位，以及果汁输出槽位。
     *
     * @param builder 配方布局构建器
     * @param recipe  灌注配方
     * @param focuses 聚焦组
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Pouring recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, BUCKET_X, BUCKET_Y).addItemStack(recipe.bucket());
        builder.addSlot(RecipeIngredientRole.INPUT, GLASS_X, GLASS_Y).addItemStack(emptyGlass());
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y).addItemStack(recipe.juice());
    }

    /**
     * 获取空酒杯物品堆叠。
     *
     * @return 空酒杯物品堆叠
     */
    private static ItemStack emptyGlass() {
        return new ItemStack(
                com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks.EMPTY_GLASSWARE.get().asItem());
    }

    /**
     * 获取配方类型。
     *
     * @return 灌注配方类型
     */
    @Override
    public RecipeType<Pouring> getRecipeType() {
        return TYPE;
    }

    /**
     * 获取分类标题。
     *
     * @return 分类标题
     */
    @Override
    public Component getTitle() {
        return TITLE;
    }

    /**
     * 获取分类宽度。
     *
     * @return 分类宽度
     */
    @Override
    public int getWidth() {
        return WIDTH;
    }

    /**
     * 获取分类高度。
     *
     * @return 分类高度
     */
    @Override
    public int getHeight() {
        return HEIGHT;
    }

    /**
     * 获取分类图标。
     *
     * @return 分类图标绘制对象
     */
    @Override
    public @Nullable IDrawable getIcon() {
        return this.iconDraw;
    }
}