package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class ChiseledBookShelfBlock extends BaseEntityBlock implements SelectableSlotContainer {
   public static final MapCodec<ChiseledBookShelfBlock> CODEC = simpleCodec(ChiseledBookShelfBlock::new);
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty SLOT_0_OCCUPIED = BlockStateProperties.SLOT_0_OCCUPIED;
   public static final BooleanProperty SLOT_1_OCCUPIED = BlockStateProperties.SLOT_1_OCCUPIED;
   public static final BooleanProperty SLOT_2_OCCUPIED = BlockStateProperties.SLOT_2_OCCUPIED;
   public static final BooleanProperty SLOT_3_OCCUPIED = BlockStateProperties.SLOT_3_OCCUPIED;
   public static final BooleanProperty SLOT_4_OCCUPIED = BlockStateProperties.SLOT_4_OCCUPIED;
   public static final BooleanProperty SLOT_5_OCCUPIED = BlockStateProperties.SLOT_5_OCCUPIED;
   private static final int MAX_BOOKS_IN_STORAGE = 6;
   private static final int BOOKS_PER_ROW = 3;
   public static final List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES = List.of(
      SLOT_0_OCCUPIED, SLOT_1_OCCUPIED, SLOT_2_OCCUPIED, SLOT_3_OCCUPIED, SLOT_4_OCCUPIED, SLOT_5_OCCUPIED
   );

   @Override
   public MapCodec<ChiseledBookShelfBlock> codec() {
      return CODEC;
   }

   @Override
   public int getRows() {
      return 2;
   }

   @Override
   public int getColumns() {
      return 3;
   }

   public ChiseledBookShelfBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      BlockState $$1 = this.stateDefinition.any().setValue(FACING, Direction.NORTH);

      for (BooleanProperty $$2 : SLOT_OCCUPIED_PROPERTIES) {
         $$1 = $$1.setValue($$2, false);
      }

      this.registerDefaultState($$1);
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if ($$2.getBlockEntity($$3) instanceof ChiseledBookShelfBlockEntity $$7) {
         if (!$$0.is(ItemTags.BOOKSHELF_BOOKS)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
         } else {
            OptionalInt $$9 = this.getHitSlot($$6, $$1.getValue(FACING));
            if ($$9.isEmpty()) {
               return InteractionResult.PASS;
            } else if ($$1.getValue(SLOT_OCCUPIED_PROPERTIES.get($$9.getAsInt()))) {
               return InteractionResult.TRY_WITH_EMPTY_HAND;
            } else {
               addBook($$2, $$3, $$4, $$7, $$0, $$9.getAsInt());
               return InteractionResult.SUCCESS;
            }
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$1.getBlockEntity($$2) instanceof ChiseledBookShelfBlockEntity $$5) {
         OptionalInt $$7 = this.getHitSlot($$4, $$0.getValue(FACING));
         if ($$7.isEmpty()) {
            return InteractionResult.PASS;
         } else if (!$$0.getValue(SLOT_OCCUPIED_PROPERTIES.get($$7.getAsInt()))) {
            return InteractionResult.CONSUME;
         } else {
            removeBook($$1, $$2, $$3, $$5, $$7.getAsInt());
            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private static void addBook(net.minecraft.world.level.Level $$0, BlockPos $$1, Player $$2, ChiseledBookShelfBlockEntity $$3, ItemStack $$4, int $$5) {
      if (!$$0.isClientSide()) {
         $$2.awardStat(Stats.ITEM_USED.get($$4.getItem()));
         SoundEvent $$6 = $$4.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_INSERT;
         $$3.setItem($$5, $$4.consumeAndReturn(1, $$2));
         $$0.playSound(null, $$1, $$6, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   private static void removeBook(net.minecraft.world.level.Level $$0, BlockPos $$1, Player $$2, ChiseledBookShelfBlockEntity $$3, int $$4) {
      if (!$$0.isClientSide()) {
         ItemStack $$5 = $$3.removeItem($$4, 1);
         SoundEvent $$6 = $$5.is(Items.ENCHANTED_BOOK) ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.CHISELED_BOOKSHELF_PICKUP;
         $$0.playSound(null, $$1, $$6, SoundSource.BLOCKS, 1.0F, 1.0F);
         if (!$$2.getInventory().add($$5)) {
            $$2.drop($$5, false);
         }

         $$0.gameEvent($$2, GameEvent.BLOCK_CHANGE, $$1);
      }
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new ChiseledBookShelfBlockEntity($$0, $$1);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING);
      SLOT_OCCUPIED_PROPERTIES.forEach($$1 -> $$0.add($$1));
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      Containers.updateNeighboursAfterDestroy($$0, $$1, $$2);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(FACING, $$0.getHorizontalDirection().getOpposite());
   }

   @Override
   public BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   public BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(FACING)));
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      if ($$1.isClientSide()) {
         return 0;
      } else {
         return $$1.getBlockEntity($$2) instanceof ChiseledBookShelfBlockEntity $$4 ? $$4.getLastInteractedSlot() + 1 : 0;
      }
   }
}
