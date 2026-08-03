package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SpawnerBlockEntity extends BlockEntity implements net.minecraft.world.level.Spawner {
   private final net.minecraft.world.level.BaseSpawner spawner = new net.minecraft.world.level.BaseSpawner() {
      @Override
      public void broadcastEvent(net.minecraft.world.level.Level $$0, BlockPos $$1, int $$2) {
         $$0.blockEvent($$1, Blocks.SPAWNER, $$2, 0);
      }

      @Override
      public void setNextSpawnData(@Nullable net.minecraft.world.level.Level $$0, BlockPos $$1, net.minecraft.world.level.SpawnData $$2) {
         super.setNextSpawnData($$0, $$1, $$2);
         if ($$0 != null) {
            BlockState $$3 = $$0.getBlockState($$1);
            $$0.sendBlockUpdated($$1, $$3, $$3, 260);
         }
      }
   };

   public SpawnerBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.MOB_SPAWNER, $$0, $$1);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.spawner.load(this.level, this.worldPosition, $$0);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      this.spawner.save($$0);
   }

   public static void clientTick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, SpawnerBlockEntity $$3) {
      $$3.spawner.clientTick($$0, $$1);
   }

   public static void serverTick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, SpawnerBlockEntity $$3) {
      $$3.spawner.serverTick((ServerLevel)$$0, $$1);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      CompoundTag $$1 = this.saveCustomOnly($$0);
      $$1.remove("SpawnPotentials");
      return $$1;
   }

   @Override
   public boolean triggerEvent(int $$0, int $$1) {
      return this.spawner.onEventTriggered(this.level, $$0) ? true : super.triggerEvent($$0, $$1);
   }

   @Override
   public void setEntityId(EntityType<?> $$0, RandomSource $$1) {
      this.spawner.setEntityId($$0, this.level, $$1, this.worldPosition);
      this.setChanged();
   }

   public net.minecraft.world.level.BaseSpawner getSpawner() {
      return this.spawner;
   }
}
