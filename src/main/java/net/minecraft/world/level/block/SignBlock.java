package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignApplicator;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class SignBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final VoxelShape SHAPE = Block.column(8.0, 0.0, 16.0);
   private final WoodType type;

   protected SignBlock(WoodType $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.type = $$0;
   }

   @Override
   protected abstract MapCodec<? extends SignBlock> codec();

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
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   public boolean isPossibleToRespawnInThis(BlockState $$0) {
      return true;
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new SignBlockEntity($$0, $$1);
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if ($$2.getBlockEntity($$3) instanceof SignBlockEntity $$7) {
         SignApplicator $$10 = $$0.getItem() instanceof SignApplicator $$9 ? $$9 : null;
         boolean $$11 = $$10 != null && $$4.mayBuild();
         if ($$2 instanceof ServerLevel $$12) {
            if ($$11 && !$$7.isWaxed() && !this.otherPlayerIsEditingSign($$4, $$7)) {
               boolean $$14 = $$7.isFacingFrontText($$4);
               if ($$10.canApplyToSign($$7.getText($$14), $$4) && $$10.tryApplyToSign($$12, $$7, $$14, $$4)) {
                  $$7.executeClickCommandsIfPresent($$12, $$4, $$3, $$14);
                  $$4.awardStat(Stats.ITEM_USED.get($$0.getItem()));
                  $$12.gameEvent(GameEvent.BLOCK_CHANGE, $$7.getBlockPos(), GameEvent.Context.of($$4, $$7.getBlockState()));
                  $$0.consume(1, $$4);
                  return InteractionResult.SUCCESS;
               } else {
                  return InteractionResult.TRY_WITH_EMPTY_HAND;
               }
            } else {
               return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
         } else {
            return !$$11 && !$$7.isWaxed() ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$1.getBlockEntity($$2) instanceof SignBlockEntity $$5) {
         if ($$1 instanceof ServerLevel $$7) {
            boolean $$9 = $$5.isFacingFrontText($$3);
            boolean $$10 = $$5.executeClickCommandsIfPresent($$7, $$3, $$2, $$9);
            if ($$5.isWaxed()) {
               $$7.playSound(null, $$5.getBlockPos(), $$5.getSignInteractionFailedSoundEvent(), SoundSource.BLOCKS);
               return InteractionResult.SUCCESS_SERVER;
            } else if ($$10) {
               return InteractionResult.SUCCESS_SERVER;
            } else if (!this.otherPlayerIsEditingSign($$3, $$5) && $$3.mayBuild() && this.hasEditableText($$3, $$5, $$9)) {
               this.openTextEdit($$3, $$5, $$9);
               return InteractionResult.SUCCESS_SERVER;
            } else {
               return InteractionResult.PASS;
            }
         } else {
            Util.pauseInIde(new IllegalStateException("Expected to only call this on server"));
            return InteractionResult.CONSUME;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   private boolean hasEditableText(Player $$0, SignBlockEntity $$1, boolean $$2) {
      SignText $$3 = $$1.getText($$2);
      return Arrays.stream($$3.getMessages($$0.isTextFilteringEnabled()))
         .allMatch($$0x -> $$0x.equals(CommonComponents.EMPTY) || $$0x.getContents() instanceof PlainTextContents);
   }

   public abstract float getYRotationDegrees(BlockState var1);

   public Vec3 getSignHitboxCenterPosition(BlockState $$0) {
      return new Vec3(0.5, 0.5, 0.5);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   public WoodType type() {
      return this.type;
   }

   public static WoodType getWoodType(Block $$0) {
      WoodType $$1;
      if ($$0 instanceof SignBlock) {
         $$1 = ((SignBlock)$$0).type();
      } else {
         $$1 = WoodType.OAK;
      }

      return $$1;
   }

   public void openTextEdit(Player $$0, SignBlockEntity $$1, boolean $$2) {
      $$1.setAllowedPlayerEditor($$0.getUUID());
      $$0.openTextEdit($$1, $$2);
   }

   private boolean otherPlayerIsEditingSign(Player $$0, SignBlockEntity $$1) {
      UUID $$2 = $$1.getPlayerWhoMayEdit();
      return $$2 != null && !$$2.equals($$0.getUUID());
   }

   
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return createTickerHelper($$2, BlockEntityType.SIGN, SignBlockEntity::tick);
   }
}
