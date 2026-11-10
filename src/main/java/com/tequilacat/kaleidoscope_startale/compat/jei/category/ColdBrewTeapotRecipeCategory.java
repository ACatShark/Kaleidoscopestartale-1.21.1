package com.tequilacat.kaleidoscope_startale.compat.jei.category;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.google.common.collect.Lists;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdBrewTeapotRecipe;
import com.tequilacat.kaleidoscope_startale.init.KSRecipes;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 冷萃配方的 EMI 展示。
 * <p>
 * 定义冷萃配方在 JEI 中的展示标题、背景、图标与槽位布局，
 * 并显示所需的茶液、原料与产出结果及处理时长。
 */
public class ColdBrewTeapotRecipeCategory implements IRecipeCategory<RecipeHolder<ColdBrewTeapotRecipe>> {

    /** JEI 配方类型标识 */
    public static final RecipeType<RecipeHolder<ColdBrewTeapotRecipe>> TYPE = RecipeType.createRecipeHolderType(KaleidoscopeStartale.id("cold_brew_teapot"));

    /** 界面背景纹理位置 */
    private static final ResourceLocation BG = KaleidoscopeStartale.id("textures/gui/jei/cold_brew_teapot.png");
    /** 界面标题 */
    private static final MutableComponent TITLE = Component.translatable("jei.kaleidoscope_startale.cold_brew_teapot.title");

    /** 界面宽度 */
    public static final int WIDTH = 176;
    /** 界面高度 */
    public static final int HEIGHT = 78;

    /** 背景绘制对象 */
    private final IDrawable bgDraw;
    /** 图标绘制对象 */
    private final IDrawable iconDraw;

    /**
     * 构造冷萃配方分类。
     * <p>
     * 初始化背景绘制对象与以茶壶物品为图标的绘制对象。
     *
     * @param guiHelper JEI 界面辅助对象
     */
    public ColdBrewTeapotRecipeCategory(IGuiHelper guiHelper) {
        this.bgDraw = guiHelper.createDrawable(BG, 0, 0, WIDTH, HEIGHT);
        this.iconDraw = guiHelper.createDrawableItemLike(ModItems.TEAPOT.get());
    }

    /**
     * 获取客户端当前可用的冷萃配方列表。
     * <p>
     * 若当前无客户端世界则返回空列表。
     *
     * @return 冷萃配方列表
     */
    public static List<RecipeHolder<ColdBrewTeapotRecipe>> getRecipes() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        List<RecipeHolder<ColdBrewTeapotRecipe>> recipes = Lists.newArrayList();
        recipes.addAll(level.getRecipeManager().getAllRecipesFor(KSRecipes.COLD_BREW_TEAPOT_TYPE));
        return recipes;
    }

    /**
     * 绘制配方界面。
     * <p>
     * 绘制背景纹理，并在底部居中显示处理时长（以秒为单位）。
     *
     * @param recipe         配方实例
     * @param recipeSlotsView 配方槽位视图
     * @param guiGraphics    图形上下文
     * @param mouseX         鼠标 X 坐标
     * @param mouseY         鼠标 Y 坐标
     */
    @Override
    public void draw(RecipeHolder<ColdBrewTeapotRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.bgDraw.draw(guiGraphics);
        Component brewTime = Component.translatable("jei.kaleidoscope_startale.cold_brew_teapot.time", recipe.value().time() / 20);
        drawCenteredString(guiGraphics, brewTime, WIDTH / 2, 70);
    }

    /**
     * 在指定水平中心处绘制文本。
     *
     * @param guiGraphics 图形上下文
     * @param text        文本组件
     * @param centerX     中心 X 坐标
     * @param y           纵坐标
     */
    private void drawCenteredString(GuiGraphics guiGraphics, Component text, int centerX, int y) {
        Font font = Minecraft.getInstance().font;
        FormattedCharSequence sequence = text.getVisualOrderText();
        guiGraphics.drawString(font, sequence, centerX - font.width(sequence) / 2, y, 0x555555, false);
    }

    /**
     * 设置配方界面的槽位布局。
     * <p>
     * 依次布置茶液桶输入槽、原料输入槽、仅渲染的杯子槽与产出槽，
     * 其中原料按配方所需数量显示，产出按固定数量显示。
     *
     * @param builder  配方布局构建器
     * @param holder   配方持有者
     * @param focuses  焦点组
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ColdBrewTeapotRecipe> holder, IFocusGroup focuses) {
        ColdBrewTeapotRecipe recipe = holder.value();
        List<ItemStack> inputs = Arrays.stream(recipe.ingredient().getItems())
                .map(stack -> stack.copyWithCount(recipe.ingredientCount()))
                .toList();
        ItemStack output = recipe.result().copyWithCount(ColdBrewTeapotRecipe.OUTPUT_COUNT);

        Fluid fluid = BuiltInRegistries.FLUID.get(recipe.teaFluid());
        net.minecraft.world.item.Item bucket = fluid.getBucket();

        builder.addSlot(RecipeIngredientRole.INPUT, 65, 0).setStandardSlotBackground().addItemLike(bucket);
        builder.addSlot(RecipeIngredientRole.INPUT, 83, 0).setStandardSlotBackground().addItemStacks(inputs);
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 122, 0).addIngredients(recipe.container());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 128, 45).addItemStack(output);
    }

    /**
     * 获取 JEI 配方类型。
     *
     * @return 配方类型标识
     */
    @Override
    public RecipeType<RecipeHolder<ColdBrewTeapotRecipe>> getRecipeType() {
        return TYPE;
    }

    /**
     * 获取界面标题。
     *
     * @return 界面标题
     */
    @Override
    public Component getTitle() {
        return TITLE;
    }

    /**
     * 获取界面宽度。
     *
     * @return 界面宽度
     */
    @Override
    public int getWidth() {
        return WIDTH;
    }

    /**
     * 获取界面高度。
     *
     * @return 界面高度
     */
    @Override
    public int getHeight() {
        return HEIGHT;
    }

    /**
     * 获取界面图标。
     *
     * @return 图标绘制对象
     */
    @Override
    @Nullable
    public IDrawable getIcon() {
        return iconDraw;
    }
}