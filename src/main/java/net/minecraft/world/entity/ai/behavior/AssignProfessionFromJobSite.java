package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

public class AssignProfessionFromJobSite {
   public static BehaviorControl<Villager> create() {
      return BehaviorBuilder.create(
         $$0 -> $$0.group($$0.present(MemoryModuleType.POTENTIAL_JOB_SITE), $$0.registered(MemoryModuleType.JOB_SITE))
            .apply(
               $$0,
               ($$1, $$2) -> ($$3, $$4, $$5) -> {
                  GlobalPos $$6 = $$0.get($$1);
                  if (!$$6.pos().closerToCenterThan($$4.position(), 2.0) && !$$4.assignProfessionWhenSpawned()) {
                     return false;
                  } else {
                     $$1.erase();
                     $$2.set($$6);
                     $$3.broadcastEntityEvent($$4, (byte)14);
                     if (!$$4.getVillagerData().profession().is(VillagerProfession.NONE)) {
                        return true;
                     } else {
                        MinecraftServer $$7 = $$3.getServer();
                        Optional.ofNullable($$7.getLevel($$6.dimension()))
                           .flatMap($$1xx -> $$1xx.getPoiManager().getType($$6.pos()))
                           .flatMap(
                              $$0xxx -> BuiltInRegistries.VILLAGER_PROFESSION
                                 .listElements()
                                 .filter($$1xx -> ((VillagerProfession)$$1xx.value()).heldJobSite().test($$0xxx))
                                 .findFirst()
                           )
                           .ifPresent($$2xx -> {
                              $$4.setVillagerData($$4.getVillagerData().withProfession($$2xx));
                              $$4.refreshBrain($$3);
                           });
                        return true;
                     }
                  }
               }
            )
      );
   }
}
