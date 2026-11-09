package com.tequilacat.kaleidoscope_startale.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tequilacat.kaleidoscope_startale.blockentity.JuicerBlockEntity;
import com.tequilacat.kaleidoscope_startale.init.KSBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * 榨汁机物品渲染器。
 * <p>
 * 用于在物品栏、手持等场景渲染榨汁机物品：
 * 先绘制方块模型，再根据物品携带的方块实体数据
 * 还原出槽内流体与物品并绘制，以呈现榨汁机的实时内容。
 */
public class JuicerItemStackRenderer extends BlockEntityWithoutLevelRenderer {

    /**
     * 构造榨汁机物品渲染器。
     */
    public JuicerItemStackRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    /**
     * 按物品渲染榨汁机。
     * <p>
     * 绘制方块模型后，若物品带有方块实体数据，
     * 则还原并绘制其中的流体与漂浮物品。
     *
     * @param stack        物品堆叠
     * @param context      物品显示上下文
     * @param poseStack    姿态栈
     * @param buffer       缓冲区
     * @param packedLight  打包光照
     * @param packedOverlay 打包覆盖
     */
    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext context, @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        JuicerRenderer.renderBlockModel(KSBlocks.JUICER.get().defaultBlockState(), poseStack, buffer, packedLight, packedOverlay);

        Contents contents = this.readContents(stack);
        if (contents != null) {
            JuicerRenderer.drawFluid(poseStack, buffer, contents.tank().getFluid(), packedLight);
            JuicerRenderer.drawFloatingItems(contents.items(), contents.level(), JuicerRenderer.ITEM_RENDER_SEED,
                    poseStack, buffer, packedLight);
        }

        poseStack.popPose();
    }

    /**
     * 从物品数据中读取榨汁机内容。
     * <p>
     * 客户端世界不可用或无方块实体数据时返回 null；
     * 否则从方块实体数据中还原流体罐与物品处理器。
     *
     * @param stack 物品堆叠
     * @return 榨汁机内容，无法读取时返回 null
     */
    @Nullable
    private Contents readContents(ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }

        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) {
            return null;
        }

        CompoundTag tag = data.copyTag();
        HolderLookup.Provider registries = level.registryAccess();

        FluidTank tank = new FluidTank(JuicerBlockEntity.TANK_CAPACITY);
        tank.readFromNBT(registries, tag.getCompound("tank"));

        ItemStackHandler items = new ItemStackHandler(JuicerBlockEntity.INPUT_SLOTS);
        items.deserializeNBT(registries, tag.getCompound("items"));

        return new Contents(tank, items, level);
    }

    /**
     * 榨汁机内容，包含流体罐、物品处理器与世界。
     *
     * @param tank  流体罐
     * @param items 物品处理器
     * @param level 世界
     */
    private record Contents(FluidTank tank, ItemStackHandler items, Level level) {
    }
}