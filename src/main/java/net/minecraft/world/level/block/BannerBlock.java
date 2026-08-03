package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BannerBlock extends AbstractBannerBlock {
   public static final MapCodec<BannerBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(DyeColor.CODEC.fieldOf("color").forGetter(AbstractBannerBlock::getColor), propertiesCodec()).apply($$0, BannerBlock::new)
   );
   public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
   private static final Map<DyeColor, Block> BY_COLOR = Maps.newHashMap();
   private static final VoxelShape SHAPE = Block.column(8.0, 0.0, 16.0);

   @Override
   public MapCodec<BannerBlock> codec() {
      return CODEC;
   }

   public BannerBlock(DyeColor $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1);
      this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
      BY_COLOR.put($$0, this);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return $$1.getBlockState($$2.below()).isSolid();
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(ROTATION, RotationSegment.convertToSegment($$0.getRotation() + 180.0F));
   }

   @Override
   protected BlockState updateShape(
      BlockState $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.ScheduledTickAccess $$2,
      BlockPos $$3,
      Direction $$4,
      BlockPos $$5,
      BlockState $$6,
      RandomSource $$7
   ) {
      return $$4 == Direction.DOWN && !$$0.canSurvive($$1, $$3) ? Blocks.AIR.defaultBlockState() : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(ROTATION, $$1.rotate($$0.getValue(ROTATION), 16));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.setValue(ROTATION, $$1.mirror($$0.getValue(ROTATION), 16));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(ROTATION);
   }

   public static Block byColor(DyeColor $$0) {
      return BY_COLOR.getOrDefault($$0, Blocks.WHITE_BANNER);
   }
}
