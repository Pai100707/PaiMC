package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

public class AnimalPanic<E extends net.minecraft.world.entity.PathfinderMob> extends Behavior<E> {
   private static final int PANIC_MIN_DURATION = 100;
   private static final int PANIC_MAX_DURATION = 120;
   private static final int PANIC_DISTANCE_HORIZONTAL = 5;
   private static final int PANIC_DISTANCE_VERTICAL = 4;
   private final float speedMultiplier;
   private final Function<net.minecraft.world.entity.PathfinderMob, TagKey<DamageType>> panicCausingDamageTypes;
   private final Function<E, Vec3> positionGetter;

   public AnimalPanic(float $$0) {
      this($$0, $$0x -> DamageTypeTags.PANIC_CAUSES, $$0x -> LandRandomPos.getPos($$0x, 5, 4));
   }

   public AnimalPanic(float $$0, int $$1) {
      this(
         $$0,
         $$0x -> DamageTypeTags.PANIC_CAUSES,
         $$1x -> AirAndWaterRandomPos.getPos($$1x, 5, 4, $$1, $$1x.getViewVector(0.0F).x, $$1x.getViewVector(0.0F).z, (float) (Math.PI / 2))
      );
   }

   public AnimalPanic(float $$0, Function<net.minecraft.world.entity.PathfinderMob, TagKey<DamageType>> $$1) {
      this($$0, $$1, $$0x -> LandRandomPos.getPos($$0x, 5, 4));
   }

   public AnimalPanic(float $$0, Function<net.minecraft.world.entity.PathfinderMob, TagKey<DamageType>> $$1, Function<E, Vec3> $$2) {
      super(Map.of(MemoryModuleType.IS_PANICKING, MemoryStatus.REGISTERED, MemoryModuleType.HURT_BY, MemoryStatus.REGISTERED), 100, 120);
      this.speedMultiplier = $$0;
      this.panicCausingDamageTypes = $$1;
      this.positionGetter = $$2;
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, E $$1) {
      return $$1.getBrain().getMemory(MemoryModuleType.HURT_BY).map($$1x -> $$1x.is(this.panicCausingDamageTypes.apply($$1))).orElse(false)
         || $$1.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
   }

   protected boolean canStillUse(ServerLevel $$0, E $$1, long $$2) {
      return true;
   }

   protected void start(ServerLevel $$0, E $$1, long $$2) {
      $$1.getBrain().setMemory(MemoryModuleType.IS_PANICKING, true);
      $$1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      $$1.getNavigation().stop();
   }

   protected void stop(ServerLevel $$0, E $$1, long $$2) {
      Brain<?> $$3 = $$1.getBrain();
      $$3.eraseMemory(MemoryModuleType.IS_PANICKING);
   }

   protected void tick(ServerLevel $$0, E $$1, long $$2) {
      if ($$1.getNavigation().isDone()) {
         Vec3 $$3 = this.getPanicPos($$1, $$0);
         if ($$3 != null) {
            $$1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget($$3, this.speedMultiplier, 0));
         }
      }
   }

   
   private Vec3 getPanicPos(E $$0, ServerLevel $$1) {
      if ($$0.isOnFire()) {
         Optional<Vec3> $$2 = this.lookForWater($$1, $$0).map(Vec3::atBottomCenterOf);
         if ($$2.isPresent()) {
            return $$2.get();
         }
      }

      return this.positionGetter.apply($$0);
   }

   private Optional<BlockPos> lookForWater(BlockGetter $$0, net.minecraft.world.entity.Entity $$1) {
      BlockPos $$2 = $$1.blockPosition();
      if (!$$0.getBlockState($$2).getCollisionShape($$0, $$2).isEmpty()) {
         return Optional.empty();
      } else {
         Predicate<BlockPos> $$3;
         if (Mth.ceil($$1.getBbWidth()) == 2) {
            $$3 = $$1x -> BlockPos.squareOutSouthEast($$1x).allMatch($$1xx -> $$0.getFluidState($$1xx).is(FluidTags.WATER));
         } else {
            $$3 = $$1x -> $$0.getFluidState($$1x).is(FluidTags.WATER);
         }

         return BlockPos.findClosestMatch($$2, 5, 1, $$3);
      }
   }
}
