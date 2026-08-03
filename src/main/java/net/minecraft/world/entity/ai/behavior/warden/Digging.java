package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;

public class Digging<E extends Warden> extends Behavior<E> {
   public Digging(int $$0) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), $$0);
   }

   protected boolean canStillUse(ServerLevel $$0, E $$1, long $$2) {
      return $$1.getRemovalReason() == null;
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, E $$1) {
      return $$1.onGround() || $$1.isInWater() || $$1.isInLava();
   }

   protected void start(ServerLevel $$0, E $$1, long $$2) {
      if ($$1.onGround()) {
         $$1.setPose(net.minecraft.world.entity.Pose.DIGGING);
         $$1.playSound(SoundEvents.WARDEN_DIG, 5.0F, 1.0F);
      } else {
         $$1.playSound(SoundEvents.WARDEN_AGITATED, 5.0F, 1.0F);
         this.stop($$0, $$1, $$2);
      }
   }

   protected void stop(ServerLevel $$0, E $$1, long $$2) {
      if ($$1.getRemovalReason() == null) {
         $$1.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
      }
   }
}
