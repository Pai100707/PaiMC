package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class BaseContainerBlockEntity extends BlockEntity implements Container, MenuProvider, Nameable {
   private LockCode lockKey = LockCode.NO_LOCK;
   
   private Component name;

   protected BaseContainerBlockEntity(BlockEntityType<?> $$0, BlockPos $$1, BlockState $$2) {
      super($$0, $$1, $$2);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.lockKey = LockCode.fromTag($$0);
      this.name = parseCustomNameSafe($$0, "CustomName");
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      this.lockKey.addToTag($$0);
      $$0.storeNullable("CustomName", ComponentSerialization.CODEC, this.name);
   }

   public Component getName() {
      return this.name != null ? this.name : this.getDefaultName();
   }

   public Component getDisplayName() {
      return this.getName();
   }

   
   public Component getCustomName() {
      return this.name;
   }

   protected abstract Component getDefaultName();

   public boolean canOpen(Player $$0) {
      return this.lockKey.canUnlock($$0);
   }

   public static void sendChestLockedNotifications(Vec3 $$0, Player $$1, Component $$2) {
      net.minecraft.world.level.Level $$3 = $$1.level();
      $$1.displayClientMessage(Component.translatable("container.isLocked", new Object[]{$$2}), true);
      if (!$$3.isClientSide()) {
         $$3.playSound(null, $$0.x(), $$0.y(), $$0.z(), SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   public boolean isLocked() {
      return !this.lockKey.equals(LockCode.NO_LOCK);
   }

   protected abstract NonNullList<ItemStack> getItems();

   protected abstract void setItems(NonNullList<ItemStack> var1);

   public boolean isEmpty() {
      for (ItemStack $$0 : this.getItems()) {
         if (!$$0.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int $$0) {
      return (ItemStack)this.getItems().get($$0);
   }

   public ItemStack removeItem(int $$0, int $$1) {
      ItemStack $$2 = ContainerHelper.removeItem(this.getItems(), $$0, $$1);
      if (!$$2.isEmpty()) {
         this.setChanged();
      }

      return $$2;
   }

   public ItemStack removeItemNoUpdate(int $$0) {
      return ContainerHelper.takeItem(this.getItems(), $$0);
   }

   public void setItem(int $$0, ItemStack $$1) {
      this.getItems().set($$0, $$1);
      $$1.limitSize(this.getMaxStackSize($$1));
      this.setChanged();
   }

   public boolean stillValid(Player $$0) {
      return Container.stillValidBlockEntity(this, $$0);
   }

   public void clearContent() {
      this.getItems().clear();
   }

   
   public AbstractContainerMenu createMenu(int $$0, Inventory $$1, Player $$2) {
      if (this.canOpen($$2)) {
         return this.createMenu($$0, $$1);
      } else {
         sendChestLockedNotifications(this.getBlockPos().getCenter(), $$2, this.getDisplayName());
         return null;
      }
   }

   protected abstract AbstractContainerMenu createMenu(int var1, Inventory var2);

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      super.applyImplicitComponents($$0);
      this.name = (Component)$$0.get(DataComponents.CUSTOM_NAME);
      this.lockKey = (LockCode)$$0.getOrDefault(DataComponents.LOCK, LockCode.NO_LOCK);
      ((ItemContainerContents)$$0.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)).copyInto(this.getItems());
   }

   @Override
   protected void collectImplicitComponents(Builder $$0) {
      super.collectImplicitComponents($$0);
      $$0.set(DataComponents.CUSTOM_NAME, this.name);
      if (this.isLocked()) {
         $$0.set(DataComponents.LOCK, this.lockKey);
      }

      $$0.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
   }

   @Override
   public void removeComponentsFromTag(ValueOutput $$0) {
      $$0.discard("CustomName");
      $$0.discard("lock");
      $$0.discard("Items");
   }
}
