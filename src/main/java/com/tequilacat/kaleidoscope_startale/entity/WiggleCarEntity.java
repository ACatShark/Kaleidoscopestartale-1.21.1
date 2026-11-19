package com.tequilacat.kaleidoscope_startale.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
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
    private static final Vec3 SEAT_OFFSET = new Vec3(0.0D, 0.57D, 0.15D);
    /** 车轮每 tick 的滚动角度换算系数。 */
    private static final float WHEEL_SPIN_SCALE = 900.0F;

    private boolean inputLeft;
    private boolean inputRight;
    private float deltaRotation;
    private float wheelAngle;

    public WiggleCarEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    public void setInput(boolean inputLeft, boolean inputRight) {
        this.inputLeft = inputLeft;
        this.inputRight = inputRight;
    }

    public float handleTilt() {
        if (!this.inputLeft && !this.inputRight) {
            return 0.0F;
        }
        float sign = this.inputRight ? 1.0F : -1.0F;
        if (this.inputLeft && this.inputRight) {
            return 0.0F;
        }
        return sign * 15.0F * Mth.DEG_TO_RAD;
    }

    public float wheelRotation() {
        return this.wheelAngle * Mth.DEG_TO_RAD;
    }

    public double horizontalSpeed() {
        return this.getDeltaMovement().horizontalDistance();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // 没有需要同步的字段
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        // 载具没有额外数据
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        // 载具没有额外数据
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
        return SEAT_OFFSET.yRot(-this.getYRot() * Mth.DEG_TO_RAD);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        if (this.isControlledByLocalInstance()) {
            return;
        }
        super.lerpTo(x, y, z, yRot, xRot, steps);
    }

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

    @Override
    public void tick() {
        super.tick();
        this.deltaRotation = 0.0F;
        if (this.isVehicle()) {
            this.control();
        }
        this.applyPhysics();
    }

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

    private void applyPhysics() {
        Vec3 movement = this.getDeltaMovement();
        boolean onGround = this.onGround();
        double dy = onGround ? 0.0D : movement.y - GRAVITY;
        this.move(MoverType.SELF, new Vec3(movement.x, dy, movement.z));

        boolean pushing = this.inputLeft || this.inputRight;
        double drag = onGround ? (pushing ? GROUND_DRAG : COAST_DRAG) : AIR_DRAG;
        Vec3 updated = this.getDeltaMovement();
        double vx = updated.x * drag;
        double vz = updated.z * drag;
        double vy = onGround ? 0.0D : updated.y;

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
        this.setDeltaMovement(vx, vy, vz);
        this.wheelAngle += (float) speed * WHEEL_SPIN_SCALE;
        if (this.wheelAngle > 360.0F) {
            this.wheelAngle -= 360.0F;
        }
    }

    @Override
    public void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        this.setInput(false, false);
    }
}
