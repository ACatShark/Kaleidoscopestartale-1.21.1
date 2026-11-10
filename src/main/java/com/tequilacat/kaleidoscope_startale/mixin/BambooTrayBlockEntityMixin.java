package com.tequilacat.kaleidoscope_startale.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.BaseBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.BambooTrayBlockEntity;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.FermentingRecipe;
import com.tequilacat.kaleidoscope_startale.crafting.recipe.ScentingRecipe;
import com.tequilacat.kaleidoscope_startale.init.KSRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * 为竹盘添加窨制与发酵处理。
 * <p>
 * 以固定间隔执行：优先尝试窨制，匹配配方后按可组成的组数消耗原料并产出结果；
 * 窨制未命中时再尝试发酵，按当前光照条件匹配配方并逐槽位推进进度。
 */
@Mixin(BambooTrayBlockEntity.class)
public abstract class BambooTrayBlockEntityMixin {

    /** 竹盘的物品存储槽位 */
    @Final
    @Shadow
    private NonNullList<ItemStack> items;

    /** 各槽位的发酵处理进度 */
    @Final
    @Shadow
    private int[] processingProgress;

    /** 窨制处理进度 */
    @Unique
    private int KS$scentingProgress = 0;

    /**
     * 注入竹盘的服务端刻逻辑。
     * <p>
     * 以固定间隔执行：优先尝试窨制处理，成功后取消原逻辑；
     * 否则尝试发酵处理，成功后同样取消原逻辑。
     *
     * @param level 世界实例
     * @param pos   方块位置
     * @param state 方块状态
     * @param tray  竹盘方块实体
     * @param ci    回调信息，用于取消原方法执行
     */
    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void KS$onServerTick(Level level, BlockPos pos, BlockState state,
                                        BambooTrayBlockEntity tray, CallbackInfo ci) {
        if (level.isClientSide) {
            return;
        }
        long offset = pos.getX() + pos.getY() + pos.getZ();
        if ((level.getGameTime() + offset) % 19 != 0) {
            return;
        }

        BambooTrayBlockEntityMixin self = (BambooTrayBlockEntityMixin) (Object) tray;

        if (self != null && self.KS$processScenting(level, pos, tray)) {
            ci.cancel();
            return;
        }

        if (self != null && self.KS$processFermenting(level, pos, tray)) {
            ci.cancel();
        }
    }

