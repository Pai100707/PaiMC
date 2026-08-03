package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;

public class AnimalMakeLove extends Behavior<Animal> {
   private static final int BREED_RANGE = 3;
   private static final int MIN_DURATION = 60;
   private static final int MAX_DURATION = 110;
   private final net.minecraft.world.entity.EntityType<? extends Animal> partnerType;
   private final float speedModifier;
   private final int closeEnoughDistance;
   private static final int DEFAULT_CLOSE_ENOUGH_DISTANCE = 2;
   private long spawnChildAtTime;

   public AnimalMakeLove(net.minecraft.world.entity.EntityType<? extends Animal> $$0) {
      this($$0, 1.0F, 2);
   }

   public AnimalMakeLove(net.minecraft.world.entity.EntityType<? extends Animal> $$0, float $$1, int $$2) {
      super(
         ImmutableMap.of(
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.BREED_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.LOOK_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.IS_PANICKING,
            MemoryStatus.VALUE_ABSENT
         ),
         110
      );
      this.partnerType = $$0;
      this.speedModifier = $$1;
      this.closeEnoughDistance = $$2;
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Animal $$1) {
      return $$1.isInLove() && this.findValidBreedPartner($$1).isPresent();
   }

   protected void start(ServerLevel $$0, Animal $$1, long $$2) {
      Animal $$3 = this.findValidBreedPartner($$1).get();
      $$1.getBrain().setMemory(MemoryModuleType.BREED_TARGET, $$3);
      $$3.getBrain().setMemory(MemoryModuleType.BREED_TARGET, $$1);
      BehaviorUtils.lockGazeAndWalkToEachOther($$1, $$3, this.speedModifier, this.closeEnoughDistance);
      int $$4 = 60 + $$1.getRandom().nextInt(50);
      this.spawnChildAtTime = $$2 + $$4;
   }

   protected boolean canStillUse(ServerLevel $$0, Animal $$1, long $$2) {
      if (!this.hasBreedTargetOfRightType($$1)) {
         return false;
      } else {
         Animal $$3 = this.getBreedTarget($$1);
         return $$3.isAlive()
            && $$1.canMate($$3)
            && BehaviorUtils.entityIsVisible($$1.getBrain(), $$3)
            && $$2 <= this.spawnChildAtTime
            && !$$1.isPanicking()
            && !$$3.isPanicking();
      }
   }

   protected void tick(ServerLevel $$0, Animal $$1, long $$2) {
      Animal $$3 = this.getBreedTarget($$1);
      BehaviorUtils.lockGazeAndWalkToEachOther($$1, $$3, this.speedModifier, this.closeEnoughDistance);
      if ($$1.closerThan($$3, 3.0)) {
         if ($$2 >= this.spawnChildAtTime) {
            $$1.spawnChildFromBreeding($$0, $$3);
            $$1.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
            $$3.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
         }
      }
   }

   protected void stop(ServerLevel $$0, Animal $$1, long $$2) {
      $$1.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
      $$1.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      $$1.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      this.spawnChildAtTime = 0L;
   }

   private Animal getBreedTarget(Animal $$0) {
      return (Animal)$$0.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
   }

   private boolean hasBreedTargetOfRightType(Animal $$0) {
      Brain<?> $$1 = $$0.getBrain();
      return $$1.hasMemoryValue(MemoryModuleType.BREED_TARGET) && $$1.getMemory(MemoryModuleType.BREED_TARGET).get().getType() == this.partnerType;
   }

   private Optional<? extends Animal> findValidBreedPartner(Animal $$0) {
      return $$0.getBrain()
         .getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
         .get()
         .findClosest($$1 -> $$1.getType() == this.partnerType && $$1 instanceof Animal $$2 && $$0.canMate($$2) && !$$2.isPanicking())
         .map(Animal.class::cast);
   }
}
