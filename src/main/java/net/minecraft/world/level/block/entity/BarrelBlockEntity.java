package net.minecraft.world.level.block.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BarrelBlockEntity extends RandomizableContainerBlockEntity {
   private static final Component DEFAULT_NAME = Component.translatable("container.barrel");
   private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
   private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
      @Override
      protected void onOpen(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
         BarrelBlockEntity.this.playSound($$2, SoundEvents.BARREL_OPEN);
         BarrelBlockEntity.this.updateBlockState($$2, true);
      }

      @Override
      protected void onClose(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
         BarrelBlockEntity.this.playSound($$2, SoundEvents.BARREL_CLOSE);
         BarrelBlockEntity.this.updateBlockState($$2, false);
      }

      @Override
      protected void openerCountChanged(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, int $$3, int $$4) {
      }

      @Override
      public boolean isOwnContainer(Player $$0) {
         if ($$0.containerMenu instanceof ChestMenu) {
            Container $$1 = ((ChestMenu)$$0.containerMenu).getContainer();
            return $$1 == BarrelBlockEntity.this;
         } else {
            return false;
         }
      }
   };

   public BarrelBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.BARREL, $$0, $$1);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      if (!this.trySaveLootTable($$0)) {
         ContainerHelper.saveAllItems($$0, this.items);
      }
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable($$0)) {
         ContainerHelper.loadAllItems($$0, this.items);
      }
   }

   public int getContainerSize() {
      return 27;
   }

   @Override
   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   @Override
   protected void setItems(NonNullList<ItemStack> $$0) {
      this.items = $$0;
   }

   @Override
   protected Component getDefaultName() {
      return DEFAULT_NAME;
   }

   @Override
   protected AbstractContainerMenu createMenu(int $$0, Inventory $$1) {
      return ChestMenu.threeRows($$0, $$1, this);
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

   public List<ContainerUser> getEntitiesWithContainerOpen() {
      return this.openersCounter.getEntitiesWithContainerOpen(this.getLevel(), this.getBlockPos());
   }

   public void recheckOpen() {
      if (!this.remove) {
         this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }
   }

   void updateBlockState(BlockState $$0, boolean $$1) {
      this.level.setBlock(this.getBlockPos(), $$0.setValue(BarrelBlock.OPEN, $$1), 3);
   }

   void playSound(BlockState $$0, SoundEvent $$1) {
      Vec3i $$2 = ((Direction)$$0.getValue(BarrelBlock.FACING)).getUnitVec3i();
      double $$3 = this.worldPosition.getX() + 0.5 + $$2.getX() / 2.0;
      double $$4 = this.worldPosition.getY() + 0.5 + $$2.getY() / 2.0;
      double $$5 = this.worldPosition.getZ() + 0.5 + $$2.getZ() / 2.0;
      this.level.playSound(null, $$3, $$4, $$5, $$1, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
   }
}
