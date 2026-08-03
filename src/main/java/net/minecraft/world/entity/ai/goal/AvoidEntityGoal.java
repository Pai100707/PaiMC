package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AvoidEntityGoal<T extends net.minecraft.world.entity.LivingEntity> extends Goal {
   protected final net.minecraft.world.entity.PathfinderMob mob;
   private final double walkSpeedModifier;
   private final double sprintSpeedModifier;
   @Nullable
   protected T toAvoid;
   protected final float maxDist;
   @Nullable
   protected Path path;
   protected final PathNavigation pathNav;
   protected final Class<T> avoidClass;
   protected final Predicate<? super net.minecraft.world.entity.LivingEntity> avoidPredicate;
   protected final Predicate<? super net.minecraft.world.entity.LivingEntity> predicateOnAvoidEntity;
   private final TargetingConditions avoidEntityTargeting;

   public AvoidEntityGoal(net.minecraft.world.entity.PathfinderMob $$0, Class<T> $$1, float $$2, double $$3, double $$4) {
      this($$0, $$1, $$0x -> true, $$2, $$3, $$4, net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR);
   }

   public AvoidEntityGoal(
      net.minecraft.world.entity.PathfinderMob $$0,
      Class<T> $$1,
      Predicate<net.minecraft.world.entity.LivingEntity> $$2,
      float $$3,
      double $$4,
      double $$5,
      Predicate<? super net.minecraft.world.entity.LivingEntity> $$6
   ) {
      this.mob = $$0;
      this.avoidClass = $$1;
      this.avoidPredicate = $$2;
      this.maxDist = $$3;
      this.walkSpeedModifier = $$4;
      this.sprintSpeedModifier = $$5;
      this.predicateOnAvoidEntity = $$6;
      this.pathNav = $$0.getNavigation();
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      this.avoidEntityTargeting = TargetingConditions.forCombat().range($$3).selector(($$2x, $$3x) -> $$6.test($$2x) && $$2.test($$2x));
   }

   public AvoidEntityGoal(
      net.minecraft.world.entity.PathfinderMob $$0,
      Class<T> $$1,
      float $$2,
      double $$3,
      double $$4,
      Predicate<? super net.minecraft.world.entity.LivingEntity> $$5
   ) {
      this($$0, $$1, $$0x -> true, $$2, $$3, $$4, $$5);
   }

   @Override
   public boolean canUse() {
      this.toAvoid = (T)getServerLevel(this.mob)
         .getNearestEntity(
            this.mob.level().getEntitiesOfClass(this.avoidClass, this.mob.getBoundingBox().inflate(this.maxDist, 3.0, this.maxDist), $$0x -> true),
            this.avoidEntityTargeting,
            this.mob,
            this.mob.getX(),
            this.mob.getY(),
            this.mob.getZ()
         );
      if (this.toAvoid == null) {
         return false;
      } else {
         Vec3 $$0 = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.toAvoid.position());
         if ($$0 == null) {
            return false;
         } else if (this.toAvoid.distanceToSqr($$0.x, $$0.y, $$0.z) < this.toAvoid.distanceToSqr(this.mob)) {
            return false;
         } else {
            this.path = this.pathNav.createPath($$0.x, $$0.y, $$0.z, 0);
            return this.path != null;
         }
      }
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
      if (this.mob.distanceToSqr(this.toAvoid) < 49.0) {
         this.mob.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
      } else {
         this.mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
      }
   }
}
