package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SlimeBlock extends HalfTransparentBlock {
   public static final MapCodec<SlimeBlock> CODEC = simpleCodec(SlimeBlock::new);

   @Override
   public MapCodec<SlimeBlock> codec() {
      return CODEC;
   }

   public SlimeBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   public void fallOn(net.minecraft.world.level.Level $$0, BlockState $$1, BlockPos $$2, Entity $$3, double $$4) {
      if (!$$3.isSuppressingBounce()) {
         $$3.causeFallDamage($$4, 0.0F, $$0.damageSources().fall());
      }
   }

   @Override
   public void updateEntityMovementAfterFallOn(net.minecraft.world.level.BlockGetter $$0, Entity $$1) {
      if ($$1.isSuppressingBounce()) {
         super.updateEntityMovementAfterFallOn($$0, $$1);
      } else {
         this.bounceUp($$1);
      }
   }

   private void bounceUp(Entity $$0) {
      Vec3 $$1 = $$0.getDeltaMovement();
      if ($$1.y < 0.0) {
         double $$2 = $$0 instanceof LivingEntity ? 1.0 : 0.8;
         $$0.setDeltaMovement($$1.x, -$$1.y * $$2, $$1.z);
      }
   }

   @Override
   public void stepOn(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Entity $$3) {
      double $$4 = Math.abs($$3.getDeltaMovement().y);
      if ($$4 < 0.1 && !$$3.isSteppingCarefully()) {
         double $$5 = 0.4 + $$4 * 0.2;
         $$3.setDeltaMovement($$3.getDeltaMovement().multiply($$5, 1.0, $$5));
      }

      super.stepOn($$0, $$1, $$2, $$3);
   }
}
