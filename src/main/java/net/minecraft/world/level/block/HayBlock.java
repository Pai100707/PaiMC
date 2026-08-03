package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class HayBlock extends RotatedPillarBlock {
   public static final MapCodec<HayBlock> CODEC = simpleCodec(HayBlock::new);

   @Override
   public MapCodec<HayBlock> codec() {
      return CODEC;
   }

   public HayBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Axis.Y));
   }

   @Override
   public void fallOn(net.minecraft.world.level.Level $$0, BlockState $$1, BlockPos $$2, Entity $$3, double $$4) {
      $$3.causeFallDamage($$4, 0.2F, $$0.damageSources().fall());
   }
}
