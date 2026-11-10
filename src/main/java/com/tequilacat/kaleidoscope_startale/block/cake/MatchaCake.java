package com.tequilacat.kaleidoscope_startale.block.cake;

import com.tequilacat.kaleidoscope_startale.init.KSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * 抹茶蛋糕方块。
 * <p>
 * 继承原版蛋糕：空手右键吃一块。
 * 此外支持使用刀具将蛋糕整块切分为若干切片。
 */
public class MatchaCake extends CakeBlock {

    /** 刀具标签，用于判断手持物品是否为刀 */
    private static final TagKey<Item> KNIVES = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/knives")
    );

    /**
     * 无参构造，供 Block 注册表的方法引用使用。
     */
    public MatchaCake() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.5F)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .noLootTable());
    }

    /**
     * 手持物品右键蛋糕。
     * <p>
     * 若持有小刀，则将蛋糕整块切分为剩余数量的切片并移除方块；
     * 否则交给父类处理（如蜡烛装饰等）。
     *
     * @param stack     使用的物品堆
     * @param state     方块状态
     * @param level     世界实例
     * @param pos       方块位置
     * @param player    交互的玩家
     * @param hand      交互手
     * @param hitResult 方块点击结果
     * @return 物品交互结果
     */
    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(KNIVES)) {
            if (!level.isClientSide) {
                // BITES=0 是完整蛋糕 → 7 块；BITES=6 只剩最后 1 块
                int slices = MAX_BITES + 1 - state.getValue(BITES);
                for (int i = 0; i < slices; i++) {
                    popResource(level, pos, new ItemStack(KSItems.MATCHA_CAKE_SLICE.get()));
                }
                level.removeBlock(pos, false);
                level.levelEvent(2001, pos, Block.BLOCK_STATE_REGISTRY.getId(state));
                EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, player, slot);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}