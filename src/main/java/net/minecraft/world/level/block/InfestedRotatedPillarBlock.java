package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class InfestedRotatedPillarBlock extends InfestedBlock {
   public static final MapCodec<InfestedRotatedPillarBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("host").forGetter(InfestedBlock::getHostBlock), propertiesCodec())
         .apply($$0, InfestedRotatedPillarBlock::new)
   );

   @Override
   public MapCodec<InfestedRotatedPillarBlock> codec() {
      return CODEC;
   }

   public InfestedRotatedPillarBlock(Block $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1);
      this.registerDefaultState(this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Axis.Y));
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return RotatedPillarBlock.rotatePillar($$0, $$1);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(RotatedPillarBlock.AXIS);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(RotatedPillarBlock.AXIS, $$0.getClickedFace().getAxis());
   }
}
