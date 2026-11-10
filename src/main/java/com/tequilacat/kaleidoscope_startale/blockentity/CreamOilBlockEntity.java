package com.tequilacat.kaleidoscope_startale.blockentity;

import com.tequilacat.kaleidoscope_startale.init.KSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 奶油壶方块实体。
 * <p>
 * 存储锅内的奶油数量（0 到 MAX_CREAM_COUNT）。
 * 炒锅方块实体的 Mixin 会读取并递减奶油数量，数量归零时油壶变回空油壶。
 */
public class CreamOilBlockEntity extends BlockEntity {

    /** 奶油数量上限 */
    public static final int MAX_CREAM_COUNT = 256;
    /** NBT 中记录奶油数量的键名 */
    private static final String CREAM_COUNT_KEY = "cream_count";

    /** 当前奶油数量 */
    private int creamCount = 0;

    /**
     * 构造奶油壶方块实体。
     *
     * @param pos   方块位置
     * @param state 方块状态
     */
    public CreamOilBlockEntity(BlockPos pos, BlockState state) {
        super(KSBlockEntities.CREAM_OIL.get(), pos, state);
    }

    /**
     * 保存方块实体的额外数据。
     * <p>
     * 将当前奶油数量写入 NBT。
     *
     * @param tag      要写入的 NBT 标签
     * @param provider 注册表提供者
     */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt(CREAM_COUNT_KEY, creamCount);
    }

    /**
     * 从 NBT 加载方块实体的额外数据。
     * <p>
     * 若存在奶油数量字段则读取并恢复。
     *
     * @param tag      包含数据的 NBT 标签
     * @param provider 注册表提供者
     */
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains(CREAM_COUNT_KEY)) {
            this.creamCount = tag.getInt(CREAM_COUNT_KEY);
        }
    }

    /**
     * 获取当前奶油数量。
     *
     * @return 奶油数量
     */
    public int getCreamCount() {
        return creamCount;
    }

    /**
     * 设置奶油数量，超出范围时会被钳制到 0 到上限之间，并标记为脏数据。
     *
     * @param creamCount 要设置的奶油数量
     */
    public void setCreamCount(int creamCount) {
        this.creamCount = Math.clamp(creamCount, 0, MAX_CREAM_COUNT);
        this.setChanged();
    }

    /**
     * 添加奶油。
     * <p>
     * 添加量受剩余容量限制，实际添加成功时标记为脏数据。
     *
     * @param count 期望添加的数量
     */
    public void addCream(int count) {
        int need = MAX_CREAM_COUNT - creamCount;
        int add = Math.min(need, count);
        if (add > 0) {
            creamCount += add;
            setChanged();
        }
    }

    /**
     * 抽取奶油。
     * <p>
     * 抽取量不超过当前存量，实际抽取成功时标记为脏数据。
     *
     * @param count 期望抽取的数量
     */
    public void extractCream(int count) {
        int extract = Math.min(creamCount, count);
        if (extract > 0) {
            creamCount -= extract;
            setChanged();
        }
    }
}