    /**
     * 处理窨制逻辑。
     * <p>
     * 匹配窨制配方后累加进度，达到时长后按可组成的组数一次性消耗原料，
     * 并将产物堆叠到已有结果槽位、空槽或掉落于世界中。
     * 未匹配配方时重置进度，并返回本次是否产生了变化。
     *
     * @param level 世界实例
     * @param pos   方块位置
     * @param tray  竹盘方块实体
     * @return 本次处理是否产生了变化
     */
    @Unique
    private boolean KS$processScenting(Level level, BlockPos pos, BambooTrayBlockEntity tray) {
        List<RecipeHolder<ScentingRecipe>> recipes = level.getRecipeManager().getAllRecipesFor(KSRecipes.SCENTING_TYPE);
        if (recipes.isEmpty()) {
            return false;
        }

        ScentingRecipe.Input input = new ScentingRecipe.Input(items);
        ScentingRecipe recipe = recipes.stream()
                .map(RecipeHolder::value)
                .filter(r -> r.matches(input, level))
                .findFirst()
                .orElse(null);

        if (recipe == null) {
            if (KS$scentingProgress != 0) {
                KS$scentingProgress = 0;
                ((BaseBlockEntity) (Object) this).refresh();
            }
            return false;
        }

        KS$scentingProgress += 19;

        if (KS$scentingProgress >= recipe.duration()) {
            // 计算能组成多少组配方
            NonNullList<ItemStack> remaining = NonNullList.create();
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    remaining.add(stack.copy());
                }
            }
            int maxSets = Integer.MAX_VALUE;
            for (Ingredient ingredient : recipe.ingredients()) {
                int available = 0;
                for (ItemStack stack : remaining) {
                    if (!stack.isEmpty() && ingredient.test(stack)) {
                        available += stack.getCount();
                    }
                }
                maxSets = Math.min(maxSets, available);
            }
            if (maxSets <= 0) {
                KS$scentingProgress = 0;
                return true;
            }
            // 按组数一次性消耗所有原料
            for (Ingredient ingredient : recipe.ingredients()) {
                int toConsume = maxSets;
                for (int i = 0; i < remaining.size() && toConsume > 0; i++) {
                    ItemStack stack = remaining.get(i);
                    if (stack.isEmpty() || !ingredient.test(stack)) {
                        continue;
                    }
                    int take = Math.min(stack.getCount(), toConsume);
                    stack.shrink(take);
                    toConsume -= take;
                }
            }
            // 写回消耗后的物品到槽位
            int remIdx = 0;
            for (int i = 0; i < items.size(); i++) {
                if (remIdx < remaining.size() && !remaining.get(remIdx).isEmpty()) {
                    items.set(i, remaining.get(remIdx));
                    remIdx++;
                } else if (remIdx < remaining.size() && remaining.get(remIdx).isEmpty()) {
                    remIdx++;
                    i--;
                } else {
                    items.set(i, ItemStack.EMPTY);
                }
            }
            // 一次性产出所有结果：优先堆叠到已有结果，再放空槽，放不下则弹出
            int remaining2 = maxSets;
            while (remaining2 > 0) {
                int toPlace = Math.min(remaining2, recipe.result().getMaxStackSize());
                ItemStack result = recipe.result().copy();
                result.setCount(toPlace);
                // 先尝试堆叠到已有的相同物品
                boolean placed = false;
                for (ItemStack existing : items) {
                    if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, result)) {
                        int space = existing.getMaxStackSize() - existing.getCount();
                        if (space > 0) {
                            int add = Math.min(space, toPlace);
                            existing.grow(add);
                            remaining2 -= add;
                            placed = true;
                            if (remaining2 <= 0) break;
                            toPlace = Math.min(remaining2, recipe.result().getMaxStackSize());
                            result.setCount(toPlace);
                        }
                    }
                }
                if (remaining2 <= 0) break;
                // 再放空槽
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isEmpty()) {
                        items.set(i, result);
                        remaining2 -= toPlace;
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY() + 1, pos.getZ(), result);
                    remaining2 -= toPlace;
                }
            }
            KS$scentingProgress = 0;
        }

        ((BaseBlockEntity) (Object) this).refresh();
        return true;
    }

    /**
     * 处理发酵逻辑。
     * <p>
     * 逐槽位匹配发酵配方（需同时满足原料与光照条件）并累加进度，
     * 达到时长后将输入按配方结果替换。未匹配配方的槽位会重置进度。
     * 返回本次处理是否产生了变化。
     *
     * @param level 世界实例
     * @param pos   方块位置
     * @param tray  竹盘方块实体
     * @return 本次处理是否产生了变化
     */
    @Unique
    private boolean KS$processFermenting(Level level, BlockPos pos, BambooTrayBlockEntity tray) {
        List<RecipeHolder<FermentingRecipe>> recipes = level.getRecipeManager().getAllRecipesFor(KSRecipes.FERMENTING_TYPE);
        if (recipes.isEmpty()) {
            return false;
        }

        int light = level.getMaxLocalRawBrightness(pos);
        boolean changed = false;
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack input = items.get(slot);
            if (input.isEmpty()) {
                continue;
            }

            FermentingRecipe recipe = recipes.stream()
                    .map(RecipeHolder::value)
                    .filter(r -> r.ingredient().test(input) && r.matchesLight(light))
                    .findFirst()
                    .orElse(null);
            if (recipe == null) {
                if (processingProgress[slot] != 0) {
                    processingProgress[slot] = 0;
                    changed = true;
                }
                continue;
            }

            processingProgress[slot] += 19;
            changed = true;

            if (processingProgress[slot] >= recipe.duration()) {
                ItemStack result = recipe.result().copy();
                int count = Math.min(result.getMaxStackSize(), result.getCount() * input.getCount());
                result.setCount(count);
                items.set(slot, result);
                processingProgress[slot] = 0;
            }
        }

        if (changed) {
            ((BaseBlockEntity) (Object) this).refresh();
        }
        return changed;
    }
}