package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.villager.Villager;

public class WorkAtPoi extends Behavior<Villager> {
   private static final int CHECK_COOLDOWN = 300;
   private static final double DISTANCE = 1.73;
   private long lastCheck;

   public WorkAtPoi() {
      super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Villager $$1) {
      if ($$0.getGameTime() - this.lastCheck < 300L) {
         return false;
      } else if ($$0.random.nextInt(2) != 0) {
         return false;
      } else {
         this.lastCheck = $$0.getGameTime();
         GlobalPos $$2 = $$1.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
         return $$2.dimension() == $$0.dimension() && $$2.pos().closerToCenterThan($$1.position(), 1.73);
      }
   }

   protected void start(ServerLevel $$0, Villager $$1, long $$2) {
      Brain<Villager> $$3 = $$1.getBrain();
      $$3.setMemory(MemoryModuleType.LAST_WORKED_AT_POI, $$2);
      $$3.getMemory(MemoryModuleType.JOB_SITE).ifPresent($$1x -> $$3.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker($$1x.pos())));
      $$1.playWorkSound();
      this.useWorkstation($$0, $$1);
      if ($$1.shouldRestock($$0)) {
         $$1.restock();
      }
   }

   protected void useWorkstation(ServerLevel $$0, Villager $$1) {
   }

   protected boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
      Optional<GlobalPos> $$3 = $$1.getBrain().getMemory(MemoryModuleType.JOB_SITE);
      if ($$3.isEmpty()) {
         return false;
      } else {
         GlobalPos $$4 = $$3.get();
         return $$4.dimension() == $$0.dimension() && $$4.pos().closerToCenterThan($$1.position(), 1.73);
      }
   }
}
