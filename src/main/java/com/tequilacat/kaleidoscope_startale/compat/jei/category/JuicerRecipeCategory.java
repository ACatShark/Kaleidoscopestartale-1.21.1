package com.tequilacat.kaleidoscope_startale.compat.jei.category;

import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.JuicerRecipe;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import com.tequilacat.kaleidoscope_startale.init.KSRecipes;
import mezz.jei.api.constants.VanillaTypes;
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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 榨汁机配方 JEI 分类。
 * <p>
 * 定义榨汁机配方的展示布局：四个输入槽位、一个输出槽位，
 * 并绘制背景、流体量与制作时间文本；产物为空时以流体桶作为输出展示。
 */
public class JuicerRecipeCategory implements IRecipeCategory<RecipeHolder<JuicerRecipe>> {

    /** 榨汁机配方类型。 */
    public static final RecipeType<RecipeHolder<JuicerRecipe>> TYPE = RecipeType.createRecipeHolderType(KSRecipes.JUICER_ID);

    /**
     * 背景贴图路径。
     */
    private static final ResourceLocation BG = KaleidoscopeStartale.id("textures/gui/jei/juicer.png");

    /**
     * 分类标题。
     */
    private static final Component TITLE = Component.translatable("block.kaleidoscope_startale.juicer");

    /** 分类宽度。 */
    public static final int WIDTH = 160;

    /** 分类高度。 */
    public static final int HEIGHT = 64;

    /** 输入槽位坐标。 */
    private static final int[][] INPUT_POS = {{17, 11}, {39, 11}, {17, 33}, {39, 33}};

    /** 输出槽位 X 坐标。 */
    private static final int OUTPUT_X = 121;

    /** 输出槽位 Y 坐标。 */
    private static final int OUTPUT_Y = 22;

    /** 文本颜色。 */
    private static final int TEXT_COLOR = 5592405;

    /** 背景绘制对象。 */
    private final IDrawable bgDraw;

    /** 分类图标绘制对象。 */
    private final IDrawable iconDraw;

    /**
     * 构造榨汁机配方分类。
     *
     * @param guiHelper JEI 界面辅助对象
     */
    public JuicerRecipeCategory(IGuiHelper guiHelper) {
        this.bgDraw = guiHelper.createDrawable(BG, 0, 0, WIDTH, HEIGHT);
        this.iconDraw = guiHelper.createDrawableItemLike(KSItems.JUICER.get());
    }

    /**
     * 获取当前世界中的所有榨汁机配方。
     * <p>
     * 客户端世界不可用时返回空列表。
     *
     * @return 榨汁机配方列表
     */
    public static List<RecipeHolder<JuicerRecipe>> getRecipes() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        return new LinkedList<>(level.getRecipeManager().getAllRecipesFor(KSRecipes.JUICER_TYPE));
    }

    /**
     * 绘制分类背景与信息文本。
     * <p>
     * 在背景上绘制流体量与制作时间（以秒为单位，保留一位小数）。
     *
     * @param holder      配方持有者
     * @param slotsView   槽位视图
     * @param guiGraphics 图形绘制对象
     * @param mouseX      鼠标 X 坐标
     * @param mouseY      鼠标 Y 坐标
     */
    @Override
    public void draw(RecipeHolder<JuicerRecipe> holder, IRecipeSlotsView slotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        this.bgDraw.draw(guiGraphics);

        JuicerRecipe recipe = holder.value();
        Font font = Minecraft.getInstance().font;

        Component volume = Component.translatable("jei.kaleidoscope_startale.juicer.volume", recipe.fluidAmount());
        Component time = Component.translatable("jei.kaleidoscope_startale.juicer.time",
                String.format("%.1f", (float) recipe.craftTime() / 20.0F));

        guiGraphics.drawString(font, volume, WIDTH - font.width(volume) - 4, 42, TEXT_COLOR, false);
        guiGraphics.drawString(font, time, WIDTH - font.width(time) - 4, 53, TEXT_COLOR, false);
    }

    /**
     * 设置配方布局。
     * <p>
     * 按顺序放置非空输入原料；若有产物则展示产物，
     * 否则以产出流体的桶作为输出展示。
     *
     * @param builder  配方布局构建器
     * @param holder   配方持有者
     * @param focuses  聚焦组
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<JuicerRecipe> holder, IFocusGroup focuses) {
        JuicerRecipe recipe = holder.value();
        List<Ingredient> ingredients = recipe.getIngredients();

        for (int i = 0; i < ingredients.size() && i < INPUT_POS.length; ++i) {
            Ingredient ingredient = ingredients.get(i);
            if (!ingredient.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.INPUT, INPUT_POS[i][0], INPUT_POS[i][1]).addIngredients(ingredient);
            }
        }

        if (!recipe.result().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y).addItemStack(recipe.result());
        } else {
            Fluid fluid = recipe.fluid();
            ItemStack bucket = fluid.getBucket().getDefaultInstance();
            if (!bucket.isEmpty()) {
                builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y)
                        .addIngredients(VanillaTypes.ITEM_STACK, new ArrayList<>(List.of(bucket)));
            }
        }
    }

    /**
     * 获取配方类型。
     *
     * @return 榨汁机配方类型
     */
    @Override
    public RecipeType<RecipeHolder<JuicerRecipe>> getRecipeType() {
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