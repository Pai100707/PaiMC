package net.minecraft.world.entity.ai.behavior;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Util;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InsideBrownianWalk {
   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> create(float $$0) {
      return BehaviorBuilder.create(
         $$1 -> $$1.group($$1.absent(MemoryModuleType.WALK_TARGET))
            .apply(
               $$1,
               $$1x -> ($$2, $$3, $$4) -> {
                  if ($$2.canSeeSky($$3.blockPosition())) {
                     return false;
                  } else {
                     BlockPos $$5 = $$3.blockPosition();
                     List<BlockPos> $$6 = BlockPos.betweenClosedStream($$5.offset(-1, -1, -1), $$5.offset(1, 1, 1))
                        .map(BlockPos::immutable)
                        .collect(Util.toMutableList());
                     Collections.shuffle($$6);
                     $$6.stream()
                        .filter($$1xxx -> !$$2.canSeeSky($$1xxx))
                        .filter($$2x -> $$2.loadedAndEntityCanStandOn($$2x, $$3))
                        .filter($$2x -> $$2.noCollision($$3))
                        .findFirst()
                        .ifPresent($$2x -> $$1x.set(new WalkTarget($$2x, $$0, 0)));
                     return true;
                  }
               }
            )
      );
   }
}
