package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DecoratedPotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final MapCodec<DecoratedPotBlock> CODEC = simpleCodec(DecoratedPotBlock::new);
   public static final Identifier SHERDS_DYNAMIC_DROP_ID = Identifier.withDefaultNamespace("sherds");
   public static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty CRACKED = BlockStateProperties.CRACKED;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 16.0);

   @Override
   public MapCodec<DecoratedPotBlock> codec() {
      return CODEC;
   }

   protected DecoratedPotBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(CRACKED, false));
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

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      FluidState $$1 = $$0.getLevel().getFluidState($$0.getClickedPos());
      return this.defaultBlockState()
         .setValue(HORIZONTAL_FACING, $$0.getHorizontalDirection())
         .setValue(WATERLOGGED, $$1.getType() == Fluids.WATER)
         .setValue(CRACKED, false);
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if ($$2.getBlockEntity($$3) instanceof DecoratedPotBlockEntity $$7) {
         if ($$2.isClientSide()) {
            return InteractionResult.SUCCESS;
         } else {
            ItemStack $$9 = $$7.getTheItem();
            if (!$$0.isEmpty() && ($$9.isEmpty() || ItemStack.isSameItemSameComponents($$9, $$0) && $$9.getCount() < $$9.getMaxStackSize())) {
               $$7.wobble(DecoratedPotBlockEntity.WobbleStyle.POSITIVE);
               $$4.awardStat(Stats.ITEM_USED.get($$0.getItem()));
               ItemStack $$10 = $$0.consumeAndReturn(1, $$4);
               float $$11;
               if ($$7.isEmpty()) {
                  $$7.setTheItem($$10);
                  $$11 = (float)$$10.getCount() / $$10.getMaxStackSize();
               } else {
                  $$9.grow(1);
                  $$11 = (float)$$9.getCount() / $$9.getMaxStackSize();
               }

               $$2.playSound(null, $$3, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * $$11);
               if ($$2 instanceof ServerLevel $$13) {
                  $$13.sendParticles(ParticleTypes.DUST_PLUME, $$3.getX() + 0.5, $$3.getY() + 1.2, $$3.getZ() + 0.5, 7, 0.0, 0.0, 0.0, 0.0);
               }

               $$7.setChanged();
               $$2.gameEvent($$4, GameEvent.BLOCK_CHANGE, $$3);
               return InteractionResult.SUCCESS;
            } else {
               return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$1.getBlockEntity($$2) instanceof DecoratedPotBlockEntity $$5) {
         $$1.playSound(null, $$2, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
         $$5.wobble(DecoratedPotBlockEntity.WobbleStyle.NEGATIVE);
         $$1.gameEvent($$3, GameEvent.BLOCK_CHANGE, $$2);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(HORIZONTAL_FACING, WATERLOGGED, CRACKED);
   }

   
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new DecoratedPotBlockEntity($$0, $$1);
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      Containers.updateNeighboursAfterDestroy($$0, $$1, $$2);
   }

   @Override
   protected List<ItemStack> getDrops(BlockState $$0, LootParams.Builder $$1) {
      BlockEntity $$2 = $$1.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      if ($$2 instanceof DecoratedPotBlockEntity $$3) {
         $$1.withDynamicDrop(SHERDS_DYNAMIC_DROP_ID, $$1x -> {
            for (Item $$2x : $$3.getDecorations().ordered()) {
               $$1x.accept($$2x.getDefaultInstance());
            }
         });
      }

      return super.getDrops($$0, $$1);
   }

   @Override
   public BlockState playerWillDestroy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Player $$3) {
      ItemStack $$4 = $$3.getMainHandItem();
      BlockState $$5 = $$2;
      if ($$4.is(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasTag($$4, EnchantmentTags.PREVENTS_DECORATED_POT_SHATTERING)) {
         $$5 = $$2.setValue(CRACKED, true);
         $$0.setBlock($$1, $$5, 260);
      }

      return super.playerWillDestroy($$0, $$1, $$5, $$3);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   @Override
   protected SoundType getSoundType(BlockState $$0) {
      return $$0.getValue(CRACKED) ? SoundType.DECORATED_POT_CRACKED : SoundType.DECORATED_POT;
   }

   @Override
   protected void onProjectileHit(net.minecraft.world.level.Level $$0, BlockState $$1, BlockHitResult $$2, Projectile $$3) {
      BlockPos $$4 = $$2.getBlockPos();
      if ($$0 instanceof ServerLevel $$5 && $$3.mayInteract($$5, $$4) && $$3.mayBreak($$5)) {
         $$0.setBlock($$4, $$1.setValue(CRACKED, true), 260);
         $$0.destroyBlock($$4, true, $$3);
      }
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      if ($$0.getBlockEntity($$1) instanceof DecoratedPotBlockEntity $$4) {
         PotDecorations $$5 = $$4.getDecorations();
         return DecoratedPotBlockEntity.createDecoratedPotItem($$5);
      } else {
         return super.getCloneItemStack($$0, $$1, $$2, $$3);
      }
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return AbstractContainerMenu.getRedstoneSignalFromBlockEntity($$1.getBlockEntity($$2));
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(HORIZONTAL_FACING, $$1.rotate($$0.getValue(HORIZONTAL_FACING)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(HORIZONTAL_FACING)));
   }
}
