/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

public class SmoothSwimmingLookControl
extends LookControl {
    private final int maxYRotFromCenter;
    private static final int HEAD_TILT_X = 10;
    private static final int HEAD_TILT_Y = 20;

    public SmoothSwimmingLookControl(Mob $$0, int $$1) {
        super($$0);
        this.maxYRotFromCenter = $$1;
    }

    @Override
    public void tick() {
        if (this.lookAtCooldown > 0) {
            --this.lookAtCooldown;
            this.getYRotD().ifPresent($$0 -> {
                this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, $$0.floatValue() + 20.0f, this.yMaxRotSpeed);
            });
            this.getXRotD().ifPresent($$0 -> this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), $$0.floatValue() + 10.0f, this.xMaxRotAngle)));
        } else {
            if (this.mob.getNavigation().isDone()) {
                this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), 0.0f, 5.0f));
            }
            this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, this.yMaxRotSpeed);
        }
        float $$02 = Mth.wrapDegrees(this.mob.yHeadRot - this.mob.yBodyRot);
        if ($$02 < (float)(-this.maxYRotFromCenter)) {
            this.mob.yBodyRot -= 4.0f;
        } else if ($$02 > (float)this.maxYRotFromCenter) {
            this.mob.yBodyRot += 4.0f;
        }
    }
}

