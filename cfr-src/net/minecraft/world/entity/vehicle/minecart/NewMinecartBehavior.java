/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.vehicle.minecart;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class NewMinecartBehavior
extends MinecartBehavior {
    public static final int POS_ROT_LERP_TICKS = 3;
    public static final double ON_RAIL_Y_OFFSET = 0.1;
    public static final double OPPOSING_SLOPES_REST_AT_SPEED_THRESHOLD = 0.005;
    private @Nullable StepPartialTicks cacheIndexAlpha;
    private int cachedLerpDelay;
    private float cachedPartialTick;
    private int lerpDelay = 0;
    public final List<MinecartStep> lerpSteps = new LinkedList<MinecartStep>();
    public final List<MinecartStep> currentLerpSteps = new LinkedList<MinecartStep>();
    public double currentLerpStepsTotalWeight = 0.0;
    public MinecartStep oldLerp = MinecartStep.ZERO;

    public NewMinecartBehavior(AbstractMinecart $$0) {
        super($$0);
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void tick() {
        void $$2;
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            this.lerpClientPositionAndRotation();
            boolean $$1 = BaseRailBlock.isRail(this.level().getBlockState(this.minecart.getCurrentBlockPosOrRailBelow()));
            this.minecart.setOnRails($$1);
            return;
        }
        ServerLevel $$0 = (ServerLevel)level;
        BlockPos $$3 = this.minecart.getCurrentBlockPosOrRailBelow();
        BlockState $$4 = this.level().getBlockState($$3);
        if (this.minecart.isFirstTick()) {
            this.minecart.setOnRails(BaseRailBlock.isRail($$4));
            this.adjustToRails($$3, $$4, true);
        }
        this.minecart.applyGravity();
        this.minecart.moveAlongTrack((ServerLevel)$$2);
    }

    private void lerpClientPositionAndRotation() {
        if (--this.lerpDelay <= 0) {
            this.setOldLerpValues();
            this.currentLerpSteps.clear();
            if (!this.lerpSteps.isEmpty()) {
                this.currentLerpSteps.addAll(this.lerpSteps);
                this.lerpSteps.clear();
                this.currentLerpStepsTotalWeight = 0.0;
                for (MinecartStep $$0 : this.currentLerpSteps) {
                    this.currentLerpStepsTotalWeight += (double)$$0.weight;
                }
                int n = this.lerpDelay = this.currentLerpStepsTotalWeight == 0.0 ? 0 : 3;
            }
        }
        if (this.cartHasPosRotLerp()) {
            this.setPos(this.getCartLerpPosition(1.0f));
            this.setDeltaMovement(this.getCartLerpMovements(1.0f));
            this.setXRot(this.getCartLerpXRot(1.0f));
            this.setYRot(this.getCartLerpYRot(1.0f));
        }
    }

    public void setOldLerpValues() {
        this.oldLerp = new MinecartStep(this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), 0.0f);
    }

    public boolean cartHasPosRotLerp() {
        return !this.currentLerpSteps.isEmpty();
    }

    public float getCartLerpXRot(float $$0) {
        StepPartialTicks $$1 = this.getCurrentLerpStep($$0);
        return Mth.rotLerp($$1.partialTicksInStep, $$1.previousStep.xRot, $$1.currentStep.xRot);
    }

    public float getCartLerpYRot(float $$0) {
        StepPartialTicks $$1 = this.getCurrentLerpStep($$0);
        return Mth.rotLerp($$1.partialTicksInStep, $$1.previousStep.yRot, $$1.currentStep.yRot);
    }

    public Vec3 getCartLerpPosition(float $$0) {
        StepPartialTicks $$1 = this.getCurrentLerpStep($$0);
        return Mth.lerp((double)$$1.partialTicksInStep, $$1.previousStep.position, $$1.currentStep.position);
    }

    public Vec3 getCartLerpMovements(float $$0) {
        StepPartialTicks $$1 = this.getCurrentLerpStep($$0);
        return Mth.lerp((double)$$1.partialTicksInStep, $$1.previousStep.movement, $$1.currentStep.movement);
    }

    private StepPartialTicks getCurrentLerpStep(float $$0) {
        int $$5;
        if ($$0 == this.cachedPartialTick && this.lerpDelay == this.cachedLerpDelay && this.cacheIndexAlpha != null) {
            return this.cacheIndexAlpha;
        }
        float $$1 = ((float)(3 - this.lerpDelay) + $$0) / 3.0f;
        float $$2 = 0.0f;
        float $$3 = 1.0f;
        boolean $$4 = false;
        for ($$5 = 0; $$5 < this.currentLerpSteps.size(); ++$$5) {
            float $$6 = this.currentLerpSteps.get((int)$$5).weight;
            if ($$6 <= 0.0f || !((double)($$2 += $$6) >= this.currentLerpStepsTotalWeight * (double)$$1)) continue;
            float $$7 = $$2 - $$6;
            $$3 = (float)(((double)$$1 * this.currentLerpStepsTotalWeight - (double)$$7) / (double)$$6);
            $$4 = true;
            break;
        }
        if (!$$4) {
            $$5 = this.currentLerpSteps.size() - 1;
        }
        MinecartStep $$8 = this.currentLerpSteps.get($$5);
        MinecartStep $$9 = $$5 > 0 ? this.currentLerpSteps.get($$5 - 1) : this.oldLerp;
        this.cacheIndexAlpha = new StepPartialTicks($$3, $$8, $$9);
        this.cachedLerpDelay = this.lerpDelay;
        this.cachedPartialTick = $$0;
        return this.cacheIndexAlpha;
    }

    public void adjustToRails(BlockPos $$0, BlockState $$1, boolean $$2) {
        boolean $$22;
        Vec3 $$19;
        boolean $$12;
        if (!BaseRailBlock.isRail($$1)) {
            return;
        }
        RailShape $$3 = $$1.getValue(((BaseRailBlock)$$1.getBlock()).getShapeProperty());
        Pair<Vec3i, Vec3i> $$4 = AbstractMinecart.exits($$3);
        Vec3 $$5 = new Vec3((Vec3i)$$4.getFirst()).scale(0.5);
        Vec3 $$6 = new Vec3((Vec3i)$$4.getSecond()).scale(0.5);
        Vec3 $$7 = $$5.horizontal();
        Vec3 $$8 = $$6.horizontal();
        if (this.getDeltaMovement().length() > (double)1.0E-5f && this.getDeltaMovement().dot($$7) < this.getDeltaMovement().dot($$8) || this.isDecending($$8, $$3)) {
            Vec3 $$9 = $$7;
            $$7 = $$8;
            $$8 = $$9;
        }
        float $$10 = 180.0f - (float)(Math.atan2($$7.z, $$7.x) * 180.0 / Math.PI);
        $$10 += this.minecart.isFlipped() ? 180.0f : 0.0f;
        Vec3 $$11 = this.position();
        boolean bl = $$12 = $$5.x() != $$6.x() && $$5.z() != $$6.z();
        if ($$12) {
            Vec3 $$13 = $$6.subtract($$5);
            Vec3 $$14 = $$11.subtract($$0.getBottomCenter()).subtract($$5);
            Vec3 $$15 = $$13.scale($$13.dot($$14) / $$13.dot($$13));
            Vec3 $$16 = $$0.getBottomCenter().add($$5).add($$15);
            $$10 = 180.0f - (float)(Math.atan2($$15.z, $$15.x) * 180.0 / Math.PI);
            $$10 += this.minecart.isFlipped() ? 180.0f : 0.0f;
        } else {
            boolean $$17 = $$5.subtract((Vec3)$$6).x != 0.0;
            boolean $$18 = $$5.subtract((Vec3)$$6).z != 0.0;
            $$19 = new Vec3($$18 ? $$0.getCenter().x : $$11.x, $$0.getY(), $$17 ? $$0.getCenter().z : $$11.z);
        }
        Vec3 $$20 = $$19.subtract($$11);
        this.setPos($$11.add($$20));
        float $$21 = 0.0f;
        boolean bl2 = $$22 = $$5.y() != $$6.y();
        if ($$22) {
            Vec3 $$23 = $$0.getBottomCenter().add($$8);
            double $$24 = $$23.distanceTo(this.position());
            this.setPos(this.position().add(0.0, $$24 + 0.1, 0.0));
            $$21 = this.minecart.isFlipped() ? 45.0f : -45.0f;
        } else {
            this.setPos(this.position().add(0.0, 0.1, 0.0));
        }
        this.setRotation($$10, $$21);
        double $$25 = $$11.distanceTo(this.position());
        if ($$25 > 0.0) {
            this.lerpSteps.add(new MinecartStep(this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), $$2 ? 0.0f : (float)$$25));
        }
    }

    private void setRotation(float $$0, float $$1) {
        double $$2 = Math.abs($$0 - this.getYRot());
        if ($$2 >= 175.0 && $$2 <= 185.0) {
            this.minecart.setFlipped(!this.minecart.isFlipped());
            $$0 -= 180.0f;
            $$1 *= -1.0f;
        }
        $$1 = Math.clamp($$1, -45.0f, 45.0f);
        this.setXRot($$1 % 360.0f);
        this.setYRot($$0 % 360.0f);
    }

    @Override
    public void moveAlongTrack(ServerLevel $$0) {
        TrackIteration $$1 = new TrackIteration();
        while ($$1.shouldIterate() && this.minecart.isAlive()) {
            Vec3 $$2 = this.getDeltaMovement();
            BlockPos $$3 = this.minecart.getCurrentBlockPosOrRailBelow();
            BlockState $$4 = this.level().getBlockState($$3);
            boolean $$5 = BaseRailBlock.isRail($$4);
            if (this.minecart.isOnRails() != $$5) {
                this.minecart.setOnRails($$5);
                this.adjustToRails($$3, $$4, false);
            }
            if ($$5) {
                this.minecart.resetFallDistance();
                this.minecart.setOldPosAndRot();
                if ($$4.is(Blocks.ACTIVATOR_RAIL)) {
                    this.minecart.activateMinecart($$0, $$3.getX(), $$3.getY(), $$3.getZ(), $$4.getValue(PoweredRailBlock.POWERED));
                }
                RailShape $$6 = $$4.getValue(((BaseRailBlock)$$4.getBlock()).getShapeProperty());
                Vec3 $$7 = this.calculateTrackSpeed($$0, $$2.horizontal(), $$1, $$3, $$4, $$6);
                $$1.movementLeft = $$1.firstIteration ? $$7.horizontalDistance() : ($$1.movementLeft += $$7.horizontalDistance() - $$2.horizontalDistance());
                this.setDeltaMovement($$7);
                $$1.movementLeft = this.minecart.makeStepAlongTrack($$3, $$6, $$1.movementLeft);
            } else {
                this.minecart.comeOffTrack($$0);
                $$1.movementLeft = 0.0;
            }
            Vec3 $$8 = this.position();
            Vec3 $$9 = $$8.subtract(this.minecart.oldPosition());
            double $$10 = $$9.length();
            if ($$10 > (double)1.0E-5f) {
                if ($$9.horizontalDistanceSqr() > (double)1.0E-5f) {
                    float $$11 = 180.0f - (float)(Math.atan2($$9.z, $$9.x) * 180.0 / Math.PI);
                    float $$12 = this.minecart.onGround() && !this.minecart.isOnRails() ? 0.0f : 90.0f - (float)(Math.atan2($$9.horizontalDistance(), $$9.y) * 180.0 / Math.PI);
                    this.setRotation($$11 += this.minecart.isFlipped() ? 180.0f : 0.0f, $$12 *= this.minecart.isFlipped() ? -1.0f : 1.0f);
                } else if (!this.minecart.isOnRails()) {
                    this.setXRot(this.minecart.onGround() ? 0.0f : Mth.rotLerp(0.2f, this.getXRot(), 0.0f));
                }
                this.lerpSteps.add(new MinecartStep($$8, this.getDeltaMovement(), this.getYRot(), this.getXRot(), (float)Math.min($$10, this.getMaxSpeed($$0))));
            } else if ($$2.horizontalDistanceSqr() > 0.0) {
                this.lerpSteps.add(new MinecartStep($$8, this.getDeltaMovement(), this.getYRot(), this.getXRot(), 1.0f));
            }
            if ($$10 > (double)1.0E-5f || $$1.firstIteration) {
                this.minecart.applyEffectsFromBlocks();
                this.minecart.applyEffectsFromBlocks();
            }
            $$1.firstIteration = false;
        }
    }

    private Vec3 calculateTrackSpeed(ServerLevel $$0, Vec3 $$1, TrackIteration $$2, BlockPos $$3, BlockState $$4, RailShape $$5) {
        Vec3 $$11;
        Vec3 $$9;
        Vec3 $$8;
        Vec3 $$7;
        Vec3 $$6 = $$1;
        if (!$$2.hasGainedSlopeSpeed && ($$7 = this.calculateSlopeSpeed($$6, $$5)).horizontalDistanceSqr() != $$6.horizontalDistanceSqr()) {
            $$2.hasGainedSlopeSpeed = true;
            $$6 = $$7;
        }
        if ($$2.firstIteration && ($$8 = this.calculatePlayerInputSpeed($$6)).horizontalDistanceSqr() != $$6.horizontalDistanceSqr()) {
            $$2.hasHalted = true;
            $$6 = $$8;
        }
        if (!$$2.hasHalted && ($$9 = this.calculateHaltTrackSpeed($$6, $$4)).horizontalDistanceSqr() != $$6.horizontalDistanceSqr()) {
            $$2.hasHalted = true;
            $$6 = $$9;
        }
        if ($$2.firstIteration && ($$6 = this.minecart.applyNaturalSlowdown($$6)).lengthSqr() > 0.0) {
            double $$10 = Math.min($$6.length(), this.minecart.getMaxSpeed($$0));
            $$6 = $$6.normalize().scale($$10);
        }
        if (!$$2.hasBoosted && ($$11 = this.calculateBoostTrackSpeed($$6, $$3, $$4)).horizontalDistanceSqr() != $$6.horizontalDistanceSqr()) {
            $$2.hasBoosted = true;
            $$6 = $$11;
        }
        return $$6;
    }

    private Vec3 calculateSlopeSpeed(Vec3 $$0, RailShape $$1) {
        double $$2 = Math.max(0.0078125, $$0.horizontalDistance() * 0.02);
        if (this.minecart.isInWater()) {
            $$2 *= 0.2;
        }
        return switch ($$1) {
            case RailShape.ASCENDING_EAST -> $$0.add(-$$2, 0.0, 0.0);
            case RailShape.ASCENDING_WEST -> $$0.add($$2, 0.0, 0.0);
            case RailShape.ASCENDING_NORTH -> $$0.add(0.0, 0.0, $$2);
            case RailShape.ASCENDING_SOUTH -> $$0.add(0.0, 0.0, -$$2);
            default -> $$0;
        };
    }

    /*
     * WARNING - void declaration
     */
    private Vec3 calculatePlayerInputSpeed(Vec3 $$0) {
        void $$2;
        Entity entity = this.minecart.getFirstPassenger();
        if (!(entity instanceof ServerPlayer)) {
            return $$0;
        }
        ServerPlayer $$1 = (ServerPlayer)entity;
        Vec3 $$3 = $$2.getLastClientMoveIntent();
        if ($$3.lengthSqr() > 0.0) {
            Vec3 $$4 = $$3.normalize();
            double $$5 = $$0.horizontalDistanceSqr();
            if ($$4.lengthSqr() > 0.0 && $$5 < 0.01) {
                return $$0.add(new Vec3($$4.x, 0.0, $$4.z).normalize().scale(0.001));
            }
        }
        return $$0;
    }

    private Vec3 calculateHaltTrackSpeed(Vec3 $$0, BlockState $$1) {
        if (!$$1.is(Blocks.POWERED_RAIL) || $$1.getValue(PoweredRailBlock.POWERED).booleanValue()) {
            return $$0;
        }
        if ($$0.length() < 0.03) {
            return Vec3.ZERO;
        }
        return $$0.scale(0.5);
    }

    private Vec3 calculateBoostTrackSpeed(Vec3 $$0, BlockPos $$1, BlockState $$2) {
        if (!$$2.is(Blocks.POWERED_RAIL) || !$$2.getValue(PoweredRailBlock.POWERED).booleanValue()) {
            return $$0;
        }
        if ($$0.length() > 0.01) {
            return $$0.normalize().scale($$0.length() + 0.06);
        }
        Vec3 $$3 = this.minecart.getRedstoneDirection($$1);
        if ($$3.lengthSqr() <= 0.0) {
            return $$0;
        }
        return $$3.scale($$0.length() + 0.2);
    }

    @Override
    public double stepAlongTrack(BlockPos $$0, RailShape $$1, double $$2) {
        if ($$2 < (double)1.0E-5f) {
            return 0.0;
        }
        Vec3 $$3 = this.position();
        Pair<Vec3i, Vec3i> $$4 = AbstractMinecart.exits($$1);
        Vec3i $$5 = (Vec3i)$$4.getFirst();
        Vec3i $$6 = (Vec3i)$$4.getSecond();
        Vec3 $$7 = this.getDeltaMovement().horizontal();
        if ($$7.length() < (double)1.0E-5f) {
            this.setDeltaMovement(Vec3.ZERO);
            return 0.0;
        }
        boolean $$8 = $$5.getY() != $$6.getY();
        Vec3 $$9 = new Vec3($$6).scale(0.5).horizontal();
        Vec3 $$10 = new Vec3($$5).scale(0.5).horizontal();
        if ($$7.dot($$10) < $$7.dot($$9)) {
            $$10 = $$9;
        }
        Vec3 $$11 = $$0.getBottomCenter().add($$10).add(0.0, 0.1, 0.0).add($$10.normalize().scale(1.0E-5f));
        if ($$8 && !this.isDecending($$7, $$1)) {
            $$11 = $$11.add(0.0, 1.0, 0.0);
        }
        Vec3 $$12 = $$11.subtract(this.position()).normalize();
        $$7 = $$12.scale($$7.length() / $$12.horizontalDistance());
        Vec3 $$13 = $$3.add($$7.normalize().scale($$2 * (double)($$8 ? Mth.SQRT_OF_TWO : 1.0f)));
        if ($$3.distanceToSqr($$11) <= $$3.distanceToSqr($$13)) {
            $$2 = $$11.subtract($$13).horizontalDistance();
            $$13 = $$11;
        } else {
            $$2 = 0.0;
        }
        this.minecart.move(MoverType.SELF, $$13.subtract($$3));
        BlockState $$14 = this.level().getBlockState(BlockPos.containing($$13));
        if ($$8) {
            RailShape $$15;
            if (BaseRailBlock.isRail($$14) && this.restAtVShape($$1, $$15 = $$14.getValue(((BaseRailBlock)$$14.getBlock()).getShapeProperty()))) {
                return 0.0;
            }
            double $$16 = $$11.horizontal().distanceTo(this.position().horizontal());
            double $$17 = $$11.y + (this.isDecending($$7, $$1) ? $$16 : -$$16);
            if (this.position().y < $$17) {
                this.setPos(this.position().x, $$17, this.position().z);
            }
        }
        if (this.position().distanceTo($$3) < (double)1.0E-5f && $$13.distanceTo($$3) > (double)1.0E-5f) {
            this.setDeltaMovement(Vec3.ZERO);
            return 0.0;
        }
        this.setDeltaMovement($$7);
        return $$2;
    }

    private boolean restAtVShape(RailShape $$0, RailShape $$1) {
        if (this.getDeltaMovement().lengthSqr() < 0.005 && $$1.isSlope() && this.isDecending(this.getDeltaMovement(), $$0) && !this.isDecending(this.getDeltaMovement(), $$1)) {
            this.setDeltaMovement(Vec3.ZERO);
            return true;
        }
        return false;
    }

    @Override
    public double getMaxSpeed(ServerLevel $$0) {
        return (double)$$0.getGameRules().get(GameRules.MAX_MINECART_SPEED).intValue() * (this.minecart.isInWater() ? 0.5 : 1.0) / 20.0;
    }

    private boolean isDecending(Vec3 $$0, RailShape $$1) {
        return switch ($$1) {
            case RailShape.ASCENDING_EAST -> {
                if ($$0.x < 0.0) {
                    yield true;
                }
                yield false;
            }
            case RailShape.ASCENDING_WEST -> {
                if ($$0.x > 0.0) {
                    yield true;
                }
                yield false;
            }
            case RailShape.ASCENDING_NORTH -> {
                if ($$0.z > 0.0) {
                    yield true;
                }
                yield false;
            }
            case RailShape.ASCENDING_SOUTH -> {
                if ($$0.z < 0.0) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    @Override
    public double getSlowdownFactor() {
        return this.minecart.isVehicle() ? 0.997 : 0.975;
    }

    @Override
    public boolean pushAndPickupEntities() {
        boolean $$0 = this.pickupEntities(this.minecart.getBoundingBox().inflate(0.2, 0.0, 0.2));
        if (this.minecart.horizontalCollision || this.minecart.verticalCollision) {
            boolean $$1 = this.pushEntities(this.minecart.getBoundingBox().inflate(1.0E-7));
            return $$0 && !$$1;
        }
        return false;
    }

    public boolean pickupEntities(AABB $$0) {
        List<Entity> $$1;
        if (this.minecart.isRideable() && !this.minecart.isVehicle() && !($$1 = this.level().getEntities(this.minecart, $$0, EntitySelector.pushableBy(this.minecart))).isEmpty()) {
            for (Entity $$2 : $$1) {
                boolean $$3;
                if ($$2 instanceof Player || $$2 instanceof IronGolem || $$2 instanceof AbstractMinecart || this.minecart.isVehicle() || $$2.isPassenger() || !($$3 = $$2.startRiding(this.minecart))) continue;
                return true;
            }
        }
        return false;
    }

    public boolean pushEntities(AABB $$0) {
        boolean $$1;
        block3: {
            block2: {
                $$1 = false;
                if (!this.minecart.isRideable()) break block2;
                List<Entity> $$2 = this.level().getEntities(this.minecart, $$0, EntitySelector.pushableBy(this.minecart));
                if ($$2.isEmpty()) break block3;
                for (Entity $$3 : $$2) {
                    if (!($$3 instanceof Player) && !($$3 instanceof IronGolem) && !($$3 instanceof AbstractMinecart) && !this.minecart.isVehicle() && !$$3.isPassenger()) continue;
                    $$3.push(this.minecart);
                    $$1 = true;
                }
                break block3;
            }
            for (Entity $$4 : this.level().getEntities(this.minecart, $$0)) {
                if (this.minecart.hasPassenger($$4) || !$$4.isPushable() || !($$4 instanceof AbstractMinecart)) continue;
                $$4.push(this.minecart);
                $$1 = true;
            }
        }
        return $$1;
    }

    public static final class MinecartStep
    extends Record {
        final Vec3 position;
        final Vec3 movement;
        final float yRot;
        final float xRot;
        final float weight;
        public static final StreamCodec<ByteBuf, MinecartStep> STREAM_CODEC = StreamCodec.composite(Vec3.STREAM_CODEC, MinecartStep::position, Vec3.STREAM_CODEC, MinecartStep::movement, ByteBufCodecs.ROTATION_BYTE, MinecartStep::yRot, ByteBufCodecs.ROTATION_BYTE, MinecartStep::xRot, ByteBufCodecs.FLOAT, MinecartStep::weight, MinecartStep::new);
        public static MinecartStep ZERO = new MinecartStep(Vec3.ZERO, Vec3.ZERO, 0.0f, 0.0f, 0.0f);

        public MinecartStep(Vec3 $$0, Vec3 $$1, float $$2, float $$3, float $$4) {
            this.position = $$0;
            this.movement = $$1;
            this.yRot = $$2;
            this.xRot = $$3;
            this.weight = $$4;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{MinecartStep.class, "position;movement;yRot;xRot;weight", "position", "movement", "yRot", "xRot", "weight"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{MinecartStep.class, "position;movement;yRot;xRot;weight", "position", "movement", "yRot", "xRot", "weight"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{MinecartStep.class, "position;movement;yRot;xRot;weight", "position", "movement", "yRot", "xRot", "weight"}, this, $$0);
        }

        public Vec3 position() {
            return this.position;
        }

        public Vec3 movement() {
            return this.movement;
        }

        public float yRot() {
            return this.yRot;
        }

        public float xRot() {
            return this.xRot;
        }

        public float weight() {
            return this.weight;
        }
    }

    static final class StepPartialTicks
    extends Record {
        final float partialTicksInStep;
        final MinecartStep currentStep;
        final MinecartStep previousStep;

        StepPartialTicks(float $$0, MinecartStep $$1, MinecartStep $$2) {
            this.partialTicksInStep = $$0;
            this.currentStep = $$1;
            this.previousStep = $$2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{StepPartialTicks.class, "partialTicksInStep;currentStep;previousStep", "partialTicksInStep", "currentStep", "previousStep"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{StepPartialTicks.class, "partialTicksInStep;currentStep;previousStep", "partialTicksInStep", "currentStep", "previousStep"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{StepPartialTicks.class, "partialTicksInStep;currentStep;previousStep", "partialTicksInStep", "currentStep", "previousStep"}, this, $$0);
        }

        public float partialTicksInStep() {
            return this.partialTicksInStep;
        }

        public MinecartStep currentStep() {
            return this.currentStep;
        }

        public MinecartStep previousStep() {
            return this.previousStep;
        }
    }

    static class TrackIteration {
        double movementLeft = 0.0;
        boolean firstIteration = true;
        boolean hasGainedSlopeSpeed = false;
        boolean hasHalted = false;
        boolean hasBoosted = false;

        TrackIteration() {
        }

        public boolean shouldIterate() {
            return this.firstIteration || this.movementLeft > (double)1.0E-5f;
        }
    }
}

