package com.tequilacat.kaleidoscope_startale.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.ITeapot;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.BaseBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.TeapotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.TeapotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.TeapotRecipeSerializer;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdBrewTeapotRecipe;
import com.tequilacat.kaleidoscope_startale.init.KSRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * 为茶壶添加冷萃配方处理。
 * <p>
 * 当茶壶下方为灵魂火源（灵魂营火、灵魂火或已点燃的冷灶）时，
 * 使用冷萃配方替代原烹饪配方推进状态机。
 */
@Mixin(TeapotBlockEntity.class)
public abstract class TeapotBlockEntityMixin {

    /** 茶壶当前输入原料 */
    @Shadow
    private ItemStack input;

    /** 茶壶内茶液标识符 */
    @Shadow
    private ResourceLocation teaFluidId;

    /** 处理完成的产物 */
    @Shadow
    private ItemStack result;

    /** 茶壶当前处理状态 */
    @Shadow
    private int status;

    /** 当前阶段剩余刻数 */
    @Shadow
    private int currentTick;

    /** 沸腾动画状态 */
    @Shadow
    public AnimationState boilingState;

    /** 处理中特效回调 */
    @Shadow
    private void onProcessingEffects(Level level) {}

    /** 完成特效回调 */
    @Shadow
    private void onFinishEffects(Level level) {}

    /** 沸腾特效回调 */
    @Shadow
    private void onBoilingEffects(Level level) {}

    /**
     * 触发方块实体刷新（同步到客户端）。
     */
    @Unique
    private void KS$refresh() {
        ((BaseBlockEntity) (Object) this).refresh();
    }

    /** 冷萃配方查询缓存，避免每次 tick 都进行完整配方匹配 */
    @Unique
    private RecipeManager.CachedCheck<TeapotInput, ColdBrewTeapotRecipe> KS$coldBrewCheck;

    /**
     * 注入 tick 方法：若下方为灵魂火源，则执行冷萃逻辑并取消原 tick。
     *
     * @param level 世界实例
     * @param ci    回调信息，用于取消原方法执行
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void KS$tickSoulFire(Level level, CallbackInfo ci) {
        if (!KS$hasSoulFireSource(level)) {
            return;
        }
        ci.cancel();
        KS$tickColdBrew(level);
    }

    /**
     * 冷萃 tick 逻辑，与原版茶壶 tick 结构一致，
     * 但热源判断改为灵魂火，配方查询改为冷萃配方类型。
     * <p>
     * 状态流转：PUT_INGREDIENT（放料）→ PROCESSING（处理中）→ FINISHED（完成）。
     * 配方查找失败时弹出原料并重置为放料状态。
     *
     * @param level 世界实例
     */
    @Unique
    private void KS$tickColdBrew(Level level) {
        BlockPos pos = ((BlockEntity) (Object) this).getBlockPos();

        // 放入原料阶段
        if (status == ITeapot.PUT_INGREDIENT) {
            long offset = level.getGameTime() + pos.hashCode();
            if (Math.floorMod(offset, 23) == 0) {
                // 茶壶为空
                if (teaFluidId.equals(TeapotRecipeSerializer.EMPTY_TEA_FLUID)) {
                    return;
                }
                // 没有灵魂火，不进行
                if (!KS$hasSoulFireSource(level)) {
                    return;
                }

                this.onProcessingEffects(level);

                // 输入原料为空
                if (input.isEmpty()) {
                    return;
                }
                // 材料放入冷却阶段
                if (this.currentTick > 0) {
                    this.currentTick = Math.max(-1, this.currentTick - 23);
                    this.KS$refresh();
                    return;
                }
                // 时间到，查找冷萃配方并进入 PROCESSING
                TeapotInput container = new TeapotInput(this.input, this.teaFluidId);
                Optional<RecipeHolder<ColdBrewTeapotRecipe>> recipeOpt =
                        KS$getColdBrewCheck().getRecipeFor(container, level);
                if (recipeOpt.isPresent()) {
                    ColdBrewTeapotRecipe recipe = recipeOpt.get().value();
                    this.result = recipe.assemble(container, level.registryAccess());
                    this.currentTick = recipe.time();
                    this.status = ITeapot.PROCESSING;
                    this.KS$refresh();
                    return;
                }
                // 配方找不到，弹出原料
                Block.popResource(level, pos, input);
                this.input = ItemStack.EMPTY;
                this.result = ItemStack.EMPTY;
                this.status = ITeapot.PUT_INGREDIENT;
                this.currentTick = -1;
                this.KS$refresh();
            }
            return;
        }

        // 处理阶段
        if (status == ITeapot.PROCESSING) {
            long offset = level.getGameTime() + pos.hashCode();
            if (Math.floorMod(offset, 23) == 0) {
                if (!KS$hasSoulFireSource(level)) {
                    return;
                }

                this.onProcessingEffects(level);

                if (currentTick > 0) {
                    this.currentTick = Math.max(-1, this.currentTick - 23);
                    this.KS$refresh();
                    return;
                }
                this.status = ITeapot.FINISHED;
                this.currentTick = -1;
                this.KS$refresh();
            }
        }

        // 完成阶段
        if (status == ITeapot.FINISHED) {
            long offset = level.getGameTime() + pos.hashCode();
            if (Math.floorMod(offset, 11) == 0) {
                if (!KS$hasSoulFireSource(level)) {
                    this.boilingState.stop();
                    this.onFinishEffects(level);
                } else {
                    this.boilingState.start((int) level.getGameTime());
                    this.onBoilingEffects(level);
                }
            }
        }
    }

