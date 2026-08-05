package net.minecraft.world.entity.animal.equine;

import java.util.EnumSet;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TraderLlama extends Llama {
   private static final int DEFAULT_DESPAWN_DELAY = 47999;
   private int despawnDelay = 47999;

   public TraderLlama(net.minecraft.world.entity.EntityType<? extends TraderLlama> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   public boolean isTraderLlama() {
      return true;
   }

   
   @Override
   protected Llama makeNewLlama() {
      return net.minecraft.world.entity.EntityType.TRADER_LLAMA.create(this.level(), net.minecraft.world.entity.EntitySpawnReason.BREEDING);
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
      this.targetSelector.addGoal(1, new TraderLlama.TraderLlamaDefendWanderingTraderGoal(this));
      this.targetSelector
         .addGoal(
            2,
            new NearestAttackableTargetGoal<>(this, Zombie.class, true, ($$0, $$1) -> $$0.getType() != net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN)
         );
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));
   }

   public void setDespawnDelay(int $$0) {
      this.despawnDelay = $$0;
   }

   @Override
   protected void doPlayerRide(Player $$0) {
      net.minecraft.world.entity.Entity $$1 = this.getLeashHolder();
      if (!($$1 instanceof WanderingTrader)) {
         super.doPlayerRide($$0);
      }
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide()) {
         this.maybeDespawn();
      }
   }

   private void maybeDespawn() {
      if (this.canDespawn()) {
         this.despawnDelay = this.isLeashedToWanderingTrader() ? ((WanderingTrader)this.getLeashHolder()).getDespawnDelay() - 1 : this.despawnDelay - 1;
         if (this.despawnDelay <= 0) {
            this.removeLeash();
            this.discard();
         }
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
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      if ($$2 == net.minecraft.world.entity.EntitySpawnReason.EVENT) {
         this.setAge(0);
      }

      if ($$3 == null) {
         $$3 = new net.minecraft.world.entity.AgeableMob.AgeableMobGroupData(false);
      }

      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   protected static class TraderLlamaDefendWanderingTraderGoal extends TargetGoal {
      private final Llama llama;
      private net.minecraft.world.entity.LivingEntity ownerLastHurtBy;
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
         } else if (!(this.llama.getLeashHolder() instanceof WanderingTrader $$1)) {
            return false;
         } else {
            this.ownerLastHurtBy = $$1.getLastHurtByMob();
            int $$2 = $$1.getLastHurtByMobTimestamp();
            return $$2 != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
         }
      }

      @Override
      public void start() {
         this.mob.setTarget(this.ownerLastHurtBy);
         net.minecraft.world.entity.Entity $$0 = this.llama.getLeashHolder();
         if ($$0 instanceof WanderingTrader) {
            this.timestamp = ((WanderingTrader)$$0).getLastHurtByMobTimestamp();
         }

         super.start();
      }
   }
}
