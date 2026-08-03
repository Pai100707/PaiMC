package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.pathfinder.Path;

public class YieldJobSite {
   public static BehaviorControl<Villager> create(float $$0) {
      return BehaviorBuilder.create(
         $$1 -> $$1.group(
               $$1.present(MemoryModuleType.POTENTIAL_JOB_SITE),
               $$1.absent(MemoryModuleType.JOB_SITE),
               $$1.present(MemoryModuleType.NEAREST_LIVING_ENTITIES),
               $$1.registered(MemoryModuleType.WALK_TARGET),
               $$1.registered(MemoryModuleType.LOOK_TARGET)
            )
            .apply(
               $$1,
               ($$2, $$3, $$4, $$5, $$6) -> ($$6x, $$7, $$8) -> {
                  if ($$7.isBaby()) {
                     return false;
                  } else if (!$$7.getVillagerData().profession().is(VillagerProfession.NONE)) {
                     return false;
                  } else {
                     BlockPos $$9 = $$1.<GlobalPos>get($$2).pos();
                     Optional<Holder<PoiType>> $$10 = $$6x.getPoiManager().getType($$9);
                     if ($$10.isEmpty()) {
                        return true;
                     } else {
                        $$1.<List<net.minecraft.world.entity.LivingEntity>>get($$4)
                           .stream()
                           .filter($$1xxx -> $$1xxx instanceof Villager && $$1xxx != $$7)
                           .map($$0xxxx -> (Villager)$$0xxxx)
                           .filter(net.minecraft.world.entity.LivingEntity::isAlive)
                           .filter($$2xx -> nearbyWantsJobsite($$10.get(), $$2xx, $$9))
                           .findFirst()
                           .ifPresent($$6xx -> {
                              $$5.erase();
                              $$6.erase();
                              $$2.erase();
                              if ($$6xx.getBrain().getMemory(MemoryModuleType.JOB_SITE).isEmpty()) {
                                 BehaviorUtils.setWalkAndLookTargetMemories($$6xx, $$9, $$0, 1);
                                 $$6xx.getBrain().setMemory(MemoryModuleType.POTENTIAL_JOB_SITE, GlobalPos.of($$6x.dimension(), $$9));
                                 $$6x.debugSynchronizers().updatePoi($$9);
                              }
                           });
                        return true;
                     }
                  }
               }
            )
      );
   }

   private static boolean nearbyWantsJobsite(Holder<PoiType> $$0, Villager $$1, BlockPos $$2) {
      boolean $$3 = $$1.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).isPresent();
      if ($$3) {
         return false;
      } else {
         Optional<GlobalPos> $$4 = $$1.getBrain().getMemory(MemoryModuleType.JOB_SITE);
         Holder<VillagerProfession> $$5 = $$1.getVillagerData().profession();
         if (((VillagerProfession)$$5.value()).heldJobSite().test($$0)) {
            return $$4.isEmpty() ? canReachPos($$1, $$2, (PoiType)$$0.value()) : $$4.get().pos().equals($$2);
         } else {
            return false;
         }
      }
   }

   private static boolean canReachPos(net.minecraft.world.entity.PathfinderMob $$0, BlockPos $$1, PoiType $$2) {
      Path $$3 = $$0.getNavigation().createPath($$1, $$2.validRange());
      return $$3 != null && $$3.canReach();
   }
}
