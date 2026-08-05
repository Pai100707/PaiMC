/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.equine;

import java.util.EnumSet;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class TraderLlama
extends Llama {
    private static final int DEFAULT_DESPAWN_DELAY = 47999;
    private int despawnDelay = 47999;

    public TraderLlama(EntityType<? extends TraderLlama> $$0, Level $$1) {
        super((EntityType<? extends Llama>)$$0, $$1);
    }

    @Override
    public boolean isTraderLlama() {
        return true;
    }

    @Override
    protected @Nullable Llama makeNewLlama() {
        return EntityType.TRADER_LLAMA.create(this.level(), EntitySpawnReason.BREEDING);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putInt("DespawnDelay", this.despawnDelay);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.despawnDelay = $$0.getIntOr("DespawnDelay", 47999);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
        this.targetSelector.addGoal(1, new TraderLlamaDefendWanderingTraderGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Zombie>((Mob)this, Zombie.class, true, ($$0, $$1) -> $$0.getType() != EntityType.ZOMBIFIED_PIGLIN));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<AbstractIllager>((Mob)this, AbstractIllager.class, true));
    }

    public void setDespawnDelay(int $$0) {
        this.despawnDelay = $$0;
    }

    @Override
    protected void doPlayerRide(Player $$0) {
        Entity $$1 = this.getLeashHolder();
        if ($$1 instanceof WanderingTrader) {
            return;
        }
        super.doPlayerRide($$0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            this.maybeDespawn();
        }
    }

    private void maybeDespawn() {
        if (!this.canDespawn()) {
            return;
        }
        int n = this.despawnDelay = this.isLeashedToWanderingTrader() ? ((WanderingTrader)this.getLeashHolder()).getDespawnDelay() - 1 : this.despawnDelay - 1;
        if (this.despawnDelay <= 0) {
            this.removeLeash();
            this.discard();
        }
    }

    private boolean canDespawn() {
        return !this.isTamed() && !this.isLeashedToSomethingOtherThanTheWanderingTrader() && !this.hasExactlyOnePlayerPassenger();
    }

    private boolean isLeashedToWanderingTrader() {
        return this.getLeashHolder() instanceof WanderingTrader;
    }

    private boolean isLeashedToSomethingOtherThanTheWanderingTrader() {
        return this.isLeashed() && !this.isLeashedToWanderingTrader();
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        if ($$2 == EntitySpawnReason.EVENT) {
            this.setAge(0);
        }
        if ($$3 == null) {
            $$3 = new AgeableMob.AgeableMobGroupData(false);
        }
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    protected static class TraderLlamaDefendWanderingTraderGoal
    extends TargetGoal {
        private final Llama llama;
        private LivingEntity ownerLastHurtBy;
        private int timestamp;

        public TraderLlamaDefendWanderingTraderGoal(Llama $$0) {
            super($$0, false);
            this.llama = $$0;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.llama.isLeashed()) {
                return false;
            }
            Entity $$0 = this.llama.getLeashHolder();
            if (!($$0 instanceof WanderingTrader)) {
                return false;
            }
            WanderingTrader $$1 = (WanderingTrader)$$0;
            this.ownerLastHurtBy = $$1.getLastHurtByMob();
            int $$2 = $$1.getLastHurtByMobTimestamp();
            return $$2 != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.ownerLastHurtBy);
            Entity $$0 = this.llama.getLeashHolder();
            if ($$0 instanceof WanderingTrader) {
                this.timestamp = ((WanderingTrader)$$0).getLastHurtByMobTimestamp();
            }
            super.start();
        }
    }
}

