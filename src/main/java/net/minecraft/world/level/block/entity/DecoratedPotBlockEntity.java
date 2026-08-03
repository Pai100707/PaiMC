package net.minecraft.world.level.block.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.ticks.ContainerSingleItem.BlockContainerSingleItem;
import org.jspecify.annotations.Nullable;

public class DecoratedPotBlockEntity extends BlockEntity implements RandomizableContainer, BlockContainerSingleItem {
   public static final String TAG_SHERDS = "sherds";
   public static final String TAG_ITEM = "item";
   public static final int EVENT_POT_WOBBLES = 1;
   public long wobbleStartedAtTick;
   @Nullable
   public DecoratedPotBlockEntity.WobbleStyle lastWobbleStyle;
   private PotDecorations decorations;
   private ItemStack item = ItemStack.EMPTY;
   @Nullable
   protected ResourceKey<LootTable> lootTable;
   protected long lootTableSeed;

   public DecoratedPotBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.DECORATED_POT, $$0, $$1);
      this.decorations = PotDecorations.EMPTY;
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      if (!this.decorations.equals(PotDecorations.EMPTY)) {
         $$0.store("sherds", PotDecorations.CODEC, this.decorations);
      }

      if (!this.trySaveLootTable($$0) && !this.item.isEmpty()) {
         $$0.store("item", ItemStack.CODEC, this.item);
      }
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.decorations = $$0.<PotDecorations>read("sherds", PotDecorations.CODEC).orElse(PotDecorations.EMPTY);
      if (!this.tryLoadLootTable($$0)) {
         this.item = $$0.<ItemStack>read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
      } else {
         this.item = ItemStack.EMPTY;
      }
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      return this.saveCustomOnly($$0);
   }

   public Direction getDirection() {
      return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
   }

   public PotDecorations getDecorations() {
      return this.decorations;
   }

   public static ItemStack createDecoratedPotItem(PotDecorations $$0) {
      ItemStack $$1 = Items.DECORATED_POT.getDefaultInstance();
      $$1.set(DataComponents.POT_DECORATIONS, $$0);
      return $$1;
   }

   @Nullable
   public ResourceKey<LootTable> getLootTable() {
      return this.lootTable;
   }

   public void setLootTable(@Nullable ResourceKey<LootTable> $$0) {
      this.lootTable = $$0;
   }

   public long getLootTableSeed() {
      return this.lootTableSeed;
   }

   public void setLootTableSeed(long $$0) {
      this.lootTableSeed = $$0;
   }

   @Override
   protected void collectImplicitComponents(Builder $$0) {
      super.collectImplicitComponents($$0);
      $$0.set(DataComponents.POT_DECORATIONS, this.decorations);
      $$0.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(this.item)));
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      super.applyImplicitComponents($$0);
      this.decorations = (PotDecorations)$$0.getOrDefault(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY);
      this.item = ((ItemContainerContents)$$0.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)).copyOne();
   }

   @Override
   public void removeComponentsFromTag(ValueOutput $$0) {
      super.removeComponentsFromTag($$0);
      $$0.discard("sherds");
      $$0.discard("item");
   }

   public ItemStack getTheItem() {
      this.unpackLootTable(null);
      return this.item;
   }

   public ItemStack splitTheItem(int $$0) {
      this.unpackLootTable(null);
      ItemStack $$1 = this.item.split($$0);
      if (this.item.isEmpty()) {
         this.item = ItemStack.EMPTY;
      }

      return $$1;
   }

   public void setTheItem(ItemStack $$0) {
      this.unpackLootTable(null);
      this.item = $$0;
   }

   public BlockEntity getContainerBlockEntity() {
      return this;
   }

   public void wobble(DecoratedPotBlockEntity.WobbleStyle $$0) {
      if (this.level != null && !this.level.isClientSide()) {
         this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, $$0.ordinal());
      }
   }

   @Override
   public boolean triggerEvent(int $$0, int $$1) {
      if (this.level != null && $$0 == 1 && $$1 >= 0 && $$1 < DecoratedPotBlockEntity.WobbleStyle.values().length) {
         this.wobbleStartedAtTick = this.level.getGameTime();
         this.lastWobbleStyle = DecoratedPotBlockEntity.WobbleStyle.values()[$$1];
         return true;
      } else {
         return super.triggerEvent($$0, $$1);
      }
   }

   public static enum WobbleStyle {
      POSITIVE(7),
      NEGATIVE(10);

      public final int duration;

      private WobbleStyle(final int $$0) {
         this.duration = $$0;
      }
   }
}
