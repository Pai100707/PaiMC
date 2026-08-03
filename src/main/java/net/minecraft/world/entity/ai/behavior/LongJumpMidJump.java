package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class LongJumpMidJump extends Behavior<net.minecraft.world.entity.Mob> {
   public static final int TIME_OUT_DURATION = 100;
   private final UniformInt timeBetweenLongJumps;
   private final SoundEvent landingSound;

   public LongJumpMidJump(UniformInt $$0, SoundEvent $$1) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_PRESENT), 100);
      this.timeBetweenLongJumps = $$0;
      this.landingSound = $$1;
   }

   protected boolean canStillUse(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      return !$$1.onGround();
   }

   protected void start(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      $$1.setDiscardFriction(true);
      $$1.setPose(net.minecraft.world.entity.Pose.LONG_JUMPING);
   }

   protected void stop(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      if ($$1.onGround()) {
         $$1.setDeltaMovement($$1.getDeltaMovement().multiply(0.1F, 1.0, 0.1F));
         $$0.playSound(null, $$1, this.landingSound, SoundSource.NEUTRAL, 2.0F, 1.0F);
      }

      $$1.setDiscardFriction(false);
      $$1.setPose(net.minecraft.world.entity.Pose.STANDING);
      $$1.getBrain().eraseMemory(MemoryModuleType.LONG_JUMP_MID_JUMP);
      $$1.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample($$0.random));
   }
}
