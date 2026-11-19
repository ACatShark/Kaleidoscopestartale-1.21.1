package com.tequilacat.kaleidoscope_startale.item;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * 载具物品。
 * <p>
 * 使用后在对准的方块面上生成对应载具，朝向沿用玩家当前朝向，
 * 摆放空间不足时不生成、也不消耗物品。
 */
public class VehicleItem extends Item {

    /** 载具实体类型，延迟获取以避免类加载阶段访问注册表。 */
    private final Supplier<? extends EntityType<? extends Entity>> entityType;

    /**
     * 构造载具物品。
     *
     * @param properties 物品属性
     * @param entityType 载具实体类型
     */
    public VehicleItem(Properties properties, Supplier<? extends EntityType<? extends Entity>> entityType) {
        super(properties);
        this.entityType = entityType;
    }

    /**
     * 对方块使用物品时生成载具。
     * <p>
     * 客户端直接返回成功以便播放动画；服务端在点击面的外侧生成载具，
     * 碰撞检测不通过时返回失败，物品不会被消耗。
     *
     * @param context 使用上下文
     * @return 交互结果
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (context.getClickedFace() == Direction.DOWN) {
            pos = pos.below();
        } else {
            pos = pos.relative(context.getClickedFace());
        }
        Entity vehicle = this.entityType.get().create(level);
        if (vehicle == null) {
            return InteractionResult.FAIL;
        }
        float yaw = player == null ? 0.0F : player.getYRot();
        vehicle.moveTo(pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D, yaw, 0.0F);
        if (!level.noCollision(vehicle)) {
            return InteractionResult.FAIL;
        }
        level.addFreshEntity(vehicle);
        level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 0.9F);
        ItemStack stack = context.getItemInHand();
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
