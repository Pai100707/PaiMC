/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AvoidEntityGoal<T extends LivingEntity>
extends Goal {
    protected final PathfinderMob mob;
    private final double walkSpeedModifier;
    private final double sprintSpeedModifier;
    protected @Nullable T toAvoid;
    protected final float maxDist;
    protected @Nullable Path path;
    protected final PathNavigation pathNav;
    protected final Class<T> avoidClass;
    protected final Predicate<? super LivingEntity> avoidPredicate;
    protected final Predicate<? super LivingEntity> predicateOnAvoidEntity;
    private final TargetingConditions avoidEntityTargeting;

    public AvoidEntityGoal(PathfinderMob $$02, Class<T> $$1, float $$2, double $$3, double $$4) {
        this($$02, $$1, $$0 -> true, $$2, $$3, $$4, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
    }

    public AvoidEntityGoal(PathfinderMob $$0, Class<T> $$1, Predicate<LivingEntity> $$22, float $$32, double $$4, double $$5, Predicate<? super LivingEntity> $$6) {
        this.mob = $$0;
        this.avoidClass = $$1;
        this.avoidPredicate = $$22;
        this.maxDist = $$32;
        this.walkSpeedModifier = $$4;
        this.sprintSpeedModifier = $$5;
        this.predicateOnAvoidEntity = $$6;
        this.pathNav = $$0.getNavigation();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.avoidEntityTargeting = TargetingConditions.forCombat().range($$32).selector(($$2, $$3) -> $$6.test($$2) && $$22.test($$2));
    }

    public AvoidEntityGoal(PathfinderMob $$02, Class<T> $$1, float $$2, double $$3, double $$4, Predicate<? super LivingEntity> $$5) {
        this($$02, $$1, $$0 -> true, $$2, $$3, $$4, $$5);
    }

    @Override
    public boolean canUse() {
        this.toAvoid = AvoidEntityGoal.getServerLevel(this.mob).getNearestEntity(this.mob.level().getEntitiesOfClass(this.avoidClass, this.mob.getBoundingBox().inflate(this.maxDist, 3.0, this.maxDist), $$0 -> true), this.avoidEntityTargeting, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ());
        if (this.toAvoid == null) {
            return false;
        }
        Vec3 $$02 = DefaultRandomPos.getPosAway(this.mob, 16, 7, ((Entity)this.toAvoid).position());
        if ($$02 == null) {
            return false;
        }
        if (((Entity)this.toAvoid).distanceToSqr($$02.x, $$02.y, $$02.z) < ((Entity)this.toAvoid).distanceToSqr(this.mob)) {
            return false;
        }
        this.path = this.pathNav.createPath($$02.x, $$02.y, $$02.z, 0);
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.pathNav.isDone();
    }

    @Override
    public void start() {
        this.pathNav.moveTo(this.path, this.walkSpeedModifier);
    }

    @Override
    public void stop() {
        this.toAvoid = null;
    }

    @Override
    public void tick() {
        if (this.mob.distanceToSqr((Entity)this.toAvoid) < 49.0) {
            this.mob.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
        } else {
            this.mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
        }
    }
}

