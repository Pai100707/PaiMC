package net.minecraft.world.entity.monster.breeze;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class Slide extends Behavior<Breeze> {
   public Slide() {
      super(
         Map.of(
            MemoryModuleType.ATTACK_TARGET,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.BREEZE_JUMP_COOLDOWN,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.BREEZE_SHOOT,
            MemoryStatus.VALUE_ABSENT
         )
      );
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Breeze $$1) {
      return $$1.onGround() && !$$1.isInWater() && $$1.getPose() == net.minecraft.world.entity.Pose.STANDING;
   }

   protected void start(ServerLevel $$0, Breeze $$1, long $$2) {
      net.minecraft.world.entity.LivingEntity $$3 = $$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
      if ($$3 != null) {
         boolean $$4 = $$1.withinInnerCircleRange($$3.position());
         Vec3 $$5 = null;
         if ($$4) {
            Vec3 $$6 = DefaultRandomPos.getPosAway($$1, 5, 5, $$3.position());
            if ($$6 != null && BreezeUtil.hasLineOfSight($$1, $$6) && $$3.distanceToSqr($$6.x, $$6.y, $$6.z) > $$3.distanceToSqr($$1)) {
               $$5 = $$6;
            }
         }

         if ($$5 == null) {
            $$5 = $$1.getRandom().nextBoolean() ? BreezeUtil.randomPointBehindTarget($$3, $$1.getRandom()) : randomPointInMiddleCircle($$1, $$3);
         }

         $$1.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(BlockPos.containing($$5), 0.6F, 1));
      }
   }

   private static Vec3 randomPointInMiddleCircle(Breeze $$0, net.minecraft.world.entity.LivingEntity $$1) {
      Vec3 $$2 = $$1.position().subtract($$0.position());
      double $$3 = $$2.length() - Mth.lerp($$0.getRandom().nextDouble(), 8.0, 4.0);
      Vec3 $$4 = $$2.normalize().multiply($$3, $$3, $$3);
      return $$0.position().add($$4);
   }
}
