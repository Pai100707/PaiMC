package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FenceGateBlock extends HorizontalDirectionalBlock {
   public static final MapCodec<FenceGateBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(WoodType.CODEC.fieldOf("wood_type").forGetter($$0x -> $$0x.type), propertiesCodec()).apply($$0, FenceGateBlock::new)
   );
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty IN_WALL = BlockStateProperties.IN_WALL;
   private static final Map<Axis, VoxelShape> SHAPES = Shapes.rotateHorizontalAxis(Block.cube(16.0, 16.0, 4.0));
   private static final Map<Axis, VoxelShape> SHAPES_WALL = Maps.newEnumMap(
      Util.mapValues(SHAPES, $$0 -> Shapes.join($$0, Block.column(16.0, 13.0, 16.0), BooleanOp.ONLY_FIRST))
   );
   private static final Map<Axis, VoxelShape> SHAPE_COLLISION = Shapes.rotateHorizontalAxis(Block.column(16.0, 4.0, 0.0, 24.0));
   private static final Map<Axis, VoxelShape> SHAPE_SUPPORT = Shapes.rotateHorizontalAxis(Block.column(16.0, 4.0, 5.0, 24.0));
   private static final Map<Axis, VoxelShape> SHAPE_OCCLUSION = Shapes.rotateHorizontalAxis(
      Shapes.or(Block.box(0.0, 5.0, 7.0, 2.0, 16.0, 9.0), Block.box(14.0, 5.0, 7.0, 16.0, 16.0, 9.0))
   );
   private static final Map<Axis, VoxelShape> SHAPE_OCCLUSION_WALL = Maps.newEnumMap(
      Util.mapValues(SHAPE_OCCLUSION, $$0 -> $$0.move(0.0, -0.1875, 0.0).optimize())
   );
   private final WoodType type;

   @Override
   public MapCodec<FenceGateBlock> codec() {
      return CODEC;
   }

   public FenceGateBlock(WoodType $$0, BlockBehaviour.Properties $$1) {
      super($$1.sound($$0.soundType()));
      this.type = $$0;
      this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false).setValue(POWERED, false).setValue(IN_WALL, false));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      Axis $$4 = ((Direction)$$0.getValue(FACING)).getAxis();
      return ($$0.getValue(IN_WALL) ? SHAPES_WALL : SHAPES).get($$4);
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
      Axis $$8 = $$4.getAxis();
      if (((Direction)$$0.getValue(FACING)).getClockWise().getAxis() != $$8) {
         return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else {
         boolean $$9 = this.isWall($$6) || this.isWall($$1.getBlockState($$3.relative($$4.getOpposite())));
         return $$0.setValue(IN_WALL, $$9);
      }
   }

   @Override
   protected VoxelShape getBlockSupportShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      Axis $$3 = ((Direction)$$0.getValue(FACING)).getAxis();
      return $$0.getValue(OPEN) ? Shapes.empty() : SHAPE_SUPPORT.get($$3);
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      Axis $$4 = ((Direction)$$0.getValue(FACING)).getAxis();
      return $$0.getValue(OPEN) ? Shapes.empty() : SHAPE_COLLISION.get($$4);
   }

   @Override
   protected VoxelShape getOcclusionShape(BlockState $$0) {
      Axis $$1 = ((Direction)$$0.getValue(FACING)).getAxis();
      return ($$0.getValue(IN_WALL) ? SHAPE_OCCLUSION_WALL : SHAPE_OCCLUSION).get($$1);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      switch ($$1) {
         case LAND:
            return $$0.getValue(OPEN);
         case WATER:
            return false;
         case AIR:
            return $$0.getValue(OPEN);
         default:
            return false;
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      net.minecraft.world.level.Level $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      boolean $$3 = $$1.hasNeighborSignal($$2);
      Direction $$4 = $$0.getHorizontalDirection();
      Axis $$5 = $$4.getAxis();
      boolean $$6 = $$5 == Axis.Z && (this.isWall($$1.getBlockState($$2.west())) || this.isWall($$1.getBlockState($$2.east())))
         || $$5 == Axis.X && (this.isWall($$1.getBlockState($$2.north())) || this.isWall($$1.getBlockState($$2.south())));
      return this.defaultBlockState().setValue(FACING, $$4).setValue(OPEN, $$3).setValue(POWERED, $$3).setValue(IN_WALL, $$6);
   }

   private boolean isWall(BlockState $$0) {
      return $$0.is(BlockTags.WALLS);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$0.getValue(OPEN)) {
         $$0 = $$0.setValue(OPEN, false);
         $$1.setBlock($$2, $$0, 10);
      } else {
         Direction $$5 = $$3.getDirection();
         if ($$0.getValue(FACING) == $$5.getOpposite()) {
            $$0 = $$0.setValue(FACING, $$5);
         }

         $$0 = $$0.setValue(OPEN, true);
         $$1.setBlock($$2, $$0, 10);
      }

      boolean $$6 = $$0.getValue(OPEN);
      $$1.playSound($$3, $$2, $$6 ? this.type.fenceGateOpen() : this.type.fenceGateClose(), SoundSource.BLOCKS, 1.0F, $$1.getRandom().nextFloat() * 0.1F + 0.9F);
      $$1.gameEvent($$3, $$6 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, $$2);
      return InteractionResult.SUCCESS;
   }

   @Override
   protected void onExplosionHit(BlockState $$0, ServerLevel $$1, BlockPos $$2, net.minecraft.world.level.Explosion $$3, BiConsumer<ItemStack, BlockPos> $$4) {
      if ($$3.canTriggerBlocks() && !$$0.getValue(POWERED)) {
         boolean $$5 = $$0.getValue(OPEN);
         $$1.setBlockAndUpdate($$2, $$0.setValue(OPEN, !$$5));
         $$1.playSound(
            null, $$2, $$5 ? this.type.fenceGateClose() : this.type.fenceGateOpen(), SoundSource.BLOCKS, 1.0F, $$1.getRandom().nextFloat() * 0.1F + 0.9F
         );
         $$1.gameEvent($$5 ? GameEvent.BLOCK_CLOSE : GameEvent.BLOCK_OPEN, $$2, GameEvent.Context.of($$0));
      }

      super.onExplosionHit($$0, $$1, $$2, $$3, $$4);
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      if (!$$1.isClientSide()) {
         boolean $$6 = $$1.hasNeighborSignal($$2);
         if ($$0.getValue(POWERED) != $$6) {
            $$1.setBlock($$2, $$0.setValue(POWERED, $$6).setValue(OPEN, $$6), 2);
            if ($$0.getValue(OPEN) != $$6) {
               $$1.playSound(
                  null, $$2, $$6 ? this.type.fenceGateOpen() : this.type.fenceGateClose(), SoundSource.BLOCKS, 1.0F, $$1.getRandom().nextFloat() * 0.1F + 0.9F
               );
               $$1.gameEvent(null, $$6 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, $$2);
            }
         }
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, OPEN, POWERED, IN_WALL);
   }

   public static boolean connectsToDirection(BlockState $$0, Direction $$1) {
      return ((Direction)$$0.getValue(FACING)).getAxis() == $$1.getClockWise().getAxis();
   }
}
