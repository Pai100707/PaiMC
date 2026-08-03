package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

public class PoiCompetitorScan {
   public static BehaviorControl<Villager> create() {
      return BehaviorBuilder.create(
         $$0 -> $$0.group($$0.present(MemoryModuleType.JOB_SITE), $$0.present(MemoryModuleType.NEAREST_LIVING_ENTITIES))
            .apply(
               $$0,
               ($$1, $$2) -> ($$3, $$4, $$5) -> {
                  GlobalPos $$6 = $$0.get($$1);
                  $$3.getPoiManager()
                     .getType($$6.pos())
                     .ifPresent(
                        $$4x -> $$0.<List<net.minecraft.world.entity.LivingEntity>>get($$2)
                           .stream()
                           .filter($$1xxx -> $$1xxx instanceof Villager && $$1xxx != $$4)
                           .map($$0xxxx -> (Villager)$$0xxxx)
                           .filter(net.minecraft.world.entity.LivingEntity::isAlive)
                           .filter($$2xxx -> competesForSameJobsite($$6, $$4x, $$2xxx))
                           .reduce($$4, PoiCompetitorScan::selectWinner)
                     );
                  return true;
               }
            )
      );
   }

   private static Villager selectWinner(Villager $$0, Villager $$1) {
      Villager $$2;
      Villager $$3;
      if ($$0.getVillagerXp() > $$1.getVillagerXp()) {
         $$2 = $$0;
         $$3 = $$1;
      } else {
         $$2 = $$1;
         $$3 = $$0;
      }

      $$3.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
      return $$2;
   }

   private static boolean competesForSameJobsite(GlobalPos $$0, Holder<PoiType> $$1, Villager $$2) {
      Optional<GlobalPos> $$3 = $$2.getBrain().getMemory(MemoryModuleType.JOB_SITE);
      return $$3.isPresent() && $$0.equals($$3.get()) && hasMatchingProfession($$1, $$2.getVillagerData().profession());
   }

   private static boolean hasMatchingProfession(Holder<PoiType> $$0, Holder<VillagerProfession> $$1) {
      return ((VillagerProfession)$$1.value()).heldJobSite().test($$0);
   }
}
