package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.redstone.DefaultRedstoneWireEvaluator;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.ExperimentalRedstoneWireEvaluator;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedStoneWireBlock extends Block {
   public static final MapCodec<RedStoneWireBlock> CODEC = simpleCodec(RedStoneWireBlock::new);
   public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
   public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
   public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
   public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(
      Maps.newEnumMap(Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST))
   );
   private static final int[] COLORS = (int[])Util.make(new int[16], $$0 -> {
      for (int $$1 = 0; $$1 <= 15; $$1++) {
         float $$2 = $$1 / 15.0F;
         float $$3 = $$2 * 0.6F + ($$2 > 0.0F ? 0.4F : 0.3F);
         float $$4 = Mth.clamp($$2 * $$2 * 0.7F - 0.5F, 0.0F, 1.0F);
         float $$5 = Mth.clamp($$2 * $$2 * 0.6F - 0.7F, 0.0F, 1.0F);
         $$0[$$1] = ARGB.colorFromFloat(1.0F, $$3, $$4, $$5);
      }
   });
   private static final float PARTICLE_DENSITY = 0.2F;
   private final Function<BlockState, VoxelShape> shapes;
   private final BlockState crossState;
   private final RedstoneWireEvaluator evaluator = new DefaultRedstoneWireEvaluator(this);
   private boolean shouldSignal = true;

   @Override
   public MapCodec<RedStoneWireBlock> codec() {
      return CODEC;
   }

   public RedStoneWireBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(
         this.stateDefinition
            .any()
            .setValue(NORTH, RedstoneSide.NONE)
            .setValue(EAST, RedstoneSide.NONE)
            .setValue(SOUTH, RedstoneSide.NONE)
            .setValue(WEST, RedstoneSide.NONE)
            .setValue(POWER, 0)
      );
      this.shapes = this.makeShapes();
      this.crossState = this.defaultBlockState()
         .setValue(NORTH, RedstoneSide.SIDE)
         .setValue(EAST, RedstoneSide.SIDE)
         .setValue(SOUTH, RedstoneSide.SIDE)
         .setValue(WEST, RedstoneSide.SIDE);
   }

   private Function<BlockState, VoxelShape> makeShapes() {
      int $$0 = 1;
      int $$1 = 10;
      VoxelShape $$2 = Block.column(10.0, 0.0, 1.0);
      Map<Direction, VoxelShape> $$3 = Shapes.rotateHorizontal(Block.boxZ(10.0, 0.0, 1.0, 0.0, 8.0));
      Map<Direction, VoxelShape> $$4 = Shapes.rotateHorizontal(Block.boxZ(10.0, 16.0, 0.0, 1.0));
      return this.getShapeForEachState($$3x -> {
         VoxelShape $$4x = $$2;

         for (Entry<Direction, EnumProperty<RedstoneSide>> $$5 : PROPERTY_BY_DIRECTION.entrySet()) {
            $$4x = switch ((RedstoneSide)$$3x.getValue($$5.getValue())) {
               case UP -> Shapes.or($$4x, new VoxelShape[]{$$3.get($$5.getKey()), $$4.get($$5.getKey())});
               case SIDE -> Shapes.or($$4x, $$3.get($$5.getKey()));
               case NONE -> $$4x;
            };
         }

         return $$4x;
      }, new Property[]{POWER});
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.shapes.apply($$0);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.getConnectionState($$0.getLevel(), this.crossState, $$0.getClickedPos());
   }

   private BlockState getConnectionState(net.minecraft.world.level.BlockGetter $$0, BlockState $$1, BlockPos $$2) {
      boolean $$3 = isDot($$1);
      $$1 = this.getMissingConnections($$0, this.defaultBlockState().setValue(POWER, $$1.getValue(POWER)), $$2);
      if ($$3 && isDot($$1)) {
         return $$1;
      } else {
         boolean $$4 = $$1.getValue(NORTH).isConnected();
         boolean $$5 = $$1.getValue(SOUTH).isConnected();
         boolean $$6 = $$1.getValue(EAST).isConnected();
         boolean $$7 = $$1.getValue(WEST).isConnected();
         boolean $$8 = !$$4 && !$$5;
         boolean $$9 = !$$6 && !$$7;
         if (!$$7 && $$8) {
            $$1 = $$1.setValue(WEST, RedstoneSide.SIDE);
         }

         if (!$$6 && $$8) {
            $$1 = $$1.setValue(EAST, RedstoneSide.SIDE);
         }

         if (!$$4 && $$9) {
            $$1 = $$1.setValue(NORTH, RedstoneSide.SIDE);
         }

         if (!$$5 && $$9) {
            $$1 = $$1.setValue(SOUTH, RedstoneSide.SIDE);
         }

         return $$1;
      }
   }

   private BlockState getMissingConnections(net.minecraft.world.level.BlockGetter $$0, BlockState $$1, BlockPos $$2) {
      boolean $$3 = !$$0.getBlockState($$2.above()).isRedstoneConductor($$0, $$2);

      for (Direction $$4 : Plane.HORIZONTAL) {
         if (!$$1.getValue(PROPERTY_BY_DIRECTION.get($$4)).isConnected()) {
            RedstoneSide $$5 = this.getConnectingSide($$0, $$2, $$4, $$3);
            $$1 = $$1.setValue(PROPERTY_BY_DIRECTION.get($$4), $$5);
         }
      }

      return $$1;
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
      if ($$4 == Direction.DOWN) {
         return !this.canSurviveOn($$1, $$5, $$6) ? Blocks.AIR.defaultBlockState() : $$0;
      } else if ($$4 == Direction.UP) {
         return this.getConnectionState($$1, $$0, $$3);
      } else {
         RedstoneSide $$8 = this.getConnectingSide($$1, $$3, $$4);
         return $$8.isConnected() == $$0.getValue(PROPERTY_BY_DIRECTION.get($$4)).isConnected() && !isCross($$0)
            ? $$0.setValue(PROPERTY_BY_DIRECTION.get($$4), $$8)
            : this.getConnectionState($$1, this.crossState.setValue(POWER, $$0.getValue(POWER)).setValue(PROPERTY_BY_DIRECTION.get($$4), $$8), $$3);
      }
   }

   private static boolean isCross(BlockState $$0) {
      return $$0.getValue(NORTH).isConnected() && $$0.getValue(SOUTH).isConnected() && $$0.getValue(EAST).isConnected() && $$0.getValue(WEST).isConnected();
   }

   private static boolean isDot(BlockState $$0) {
      return !$$0.getValue(NORTH).isConnected() && !$$0.getValue(SOUTH).isConnected() && !$$0.getValue(EAST).isConnected() && !$$0.getValue(WEST).isConnected();
   }

   @Override
   protected void updateIndirectNeighbourShapes(BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, @Block.UpdateFlags int $$3, int $$4) {
      MutableBlockPos $$5 = new MutableBlockPos();

      for (Direction $$6 : Plane.HORIZONTAL) {
         RedstoneSide $$7 = $$0.getValue(PROPERTY_BY_DIRECTION.get($$6));
         if ($$7 != RedstoneSide.NONE && !$$1.getBlockState($$5.setWithOffset($$2, $$6)).is(this)) {
            $$5.move(Direction.DOWN);
            BlockState $$8 = $$1.getBlockState($$5);
            if ($$8.is(this)) {
               BlockPos $$9 = $$5.relative($$6.getOpposite());
               $$1.neighborShapeChanged($$6.getOpposite(), $$5, $$9, $$1.getBlockState($$9), $$3, $$4);
            }

            $$5.setWithOffset($$2, $$6).move(Direction.UP);
            BlockState $$10 = $$1.getBlockState($$5);
            if ($$10.is(this)) {
               BlockPos $$11 = $$5.relative($$6.getOpposite());
               $$1.neighborShapeChanged($$6.getOpposite(), $$5, $$11, $$1.getBlockState($$11), $$3, $$4);
            }
         }
      }
   }

   private RedstoneSide getConnectingSide(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2) {
      return this.getConnectingSide($$0, $$1, $$2, !$$0.getBlockState($$1.above()).isRedstoneConductor($$0, $$1));
   }

   private RedstoneSide getConnectingSide(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2, boolean $$3) {
      BlockPos $$4 = $$1.relative($$2);
      BlockState $$5 = $$0.getBlockState($$4);
      if ($$3) {
         boolean $$6 = $$5.getBlock() instanceof TrapDoorBlock || this.canSurviveOn($$0, $$4, $$5);
         if ($$6 && shouldConnectTo($$0.getBlockState($$4.above()))) {
            if ($$5.isFaceSturdy($$0, $$4, $$2.getOpposite())) {
               return RedstoneSide.UP;
            }

            return RedstoneSide.SIDE;
         }
      }

      return !shouldConnectTo($$5, $$2) && ($$5.isRedstoneConductor($$0, $$4) || !shouldConnectTo($$0.getBlockState($$4.below())))
         ? RedstoneSide.NONE
         : RedstoneSide.SIDE;
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.below();
      BlockState $$4 = $$1.getBlockState($$3);
      return this.canSurviveOn($$1, $$3, $$4);
   }

   private boolean canSurviveOn(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockState $$2) {
      return $$2.isFaceSturdy($$0, $$1, Direction.UP) || $$2.is(Blocks.HOPPER);
   }

   private void updatePowerStrength(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Orientation $$3, boolean $$4) {
      if (useExperimentalEvaluator($$0)) {
         new ExperimentalRedstoneWireEvaluator(this).updatePowerStrength($$0, $$1, $$2, $$3, $$4);
      } else {
         this.evaluator.updatePowerStrength($$0, $$1, $$2, $$3, $$4);
      }
   }

   public int getBlockSignal(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      this.shouldSignal = false;
      int $$2 = $$0.getBestNeighborSignal($$1);
      this.shouldSignal = true;
      return $$2;
   }

   private void checkCornerChangeAt(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      if ($$0.getBlockState($$1).is(this)) {
         $$0.updateNeighborsAt($$1, this);

         for (Direction $$2 : Direction.values()) {
            $$0.updateNeighborsAt($$1.relative($$2), this);
         }
      }
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if (!$$3.is($$0.getBlock()) && !$$1.isClientSide()) {
         this.updatePowerStrength($$1, $$2, $$0, null, true);

         for (Direction $$5 : Plane.VERTICAL) {
            $$1.updateNeighborsAt($$2.relative($$5), this);
         }

         this.updateNeighborsOfNeighboringWires($$1, $$2);
      }
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      if (!$$3) {
         for (Direction $$4 : Direction.values()) {
            $$1.updateNeighborsAt($$2.relative($$4), this);
         }

         this.updatePowerStrength($$1, $$2, $$0, null, false);
         this.updateNeighborsOfNeighboringWires($$1, $$2);
      }
   }

   private void updateNeighborsOfNeighboringWires(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      for (Direction $$2 : Plane.HORIZONTAL) {
         this.checkCornerChangeAt($$0, $$1.relative($$2));
      }

      for (Direction $$3 : Plane.HORIZONTAL) {
         BlockPos $$4 = $$1.relative($$3);
         if ($$0.getBlockState($$4).isRedstoneConductor($$0, $$4)) {
            this.checkCornerChangeAt($$0, $$4.above());
         } else {
            this.checkCornerChangeAt($$0, $$4.below());
         }
      }
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      if (!$$1.isClientSide()) {
         if ($$3 != this || !useExperimentalEvaluator($$1)) {
            if ($$0.canSurvive($$1, $$2)) {
               this.updatePowerStrength($$1, $$2, $$0, $$4, false);
            } else {
               dropResources($$0, $$1, $$2);
               $$1.removeBlock($$2, false);
            }
         }
      }
   }

   private static boolean useExperimentalEvaluator(net.minecraft.world.level.Level $$0) {
      return $$0.enabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS);
   }

   @Override
   protected int getDirectSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return !this.shouldSignal ? 0 : $$0.getSignal($$1, $$2, $$3);
   }

   @Override
   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      if (this.shouldSignal && $$3 != Direction.DOWN) {
         int $$4 = $$0.getValue(POWER);
         if ($$4 == 0) {
            return 0;
         } else {
            return $$3 != Direction.UP && !this.getConnectionState($$1, $$0, $$2).getValue(PROPERTY_BY_DIRECTION.get($$3.getOpposite())).isConnected()
               ? 0
               : $$4;
         }
      } else {
         return 0;
      }
   }

   protected static boolean shouldConnectTo(BlockState $$0) {
      return shouldConnectTo($$0, null);
   }

   protected static boolean shouldConnectTo(BlockState $$0, Direction $$1) {
      if ($$0.is(Blocks.REDSTONE_WIRE)) {
         return true;
      } else if ($$0.is(Blocks.REPEATER)) {
         Direction $$2 = $$0.getValue(RepeaterBlock.FACING);
         return $$2 == $$1 || $$2.getOpposite() == $$1;
      } else {
         return $$0.is(Blocks.OBSERVER) ? $$1 == $$0.getValue(ObserverBlock.FACING) : $$0.isSignalSource() && $$1 != null;
      }
   }

   @Override
   protected boolean isSignalSource(BlockState $$0) {
      return this.shouldSignal;
   }

   public static int getColorForPower(int $$0) {
      return COLORS[$$0];
   }

   private static void spawnParticlesAlongLine(
      net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, int $$3, Direction $$4, Direction $$5, float $$6, float $$7
   ) {
      float $$8 = $$7 - $$6;
      if (!($$1.nextFloat() >= 0.2F * $$8)) {
         float $$9 = 0.4375F;
         float $$10 = $$6 + $$8 * $$1.nextFloat();
         double $$11 = 0.5 + 0.4375F * $$4.getStepX() + $$10 * $$5.getStepX();
         double $$12 = 0.5 + 0.4375F * $$4.getStepY() + $$10 * $$5.getStepY();
         double $$13 = 0.5 + 0.4375F * $$4.getStepZ() + $$10 * $$5.getStepZ();
         $$0.addParticle(new DustParticleOptions($$3, 1.0F), $$2.getX() + $$11, $$2.getY() + $$12, $$2.getZ() + $$13, 0.0, 0.0, 0.0);
      }
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      int $$4 = $$0.getValue(POWER);
      if ($$4 != 0) {
         for (Direction $$5 : Plane.HORIZONTAL) {
            RedstoneSide $$6 = $$0.getValue(PROPERTY_BY_DIRECTION.get($$5));
            switch ($$6) {
               case UP:
                  spawnParticlesAlongLine($$1, $$3, $$2, COLORS[$$4], $$5, Direction.UP, -0.5F, 0.5F);
               case SIDE:
                  spawnParticlesAlongLine($$1, $$3, $$2, COLORS[$$4], Direction.DOWN, $$5, 0.0F, 0.5F);
                  break;
               case NONE:
               default:
                  spawnParticlesAlongLine($$1, $$3, $$2, COLORS[$$4], Direction.DOWN, $$5, 0.0F, 0.3F);
            }
         }
      }
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      switch ($$1) {
         case CLOCKWISE_180:
            return $$0.setValue(NORTH, $$0.getValue(SOUTH))
               .setValue(EAST, $$0.getValue(WEST))
               .setValue(SOUTH, $$0.getValue(NORTH))
               .setValue(WEST, $$0.getValue(EAST));
         case COUNTERCLOCKWISE_90:
            return $$0.setValue(NORTH, $$0.getValue(EAST))
               .setValue(EAST, $$0.getValue(SOUTH))
               .setValue(SOUTH, $$0.getValue(WEST))
               .setValue(WEST, $$0.getValue(NORTH));
         case CLOCKWISE_90:
            return $$0.setValue(NORTH, $$0.getValue(WEST))
               .setValue(EAST, $$0.getValue(NORTH))
               .setValue(SOUTH, $$0.getValue(EAST))
               .setValue(WEST, $$0.getValue(SOUTH));
         default:
            return $$0;
      }
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      switch ($$1) {
         case LEFT_RIGHT:
            return $$0.setValue(NORTH, $$0.getValue(SOUTH)).setValue(SOUTH, $$0.getValue(NORTH));
         case FRONT_BACK:
            return $$0.setValue(EAST, $$0.getValue(WEST)).setValue(WEST, $$0.getValue(EAST));
         default:
            return super.mirror($$0, $$1);
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(NORTH, EAST, SOUTH, WEST, POWER);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!$$3.getAbilities().mayBuild) {
         return InteractionResult.PASS;
      } else {
         if (isCross($$0) || isDot($$0)) {
            BlockState $$5 = isCross($$0) ? this.defaultBlockState() : this.crossState;
            $$5 = $$5.setValue(POWER, $$0.getValue(POWER));
            $$5 = this.getConnectionState($$1, $$5, $$2);
            if ($$5 != $$0) {
               $$1.setBlock($$2, $$5, 3);
               this.updatesOnShapeChange($$1, $$2, $$0, $$5);
               return InteractionResult.SUCCESS;
            }
         }

         return InteractionResult.PASS;
      }
   }

   private void updatesOnShapeChange(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, BlockState $$3) {
      Orientation $$4 = ExperimentalRedstoneUtils.initialOrientation($$0, null, Direction.UP);

      for (Direction $$5 : Plane.HORIZONTAL) {
         BlockPos $$6 = $$1.relative($$5);
         if ($$2.getValue(PROPERTY_BY_DIRECTION.get($$5)).isConnected() != $$3.getValue(PROPERTY_BY_DIRECTION.get($$5)).isConnected()
            && $$0.getBlockState($$6).isRedstoneConductor($$0, $$6)) {
            $$0.updateNeighborsAtExceptFromFacing($$6, $$3.getBlock(), $$5.getOpposite(), ExperimentalRedstoneUtils.withFront($$4, $$5));
         }
      }
   }
}
