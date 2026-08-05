package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MossyCarpetBlock extends Block implements BonemealableBlock {
   public static final MapCodec<MossyCarpetBlock> CODEC = simpleCodec(MossyCarpetBlock::new);
   public static final BooleanProperty BASE = BlockStateProperties.BOTTOM;
   public static final EnumProperty<WallSide> NORTH = BlockStateProperties.NORTH_WALL;
   public static final EnumProperty<WallSide> EAST = BlockStateProperties.EAST_WALL;
   public static final EnumProperty<WallSide> SOUTH = BlockStateProperties.SOUTH_WALL;
   public static final EnumProperty<WallSide> WEST = BlockStateProperties.WEST_WALL;
   public static final Map<Direction, EnumProperty<WallSide>> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(
      Maps.newEnumMap(Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST))
   );
   private final Function<BlockState, VoxelShape> shapes;

   @Override
   public MapCodec<MossyCarpetBlock> codec() {
      return CODEC;
   }

   public MossyCarpetBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(
         this.stateDefinition
            .any()
            .setValue(BASE, true)
            .setValue(NORTH, WallSide.NONE)
            .setValue(EAST, WallSide.NONE)
            .setValue(SOUTH, WallSide.NONE)
            .setValue(WEST, WallSide.NONE)
      );
      this.shapes = this.makeShapes();
   }

   public Function<BlockState, VoxelShape> makeShapes() {
      Map<Direction, VoxelShape> $$0 = Shapes.rotateHorizontal(Block.boxZ(16.0, 0.0, 10.0, 0.0, 1.0));
      Map<Direction, VoxelShape> $$1 = Shapes.rotateAll(Block.boxZ(16.0, 0.0, 1.0));
      return this.getShapeForEachState($$2 -> {
         VoxelShape $$3 = $$2.getValue(BASE) ? $$1.get(Direction.DOWN) : Shapes.empty();

         for (Entry<Direction, EnumProperty<WallSide>> $$4 : PROPERTY_BY_DIRECTION.entrySet()) {
            switch ((WallSide)$$2.getValue($$4.getValue())) {
               case NONE:
               default:
                  break;
               case LOW:
                  $$3 = Shapes.or($$3, $$0.get($$4.getKey()));
                  break;
               case TALL:
                  $$3 = Shapes.or($$3, $$1.get($$4.getKey()));
            }
         }

         return $$3.isEmpty() ? Shapes.block() : $$3;
      });
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.shapes.apply($$0);
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return $$0.getValue(BASE) ? this.shapes.apply(this.defaultBlockState()) : Shapes.empty();
   }

   @Override
   protected boolean propagatesSkylightDown(BlockState $$0) {
      return true;
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockState $$3 = $$1.getBlockState($$2.below());
      return $$0.getValue(BASE) ? !$$3.isAir() : $$3.is(this) && $$3.getValue(BASE);
   }

   private static boolean hasFaces(BlockState $$0) {
      if ($$0.getValue(BASE)) {
         return true;
      } else {
         for (EnumProperty<WallSide> $$1 : PROPERTY_BY_DIRECTION.values()) {
            if ($$0.getValue($$1) != WallSide.NONE) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean canSupportAtFace(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2) {
      return $$2 == Direction.UP ? false : MultifaceBlock.canAttachTo($$0, $$1, $$2);
   }

   private static BlockState getUpdatedState(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, boolean $$3) {
      BlockState $$4 = null;
      BlockState $$5 = null;
      $$3 |= $$0.getValue(BASE);

      for (Direction $$6 : Plane.HORIZONTAL) {
         EnumProperty<WallSide> $$7 = getPropertyForFace($$6);
         WallSide $$8 = canSupportAtFace($$1, $$2, $$6) ? ($$3 ? WallSide.LOW : $$0.getValue($$7)) : WallSide.NONE;
         if ($$8 == WallSide.LOW) {
            if ($$4 == null) {
               $$4 = $$1.getBlockState($$2.above());
            }

            if ($$4.is(Blocks.PALE_MOSS_CARPET) && $$4.getValue($$7) != WallSide.NONE && !$$4.getValue(BASE)) {
               $$8 = WallSide.TALL;
            }

            if (!$$0.getValue(BASE)) {
               if ($$5 == null) {
                  $$5 = $$1.getBlockState($$2.below());
               }

               if ($$5.is(Blocks.PALE_MOSS_CARPET) && $$5.getValue($$7) == WallSide.NONE) {
                  $$8 = WallSide.NONE;
               }
            }
         }

         $$0 = $$0.setValue($$7, $$8);
      }

      return $$0;
   }

   
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return getUpdatedState(this.defaultBlockState(), $$0.getLevel(), $$0.getClickedPos(), true);
   }

   public static void placeAt(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, RandomSource $$2, @Block.UpdateFlags int $$3) {
      BlockState $$4 = Blocks.PALE_MOSS_CARPET.defaultBlockState();
      BlockState $$5 = getUpdatedState($$4, $$0, $$1, true);
      $$0.setBlock($$1, $$5, $$3);
      BlockState $$6 = createTopperWithSideChance($$0, $$1, $$2::nextBoolean);
      if (!$$6.isAir()) {
         $$0.setBlock($$1.above(), $$6, $$3);
         BlockState $$7 = getUpdatedState($$5, $$0, $$1, true);
         $$0.setBlock($$1, $$7, $$3);
      }
   }

   @Override
   public void setPlacedBy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, LivingEntity $$3, ItemStack $$4) {
      if (!$$0.isClientSide()) {
         RandomSource $$5 = $$0.getRandom();
         BlockState $$6 = createTopperWithSideChance($$0, $$1, $$5::nextBoolean);
         if (!$$6.isAir()) {
            $$0.setBlock($$1.above(), $$6, 3);
         }
      }
   }

   private static BlockState createTopperWithSideChance(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BooleanSupplier $$2) {
      BlockPos $$3 = $$1.above();
      BlockState $$4 = $$0.getBlockState($$3);
      boolean $$5 = $$4.is(Blocks.PALE_MOSS_CARPET);
      if ((!$$5 || !$$4.getValue(BASE)) && ($$5 || $$4.canBeReplaced())) {
         BlockState $$6 = Blocks.PALE_MOSS_CARPET.defaultBlockState().setValue(BASE, false);
         BlockState $$7 = getUpdatedState($$6, $$0, $$1.above(), true);

         for (Direction $$8 : Plane.HORIZONTAL) {
            EnumProperty<WallSide> $$9 = getPropertyForFace($$8);
            if ($$7.getValue($$9) != WallSide.NONE && !$$2.getAsBoolean()) {
               $$7 = $$7.setValue($$9, WallSide.NONE);
            }
         }

         return hasFaces($$7) && $$7 != $$4 ? $$7 : Blocks.AIR.defaultBlockState();
      } else {
         return Blocks.AIR.defaultBlockState();
      }
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
      if (!$$0.canSurvive($$1, $$3)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         BlockState $$8 = getUpdatedState($$0, $$1, $$3, false);
         return !hasFaces($$8) ? Blocks.AIR.defaultBlockState() : $$8;
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(BASE, NORTH, EAST, SOUTH, WEST);
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return switch ($$1) {
         case CLOCKWISE_180 -> (BlockState)$$0.setValue(NORTH, $$0.getValue(SOUTH))
            .setValue(EAST, $$0.getValue(WEST))
            .setValue(SOUTH, $$0.getValue(NORTH))
            .setValue(WEST, $$0.getValue(EAST));
         case COUNTERCLOCKWISE_90 -> (BlockState)$$0.setValue(NORTH, $$0.getValue(EAST))
            .setValue(EAST, $$0.getValue(SOUTH))
            .setValue(SOUTH, $$0.getValue(WEST))
            .setValue(WEST, $$0.getValue(NORTH));
         case CLOCKWISE_90 -> (BlockState)$$0.setValue(NORTH, $$0.getValue(WEST))
            .setValue(EAST, $$0.getValue(NORTH))
            .setValue(SOUTH, $$0.getValue(EAST))
            .setValue(WEST, $$0.getValue(SOUTH));
         default -> $$0;
      };
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return switch ($$1) {
         case LEFT_RIGHT -> (BlockState)$$0.setValue(NORTH, $$0.getValue(SOUTH)).setValue(SOUTH, $$0.getValue(NORTH));
         case FRONT_BACK -> (BlockState)$$0.setValue(EAST, $$0.getValue(WEST)).setValue(WEST, $$0.getValue(EAST));
         default -> super.mirror($$0, $$1);
      };
   }

   
   public static EnumProperty<WallSide> getPropertyForFace(Direction $$0) {
      return PROPERTY_BY_DIRECTION.get($$0);
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return $$2.getValue(BASE) && !createTopperWithSideChance($$0, $$1, () -> true).isAir();
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      BlockState $$4 = createTopperWithSideChance($$0, $$2, () -> true);
      if (!$$4.isAir()) {
         $$0.setBlock($$2.above(), $$4, 3);
      }
   }
}
