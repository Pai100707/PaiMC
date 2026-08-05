/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.vehicle.boat;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.BlockUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBoat
extends VehicleEntity
implements Leashable {
    private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_LEFT = SynchedEntityData.defineId(AbstractBoat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_RIGHT = SynchedEntityData.defineId(AbstractBoat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ID_BUBBLE_TIME = SynchedEntityData.defineId(AbstractBoat.class, EntityDataSerializers.INT);
    public static final int PADDLE_LEFT = 0;
    public static final int PADDLE_RIGHT = 1;
    private static final int TIME_TO_EJECT = 60;
    private static final float PADDLE_SPEED = 0.3926991f;
    public static final double PADDLE_SOUND_TIME = 0.7853981852531433;
    public static final int BUBBLE_TIME = 60;
    private final float[] paddlePositions = new float[2];
    private float outOfControlTicks;
    private float deltaRotation;
    private final InterpolationHandler interpolation = new InterpolationHandler((Entity)this, 3);
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputUp;
    private boolean inputDown;
    private double waterLevel;
    private float landFriction;
    private Status status;
    private Status oldStatus;
    private double lastYd;
    private boolean isAboveBubbleColumn;
    private boolean bubbleColumnDirectionIsDown;
    private float bubbleMultiplier;
    private float bubbleAngle;
    private float bubbleAngleO;
    private @Nullable Leashable.LeashData leashData;
    private final Supplier<Item> dropItem;

    public AbstractBoat(EntityType<? extends AbstractBoat> $$0, Level $$1, Supplier<Item> $$2) {
        super($$0, $$1);
        this.dropItem = $$2;
        this.blocksBuilding = true;
    }

    public void setInitialPos(double $$0, double $$1, double $$2) {
        this.setPos($$0, $$1, $$2);
        this.xo = $$0;
        this.yo = $$1;
        this.zo = $$2;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_ID_PADDLE_LEFT, false);
        $$0.define(DATA_ID_PADDLE_RIGHT, false);
        $$0.define(DATA_ID_BUBBLE_TIME, 0);
    }

    @Override
    public boolean canCollideWith(Entity $$0) {
        return AbstractBoat.canVehicleCollide(this, $$0);
    }

    public static boolean canVehicleCollide(Entity $$0, Entity $$1) {
        return ($$1.canBeCollidedWith($$0) || $$1.isPushable()) && !$$0.isPassengerOfSameVehicle($$1);
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity $$0) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public Vec3 getRelativePortalPosition(Direction.Axis $$0, BlockUtil.FoundRectangle $$1) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition($$0, $$1));
    }

    protected abstract double rideHeight(EntityDimensions var1);

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity $$0, EntityDimensions $$1, float $$2) {
        float $$3 = this.getSinglePassengerXOffset();
        if (this.getPassengers().size() > 1) {
            int $$4 = this.getPassengers().indexOf($$0);
            $$3 = $$4 == 0 ? 0.2f : -0.6f;
            if ($$0 instanceof Animal) {
                $$3 += 0.2f;
            }
        }
        return new Vec3(0.0, this.rideHeight($$1), $$3).yRot(-this.getYRot() * ((float)Math.PI / 180));
    }

    @Override
    public void onAboveBubbleColumn(boolean $$0, BlockPos $$1) {
        if (this.level() instanceof ServerLevel) {
            this.isAboveBubbleColumn = true;
            this.bubbleColumnDirectionIsDown = $$0;
            if (this.getBubbleTime() == 0) {
                this.setBubbleTime(60);
            }
        }
        if (!this.isUnderWater() && this.random.nextInt(100) == 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), this.getSwimSplashSound(), this.getSoundSource(), 1.0f, 0.8f + 0.4f * this.random.nextFloat(), false);
            this.level().addParticle(ParticleTypes.SPLASH, this.getX() + (double)this.random.nextFloat(), this.getY() + 0.7, this.getZ() + (double)this.random.nextFloat(), 0.0, 0.0, 0.0);
            this.gameEvent(GameEvent.SPLASH, this.getControllingPassenger());
        }
    }

    @Override
    public void push(Entity $$0) {
        if ($$0 instanceof AbstractBoat) {
            if ($$0.getBoundingBox().minY < this.getBoundingBox().maxY) {
                super.push($$0);
            }
        } else if ($$0.getBoundingBox().minY <= this.getBoundingBox().minY) {
            super.push($$0);
        }
    }

    @Override
    public void animateHurt(float $$0) {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() * 11.0f);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolation;
    }

    @Override
    public Direction getMotionDirection() {
        return this.getDirection().getClockWise();
    }

    @Override
    public void tick() {
        this.oldStatus = this.status;
        this.status = this.getStatus();
        this.outOfControlTicks = this.status == Status.UNDER_WATER || this.status == Status.UNDER_FLOWING_WATER ? (this.outOfControlTicks += 1.0f) : 0.0f;
        if (!this.level().isClientSide() && this.outOfControlTicks >= 60.0f) {
            this.ejectPassengers();
        }
        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }
        if (this.getDamage() > 0.0f) {
            this.setDamage(this.getDamage() - 1.0f);
        }
        super.tick();
        this.interpolation.interpolate();
        if (this.isLocalInstanceAuthoritative()) {
            if (!(this.getFirstPassenger() instanceof Player)) {
                this.setPaddleState(false, false);
            }
            this.floatBoat();
            if (this.level().isClientSide()) {
                this.controlBoat();
                this.level().sendPacketToServer(new ServerboundPaddleBoatPacket(this.getPaddleState(0), this.getPaddleState(1)));
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.applyEffectsFromBlocks();
        this.applyEffectsFromBlocks();
        this.tickBubbleColumn();
        for (int $$0 = 0; $$0 <= 1; ++$$0) {
            if (this.getPaddleState($$0)) {
                SoundEvent $$1;
                if (!this.isSilent() && (double)(this.paddlePositions[$$0] % ((float)Math.PI * 2)) <= 0.7853981852531433 && (double)((this.paddlePositions[$$0] + 0.3926991f) % ((float)Math.PI * 2)) >= 0.7853981852531433 && ($$1 = this.getPaddleSound()) != null) {
                    Vec3 $$2 = this.getViewVector(1.0f);
                    double $$3 = $$0 == 1 ? -$$2.z : $$2.z;
                    double $$4 = $$0 == 1 ? $$2.x : -$$2.x;
                    this.level().playSound(null, this.getX() + $$3, this.getY(), this.getZ() + $$4, $$1, this.getSoundSource(), 1.0f, 0.8f + 0.4f * this.random.nextFloat());
                }
                int n = $$0;
                this.paddlePositions[n] = this.paddlePositions[n] + 0.3926991f;
                continue;
            }
            this.paddlePositions[$$0] = 0.0f;
        }
        List<Entity> $$5 = this.level().getEntities(this, this.getBoundingBox().inflate(0.2f, -0.01f, 0.2f), EntitySelector.pushableBy(this));
        if (!$$5.isEmpty()) {
            boolean $$6 = !this.level().isClientSide() && !(this.getControllingPassenger() instanceof Player);
            for (Entity $$7 : $$5) {
                if ($$7.hasPassenger(this)) continue;
                if ($$6 && this.getPassengers().size() < this.getMaxPassengers() && !$$7.isPassenger() && this.hasEnoughSpaceFor($$7) && $$7 instanceof LivingEntity && !$$7.getType().is(EntityTypeTags.CANNOT_BE_PUSHED_ONTO_BOATS)) {
                    $$7.startRiding(this);
                    continue;
                }
                this.push($$7);
            }
        }
    }

    private void tickBubbleColumn() {
        if (this.level().isClientSide()) {
            int $$02 = this.getBubbleTime();
            this.bubbleMultiplier = $$02 > 0 ? (this.bubbleMultiplier += 0.05f) : (this.bubbleMultiplier -= 0.1f);
            this.bubbleMultiplier = Mth.clamp(this.bubbleMultiplier, 0.0f, 1.0f);
            this.bubbleAngleO = this.bubbleAngle;
            this.bubbleAngle = 10.0f * (float)Math.sin(0.5 * (double)this.tickCount) * this.bubbleMultiplier;
        } else {
            int $$1;
            if (!this.isAboveBubbleColumn) {
                this.setBubbleTime(0);
            }
            if (($$1 = this.getBubbleTime()) > 0) {
                this.setBubbleTime(--$$1);
                int $$2 = 60 - $$1 - 1;
                if ($$2 > 0 && $$1 == 0) {
                    this.setBubbleTime(0);
                    Vec3 $$3 = this.getDeltaMovement();
                    if (this.bubbleColumnDirectionIsDown) {
                        this.setDeltaMovement($$3.add(0.0, -0.7, 0.0));
                        this.ejectPassengers();
                    } else {
                        this.setDeltaMovement($$3.x, this.hasPassenger((Entity $$0) -> $$0 instanceof Player) ? 2.7 : 0.6, $$3.z);
                    }
                }
                this.isAboveBubbleColumn = false;
            }
        }
    }

    protected @Nullable SoundEvent getPaddleSound() {
        return switch (this.getStatus().ordinal()) {
            case 0, 1, 2 -> SoundEvents.BOAT_PADDLE_WATER;
            case 3 -> SoundEvents.BOAT_PADDLE_LAND;
            default -> null;
        };
    }

    public void setPaddleState(boolean $$0, boolean $$1) {
        this.entityData.set(DATA_ID_PADDLE_LEFT, $$0);
        this.entityData.set(DATA_ID_PADDLE_RIGHT, $$1);
    }

    public float getRowingTime(int $$0, float $$1) {
        if (this.getPaddleState($$0)) {
            return Mth.clampedLerp($$1, this.paddlePositions[$$0] - 0.3926991f, this.paddlePositions[$$0]);
        }
        return 0.0f;
    }

    @Override
    public @Nullable Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData $$0) {
        this.leashData = $$0;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.88f * this.getBbHeight(), 0.64f * this.getBbWidth());
    }

    @Override
    public boolean supportQuadLeash() {
        return true;
    }

    @Override
    public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.0, 0.64, 0.382, 0.88);
    }

    private Status getStatus() {
        Status $$0 = this.isUnderwater();
        if ($$0 != null) {
            this.waterLevel = this.getBoundingBox().maxY;
            return $$0;
        }
        if (this.checkInWater()) {
            return Status.IN_WATER;
        }
        float $$1 = this.getGroundFriction();
        if ($$1 > 0.0f) {
            this.landFriction = $$1;
            return Status.ON_LAND;
        }
        return Status.IN_AIR;
    }

    public float getWaterLevelAbove() {
        AABB $$0 = this.getBoundingBox();
        int $$1 = Mth.floor($$0.minX);
        int $$2 = Mth.ceil($$0.maxX);
        int $$3 = Mth.floor($$0.maxY);
        int $$4 = Mth.ceil($$0.maxY - this.lastYd);
        int $$5 = Mth.floor($$0.minZ);
        int $$6 = Mth.ceil($$0.maxZ);
        BlockPos.MutableBlockPos $$7 = new BlockPos.MutableBlockPos();
        block0: for (int $$8 = $$3; $$8 < $$4; ++$$8) {
            float $$9 = 0.0f;
            for (int $$10 = $$1; $$10 < $$2; ++$$10) {
                for (int $$11 = $$5; $$11 < $$6; ++$$11) {
                    $$7.set($$10, $$8, $$11);
                    FluidState $$12 = this.level().getFluidState($$7);
                    if ($$12.is(FluidTags.WATER)) {
                        $$9 = Math.max($$9, $$12.getHeight(this.level(), $$7));
                    }
                    if ($$9 >= 1.0f) continue block0;
                }
            }
            if (!($$9 < 1.0f)) continue;
            return (float)$$7.getY() + $$9;
        }
        return $$4 + 1;
    }

    public float getGroundFriction() {
        AABB $$0 = this.getBoundingBox();
        AABB $$1 = new AABB($$0.minX, $$0.minY - 0.001, $$0.minZ, $$0.maxX, $$0.minY, $$0.maxZ);
        int $$2 = Mth.floor($$1.minX) - 1;
        int $$3 = Mth.ceil($$1.maxX) + 1;
        int $$4 = Mth.floor($$1.minY) - 1;
        int $$5 = Mth.ceil($$1.maxY) + 1;
        int $$6 = Mth.floor($$1.minZ) - 1;
        int $$7 = Mth.ceil($$1.maxZ) + 1;
        VoxelShape $$8 = Shapes.create($$1);
        float $$9 = 0.0f;
        int $$10 = 0;
        BlockPos.MutableBlockPos $$11 = new BlockPos.MutableBlockPos();
        for (int $$12 = $$2; $$12 < $$3; ++$$12) {
            for (int $$13 = $$6; $$13 < $$7; ++$$13) {
                int $$14 = ($$12 == $$2 || $$12 == $$3 - 1 ? 1 : 0) + ($$13 == $$6 || $$13 == $$7 - 1 ? 1 : 0);
                if ($$14 == 2) continue;
                for (int $$15 = $$4; $$15 < $$5; ++$$15) {
                    if ($$14 > 0 && ($$15 == $$4 || $$15 == $$5 - 1)) continue;
                    $$11.set($$12, $$15, $$13);
                    BlockState $$16 = this.level().getBlockState($$11);
                    if ($$16.getBlock() instanceof WaterlilyBlock || !Shapes.joinIsNotEmpty($$16.getCollisionShape(this.level(), $$11).move($$11), $$8, BooleanOp.AND)) continue;
                    $$9 += $$16.getBlock().getFriction();
                    ++$$10;
                }
            }
        }
        return $$9 / (float)$$10;
    }

    private boolean checkInWater() {
        AABB $$0 = this.getBoundingBox();
        int $$1 = Mth.floor($$0.minX);
        int $$2 = Mth.ceil($$0.maxX);
        int $$3 = Mth.floor($$0.minY);
        int $$4 = Mth.ceil($$0.minY + 0.001);
        int $$5 = Mth.floor($$0.minZ);
        int $$6 = Mth.ceil($$0.maxZ);
        boolean $$7 = false;
        this.waterLevel = -1.7976931348623157E308;
        BlockPos.MutableBlockPos $$8 = new BlockPos.MutableBlockPos();
        for (int $$9 = $$1; $$9 < $$2; ++$$9) {
            for (int $$10 = $$3; $$10 < $$4; ++$$10) {
                for (int $$11 = $$5; $$11 < $$6; ++$$11) {
                    $$8.set($$9, $$10, $$11);
                    FluidState $$12 = this.level().getFluidState($$8);
                    if (!$$12.is(FluidTags.WATER)) continue;
                    float $$13 = (float)$$10 + $$12.getHeight(this.level(), $$8);
                    this.waterLevel = Math.max((double)$$13, this.waterLevel);
                    $$7 |= $$0.minY < (double)$$13;
                }
            }
        }
        return $$7;
    }

    private @Nullable Status isUnderwater() {
        AABB $$0 = this.getBoundingBox();
        double $$1 = $$0.maxY + 0.001;
        int $$2 = Mth.floor($$0.minX);
        int $$3 = Mth.ceil($$0.maxX);
        int $$4 = Mth.floor($$0.maxY);
        int $$5 = Mth.ceil($$1);
        int $$6 = Mth.floor($$0.minZ);
        int $$7 = Mth.ceil($$0.maxZ);
        boolean $$8 = false;
        BlockPos.MutableBlockPos $$9 = new BlockPos.MutableBlockPos();
        for (int $$10 = $$2; $$10 < $$3; ++$$10) {
            for (int $$11 = $$4; $$11 < $$5; ++$$11) {
                for (int $$12 = $$6; $$12 < $$7; ++$$12) {
                    $$9.set($$10, $$11, $$12);
                    FluidState $$13 = this.level().getFluidState($$9);
                    if (!$$13.is(FluidTags.WATER) || !($$1 < (double)((float)$$9.getY() + $$13.getHeight(this.level(), $$9)))) continue;
                    if ($$13.isSource()) {
                        $$8 = true;
                        continue;
                    }
                    return Status.UNDER_FLOWING_WATER;
                }
            }
        }
        return $$8 ? Status.UNDER_WATER : null;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    private void floatBoat() {
        double $$0 = -this.getGravity();
        double $$1 = 0.0;
        float $$2 = 0.05f;
        if (this.oldStatus == Status.IN_AIR && this.status != Status.IN_AIR && this.status != Status.ON_LAND) {
            this.waterLevel = this.getY(1.0);
            double $$3 = (double)(this.getWaterLevelAbove() - this.getBbHeight()) + 0.101;
            if (this.level().noCollision(this, this.getBoundingBox().move(0.0, $$3 - this.getY(), 0.0))) {
                this.setPos(this.getX(), $$3, this.getZ());
                this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                this.lastYd = 0.0;
            }
            this.status = Status.IN_WATER;
        } else {
            if (this.status == Status.IN_WATER) {
                $$1 = (this.waterLevel - this.getY()) / (double)this.getBbHeight();
                $$2 = 0.9f;
            } else if (this.status == Status.UNDER_FLOWING_WATER) {
                $$0 = -7.0E-4;
                $$2 = 0.9f;
            } else if (this.status == Status.UNDER_WATER) {
                $$1 = 0.01f;
                $$2 = 0.45f;
            } else if (this.status == Status.IN_AIR) {
                $$2 = 0.9f;
            } else if (this.status == Status.ON_LAND) {
                $$2 = this.landFriction;
                if (this.getControllingPassenger() instanceof Player) {
                    this.landFriction /= 2.0f;
                }
            }
            Vec3 $$4 = this.getDeltaMovement();
            this.setDeltaMovement($$4.x * (double)$$2, $$4.y + $$0, $$4.z * (double)$$2);
            this.deltaRotation *= $$2;
            if ($$1 > 0.0) {
                Vec3 $$5 = this.getDeltaMovement();
                this.setDeltaMovement($$5.x, ($$5.y + $$1 * (this.getDefaultGravity() / 0.65)) * 0.75, $$5.z);
            }
        }
    }

    private void controlBoat() {
        if (!this.isVehicle()) {
            return;
        }
        float $$0 = 0.0f;
        if (this.inputLeft) {
            this.deltaRotation -= 1.0f;
        }
        if (this.inputRight) {
            this.deltaRotation += 1.0f;
        }
        if (this.inputRight != this.inputLeft && !this.inputUp && !this.inputDown) {
            $$0 += 0.005f;
        }
        this.setYRot(this.getYRot() + this.deltaRotation);
        if (this.inputUp) {
            $$0 += 0.04f;
        }
        if (this.inputDown) {
            $$0 -= 0.005f;
        }
        this.setDeltaMovement(this.getDeltaMovement().add(Mth.sin(-this.getYRot() * ((float)Math.PI / 180)) * $$0, 0.0, Mth.cos(this.getYRot() * ((float)Math.PI / 180)) * $$0));
        this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
    }

    protected float getSinglePassengerXOffset() {
        return 0.0f;
    }

    public boolean hasEnoughSpaceFor(Entity $$0) {
        return $$0.getBbWidth() < this.getBbWidth();
    }

    @Override
    protected void positionRider(Entity $$0, Entity.MoveFunction $$1) {
        super.positionRider($$0, $$1);
        if ($$0.getType().is(EntityTypeTags.CAN_TURN_IN_BOATS)) {
            return;
        }
        $$0.setYRot($$0.getYRot() + this.deltaRotation);
        $$0.setYHeadRot($$0.getYHeadRot() + this.deltaRotation);
        this.clampRotation($$0);
        if ($$0 instanceof Animal && this.getPassengers().size() == this.getMaxPassengers()) {
            int $$2 = $$0.getId() % 2 == 0 ? 90 : 270;
            $$0.setYBodyRot(((Animal)$$0).yBodyRot + (float)$$2);
            $$0.setYHeadRot($$0.getYHeadRot() + (float)$$2);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity $$0) {
        Vec3 $$1 = AbstractBoat.getCollisionHorizontalEscapeVector(this.getBbWidth() * Mth.SQRT_OF_TWO, $$0.getBbWidth(), $$0.getYRot());
        double $$2 = this.getX() + $$1.x;
        double $$3 = this.getZ() + $$1.z;
        BlockPos $$4 = BlockPos.containing($$2, this.getBoundingBox().maxY, $$3);
        BlockPos $$5 = $$4.below();
        if (!this.level().isWaterAt($$5)) {
            double $$8;
            ArrayList $$6 = Lists.newArrayList();
            double $$7 = this.level().getBlockFloorHeight($$4);
            if (DismountHelper.isBlockFloorValid($$7)) {
                $$6.add(new Vec3($$2, (double)$$4.getY() + $$7, $$3));
            }
            if (DismountHelper.isBlockFloorValid($$8 = this.level().getBlockFloorHeight($$5))) {
                $$6.add(new Vec3($$2, (double)$$5.getY() + $$8, $$3));
            }
            for (Pose $$9 : $$0.getDismountPoses()) {
                for (Vec3 $$10 : $$6) {
                    if (!DismountHelper.canDismountTo(this.level(), $$10, $$0, $$9)) continue;
                    $$0.setPose($$9);
                    return $$10;
                }
            }
        }
        return super.getDismountLocationForPassenger($$0);
    }

    protected void clampRotation(Entity $$0) {
        $$0.setYBodyRot(this.getYRot());
        float $$1 = Mth.wrapDegrees($$0.getYRot() - this.getYRot());
        float $$2 = Mth.clamp($$1, -105.0f, 105.0f);
        $$0.yRotO += $$2 - $$1;
        $$0.setYRot($$0.getYRot() + $$2 - $$1);
        $$0.setYHeadRot($$0.getYRot());
    }

    @Override
    public void onPassengerTurned(Entity $$0) {
        this.clampRotation($$0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        this.writeLeashData($$0, this.leashData);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.readLeashData($$0);
    }

    @Override
    public InteractionResult interact(Player $$0, InteractionHand $$1) {
        InteractionResult $$2 = super.interact($$0, $$1);
        if ($$2 != InteractionResult.PASS) {
            return $$2;
        }
        if (!$$0.isSecondaryUseActive() && this.outOfControlTicks < 60.0f && (this.level().isClientSide() || $$0.startRiding(this))) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void remove(Entity.RemovalReason $$0) {
        if (!this.level().isClientSide() && $$0.shouldDestroy() && this.isLeashed()) {
            this.dropLeash();
        }
        super.remove($$0);
    }

    @Override
    protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
        this.lastYd = this.getDeltaMovement().y;
        if (this.isPassenger()) {
            return;
        }
        if ($$1) {
            this.resetFallDistance();
        } else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && $$0 < 0.0) {
            this.fallDistance -= (double)((float)$$0);
        }
    }

    public boolean getPaddleState(int $$0) {
        return this.entityData.get($$0 == 0 ? DATA_ID_PADDLE_LEFT : DATA_ID_PADDLE_RIGHT) != false && this.getControllingPassenger() != null;
    }

    private void setBubbleTime(int $$0) {
        this.entityData.set(DATA_ID_BUBBLE_TIME, $$0);
    }

    private int getBubbleTime() {
        return this.entityData.get(DATA_ID_BUBBLE_TIME);
    }

    public float getBubbleAngle(float $$0) {
        return Mth.lerp($$0, this.bubbleAngleO, this.bubbleAngle);
    }

    @Override
    protected boolean canAddPassenger(Entity $$0) {
        return this.getPassengers().size() < this.getMaxPassengers() && !this.isEyeInFluid(FluidTags.WATER);
    }

    protected int getMaxPassengers() {
        return 2;
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        LivingEntity $$0;
        Entity entity = this.getFirstPassenger();
        return entity instanceof LivingEntity ? ($$0 = (LivingEntity)entity) : super.getControllingPassenger();
    }

    public void setInput(boolean $$0, boolean $$1, boolean $$2, boolean $$3) {
        this.inputLeft = $$0;
        this.inputRight = $$1;
        this.inputUp = $$2;
        this.inputDown = $$3;
    }

    @Override
    public boolean isUnderWater() {
        return this.status == Status.UNDER_WATER || this.status == Status.UNDER_FLOWING_WATER;
    }

    @Override
    protected final Item getDropItem() {
        return this.dropItem.get();
    }

    @Override
    public final ItemStack getPickResult() {
        return new ItemStack(this.dropItem.get());
    }

    public static enum Status {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR;

    }
}

