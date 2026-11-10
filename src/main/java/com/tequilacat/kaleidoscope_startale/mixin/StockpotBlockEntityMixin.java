package com.tequilacat.kaleidoscope_startale.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.BaseBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ColdStewRecipe;
import com.tequilacat.kaleidoscope_startale.init.KSRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * 为汤锅添加冷炖配方处理。
 * <p>
 * 当汤锅下方为已点燃的冷灶时，使用冷炖配方替代原烹饪配方推进状态机，
 * 并额外校验汤底是否与配方匹配。
 */
@Mixin(StockpotBlockEntity.class)
public abstract class StockpotBlockEntityMixin {

    /** 汤锅的原料槽位 */
    @Shadow
    private NonNullList<ItemStack> inputs;

    /** 当前汤底标识符 */
    @Shadow
    private ResourceLocation soupBaseId;

    /** 处理完成的产物 */
    @Shadow
    private ItemStack result;

    /** 汤锅状态 */
    @Shadow
    private int status;

    /** 当前阶段剩余刻数 */
    @Shadow
    private int currentTick;

    /** 可取出的成品数量 */
    @Shadow
    private int takeoutCount;

    /** 是否有锅盖 */
    @Shadow
    public boolean hasLid() { return false; }

    /** 是否为空 */
    @Shadow
    public boolean isEmpty() { return false; }

    /** 获取当前汤底 */
    @Shadow
    public ISoupBase getSoupBase() { return null; }

    /**
     * 注入 hasHeatSource 方法：当下方为冷灶时视为存在热源。
     *
     * @param level 世界实例
     * @param cir   回调信息，用于设置返回值
     */
    @Inject(
            method = "hasHeatSource(Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void KS$injectHasHeatSource(Level level, CallbackInfoReturnable<Boolean> cir) {
        if (KS$hasColdStoveSource(level)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * 注入汤锅的 tick 方法。
     * <p>
     * 若下方不是冷灶则放行原逻辑，否则执行冷炖逻辑并取消原 tick。
     *
     * @param level 世界实例
     * @param ci    回调信息，用于取消原方法执行
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void KS$coldStewTick(Level level, CallbackInfo ci) {
        if (!KS$hasColdStoveSource(level)) {
            return;
        }
        ci.cancel();
        KS$tickColdStew(level);
    }

    /** 冷炖配方查询缓存，避免每次 tick 都进行完整配方匹配 */
    @Unique
    private RecipeManager.CachedCheck<SimpleInput, ColdStewRecipe> KS$coldStewCheck;

    /**
     * 冷炖 tick 状态机。
     * <p>
     * 状态流转：1（放料完成，待开始）→ 2（冷炖处理中）→ 3（完成）。
     * 开始烹饪前需同时满足有锅盖、锅内有物品、匹配冷炖配方且汤底相符。
     *
     * @param level 世界实例
     */
    @Unique
    private void KS$tickColdStew(Level level) {
        if (this.status == 0) {
            return;
        }

        // status=1: 尝试匹配冷炖配方并进入处理阶段
        if (this.status == 1) {
            if (!this.hasLid()) return;
            if (this.isEmpty()) return;

            SimpleInput simpleInput = new SimpleInput(this.inputs);
            Optional<RecipeHolder<ColdStewRecipe>> recipeOpt =
                    KS$getColdStewCheck().getRecipeFor(simpleInput, level);

            if (recipeOpt.isPresent()) {
                ColdStewRecipe recipe = recipeOpt.get().value();

                if (!KS$soupBaseMatches(recipe)) return;

                this.result = recipe.assemble(simpleInput, level.registryAccess());
                this.currentTick = recipe.duration();
                this.takeoutCount = Math.min(this.result.getCount(), 9);
                this.status = 2;
                ((BaseBlockEntity) (Object) this).refresh();
            }
            return;
        }

        // status=2: 冷炖处理 —— currentTick 递减到 0 进入完成
        if (this.status == 2) {
            if (this.currentTick > 0) {
                --this.currentTick;
                if (this.currentTick % 5 == 0) {
                    ((BaseBlockEntity) (Object) this).refresh();
                }
            }
            if (this.currentTick == 0) {
                this.status = 3;
                this.currentTick = -1;
                ((BaseBlockEntity) (Object) this).refresh();
            }
        }
    }

    /**
     * 检查当前汤底是否与冷炖配方匹配。
     * <p>
     * 配方未声明汤底时视为匹配；否则要求当前汤底能识别配方中任意一个汤底物品。
     *
     * @param recipe 冷炖配方
     * @return 匹配返回 true
     */
    @Unique
    private boolean KS$soupBaseMatches(ColdStewRecipe recipe) {
        if (recipe.soupBase().isEmpty()) return true;

        ISoupBase soup = getSoupBase();
        if (soup == null) return false;

        for (ItemStack candidate : recipe.soupBase().getItems()) {
            if (soup.isSoupBase(candidate)) return true;
        }
        return false;
    }

    /**
     * 获取冷炖配方查询缓存，首次调用时创建。
     *
     * @return 冷炖配方查询缓存
     */
    @Unique
    private RecipeManager.CachedCheck<SimpleInput, ColdStewRecipe> KS$getColdStewCheck() {
        if (KS$coldStewCheck == null) {
            KS$coldStewCheck = RecipeManager.createCheck(KSRecipes.COLD_STEW_TYPE);
        }
        return KS$coldStewCheck;
    }

    /** 冷灶方块的缓存引用，避免每次 tick 都查询注册表 */
    @Unique
    private static Block KS$coldStoveBlock;

    /**
     * 检查汤锅下方是否为 KTC 冷灶。
     *
     * @param level 世界实例
     * @return 下方为已点燃的 KTC 冷灶返回 true
     */
    @Unique
    private boolean KS$hasColdStoveSource(Level level) {
        BlockPos belowPos = ((BlockEntity) (Object) this).getBlockPos().below();
        BlockState belowState = level.getBlockState(belowPos);
        Block coldStove = KS$getColdStoveBlock();
        if (coldStove != null && belowState.is(coldStove)) {
            // 冷灶需要 LIT=true 才算有效热源
            return !belowState.hasProperty(BlockStateProperties.LIT) || belowState.getValue(BlockStateProperties.LIT);
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
}