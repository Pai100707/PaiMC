package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class GoToPotentialJobSite extends Behavior<Villager> {
   private static final int TICKS_UNTIL_TIMEOUT = 1200;
   final float speedModifier;

   public GoToPotentialJobSite(float $$0) {
      super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_PRESENT), 1200);
      this.speedModifier = $$0;
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Villager $$1) {
      return $$1.getBrain().getActiveNonCoreActivity().map($$0x -> $$0x == Activity.IDLE || $$0x == Activity.WORK || $$0x == Activity.PLAY).orElse(true);
   }

   protected boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
      return $$1.getBrain().hasMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE);
   }

   protected void tick(ServerLevel $$0, Villager $$1, long $$2) {
      BehaviorUtils.setWalkAndLookTargetMemories($$1, $$1.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos(), this.speedModifier, 1);
   }

   protected void stop(ServerLevel $$0, Villager $$1, long $$2) {
      Optional<GlobalPos> $$3 = $$1.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
      $$3.ifPresent($$1x -> {
         BlockPos $$2x = $$1x.pos();
         ServerLevel $$3x = $$0.getServer().getLevel($$1x.dimension());
         if ($$3x != null) {
            PoiManager $$4 = $$3x.getPoiManager();
            if ($$4.exists($$2x, $$0xx -> true)) {
               $$4.release($$2x);
            }

            $$0.debugSynchronizers().updatePoi($$2x);
         }
      });
      $$1.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
   }
}
