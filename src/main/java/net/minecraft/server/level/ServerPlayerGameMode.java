package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Objects;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionResult.Fail;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.InteractionResult.TryEmptyHandInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerPlayerGameMode {
   private static final double FLIGHT_DISABLE_RANGE = 1.0;
   private static final Logger LOGGER = LogUtils.getLogger();
   protected ServerLevel level;
   protected final ServerPlayer player;
   private GameType gameModeForPlayer = GameType.DEFAULT_MODE;
   @Nullable
   private GameType previousGameModeForPlayer;
   private boolean isDestroyingBlock;
   private int destroyProgressStart;
   private BlockPos destroyPos = BlockPos.ZERO;
   private int gameTicks;
   private boolean hasDelayedDestroy;
   private BlockPos delayedDestroyPos = BlockPos.ZERO;
   private int delayedTickStart;
   private int lastSentState = -1;

   public ServerPlayerGameMode(ServerPlayer $$0) {
      this.player = $$0;
      this.level = $$0.level();
   }

   public boolean changeGameModeForPlayer(GameType $$0) {
      if ($$0 == this.gameModeForPlayer) {
         return false;
      } else {
         Abilities $$1 = this.player.getAbilities();
         this.setGameModeForPlayer($$0, this.gameModeForPlayer);
         if ($$1.flying && $$0 != GameType.SPECTATOR && this.isInRangeOfGround()) {
            $$1.flying = false;
         }

         this.player.onUpdateAbilities();
         this.level.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(Action.UPDATE_GAME_MODE, this.player));
         this.level.updateSleepingPlayerList();
         if ($$0 == GameType.CREATIVE) {
            this.player.resetCurrentImpulseContext();
         }

         return true;
      }
   }

   protected void setGameModeForPlayer(GameType $$0, @Nullable GameType $$1) {
      this.previousGameModeForPlayer = $$1;
      this.gameModeForPlayer = $$0;
      Abilities $$2 = this.player.getAbilities();
      $$0.updatePlayerAbilities($$2);
   }

   private boolean isInRangeOfGround() {
      List<VoxelShape> $$0 = Entity.collectAllColliders(this.player, this.level, this.player.getBoundingBox());
      return $$0.isEmpty() && this.player.getAvailableSpaceBelow(1.0) < 1.0;
   }

   public GameType getGameModeForPlayer() {
      return this.gameModeForPlayer;
   }

   @Nullable
   public GameType getPreviousGameModeForPlayer() {
      return this.previousGameModeForPlayer;
   }

   public boolean isSurvival() {
      return this.gameModeForPlayer.isSurvival();
   }

   public boolean isCreative() {
      return this.gameModeForPlayer.isCreative();
   }

   public void tick() {
      this.gameTicks++;
      if (this.hasDelayedDestroy) {
         BlockState $$0 = this.level.getBlockState(this.delayedDestroyPos);
         if ($$0.isAir()) {
            this.hasDelayedDestroy = false;
         } else {
            float $$1 = this.incrementDestroyProgress($$0, this.delayedDestroyPos, this.delayedTickStart);
            if ($$1 >= 1.0F) {
               this.hasDelayedDestroy = false;
               this.destroyBlock(this.delayedDestroyPos);
            }
         }
      } else if (this.isDestroyingBlock) {
         BlockState $$2 = this.level.getBlockState(this.destroyPos);
         if ($$2.isAir()) {
            this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
            this.lastSentState = -1;
            this.isDestroyingBlock = false;
         } else {
            this.incrementDestroyProgress($$2, this.destroyPos, this.destroyProgressStart);
         }
      }
   }

   private float incrementDestroyProgress(BlockState $$0, BlockPos $$1, int $$2) {
      int $$3 = this.gameTicks - $$2;
      float $$4 = $$0.getDestroyProgress(this.player, this.player.level(), $$1) * ($$3 + 1);
      int $$5 = (int)($$4 * 10.0F);
      if ($$5 != this.lastSentState) {
         this.level.destroyBlockProgress(this.player.getId(), $$1, $$5);
         this.lastSentState = $$5;
      }

      return $$4;
   }

   private void debugLogging(BlockPos $$0, boolean $$1, int $$2, String $$3) {
      if (SharedConstants.DEBUG_BLOCK_BREAK) {
         LOGGER.debug("Server ACK {} {} {} {}", new Object[]{$$2, $$0, $$1, $$3});
      }
   }

   public void handleBlockBreakAction(
      BlockPos $$0, net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action $$1, Direction $$2, int $$3, int $$4
   ) {
      if (!this.player.isWithinBlockInteractionRange($$0, 1.0)) {
         this.debugLogging($$0, false, $$4, "too far");
      } else if ($$0.getY() > $$3) {
         this.player.connection.send(new ClientboundBlockUpdatePacket($$0, this.level.getBlockState($$0)));
         this.debugLogging($$0, false, $$4, "too high");
      } else {
         if ($$1 == net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            if (!this.level.mayInteract(this.player, $$0)) {
               this.player.connection.send(new ClientboundBlockUpdatePacket($$0, this.level.getBlockState($$0)));
               this.debugLogging($$0, false, $$4, "may not interact");
               return;
            }

            if (this.player.getAbilities().instabuild) {
               this.destroyAndAck($$0, $$4, "creative destroy");
               return;
            }

            if (this.player.blockActionRestricted(this.level, $$0, this.gameModeForPlayer)) {
               this.player.connection.send(new ClientboundBlockUpdatePacket($$0, this.level.getBlockState($$0)));
               this.debugLogging($$0, false, $$4, "block action restricted");
               return;
            }

            this.destroyProgressStart = this.gameTicks;
            float $$5 = 1.0F;
            BlockState $$6 = this.level.getBlockState($$0);
            if (!$$6.isAir()) {
               EnchantmentHelper.onHitBlock(
                  this.level,
                  this.player.getMainHandItem(),
                  this.player,
                  this.player,
                  EquipmentSlot.MAINHAND,
                  Vec3.atCenterOf($$0),
                  $$6,
                  $$0x -> this.player.onEquippedItemBroken($$0x, EquipmentSlot.MAINHAND)
               );
               $$6.attack(this.level, $$0, this.player);
               $$5 = $$6.getDestroyProgress(this.player, this.player.level(), $$0);
            }

            if (!$$6.isAir() && $$5 >= 1.0F) {
               this.destroyAndAck($$0, $$4, "insta mine");
            } else {
               if (this.isDestroyingBlock) {
                  this.player.connection.send(new ClientboundBlockUpdatePacket(this.destroyPos, this.level.getBlockState(this.destroyPos)));
                  this.debugLogging($$0, false, $$4, "abort destroying since another started (client insta mine, server disagreed)");
               }

               this.isDestroyingBlock = true;
               this.destroyPos = $$0.immutable();
               int $$7 = (int)($$5 * 10.0F);
               this.level.destroyBlockProgress(this.player.getId(), $$0, $$7);
               this.debugLogging($$0, true, $$4, "actual start of destroying");
               this.lastSentState = $$7;
            }
         } else if ($$1 == net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            if ($$0.equals(this.destroyPos)) {
               int $$8 = this.gameTicks - this.destroyProgressStart;
               BlockState $$9 = this.level.getBlockState($$0);
               if (!$$9.isAir()) {
                  float $$10 = $$9.getDestroyProgress(this.player, this.player.level(), $$0) * ($$8 + 1);
                  if ($$10 >= 0.7F) {
                     this.isDestroyingBlock = false;
                     this.level.destroyBlockProgress(this.player.getId(), $$0, -1);
                     this.destroyAndAck($$0, $$4, "destroyed");
                     return;
                  }

                  if (!this.hasDelayedDestroy) {
                     this.isDestroyingBlock = false;
                     this.hasDelayedDestroy = true;
                     this.delayedDestroyPos = $$0;
                     this.delayedTickStart = this.destroyProgressStart;
                  }
               }
            }

            this.debugLogging($$0, true, $$4, "stopped destroying");
         } else if ($$1 == net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
            this.isDestroyingBlock = false;
            if (!Objects.equals(this.destroyPos, $$0)) {
               LOGGER.warn("Mismatch in destroy block pos: {} {}", this.destroyPos, $$0);
               this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
               this.debugLogging($$0, true, $$4, "aborted mismatched destroying");
            }

            this.level.destroyBlockProgress(this.player.getId(), $$0, -1);
            this.debugLogging($$0, true, $$4, "aborted destroying");
         }
      }
   }

   public void destroyAndAck(BlockPos $$0, int $$1, String $$2) {
      if (this.destroyBlock($$0)) {
         this.debugLogging($$0, true, $$1, $$2);
      } else {
         this.player.connection.send(new ClientboundBlockUpdatePacket($$0, this.level.getBlockState($$0)));
         this.debugLogging($$0, false, $$1, $$2);
      }
   }

   public boolean destroyBlock(BlockPos $$0) {
      BlockState $$1 = this.level.getBlockState($$0);
      if (!this.player.getMainHandItem().canDestroyBlock($$1, this.level, $$0, this.player)) {
         return false;
      } else {
         BlockEntity $$2 = this.level.getBlockEntity($$0);
         Block $$3 = $$1.getBlock();
         if ($$3 instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
            this.level.sendBlockUpdated($$0, $$1, $$1, 3);
            return false;
         } else if (this.player.blockActionRestricted(this.level, $$0, this.gameModeForPlayer)) {
            return false;
         } else {
            BlockState $$4 = $$3.playerWillDestroy(this.level, $$0, $$1, this.player);
            boolean $$5 = this.level.removeBlock($$0, false);
            if (SharedConstants.DEBUG_BLOCK_BREAK) {
               LOGGER.info("server broke {} {} -> {}", new Object[]{$$0, $$4, this.level.getBlockState($$0)});
            }

            if ($$5) {
               $$3.destroy(this.level, $$0, $$4);
            }

            if (this.player.preventsBlockDrops()) {
               return true;
            } else {
               ItemStack $$6 = this.player.getMainHandItem();
               ItemStack $$7 = $$6.copy();
               boolean $$8 = this.player.hasCorrectToolForDrops($$4);
               $$6.mineBlock(this.level, $$4, $$0, this.player);
               if ($$5 && $$8) {
                  $$3.playerDestroy(this.level, this.player, $$0, $$4, $$2, $$7);
               }

               return true;
            }
         }
      }
   }

   public InteractionResult useItem(ServerPlayer $$0, Level $$1, ItemStack $$2, InteractionHand $$3) {
      if (this.gameModeForPlayer == GameType.SPECTATOR) {
         return InteractionResult.PASS;
      } else if ($$0.getCooldowns().isOnCooldown($$2)) {
         return InteractionResult.PASS;
      } else {
         int $$4 = $$2.getCount();
         int $$5 = $$2.getDamageValue();
         InteractionResult $$6 = $$2.use($$1, $$0, $$3);
         ItemStack $$8;
         if ($$6 instanceof Success $$7) {
            $$8 = Objects.requireNonNullElse($$7.heldItemTransformedTo(), $$0.getItemInHand($$3));
         } else {
            $$8 = $$0.getItemInHand($$3);
         }

         if ($$8 == $$2 && $$8.getCount() == $$4 && $$8.getUseDuration($$0) <= 0 && $$8.getDamageValue() == $$5) {
            return $$6;
         } else if ($$6 instanceof Fail && $$8.getUseDuration($$0) > 0 && !$$0.isUsingItem()) {
            return $$6;
         } else {
            if ($$2 != $$8) {
               $$0.setItemInHand($$3, $$8);
            }

            if ($$8.isEmpty()) {
               $$0.setItemInHand($$3, ItemStack.EMPTY);
            }

            if (!$$0.isUsingItem()) {
               $$0.inventoryMenu.sendAllDataToRemote();
            }

            return $$6;
         }
      }
   }

   public InteractionResult useItemOn(ServerPlayer $$0, Level $$1, ItemStack $$2, InteractionHand $$3, BlockHitResult $$4) {
      BlockPos $$5 = $$4.getBlockPos();
      BlockState $$6 = $$1.getBlockState($$5);
      if (!$$6.getBlock().isEnabled($$1.enabledFeatures())) {
         return InteractionResult.FAIL;
      } else if (this.gameModeForPlayer == GameType.SPECTATOR) {
         MenuProvider $$7 = $$6.getMenuProvider($$1, $$5);
         if ($$7 != null) {
            $$0.openMenu($$7);
            return InteractionResult.CONSUME;
         } else {
            return InteractionResult.PASS;
         }
      } else {
         boolean $$8 = !$$0.getMainHandItem().isEmpty() || !$$0.getOffhandItem().isEmpty();
         boolean $$9 = $$0.isSecondaryUseActive() && $$8;
         ItemStack $$10 = $$2.copy();
         if (!$$9) {
            InteractionResult $$11 = $$6.useItemOn($$0.getItemInHand($$3), $$1, $$0, $$3, $$4);
            if ($$11.consumesAction()) {
               CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger($$0, $$5, $$10);
               return $$11;
            }

            if ($$11 instanceof TryEmptyHandInteraction && $$3 == InteractionHand.MAIN_HAND) {
               InteractionResult $$12 = $$6.useWithoutItem($$1, $$0, $$4);
               if ($$12.consumesAction()) {
                  CriteriaTriggers.DEFAULT_BLOCK_USE.trigger($$0, $$5);
                  return $$12;
               }
            }
         }

         if (!$$2.isEmpty() && !$$0.getCooldowns().isOnCooldown($$2)) {
            UseOnContext $$13 = new UseOnContext($$0, $$3, $$4);
            InteractionResult $$15;
            if ($$0.hasInfiniteMaterials()) {
               int $$14 = $$2.getCount();
               $$15 = $$2.useOn($$13);
               $$2.setCount($$14);
            } else {
               $$15 = $$2.useOn($$13);
            }

            if ($$15.consumesAction()) {
               CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger($$0, $$5, $$10);
            }

            return $$15;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public void setLevel(ServerLevel $$0) {
      this.level = $$0;
   }
}
