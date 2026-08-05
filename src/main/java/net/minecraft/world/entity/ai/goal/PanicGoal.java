package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class PanicGoal extends Goal {
   public static final int WATER_CHECK_DISTANCE_VERTICAL = 1;
   protected final net.minecraft.world.entity.PathfinderMob mob;
   protected final double speedModifier;
   protected double posX;
   protected double posY;
   protected double posZ;
   protected boolean isRunning;
   private final Function<net.minecraft.world.entity.PathfinderMob, TagKey<DamageType>> panicCausingDamageTypes;

   public PanicGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1) {
      this($$0, $$1, DamageTypeTags.PANIC_CAUSES);
   }

   public PanicGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, TagKey<DamageType> $$2) {
      this($$0, $$1, (Function<net.minecraft.world.entity.PathfinderMob, TagKey<DamageType>>)($$1x -> $$2));
   }

   public PanicGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, Function<net.minecraft.world.entity.PathfinderMob, TagKey<DamageType>> $$2) {
      this.mob = $$0;
      this.speedModifier = $$1;
      this.panicCausingDamageTypes = $$2;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (!this.shouldPanic()) {
         return false;
      } else {
         if (this.mob.isOnFire()) {
            BlockPos $$0 = this.lookForWater(this.mob.level(), this.mob, 5);
            if ($$0 != null) {
               this.posX = $$0.getX();
               this.posY = $$0.getY();
               this.posZ = $$0.getZ();
               return true;
            }
         }

         return this.findRandomPosition();
      }
   }

   protected boolean shouldPanic() {
      return this.mob.getLastDamageSource() != null && this.mob.getLastDamageSource().is(this.panicCausingDamageTypes.apply(this.mob));
   }

   protected boolean findRandomPosition() {
      Vec3 $$0 = DefaultRandomPos.getPos(this.mob, 5, 4);
      if ($$0 == null) {
         return false;
      } else {
         this.posX = $$0.x;
         this.posY = $$0.y;
         this.posZ = $$0.z;
         return true;
      }
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   @Override
   public void start() {
      this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
      this.isRunning = true;
   }

   @Override
   public void stop() {
      this.isRunning = false;
   }

   @Override
   public boolean canContinueToUse() {
      return !this.mob.getNavigation().isDone();
   }

   
   protected BlockPos lookForWater(BlockGetter $$0, net.minecraft.world.entity.Entity $$1, int $$2) {
      BlockPos $$3 = $$1.blockPosition();
      return !$$0.getBlockState($$3).getCollisionShape($$0, $$3).isEmpty()
         ? null
         : (BlockPos)BlockPos.findClosestMatch($$1.blockPosition(), $$2, 1, $$1x -> $$0.getFluidState($$1x).is(FluidTags.WATER)).orElse(null);
   }
}
