package tequila.cat.pigpig.client;

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import tequila.cat.pigpig.block.entity.JuicerBlockEntity;

/**
 * 榨汁机渲染：方块本身 + 内部液体柱 + 悬浮在液面上的食材薄片。
 * 食材按「模型厚度 / 最大边长」决定薄厚，并按槽位与层数做确定性随机摆放。
 */
public class JuicerRenderer implements BlockEntityRenderer<JuicerBlockEntity> {
    public static final long ITEM_RENDER_SEED = 0L;

    private static final float MIN_XZ = 4.55F;
    private static final float MAX_XZ = 11.45F;
    private static final float MIN_Y = 4.2F;
    private static final float MAX_Y = 13.5F;
    private static final float THIN_BASE_SIZE = 0.3F;
    private static final float THICK_SIZE_MULTIPLIER = 0.75F;
    private static final float THIN_THICKNESS_FACTOR = 0.96F;
    private static final float THICK_THICKNESS_FACTOR = 0.5F;
    private static final float RATIO_THIN_THRESHOLD = 0.15F;
    private static final float RATIO_THICK_THRESHOLD = 0.6F;
    private static final float ITEM_STACK_BASE_OFFSET = 0.05F;
    private static final float MIN_THICKNESS = 0.02F;
    private static final float RANDOM_SIZE_OFFSET = 0.02F;
    private static final int MAX_RENDER_LAYERS = 8;

    public JuicerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(JuicerBlockEntity juicer, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = juicer.getLevel();
        if (level == null) {
            return;
        }

        BlockState state = juicer.getBlockState();
        renderBlockModel(state, poseStack, buffer, packedLight, packedOverlay);
        drawFluid(poseStack, buffer, juicer.getTank().getFluid(), packedLight);
        drawFloatingItems(juicer.getItems(), level, juicer.getBlockPos().asLong(), poseStack, buffer, packedLight);
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull JuicerBlockEntity juicer) {
        return true;
    }

