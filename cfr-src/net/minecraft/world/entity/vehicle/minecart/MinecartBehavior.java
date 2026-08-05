/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.vehicle.minecart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public abstract class MinecartBehavior {
    protected final AbstractMinecart minecart;

    protected MinecartBehavior(AbstractMinecart $$0) {
        this.minecart = $$0;
    }

    public InterpolationHandler getInterpolation() {
        return null;
    }

    public void lerpMotion(Vec3 $$0) {
        this.setDeltaMovement($$0);
    }

    public abstract void tick();

    public Level level() {
        return this.minecart.level();
    }

    public abstract void moveAlongTrack(ServerLevel var1);

    public abstract double stepAlongTrack(BlockPos var1, RailShape var2, double var3);

    public abstract boolean pushAndPickupEntities();

    public Vec3 getDeltaMovement() {
        return this.minecart.getDeltaMovement();
    }

    public void setDeltaMovement(Vec3 $$0) {
        this.minecart.setDeltaMovement($$0);
    }

    public void setDeltaMovement(double $$0, double $$1, double $$2) {
        this.minecart.setDeltaMovement($$0, $$1, $$2);
    }

    public Vec3 position() {
        return this.minecart.position();
    }

    public double getX() {
        return this.minecart.getX();
    }

    public double getY() {
        return this.minecart.getY();
    }

    public double getZ() {
        return this.minecart.getZ();
    }

    public void setPos(Vec3 $$0) {
        this.minecart.setPos($$0);
    }

    public void setPos(double $$0, double $$1, double $$2) {
        this.minecart.setPos($$0, $$1, $$2);
    }

    public float getXRot() {
        return this.minecart.getXRot();
    }

    public void setXRot(float $$0) {
        this.minecart.setXRot($$0);
    }

    public float getYRot() {
        return this.minecart.getYRot();
    }

    public void setYRot(float $$0) {
        this.minecart.setYRot($$0);
    }

    public Direction getMotionDirection() {
        return this.minecart.getDirection();
    }

    public Vec3 getKnownMovement(Vec3 $$0) {
        return $$0;
    }

    public abstract double getMaxSpeed(ServerLevel var1);

    public abstract double getSlowdownFactor();
}

