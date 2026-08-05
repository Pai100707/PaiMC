package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VineBlock extends Block {
   public static final MapCodec<VineBlock> CODEC = simpleCodec(VineBlock::new);
   public static final BooleanProperty UP = PipeBlock.UP;
   public static final BooleanProperty NORTH = PipeBlock.NORTH;
   public static final BooleanProperty EAST = PipeBlock.EAST;
   public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
   public static final BooleanProperty WEST = PipeBlock.WEST;
   public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION
      .entrySet()
      .stream()
      .filter($$0 -> $$0.getKey() != Direction.DOWN)
      .collect(Util.toMap());
   private final Function<BlockState, VoxelShape> shapes;

   @Override
   public MapCodec<VineBlock> codec() {
      return CODEC;
   }

   public VineBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(
         this.stateDefinition.any().setValue(UP, false).setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false)
      );
      this.shapes = this.makeShapes();
   }

   private Function<BlockState, VoxelShape> makeShapes() {
      Map<Direction, VoxelShape> $$0 = Shapes.rotateAll(Block.boxZ(16.0, 0.0, 1.0));
      return this.getShapeForEachState($$1 -> {
         VoxelShape $$2 = Shapes.empty();

         for (Entry<Direction, BooleanProperty> $$3 : PROPERTY_BY_DIRECTION.entrySet()) {
            if ($$1.getValue($$3.getValue())) {
               $$2 = Shapes.or($$2, $$0.get($$3.getKey()));
            }
         }

         return $$2.isEmpty() ? Shapes.block() : $$2;
      });
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.shapes.apply($$0);
   }

   @Override
   protected boolean propagatesSkylightDown(BlockState $$0) {
      return true;
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return this.hasFaces(this.getUpdatedState($$0, $$1, $$2));
   }

   private boolean hasFaces(BlockState $$0) {
      return this.countFaces($$0) > 0;
   }

   private int countFaces(BlockState $$0) {
      int $$1 = 0;

      for (BooleanProperty $$2 : PROPERTY_BY_DIRECTION.values()) {
         if ($$0.getValue($$2)) {
            $$1++;
         }
      }

      return $$1;
   }

   private boolean canSupportAtFace(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2) {
      if ($$2 == Direction.DOWN) {
         return false;
      } else {
         BlockPos $$3 = $$1.relative($$2);
         if (isAcceptableNeighbour($$0, $$3, $$2)) {
            return true;
         } else if ($$2.getAxis() == Axis.Y) {
            return false;
         } else {
            BooleanProperty $$4 = PROPERTY_BY_DIRECTION.get($$2);
            BlockState $$5 = $$0.getBlockState($$1.above());
            return $$5.is(this) && $$5.getValue($$4);
         }
      }
   }

   public static boolean isAcceptableNeighbour(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2) {
      return MultifaceBlock.canAttachTo($$0, $$2, $$1, $$0.getBlockState($$1));
   }

   private BlockState getUpdatedState(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.above();
      if ($$0.getValue(UP)) {
         $$0 = $$0.setValue(UP, isAcceptableNeighbour($$1, $$3, Direction.DOWN));
      }

      BlockState $$4 = null;

      for (Direction $$5 : Plane.HORIZONTAL) {
         BooleanProperty $$6 = getPropertyForFace($$5);
         if ($$0.getValue($$6)) {
            boolean $$7 = this.canSupportAtFace($$1, $$2, $$5);
            if (!$$7) {
               if ($$4 == null) {
                  $$4 = $$1.getBlockState($$3);
               }

               $$7 = $$4.is(this) && $$4.getValue($$6);
            }

            $$0 = $$0.setValue($$6, $$7);
         }
      }

      return $$0;
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
         return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else {
         BlockState $$8 = this.getUpdatedState($$0, $$1, $$3);
         return !this.hasFaces($$8) ? Blocks.AIR.defaultBlockState() : $$8;
      }
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$1.getGameRules().get(GameRules.SPREAD_VINES)) {
         if ($$3.nextInt(4) == 0) {
            Direction $$4 = Direction.getRandom($$3);
            BlockPos $$5 = $$2.above();
            if ($$4.getAxis().isHorizontal() && !$$0.getValue(getPropertyForFace($$4))) {
               if (this.canSpread($$1, $$2)) {
                  BlockPos $$6 = $$2.relative($$4);
                  BlockState $$7 = $$1.getBlockState($$6);
                  if ($$7.isAir()) {
                     Direction $$8 = $$4.getClockWise();
                     Direction $$9 = $$4.getCounterClockWise();
                     boolean $$10 = $$0.getValue(getPropertyForFace($$8));
                     boolean $$11 = $$0.getValue(getPropertyForFace($$9));
                     BlockPos $$12 = $$6.relative($$8);
                     BlockPos $$13 = $$6.relative($$9);
                     if ($$10 && isAcceptableNeighbour($$1, $$12, $$8)) {
                        $$1.setBlock($$6, this.defaultBlockState().setValue(getPropertyForFace($$8), true), 2);
                     } else if ($$11 && isAcceptableNeighbour($$1, $$13, $$9)) {
                        $$1.setBlock($$6, this.defaultBlockState().setValue(getPropertyForFace($$9), true), 2);
                     } else {
                        Direction $$14 = $$4.getOpposite();
                        if ($$10 && $$1.isEmptyBlock($$12) && isAcceptableNeighbour($$1, $$2.relative($$8), $$14)) {
                           $$1.setBlock($$12, this.defaultBlockState().setValue(getPropertyForFace($$14), true), 2);
                        } else if ($$11 && $$1.isEmptyBlock($$13) && isAcceptableNeighbour($$1, $$2.relative($$9), $$14)) {
                           $$1.setBlock($$13, this.defaultBlockState().setValue(getPropertyForFace($$14), true), 2);
                        } else if ($$3.nextFloat() < 0.05 && isAcceptableNeighbour($$1, $$6.above(), Direction.UP)) {
                           $$1.setBlock($$6, this.defaultBlockState().setValue(UP, true), 2);
                        }
                     }
                  } else if (isAcceptableNeighbour($$1, $$6, $$4)) {
                     $$1.setBlock($$2, $$0.setValue(getPropertyForFace($$4), true), 2);
                  }
               }
            } else {
               if ($$4 == Direction.UP && $$2.getY() < $$1.getMaxY()) {
                  if (this.canSupportAtFace($$1, $$2, $$4)) {
                     $$1.setBlock($$2, $$0.setValue(UP, true), 2);
                     return;
                  }

                  if ($$1.isEmptyBlock($$5)) {
                     if (!this.canSpread($$1, $$2)) {
                        return;
                     }

                     BlockState $$15 = $$0;

                     for (Direction $$16 : Plane.HORIZONTAL) {
                        if ($$3.nextBoolean() || !isAcceptableNeighbour($$1, $$5.relative($$16), $$16)) {
                           $$15 = $$15.setValue(getPropertyForFace($$16), false);
                        }
                     }

                     if (this.hasHorizontalConnection($$15)) {
                        $$1.setBlock($$5, $$15, 2);
                     }

                     return;
                  }
               }

               if ($$2.getY() > $$1.getMinY()) {
                  BlockPos $$17 = $$2.below();
                  BlockState $$18 = $$1.getBlockState($$17);
                  if ($$18.isAir() || $$18.is(this)) {
                     BlockState $$19 = $$18.isAir() ? this.defaultBlockState() : $$18;
                     BlockState $$20 = this.copyRandomFaces($$0, $$19, $$3);
                     if ($$19 != $$20 && this.hasHorizontalConnection($$20)) {
                        $$1.setBlock($$17, $$20, 2);
                     }
                  }
               }
            }
         }
      }
   }

   private BlockState copyRandomFaces(BlockState $$0, BlockState $$1, RandomSource $$2) {
      for (Direction $$3 : Plane.HORIZONTAL) {
         if ($$2.nextBoolean()) {
            BooleanProperty $$4 = getPropertyForFace($$3);
            if ($$0.getValue($$4)) {
               $$1 = $$1.setValue($$4, true);
            }
         }
      }

      return $$1;
   }

   private boolean hasHorizontalConnection(BlockState $$0) {
      return $$0.getValue(NORTH) || $$0.getValue(EAST) || $$0.getValue(SOUTH) || $$0.getValue(WEST);
   }

   private boolean canSpread(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      int $$2 = 4;
      Iterable<BlockPos> $$3 = BlockPos.betweenClosed($$1.getX() - 4, $$1.getY() - 1, $$1.getZ() - 4, $$1.getX() + 4, $$1.getY() + 1, $$1.getZ() + 4);
      int $$4 = 5;

      for (BlockPos $$5 : $$3) {
         if ($$0.getBlockState($$5).is(this)) {
            if (--$$4 <= 0) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   protected boolean canBeReplaced(BlockState $$0, BlockPlaceContext $$1) {
      BlockState $$2 = $$1.getLevel().getBlockState($$1.getClickedPos());
      return $$2.is(this) ? this.countFaces($$2) < PROPERTY_BY_DIRECTION.size() : super.canBeReplaced($$0, $$1);
   }

   
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = $$0.getLevel().getBlockState($$0.getClickedPos());
      boolean $$2 = $$1.is(this);
      BlockState $$3 = $$2 ? $$1 : this.defaultBlockState();

      for (Direction $$4 : $$0.getNearestLookingDirections()) {
         if ($$4 != Direction.DOWN) {
            BooleanProperty $$5 = getPropertyForFace($$4);
            boolean $$6 = $$2 && $$1.getValue($$5);
            if (!$$6 && this.canSupportAtFace($$0.getLevel(), $$0.getClickedPos(), $$4)) {
               return $$3.setValue($$5, true);
            }
         }
      }

      return $$2 ? $$3 : null;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(UP, NORTH, EAST, SOUTH, WEST);
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

   public static BooleanProperty getPropertyForFace(Direction $$0) {
      return PROPERTY_BY_DIRECTION.get($$0);
   }
}
