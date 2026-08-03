package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class RandomLookAround extends Behavior<net.minecraft.world.entity.Mob> {
   private final IntProvider interval;
   private final float maxYaw;
   private final float minPitch;
   private final float pitchRange;

   public RandomLookAround(IntProvider $$0, float $$1, float $$2, float $$3) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT));
      if ($$2 > $$3) {
         throw new IllegalArgumentException("Minimum pitch is larger than maximum pitch! " + $$2 + " > " + $$3);
      } else {
         this.interval = $$0;
         this.maxYaw = $$1;
         this.minPitch = $$2;
         this.pitchRange = $$3 - $$2;
      }
   }

   protected void start(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      RandomSource $$3 = $$1.getRandom();
      float $$4 = Mth.clamp($$3.nextFloat() * this.pitchRange + this.minPitch, -90.0F, 90.0F);
      float $$5 = Mth.wrapDegrees($$1.getYRot() + 2.0F * $$3.nextFloat() * this.maxYaw - this.maxYaw);
      Vec3 $$6 = Vec3.directionFromRotation($$4, $$5);
      $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker($$1.getEyePosition().add($$6)));
      $$1.getBrain().setMemory(MemoryModuleType.GAZE_COOLDOWN_TICKS, this.interval.sample($$3));
   }
}
