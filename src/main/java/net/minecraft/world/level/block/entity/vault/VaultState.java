package net.minecraft.world.level.block.entity.vault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public enum VaultState implements StringRepresentable {
   INACTIVE("inactive", VaultState.LightLevel.HALF_LIT) {
      @Override
      protected void onEnter(ServerLevel $$0, BlockPos $$1, VaultConfig $$2, VaultSharedData $$3, boolean $$4) {
         $$3.setDisplayItem(ItemStack.EMPTY);
         $$0.levelEvent(3016, $$1, $$4 ? 1 : 0);
      }
   },
   ACTIVE("active", VaultState.LightLevel.LIT) {
      @Override
      protected void onEnter(ServerLevel $$0, BlockPos $$1, VaultConfig $$2, VaultSharedData $$3, boolean $$4) {
         if (!$$3.hasDisplayItem()) {
            VaultBlockEntity.Server.cycleDisplayItemFromLootTable($$0, this, $$2, $$3, $$1);
         }

         $$0.levelEvent(3015, $$1, $$4 ? 1 : 0);
      }
   },
   UNLOCKING("unlocking", VaultState.LightLevel.LIT) {
      @Override
      protected void onEnter(ServerLevel $$0, BlockPos $$1, VaultConfig $$2, VaultSharedData $$3, boolean $$4) {
         $$0.playSound(null, $$1, SoundEvents.VAULT_INSERT_ITEM, SoundSource.BLOCKS);
      }
   },
   EJECTING("ejecting", VaultState.LightLevel.LIT) {
      @Override
      protected void onEnter(ServerLevel $$0, BlockPos $$1, VaultConfig $$2, VaultSharedData $$3, boolean $$4) {
         $$0.playSound(null, $$1, SoundEvents.VAULT_OPEN_SHUTTER, SoundSource.BLOCKS);
      }

      @Override
      protected void onExit(ServerLevel $$0, BlockPos $$1, VaultConfig $$2, VaultSharedData $$3) {
         $$0.playSound(null, $$1, SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS);
      }
   };

   private static final int UPDATE_CONNECTED_PLAYERS_TICK_RATE = 20;
   private static final int DELAY_BETWEEN_EJECTIONS_TICKS = 20;
   private static final int DELAY_AFTER_LAST_EJECTION_TICKS = 20;
   private static final int DELAY_BEFORE_FIRST_EJECTION_TICKS = 20;
   private final String stateName;
   private final VaultState.LightLevel lightLevel;

   VaultState(final String $$0, final VaultState.LightLevel $$1) {
      this.stateName = $$0;
      this.lightLevel = $$1;
   }

   public String getSerializedName() {
      return this.stateName;
   }

   public int lightLevel() {
      return this.lightLevel.value;
   }

   public VaultState tickAndGetNext(ServerLevel $$0, BlockPos $$1, VaultConfig $$2, VaultServerData $$3, VaultSharedData $$4) {
      return switch (this) {
         case INACTIVE -> updateStateForConnectedPlayers($$0, $$1, $$2, $$3, $$4, $$2.activationRange());
         case ACTIVE -> updateStateForConnectedPlayers($$0, $$1, $$2, $$3, $$4, $$2.deactivationRange());
         case UNLOCKING -> {
            $$3.pauseStateUpdatingUntil($$0.getGameTime() + 20L);
            yield EJECTING;
         }
         case EJECTING -> {
            if ($$3.getItemsToEject().isEmpty()) {
               $$3.markEjectionFinished();
               yield updateStateForConnectedPlayers($$0, $$1, $$2, $$3, $$4, $$2.deactivationRange());
            } else {
               float $$5 = $$3.ejectionProgress();
               this.ejectResultItem($$0, $$1, $$3.popNextItemToEject(), $$5);
               $$4.setDisplayItem($$3.getNextItemToEject());
               boolean $$6 = $$3.getItemsToEject().isEmpty();
               int $$7 = $$6 ? 20 : 20;
               $$3.pauseStateUpdatingUntil($$0.getGameTime() + $$7);
               yield EJECTING;
            }
         }
      };
   }

   private static VaultState updateStateForConnectedPlayers(
      ServerLevel $$0, BlockPos $$1, VaultConfig $$2, VaultServerData $$3, VaultSharedData $$4, double $$5
   ) {
      $$4.updateConnectedPlayersWithinRange($$0, $$1, $$3, $$2, $$5);
      $$3.pauseStateUpdatingUntil($$0.getGameTime() + 20L);
      return $$4.hasConnectedPlayers() ? ACTIVE : INACTIVE;
   }

   public void onTransition(ServerLevel $$0, BlockPos $$1, VaultState $$2, VaultConfig $$3, VaultSharedData $$4, boolean $$5) {
      this.onExit($$0, $$1, $$3, $$4);
      $$2.onEnter($$0, $$1, $$3, $$4, $$5);
   }

   protected void onEnter(ServerLevel $$0, BlockPos $$1, VaultConfig $$2, VaultSharedData $$3, boolean $$4) {
   }

   protected void onExit(ServerLevel $$0, BlockPos $$1, VaultConfig $$2, VaultSharedData $$3) {
   }

   private void ejectResultItem(ServerLevel $$0, BlockPos $$1, ItemStack $$2, float $$3) {
      DefaultDispenseItemBehavior.spawnItem($$0, $$2, 2, Direction.UP, Vec3.atBottomCenterOf($$1).relative(Direction.UP, 1.2));
      $$0.levelEvent(3017, $$1, 0);
      $$0.playSound(null, $$1, SoundEvents.VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F + 0.4F * $$3);
   }

   static enum LightLevel {
      HALF_LIT(6),
      LIT(12);

      final int value;

      private LightLevel(final int $$0) {
         this.value = $$0;
      }
   }
}
