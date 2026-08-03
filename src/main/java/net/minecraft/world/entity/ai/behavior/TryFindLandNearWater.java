package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLandNearWater {
   public static BehaviorControl<net.minecraft.world.entity.PathfinderMob> create(int $$0, float $$1) {
      MutableLong $$2 = new MutableLong(0L);
      return BehaviorBuilder.create(
         $$3 -> $$3.group($$3.absent(MemoryModuleType.ATTACK_TARGET), $$3.absent(MemoryModuleType.WALK_TARGET), $$3.registered(MemoryModuleType.LOOK_TARGET))
            .apply(
               $$3,
               ($$3x, $$4, $$5) -> ($$5x, $$6, $$7) -> {
                  if ($$5x.getFluidState($$6.blockPosition()).is(FluidTags.WATER)) {
                     return false;
                  } else if ($$7 < $$2.longValue()) {
                     $$2.setValue($$7 + 40L);
                     return true;
                  } else {
                     CollisionContext $$8 = CollisionContext.of($$6);
                     BlockPos $$9 = $$6.blockPosition();
                     MutableBlockPos $$10 = new MutableBlockPos();

                     label45:
                     for (BlockPos $$11 : BlockPos.withinManhattan($$9, $$0, $$0, $$0)) {
                        if (($$11.getX() != $$9.getX() || $$11.getZ() != $$9.getZ())
                           && $$5x.getBlockState($$11).getCollisionShape($$5x, $$11, $$8).isEmpty()
                           && !$$5x.getBlockState($$10.setWithOffset($$11, Direction.DOWN)).getCollisionShape($$5x, $$11, $$8).isEmpty()) {
                           for (Direction $$12 : Plane.HORIZONTAL) {
                              $$10.setWithOffset($$11, $$12);
                              if ($$5x.getBlockState($$10).isAir() && $$5x.getBlockState($$10.move(Direction.DOWN)).is(Blocks.WATER)) {
                                 $$5.set(new BlockPosTracker($$11));
                                 $$4.set(new WalkTarget(new BlockPosTracker($$11), $$1, 0));
                                 break label45;
                              }
                           }
                        }
                     }

                     $$2.setValue($$7 + 40L);
                     return true;
                  }
               }
            )
      );
   }
}
