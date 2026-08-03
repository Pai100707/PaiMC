package net.minecraft.world.entity.monster.piglin;

import java.util.List;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

public class StartHuntingHoglin {
   public static OneShot<Piglin> create() {
      return BehaviorBuilder.create(
         $$0 -> $$0.group(
               $$0.present(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN),
               $$0.absent(MemoryModuleType.ANGRY_AT),
               $$0.absent(MemoryModuleType.HUNTED_RECENTLY),
               $$0.registered(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS)
            )
            .apply($$0, ($$1, $$2, $$3, $$4) -> ($$3x, $$4x, $$5) -> {
               if (!$$4x.isBaby() && !$$0.<List>tryGet($$4).map($$0xxx -> $$0xxx.stream().anyMatch(StartHuntingHoglin::hasHuntedRecently)).isPresent()) {
                  Hoglin $$6 = $$0.get($$1);
                  PiglinAi.setAngerTarget($$3x, $$4x, $$6);
                  PiglinAi.dontKillAnyMoreHoglinsForAWhile($$4x);
                  PiglinAi.broadcastAngerTarget($$3x, $$4x, $$6);
                  $$0.<List>tryGet($$4).ifPresent($$0xxx -> $$0xxx.forEach(PiglinAi::dontKillAnyMoreHoglinsForAWhile));
                  return true;
               } else {
                  return false;
               }
            })
      );
   }

   private static boolean hasHuntedRecently(AbstractPiglin $$0) {
      return $$0.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY);
   }
}
