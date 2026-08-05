/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.control;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.Control;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MoveControl
implements Control {
    public static final float MIN_SPEED = 5.0E-4f;
    public static final float MIN_SPEED_SQR = 2.5000003E-7f;
    protected static final int MAX_TURN = 90;
    protected final Mob mob;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected double speedModifier;
    protected float strafeForwards;
    protected float strafeRight;
    protected Operation operation = Operation.WAIT;

    public MoveControl(Mob $$0) {
        this.mob = $$0;
    }

    public boolean hasWanted() {
        return this.operation == Operation.MOVE_TO;
    }

    public double getSpeedModifier() {
        return this.speedModifier;
    }

    public void setWantedPosition(double $$0, double $$1, double $$2, double $$3) {
        this.wantedX = $$0;
        this.wantedY = $$1;
        this.wantedZ = $$2;
        this.speedModifier = $$3;
        if (this.operation != Operation.JUMPING) {
            this.operation = Operation.MOVE_TO;
        }
    }

    public void strafe(float $$0, float $$1) {
        this.operation = Operation.STRAFE;
        this.strafeForwards = $$0;
        this.strafeRight = $$1;
        this.speedModifier = 0.25;
    }

    public void tick() {
        if (this.operation == Operation.STRAFE) {
            float $$8;
            float $$0 = (float)this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float $$1 = (float)this.speedModifier * $$0;
            float $$2 = this.strafeForwards;
            float $$3 = this.strafeRight;
            float $$4 = Mth.sqrt($$2 * $$2 + $$3 * $$3);
            if ($$4 < 1.0f) {
                $$4 = 1.0f;
            }
            $$4 = $$1 / $$4;
            float $$5 = Mth.sin(this.mob.getYRot() * ((float)Math.PI / 180));
            float $$6 = Mth.cos(this.mob.getYRot() * ((float)Math.PI / 180));
            float $$7 = ($$2 *= $$4) * $$6 - ($$3 *= $$4) * $$5;
            if (!this.isWalkable($$7, $$8 = $$3 * $$6 + $$2 * $$5)) {
                this.strafeForwards = 1.0f;
                this.strafeRight = 0.0f;
            }
            this.mob.setSpeed($$1);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = Operation.WAIT;
        } else if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double $$9 = this.wantedX - this.mob.getX();
            double $$10 = this.wantedZ - this.mob.getZ();
            double $$11 = this.wantedY - this.mob.getY();
            double $$12 = $$9 * $$9 + $$11 * $$11 + $$10 * $$10;
            if ($$12 < 2.500000277905201E-7) {
                this.mob.setZza(0.0f);
                return;
            }
            float $$13 = (float)(Mth.atan2($$10, $$9) * 57.2957763671875) - 90.0f;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), $$13, 90.0f));
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            BlockPos $$14 = this.mob.blockPosition();
            BlockState $$15 = this.mob.level().getBlockState($$14);
            VoxelShape $$16 = $$15.getCollisionShape(this.mob.level(), $$14);
            if ($$11 > (double)this.mob.maxUpStep() && $$9 * $$9 + $$10 * $$10 < (double)Math.max(1.0f, this.mob.getBbWidth()) || !$$16.isEmpty() && this.mob.getY() < $$16.max(Direction.Axis.Y) + (double)$$14.getY() && !$$15.is(BlockTags.DOORS) && !$$15.is(BlockTags.FENCES)) {
                this.mob.getJumpControl().jump();
                this.operation = Operation.JUMPING;
            }
        } else if (this.operation == Operation.JUMPING) {
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            if (this.mob.onGround() || this.mob.isInLiquid() && this.mob.isAffectedByFluids()) {
                this.operation = Operation.WAIT;
            }
        } else {
            this.mob.setZza(0.0f);
        }
    }

    private boolean isWalkable(float $$0, float $$1) {
        NodeEvaluator $$3;
        PathNavigation $$2 = this.mob.getNavigation();
        return $$2 == null || ($$3 = $$2.getNodeEvaluator()) == null || $$3.getPathType(this.mob, BlockPos.containing(this.mob.getX() + (double)$$0, this.mob.getBlockY(), this.mob.getZ() + (double)$$1)) == PathType.WALKABLE;
    }

    protected float rotlerp(float $$0, float $$1, float $$2) {
        float $$4;
        float $$3 = Mth.wrapDegrees($$1 - $$0);
        if ($$3 > $$2) {
            $$3 = $$2;
        }
        if ($$3 < -$$2) {
            $$3 = -$$2;
        }
        if (($$4 = $$0 + $$3) < 0.0f) {
            $$4 += 360.0f;
        } else if ($$4 > 360.0f) {
            $$4 -= 360.0f;
        }
        return $$4;
    }

    public double getWantedX() {
        return this.wantedX;
    }

    public double getWantedY() {
        return this.wantedY;
    }

    public double getWantedZ() {
        return this.wantedZ;
    }

    public void setWait() {
        this.operation = Operation.WAIT;
    }

    protected static enum Operation {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING;

    }
}

