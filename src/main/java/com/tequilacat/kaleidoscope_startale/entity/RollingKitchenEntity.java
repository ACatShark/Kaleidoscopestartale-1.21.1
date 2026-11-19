package com.tequilacat.kaleidoscope_startale.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * 流动小厨载具实体。
 * <p>
 * 玩家骑乘后由按键输入驱动转向与前后移动，重力、摩擦与限速自行处理，
 * 每刻把转向增量同步给乘客，使乘客随车体一起转动。
 */
public class RollingKitchenEntity extends Entity {

    /** 每秒转向角度（度）。 */
    private static final float TURN_SPEED = 3.0F;
    /** 前进加速度。 */
    private static final float ACCELERATION = 0.045F;
    /** 倒退加速度。 */
    private static final float REVERSE_ACCELERATION = 0.02F;
    /** 地面摩擦。 */
    private static final double GROUND_DRAG = 0.90D;
    /** 空中摩擦。 */
    private static final double AIR_DRAG = 0.98D;
    /** 最大水平速度。 */
    private static final double MAX_SPEED = 0.36D;
    /** 重力。 */
    private static final double GRAVITY = 0.08D;
    /** 车内物品槽数量。 */
    public static final int CARGO_SIZE = 1;
    /** 下落速度上限。 */
    private static final double MAX_FALL_SPEED = 2.0D;
    /** 驾驶座相对实体原点的偏移。 */
    private static final Vec3 SEAT_OFFSET = new Vec3(0.0D, 1.12D, 1.9D);

    /** 左转按键是否按下。 */
    private boolean inputLeft;
    /** 右转按键是否按下。 */
    private boolean inputRight;
    /** 前进按键是否按下。 */
    private boolean inputUp;
    /** 后退按键是否按下。 */
    private boolean inputDown;
    /** 本刻的转向增量，用于同步转动乘客。 */
    private float deltaRotation;
    /** 车内物品（后续用于放置工作方块）。 */
    private final NonNullList<ItemStack> cargo = NonNullList.withSize(CARGO_SIZE, ItemStack.EMPTY);

    /**
     * 构造流动小厨载具实体。
     * <p>
     * 标记为建筑方块，使载具可被放置在方块之上。
     *
     * @param entityType 实体类型
     * @param level      所在世界
     */
    public RollingKitchenEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    /**
     * 写入驾驶输入。
     * <p>
     * 由客户端输入包调用，服务端据此驱动每刻的转向与加速。
     *
     * @param inputLeft  左转
     * @param inputRight 右转
     * @param inputUp    前进
     * @param inputDown  后退
     */
    public void setInput(boolean inputLeft, boolean inputRight, boolean inputUp, boolean inputDown) {
        this.inputLeft = inputLeft;
        this.inputRight = inputRight;
        this.inputUp = inputUp;
        this.inputDown = inputDown;
    }

