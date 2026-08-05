/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.phys.Vec3;

public class LlamaFollowCaravanGoal
extends Goal {
    public final Llama llama;
    private double speedModifier;
    private static final int CARAVAN_LIMIT = 8;
    private int distCheckCounter;

    public LlamaFollowCaravanGoal(Llama $$0, double $$1) {
        this.llama = $$0;
        this.speedModifier = $$1;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.llama.isLeashed() || this.llama.inCaravan()) {
            return false;
        }
        List<Entity> $$02 = this.llama.level().getEntities(this.llama, this.llama.getBoundingBox().inflate(9.0, 4.0, 9.0), $$0 -> {
            EntityType<?> $$1 = $$0.getType();
            return $$1 == EntityType.LLAMA || $$1 == EntityType.TRADER_LLAMA;
        });
        Leashable $$1 = null;
        double $$2 = Double.MAX_VALUE;
        for (Entity $$3 : $$02) {
            double $$5;
            Llama $$4 = (Llama)$$3;
            if (!$$4.inCaravan() || $$4.hasCaravanTail() || ($$5 = this.llama.distanceToSqr($$4)) > $$2) continue;
            $$2 = $$5;
            $$1 = $$4;
        }
        if ($$1 == null) {
            for (Entity $$6 : $$02) {
                double $$8;
                Llama $$7 = (Llama)$$6;
                if (!$$7.isLeashed() || $$7.hasCaravanTail() || ($$8 = this.llama.distanceToSqr($$7)) > $$2) continue;
                $$2 = $$8;
                $$1 = $$7;
            }
        }
        if ($$1 == null) {
            return false;
        }
        if ($$2 < 4.0) {
            return false;
        }
        if (!$$1.isLeashed() && !this.firstIsLeashed((Llama)$$1, 1)) {
            return false;
        }
        this.llama.joinCaravan((Llama)$$1);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!(this.llama.inCaravan() && this.llama.getCaravanHead().isAlive() && this.firstIsLeashed(this.llama, 0))) {
            return false;
        }
        double $$0 = this.llama.distanceToSqr(this.llama.getCaravanHead());
        if ($$0 > 676.0) {
            if (this.speedModifier <= 3.0) {
                this.speedModifier *= 1.2;
                this.distCheckCounter = LlamaFollowCaravanGoal.reducedTickDelay(40);
                return true;
            }
            if (this.distCheckCounter == 0) {
                return false;
            }
        }
        if (this.distCheckCounter > 0) {
            --this.distCheckCounter;
        }
        return true;
    }

    @Override
    public void stop() {
        this.llama.leaveCaravan();
        this.speedModifier = 2.1;
    }

    @Override
    public void tick() {
        if (!this.llama.inCaravan()) {
            return;
        }
        if (this.llama.getLeashHolder() instanceof LeashFenceKnotEntity) {
            return;
        }
        Llama $$0 = this.llama.getCaravanHead();
        double $$1 = this.llama.distanceTo($$0);
        float $$2 = 2.0f;
        Vec3 $$3 = new Vec3($$0.getX() - this.llama.getX(), $$0.getY() - this.llama.getY(), $$0.getZ() - this.llama.getZ()).normalize().scale(Math.max($$1 - 2.0, 0.0));
        this.llama.getNavigation().moveTo(this.llama.getX() + $$3.x, this.llama.getY() + $$3.y, this.llama.getZ() + $$3.z, this.speedModifier);
    }

    private boolean firstIsLeashed(Llama $$0, int $$1) {
        if ($$1 > 8) {
            return false;
        }
        if ($$0.inCaravan()) {
            if ($$0.getCaravanHead().isLeashed()) {
                return true;
            }
            return this.firstIsLeashed($$0.getCaravanHead(), ++$$1);
        }
        return false;
    }
}

