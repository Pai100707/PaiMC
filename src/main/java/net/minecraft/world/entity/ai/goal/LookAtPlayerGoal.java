package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class LookAtPlayerGoal extends Goal {
   public static final float DEFAULT_PROBABILITY = 0.02F;
   protected final net.minecraft.world.entity.Mob mob;
   @Nullable
   protected net.minecraft.world.entity.Entity lookAt;
   protected final float lookDistance;
   private int lookTime;
   protected final float probability;
   private final boolean onlyHorizontal;
   protected final Class<? extends net.minecraft.world.entity.LivingEntity> lookAtType;
   protected final TargetingConditions lookAtContext;

   public LookAtPlayerGoal(net.minecraft.world.entity.Mob $$0, Class<? extends net.minecraft.world.entity.LivingEntity> $$1, float $$2) {
      this($$0, $$1, $$2, 0.02F);
   }

   public LookAtPlayerGoal(net.minecraft.world.entity.Mob $$0, Class<? extends net.minecraft.world.entity.LivingEntity> $$1, float $$2, float $$3) {
      this($$0, $$1, $$2, $$3, false);
   }

   public LookAtPlayerGoal(net.minecraft.world.entity.Mob $$0, Class<? extends net.minecraft.world.entity.LivingEntity> $$1, float $$2, float $$3, boolean $$4) {
      this.mob = $$0;
      this.lookAtType = $$1;
      this.lookDistance = $$2;
      this.probability = $$3;
      this.onlyHorizontal = $$4;
      this.setFlags(EnumSet.of(Goal.Flag.LOOK));
      if ($$1 == Player.class) {
         Predicate<net.minecraft.world.entity.Entity> $$5 = net.minecraft.world.entity.EntitySelector.notRiding($$0);
         this.lookAtContext = TargetingConditions.forNonCombat().range($$2).selector(($$1x, $$2x) -> $$5.test($$1x));
      } else {
         this.lookAtContext = TargetingConditions.forNonCombat().range($$2);
      }
   }

   @Override
   public boolean canUse() {
      if (this.mob.getRandom().nextFloat() >= this.probability) {
         return false;
      } else {
         if (this.mob.getTarget() != null) {
            this.lookAt = this.mob.getTarget();
         }

         ServerLevel $$0 = getServerLevel(this.mob);
         if (this.lookAtType == Player.class) {
            this.lookAt = $$0.getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
         } else {
            this.lookAt = $$0.getNearestEntity(
               this.mob.level().getEntitiesOfClass(this.lookAtType, this.mob.getBoundingBox().inflate(this.lookDistance, 3.0, this.lookDistance), $$0x -> true),
               this.lookAtContext,
               this.mob,
               this.mob.getX(),
               this.mob.getEyeY(),
               this.mob.getZ()
            );
         }

         return this.lookAt != null;
      }
   }

   @Override
   public boolean canContinueToUse() {
      if (!this.lookAt.isAlive()) {
         return false;
      } else {
         return this.mob.distanceToSqr(this.lookAt) > this.lookDistance * this.lookDistance ? false : this.lookTime > 0;
      }
   }

   @Override
   public void start() {
      this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
   }

   @Override
   public void stop() {
      this.lookAt = null;
   }

   @Override
   public void tick() {
      if (this.lookAt.isAlive()) {
         double $$0 = this.onlyHorizontal ? this.mob.getEyeY() : this.lookAt.getEyeY();
         this.mob.getLookControl().setLookAt(this.lookAt.getX(), $$0, this.lookAt.getZ());
         this.lookTime--;
      }
   }
}
