package com.tequilacat.kaleidoscope_startale.compat.jei.category;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.google.common.collect.Lists;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ScentingRecipe;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 窨制配方 JEI 分类。
 * <p>
 * 多输入到单输出，需等待一定时间。
 * 定义窨制配方在 JEI 中的展示标题、图标与槽位布局，
 * 并按原料数量动态排列输入槽位，同时显示窨制时长。
 */
public class ScentingRecipeCategory implements IRecipeCategory<RecipeHolder<ScentingRecipe>> {

    /** JEI 配方类型标识 */
    public static final RecipeType<RecipeHolder<ScentingRecipe>> TYPE = RecipeType.createRecipeHolderType(KaleidoscopeStartale.id("scenting"));

    /** 界面背景纹理位置 */
    private static final ResourceLocation BG = KaleidoscopeStartale.id("textures/gui/jei/scenting.png");
    /** 界面标题 */
    private static final MutableComponent TITLE = Component.translatable("jei.kaleidoscope_startale.scenting.title");

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
     * 构造窨制配方分类。
     * <p>
     * 初始化以竹盘方块为图标的绘制对象。
     *
     * @param guiHelper JEI 界面辅助对象
     */
    public ScentingRecipeCategory(IGuiHelper guiHelper) {
        this.bgDraw = guiHelper.createDrawable(BG, 0, 0, WIDTH, HEIGHT);
        this.slotDraw = guiHelper.getSlotDrawable();
        this.iconDraw = guiHelper.createDrawableItemLike(ModBlocks.BAMBOO_TRAY.get());
    }

    /**
     * 获取客户端当前可用的窨制配方列表。
     * <p>
     * 若当前无客户端世界则返回空列表。
     *
     * @return 窨制配方列表
     */
    public static List<RecipeHolder<ScentingRecipe>> getRecipes() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        return Lists.newArrayList(level.getRecipeManager().getAllRecipesFor(KSRecipes.SCENTING_TYPE));
    }

    /**
     * 绘制配方界面。
     * <p>
     * 在底部左侧绘制窨制时长（以秒为单位）。
     *
     * @param recipe          配方实例
     * @param recipeSlotsView 配方槽位视图
     * @param guiGraphics     图形上下文
     * @param mouseX          鼠标 X 坐标
     * @param mouseY          鼠标 Y 坐标
     */
    @Override
    public void draw(RecipeHolder<ScentingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.bgDraw.draw(guiGraphics);
        Component timeText = Component.translatable("jei.kaleidoscope_startale.scenting.time", recipe.value().duration() / 20);
        drawCenteredString(guiGraphics, timeText, WIDTH / 2, 68);
    }

    /**
     * 在指定位置绘制文本。
     *
     * @param guiGraphics 图形上下文
     * @param text        文本组件
     * @param x           横坐标
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
     * 左侧按原料数量依次排列输入槽位，右侧为产物输出槽，
     * 输出槽位置根据输入槽数量动态计算。
     *
     * @param builder 配方布局构建器
     * @param holder  配方持有者
     * @param focuses 焦点组
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ScentingRecipe> holder, IFocusGroup focuses) {
        ScentingRecipe recipe = holder.value();
        List<Ingredient> ingredients = recipe.ingredients();
        int count = ingredients.size();

        int outputX = 131;
        int outputY = 29;

        int centerX = 41;
        int spacing = 18 + 2;
        int inputY = 27;
        int[] offsets = {-spacing, 0, +spacing};

        int offsetIdx = (3 - count) / 2;

        for (int i = 0; i < count; i++) {
            int x = centerX + offsets[offsetIdx + i];
            builder.addSlot(RecipeIngredientRole.INPUT, x, inputY)
                    .addIngredients(ingredients.get(i))
                    .setBackground(slotDraw, -1, -1);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, outputX, outputY)
                .addItemStack(recipe.result().copy())
                .setBackground(slotDraw, -1, -1);
    }

    /**
     * 获取 JEI 配方类型。
     *
     * @return 配方类型标识
     */
    @Override
    public RecipeType<RecipeHolder<ScentingRecipe>> getRecipeType() {
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