/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.google.common.collect.UnmodifiableIterator
 *  com.mojang.datafixers.util.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.vehicle.minecart;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.BlockUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class AbstractMinecart
extends VehicleEntity {
    private static final Vec3 LOWERED_PASSENGER_ATTACHMENT = new Vec3(0.0, 0.0, 0.0);
    private static final EntityDataAccessor<Optional<BlockState>> DATA_ID_CUSTOM_DISPLAY_BLOCK = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);
    private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_OFFSET = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final ImmutableMap<Pose, ImmutableList<Integer>> POSE_DISMOUNT_HEIGHTS = ImmutableMap.of((Object)Pose.STANDING, (Object)ImmutableList.of((Object)0, (Object)1, (Object)-1), (Object)Pose.CROUCHING, (Object)ImmutableList.of((Object)0, (Object)1, (Object)-1), (Object)Pose.SWIMMING, (Object)ImmutableList.of((Object)0, (Object)1));
    protected static final float WATER_SLOWDOWN_FACTOR = 0.95f;
    private static final boolean DEFAULT_FLIPPED_ROTATION = false;
    private boolean onRails;
    private boolean flipped = false;
    private final MinecartBehavior behavior;
    private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS = Maps.newEnumMap((Map)((Map)Util.make(() -> {
        Vec3i $$0 = Direction.WEST.getUnitVec3i();
        Vec3i $$1 = Direction.EAST.getUnitVec3i();
        Vec3i $$2 = Direction.NORTH.getUnitVec3i();
        Vec3i $$3 = Direction.SOUTH.getUnitVec3i();
        Vec3i $$4 = $$0.below();
        Vec3i $$5 = $$1.below();
        Vec3i $$6 = $$2.below();
        Vec3i $$7 = $$3.below();
        return ImmutableMap.of((Object)RailShape.NORTH_SOUTH, (Object)Pair.of((Object)$$2, (Object)$$3), (Object)RailShape.EAST_WEST, (Object)Pair.of((Object)$$0, (Object)$$1), (Object)RailShape.ASCENDING_EAST, (Object)Pair.of((Object)$$4, (Object)$$1), (Object)RailShape.ASCENDING_WEST, (Object)Pair.of((Object)$$0, (Object)$$5), (Object)RailShape.ASCENDING_NORTH, (Object)Pair.of((Object)$$2, (Object)$$7), (Object)RailShape.ASCENDING_SOUTH, (Object)Pair.of((Object)$$6, (Object)$$3), (Object)RailShape.SOUTH_EAST, (Object)Pair.of((Object)$$3, (Object)$$1), (Object)RailShape.SOUTH_WEST, (Object)Pair.of((Object)$$3, (Object)$$0), (Object)RailShape.NORTH_WEST, (Object)Pair.of((Object)$$2, (Object)$$0), (Object)RailShape.NORTH_EAST, (Object)Pair.of((Object)$$2, (Object)$$1));
    })));

    protected AbstractMinecart(EntityType<?> $$0, Level $$1) {
        super($$0, $$1);
        this.blocksBuilding = true;
        this.behavior = AbstractMinecart.useExperimentalMovement($$1) ? new NewMinecartBehavior(this) : new OldMinecartBehavior(this);
    }

    protected AbstractMinecart(EntityType<?> $$0, Level $$1, double $$2, double $$3, double $$4) {
        this($$0, $$1);
        this.setInitialPos($$2, $$3, $$4);
    }

    public void setInitialPos(double $$0, double $$1, double $$2) {
        this.setPos($$0, $$1, $$2);
        this.xo = $$0;
        this.yo = $$1;
        this.zo = $$2;
    }

    public static <T extends AbstractMinecart> @Nullable T createMinecart(Level $$0, double $$1, double $$2, double $$3, EntityType<T> $$4, EntitySpawnReason $$5, ItemStack $$6, @Nullable Player $$7) {
        AbstractMinecart $$8 = (AbstractMinecart)$$4.create($$0, $$5);
        if ($$8 != null) {
            $$8.setInitialPos($$1, $$2, $$3);
            EntityType.createDefaultStackConfig($$0, $$6, $$7).accept($$8);
            MinecartBehavior minecartBehavior = $$8.getBehavior();
            if (minecartBehavior instanceof NewMinecartBehavior) {
                NewMinecartBehavior $$9 = (NewMinecartBehavior)minecartBehavior;
                BlockPos $$10 = $$8.getCurrentBlockPosOrRailBelow();
                BlockState $$11 = $$0.getBlockState($$10);
                $$9.adjustToRails($$10, $$11, true);
            }
        }
        return (T)$$8;
    }

    public MinecartBehavior getBehavior() {
        return this.behavior;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_ID_CUSTOM_DISPLAY_BLOCK, Optional.empty());
        $$0.define(DATA_ID_DISPLAY_OFFSET, this.getDefaultDisplayOffset());
    }

    @Override
    public boolean canCollideWith(Entity $$0) {
        return AbstractBoat.canVehicleCollide(this, $$0);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public Vec3 getRelativePortalPosition(Direction.Axis $$0, BlockUtil.FoundRectangle $$1) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition($$0, $$1));
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity $$0, EntityDimensions $$1, float $$2) {
        boolean $$3;
        boolean bl = $$3 = $$0 instanceof Villager || $$0 instanceof WanderingTrader;
        if ($$3) {
            return LOWERED_PASSENGER_ATTACHMENT;
        }
        return super.getPassengerAttachmentPoint($$0, $$1, $$2);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity $$02) {
        Direction $$1 = this.getMotionDirection();
        if ($$1.getAxis() == Direction.Axis.Y) {
            return super.getDismountLocationForPassenger($$02);
        }
        int[][] $$2 = DismountHelper.offsetsForDirection($$1);
        BlockPos $$3 = this.blockPosition();
        BlockPos.MutableBlockPos $$4 = new BlockPos.MutableBlockPos();
        ImmutableList<Pose> $$5 = $$02.getDismountPoses();
        for (Pose $$6 : $$5) {
            EntityDimensions $$7 = $$02.getDimensions($$6);
            float $$8 = Math.min($$7.width(), 1.0f) / 2.0f;
            UnmodifiableIterator unmodifiableIterator = ((ImmutableList)POSE_DISMOUNT_HEIGHTS.get((Object)$$6)).iterator();
            while (unmodifiableIterator.hasNext()) {
                int $$9 = (Integer)unmodifiableIterator.next();
                for (int[] $$10 : $$2) {
                    $$4.set($$3.getX() + $$10[0], $$3.getY() + $$9, $$3.getZ() + $$10[1]);
                    double $$11 = this.level().getBlockFloorHeight(DismountHelper.nonClimbableShape(this.level(), $$4), () -> DismountHelper.nonClimbableShape(this.level(), (BlockPos)$$4.below()));
                    if (!DismountHelper.isBlockFloorValid($$11)) continue;
                    AABB $$12 = new AABB(-$$8, 0.0, -$$8, $$8, $$7.height(), $$8);
                    Vec3 $$13 = Vec3.upFromBottomCenterOf($$4, $$11);
                    if (!DismountHelper.canDismountTo(this.level(), $$02, $$12.move($$13))) continue;
                    $$02.setPose($$6);
                    return $$13;
                }
            }
        }
        double $$14 = this.getBoundingBox().maxY;
        $$4.set((double)$$3.getX(), $$14, (double)$$3.getZ());
        for (Pose $$15 : $$5) {
            int $$17;
            double $$18;
            double $$16 = $$02.getDimensions($$15).height();
            if (!($$14 + $$16 <= ($$18 = DismountHelper.findCeilingFrom($$4, $$17 = Mth.ceil($$14 - (double)$$4.getY() + $$16), $$0 -> this.level().getBlockState((BlockPos)$$0).getCollisionShape(this.level(), (BlockPos)$$0))))) continue;
            $$02.setPose($$15);
            break;
        }
        return super.getDismountLocationForPassenger($$02);
    }

    @Override
    protected float getBlockSpeedFactor() {
        BlockState $$0 = this.level().getBlockState(this.blockPosition());
        if ($$0.is(BlockTags.RAILS)) {
            return 1.0f;
        }
        return super.getBlockSpeedFactor();
    }

    @Override
    public void animateHurt(float $$0) {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0f);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    public static Pair<Vec3i, Vec3i> exits(RailShape $$0) {
        return EXITS.get($$0);
    }

    @Override
    public Direction getMotionDirection() {
        return this.behavior.getMotionDirection();
    }

    @Override
    protected double getDefaultGravity() {
        return this.isInWater() ? 0.005 : 0.04;
    }

    @Override
    public void tick() {
        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }
        if (this.getDamage() > 0.0f) {
            this.setDamage(this.getDamage() - 1.0f);
        }
        this.checkBelowWorld();
        this.computeSpeed();
        this.handlePortal();
        this.behavior.tick();
        this.updateInWaterStateAndDoFluidPushing();
        if (this.isInLava()) {
            this.lavaIgnite();
            this.lavaHurt();
            this.fallDistance *= 0.5;
        }
        this.firstTick = false;
    }

    public boolean isFirstTick() {
        return this.firstTick;
    }

    public BlockPos getCurrentBlockPosOrRailBelow() {
        int $$0 = Mth.floor(this.getX());
        int $$1 = Mth.floor(this.getY());
        int $$2 = Mth.floor(this.getZ());
        if (AbstractMinecart.useExperimentalMovement(this.level())) {
            double $$3 = this.getY() - 0.1 - (double)1.0E-5f;
            if (this.level().getBlockState(BlockPos.containing($$0, $$3, $$2)).is(BlockTags.RAILS)) {
                $$1 = Mth.floor($$3);
            }
        } else if (this.level().getBlockState(new BlockPos($$0, $$1 - 1, $$2)).is(BlockTags.RAILS)) {
            --$$1;
        }
        return new BlockPos($$0, $$1, $$2);
    }

    protected double getMaxSpeed(ServerLevel $$0) {
        return this.behavior.getMaxSpeed($$0);
    }

    public void activateMinecart(ServerLevel $$0, int $$1, int $$2, int $$3, boolean $$4) {
    }

    @Override
    public void lerpPositionAndRotationStep(int $$0, double $$1, double $$2, double $$3, double $$4, double $$5) {
        super.lerpPositionAndRotationStep($$0, $$1, $$2, $$3, $$4, $$5);
    }

    @Override
    public void applyGravity() {
        super.applyGravity();
    }

    @Override
    public void reapplyPosition() {
        super.reapplyPosition();
    }

    @Override
    public boolean updateInWaterStateAndDoFluidPushing() {
        return super.updateInWaterStateAndDoFluidPushing();
    }

    @Override
    public Vec3 getKnownMovement() {
        return this.behavior.getKnownMovement(super.getKnownMovement());
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.behavior.getInterpolation();
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
        super.recreateFromPacket($$0);
        this.behavior.lerpMotion(this.getDeltaMovement());
    }

    @Override
    public void lerpMotion(Vec3 $$0) {
        this.behavior.lerpMotion($$0);
    }

    protected void moveAlongTrack(ServerLevel $$0) {
        this.behavior.moveAlongTrack($$0);
    }

    protected void comeOffTrack(ServerLevel $$0) {
        double $$1 = this.getMaxSpeed($$0);
        Vec3 $$2 = this.getDeltaMovement();
        this.setDeltaMovement(Mth.clamp($$2.x, -$$1, $$1), $$2.y, Mth.clamp($$2.z, -$$1, $$1));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
        }
    }

    protected double makeStepAlongTrack(BlockPos $$0, RailShape $$1, double $$2) {
        return this.behavior.stepAlongTrack($$0, $$1, $$2);
    }

    @Override
    public void move(MoverType $$0, Vec3 $$1) {
        if (AbstractMinecart.useExperimentalMovement(this.level())) {
            Vec3 $$2 = this.position().add($$1);
            super.move($$0, $$1);
            boolean $$3 = this.behavior.pushAndPickupEntities();
            if ($$3) {
                super.move($$0, $$2.subtract(this.position()));
            }
            if ($$0.equals((Object)MoverType.PISTON)) {
                this.onRails = false;
            }
        } else {
            super.move($$0, $$1);
            this.applyEffectsFromBlocks();
        }
    }

    @Override
    public void applyEffectsFromBlocks() {
        if (AbstractMinecart.useExperimentalMovement(this.level())) {
            super.applyEffectsFromBlocks();
        } else {
            this.applyEffectsFromBlocks(this.position(), this.position());
            this.clearMovementThisTick();
        }
    }

    @Override
    public boolean isOnRails() {
        return this.onRails;
    }

    public void setOnRails(boolean $$0) {
        this.onRails = $$0;
    }

    public boolean isFlipped() {
        return this.flipped;
    }

    public void setFlipped(boolean $$0) {
        this.flipped = $$0;
    }

    public Vec3 getRedstoneDirection(BlockPos $$0) {
        BlockState $$1 = this.level().getBlockState($$0);
        if (!$$1.is(Blocks.POWERED_RAIL) || !$$1.getValue(PoweredRailBlock.POWERED).booleanValue()) {
            return Vec3.ZERO;
        }
        RailShape $$2 = $$1.getValue(((BaseRailBlock)$$1.getBlock()).getShapeProperty());
        if ($$2 == RailShape.EAST_WEST) {
            if (this.isRedstoneConductor($$0.west())) {
                return new Vec3(1.0, 0.0, 0.0);
            }
            if (this.isRedstoneConductor($$0.east())) {
                return new Vec3(-1.0, 0.0, 0.0);
            }
        } else if ($$2 == RailShape.NORTH_SOUTH) {
            if (this.isRedstoneConductor($$0.north())) {
                return new Vec3(0.0, 0.0, 1.0);
            }
            if (this.isRedstoneConductor($$0.south())) {
                return new Vec3(0.0, 0.0, -1.0);
            }
        }
        return Vec3.ZERO;
    }

    public boolean isRedstoneConductor(BlockPos $$0) {
        return this.level().getBlockState($$0).isRedstoneConductor(this.level(), $$0);
    }

    protected Vec3 applyNaturalSlowdown(Vec3 $$0) {
        double $$1 = this.behavior.getSlowdownFactor();
        Vec3 $$2 = $$0.multiply($$1, 0.0, $$1);
        if (this.isInWater()) {
            $$2 = $$2.scale(0.95f);
        }
        return $$2;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.setCustomDisplayBlockState($$0.read("DisplayState", BlockState.CODEC));
        this.setDisplayOffset($$0.getIntOr("DisplayOffset", this.getDefaultDisplayOffset()));
        this.flipped = $$0.getBooleanOr("FlippedRotation", false);
        this.firstTick = $$0.getBooleanOr("HasTicked", false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        this.getCustomDisplayBlockState().ifPresent($$1 -> $$0.store("DisplayState", BlockState.CODEC, $$1));
        int $$12 = this.getDisplayOffset();
        if ($$12 != this.getDefaultDisplayOffset()) {
            $$0.putInt("DisplayOffset", $$12);
        }
        $$0.putBoolean("FlippedRotation", this.flipped);
        $$0.putBoolean("HasTicked", this.firstTick);
    }

    @Override
    public void push(Entity $$0) {
        double $$2;
        if (this.level().isClientSide()) {
            return;
        }
        if ($$0.noPhysics || this.noPhysics) {
            return;
        }
        if (this.hasPassenger($$0)) {
            return;
        }
        double $$1 = $$0.getX() - this.getX();
        double $$3 = $$1 * $$1 + ($$2 = $$0.getZ() - this.getZ()) * $$2;
        if ($$3 >= (double)1.0E-4f) {
            $$3 = Math.sqrt($$3);
            $$1 /= $$3;
            $$2 /= $$3;
            double $$4 = 1.0 / $$3;
            if ($$4 > 1.0) {
                $$4 = 1.0;
            }
            $$1 *= $$4;
            $$2 *= $$4;
            $$1 *= (double)0.1f;
            $$2 *= (double)0.1f;
            $$1 *= 0.5;
            $$2 *= 0.5;
            if ($$0 instanceof AbstractMinecart) {
                AbstractMinecart $$5 = (AbstractMinecart)$$0;
                this.pushOtherMinecart($$5, $$1, $$2);
            } else {
                this.push(-$$1, 0.0, -$$2);
                $$0.push($$1 / 4.0, 0.0, $$2 / 4.0);
            }
        }
    }

    private void pushOtherMinecart(AbstractMinecart $$0, double $$1, double $$2) {
        double $$6;
        double $$5;
        if (AbstractMinecart.useExperimentalMovement(this.level())) {
            double $$3 = this.getDeltaMovement().x;
            double $$4 = this.getDeltaMovement().z;
        } else {
            $$5 = $$0.getX() - this.getX();
            $$6 = $$0.getZ() - this.getZ();
        }
        Vec3 $$7 = new Vec3($$5, 0.0, $$6).normalize();
        Vec3 $$8 = new Vec3(Mth.cos(this.getYRot() * ((float)Math.PI / 180)), 0.0, Mth.sin(this.getYRot() * ((float)Math.PI / 180))).normalize();
        double $$9 = Math.abs($$7.dot($$8));
        if ($$9 < (double)0.8f && !AbstractMinecart.useExperimentalMovement(this.level())) {
            return;
        }
        Vec3 $$10 = this.getDeltaMovement();
        Vec3 $$11 = $$0.getDeltaMovement();
        if ($$0.isFurnace() && !this.isFurnace()) {
            this.setDeltaMovement($$10.multiply(0.2, 1.0, 0.2));
            this.push($$11.x - $$1, 0.0, $$11.z - $$2);
            $$0.setDeltaMovement($$11.multiply(0.95, 1.0, 0.95));
        } else if (!$$0.isFurnace() && this.isFurnace()) {
            $$0.setDeltaMovement($$11.multiply(0.2, 1.0, 0.2));
            $$0.push($$10.x + $$1, 0.0, $$10.z + $$2);
            this.setDeltaMovement($$10.multiply(0.95, 1.0, 0.95));
        } else {
            double $$12 = ($$11.x + $$10.x) / 2.0;
            double $$13 = ($$11.z + $$10.z) / 2.0;
            this.setDeltaMovement($$10.multiply(0.2, 1.0, 0.2));
            this.push($$12 - $$1, 0.0, $$13 - $$2);
            $$0.setDeltaMovement($$11.multiply(0.2, 1.0, 0.2));
            $$0.push($$12 + $$1, 0.0, $$13 + $$2);
        }
    }

    public BlockState getDisplayBlockState() {
        return this.getCustomDisplayBlockState().orElseGet(this::getDefaultDisplayBlockState);
    }

    private Optional<BlockState> getCustomDisplayBlockState() {
        return this.getEntityData().get(DATA_ID_CUSTOM_DISPLAY_BLOCK);
    }

    public BlockState getDefaultDisplayBlockState() {
        return Blocks.AIR.defaultBlockState();
    }

    public int getDisplayOffset() {
        return this.getEntityData().get(DATA_ID_DISPLAY_OFFSET);
    }

    public int getDefaultDisplayOffset() {
        return 6;
    }

    public void setCustomDisplayBlockState(Optional<BlockState> $$0) {
        this.getEntityData().set(DATA_ID_CUSTOM_DISPLAY_BLOCK, $$0);
    }

    public void setDisplayOffset(int $$0) {
        this.getEntityData().set(DATA_ID_DISPLAY_OFFSET, $$0);
    }

    public static boolean useExperimentalMovement(Level $$0) {
        return $$0.enabledFeatures().contains(FeatureFlags.MINECART_IMPROVEMENTS);
    }

    @Override
    public abstract ItemStack getPickResult();

    public boolean isRideable() {
        return false;
    }

    public boolean isFurnace() {
        return false;
    }
}

