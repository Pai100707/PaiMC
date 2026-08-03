package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

public class LookAndFollowTradingPlayerSink extends Behavior<Villager> {
   private final float speedModifier;

   public LookAndFollowTradingPlayerSink(float $$0) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), Integer.MAX_VALUE);
      this.speedModifier = $$0;
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Villager $$1) {
      Player $$2 = $$1.getTradingPlayer();
      return $$1.isAlive() && $$2 != null && !$$1.isInWater() && !$$1.hurtMarked && $$1.distanceToSqr($$2) <= 16.0;
   }

   protected boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
      return this.checkExtraStartConditions($$0, $$1);
   }

   protected void start(ServerLevel $$0, Villager $$1, long $$2) {
      this.followPlayer($$1);
   }

   protected void stop(ServerLevel $$0, Villager $$1, long $$2) {
      Brain<?> $$3 = $$1.getBrain();
      $$3.eraseMemory(MemoryModuleType.WALK_TARGET);
      $$3.eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   protected void tick(ServerLevel $$0, Villager $$1, long $$2) {
      this.followPlayer($$1);
   }

   @Override
   protected boolean timedOut(long $$0) {
      return false;
   }

   private void followPlayer(Villager $$0) {
      Brain<?> $$1 = $$0.getBrain();
      $$1.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker($$0.getTradingPlayer(), false), this.speedModifier, 2));
      $$1.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker($$0.getTradingPlayer(), true));
   }
}
