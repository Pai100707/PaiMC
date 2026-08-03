package net.minecraft.world.level.block.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {
   private static final int EVENT_SET_OPEN_COUNT = 1;
   private static final Component DEFAULT_NAME = Component.translatable("container.chest");
   private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
   private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
      @Override
      protected void onOpen(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
         if ($$2.getBlock() instanceof ChestBlock $$3) {
            ChestBlockEntity.playSound($$0, $$1, $$2, $$3.getOpenChestSound());
         }
      }

      @Override
      protected void onClose(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
         if ($$2.getBlock() instanceof ChestBlock $$3) {
            ChestBlockEntity.playSound($$0, $$1, $$2, $$3.getCloseChestSound());
         }
      }

      @Override
      protected void openerCountChanged(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, int $$3, int $$4) {
         ChestBlockEntity.this.signalOpenCount($$0, $$1, $$2, $$3, $$4);
      }

      @Override
      public boolean isOwnContainer(Player $$0) {
         if (!($$0.containerMenu instanceof ChestMenu)) {
            return false;
         } else {
            Container $$1 = ((ChestMenu)$$0.containerMenu).getContainer();
            return $$1 == ChestBlockEntity.this || $$1 instanceof CompoundContainer && ((CompoundContainer)$$1).contains(ChestBlockEntity.this);
         }
      }
   };
   private final ChestLidController chestLidController = new ChestLidController();

   protected ChestBlockEntity(BlockEntityType<?> $$0, BlockPos $$1, BlockState $$2) {
      super($$0, $$1, $$2);
   }

   public ChestBlockEntity(BlockPos $$0, BlockState $$1) {
      this(BlockEntityType.CHEST, $$0, $$1);
   }

   public int getContainerSize() {
      return 27;
   }

   @Override
   protected Component getDefaultName() {
      return DEFAULT_NAME;
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable($$0)) {
         ContainerHelper.loadAllItems($$0, this.items);
      }
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      if (!this.trySaveLootTable($$0)) {
         ContainerHelper.saveAllItems($$0, this.items);
      }
   }

   public static void lidAnimateTick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, ChestBlockEntity $$3) {
      $$3.chestLidController.tickLid();
   }

   static void playSound(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, SoundEvent $$3) {
      ChestType $$4 = $$2.getValue(ChestBlock.TYPE);
      if ($$4 != ChestType.LEFT) {
         double $$5 = $$1.getX() + 0.5;
         double $$6 = $$1.getY() + 0.5;
         double $$7 = $$1.getZ() + 0.5;
         if ($$4 == ChestType.RIGHT) {
            Direction $$8 = ChestBlock.getConnectedDirection($$2);
            $$5 += $$8.getStepX() * 0.5;
            $$7 += $$8.getStepZ() * 0.5;
         }

         $$0.playSound(null, $$5, $$6, $$7, $$3, SoundSource.BLOCKS, 0.5F, $$0.random.nextFloat() * 0.1F + 0.9F);
      }
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

   public List<ContainerUser> getEntitiesWithContainerOpen() {
      return this.openersCounter.getEntitiesWithContainerOpen(this.getLevel(), this.getBlockPos());
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
   public float getOpenNess(float $$0) {
      return this.chestLidController.getOpenness($$0);
   }

   public static int getOpenCount(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1);
      if ($$2.hasBlockEntity()) {
         BlockEntity $$3 = $$0.getBlockEntity($$1);
         if ($$3 instanceof ChestBlockEntity) {
            return ((ChestBlockEntity)$$3).openersCounter.getOpenerCount();
         }
      }

      return 0;
   }

   public static void swapContents(ChestBlockEntity $$0, ChestBlockEntity $$1) {
      NonNullList<ItemStack> $$2 = $$0.getItems();
      $$0.setItems($$1.getItems());
      $$1.setItems($$2);
   }

   @Override
   protected AbstractContainerMenu createMenu(int $$0, Inventory $$1) {
      return ChestMenu.threeRows($$0, $$1, this);
   }

   public void recheckOpen() {
      if (!this.remove) {
         this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }
   }

   protected void signalOpenCount(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, int $$3, int $$4) {
      Block $$5 = $$2.getBlock();
      $$0.blockEvent($$1, $$5, 1, $$4);
   }
}
