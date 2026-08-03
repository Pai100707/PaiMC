package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.Fluids;

public class TryLaySpawnOnWaterNearLand {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create(Block $$0) {
      return BehaviorBuilder.create(
         $$1 -> $$1.group($$1.absent(MemoryModuleType.ATTACK_TARGET), $$1.present(MemoryModuleType.WALK_TARGET), $$1.present(MemoryModuleType.IS_PREGNANT))
            .apply($$1, ($$1x, $$2, $$3) -> ($$2x, $$3x, $$4) -> {
               if (!$$3x.isInWater() && $$3x.onGround()) {
                  BlockPos $$5 = $$3x.blockPosition().below();

                  for (Direction $$6 : Plane.HORIZONTAL) {
                     BlockPos $$7 = $$5.relative($$6);
                     if ($$2x.getBlockState($$7).getCollisionShape($$2x, $$7).getFaceShape(Direction.UP).isEmpty() && $$2x.getFluidState($$7).is(Fluids.WATER)) {
                        BlockPos $$8 = $$7.above();
                        if ($$2x.getBlockState($$8).isAir()) {
                           BlockState $$9 = $$0.defaultBlockState();
                           $$2x.setBlock($$8, $$9, 3);
                           $$2x.gameEvent(GameEvent.BLOCK_PLACE, $$8, Context.of($$3x, $$9));
                           $$2x.playSound(null, $$3x, SoundEvents.FROG_LAY_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                           $$3.erase();
                           return true;
                        }
                     }
                  }

                  return true;
               } else {
                  return false;
               }
            })
      );
   }
}
