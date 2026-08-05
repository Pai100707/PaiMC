package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallTorchBlock extends TorchBlock {
   public static final MapCodec<WallTorchBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(PARTICLE_OPTIONS_FIELD.forGetter($$0x -> $$0x.flameParticle), propertiesCodec()).apply($$0, WallTorchBlock::new)
   );
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.boxZ(5.0, 3.0, 13.0, 11.0, 16.0));

   @Override
   public MapCodec<WallTorchBlock> codec() {
      return CODEC;
   }

   protected WallTorchBlock(SimpleParticleType $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return getShape($$0);
   }

   public static VoxelShape getShape(BlockState $$0) {
      return SHAPES.get($$0.getValue(FACING));
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return canSurvive($$1, $$2, $$0.getValue(FACING));
   }

   public static boolean canSurvive(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, Direction $$2) {
      BlockPos $$3 = $$1.relative($$2.getOpposite());
      BlockState $$4 = $$0.getBlockState($$3);
      return $$4.isFaceSturdy($$0, $$3, $$2);
   }

   
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = this.defaultBlockState();
      net.minecraft.world.level.LevelReader $$2 = $$0.getLevel();
      BlockPos $$3 = $$0.getClickedPos();
      Direction[] $$4 = $$0.getNearestLookingDirections();

      for (Direction $$5 : $$4) {
         if ($$5.getAxis().isHorizontal()) {
            Direction $$6 = $$5.getOpposite();
            $$1 = $$1.setValue(FACING, $$6);
            if ($$1.canSurvive($$2, $$3)) {
               return $$1;
            }
         }
      }

      return null;
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
      return $$4.getOpposite() == $$0.getValue(FACING) && !$$0.canSurvive($$1, $$3) ? Blocks.AIR.defaultBlockState() : $$0;
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      Direction $$4 = $$0.getValue(FACING);
      double $$5 = $$2.getX() + 0.5;
      double $$6 = $$2.getY() + 0.7;
      double $$7 = $$2.getZ() + 0.5;
      double $$8 = 0.22;
      double $$9 = 0.27;
      Direction $$10 = $$4.getOpposite();
      $$1.addParticle(ParticleTypes.SMOKE, $$5 + 0.27 * $$10.getStepX(), $$6 + 0.22, $$7 + 0.27 * $$10.getStepZ(), 0.0, 0.0, 0.0);
      $$1.addParticle(this.flameParticle, $$5 + 0.27 * $$10.getStepX(), $$6 + 0.22, $$7 + 0.27 * $$10.getStepZ(), 0.0, 0.0, 0.0);
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(FACING)));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING);
   }
}