    /**
     * 注册需要同步的字段。
     * <p>
     * 载具状态全部由服务端驱动，暂无需要同步的字段。
     *
     * @param builder 同步数据构建器
     */
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // 暂时没有需要同步的字段
    }

    /**
     * 读取存档数据。
     * <p>
     * 载具不保存额外状态，重新加载后从静止开始。
     *
     * @param compound 实体数据标签
     */
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        ContainerHelper.loadAllItems(compound, this.cargo, this.registryAccess());
    }

    /**
     * 写入存档数据。
     * <p>
     * 载具不保存额外状态。
     *
     * @param compound 实体数据标签
     */
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        ContainerHelper.saveAllItems(compound, this.cargo, this.registryAccess());
    }

    /**
     * 获取生成实体的网络包。
     *
     * @param serverEntity 服务端实体跟踪器
     * @return 生成实体数据包
     */
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    /**
     * 判断是否可被准星选中。
     *
     * @return 恒为 true
     */
    @Override
    public boolean isPickable() {
        return true;
    }

    /**
     * 处理伤害：载具不防火，遇到火焰、岩浆、爆炸等直接销毁。
     *
     * @param source 伤害来源
     * @param amount 伤害值
     * @return 是否造成了伤害
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(DamageTypeTags.IS_LIGHTNING)) {
            this.ejectPassengers();
            this.playSound(SoundEvents.GENERIC_BURN, 0.6F, 1.6F);
            this.dropCargo();
            this.discard();
            return true;
        }
        return false;
    }

    /**
     * 获取车内物品列表。
     *
     * @return 物品列表
     */
    public NonNullList<ItemStack> getCargo() {
        return this.cargo;
    }

    /**
     * 把车里存放的物品全部掉落到地上。
     */
    protected void dropCargo() {
        if (this.level().isClientSide) {
            return;
        }
        for (int i = 0; i < this.cargo.size(); i++) {
            ItemStack stack = this.cargo.get(i);
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack.copy());
                this.cargo.set(i, ItemStack.EMPTY);
            }
        }
    }

    /**
     * 判断是否参与碰撞。
     *
     * @return 恒为 true
     */
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    /**
     * 判断是否可被其他实体推动。
     *
     * @return 恒为 false，载具只响应驾驶输入
     */
    @Override
    public boolean isPushable() {
        return false;
    }

    /**
     * 判断是否还能再载一名乘客。
     *
     * @param passenger 待上车的实体
     * @return 车上没有乘客时为 true
     */
    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    /**
     * 获取控制载具的乘客。
     *
     * @return 第一名乘客，不是活体时返回 null
     */
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    /**
     * 计算乘客的乘坐位置。
     * <p>
     * 座位偏移按车体朝向旋转，转向时乘客随之移动。
     *
     * @param entity      乘客
     * @param dimensions  乘客尺寸
     * @param partialTick 插值刻
     * @return 座位相对实体原点的偏移
     */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
        return SEAT_OFFSET.yRot(-this.getYRot() * Mth.DEG_TO_RAD);
    }

    /**
     * 调整乘客位置。
     * <p>
     * 在父类定位之后叠加本刻的转向增量，让乘客跟着车体一起转。
     *
     * @param passenger 乘客
     * @param callback  位置回调
     */
    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        super.positionRider(passenger, callback);
        if (this.deltaRotation != 0.0F) {
            passenger.setYRot(passenger.getYRot() + this.deltaRotation);
            passenger.setYHeadRot(passenger.getYHeadRot() + this.deltaRotation);
            if (passenger instanceof LivingEntity living) {
                living.yBodyRot += this.deltaRotation;
                living.yBodyRotO += this.deltaRotation;
            }
        }
    }

    /**
     * 处理玩家右键交互。
     * <p>
     * 潜行时不响应；客户端直接返回成功以便播放动画，服务端尝试让玩家上车。
     *
     * @param player 玩家
     * @param hand   交互手
     * @return 交互结果
     */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    /**
     * 限制乘客转头范围。
     * <p>
     * 乘客头部相对车体的偏转被限制在正负 105 度内，身体朝向始终对齐车体。
     *
     * @param passenger 乘客
     */
    @Override
    public void onPassengerTurned(Entity passenger) {
        if (passenger instanceof LivingEntity living) {
            living.setYBodyRot(this.getYRot());
        }
        float delta = Mth.wrapDegrees(passenger.getYRot() - this.getYRot());
        float clamped = Mth.clamp(delta, -105.0F, 105.0F);
        passenger.yRotO += clamped - delta;
        passenger.setYRot(passenger.getYRot() + clamped - delta);
        passenger.setYHeadRot(passenger.getYRot());
    }

    /**
     * 处理位置插值。
     * <p>
     * 本地控制的载具以自身物理为准，不接受服务端插值，避免回弹。
     *
     * @param x     目标 X 坐标
     * @param y     目标 Y 坐标
     * @param z     目标 Z 坐标
     * @param yRot  目标偏航角
     * @param xRot  目标俯仰角
     * @param steps 插值步数
     */
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        if (this.isControlledByLocalInstance()) {
            return;
        }
        super.lerpTo(x, y, z, yRot, xRot, steps);
    }

    /**
     * 每刻更新。
     * <p>
     * 重置转向增量，有乘客时按输入驱动，最后统一应用物理。
     */
    @Override
    public void tick() {
        super.tick();
        this.deltaRotation = 0.0F;
        if (this.isVehicle()) {
            this.controlVehicle();
        }
        this.applyPhysics();
    }

    /**
     * 根据输入驱动转向与加速。
     * <p>
     * 转向直接改变车体偏航角并记录增量；加速沿车头方向叠加到当前速度上。
     */
    private void controlVehicle() {
        float turn = 0.0F;
        if (this.inputLeft) {
            turn -= 1.0F;
        }
        if (this.inputRight) {
            turn += 1.0F;
        }
        if (turn != 0.0F) {
            float yawStep = turn * TURN_SPEED;
            this.deltaRotation = yawStep;
            this.setYRot(this.getYRot() + yawStep);
            this.setYHeadRot(this.getYRot());
        }

        float acceleration = 0.0F;
        if (this.inputUp) {
            acceleration += ACCELERATION;
        }
        if (this.inputDown) {
            acceleration -= REVERSE_ACCELERATION;
        }
        if (acceleration != 0.0F) {
            double yaw = this.getYRot() * Mth.DEG_TO_RAD;
            Vec3 forward = new Vec3(-Mth.sin((float) yaw), 0.0D, Mth.cos((float) yaw));
            this.setDeltaMovement(this.getDeltaMovement().add(forward.scale(acceleration)));
        }
    }

    /**
     * 应用重力、摩擦与限速。
     * <p>
     * 先按当前速度移动，再按地面或空中摩擦衰减，最后把水平速度限制在最大值内。
     */
    private void applyPhysics() {
        Vec3 movement = this.getDeltaMovement();
        boolean onGround = this.onGround();

        double vy = onGround ? 0.0D : movement.y - GRAVITY;
        if (vy < -MAX_FALL_SPEED) {
            vy = -MAX_FALL_SPEED;
        }

        this.move(MoverType.SELF, new Vec3(movement.x, vy, movement.z));

        double drag = onGround ? GROUND_DRAG : AIR_DRAG;
        Vec3 updated = this.getDeltaMovement();
        double vx = updated.x * drag;
        double vz = updated.z * drag;
        // 落到地面的当刻把竖直速度清零，避免残留
        double newVy = this.onGround() ? 0.0D : vy;

        double speed = Math.sqrt(vx * vx + vz * vz);
        if (speed > MAX_SPEED) {
            double scale = MAX_SPEED / speed;
            vx *= scale;
            vz *= scale;
        }
        this.setDeltaMovement(vx, newVy, vz);
    }

    /**
     * 乘客下车时清空输入。
     * <p>
     * 避免残留的按键状态在下一次上车时继续生效。
     *
     * @param passenger 下车的乘客
     */
    @Override
    public void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        this.setInput(false, false, false, false);
    }
}
