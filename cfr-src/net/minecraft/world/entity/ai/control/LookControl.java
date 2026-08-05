/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.control;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.Control;
import net.minecraft.world.phys.Vec3;

public class LookControl
implements Control {
    protected final Mob mob;
    protected float yMaxRotSpeed;
    protected float xMaxRotAngle;
    protected int lookAtCooldown;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;

    public LookControl(Mob $$0) {
        this.mob = $$0;
    }

    public void setLookAt(Vec3 $$0) {
        this.setLookAt($$0.x, $$0.y, $$0.z);
    }

    public void setLookAt(Entity $$0) {
        this.setLookAt($$0.getX(), $$0.getEyeY(), $$0.getZ());
    }

    public void setLookAt(Entity $$0, float $$1, float $$2) {
        this.setLookAt($$0.getX(), $$0.getEyeY(), $$0.getZ(), $$1, $$2);
    }

    public void setLookAt(double $$0, double $$1, double $$2) {
        this.setLookAt($$0, $$1, $$2, this.mob.getHeadRotSpeed(), this.mob.getMaxHeadXRot());
    }

    public void setLookAt(double $$0, double $$1, double $$2, float $$3, float $$4) {
        this.wantedX = $$0;
        this.wantedY = $$1;
        this.wantedZ = $$2;
        this.yMaxRotSpeed = $$3;
        this.xMaxRotAngle = $$4;
        this.lookAtCooldown = 2;
    }

    public void tick() {
        if (this.resetXRotOnTick()) {
            this.mob.setXRot(0.0f);
        }
        if (this.lookAtCooldown > 0) {
            --this.lookAtCooldown;
            this.getYRotD().ifPresent($$0 -> {
                this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, $$0.floatValue(), this.yMaxRotSpeed);
            });
            this.getXRotD().ifPresent($$0 -> this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), $$0.floatValue(), this.xMaxRotAngle)));
        } else {
            this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0f);
        }
        this.clampHeadRotationToBody();
    }

    protected void clampHeadRotationToBody() {
        if (!this.mob.getNavigation().isDone()) {
            this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, this.mob.getMaxHeadYRot());
        }
    }

    protected boolean resetXRotOnTick() {
        return true;
    }

    public boolean isLookingAtTarget() {
        return this.lookAtCooldown > 0;
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

    protected Optional<Float> getXRotD() {
        double $$0 = this.wantedX - this.mob.getX();
        double $$1 = this.wantedY - this.mob.getEyeY();
        double $$2 = this.wantedZ - this.mob.getZ();
        double $$3 = Math.sqrt($$0 * $$0 + $$2 * $$2);
        return Math.abs($$1) > (double)1.0E-5f || Math.abs($$3) > (double)1.0E-5f ? Optional.of(Float.valueOf((float)(-(Mth.atan2($$1, $$3) * 57.2957763671875)))) : Optional.empty();
    }

    protected Optional<Float> getYRotD() {
        double $$0 = this.wantedX - this.mob.getX();
        double $$1 = this.wantedZ - this.mob.getZ();
        return Math.abs($$1) > (double)1.0E-5f || Math.abs($$0) > (double)1.0E-5f ? Optional.of(Float.valueOf((float)(Mth.atan2($$1, $$0) * 57.2957763671875) - 90.0f)) : Optional.empty();
    }
}

