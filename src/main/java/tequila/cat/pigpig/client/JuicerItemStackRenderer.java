package tequila.cat.pigpig.client;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

import tequila.cat.pigpig.block.entity.JuicerBlockEntity;
import tequila.cat.pigpig.init.ModBlocks;

/** 物品形态的榨汁机：同样渲染方块模型 + 液体 + 食材。 */
public class JuicerItemStackRenderer extends BlockEntityWithoutLevelRenderer {
    public JuicerItemStackRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext context, @NotNull PoseStack poseStack,
                             @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        JuicerRenderer.renderBlockModel(ModBlocks.JUICER.get().defaultBlockState(), poseStack, buffer, packedLight, packedOverlay);

        Contents contents = this.readContents(stack);
        if (contents != null) {
            JuicerRenderer.drawFluid(poseStack, buffer, contents.tank().getFluid(), packedLight);
            JuicerRenderer.drawFloatingItems(contents.items(), contents.level(), JuicerRenderer.ITEM_RENDER_SEED,
                    poseStack, buffer, packedLight);
        }

        poseStack.popPose();
    }

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

    private record Contents(FluidTank tank, ItemStackHandler items, Level level) {
    }
}
