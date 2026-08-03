package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;

public class ChiseledBookShelfBlockEntity extends BlockEntity implements ListBackedContainer {
   public static final int MAX_BOOKS_IN_STORAGE = 6;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int DEFAULT_LAST_INTERACTED_SLOT = -1;
   private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
   private int lastInteractedSlot = -1;

   public ChiseledBookShelfBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.CHISELED_BOOKSHELF, $$0, $$1);
   }

   private void updateState(int $$0) {
      if ($$0 >= 0 && $$0 < 6) {
         this.lastInteractedSlot = $$0;
         BlockState $$1 = this.getBlockState();

         for (int $$2 = 0; $$2 < ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.size(); $$2++) {
            boolean $$3 = !this.getItem($$2).isEmpty();
            BooleanProperty $$4 = ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get($$2);
            $$1 = $$1.setValue($$4, $$3);
         }

         Objects.requireNonNull(this.level).setBlock(this.worldPosition, $$1, 3);
         this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.worldPosition, GameEvent.Context.of($$1));
      } else {
         LOGGER.error("Expected slot 0-5, got {}", $$0);
      }
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.items.clear();
      ContainerHelper.loadAllItems($$0, this.items);
      this.lastInteractedSlot = $$0.getIntOr("last_interacted_slot", -1);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      ContainerHelper.saveAllItems($$0, this.items, true);
      $$0.putInt("last_interacted_slot", this.lastInteractedSlot);
   }

   public int getMaxStackSize() {
      return 1;
   }

   @Override
   public boolean acceptsItemType(ItemStack $$0) {
      return $$0.is(ItemTags.BOOKSHELF_BOOKS);
   }

   @Override
   public ItemStack removeItem(int $$0, int $$1) {
      ItemStack $$2 = Objects.requireNonNullElse((ItemStack)this.getItems().get($$0), ItemStack.EMPTY);
      this.getItems().set($$0, ItemStack.EMPTY);
      if (!$$2.isEmpty()) {
         this.updateState($$0);
      }

      return $$2;
   }

   @Override
   public void setItem(int $$0, ItemStack $$1) {
      if (this.acceptsItemType($$1)) {
         this.getItems().set($$0, $$1);
         this.updateState($$0);
      } else if ($$1.isEmpty()) {
         this.removeItem($$0, this.getMaxStackSize());
      }
   }

   public boolean canTakeItem(Container $$0, int $$1, ItemStack $$2) {
      return $$0.hasAnyMatching(
         $$2x -> $$2x.isEmpty() ? true : ItemStack.isSameItemSameComponents($$2, $$2x) && $$2x.getCount() + $$2.getCount() <= $$0.getMaxStackSize($$2x)
      );
   }

   @Override
   public NonNullList<ItemStack> getItems() {
      return this.items;
   }

   public boolean stillValid(Player $$0) {
      return Container.stillValidBlockEntity(this, $$0);
   }

   public int getLastInteractedSlot() {
      return this.lastInteractedSlot;
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      super.applyImplicitComponents($$0);
      ((ItemContainerContents)$$0.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)).copyInto(this.items);
   }

   @Override
   protected void collectImplicitComponents(Builder $$0) {
      super.collectImplicitComponents($$0);
      $$0.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.items));
   }

   @Override
   public void removeComponentsFromTag(ValueOutput $$0) {
      $$0.discard("Items");
   }
}
