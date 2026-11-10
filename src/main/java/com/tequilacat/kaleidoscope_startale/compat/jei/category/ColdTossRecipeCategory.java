package com.tequilacat.kaleidoscope_startale.compat.jei.category;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.google.common.collect.Lists;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdTossRecipe;
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
 * 冷炒配方 JEI 分类。
 * <p>
 * 多输入（原料加奶油）到单输出，需等待一定时间。
 * 使用 cold_toss_pot.png 作为背景纹理，图标以原模组炒锅方块展示。
 */
public class ColdTossRecipeCategory implements IRecipeCategory<RecipeHolder<ColdTossRecipe>> {

    /** JEI 配方类型标识 */
    public static final RecipeType<RecipeHolder<ColdTossRecipe>> TYPE = RecipeType.createRecipeHolderType(KaleidoscopeStartale.id("cold_toss"));

    /** 界面背景纹理位置 */
    private static final ResourceLocation BG = KaleidoscopeStartale.id("textures/gui/jei/cold_toss_pot.png");
    /** 界面标题 */
    private static final MutableComponent TITLE = Component.translatable("jei.kaleidoscope_startale.cold_toss.title");

    /** 界面宽度 */
    public static final int WIDTH = 176;
    /** 界面高度 */
    public static final int HEIGHT = 102;

    /** 背景绘制对象 */
    private final IDrawable bgDraw;
    /** 槽位绘制对象 */
    private final IDrawable slotDraw;
    /** 图标绘制对象 */
    private final IDrawable iconDraw;

    /**
     * 构造冷炒配方分类。
     * <p>
     * 初始化槽位绘制、背景绘制与以炒锅方块为图标的绘制对象。
     *
     * @param guiHelper JEI 界面辅助对象
     */
    public ColdTossRecipeCategory(IGuiHelper guiHelper) {
        this.slotDraw = guiHelper.getSlotDrawable();
        this.bgDraw = guiHelper.createDrawable(BG, 0, 0, WIDTH, HEIGHT);
        this.iconDraw = guiHelper.createDrawableItemLike(ModBlocks.POT.get());
    }

    /**
     * 获取客户端当前可用的冷炒配方列表。
     * <p>
     * 若当前无客户端世界则返回空列表。
     *
     * @return 冷炒配方列表
     */
    public static List<RecipeHolder<ColdTossRecipe>> getRecipes() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        return Lists.newArrayList(level.getRecipeManager().getAllRecipesFor(KSRecipes.COLD_TOSS_TYPE));
    }

    /**
     * 绘制配方界面。
     * <p>
     * 绘制背景纹理，并在顶部居中显示严格配方标题、
     * 在底部居中显示冷炒所需翻炒次数。
     *
     * @param recipe          配方实例
     * @param recipeSlotsView 配方槽位视图
     * @param guiGraphics     图形上下文
     * @param mouseX          鼠标 X 坐标
     * @param mouseY          鼠标 Y 坐标
     */
    @Override
    public void draw(RecipeHolder<ColdTossRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.bgDraw.draw(guiGraphics);
        // 严格配方标题（与原 PotRecipeCategory 一致）
        drawCenteredString(guiGraphics, Component.translatable("jei.kaleidoscope_cookery.strict_recipe"), WIDTH / 2, 5);
        // 冷炒所需翻炒次数
        Component timeText = Component.translatable("jei.kaleidoscope_startale.cold_toss.time", recipe.value().stirFryCount());
        drawCenteredString(guiGraphics, timeText, WIDTH / 2, 85);
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
     * 原料按每行三个的网格排列在左侧；若存在奶油载体则单独放置一个输入槽；
     * 右侧为产物输出槽。
     *
     * @param builder 配方布局构建器
     * @param holder  配方持有者
     * @param focuses 焦点组
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ColdTossRecipe> holder, IFocusGroup focuses) {
        ColdTossRecipe recipe = holder.value();
        List<Ingredient> ingredients = recipe.ingredients();

        for (int i = 0; i < ingredients.size(); i++) {
            int xOffset = (i % 3) * 18 + 15;
            int yOffset = (i / 3) * 18 + 24;
            builder.addSlot(RecipeIngredientRole.INPUT, xOffset, yOffset)
                    .addIngredients(ingredients.get(i))
                    .setBackground(slotDraw, -1, -1);
        }

        Ingredient carrier = recipe.carrier();
        if (!carrier.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 133, 18)
                    .addIngredients(carrier)
                    .setBackground(slotDraw, -1, -1);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 143, 60)
                .addItemStack(recipe.result().copy())
                .setBackground(slotDraw, -1, -1);
    }

    /**
     * 获取 JEI 配方类型。
     *
     * @return 配方类型标识
     */
    @Override
    public RecipeType<RecipeHolder<ColdTossRecipe>> getRecipeType() {
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