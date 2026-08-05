/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.vehicle.minecart;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.InterpolationHandler;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class OldMinecartBehavior
extends MinecartBehavior {
    private static final double MINECART_RIDABLE_THRESHOLD = 0.01;
    private static final double MAX_SPEED_IN_WATER = 0.2;
    private static final double MAX_SPEED_ON_LAND = 0.4;
    private static final double ABSOLUTE_MAX_SPEED = 0.4;
    private final InterpolationHandler interpolation;
    private Vec3 targetDeltaMovement = Vec3.ZERO;

    public OldMinecartBehavior(AbstractMinecart $$0) {
        super($$0);
        this.interpolation = new InterpolationHandler((Entity)$$0, this::onInterpolation);
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolation;
    }

    public void onInterpolation(InterpolationHandler $$0) {
        this.setDeltaMovement(this.targetDeltaMovement);
    }

    @Override
    public void lerpMotion(Vec3 $$0) {
        this.targetDeltaMovement = $$0;
        this.setDeltaMovement(this.targetDeltaMovement);
    }

    @Override
    public void tick() {
        double $$7;
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            if (this.interpolation.hasActiveInterpolation()) {
                this.interpolation.interpolate();
            } else {
                this.minecart.reapplyPosition();
                this.setXRot(this.getXRot() % 360.0f);
                this.setYRot(this.getYRot() % 360.0f);
            }
            return;
        }
        ServerLevel $$0 = (ServerLevel)level;
        this.minecart.applyGravity();
        BlockPos $$2 = this.minecart.getCurrentBlockPosOrRailBelow();
        BlockState $$3 = this.level().getBlockState($$2);
        boolean $$4 = BaseRailBlock.isRail($$3);
        this.minecart.setOnRails($$4);
        if ($$4) {
            this.moveAlongTrack((ServerLevel)$$1);
            if ($$3.is(Blocks.ACTIVATOR_RAIL)) {
                this.minecart.activateMinecart((ServerLevel)$$1, $$2.getX(), $$2.getY(), $$2.getZ(), $$3.getValue(PoweredRailBlock.POWERED));
            }
        } else {
            this.minecart.comeOffTrack((ServerLevel)$$1);
        }
        this.minecart.applyEffectsFromBlocks();
        this.setXRot(0.0f);
        double $$5 = this.minecart.xo - this.getX();
        double $$6 = this.minecart.zo - this.getZ();
        if ($$5 * $$5 + $$6 * $$6 > 0.001) {
            this.setYRot((float)(Mth.atan2($$6, $$5) * 180.0 / Math.PI));
            if (this.minecart.isFlipped()) {
                this.setYRot(this.getYRot() + 180.0f);
            }
        }
        if (($$7 = (double)Mth.wrapDegrees(this.getYRot() - this.minecart.yRotO)) < -170.0 || $$7 >= 170.0) {
            this.setYRot(this.getYRot() + 180.0f);
            this.minecart.setFlipped(!this.minecart.isFlipped());
        }
        this.setXRot(this.getXRot() % 360.0f);
        this.setYRot(this.getYRot() % 360.0f);
        this.pushAndPickupEntities();
    }

    @Override
    public void moveAlongTrack(ServerLevel $$0) {
        double $$35;
        Vec3 $$23;
        BlockPos $$1 = this.minecart.getCurrentBlockPosOrRailBelow();
        BlockState $$2 = this.level().getBlockState($$1);
        this.minecart.resetFallDistance();
        double $$3 = this.minecart.getX();
        double $$4 = this.minecart.getY();
        double $$5 = this.minecart.getZ();
        Vec3 $$6 = this.getPos($$3, $$4, $$5);
        $$4 = $$1.getY();
        boolean $$7 = false;
        boolean $$8 = false;
        if ($$2.is(Blocks.POWERED_RAIL)) {
            $$7 = $$2.getValue(PoweredRailBlock.POWERED);
            $$8 = !$$7;
        }
        double $$9 = 0.0078125;
        if (this.minecart.isInWater()) {
            $$9 *= 0.2;
        }
        Vec3 $$10 = this.getDeltaMovement();
        RailShape $$11 = $$2.getValue(((BaseRailBlock)$$2.getBlock()).getShapeProperty());
        switch ($$11) {
            case ASCENDING_EAST: {
                this.setDeltaMovement($$10.add(-$$9, 0.0, 0.0));
                $$4 += 1.0;
                break;
            }
            case ASCENDING_WEST: {
                this.setDeltaMovement($$10.add($$9, 0.0, 0.0));
                $$4 += 1.0;
                break;
            }
            case ASCENDING_NORTH: {
                this.setDeltaMovement($$10.add(0.0, 0.0, $$9));
                $$4 += 1.0;
                break;
            }
            case ASCENDING_SOUTH: {
                this.setDeltaMovement($$10.add(0.0, 0.0, -$$9));
                $$4 += 1.0;
            }
        }
        $$10 = this.getDeltaMovement();
        Pair<Vec3i, Vec3i> $$12 = AbstractMinecart.exits($$11);
        Vec3i $$13 = (Vec3i)$$12.getFirst();
        Vec3i $$14 = (Vec3i)$$12.getSecond();
        double $$15 = $$14.getX() - $$13.getX();
        double $$16 = $$14.getZ() - $$13.getZ();
        double $$17 = Math.sqrt($$15 * $$15 + $$16 * $$16);
        double $$18 = $$10.x * $$15 + $$10.z * $$16;
        if ($$18 < 0.0) {
            $$15 = -$$15;
            $$16 = -$$16;
        }
        double $$19 = Math.min(2.0, $$10.horizontalDistance());
        $$10 = new Vec3($$19 * $$15 / $$17, $$10.y, $$19 * $$16 / $$17);
        this.setDeltaMovement($$10);
        Entity $$20 = this.minecart.getFirstPassenger();
        Entity entity = this.minecart.getFirstPassenger();
        if (entity instanceof ServerPlayer) {
            ServerPlayer $$21 = (ServerPlayer)entity;
            Vec3 $$22 = $$21.getLastClientMoveIntent();
        } else {
            $$23 = Vec3.ZERO;
        }
        if ($$20 instanceof Player && $$23.lengthSqr() > 0.0) {
            Vec3 $$24 = $$23.normalize();
            double $$25 = this.getDeltaMovement().horizontalDistanceSqr();
            if ($$24.lengthSqr() > 0.0 && $$25 < 0.01) {
                this.setDeltaMovement(this.getDeltaMovement().add($$23.x * 0.001, 0.0, $$23.z * 0.001));
                $$8 = false;
            }
        }
        if ($$8) {
            double $$26 = this.getDeltaMovement().horizontalDistance();
            if ($$26 < 0.03) {
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.0, 0.5));
            }
        }
        double $$27 = (double)$$1.getX() + 0.5 + (double)$$13.getX() * 0.5;
        double $$28 = (double)$$1.getZ() + 0.5 + (double)$$13.getZ() * 0.5;
        double $$29 = (double)$$1.getX() + 0.5 + (double)$$14.getX() * 0.5;
        double $$30 = (double)$$1.getZ() + 0.5 + (double)$$14.getZ() * 0.5;
        $$15 = $$29 - $$27;
        $$16 = $$30 - $$28;
        if ($$15 == 0.0) {
            double $$31 = $$5 - (double)$$1.getZ();
        } else if ($$16 == 0.0) {
            double $$32 = $$3 - (double)$$1.getX();
        } else {
            double $$33 = $$3 - $$27;
            double $$34 = $$5 - $$28;
            $$35 = ($$33 * $$15 + $$34 * $$16) * 2.0;
        }
        $$3 = $$27 + $$15 * $$35;
        $$5 = $$28 + $$16 * $$35;
        this.setPos($$3, $$4, $$5);
        double $$36 = this.minecart.isVehicle() ? 0.75 : 1.0;
        double $$37 = this.minecart.getMaxSpeed($$0);
        $$10 = this.getDeltaMovement();
        this.minecart.move(MoverType.SELF, new Vec3(Mth.clamp($$36 * $$10.x, -$$37, $$37), 0.0, Mth.clamp($$36 * $$10.z, -$$37, $$37)));
        if ($$13.getY() != 0 && Mth.floor(this.minecart.getX()) - $$1.getX() == $$13.getX() && Mth.floor(this.minecart.getZ()) - $$1.getZ() == $$13.getZ()) {
            this.setPos(this.minecart.getX(), this.minecart.getY() + (double)$$13.getY(), this.minecart.getZ());
        } else if ($$14.getY() != 0 && Mth.floor(this.minecart.getX()) - $$1.getX() == $$14.getX() && Mth.floor(this.minecart.getZ()) - $$1.getZ() == $$14.getZ()) {
            this.setPos(this.minecart.getX(), this.minecart.getY() + (double)$$14.getY(), this.minecart.getZ());
        }
        this.setDeltaMovement(this.minecart.applyNaturalSlowdown(this.getDeltaMovement()));
        Vec3 $$38 = this.getPos(this.minecart.getX(), this.minecart.getY(), this.minecart.getZ());
        if ($$38 != null && $$6 != null) {
            double $$39 = ($$6.y - $$38.y) * 0.05;
            Vec3 $$40 = this.getDeltaMovement();
            double $$41 = $$40.horizontalDistance();
            if ($$41 > 0.0) {
                this.setDeltaMovement($$40.multiply(($$41 + $$39) / $$41, 1.0, ($$41 + $$39) / $$41));
            }
            this.setPos(this.minecart.getX(), $$38.y, this.minecart.getZ());
        }
        int $$42 = Mth.floor(this.minecart.getX());
        int $$43 = Mth.floor(this.minecart.getZ());
        if ($$42 != $$1.getX() || $$43 != $$1.getZ()) {
            Vec3 $$44 = this.getDeltaMovement();
            double $$45 = $$44.horizontalDistance();
            this.setDeltaMovement($$45 * (double)($$42 - $$1.getX()), $$44.y, $$45 * (double)($$43 - $$1.getZ()));
        }
        if ($$7) {
            Vec3 $$46 = this.getDeltaMovement();
            double $$47 = $$46.horizontalDistance();
            if ($$47 > 0.01) {
                double $$48 = 0.06;
                this.setDeltaMovement($$46.add($$46.x / $$47 * 0.06, 0.0, $$46.z / $$47 * 0.06));
            } else {
                Vec3 $$49 = this.getDeltaMovement();
                double $$50 = $$49.x;
                double $$51 = $$49.z;
                if ($$11 == RailShape.EAST_WEST) {
                    if (this.minecart.isRedstoneConductor($$1.west())) {
                        $$50 = 0.02;
                    } else if (this.minecart.isRedstoneConductor($$1.east())) {
                        $$50 = -0.02;
                    }
                } else if ($$11 == RailShape.NORTH_SOUTH) {
                    if (this.minecart.isRedstoneConductor($$1.north())) {
                        $$51 = 0.02;
                    } else if (this.minecart.isRedstoneConductor($$1.south())) {
                        $$51 = -0.02;
                    }
                } else {
                    return;
                }
                this.setDeltaMovement($$50, $$49.y, $$51);
            }
        }
    }

    public @Nullable Vec3 getPosOffs(double $$0, double $$1, double $$2, double $$3) {
        BlockState $$7;
        int $$4 = Mth.floor($$0);
        int $$5 = Mth.floor($$1);
        int $$6 = Mth.floor($$2);
        if (this.level().getBlockState(new BlockPos($$4, $$5 - 1, $$6)).is(BlockTags.RAILS)) {
            --$$5;
        }
        if (BaseRailBlock.isRail($$7 = this.level().getBlockState(new BlockPos($$4, $$5, $$6)))) {
            RailShape $$8 = $$7.getValue(((BaseRailBlock)$$7.getBlock()).getShapeProperty());
            $$1 = $$5;
            if ($$8.isSlope()) {
                $$1 = $$5 + 1;
            }
            Pair<Vec3i, Vec3i> $$9 = AbstractMinecart.exits($$8);
            Vec3i $$10 = (Vec3i)$$9.getFirst();
            Vec3i $$11 = (Vec3i)$$9.getSecond();
            double $$12 = $$11.getX() - $$10.getX();
            double $$13 = $$11.getZ() - $$10.getZ();
            double $$14 = Math.sqrt($$12 * $$12 + $$13 * $$13);
            if ($$10.getY() != 0 && Mth.floor($$0 += ($$12 /= $$14) * $$3) - $$4 == $$10.getX() && Mth.floor($$2 += ($$13 /= $$14) * $$3) - $$6 == $$10.getZ()) {
                $$1 += (double)$$10.getY();
            } else if ($$11.getY() != 0 && Mth.floor($$0) - $$4 == $$11.getX() && Mth.floor($$2) - $$6 == $$11.getZ()) {
                $$1 += (double)$$11.getY();
            }
            return this.getPos($$0, $$1, $$2);
        }
        return null;
    }

    public @Nullable Vec3 getPos(double $$0, double $$1, double $$2) {
        BlockState $$6;
        int $$3 = Mth.floor($$0);
        int $$4 = Mth.floor($$1);
        int $$5 = Mth.floor($$2);
        if (this.level().getBlockState(new BlockPos($$3, $$4 - 1, $$5)).is(BlockTags.RAILS)) {
            --$$4;
        }
        if (BaseRailBlock.isRail($$6 = this.level().getBlockState(new BlockPos($$3, $$4, $$5)))) {
            double $$24;
            RailShape $$7 = $$6.getValue(((BaseRailBlock)$$6.getBlock()).getShapeProperty());
            Pair<Vec3i, Vec3i> $$8 = AbstractMinecart.exits($$7);
            Vec3i $$9 = (Vec3i)$$8.getFirst();
            Vec3i $$10 = (Vec3i)$$8.getSecond();
            double $$11 = (double)$$3 + 0.5 + (double)$$9.getX() * 0.5;
            double $$12 = (double)$$4 + 0.0625 + (double)$$9.getY() * 0.5;
            double $$13 = (double)$$5 + 0.5 + (double)$$9.getZ() * 0.5;
            double $$14 = (double)$$3 + 0.5 + (double)$$10.getX() * 0.5;
            double $$15 = (double)$$4 + 0.0625 + (double)$$10.getY() * 0.5;
            double $$16 = (double)$$5 + 0.5 + (double)$$10.getZ() * 0.5;
            double $$17 = $$14 - $$11;
            double $$18 = ($$15 - $$12) * 2.0;
            double $$19 = $$16 - $$13;
            if ($$17 == 0.0) {
                double $$20 = $$2 - (double)$$5;
            } else if ($$19 == 0.0) {
                double $$21 = $$0 - (double)$$3;
            } else {
                double $$22 = $$0 - $$11;
                double $$23 = $$2 - $$13;
                $$24 = ($$22 * $$17 + $$23 * $$19) * 2.0;
            }
            $$0 = $$11 + $$17 * $$24;
            $$1 = $$12 + $$18 * $$24;
            $$2 = $$13 + $$19 * $$24;
            if ($$18 < 0.0) {
                $$1 += 1.0;
            } else if ($$18 > 0.0) {
                $$1 += 0.5;
            }
            return new Vec3($$0, $$1, $$2);
        }
        return null;
    }

    @Override
    public double stepAlongTrack(BlockPos $$0, RailShape $$1, double $$2) {
        return 0.0;
    }

    @Override
    public boolean pushAndPickupEntities() {
        block4: {
            AABB $$0;
            block3: {
                $$0 = this.minecart.getBoundingBox().inflate(0.2f, 0.0, 0.2f);
                if (!this.minecart.isRideable() || !(this.getDeltaMovement().horizontalDistanceSqr() >= 0.01)) break block3;
                List<Entity> $$1 = this.level().getEntities(this.minecart, $$0, EntitySelector.pushableBy(this.minecart));
                if ($$1.isEmpty()) break block4;
                for (Entity $$2 : $$1) {
                    if ($$2 instanceof Player || $$2 instanceof IronGolem || $$2 instanceof AbstractMinecart || this.minecart.isVehicle() || $$2.isPassenger()) {
                        $$2.push(this.minecart);
                        continue;
                    }
                    $$2.startRiding(this.minecart);
                }
                break block4;
            }
            for (Entity $$3 : this.level().getEntities(this.minecart, $$0)) {
                if (this.minecart.hasPassenger($$3) || !$$3.isPushable() || !($$3 instanceof AbstractMinecart)) continue;
                $$3.push(this.minecart);
            }
        }
        return false;
    }

    @Override
    public Direction getMotionDirection() {
        return this.minecart.isFlipped() ? this.minecart.getDirection().getOpposite().getClockWise() : this.minecart.getDirection().getClockWise();
    }

    @Override
    public Vec3 getKnownMovement(Vec3 $$0) {
        if (Double.isNaN($$0.x) || Double.isNaN($$0.y) || Double.isNaN($$0.z)) {
            return Vec3.ZERO;
        }
        return new Vec3(Mth.clamp($$0.x, -0.4, 0.4), $$0.y, Mth.clamp($$0.z, -0.4, 0.4));
    }

    @Override
    public double getMaxSpeed(ServerLevel $$0) {
        return this.minecart.isInWater() ? 0.2 : 0.4;
    }

    @Override
    public double getSlowdownFactor() {
        return this.minecart.isVehicle() ? 0.997 : 0.96;
    }
}

