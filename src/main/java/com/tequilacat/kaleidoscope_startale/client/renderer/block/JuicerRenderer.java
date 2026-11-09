package com.tequilacat.kaleidoscope_startale.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tequilacat.kaleidoscope_startale.blockentity.JuicerBlockEntity;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * 榨汁机方块实体渲染器。
 * <p>
 * 依次渲染方块模型、槽内流体与漂浮物品：
 * 流体按容量比例填充槽体并绘制五个面；
 * 物品按堆叠数量分层堆叠，依据模型尺寸与厚度比例动态缩放，
 * 并带有随机旋转与偏移，形成自然的漂浮效果。
 */
public class JuicerRenderer implements BlockEntityRenderer<JuicerBlockEntity> {

    /** 物品渲染的随机种子基准。 */
    public static final long ITEM_RENDER_SEED = 0L;

    /** 槽体水平方向的最小坐标（以像素计）。 */
    private static final float MIN_XZ = 4.55F;

    /** 槽体水平方向的最大坐标（以像素计）。 */
    private static final float MAX_XZ = 11.45F;

    /** 槽体底部 Y 坐标（以像素计）。 */
    private static final float MIN_Y = 4.2F;

    /** 槽体顶部 Y 坐标（以像素计）。 */
    private static final float MAX_Y = 13.5F;

    /** 薄物品的基础尺寸。 */
    private static final float THIN_BASE_SIZE = 0.3F;

    /** 厚物品的尺寸倍率。 */
    private static final float THICK_SIZE_MULTIPLIER = 0.75F;

    /** 薄物品的厚度系数。 */
    private static final float THIN_THICKNESS_FACTOR = 0.96F;

    /** 厚物品的厚度系数。 */
    private static final float THICK_THICKNESS_FACTOR = 0.5F;

    /** 薄物品判定的厚度比例阈值。 */
    private static final float RATIO_THIN_THRESHOLD = 0.15F;

    /** 厚物品判定的厚度比例阈值。 */
    private static final float RATIO_THICK_THRESHOLD = 0.6F;

    /** 物品堆叠的起始高度偏移。 */
    private static final float ITEM_STACK_BASE_OFFSET = 0.05F;

    /** 物品最小厚度。 */
    private static final float MIN_THICKNESS = 0.02F;

    /** 随机尺寸附加量。 */
    private static final float RANDOM_SIZE_OFFSET = 0.02F;

    /** 单个槽位最大渲染层数。 */
    private static final int MAX_RENDER_LAYERS = 8;

    /**
     * 构造榨汁机渲染器。
     *
     * @param context 渲染器上下文
     */
    public JuicerRenderer(BlockEntityRendererProvider.Context context) {
    }

    /**
     * 渲染榨汁机方块实体。
     * <p>
     * 依次绘制方块模型、槽内流体与漂浮物品。
     *
     * @param juicer       榨汁机方块实体
     * @param partialTick  插值刻
     * @param poseStack    姿态栈
     * @param buffer       缓冲区
     * @param packedLight  打包光照
     * @param packedOverlay 打包覆盖
     */
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

    /**
     * 判断是否需要在屏幕外渲染。
     *
     * @param juicer 榨汁机方块实体
     * @return 恒为 true
     */
    @Override
    public boolean shouldRenderOffScreen(@NotNull JuicerBlockEntity juicer) {
        return true;
    }

    /**
     * 渲染方块模型。
     * <p>
     * 按模型提供的渲染类型逐个渲染，固定随机种子以保证一致性。
     *
     * @param state    方块状态
     * @param poseStack 姿态栈
     * @param buffer   缓冲区
     * @param light    打包光照
     * @param overlay  打包覆盖
     */
    static void renderBlockModel(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
        ModelData modelData = ModelData.EMPTY;

        for (RenderType renderType : model.getRenderTypes(state, RandomSource.create(42L), modelData)) {
            dispatcher.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(renderType), state, model,
                    1.0F, 1.0F, 1.0F, light, overlay, modelData, renderType);
        }
    }

    /**
     * 绘制槽内流体。
     * <p>
     * 按流体量占容量的比例确定液面高度，取流体贴图与着色，
     * 绘制顶面与四个侧面；透明通道为 0 时回退为不透明白色。
     *
     * @param poseStack  姿态栈
     * @param buffer     缓冲区
     * @param fluidStack 流体堆叠
     * @param light      打包光照
     */
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

    /**
     * 绘制一个四边形。
     * <p>
     * 按顺序为四个顶点写入顶点数据。
     *
     * @param consumer 顶点消费者
     * @param matrix   变换矩阵
     * @param light    打包光照
     * @param r        红色分量
     * @param g        绿色分量
     * @param b        蓝色分量
     * @param a        透明度
     * @param x1       顶点 1 的 X
     * @param y1       顶点 1 的 Y
     * @param z1       顶点 1 的 Z
     * @param u1       顶点 1 的 U
     * @param v1       顶点 1 的 V
     * @param x2       顶点 2 的 X
     * @param y2       顶点 2 的 Y
     * @param z2       顶点 2 的 Z
     * @param u2       顶点 2 的 U
     * @param v2       顶点 2 的 V
     * @param x3       顶点 3 的 X
     * @param y3       顶点 3 的 Y
     * @param z3       顶点 3 的 Z
     * @param u3       顶点 3 的 U
     * @param v3       顶点 3 的 V
     * @param x4       顶点 4 的 X
     * @param y4       顶点 4 的 Y
     * @param z4       顶点 4 的 Z
     * @param u4       顶点 4 的 U
     * @param v4       顶点 4 的 V
     * @param nx       法线 X
     * @param ny       法线 Y
     * @param nz       法线 Z
     */
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

    /**
     * 写入单个顶点数据。
     *
     * @param consumer 顶点消费者
     * @param matrix   变换矩阵
     * @param light    打包光照
     * @param r        红色分量
     * @param g        绿色分量
     * @param b        蓝色分量
     * @param a        透明度
     * @param x        X 坐标
     * @param y        Y 坐标
     * @param z        Z 坐标
     * @param u        U 坐标
     * @param v        V 坐标
     * @param nx       法线 X
     * @param ny       法线 Y
     * @param nz       法线 Z
     */
    private static void vertex(VertexConsumer consumer, Matrix4f matrix, int light, float r, float g, float b, float a,
                               float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(nx, ny, nz);
    }

    /**
     * 绘制漂浮物品。
     * <p>
     * 逐槽位、逐层堆叠渲染物品：根据模型尺寸与厚度比例计算缩放与厚度，
     * 叠加随机旋转与偏移，并累加高度使物品自然堆叠。
     *
     * @param items     物品处理器
     * @param level     世界
     * @param seed      随机种子基准
     * @param poseStack 姿态栈
     * @param buffer    缓冲区
     * @param light     打包光照
     */
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
                t = Math.clamp(t, 0.0F, 1.0F);

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

    /**
     * 获取物品模型的尺寸。
     * <p>
     * 遍历模型所有方向与无方向的面，计算顶点包围盒尺寸；
     * 无面时返回默认尺寸。
     *
     * @param stack 物品堆叠
     * @param level 世界
     * @return 模型的宽、高、厚
     */
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

    /**
     * 计算四边形顶点的包围盒边界。
     *
     * @param quad 烘焙四边形
     * @return 依次为 X 最小、X 最大、Y 最小、Y 最大、Z 最小、Z 最大
     */
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