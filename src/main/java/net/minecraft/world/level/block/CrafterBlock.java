package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeCache;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CrafterBlock extends BaseEntityBlock {
   public static final MapCodec<CrafterBlock> CODEC = simpleCodec(CrafterBlock::new);
   public static final BooleanProperty CRAFTING = BlockStateProperties.CRAFTING;
   public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
   private static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;
   private static final int MAX_CRAFTING_TICKS = 6;
   private static final int CRAFTING_TICK_DELAY = 4;
   private static final RecipeCache RECIPE_CACHE = new RecipeCache(10);
   private static final int CRAFTER_ADVANCEMENT_DIAMETER = 17;

   public CrafterBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, FrontAndTop.NORTH_UP).setValue(TRIGGERED, false).setValue(CRAFTING, false));
   }

   @Override
   protected MapCodec<CrafterBlock> codec() {
      return CODEC;
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return $$1.getBlockEntity($$2) instanceof CrafterBlockEntity $$5 ? $$5.getRedstoneSignal() : 0;
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      boolean $$6 = $$1.hasNeighborSignal($$2);
      boolean $$7 = $$0.getValue(TRIGGERED);
      BlockEntity $$8 = $$1.getBlockEntity($$2);
      if ($$6 && !$$7) {
         $$1.scheduleTick($$2, this, 4);
         $$1.setBlock($$2, $$0.setValue(TRIGGERED, true), 2);
         this.setBlockEntityTriggered($$8, true);
      } else if (!$$6 && $$7) {
         $$1.setBlock($$2, $$0.setValue(TRIGGERED, false).setValue(CRAFTING, false), 2);
         this.setBlockEntityTriggered($$8, false);
      }
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      this.dispenseFrom($$0, $$1, $$2);
   }

   
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return $$0.isClientSide() ? null : createTickerHelper($$2, BlockEntityType.CRAFTER, CrafterBlockEntity::serverTick);
   }

   private void setBlockEntityTriggered(BlockEntity $$0, boolean $$1) {
      if ($$0 instanceof CrafterBlockEntity $$2) {
         $$2.setTriggered($$1);
      }
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      CrafterBlockEntity $$2 = new CrafterBlockEntity($$0, $$1);
      $$2.setTriggered($$1.hasProperty(TRIGGERED) && $$1.getValue(TRIGGERED));
      return $$2;
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      Direction $$1 = $$0.getNearestLookingDirection().getOpposite();

      Direction $$2 = switch ($$1) {
         case DOWN -> $$0.getHorizontalDirection().getOpposite();
         case UP -> $$0.getHorizontalDirection();
         case NORTH, SOUTH, WEST, EAST -> Direction.UP;
         default -> throw new MatchException(null, null);
      };
      return this.defaultBlockState()
         .setValue(ORIENTATION, FrontAndTop.fromFrontAndTop($$1, $$2))
         .setValue(TRIGGERED, $$0.getLevel().hasNeighborSignal($$0.getClickedPos()));
   }

   @Override
   public void setPlacedBy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, LivingEntity $$3, ItemStack $$4) {
      if ($$2.getValue(TRIGGERED)) {
         $$0.scheduleTick($$1, this, 4);
      }
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      Containers.updateNeighboursAfterDestroy($$0, $$1, $$2);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!$$1.isClientSide() && $$1.getBlockEntity($$2) instanceof CrafterBlockEntity $$5) {
         $$3.openMenu($$5);
      }

      return InteractionResult.SUCCESS;
   }

   protected void dispenseFrom(BlockState $$0, ServerLevel $$1, BlockPos $$2) {
      if ($$1.getBlockEntity($$2) instanceof CrafterBlockEntity $$3) {
         CraftingInput var11 = $$3.asCraftInput();
         Optional<RecipeHolder<CraftingRecipe>> $$6 = getPotentialResults($$1, var11);
         if ($$6.isEmpty()) {
            $$1.levelEvent(1050, $$2, 0);
         } else {
            RecipeHolder<CraftingRecipe> $$7 = $$6.get();
            ItemStack $$8 = ((CraftingRecipe)$$7.value()).assemble(var11, $$1.registryAccess());
            if ($$8.isEmpty()) {
               $$1.levelEvent(1050, $$2, 0);
            } else {
               $$3.setCraftingTicksRemaining(6);
               $$1.setBlock($$2, $$0.setValue(CRAFTING, true), 2);
               $$8.onCraftedBySystem($$1);
               this.dispenseItem($$1, $$2, $$3, $$8, $$0, $$7);

               for (ItemStack $$9 : ((CraftingRecipe)$$7.value()).getRemainingItems(var11)) {
                  if (!$$9.isEmpty()) {
                     this.dispenseItem($$1, $$2, $$3, $$9, $$0, $$7);
                  }
               }

               $$3.getItems().forEach($$0x -> {
                  if (!$$0x.isEmpty()) {
                     $$0x.shrink(1);
                  }
               });
               $$3.setChanged();
            }
         }
      }
   }

   public static Optional<RecipeHolder<CraftingRecipe>> getPotentialResults(ServerLevel $$0, CraftingInput $$1) {
      return RECIPE_CACHE.get($$0, $$1);
   }

   private void dispenseItem(ServerLevel $$0, BlockPos $$1, CrafterBlockEntity $$2, ItemStack $$3, BlockState $$4, RecipeHolder<?> $$5) {
      Direction $$6 = ((FrontAndTop)$$4.getValue(ORIENTATION)).front();
      Container $$7 = HopperBlockEntity.getContainerAt($$0, $$1.relative($$6));
      ItemStack $$8 = $$3.copy();
      if ($$7 != null && ($$7 instanceof CrafterBlockEntity || $$3.getCount() > $$7.getMaxStackSize($$3))) {
         while (!$$8.isEmpty()) {
            ItemStack $$9 = $$8.copyWithCount(1);
            ItemStack $$10 = HopperBlockEntity.addItem($$2, $$7, $$9, $$6.getOpposite());
            if (!$$10.isEmpty()) {
               break;
            }

            $$8.shrink(1);
         }
      } else if ($$7 != null) {
         while (!$$8.isEmpty()) {
            int $$11 = $$8.getCount();
            $$8 = HopperBlockEntity.addItem($$2, $$7, $$8, $$6.getOpposite());
            if ($$11 == $$8.getCount()) {
               break;
            }
         }
      }

      if (!$$8.isEmpty()) {
         Vec3 $$12 = Vec3.atCenterOf($$1);
         Vec3 $$13 = $$12.relative($$6, 0.7);
         DefaultDispenseItemBehavior.spawnItem($$0, $$8, 6, $$6, $$13);

         for (ServerPlayer $$14 : $$0.getEntitiesOfClass(ServerPlayer.class, AABB.ofSize($$12, 17.0, 17.0, 17.0))) {
            CriteriaTriggers.CRAFTER_RECIPE_CRAFTED.trigger($$14, $$5.id(), $$2.getItems());
         }

         $$0.levelEvent(1049, $$1, 0);
         $$0.levelEvent(2010, $$1, $$6.get3DDataValue());
      }
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(ORIENTATION, $$1.rotation().rotate($$0.getValue(ORIENTATION)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.setValue(ORIENTATION, $$1.rotation().rotate($$0.getValue(ORIENTATION)));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(ORIENTATION, TRIGGERED, CRAFTING);
   }
}
