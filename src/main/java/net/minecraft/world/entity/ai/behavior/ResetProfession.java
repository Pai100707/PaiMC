package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

public class ResetProfession {
   public static BehaviorControl<Villager> create() {
      return BehaviorBuilder.create($$0 -> $$0.group($$0.absent(MemoryModuleType.JOB_SITE)).apply($$0, $$0x -> ($$0xx, $$1, $$2) -> {
         VillagerData $$3 = $$1.getVillagerData();
         boolean $$4 = !$$3.profession().is(VillagerProfession.NONE) && !$$3.profession().is(VillagerProfession.NITWIT);
         if ($$4 && $$1.getVillagerXp() == 0 && $$3.level() <= 1) {
            $$1.setVillagerData($$1.getVillagerData().withProfession($$0xx.registryAccess(), VillagerProfession.NONE));
            $$1.refreshBrain($$0xx);
            return true;
         } else {
            return false;
         }
      }));
   }
}