    /** 冷灶方块的缓存引用，避免每次 tick 都查询注册表 */
    @Unique
    private static Block KS$coldStoveBlock;

    /**
     * 检查茶壶下方是否为灵魂火源（灵魂营火需点燃，灵魂火方块，或本模组的冷灶需点燃）。
     *
     * @param level 世界实例
     * @return 下方为有效灵魂火源返回 true
     */
    @Unique
    private boolean KS$hasSoulFireSource(Level level) {
        BlockPos belowPos = ((BlockEntity) (Object) this).getBlockPos().below();
        BlockState belowState = level.getBlockState(belowPos);
        if (belowState.is(Blocks.SOUL_CAMPFIRE)) {
            return belowState.hasProperty(BlockStateProperties.LIT) && belowState.getValue(BlockStateProperties.LIT);
        }
        if (belowState.is(Blocks.SOUL_FIRE)) {
            return true;
        }
        // 识别本模组的冷灶作为冷萃热源
        if (belowState.is(KS$getColdStoveBlock())) {
            return belowState.hasProperty(BlockStateProperties.LIT) && belowState.getValue(BlockStateProperties.LIT);
        }
        return false;
    }

    /**
     * 懒加载冷灶方块实例。
     *
     * @return 冷灶方块实例
     */
    @Unique
    private static Block KS$getColdStoveBlock() {
        if (KS$coldStoveBlock == null) {
            KS$coldStoveBlock = BuiltInRegistries.BLOCK.get(
                    ResourceLocation.fromNamespaceAndPath("kaleidoscope_teachronicles", "cold_stove")
            );
        }
        return KS$coldStoveBlock;
    }

    /**
     * 获取冷萃配方查询缓存，首次调用时创建。
     *
     * @return 冷萃配方查询缓存
     */
    @Unique
    private RecipeManager.CachedCheck<TeapotInput, ColdBrewTeapotRecipe> KS$getColdBrewCheck() {
        if (KS$coldBrewCheck == null) {
            KS$coldBrewCheck = RecipeManager.createCheck(KSRecipes.COLD_BREW_TEAPOT_TYPE);
        }
        return KS$coldBrewCheck;
    }
}