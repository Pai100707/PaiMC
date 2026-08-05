/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

public class FlyingMoveControl
extends MoveControl {
    private final int maxTurn;
    private final boolean hoversInPlace;

    public FlyingMoveControl(Mob $$0, int $$1, boolean $$2) {
        super($$0);
        this.maxTurn = $$1;
        this.hoversInPlace = $$2;
    }

    @Override
    public void tick() {
        if (this.operation == MoveControl.Operation.MOVE_TO) {
            float $$6;
            this.operation = MoveControl.Operation.WAIT;
            this.mob.setNoGravity(true);
            double $$0 = this.wantedX - this.mob.getX();
            double $$1 = this.wantedY - this.mob.getY();
            double $$2 = this.wantedZ - this.mob.getZ();
            double $$3 = $$0 * $$0 + $$1 * $$1 + $$2 * $$2;
            if ($$3 < 2.500000277905201E-7) {
                this.mob.setYya(0.0f);
                this.mob.setZza(0.0f);
                return;
            }
            float $$4 = (float)(Mth.atan2($$2, $$0) * 57.2957763671875) - 90.0f;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), $$4, 90.0f));
            if (this.mob.onGround()) {
                float $$5 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            } else {
                $$6 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));
            }
            this.mob.setSpeed($$6);
            double $$7 = Math.sqrt($$0 * $$0 + $$2 * $$2);
            if (Math.abs($$1) > (double)1.0E-5f || Math.abs($$7) > (double)1.0E-5f) {
                float $$8 = (float)(-(Mth.atan2($$1, $$7) * 57.2957763671875));
                this.mob.setXRot(this.rotlerp(this.mob.getXRot(), $$8, this.maxTurn));
                this.mob.setYya($$1 > 0.0 ? $$6 : -$$6);
            }
        } else {
            if (!this.hoversInPlace) {
                this.mob.setNoGravity(false);
            }
            this.mob.setYya(0.0f);
            this.mob.setZza(0.0f);
        }
    }
}

