package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DoorBlock extends Block {
   public static final MapCodec<DoorBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter(DoorBlock::type), propertiesCodec()).apply($$0, DoorBlock::new)
   );
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
   public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(Block.boxZ(16.0, 13.0, 16.0));
   private final BlockSetType type;

   @Override
   public MapCodec<? extends DoorBlock> codec() {
      return CODEC;
   }

   protected DoorBlock(BlockSetType $$0, BlockBehaviour.Properties $$1) {
      super($$1.sound($$0.soundType()));
      this.type = $$0;
      this.registerDefaultState(
         this.stateDefinition
            .any()
            .setValue(FACING, Direction.NORTH)
            .setValue(OPEN, false)
            .setValue(HINGE, DoorHingeSide.LEFT)
            .setValue(POWERED, false)
            .setValue(HALF, DoubleBlockHalf.LOWER)
      );
   }

   public BlockSetType type() {
      return this.type;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      Direction $$4 = $$0.getValue(FACING);
      Direction $$5 = $$0.getValue(OPEN) ? ($$0.getValue(HINGE) == DoorHingeSide.RIGHT ? $$4.getCounterClockWise() : $$4.getClockWise()) : $$4;
      return SHAPES.get($$5);
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
      DoubleBlockHalf $$8 = $$0.getValue(HALF);
      if ($$4.getAxis() != Axis.Y || $$8 == DoubleBlockHalf.LOWER != ($$4 == Direction.UP)) {
         return $$8 == DoubleBlockHalf.LOWER && $$4 == Direction.DOWN && !$$0.canSurvive($$1, $$3)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      } else {
         return $$6.getBlock() instanceof DoorBlock && $$6.getValue(HALF) != $$8 ? $$6.setValue(HALF, $$8) : Blocks.AIR.defaultBlockState();
      }
   }

   @Override
   protected void onExplosionHit(BlockState $$0, ServerLevel $$1, BlockPos $$2, net.minecraft.world.level.Explosion $$3, BiConsumer<ItemStack, BlockPos> $$4) {
      if ($$3.canTriggerBlocks() && $$0.getValue(HALF) == DoubleBlockHalf.LOWER && this.type.canOpenByWindCharge() && !$$0.getValue(POWERED)) {
         this.setOpen(null, $$1, $$0, $$2, !this.isOpen($$0));
      }

      super.onExplosionHit($$0, $$1, $$2, $$3, $$4);
   }

   @Override
   public BlockState playerWillDestroy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Player $$3) {
      if (!$$0.isClientSide() && ($$3.preventsBlockDrops() || !$$3.hasCorrectToolForDrops($$2))) {
         DoublePlantBlock.preventDropFromBottomPart($$0, $$1, $$2, $$3);
      }

      return super.playerWillDestroy($$0, $$1, $$2, $$3);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return switch ($$1) {
         case LAND, AIR -> $$0.getValue(OPEN);
         case WATER -> false;
      };
   }

   
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockPos $$1 = $$0.getClickedPos();
      net.minecraft.world.level.Level $$2 = $$0.getLevel();
      if ($$1.getY() < $$2.getMaxY() && $$2.getBlockState($$1.above()).canBeReplaced($$0)) {
         boolean $$3 = $$2.hasNeighborSignal($$1) || $$2.hasNeighborSignal($$1.above());
         return this.defaultBlockState()
            .setValue(FACING, $$0.getHorizontalDirection())
            .setValue(HINGE, this.getHinge($$0))
            .setValue(POWERED, $$3)
            .setValue(OPEN, $$3)
            .setValue(HALF, DoubleBlockHalf.LOWER);
      } else {
         return null;
      }
   }

   @Override
   public void setPlacedBy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, LivingEntity $$3, ItemStack $$4) {
      $$0.setBlock($$1.above(), $$2.setValue(HALF, DoubleBlockHalf.UPPER), 3);
   }

   private DoorHingeSide getHinge(BlockPlaceContext $$0) {
      net.minecraft.world.level.BlockGetter $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      Direction $$3 = $$0.getHorizontalDirection();
      BlockPos $$4 = $$2.above();
      Direction $$5 = $$3.getCounterClockWise();
      BlockPos $$6 = $$2.relative($$5);
      BlockState $$7 = $$1.getBlockState($$6);
      BlockPos $$8 = $$4.relative($$5);
      BlockState $$9 = $$1.getBlockState($$8);
      Direction $$10 = $$3.getClockWise();
      BlockPos $$11 = $$2.relative($$10);
      BlockState $$12 = $$1.getBlockState($$11);
      BlockPos $$13 = $$4.relative($$10);
      BlockState $$14 = $$1.getBlockState($$13);
      int $$15 = ($$7.isCollisionShapeFullBlock($$1, $$6) ? -1 : 0)
         + ($$9.isCollisionShapeFullBlock($$1, $$8) ? -1 : 0)
         + ($$12.isCollisionShapeFullBlock($$1, $$11) ? 1 : 0)
         + ($$14.isCollisionShapeFullBlock($$1, $$13) ? 1 : 0);
      boolean $$16 = $$7.getBlock() instanceof DoorBlock && $$7.getValue(HALF) == DoubleBlockHalf.LOWER;
      boolean $$17 = $$12.getBlock() instanceof DoorBlock && $$12.getValue(HALF) == DoubleBlockHalf.LOWER;
      if ((!$$16 || $$17) && $$15 <= 0) {
         if ((!$$17 || $$16) && $$15 >= 0) {
            int $$18 = $$3.getStepX();
            int $$19 = $$3.getStepZ();
            Vec3 $$20 = $$0.getClickLocation();
            double $$21 = $$20.x - $$2.getX();
            double $$22 = $$20.z - $$2.getZ();
            return ($$18 >= 0 || !($$22 < 0.5)) && ($$18 <= 0 || !($$22 > 0.5)) && ($$19 >= 0 || !($$21 > 0.5)) && ($$19 <= 0 || !($$21 < 0.5))
               ? DoorHingeSide.LEFT
               : DoorHingeSide.RIGHT;
         } else {
            return DoorHingeSide.LEFT;
         }
      } else {
         return DoorHingeSide.RIGHT;
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!this.type.canOpenByHand()) {
         return InteractionResult.PASS;
      } else {
         $$0 = $$0.cycle(OPEN);
         $$1.setBlock($$2, $$0, 10);
         this.playSound($$3, $$1, $$2, $$0.getValue(OPEN));
         $$1.gameEvent($$3, this.isOpen($$0) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, $$2);
         return InteractionResult.SUCCESS;
      }
   }

   public boolean isOpen(BlockState $$0) {
      return $$0.getValue(OPEN);
   }

   public void setOpen(Entity $$0, net.minecraft.world.level.Level $$1, BlockState $$2, BlockPos $$3, boolean $$4) {
      if ($$2.is(this) && $$2.getValue(OPEN) != $$4) {
         $$1.setBlock($$3, $$2.setValue(OPEN, $$4), 10);
         this.playSound($$0, $$1, $$3, $$4);
         $$1.gameEvent($$0, $$4 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, $$3);
      }
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      boolean $$6 = $$1.hasNeighborSignal($$2)
         || $$1.hasNeighborSignal($$2.relative($$0.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
      if (!this.defaultBlockState().is($$3) && $$6 != $$0.getValue(POWERED)) {
         if ($$6 != $$0.getValue(OPEN)) {
            this.playSound(null, $$1, $$2, $$6);
            $$1.gameEvent(null, $$6 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, $$2);
         }

         $$1.setBlock($$2, $$0.setValue(POWERED, $$6).setValue(OPEN, $$6), 2);
      }
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.below();
      BlockState $$4 = $$1.getBlockState($$3);
      return $$0.getValue(HALF) == DoubleBlockHalf.LOWER ? $$4.isFaceSturdy($$1, $$3, Direction.UP) : $$4.is(this);
   }

   private void playSound(Entity $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, boolean $$3) {
      $$1.playSound($$0, $$2, $$3 ? this.type.doorOpen() : this.type.doorClose(), SoundSource.BLOCKS, 1.0F, $$1.getRandom().nextFloat() * 0.1F + 0.9F);
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$1 == Mirror.NONE ? $$0 : $$0.rotate($$1.getRotation($$0.getValue(FACING))).cycle(HINGE);
   }

   @Override
   protected long getSeed(BlockState $$0, BlockPos $$1) {
      return Mth.getSeed($$1.getX(), $$1.below($$0.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), $$1.getZ());
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(HALF, FACING, OPEN, HINGE, POWERED);
   }

   public static boolean isWoodenDoor(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      return isWoodenDoor($$0.getBlockState($$1));
   }

   public static boolean isWoodenDoor(BlockState $$0) {
      return $$0.getBlock() instanceof DoorBlock $$1 && $$1.type().canOpenByHand();
   }
}
