package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindWater {
   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> create(int $$0, float $$1) {
      MutableLong $$2 = new MutableLong(0L);
      return BehaviorBuilder.create(
         $$3 -> $$3.group($$3.absent(MemoryModuleType.ATTACK_TARGET), $$3.absent(MemoryModuleType.WALK_TARGET), $$3.registered(MemoryModuleType.LOOK_TARGET))
            .apply($$3, ($$3x, $$4, $$5) -> ($$5x, $$6, $$7) -> {
               if ($$5x.getFluidState($$6.blockPosition()).is(FluidTags.WATER)) {
                  return false;
               } else if ($$7 < $$2.longValue()) {
                  $$2.setValue($$7 + 20L + 2L);
                  return true;
               } else {
                  BlockPos $$8 = null;
                  BlockPos $$9 = null;
                  BlockPos $$10 = $$6.blockPosition();

                  for (BlockPos $$12 : BlockPos.withinManhattan($$10, $$0, $$0, $$0)) {
                     if ($$12.getX() != $$10.getX() || $$12.getZ() != $$10.getZ()) {
                        BlockState $$13 = $$6.level().getBlockState($$12.above());
                        BlockState $$14 = $$6.level().getBlockState($$12);
                        if ($$14.is(Blocks.WATER)) {
                           if ($$13.isAir()) {
                              $$8 = $$12.immutable();
                              break;
                           }

                           if ($$9 == null && !$$12.closerToCenterThan($$6.position(), 1.5)) {
                              $$9 = $$12.immutable();
                           }
                        }
                     }
                  }

                  if ($$8 == null) {
                     $$8 = $$9;
                  }

                  if ($$8 != null) {
                     $$5.set(new BlockPosTracker($$8));
                     $$4.set(new WalkTarget(new BlockPosTracker($$8), $$1, 0));
                  }

                  $$2.setValue($$7 + 40L);
                  return true;
               }
            })
      );
   }
}
