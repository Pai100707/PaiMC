package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.pathfinder.Path;

public class VillagerMakeLove extends Behavior<Villager> {
   private long birthTimestamp;

   public VillagerMakeLove() {
      super(
         ImmutableMap.of(
            MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
         ),
         350,
         350
      );
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Villager $$1) {
      return this.isBreedingPossible($$1);
   }

   protected boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
      return $$2 <= this.birthTimestamp && this.isBreedingPossible($$1);
   }

   protected void start(ServerLevel $$0, Villager $$1, long $$2) {
      net.minecraft.world.entity.AgeableMob $$3 = $$1.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
      BehaviorUtils.lockGazeAndWalkToEachOther($$1, $$3, 0.5F, 2);
      $$0.broadcastEntityEvent($$3, (byte)18);
      $$0.broadcastEntityEvent($$1, (byte)18);
      int $$4 = 275 + $$1.getRandom().nextInt(50);
      this.birthTimestamp = $$2 + $$4;
   }

   protected void tick(ServerLevel $$0, Villager $$1, long $$2) {
      Villager $$3 = (Villager)$$1.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
      if (!($$1.distanceToSqr($$3) > 5.0)) {
         BehaviorUtils.lockGazeAndWalkToEachOther($$1, $$3, 0.5F, 2);
         if ($$2 >= this.birthTimestamp) {
            $$1.eatAndDigestFood();
            $$3.eatAndDigestFood();
            this.tryToGiveBirth($$0, $$1, $$3);
         } else if ($$1.getRandom().nextInt(35) == 0) {
            $$0.broadcastEntityEvent($$3, (byte)12);
            $$0.broadcastEntityEvent($$1, (byte)12);
         }
      }
   }

   private void tryToGiveBirth(ServerLevel $$0, Villager $$1, Villager $$2) {
      Optional<BlockPos> $$3 = this.takeVacantBed($$0, $$1);
      if ($$3.isEmpty()) {
         $$0.broadcastEntityEvent($$2, (byte)13);
         $$0.broadcastEntityEvent($$1, (byte)13);
      } else {
         Optional<Villager> $$4 = this.breed($$0, $$1, $$2);
         if ($$4.isPresent()) {
            this.giveBedToChild($$0, $$4.get(), $$3.get());
         } else {
            $$0.getPoiManager().release($$3.get());
            $$0.debugSynchronizers().updatePoi($$3.get());
         }
      }
   }

   protected void stop(ServerLevel $$0, Villager $$1, long $$2) {
      $$1.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
   }

   private boolean isBreedingPossible(Villager $$0) {
      Brain<Villager> $$1 = $$0.getBrain();
      Optional<net.minecraft.world.entity.AgeableMob> $$2 = $$1.getMemory(MemoryModuleType.BREED_TARGET)
         .filter($$0x -> $$0x.getType() == net.minecraft.world.entity.EntityType.VILLAGER);
      return $$2.isEmpty()
         ? false
         : BehaviorUtils.targetIsValid($$1, MemoryModuleType.BREED_TARGET, net.minecraft.world.entity.EntityType.VILLAGER)
            && $$0.canBreed()
            && $$2.get().canBreed();
   }

   private Optional<BlockPos> takeVacantBed(ServerLevel $$0, Villager $$1) {
      return $$0.getPoiManager().take($$0x -> $$0x.is(PoiTypes.HOME), ($$1x, $$2) -> this.canReach($$1, $$2, $$1x), $$1.blockPosition(), 48);
   }

   private boolean canReach(Villager $$0, BlockPos $$1, Holder<PoiType> $$2) {
      Path $$3 = $$0.getNavigation().createPath($$1, ((PoiType)$$2.value()).validRange());
      return $$3 != null && $$3.canReach();
   }

   private Optional<Villager> breed(ServerLevel $$0, Villager $$1, Villager $$2) {
      Villager $$3 = $$1.getBreedOffspring($$0, $$2);
      if ($$3 == null) {
         return Optional.empty();
      } else {
         $$1.setAge(6000);
         $$2.setAge(6000);
         $$3.setAge(-24000);
         $$3.snapTo($$1.getX(), $$1.getY(), $$1.getZ(), 0.0F, 0.0F);
         $$0.addFreshEntityWithPassengers($$3);
         $$0.broadcastEntityEvent($$3, (byte)12);
         return Optional.of($$3);
      }
   }

   private void giveBedToChild(ServerLevel $$0, Villager $$1, BlockPos $$2) {
      GlobalPos $$3 = GlobalPos.of($$0.dimension(), $$2);
      $$1.getBrain().setMemory(MemoryModuleType.HOME, $$3);
   }
}
