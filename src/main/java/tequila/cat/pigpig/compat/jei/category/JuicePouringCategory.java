package tequila.cat.pigpig.compat.jei.category;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

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

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.data.JuiceBlocks;
import tequila.cat.pigpig.init.ModItems;

/**
 * JEI：果汁桶加空酒杯，倒成对应口味的果汁杯。
 * 槽位与箭头都用 JEI 自带元素绘制，标题栏宽度对齐 JEI 标准配方（116）。
 * 左边是果汁桶，中间箭头，右边是果汁，箭头正下方是酒馆的空酒杯，底部标注每杯所需果汁量。
 */
public class JuicePouringCategory implements IRecipeCategory<JuicePouringCategory.Pouring> {
    /** 一条倒入酒杯的配方：某口味的果汁桶得到对应果汁杯。 */
    public record Pouring(ItemStack bucket, ItemStack juice) {
    }

    public static final RecipeType<Pouring> TYPE =
            RecipeType.create(KaleidoscopeStartale.MODID, "juice_pouring", Pouring.class);

    private static final Component TITLE = Component.translatable("jei.kaleidoscope_startale.juice_pouring");
    private static final String TAVERN = "kaleidoscope_tavern";

    /** 与 JEI 标准配方边框同宽。 */
    private static final int WIDTH = 116;
    private static final int HEIGHT = 58;

    private static final int BUCKET_X = 40;
    private static final int BUCKET_Y = 5;
    /** 箭头 22 宽，居中在 40~57 与 86~103 两个槽之间。 */
    private static final int ARROW_X = 61;
    /** 箭头 16 高，垂直居中对齐两侧 18 高的槽位。 */
    private static final int ARROW_Y = 6;
    private static final int OUTPUT_X = 86;
    private static final int OUTPUT_Y = 5;
    /** 空酒杯 18 宽，居中在箭头正下方。 */
    private static final int GLASS_X = 63;
    private static final int GLASS_Y = 23;
    private static final int AMOUNT_Y = 43;
    private static final int TEXT_COLOR = 0xFF404040;

    private final IDrawable arrowDraw;
    private final IDrawable iconDraw;

    public JuicePouringCategory(IGuiHelper guiHelper) {
        this.arrowDraw = guiHelper.getRecipeArrow();
        this.iconDraw = guiHelper.createDrawableItemLike(ModItems.JUICER.get());
    }

    public static List<Pouring> getRecipes() {
        ItemStack glass = new ItemStack(
                com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks.EMPTY_GLASSWARE.get().asItem());
        if (glass.isEmpty()) {
            return List.of();
        }

        List<Pouring> recipes = new ArrayList<>();
        add(recipes, "sweet_berries_bucket", glass, ModItems.BERRY_JUICE);
        add(recipes, "glow_berries_bucket", glass, ModItems.GLOW_BERRY_JUICE);
        add(recipes, "grape_bucket", glass, ModItems.GRAPE_JUICE);
        add(recipes, "gold_grape_bucket", glass, ModItems.GOLD_GRAPE_JUICE);
        add(recipes, "ice_grape_bucket", glass, ModItems.ICE_GRAPE_JUICE);
        add(recipes, "green_grape_bucket", glass, ModItems.GREEN_GRAPE_JUICE);
        return recipes;
    }

    private static void add(List<Pouring> recipes, String bucketPath, ItemStack glass, DeferredItem<?> juice) {
        Item bucket = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(TAVERN, bucketPath));
        if (bucket == null || bucket == Items.AIR) {
            return;
        }

        recipes.add(new Pouring(new ItemStack(bucket), juice.get().getDefaultInstance()));
    }

    @Override
    public void draw(Pouring recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        this.arrowDraw.draw(guiGraphics, ARROW_X, ARROW_Y);

        Font font = Minecraft.getInstance().font;
        Component amount = Component.translatable("jei.kaleidoscope_startale.juice_pouring.amount",
                JuiceBlocks.AMOUNT_PER_GLASS);
        guiGraphics.drawString(font, amount, (WIDTH - font.width(amount)) / 2, AMOUNT_Y, TEXT_COLOR, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Pouring recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, BUCKET_X, BUCKET_Y).addItemStack(recipe.bucket());
        builder.addSlot(RecipeIngredientRole.INPUT, GLASS_X, GLASS_Y).addItemStack(emptyGlass());
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y).addItemStack(recipe.juice());
    }

    private static ItemStack emptyGlass() {
        return new ItemStack(
                com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks.EMPTY_GLASSWARE.get().asItem());
    }

    @Override
    public RecipeType<Pouring> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.iconDraw;
    }
}
