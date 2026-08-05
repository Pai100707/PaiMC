package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MultifaceBlock extends Block implements SimpleWaterloggedBlock {
   public static final MapCodec<MultifaceBlock> CODEC = simpleCodec(MultifaceBlock::new);
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
   protected static final Direction[] DIRECTIONS = Direction.values();
   private final Function<BlockState, VoxelShape> shapes;
   private final boolean canRotate;
   private final boolean canMirrorX;
   private final boolean canMirrorZ;

   @Override
   protected MapCodec<? extends MultifaceBlock> codec() {
      return CODEC;
   }

   public MultifaceBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(getDefaultMultifaceState(this.stateDefinition));
      this.shapes = this.makeShapes();
      this.canRotate = Plane.HORIZONTAL.stream().allMatch(this::isFaceSupported);
      this.canMirrorX = Plane.HORIZONTAL.stream().filter(Axis.X).filter(this::isFaceSupported).count() % 2L == 0L;
      this.canMirrorZ = Plane.HORIZONTAL.stream().filter(Axis.Z).filter(this::isFaceSupported).count() % 2L == 0L;
   }

   private Function<BlockState, VoxelShape> makeShapes() {
      Map<Direction, VoxelShape> $$0 = Shapes.rotateAll(Block.boxZ(16.0, 0.0, 1.0));
      return this.getShapeForEachState($$1 -> {
         VoxelShape $$2 = Shapes.empty();

         for (Direction $$3 : DIRECTIONS) {
            if (hasFace($$1, $$3)) {
               $$2 = Shapes.or($$2, $$0.get($$3));
            }
         }

         return $$2.isEmpty() ? Shapes.block() : $$2;
      }, new Property[]{WATERLOGGED});
   }

   public static Set<Direction> availableFaces(BlockState $$0) {
      if (!($$0.getBlock() instanceof MultifaceBlock)) {
         return Set.of();
      } else {
         Set<Direction> $$1 = EnumSet.noneOf(Direction.class);

         for (Direction $$2 : Direction.values()) {
            if (hasFace($$0, $$2)) {
               $$1.add($$2);
            }
         }

         return $$1;
      }
   }

   public static Set<Direction> unpack(byte $$0) {
      Set<Direction> $$1 = EnumSet.noneOf(Direction.class);

      for (Direction $$2 : Direction.values()) {
         if (($$0 & (byte)(1 << $$2.ordinal())) > 0) {
            $$1.add($$2);
         }
      }

      return $$1;
   }

   public static byte pack(Collection<Direction> $$0) {
      byte $$1 = 0;

      for (Direction $$2 : $$0) {
         $$1 = (byte)($$1 | 1 << $$2.ordinal());
      }

      return $$1;
   }

   protected boolean isFaceSupported(Direction $$0) {
      return true;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      for (Direction $$1 : DIRECTIONS) {
         if (this.isFaceSupported($$1)) {
            $$0.add(getFaceProperty($$1));
         }
      }

      $$0.add(WATERLOGGED);
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
      if ($$0.getValue(WATERLOGGED)) {
         $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
      }

      if (!hasAnyFace($$0)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         return hasFace($$0, $$4) && !canAttachTo($$1, $$4, $$5, $$6) ? removeFace($$0, getFaceProperty($$4)) : $$0;
      }
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.shapes.apply($$0);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      boolean $$3 = false;

      for (Direction $$4 : DIRECTIONS) {
         if (hasFace($$0, $$4)) {
            if (!canAttachTo($$1, $$2, $$4)) {
               return false;
            }

            $$3 = true;
         }
      }

      return $$3;
   }

   @Override
   protected boolean canBeReplaced(BlockState $$0, BlockPlaceContext $$1) {
      return !$$1.getItemInHand().is(this.asItem()) || hasAnyVacantFace($$0);
   }

   
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      net.minecraft.world.level.Level $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      BlockState $$3 = $$1.getBlockState($$2);
      return Arrays.stream($$0.getNearestLookingDirections())
         .map($$3x -> this.getStateForPlacement($$3, $$1, $$2, $$3x))
         .filter(Objects::nonNull)
         .findFirst()
         .orElse(null);
   }

   public boolean isValidStateForPlacement(net.minecraft.world.level.BlockGetter $$0, BlockState $$1, BlockPos $$2, Direction $$3) {
      if (this.isFaceSupported($$3) && (!$$1.is(this) || !hasFace($$1, $$3))) {
         BlockPos $$4 = $$2.relative($$3);
         return canAttachTo($$0, $$3, $$4, $$0.getBlockState($$4));
      } else {
         return false;
      }
   }

   
   public BlockState getStateForPlacement(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      if (!this.isValidStateForPlacement($$1, $$0, $$2, $$3)) {
         return null;
      } else {
         BlockState $$4;
         if ($$0.is(this)) {
            $$4 = $$0;
         } else if ($$0.getFluidState().isSourceOfType(Fluids.WATER)) {
            $$4 = this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
         } else {
            $$4 = this.defaultBlockState();
         }

         return $$4.setValue(getFaceProperty($$3), true);
      }
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return !this.canRotate ? $$0 : this.mapDirections($$0, $$1::rotate);
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      if ($$1 == Mirror.FRONT_BACK && !this.canMirrorX) {
         return $$0;
      } else {
         return $$1 == Mirror.LEFT_RIGHT && !this.canMirrorZ ? $$0 : this.mapDirections($$0, $$1::mirror);
      }
   }

   private BlockState mapDirections(BlockState $$0, Function<Direction, Direction> $$1) {
      BlockState $$2 = $$0;

      for (Direction $$3 : DIRECTIONS) {
         if (this.isFaceSupported($$3)) {
            $$2 = $$2.setValue(getFaceProperty($$1.apply($$3)), $$0.getValue(getFaceProperty($$3)));
         }
      }

      return $$2;
   }

   public static boolean hasFace(BlockState $$0, Direction $$1) {
      BooleanProperty $$2 = getFaceProperty($$1);
      return $$0.getValueOrElse($$2, false);
   }

   public static boolean canAttachTo(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2) {
      BlockPos $$3 = $$1.relative($$2);
      BlockState $$4 = $$0.getBlockState($$3);
      return canAttachTo($$0, $$2, $$3, $$4);
   }

   public static boolean canAttachTo(net.minecraft.world.level.BlockGetter $$0, Direction $$1, BlockPos $$2, BlockState $$3) {
      return Block.isFaceFull($$3.getBlockSupportShape($$0, $$2), $$1.getOpposite()) || Block.isFaceFull($$3.getCollisionShape($$0, $$2), $$1.getOpposite());
   }

   private static BlockState removeFace(BlockState $$0, BooleanProperty $$1) {
      BlockState $$2 = $$0.setValue($$1, false);
      return hasAnyFace($$2) ? $$2 : Blocks.AIR.defaultBlockState();
   }

   public static BooleanProperty getFaceProperty(Direction $$0) {
      return PROPERTY_BY_DIRECTION.get($$0);
   }

   private static BlockState getDefaultMultifaceState(StateDefinition<Block, BlockState> $$0) {
      BlockState $$1 = $$0.any().setValue(WATERLOGGED, false);

      for (BooleanProperty $$2 : PROPERTY_BY_DIRECTION.values()) {
         $$1 = $$1.trySetValue($$2, false);
      }

      return $$1;
   }

   protected static boolean hasAnyFace(BlockState $$0) {
      for (Direction $$1 : DIRECTIONS) {
         if (hasFace($$0, $$1)) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasAnyVacantFace(BlockState $$0) {
      for (Direction $$1 : DIRECTIONS) {
         if (!hasFace($$0, $$1)) {
            return true;
         }
      }

      return false;
   }
}
