package com.tequilacat.kaleidoscope_startale.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;

/**
 * 饮品碰撞箱工具类。
 * <p>
 * 为各类饮品方块提供按堆叠数量区分的碰撞箱形状数组，
 * 并提供盒体构建与并集组合的辅助方法。
 */
public class DrinkShapes {

    /**
     * 构建一个盒体形状。
     *
     * @param x1 起始 X
     * @param y1 起始 Y
     * @param z1 起始 Z
     * @param x2 结束 X
     * @param y2 结束 Y
     * @param z2 结束 Z
     * @return 盒体形状
     */
    private static VoxelShape box(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

    /**
     * 合并多个形状为一个并集形状。
     * <p>
     * 不允许空数组；只有一个元素时直接返回该元素。
     *
     * @param parts 形状数组
     * @return 并集形状
     */
    private static VoxelShape union(VoxelShape... parts) {
        if (parts.length == 0) throw new IllegalArgumentException("empty union");
        if (parts.length == 1) return parts[0];
        return Shapes.or(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
    }

    /**
     * 获取远古果酒的碰撞箱形状。
     *
     * @return 按堆叠数量区分的形状数组
     */
    public static VoxelShape[] ancientFruitWine() {
        return new VoxelShape[]{
                box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D),
                box(2.0D, 0.0D, 2.0D, 15.0D, 16.0D, 14.0D)
        };
    }

    /**
     * 获取燕京 U7 的碰撞箱形状。
     *
     * @return 按堆叠数量区分的形状数组
     */
    public static VoxelShape[] yanjingU7() {
        return new VoxelShape[]{
                box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D),
                box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 12.0D),
                box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 15.0D),
                box(3.0D, 0.0D, 2.0D, 13.0D, 16.0D, 13.0D)
        };
    }

    /**
     * 获取红眼酒的碰撞箱形状。
     *
     * @return 按堆叠数量区分的形状数组
     */
    public static VoxelShape[] redEyeWine() {
        return new VoxelShape[]{
                box(3.05D, 0.0D, 6.0D, 12.95D, 15.0D, 10.0D),
                box(0.0D, 0.0D, 6.0D, 16.0D, 15.0D, 10.0D),
                box(0.0D, 0.0D, 4.0D, 16.0D, 15.0D, 14.0D),
                box(0.0D, 0.0D, 4.0D, 16.0D, 15.0D, 14.0D)
        };
    }

    /**
     * 获取 Joja 可乐的碰撞箱形状。
     * <p>
     * 数量为 3 与 4 时由多个盒体组合而成。
     *
     * @return 按堆叠数量区分的形状数组
     */
    public static VoxelShape[] jojaCola() {
        return new VoxelShape[]{
                // count=1
                box(5.5D, 0.0D, 5.5D, 10.5D, 7.5D, 10.5D),
                // count=2
                box(5.5D, 0.0D, 5.5D, 10.5D, 14.0D, 10.5D),
                // count=3
                union(
                        box(5.5D, 0.0D, 2.5D, 10.5D, 7.0D, 7.5D),
                        box(5.5D, 0.0D, 8.5D, 10.5D, 7.0D, 13.5D),
                        box(5.5D, 7.0D, 5.5D, 10.5D, 14.0D, 10.5D)),
                // count=4
                union(
                        box(1.5D, 0.0D, 1.5D, 6.5D, 7.0D, 6.5D),
                        box(9.5D, 0.0D, 1.5D, 14.5D, 7.0D, 6.5D),
                        box(1.5D, 0.0D, 9.5D, 6.5D, 7.0D, 14.5D),
                        box(9.5D, 0.0D, 9.5D, 14.5D, 7.0D, 14.5D))
        };
    }

    /**
     * 获取大黄果酱的碰撞箱形状。
     *
     * @return 按堆叠数量区分的形状数组
     */
    public static VoxelShape[] rhubarbJam() {
        return new VoxelShape[]{
                box(4.0D, 0.0D, 6.0D, 12.0D, 13.0D, 10.0D),
                box(0.0D, 0.0D, 3.0D, 16.0D, 13.0D, 14.0D)
        };
    }

    /**
     * 获取默认的碰撞箱形状。
     *
     * @return 按堆叠数量区分的形状数组
     */
    public static VoxelShape[] defaultShapes() {
        return new VoxelShape[]{
                box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D),
                box(4.0D, 0.0D, 3.0D, 12.0D, 16.0D, 13.0D),
                box(2.0D, 0.0D, 3.0D, 13.0D, 16.0D, 14.0D),
                box(2.0D, 0.0D, 3.0D, 14.0D, 16.0D, 14.0D)
        };
    }
}