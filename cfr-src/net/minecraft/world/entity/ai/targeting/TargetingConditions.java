/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.targeting;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;

public class TargetingConditions {
    public static final TargetingConditions DEFAULT = TargetingConditions.forCombat();
    private static final double MIN_VISIBILITY_DISTANCE_FOR_INVISIBLE_TARGET = 2.0;
    private final boolean isCombat;
    private double range = -1.0;
    private boolean checkLineOfSight = true;
    private boolean testInvisible = true;
    private @Nullable Selector selector;

    private TargetingConditions(boolean $$0) {
        this.isCombat = $$0;
    }

    public static TargetingConditions forCombat() {
        return new TargetingConditions(true);
    }

    public static TargetingConditions forNonCombat() {
        return new TargetingConditions(false);
    }

    public TargetingConditions copy() {
        TargetingConditions $$0 = this.isCombat ? TargetingConditions.forCombat() : TargetingConditions.forNonCombat();
        $$0.range = this.range;
        $$0.checkLineOfSight = this.checkLineOfSight;
        $$0.testInvisible = this.testInvisible;
        $$0.selector = this.selector;
        return $$0;
    }

    public TargetingConditions range(double $$0) {
        this.range = $$0;
        return this;
    }

    public TargetingConditions ignoreLineOfSight() {
        this.checkLineOfSight = false;
        return this;
    }

    public TargetingConditions ignoreInvisibilityTesting() {
        this.testInvisible = false;
        return this;
    }

    public TargetingConditions selector(@Nullable Selector $$0) {
        this.selector = $$0;
        return this;
    }

    public boolean test(ServerLevel $$0, @Nullable LivingEntity $$1, LivingEntity $$2) {
        if ($$1 == $$2) {
            return false;
        }
        if (!$$2.canBeSeenByAnyone()) {
            return false;
        }
        if (this.selector != null && !this.selector.test($$2, $$0)) {
            return false;
        }
        if ($$1 == null) {
            if (this.isCombat && (!$$2.canBeSeenAsEnemy() || $$0.getDifficulty() == Difficulty.PEACEFUL)) {
                return false;
            }
        } else {
            Mob $$6;
            if (this.isCombat && (!$$1.canAttack($$2) || !$$1.canAttackType($$2.getType()) || $$1.isAlliedTo($$2))) {
                return false;
            }
            if (this.range > 0.0) {
                double $$3 = this.testInvisible ? $$2.getVisibilityPercent($$1) : 1.0;
                double $$4 = Math.max(this.range * $$3, 2.0);
                double $$5 = $$1.distanceToSqr($$2.getX(), $$2.getY(), $$2.getZ());
                if ($$5 > $$4 * $$4) {
                    return false;
                }
            }
            if (this.checkLineOfSight && $$1 instanceof Mob && !($$6 = (Mob)$$1).getSensing().hasLineOfSight($$2)) {
                return false;
            }
        }
        return true;
    }

    @FunctionalInterface
    public static interface Selector {
        public boolean test(LivingEntity var1, ServerLevel var2);
    }
}

