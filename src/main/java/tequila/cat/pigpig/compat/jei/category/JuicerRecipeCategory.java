package tequila.cat.pigpig.compat.jei.category;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

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

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.crafting.JuicerRecipe;
import tequila.cat.pigpig.init.ModItems;
import tequila.cat.pigpig.init.ModRecipes;

public class JuicerRecipeCategory implements IRecipeCategory<RecipeHolder<JuicerRecipe>> {
    public static final RecipeType<RecipeHolder<JuicerRecipe>> TYPE =
            RecipeType.createRecipeHolderType(ModRecipes.JUICER_ID);
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(
            KaleidoscopeStartale.MODID, "textures/gui/jei/juicer.png");
    private static final Component TITLE = Component.translatable("block.kaleidoscope_startale.juicer");

    public static final int WIDTH = 160;
    public static final int HEIGHT = 64;

    /** 与背景图里画好的槽框逐像素对齐：(17,11)、(39,11)、(17,33)、(39,33)。 */
    private static final int[][] INPUT_POS = {{17, 11}, {39, 11}, {17, 33}, {39, 33}};
    private static final int OUTPUT_X = 121;
    private static final int OUTPUT_Y = 22;
    private static final int TEXT_COLOR = 5592405;

    private final IDrawable bgDraw;
    private final IDrawable iconDraw;

    public JuicerRecipeCategory(IGuiHelper guiHelper) {
        this.bgDraw = guiHelper.createDrawable(BG, 0, 0, WIDTH, HEIGHT);
        this.iconDraw = guiHelper.createDrawableItemLike(ModItems.JUICER.get());
    }

    public static List<RecipeHolder<JuicerRecipe>> getRecipes() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        return new LinkedList<>(level.getRecipeManager().getAllRecipesFor(ModRecipes.JUICER_TYPE));
    }

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

    @Override
    public RecipeType<RecipeHolder<JuicerRecipe>> getRecipeType() {
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
