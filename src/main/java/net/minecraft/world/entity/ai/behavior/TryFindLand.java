package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLand {
   private static final int COOLDOWN_TICKS = 60;

   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> create(int $$0, float $$1) {
      MutableLong $$2 = new MutableLong(0L);
      return BehaviorBuilder.create(
         $$3 -> $$3.group($$3.absent(MemoryModuleType.ATTACK_TARGET), $$3.absent(MemoryModuleType.WALK_TARGET), $$3.registered(MemoryModuleType.LOOK_TARGET))
            .apply(
               $$3,
               ($$3x, $$4, $$5) -> ($$5x, $$6, $$7) -> {
                  if (!$$5x.getFluidState($$6.blockPosition()).is(FluidTags.WATER)) {
                     return false;
                  } else if ($$7 < $$2.longValue()) {
                     $$2.setValue($$7 + 60L);
                     return true;
                  } else {
                     BlockPos $$8 = $$6.blockPosition();
                     MutableBlockPos $$9 = new MutableBlockPos();
                     CollisionContext $$10 = CollisionContext.of($$6);

                     for (BlockPos $$11 : BlockPos.withinManhattan($$8, $$0, $$0, $$0)) {
                        if ($$11.getX() != $$8.getX() || $$11.getZ() != $$8.getZ()) {
                           BlockState $$12 = $$5x.getBlockState($$11);
                           BlockState $$13 = $$5x.getBlockState($$9.setWithOffset($$11, Direction.DOWN));
                           if (!$$12.is(Blocks.WATER)
                              && $$5x.getFluidState($$11).isEmpty()
                              && $$12.getCollisionShape($$5x, $$11, $$10).isEmpty()
                              && $$13.isFaceSturdy($$5x, $$9, Direction.UP)) {
                              BlockPos $$14 = $$11.immutable();
                              $$5.set(new BlockPosTracker($$14));
                              $$4.set(new WalkTarget(new BlockPosTracker($$14), $$1, 1));
                              break;
                           }
                        }
                     }

                     $$2.setValue($$7 + 60L);
                     return true;
                  }
               }
            )
      );
   }
}
