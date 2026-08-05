package net.minecraft.world.level.block.entity.vault;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class VaultBlockEntity extends BlockEntity {
   private final VaultServerData serverData = new VaultServerData();
   private final VaultSharedData sharedData = new VaultSharedData();
   private final VaultClientData clientData = new VaultClientData();
   private VaultConfig config = VaultConfig.DEFAULT;

   public VaultBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.VAULT, $$0, $$1);
   }

   
   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      return (CompoundTag)Util.make(
         new CompoundTag(), $$1 -> $$1.store("shared_data", VaultSharedData.CODEC, $$0.createSerializationContext(NbtOps.INSTANCE), this.sharedData)
      );
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      $$0.store("config", VaultConfig.CODEC, this.config);
      $$0.store("shared_data", VaultSharedData.CODEC, this.sharedData);
      $$0.store("server_data", VaultServerData.CODEC, this.serverData);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      $$0.<VaultServerData>read("server_data", VaultServerData.CODEC).ifPresent(this.serverData::set);
      this.config = $$0.<VaultConfig>read("config", VaultConfig.CODEC).orElse(VaultConfig.DEFAULT);
      $$0.<VaultSharedData>read("shared_data", VaultSharedData.CODEC).ifPresent(this.sharedData::set);
   }

   
   public VaultServerData getServerData() {
      return this.level != null && !this.level.isClientSide() ? this.serverData : null;
   }

   public VaultSharedData getSharedData() {
      return this.sharedData;
   }

   public VaultClientData getClientData() {
      return this.clientData;
   }

   public VaultConfig getConfig() {
      return this.config;
   }

   @VisibleForTesting
   public void setConfig(VaultConfig $$0) {
      this.config = $$0;
   }

   public static final class Client {
      private static final int PARTICLE_TICK_RATE = 20;
      private static final float IDLE_PARTICLE_CHANCE = 0.5F;
      private static final float AMBIENT_SOUND_CHANCE = 0.02F;
      private static final int ACTIVATION_PARTICLE_COUNT = 20;
      private static final int DEACTIVATION_PARTICLE_COUNT = 20;

      public static void tick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, VaultClientData $$3, VaultSharedData $$4) {
         $$3.updateDisplayItemSpin();
         if ($$0.getGameTime() % 20L == 0L) {
            emitConnectionParticlesForNearbyPlayers($$0, $$1, $$2, $$4);
         }

         emitIdleParticles($$0, $$1, $$4, $$2.getValue(VaultBlock.OMINOUS) ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME);
         playIdleSounds($$0, $$1, $$4);
      }

      public static void emitActivationParticles(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, VaultSharedData $$3, ParticleOptions $$4) {
         emitConnectionParticlesForNearbyPlayers($$0, $$1, $$2, $$3);
         RandomSource $$5 = $$0.random;

         for (int $$6 = 0; $$6 < 20; $$6++) {
            Vec3 $$7 = randomPosInsideCage($$1, $$5);
            $$0.addParticle(ParticleTypes.SMOKE, $$7.x(), $$7.y(), $$7.z(), 0.0, 0.0, 0.0);
            $$0.addParticle($$4, $$7.x(), $$7.y(), $$7.z(), 0.0, 0.0, 0.0);
         }
      }

      public static void emitDeactivationParticles(net.minecraft.world.level.Level $$0, BlockPos $$1, ParticleOptions $$2) {
         RandomSource $$3 = $$0.random;

         for (int $$4 = 0; $$4 < 20; $$4++) {
            Vec3 $$5 = randomPosCenterOfCage($$1, $$3);
            Vec3 $$6 = new Vec3($$3.nextGaussian() * 0.02, $$3.nextGaussian() * 0.02, $$3.nextGaussian() * 0.02);
            $$0.addParticle($$2, $$5.x(), $$5.y(), $$5.z(), $$6.x(), $$6.y(), $$6.z());
         }
      }

      private static void emitIdleParticles(net.minecraft.world.level.Level $$0, BlockPos $$1, VaultSharedData $$2, ParticleOptions $$3) {
         RandomSource $$4 = $$0.getRandom();
         if ($$4.nextFloat() <= 0.5F) {
            Vec3 $$5 = randomPosInsideCage($$1, $$4);
            $$0.addParticle(ParticleTypes.SMOKE, $$5.x(), $$5.y(), $$5.z(), 0.0, 0.0, 0.0);
            if (shouldDisplayActiveEffects($$2)) {
               $$0.addParticle($$3, $$5.x(), $$5.y(), $$5.z(), 0.0, 0.0, 0.0);
            }
         }
      }

      private static void emitConnectionParticlesForPlayer(net.minecraft.world.level.Level $$0, Vec3 $$1, Player $$2) {
         RandomSource $$3 = $$0.random;
         Vec3 $$4 = $$1.vectorTo($$2.position().add(0.0, $$2.getBbHeight() / 2.0F, 0.0));
         int $$5 = Mth.nextInt($$3, 2, 5);

         for (int $$6 = 0; $$6 < $$5; $$6++) {
            Vec3 $$7 = $$4.offsetRandom($$3, 1.0F);
            $$0.addParticle(ParticleTypes.VAULT_CONNECTION, $$1.x(), $$1.y(), $$1.z(), $$7.x(), $$7.y(), $$7.z());
         }
      }

      private static void emitConnectionParticlesForNearbyPlayers(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, VaultSharedData $$3) {
         Set<UUID> $$4 = $$3.getConnectedPlayers();
         if (!$$4.isEmpty()) {
            Vec3 $$5 = keyholePos($$1, $$2.getValue(VaultBlock.FACING));

            for (UUID $$6 : $$4) {
               Player $$7 = $$0.getPlayerByUUID($$6);
               if ($$7 != null && isWithinConnectionRange($$1, $$3, $$7)) {
                  emitConnectionParticlesForPlayer($$0, $$5, $$7);
               }
            }
         }
      }

      private static boolean isWithinConnectionRange(BlockPos $$0, VaultSharedData $$1, Player $$2) {
         return $$2.blockPosition().distSqr($$0) <= Mth.square($$1.connectedParticlesRange());
      }

      private static void playIdleSounds(net.minecraft.world.level.Level $$0, BlockPos $$1, VaultSharedData $$2) {
         if (shouldDisplayActiveEffects($$2)) {
            RandomSource $$3 = $$0.getRandom();
            if ($$3.nextFloat() <= 0.02F) {
               $$0.playLocalSound($$1, SoundEvents.VAULT_AMBIENT, SoundSource.BLOCKS, $$3.nextFloat() * 0.25F + 0.75F, $$3.nextFloat() + 0.5F, false);
            }
         }
      }

      public static boolean shouldDisplayActiveEffects(VaultSharedData $$0) {
         return $$0.hasDisplayItem();
      }

      private static Vec3 randomPosCenterOfCage(BlockPos $$0, RandomSource $$1) {
         return Vec3.atLowerCornerOf($$0).add(Mth.nextDouble($$1, 0.4, 0.6), Mth.nextDouble($$1, 0.4, 0.6), Mth.nextDouble($$1, 0.4, 0.6));
      }

      private static Vec3 randomPosInsideCage(BlockPos $$0, RandomSource $$1) {
         return Vec3.atLowerCornerOf($$0).add(Mth.nextDouble($$1, 0.1, 0.9), Mth.nextDouble($$1, 0.25, 0.75), Mth.nextDouble($$1, 0.1, 0.9));
      }

      private static Vec3 keyholePos(BlockPos $$0, Direction $$1) {
         return Vec3.atBottomCenterOf($$0).add($$1.getStepX() * 0.5, 1.75, $$1.getStepZ() * 0.5);
      }
   }

   public static final class Server {
      private static final int UNLOCKING_DELAY_TICKS = 14;
      private static final int DISPLAY_CYCLE_TICK_RATE = 20;
      private static final int INSERT_FAIL_SOUND_BUFFER_TICKS = 15;

      public static void tick(ServerLevel $$0, BlockPos $$1, BlockState $$2, VaultConfig $$3, VaultServerData $$4, VaultSharedData $$5) {
         VaultState $$6 = $$2.getValue(VaultBlock.STATE);
         if (shouldCycleDisplayItem($$0.getGameTime(), $$6)) {
            cycleDisplayItemFromLootTable($$0, $$6, $$3, $$5, $$1);
         }

         BlockState $$7 = $$2;
         if ($$0.getGameTime() >= $$4.stateUpdatingResumesAt()) {
            $$7 = $$2.setValue(VaultBlock.STATE, $$6.tickAndGetNext($$0, $$1, $$3, $$4, $$5));
            if ($$2 != $$7) {
               setVaultState($$0, $$1, $$2, $$7, $$3, $$5);
            }
         }

         if ($$4.isDirty || $$5.isDirty) {
            VaultBlockEntity.setChanged($$0, $$1, $$2);
            if ($$5.isDirty) {
               $$0.sendBlockUpdated($$1, $$2, $$7, 2);
            }

            $$4.isDirty = false;
            $$5.isDirty = false;
         }
      }

      public static void tryInsertKey(
         ServerLevel $$0, BlockPos $$1, BlockState $$2, VaultConfig $$3, VaultServerData $$4, VaultSharedData $$5, Player $$6, ItemStack $$7
      ) {
         VaultState $$8 = $$2.getValue(VaultBlock.STATE);
         if (canEjectReward($$3, $$8)) {
            if (!isValidToInsert($$3, $$7)) {
               playInsertFailSound($$0, $$4, $$1, SoundEvents.VAULT_INSERT_ITEM_FAIL);
            } else if ($$4.hasRewardedPlayer($$6)) {
               playInsertFailSound($$0, $$4, $$1, SoundEvents.VAULT_REJECT_REWARDED_PLAYER);
            } else {
               List<ItemStack> $$9 = resolveItemsToEject($$0, $$3, $$1, $$6, $$7);
               if (!$$9.isEmpty()) {
                  $$6.awardStat(Stats.ITEM_USED.get($$7.getItem()));
                  $$7.consume($$3.keyItem().getCount(), $$6);
                  unlock($$0, $$2, $$1, $$3, $$4, $$5, $$9);
                  $$4.addToRewardedPlayers($$6);
                  $$5.updateConnectedPlayersWithinRange($$0, $$1, $$4, $$3, $$3.deactivationRange());
               }
            }
         }
      }

      static void setVaultState(ServerLevel $$0, BlockPos $$1, BlockState $$2, BlockState $$3, VaultConfig $$4, VaultSharedData $$5) {
         VaultState $$6 = $$2.getValue(VaultBlock.STATE);
         VaultState $$7 = $$3.getValue(VaultBlock.STATE);
         $$0.setBlock($$1, $$3, 3);
         $$6.onTransition($$0, $$1, $$7, $$4, $$5, $$3.getValue(VaultBlock.OMINOUS));
      }

      static void cycleDisplayItemFromLootTable(ServerLevel $$0, VaultState $$1, VaultConfig $$2, VaultSharedData $$3, BlockPos $$4) {
         if (!canEjectReward($$2, $$1)) {
            $$3.setDisplayItem(ItemStack.EMPTY);
         } else {
            ItemStack $$5 = getRandomDisplayItemFromLootTable($$0, $$4, $$2.overrideLootTableToDisplay().orElse($$2.lootTable()));
            $$3.setDisplayItem($$5);
         }
      }

      private static ItemStack getRandomDisplayItemFromLootTable(ServerLevel $$0, BlockPos $$1, ResourceKey<LootTable> $$2) {
         LootTable $$3 = $$0.getServer().reloadableRegistries().getLootTable($$2);
         LootParams $$4 = new LootParams.Builder($$0).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf($$1)).create(LootContextParamSets.VAULT);
         List<ItemStack> $$5 = $$3.getRandomItems($$4, $$0.getRandom());
         return $$5.isEmpty() ? ItemStack.EMPTY : (ItemStack)Util.getRandom($$5, $$0.getRandom());
      }

      private static void unlock(ServerLevel $$0, BlockState $$1, BlockPos $$2, VaultConfig $$3, VaultServerData $$4, VaultSharedData $$5, List<ItemStack> $$6) {
         $$4.setItemsToEject($$6);
         $$5.setDisplayItem($$4.getNextItemToEject());
         $$4.pauseStateUpdatingUntil($$0.getGameTime() + 14L);
         setVaultState($$0, $$2, $$1, $$1.setValue(VaultBlock.STATE, VaultState.UNLOCKING), $$3, $$5);
      }

      private static List<ItemStack> resolveItemsToEject(ServerLevel $$0, VaultConfig $$1, BlockPos $$2, Player $$3, ItemStack $$4) {
         LootTable $$5 = $$0.getServer().reloadableRegistries().getLootTable($$1.lootTable());
         LootParams $$6 = new LootParams.Builder($$0)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf($$2))
            .withLuck($$3.getLuck())
            .withParameter(LootContextParams.THIS_ENTITY, $$3)
            .withParameter(LootContextParams.TOOL, $$4)
            .create(LootContextParamSets.VAULT);
         return $$5.getRandomItems($$6);
      }

      private static boolean canEjectReward(VaultConfig $$0, VaultState $$1) {
         return !$$0.keyItem().isEmpty() && $$1 != VaultState.INACTIVE;
      }

      private static boolean isValidToInsert(VaultConfig $$0, ItemStack $$1) {
         return ItemStack.isSameItemSameComponents($$1, $$0.keyItem()) && $$1.getCount() >= $$0.keyItem().getCount();
      }

      private static boolean shouldCycleDisplayItem(long $$0, VaultState $$1) {
         return $$0 % 20L == 0L && $$1 == VaultState.ACTIVE;
      }

      private static void playInsertFailSound(ServerLevel $$0, VaultServerData $$1, BlockPos $$2, SoundEvent $$3) {
         if ($$0.getGameTime() >= $$1.getLastInsertFailTimestamp() + 15L) {
            $$0.playSound(null, $$2, $$3, SoundSource.BLOCKS);
            $$1.setLastInsertFailTimestamp($$0.getGameTime());
         }
      }
   }
}
