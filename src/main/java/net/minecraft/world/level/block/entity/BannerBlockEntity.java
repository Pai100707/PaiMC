package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BannerBlockEntity extends BlockEntity implements Nameable {
   public static final int MAX_PATTERNS = 6;
   private static final String TAG_PATTERNS = "patterns";
   private static final Component DEFAULT_NAME = Component.translatable("block.minecraft.banner");
   
   private Component name;
   private final DyeColor baseColor;
   private BannerPatternLayers patterns = BannerPatternLayers.EMPTY;

   public BannerBlockEntity(BlockPos $$0, BlockState $$1) {
      this($$0, $$1, ((AbstractBannerBlock)$$1.getBlock()).getColor());
   }

   public BannerBlockEntity(BlockPos $$0, BlockState $$1, DyeColor $$2) {
      super(BlockEntityType.BANNER, $$0, $$1);
      this.baseColor = $$2;
   }

   public Component getName() {
      return this.name != null ? this.name : DEFAULT_NAME;
   }

   
   public Component getCustomName() {
      return this.name;
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      if (!this.patterns.equals(BannerPatternLayers.EMPTY)) {
         $$0.store("patterns", BannerPatternLayers.CODEC, this.patterns);
      }

      $$0.storeNullable("CustomName", ComponentSerialization.CODEC, this.name);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.name = parseCustomNameSafe($$0, "CustomName");
      this.patterns = $$0.<BannerPatternLayers>read("patterns", BannerPatternLayers.CODEC).orElse(BannerPatternLayers.EMPTY);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      return this.saveWithoutMetadata($$0);
   }

   public BannerPatternLayers getPatterns() {
      return this.patterns;
   }

   public ItemStack getItem() {
      ItemStack $$0 = new ItemStack(BannerBlock.byColor(this.baseColor));
      $$0.applyComponents(this.collectComponents());
      return $$0;
   }

   public DyeColor getBaseColor() {
      return this.baseColor;
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      super.applyImplicitComponents($$0);
      this.patterns = (BannerPatternLayers)$$0.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
      this.name = (Component)$$0.get(DataComponents.CUSTOM_NAME);
   }

   @Override
   protected void collectImplicitComponents(Builder $$0) {
      super.collectImplicitComponents($$0);
      $$0.set(DataComponents.BANNER_PATTERNS, this.patterns);
      $$0.set(DataComponents.CUSTOM_NAME, this.name);
   }

   @Override
   public void removeComponentsFromTag(ValueOutput $$0) {
      $$0.discard("patterns");
      $$0.discard("CustomName");
   }
}
