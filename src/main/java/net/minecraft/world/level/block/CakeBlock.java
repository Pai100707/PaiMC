package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CakeBlock extends Block {
   public static final MapCodec<CakeBlock> CODEC = simpleCodec(CakeBlock::new);
   public static final int MAX_BITES = 6;
   public static final IntegerProperty BITES = BlockStateProperties.BITES;
   public static final int FULL_CAKE_SIGNAL = getOutputSignal(0);
   private static final VoxelShape[] SHAPES = Block.boxes(6, $$0 -> Block.box(1 + $$0 * 2, 0.0, 1.0, 15.0, 8.0, 15.0));

   @Override
   public MapCodec<CakeBlock> codec() {
      return CODEC;
   }

   protected CakeBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES[$$0.getValue(BITES)];
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      Item $$7 = $$0.getItem();
      if ($$0.is(ItemTags.CANDLES) && $$1.getValue(BITES) == 0 && Block.byItem($$7) instanceof CandleBlock $$8) {
         $$0.consume(1, $$4);
         $$2.playSound(null, $$3, SoundEvents.CAKE_ADD_CANDLE, SoundSource.BLOCKS, 1.0F, 1.0F);
         $$2.setBlockAndUpdate($$3, CandleCakeBlock.byCandle($$8));
         $$2.gameEvent($$4, GameEvent.BLOCK_CHANGE, $$3);
         $$4.awardStat(Stats.ITEM_USED.get($$7));
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$1.isClientSide()) {
         if (eat($$1, $$2, $$0, $$3).consumesAction()) {
            return InteractionResult.SUCCESS;
         }

         if ($$3.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            return InteractionResult.CONSUME;
         }
      }

      return eat($$1, $$2, $$0, $$3);
   }

   protected static InteractionResult eat(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2, Player $$3) {
      if (!$$3.canEat(false)) {
         return InteractionResult.PASS;
      } else {
         $$3.awardStat(Stats.EAT_CAKE_SLICE);
         $$3.getFoodData().eat(2, 0.1F);
         int $$4 = $$2.getValue(BITES);
         $$0.gameEvent($$3, GameEvent.EAT, $$1);
         if ($$4 < 6) {
            $$0.setBlock($$1, $$2.setValue(BITES, $$4 + 1), 3);
         } else {
            $$0.removeBlock($$1, false);
            $$0.gameEvent($$3, GameEvent.BLOCK_DESTROY, $$1);
         }

         return InteractionResult.SUCCESS;
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
      return $$4 == Direction.DOWN && !$$0.canSurvive($$1, $$3) ? Blocks.AIR.defaultBlockState() : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return $$1.getBlockState($$2.below()).isSolid();
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(BITES);
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return getOutputSignal($$0.getValue(BITES));
   }

   public static int getOutputSignal(int $$0) {
      return (7 - $$0) * 2;
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }
}
