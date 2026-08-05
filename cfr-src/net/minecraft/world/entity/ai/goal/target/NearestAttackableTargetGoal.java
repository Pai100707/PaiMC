/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class NearestAttackableTargetGoal<T extends LivingEntity>
extends TargetGoal {
    private static final int DEFAULT_RANDOM_INTERVAL = 10;
    protected final Class<T> targetType;
    protected final int randomInterval;
    protected @Nullable LivingEntity target;
    protected TargetingConditions targetConditions;

    public NearestAttackableTargetGoal(Mob $$0, Class<T> $$1, boolean $$2) {
        this($$0, $$1, 10, $$2, false, null);
    }

    public NearestAttackableTargetGoal(Mob $$0, Class<T> $$1, boolean $$2, TargetingConditions.Selector $$3) {
        this($$0, $$1, 10, $$2, false, $$3);
    }

    public NearestAttackableTargetGoal(Mob $$0, Class<T> $$1, boolean $$2, boolean $$3) {
        this($$0, $$1, 10, $$2, $$3, null);
    }

    public NearestAttackableTargetGoal(Mob $$0, Class<T> $$1, int $$2, boolean $$3, boolean $$4, @Nullable TargetingConditions.Selector $$5) {
        super($$0, $$3, $$4);
        this.targetType = $$1;
        this.randomInterval = NearestAttackableTargetGoal.reducedTickDelay($$2);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector($$5);
    }

    @Override
    public boolean canUse() {
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        }
        this.findTarget();
        return this.target != null;
    }

    protected AABB getTargetSearchArea(double $$0) {
        return this.mob.getBoundingBox().inflate($$0, $$0, $$0);
    }

    protected void findTarget() {
        ServerLevel $$02 = NearestAttackableTargetGoal.getServerLevel(this.mob);
        this.target = this.targetType == Player.class || this.targetType == ServerPlayer.class ? $$02.getNearestPlayer(this.getTargetConditions(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()) : $$02.getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), $$0 -> true), this.getTargetConditions(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable LivingEntity $$0) {
        this.target = $$0;
    }

    private TargetingConditions getTargetConditions() {
        return this.targetConditions.range(this.getFollowDistance());
    }
}

