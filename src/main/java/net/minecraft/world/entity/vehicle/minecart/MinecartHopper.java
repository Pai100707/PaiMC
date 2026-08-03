package net.minecraft.world.entity.vehicle.minecart;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MinecartHopper extends AbstractMinecartContainer implements Hopper {
   private static final boolean DEFAULT_ENABLED = true;
   private boolean enabled = true;
   private boolean consumedItemThisFrame = false;

   public MinecartHopper(net.minecraft.world.entity.EntityType<? extends MinecartHopper> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   public BlockState getDefaultDisplayBlockState() {
      return Blocks.HOPPER.defaultBlockState();
   }

   @Override
   public int getDefaultDisplayOffset() {
      return 1;
   }

   public int getContainerSize() {
      return 5;
   }

   @Override
   public void activateMinecart(ServerLevel $$0, int $$1, int $$2, int $$3, boolean $$4) {
      boolean $$5 = !$$4;
      if ($$5 != this.isEnabled()) {
         this.setEnabled($$5);
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean $$0) {
      this.enabled = $$0;
   }

   public double getLevelX() {
      return this.getX();
   }

   public double getLevelY() {
      return this.getY() + 0.5;
   }

   public double getLevelZ() {
      return this.getZ();
   }

   public boolean isGridAligned() {
      return false;
   }

   @Override
   public void tick() {
      this.consumedItemThisFrame = false;
      super.tick();
      this.tryConsumeItems();
   }

   @Override
   protected double makeStepAlongTrack(BlockPos $$0, RailShape $$1, double $$2) {
      double $$3 = super.makeStepAlongTrack($$0, $$1, $$2);
      this.tryConsumeItems();
      return $$3;
   }

   private void tryConsumeItems() {
      if (!this.level().isClientSide() && this.isAlive() && this.isEnabled() && !this.consumedItemThisFrame && this.suckInItems()) {
         this.consumedItemThisFrame = true;
         this.setChanged();
      }
   }

   public boolean suckInItems() {
      if (HopperBlockEntity.suckInItems(this.level(), this)) {
         return true;
      } else {
         for (ItemEntity $$1 : this.level()
            .getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.25, 0.0, 0.25), net.minecraft.world.entity.EntitySelector.ENTITY_STILL_ALIVE)) {
            if (HopperBlockEntity.addItem(this, $$1)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   protected Item getDropItem() {
      return Items.HOPPER_MINECART;
   }

   @Override
   public ItemStack getPickResult() {
      return new ItemStack(Items.HOPPER_MINECART);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putBoolean("Enabled", this.enabled);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.enabled = $$0.getBooleanOr("Enabled", true);
   }

   @Override
   public AbstractContainerMenu createMenu(int $$0, Inventory $$1) {
      return new HopperMenu($$0, $$1, this);
   }
}
