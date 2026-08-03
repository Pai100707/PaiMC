package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class TrapDoorBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
   public static final MapCodec<TrapDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter($$0x -> $$0x.type), propertiesCodec()).apply($$0, TrapDoorBlock::new)
   );
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateAll(Block.boxZ(16.0, 13.0, 16.0));
   private final BlockSetType type;

   @Override
   public MapCodec<? extends TrapDoorBlock> codec() {
      return CODEC;
   }

   protected TrapDoorBlock(BlockSetType $$0, BlockBehaviour.Properties $$1) {
      super($$1.sound($$0.soundType()));
      this.type = $$0;
      this.registerDefaultState(
         this.stateDefinition
            .any()
            .setValue(FACING, Direction.NORTH)
            .setValue(OPEN, false)
            .setValue(HALF, Half.BOTTOM)
            .setValue(POWERED, false)
            .setValue(WATERLOGGED, false)
      );
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES.get($$0.getValue(OPEN) ? $$0.getValue(FACING) : ($$0.getValue(HALF) == Half.TOP ? Direction.DOWN : Direction.UP));
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      switch ($$1) {
         case LAND:
            return $$0.getValue(OPEN);
         case WATER:
            return $$0.getValue(WATERLOGGED);
         case AIR:
            return $$0.getValue(OPEN);
         default:
            return false;
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!this.type.canOpenByHand()) {
         return InteractionResult.PASS;
      } else {
         this.toggle($$0, $$1, $$2, $$3);
         return InteractionResult.SUCCESS;
      }
   }

   @Override
   protected void onExplosionHit(BlockState $$0, ServerLevel $$1, BlockPos $$2, net.minecraft.world.level.Explosion $$3, BiConsumer<ItemStack, BlockPos> $$4) {
      if ($$3.canTriggerBlocks() && this.type.canOpenByWindCharge() && !$$0.getValue(POWERED)) {
         this.toggle($$0, $$1, $$2, null);
      }

      super.onExplosionHit($$0, $$1, $$2, $$3, $$4);
   }

   private void toggle(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, @Nullable Player $$3) {
      BlockState $$4 = $$0.cycle(OPEN);
      $$1.setBlock($$2, $$4, 2);
      if ($$4.getValue(WATERLOGGED)) {
         $$1.scheduleTick($$2, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
      }

      this.playSound($$3, $$1, $$2, $$4.getValue(OPEN));
   }

   protected void playSound(@Nullable Player $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, boolean $$3) {
      $$1.playSound($$0, $$2, $$3 ? this.type.trapdoorOpen() : this.type.trapdoorClose(), SoundSource.BLOCKS, 1.0F, $$1.getRandom().nextFloat() * 0.1F + 0.9F);
      $$1.gameEvent($$0, $$3 ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, $$2);
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, @Nullable Orientation $$4, boolean $$5) {
      if (!$$1.isClientSide()) {
         boolean $$6 = $$1.hasNeighborSignal($$2);
         if ($$6 != $$0.getValue(POWERED)) {
            if ($$0.getValue(OPEN) != $$6) {
               $$0 = $$0.setValue(OPEN, $$6);
               this.playSound(null, $$1, $$2, $$6);
            }

            $$1.setBlock($$2, $$0.setValue(POWERED, $$6), 2);
            if ($$0.getValue(WATERLOGGED)) {
               $$1.scheduleTick($$2, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
            }
         }
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      BlockState $$1 = this.defaultBlockState();
      FluidState $$2 = $$0.getLevel().getFluidState($$0.getClickedPos());
      Direction $$3 = $$0.getClickedFace();
      if (!$$0.replacingClickedOnBlock() && $$3.getAxis().isHorizontal()) {
         $$1 = $$1.setValue(FACING, $$3).setValue(HALF, $$0.getClickLocation().y - $$0.getClickedPos().getY() > 0.5 ? Half.TOP : Half.BOTTOM);
      } else {
         $$1 = $$1.setValue(FACING, $$0.getHorizontalDirection().getOpposite()).setValue(HALF, $$3 == Direction.UP ? Half.BOTTOM : Half.TOP);
      }

      if ($$0.getLevel().hasNeighborSignal($$0.getClickedPos())) {
         $$1 = $$1.setValue(OPEN, true).setValue(POWERED, true);
      }

      return $$1.setValue(WATERLOGGED, $$2.getType() == Fluids.WATER);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, OPEN, HALF, POWERED, WATERLOGGED);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
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

      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   protected BlockSetType getType() {
      return this.type;
   }
}
