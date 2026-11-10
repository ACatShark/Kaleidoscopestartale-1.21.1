package com.tequilacat.kaleidoscope_startale.compat.jei.category;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.google.common.collect.Lists;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.FermentingRecipe;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 发酵配方 JEI 分类。
 * <p>
 * 单输入到单输出，需光照在配方指定区间内。
 * 定义发酵配方在 JEI 中的展示标题、图标与槽位布局，
 * 并显示所需光照条件与发酵时长。
 */
public class FermentingRecipeCategory implements IRecipeCategory<RecipeHolder<FermentingRecipe>> {

    /** JEI 配方类型标识 */
    public static final RecipeType<RecipeHolder<FermentingRecipe>> TYPE = RecipeType.createRecipeHolderType(KaleidoscopeStartale.id("fermenting"));
    /** 界面背景纹理位置 */
    private static final ResourceLocation BG = KaleidoscopeStartale.id("textures/gui/jei/fermenting.png");
    /** 界面标题 */
    private static final MutableComponent TITLE = Component.translatable("jei.kaleidoscope_startale.fermenting.title");

    /** 界面宽度 */
    public static final int WIDTH = 176;
    /** 界面高度 */
    public static final int HEIGHT = 78;

    /** 背景绘制对象 */
    private final IDrawable bgDraw;
    /** 槽位绘制对象 */
    private final IDrawable slotDraw;
    /** 图标绘制对象 */
    private final IDrawable iconDraw;

    /**
     * 构造发酵配方分类。
     * <p>
     * 初始化以竹盘方块为图标的绘制对象。
     *
     * @param guiHelper JEI 界面辅助对象
     */
    public FermentingRecipeCategory(IGuiHelper guiHelper) {
        this.bgDraw = guiHelper.createDrawable(BG, 0, 0, WIDTH, HEIGHT);
        this.slotDraw = guiHelper.getSlotDrawable();
        this.iconDraw = guiHelper.createDrawableItemLike(ModBlocks.BAMBOO_TRAY.get());
    }

    /**
     * 获取客户端当前可用的发酵配方列表。
     * <p>
     * 若当前无客户端世界则返回空列表。
     *
     * @return 发酵配方列表
     */
    public static List<RecipeHolder<FermentingRecipe>> getRecipes() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        return Lists.newArrayList(level.getRecipeManager().getAllRecipesFor(KSRecipes.FERMENTING_TYPE));
    }

    /**
     * 绘制配方界面。
     * <p>
     * 在底部两侧分别绘制所需光照条件与发酵时长（以秒为单位）。
     *
     * @param recipe          配方实例
     * @param recipeSlotsView 配方槽位视图
     * @param guiGraphics     图形上下文
     * @param mouseX          鼠标 X 坐标
     * @param mouseY          鼠标 Y 坐标
     */
    @Override
    public void draw(RecipeHolder<FermentingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.bgDraw.draw(guiGraphics);
        FermentingRecipe r = recipe.value();
        Component infoText = Component.translatable("jei.kaleidoscope_startale.fermenting.info",
                r.minLight(), r.maxLight(), r.duration() / 20);
        drawCenteredString(guiGraphics, infoText, WIDTH / 2, 68);
    }

    /**
     * 在指定位置绘制居中文本。
     *
     * @param guiGraphics 图形上下文
     * @param text        文本组件
     * @param x           中心点横坐标
     * @param y           纵坐标
     */
    private void drawCenteredString(GuiGraphics guiGraphics, Component text, int x, int y) {
        Font font = Minecraft.getInstance().font;
        FormattedCharSequence sequence = text.getVisualOrderText();
        guiGraphics.drawString(font, sequence, x - font.width(sequence) / 2, y, 0x555555, false);
    }

    /**
     * 设置配方界面的槽位布局。
     * <p>
     * 左侧为原料输入槽，右侧为产物输出槽。
     *
     * @param builder 配方布局构建器
     * @param holder  配方持有者
     * @param focuses 焦点组
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<FermentingRecipe> holder, IFocusGroup focuses) {
        FermentingRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 41, 27)
                .addIngredients(recipe.ingredient())
                .setBackground(slotDraw, -1, -1);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 131, 29)
                .addItemStack(recipe.result().copy())
                .setBackground(slotDraw, -1, -1);
    }

    /**
     * 获取 JEI 配方类型。
     *
     * @return 配方类型标识
     */
    @Override
    public RecipeType<RecipeHolder<FermentingRecipe>> getRecipeType() {
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