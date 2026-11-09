package tequila.cat.pigpig.compat.jei;

import com.github.ysbbbbbb.kaleidoscopetavern.compat.jei.category.PressingTubCategory;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import net.minecraft.resources.ResourceLocation;

import tequila.cat.pigpig.KaleidoscopeStartale;
import tequila.cat.pigpig.compat.jei.category.JuicePouringCategory;
import tequila.cat.pigpig.compat.jei.category.JuicerRecipeCategory;
import tequila.cat.pigpig.init.ModItems;

@JeiPlugin
public class ModJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(KaleidoscopeStartale.MODID, "jei");

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JuicerRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new JuicePouringCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(JuicerRecipeCategory.TYPE, JuicerRecipeCategory.getRecipes());
        registration.addRecipes(JuicePouringCategory.TYPE, JuicePouringCategory.getRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // 榨汁机同时能查本模组的榨汁配方和 tavern 的压榨桶配方
        registration.addRecipeCatalyst(ModItems.JUICER.get().getDefaultInstance(), JuicerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModItems.JUICER.get().getDefaultInstance(), PressingTubCategory.TYPE);
        // 榨汁机 → 空酒杯灌汁
        registration.addRecipeCatalyst(ModItems.JUICER.get().getDefaultInstance(), JuicePouringCategory.TYPE);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }
}
