package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;

public class Roar extends Behavior<Warden> {
   private static final int TICKS_BEFORE_PLAYING_ROAR_SOUND = 25;
   private static final int ROAR_ANGER_INCREASE = 20;

   public Roar() {
      super(
         ImmutableMap.of(
            MemoryModuleType.ROAR_TARGET,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.ATTACK_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.ROAR_SOUND_COOLDOWN,
            MemoryStatus.REGISTERED,
            MemoryModuleType.ROAR_SOUND_DELAY,
            MemoryStatus.REGISTERED
         ),
         WardenAi.ROAR_DURATION
      );
   }

   protected void start(ServerLevel $$0, Warden $$1, long $$2) {
      Brain<Warden> $$3 = $$1.getBrain();
      $$3.setMemoryWithExpiry(MemoryModuleType.ROAR_SOUND_DELAY, Unit.INSTANCE, 25L);
      $$3.eraseMemory(MemoryModuleType.WALK_TARGET);
      net.minecraft.world.entity.LivingEntity $$4 = $$1.getBrain().getMemory(MemoryModuleType.ROAR_TARGET).get();
      BehaviorUtils.lookAtEntity($$1, $$4);
      $$1.setPose(net.minecraft.world.entity.Pose.ROARING);
      $$1.increaseAngerAt($$4, 20, false);
   }

   protected boolean canStillUse(ServerLevel $$0, Warden $$1, long $$2) {
      return true;
   }

   protected void tick(ServerLevel $$0, Warden $$1, long $$2) {
      if (!$$1.getBrain().hasMemoryValue(MemoryModuleType.ROAR_SOUND_DELAY) && !$$1.getBrain().hasMemoryValue(MemoryModuleType.ROAR_SOUND_COOLDOWN)) {
         $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.ROAR_SOUND_COOLDOWN, Unit.INSTANCE, WardenAi.ROAR_DURATION - 25);
         $$1.playSound(SoundEvents.WARDEN_ROAR, 3.0F, 1.0F);
      }
   }

   protected void stop(ServerLevel $$0, Warden $$1, long $$2) {
      if ($$1.hasPose(net.minecraft.world.entity.Pose.ROARING)) {
         $$1.setPose(net.minecraft.world.entity.Pose.STANDING);
      }

      $$1.getBrain().getMemory(MemoryModuleType.ROAR_TARGET).ifPresent($$1::setAttackTarget);
      $$1.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
   }
}
