package com.tequilacat.kaleidoscope_startale.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.damagesource.DamageSource;
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
 * 扭扭车载具。
 * <p>
 * 只有一个座位，靠左右两边交替扭动前进：按左键向左前方扭，按右键向右前方扭，
 * 两边一起按则直行。速度越高，车把的摆动与轮子的滚动越明显。
 */
public class WiggleCarEntity extends Entity {

    /** 每次扭动给的推力。 */
    private static final float PUSH_ACCELERATION = 0.045F;
    /** 每次扭动的转向角度（度）。 */
    private static final float TURN_PER_TICK = 5.0F;
    /** 蹬动过程中的地面摩擦。 */
    private static final double GROUND_DRAG = 0.90D;
    /** 松手后的地面摩擦。 */
    private static final double COAST_DRAG = 0.78D;
    /** 空中摩擦。 */
    private static final double AIR_DRAG = 0.98D;
    /** 最大水平速度。 */
    private static final double MAX_SPEED = 0.30D;
    /** 重力。 */
    private static final double GRAVITY = 0.08D;
    /** 低于该水平速度时直接停下，防止无限缓慢滑动。 */
    private static final double STOP_SPEED = 0.015D;
    /** 座位相对实体原点的偏移。 */
    private static final Vec3 SEAT_OFFSET = new Vec3(0.0D, 0.57D, -0.30D);
    /** 车轮每 tick 的滚动角度换算系数。 */
    private static final float WHEEL_SPIN_SCALE = 900.0F;
    /** 车把最大摆动角度（弧度，约 15 度）。 */
    private static final float MAX_HANDLE_TILT = 15.0F * Mth.DEG_TO_RAD;
    /** 车把摆动的平滑系数。 */
    private static final float HANDLE_TILT_LERP = 0.35F;
    /** 车内物品槽数量。 */
    public static final int CARGO_SIZE = 0;
    /** 下落速度上限（格/刻），避免无限加速。 */
    private static final double MAX_FALL_SPEED = 2.0D;

    /** 左转按键是否按下。 */
    private boolean inputLeft;
    /** 右转按键是否按下。 */
    private boolean inputRight;
    /** 本刻的转向增量，用于同步转动乘客。 */
    private float deltaRotation;
    /** 车轮累计滚动角度（度）。 */
    private float wheelAngle;
    /** 车把当前摆动角度（弧度）。 */
    private float handleTilt;
    /** 车内物品。 */
    private final NonNullList<ItemStack> cargo = NonNullList.withSize(CARGO_SIZE, ItemStack.EMPTY);

    /**
     * 构造扭扭车载具实体。
     * <p>
     * 标记为建筑方块，使载具可被放置在方块之上。
     *
     * @param entityType 实体类型
     * @param level      所在世界
     */
    public WiggleCarEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    /**
     * 写入驾驶输入。
     * <p>
     * 由客户端输入包调用，两个键同时按下时转向相互抵消，车把回正。
     *
     * @param inputLeft  左转
     * @param inputRight 右转
     */
    public void setInput(boolean inputLeft, boolean inputRight) {
        this.inputLeft = inputLeft;
        this.inputRight = inputRight;
    }

    /**
     * 更新车把的摆动角度，向目标角度平滑过渡。
     */
    private void updateHandleTilt() {
        float target;
        if (this.inputLeft == this.inputRight) {
            target = 0.0F;
        } else {
            target = (this.inputRight ? 1.0F : -1.0F) * MAX_HANDLE_TILT;
        }
        this.handleTilt += (target - this.handleTilt) * HANDLE_TILT_LERP;
    }

    /**
     * 获取车把的摆动角度。
     *
     * @return 车把摆动角度（弧度）
     */
    public float handleTilt() {
        return this.handleTilt;
    }

    /**
     * 获取车轮的滚动角度。
     *
     * @return 车轮滚动角度（弧度）
     */
    public float wheelRotation() {
        return this.wheelAngle * Mth.DEG_TO_RAD;
    }

    /**
     * 获取当前水平速度。
     *
     * @return 水平速度（格/刻）
     */
    public double horizontalSpeed() {
        return this.getDeltaMovement().horizontalDistance();
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
        // 没有需要同步的字段
    }

    /**
     * 读取存档数据。
     * <p>
     * 从数据标签恢复车内物品，槽位数量由 CARGO_SIZE 决定。
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
     * 把车内物品存入数据标签。
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
     * 获取车内物品列表。
     *
     * @return 物品列表
     */
    public NonNullList<ItemStack> getCargo() {
        return this.cargo;
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
     * 每刻更新。
     * <p>
     * 重置转向增量，有乘客时按输入驱动，随后更新车把摆动并应用物理。
     */
    @Override
    public void tick() {
        super.tick();
        this.deltaRotation = 0.0F;
        if (this.isVehicle()) {
            this.control();
        }
        this.updateHandleTilt();
        this.applyPhysics();
    }

    /**
     * 根据输入扭动前进。
     * <p>
     * 每按一侧就同时产生转向与推力，两侧同按则转向相互抵消、只保留推力，
     * 表现为直行。推力沿车头方向叠加到当前速度上。
     */
    private void control() {
        if (this.inputLeft) {
            this.deltaRotation -= TURN_PER_TICK;
        }
        if (this.inputRight) {
            this.deltaRotation += TURN_PER_TICK;
        }
        if (this.deltaRotation != 0.0F) {
            this.setYRot(this.getYRot() + this.deltaRotation);
            this.setYHeadRot(this.getYRot());
        }
        if (this.inputLeft || this.inputRight) {
            double yaw = this.getYRot() * Mth.DEG_TO_RAD;
            Vec3 forward = new Vec3(-Mth.sin((float) yaw), 0.0D, Mth.cos((float) yaw));
            this.setDeltaMovement(this.getDeltaMovement().add(forward.scale(PUSH_ACCELERATION)));
        }
    }

    /**
     * 应用重力、摩擦与限速。
     * <p>
     * 蹬动时用较大的摩擦快速衰减，松手后换成滑行摩擦；低速时直接停下，
     * 避免无限缓慢滑动。车轮滚动角度随水平速度累加。
     */
    private void applyPhysics() {
        Vec3 movement = this.getDeltaMovement();
        boolean onGround = this.onGround();

        // 竖直速度必须逐刻累积，否则每刻都是同一个值，表现为恒速下落（像缓降）
        double vy = onGround ? 0.0D : movement.y - GRAVITY;
        if (vy < -MAX_FALL_SPEED) {
            vy = -MAX_FALL_SPEED;
        }

        this.move(MoverType.SELF, new Vec3(movement.x, vy, movement.z));

        boolean pushing = this.inputLeft || this.inputRight;
        double drag = onGround ? (pushing ? GROUND_DRAG : COAST_DRAG) : AIR_DRAG;
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
            speed = MAX_SPEED;
        }
        if (onGround && !pushing && speed < STOP_SPEED) {
            vx = 0.0D;
            vz = 0.0D;
            speed = 0.0D;
        }
        this.setDeltaMovement(vx, newVy, vz);
        this.wheelAngle += (float) speed * WHEEL_SPIN_SCALE;
        if (this.wheelAngle > 360.0F) {
            this.wheelAngle -= 360.0F;
        }
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
        this.setInput(false, false);
    }
}
