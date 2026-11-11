package com.tequilacat.kaleidoscope_startale.compat.jei;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopetavern.compat.jei.category.PressingTubCategory;
import com.tequilacat.kaleidoscope_startale.KaleidoscopeStartale;
import com.tequilacat.kaleidoscope_startale.compat.jei.category.*;
import com.tequilacat.kaleidoscope_startale.init.KSItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

/**
 * JEI 兼容插件。
 * <p>
 * 注册本模组的榨汁机配方分类与果汁灌注分类，
 * 并提供其配方数据与配方催化剂（榨汁机物品）。
 */
@JeiPlugin
public class ModJeiPlugin implements IModPlugin {

    /** 插件唯一标识。 */
    private static final ResourceLocation UID = KaleidoscopeStartale.id("jei");

    /**
     * 注册配方分类。
     * <p>
     * 添加榨汁机配方分类与果汁灌注分类。
     *
     * @param registration 配方分类注册
     */
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ColdBrewTeapotRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new FermentingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ScentingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ColdTossRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ColdStewRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    /**
     * 注册配方数据。
     * <p>
     * 为榨汁机分类与果汁灌注分类提供各自的配方列表。
     *
     * @param registration 配方注册
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(ColdBrewTeapotRecipeCategory.TYPE, ColdBrewTeapotRecipeCategory.getRecipes());
        registration.addRecipes(FermentingRecipeCategory.TYPE, FermentingRecipeCategory.getRecipes());
        registration.addRecipes(ScentingRecipeCategory.TYPE, ScentingRecipeCategory.getRecipes());
        registration.addRecipes(ColdTossRecipeCategory.TYPE, ColdTossRecipeCategory.getRecipes());
        registration.addRecipes(ColdStewRecipeCategory.TYPE, ColdStewRecipeCategory.getRecipes());
    }

    /**
     * 注册配方催化剂。
     * <p>
     * 将榨汁机作为催化剂：榨汁机同时能查本模组的榨汁配方和 tavern 的压榨桶配方，
     * 也用于空酒杯灌汁的果汁灌注分类。
     *
     * @param registration 配方催化剂注册
     */
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModItems.TEAPOT.get(), ColdBrewTeapotRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.BAMBOO_TRAY.get(), FermentingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.BAMBOO_TRAY.get(), ScentingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.POT.get(), ColdTossRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModBlocks.STOCKPOT.get(), ColdStewRecipeCategory.TYPE);
    }

    /**
     * 获取插件唯一标识。
     *
     * @return 插件唯一标识
     */
    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }
}