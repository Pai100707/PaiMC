package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EnderChestBlockEntity extends BlockEntity implements LidBlockEntity {
   private final ChestLidController chestLidController = new ChestLidController();
   private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
      @Override
      protected void onOpen(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
         $$0.playSound(
            null,
            $$1.getX() + 0.5,
            $$1.getY() + 0.5,
            $$1.getZ() + 0.5,
            SoundEvents.ENDER_CHEST_OPEN,
            SoundSource.BLOCKS,
            0.5F,
            $$0.random.nextFloat() * 0.1F + 0.9F
         );
      }

      @Override
      protected void onClose(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
         $$0.playSound(
            null,
            $$1.getX() + 0.5,
            $$1.getY() + 0.5,
            $$1.getZ() + 0.5,
            SoundEvents.ENDER_CHEST_CLOSE,
            SoundSource.BLOCKS,
            0.5F,
            $$0.random.nextFloat() * 0.1F + 0.9F
         );
      }

      @Override
      protected void openerCountChanged(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, int $$3, int $$4) {
         $$0.blockEvent(EnderChestBlockEntity.this.worldPosition, Blocks.ENDER_CHEST, 1, $$4);
      }

      @Override
      public boolean isOwnContainer(Player $$0) {
         return $$0.getEnderChestInventory().isActiveChest(EnderChestBlockEntity.this);
      }
   };

   public EnderChestBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.ENDER_CHEST, $$0, $$1);
   }

   public static void lidAnimateTick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, EnderChestBlockEntity $$3) {
      $$3.chestLidController.tickLid();
   }

   @Override
   public boolean triggerEvent(int $$0, int $$1) {
      if ($$0 == 1) {
         this.chestLidController.shouldBeOpen($$1 > 0);
         return true;
      } else {
         return super.triggerEvent($$0, $$1);
      }
   }

   public void startOpen(ContainerUser $$0) {
      if (!this.remove && !$$0.getLivingEntity().isSpectator()) {
         this.openersCounter
            .incrementOpeners($$0.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState(), $$0.getContainerInteractionRange());
      }
   }

   public void stopOpen(ContainerUser $$0) {
      if (!this.remove && !$$0.getLivingEntity().isSpectator()) {
         this.openersCounter.decrementOpeners($$0.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState());
      }
   }

   public boolean stillValid(Player $$0) {
      return Container.stillValidBlockEntity(this, $$0);
   }

   public void recheckOpen() {
      if (!this.remove) {
         this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }
   }

   @Override
   public float getOpenNess(float $$0) {
      return this.chestLidController.getOpenness($$0);
   }
}
