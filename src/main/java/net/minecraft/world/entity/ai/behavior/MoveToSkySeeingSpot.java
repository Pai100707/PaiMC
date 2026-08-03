package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MoveToSkySeeingSpot {
   public static OneShot<net.minecraft.world.entity.LivingEntity> create(float $$0) {
      return BehaviorBuilder.create($$1 -> $$1.group($$1.absent(MemoryModuleType.WALK_TARGET)).apply($$1, $$1x -> ($$2, $$3, $$4) -> {
         if ($$2.canSeeSky($$3.blockPosition())) {
            return false;
         } else {
            Optional<Vec3> $$5 = Optional.ofNullable(getOutdoorPosition($$2, $$3));
            $$5.ifPresent($$2x -> $$1x.set(new WalkTarget($$2x, $$0, 0)));
            return true;
         }
      }));
   }

   @Nullable
   private static Vec3 getOutdoorPosition(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      RandomSource $$2 = $$1.getRandom();
      BlockPos $$3 = $$1.blockPosition();

      for (int $$4 = 0; $$4 < 10; $$4++) {
         BlockPos $$5 = $$3.offset($$2.nextInt(20) - 10, $$2.nextInt(6) - 3, $$2.nextInt(20) - 10);
         if (hasNoBlocksAbove($$0, $$1, $$5)) {
            return Vec3.atBottomCenterOf($$5);
         }
      }

      return null;
   }

   public static boolean hasNoBlocksAbove(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, BlockPos $$2) {
      return $$0.canSeeSky($$2) && $$0.getHeightmapPos(Types.MOTION_BLOCKING, $$2).getY() <= $$1.getY();
   }
}
