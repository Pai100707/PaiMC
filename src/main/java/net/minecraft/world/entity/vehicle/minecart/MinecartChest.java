package net.minecraft.world.entity.vehicle.minecart;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;

public class MinecartChest extends AbstractMinecartContainer {
   public MinecartChest(net.minecraft.world.entity.EntityType<? extends MinecartChest> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected Item getDropItem() {
      return Items.CHEST_MINECART;
   }

   @Override
   public ItemStack getPickResult() {
      return new ItemStack(Items.CHEST_MINECART);
   }

   public int getContainerSize() {
      return 27;
   }

   @Override
   public BlockState getDefaultDisplayBlockState() {
      return (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
   }

   @Override
   public int getDefaultDisplayOffset() {
      return 8;
   }

   @Override
   public AbstractContainerMenu createMenu(int $$0, Inventory $$1) {
      return ChestMenu.threeRows($$0, $$1, this);
   }

   public void stopOpen(net.minecraft.world.entity.ContainerUser $$0) {
      this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), Context.of($$0.getLivingEntity()));
   }

   @Override
   public InteractionResult interact(Player $$0, InteractionHand $$1) {
      InteractionResult $$2 = this.interactWithContainerVehicle($$0);
      if ($$2.consumesAction() && $$0.level() instanceof ServerLevel $$3) {
         this.gameEvent(GameEvent.CONTAINER_OPEN, $$0);
         PiglinAi.angerNearbyPiglins($$3, $$0, true);
      }

      return $$2;
   }
}