    static void renderBlockModel(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
        ModelData modelData = ModelData.EMPTY;

        for (RenderType renderType : model.getRenderTypes(state, RandomSource.create(42L), modelData)) {
            dispatcher.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(renderType), state, model,
                    1.0F, 1.0F, 1.0F, light, overlay, modelData, renderType);
        }
    }

    static void drawFluid(PoseStack poseStack, MultiBufferSource buffer, FluidStack fluidStack, int light) {
        if (fluidStack.isEmpty()) {
            return;
        }

        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ext.getStillTexture(fluidStack));
        float fill = Math.min((float) fluidStack.getAmount() / (float) JuicerBlockEntity.TANK_CAPACITY, 1.0F);
        float yTop = MIN_Y + (MAX_Y - MIN_Y) * fill;

        int color = ext.getTintColor(fluidStack);
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        float a = (float) (color >>> 24) / 255.0F;
        if (a <= 0.0F) {
            r = 1.0F;
            g = 1.0F;
            b = 1.0F;
            a = 1.0F;
        }

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        poseStack.pushPose();
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));

        // 顶面
        quad(consumer, matrix, light, r, g, b, a,
                MIN_XZ, yTop, MAX_XZ, u0, v1,
                MIN_XZ, yTop, MIN_XZ, u0, v0,
                MAX_XZ, yTop, MIN_XZ, u1, v0,
                MAX_XZ, yTop, MAX_XZ, u1, v1, 0.0F, 1.0F, 0.0F);
        // 北面
        quad(consumer, matrix, light, r, g, b, a,
                MIN_XZ, MIN_Y, MIN_XZ, u0, v1,
                MIN_XZ, yTop, MIN_XZ, u0, v0,
                MAX_XZ, yTop, MIN_XZ, u1, v0,
                MAX_XZ, MIN_Y, MIN_XZ, u1, v1, 0.0F, 0.0F, -1.0F);
        // 南面
        quad(consumer, matrix, light, r, g, b, a,
                MAX_XZ, MIN_Y, MAX_XZ, u0, v1,
                MAX_XZ, yTop, MAX_XZ, u0, v0,
                MIN_XZ, yTop, MAX_XZ, u1, v0,
                MIN_XZ, MIN_Y, MAX_XZ, u1, v1, 0.0F, 0.0F, 1.0F);
        // 西面
        quad(consumer, matrix, light, r, g, b, a,
                MIN_XZ, MIN_Y, MAX_XZ, u0, v1,
                MIN_XZ, yTop, MAX_XZ, u0, v0,
                MIN_XZ, yTop, MIN_XZ, u1, v0,
                MIN_XZ, MIN_Y, MIN_XZ, u1, v1, -1.0F, 0.0F, 0.0F);
        // 东面
        quad(consumer, matrix, light, r, g, b, a,
                MAX_XZ, MIN_Y, MIN_XZ, u0, v1,
                MAX_XZ, yTop, MIN_XZ, u0, v0,
                MAX_XZ, yTop, MAX_XZ, u1, v0,
                MAX_XZ, MIN_Y, MAX_XZ, u1, v1, 1.0F, 0.0F, 0.0F);

        poseStack.popPose();
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix, int light, float r, float g, float b, float a,
                             float x1, float y1, float z1, float u1, float v1,
                             float x2, float y2, float z2, float u2, float v2,
                             float x3, float y3, float z3, float u3, float v3,
                             float x4, float y4, float z4, float u4, float v4,
                             float nx, float ny, float nz) {
        vertex(consumer, matrix, light, r, g, b, a, x1, y1, z1, u1, v1, nx, ny, nz);
        vertex(consumer, matrix, light, r, g, b, a, x2, y2, z2, u2, v2, nx, ny, nz);
        vertex(consumer, matrix, light, r, g, b, a, x3, y3, z3, u3, v3, nx, ny, nz);
        vertex(consumer, matrix, light, r, g, b, a, x4, y4, z4, u4, v4, nx, ny, nz);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, int light, float r, float g, float b, float a,
                               float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(nx, ny, nz);
    }

    static void drawFloatingItems(ItemStackHandler items, @Nullable Level level, long seed,
                                  PoseStack poseStack, MultiBufferSource buffer, int light) {
        float accumulatedHeight = 0.0F;

        for (int slot = 0; slot < items.getSlots(); ++slot) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            int layers = Math.min(stack.getCount(), MAX_RENDER_LAYERS);
            for (int count = 0; count < layers; ++count) {
                Random random = new Random(seed + (long) slot * 999L + (long) count);

                poseStack.pushPose();
                Vector3f modelSize = getModelDimensions(stack, level);
                float xWidth = modelSize.x;
                float yHeight = modelSize.y;
                float zThickness = modelSize.z;

                float maxPlane = Math.max(xWidth, yHeight);
                float thicknessRatio = zThickness / maxPlane;
                float t = (thicknessRatio - RATIO_THIN_THRESHOLD) / (RATIO_THICK_THRESHOLD - RATIO_THIN_THRESHOLD);
                t = Math.max(0.0F, Math.min(1.0F, t));

                float sizeMultiplier = 1.0F + (THICK_SIZE_MULTIPLIER - 1.0F) * t;
                float thicknessFactor = THIN_THICKNESS_FACTOR + (THICK_THICKNESS_FACTOR - THIN_THICKNESS_FACTOR) * t;

                float scale = THIN_BASE_SIZE / maxPlane * sizeMultiplier;
                scale += random.nextFloat() * RANDOM_SIZE_OFFSET;

                float scaledThickness = Math.max(zThickness * scale, MIN_THICKNESS) * thicknessFactor;
                float x = 0.5F + (random.nextFloat() - 0.5F) * 0.04F;
                float z = 0.5F + (random.nextFloat() - 0.5F) * 0.04F;
                float y = 0.3125F + ITEM_STACK_BASE_OFFSET + accumulatedHeight + scaledThickness / 2.0F;

                poseStack.translate(x, y, z);
                poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.scale(scale, scale, scale);

                accumulatedHeight += scaledThickness;

                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light,
                        OverlayTexture.NO_OVERLAY, poseStack, buffer, level, 0);
                poseStack.popPose();
            }
        }
    }

    private static Vector3f getModelDimensions(ItemStack stack, @Nullable Level level) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, level, (LivingEntity) null, 0);

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;
        RandomSource random = RandomSource.create(42L);

        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : model.getQuads((BlockState) null, direction, random)) {
                float[] bounds = boundsOf(quad);
                minX = Math.min(minX, bounds[0]);
                maxX = Math.max(maxX, bounds[1]);
                minY = Math.min(minY, bounds[2]);
                maxY = Math.max(maxY, bounds[3]);
                minZ = Math.min(minZ, bounds[4]);
                maxZ = Math.max(maxZ, bounds[5]);
            }
        }
        for (BakedQuad quad : model.getQuads((BlockState) null, (Direction) null, random)) {
            float[] bounds = boundsOf(quad);
            minX = Math.min(minX, bounds[0]);
            maxX = Math.max(maxX, bounds[1]);
            minY = Math.min(minY, bounds[2]);
            maxY = Math.max(maxY, bounds[3]);
            minZ = Math.min(minZ, bounds[4]);
            maxZ = Math.max(maxZ, bounds[5]);
        }

        if (minX == Float.MAX_VALUE) {
            return new Vector3f(0.5F, 0.5F, 0.1F);
        }
        return new Vector3f(maxX - minX, maxY - minY, maxZ - minZ);
    }

    /** 返回 [minX, maxX, minY, maxY, minZ, maxZ] */
    private static float[] boundsOf(BakedQuad quad) {
        int[] vertices = quad.getVertices();
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        for (int i = 0; i < 4; ++i) {
            int base = i * 8;
            float x = Float.intBitsToFloat(vertices[base]);
            float y = Float.intBitsToFloat(vertices[base + 1]);
            float z = Float.intBitsToFloat(vertices[base + 2]);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
        }
        return new float[]{minX, maxX, minY, maxY, minZ, maxZ};
    }
}
