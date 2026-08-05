package net.minecraft.world.item;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ItemLike;

public class CreativeModeTab {
   static final Identifier DEFAULT_BACKGROUND = createTextureLocation("items");
   private final Component displayName;
   Identifier backgroundTexture = DEFAULT_BACKGROUND;
   boolean canScroll = true;
   boolean showTitle = true;
   boolean alignedRight = false;
   private final net.minecraft.world.item.CreativeModeTab.Row row;
   private final int column;
   private final net.minecraft.world.item.CreativeModeTab.Type type;
   
   private net.minecraft.world.item.ItemStack iconItemStack;
   private Collection<net.minecraft.world.item.ItemStack> displayItems = net.minecraft.world.item.ItemStackLinkedSet.createTypeAndComponentsSet();
   private Set<net.minecraft.world.item.ItemStack> displayItemsSearchTab = net.minecraft.world.item.ItemStackLinkedSet.createTypeAndComponentsSet();
   private final Supplier<net.minecraft.world.item.ItemStack> iconGenerator;
   private final net.minecraft.world.item.CreativeModeTab.DisplayItemsGenerator displayItemsGenerator;

   CreativeModeTab(
      net.minecraft.world.item.CreativeModeTab.Row $$0,
      int $$1,
      net.minecraft.world.item.CreativeModeTab.Type $$2,
      Component $$3,
      Supplier<net.minecraft.world.item.ItemStack> $$4,
      net.minecraft.world.item.CreativeModeTab.DisplayItemsGenerator $$5
   ) {
      this.row = $$0;
      this.column = $$1;
      this.displayName = $$3;
      this.iconGenerator = $$4;
      this.displayItemsGenerator = $$5;
      this.type = $$2;
   }

   public static Identifier createTextureLocation(String $$0) {
      return Identifier.withDefaultNamespace("textures/gui/container/creative_inventory/tab_" + $$0 + ".png");
   }

   public static net.minecraft.world.item.CreativeModeTab.Builder builder(net.minecraft.world.item.CreativeModeTab.Row $$0, int $$1) {
      return new net.minecraft.world.item.CreativeModeTab.Builder($$0, $$1);
   }

   public Component getDisplayName() {
      return this.displayName;
   }

   public net.minecraft.world.item.ItemStack getIconItem() {
      if (this.iconItemStack == null) {
         this.iconItemStack = this.iconGenerator.get();
      }

      return this.iconItemStack;
   }

   public Identifier getBackgroundTexture() {
      return this.backgroundTexture;
   }

   public boolean showTitle() {
      return this.showTitle;
   }

   public boolean canScroll() {
      return this.canScroll;
   }

   public int column() {
      return this.column;
   }

   public net.minecraft.world.item.CreativeModeTab.Row row() {
      return this.row;
   }

   public boolean hasAnyItems() {
      return !this.displayItems.isEmpty();
   }

   public boolean shouldDisplay() {
      return this.type != net.minecraft.world.item.CreativeModeTab.Type.CATEGORY || this.hasAnyItems();
   }

   public boolean isAlignedRight() {
      return this.alignedRight;
   }

   public net.minecraft.world.item.CreativeModeTab.Type getType() {
      return this.type;
   }

   public void buildContents(net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters $$0) {
      net.minecraft.world.item.CreativeModeTab.ItemDisplayBuilder $$1 = new net.minecraft.world.item.CreativeModeTab.ItemDisplayBuilder(
         this, $$0.enabledFeatures
      );
      ResourceKey<net.minecraft.world.item.CreativeModeTab> $$2 = (ResourceKey<net.minecraft.world.item.CreativeModeTab>)BuiltInRegistries.CREATIVE_MODE_TAB
         .getResourceKey(this)
         .orElseThrow(() -> new IllegalStateException("Unregistered creative tab: " + this));
      this.displayItemsGenerator.accept($$0, $$1);
      this.displayItems = $$1.tabContents;
      this.displayItemsSearchTab = $$1.searchTabContents;
   }

   public Collection<net.minecraft.world.item.ItemStack> getDisplayItems() {
      return this.displayItems;
   }

   public Collection<net.minecraft.world.item.ItemStack> getSearchTabDisplayItems() {
      return this.displayItemsSearchTab;
   }

   public boolean contains(net.minecraft.world.item.ItemStack $$0) {
      return this.displayItemsSearchTab.contains($$0);
   }

   public static class Builder {
      private static final net.minecraft.world.item.CreativeModeTab.DisplayItemsGenerator EMPTY_GENERATOR = ($$0, $$1) -> {};
      private final net.minecraft.world.item.CreativeModeTab.Row row;
      private final int column;
      private Component displayName = Component.empty();
      private Supplier<net.minecraft.world.item.ItemStack> iconGenerator = () -> net.minecraft.world.item.ItemStack.EMPTY;
      private net.minecraft.world.item.CreativeModeTab.DisplayItemsGenerator displayItemsGenerator = EMPTY_GENERATOR;
      private boolean canScroll = true;
      private boolean showTitle = true;
      private boolean alignedRight = false;
      private net.minecraft.world.item.CreativeModeTab.Type type = net.minecraft.world.item.CreativeModeTab.Type.CATEGORY;
      private Identifier backgroundTexture = net.minecraft.world.item.CreativeModeTab.DEFAULT_BACKGROUND;

      public Builder(net.minecraft.world.item.CreativeModeTab.Row $$0, int $$1) {
         this.row = $$0;
         this.column = $$1;
      }

      public net.minecraft.world.item.CreativeModeTab.Builder title(Component $$0) {
         this.displayName = $$0;
         return this;
      }

      public net.minecraft.world.item.CreativeModeTab.Builder icon(Supplier<net.minecraft.world.item.ItemStack> $$0) {
         this.iconGenerator = $$0;
         return this;
      }

      public net.minecraft.world.item.CreativeModeTab.Builder displayItems(net.minecraft.world.item.CreativeModeTab.DisplayItemsGenerator $$0) {
         this.displayItemsGenerator = $$0;
         return this;
      }

      public net.minecraft.world.item.CreativeModeTab.Builder alignedRight() {
         this.alignedRight = true;
         return this;
      }

      public net.minecraft.world.item.CreativeModeTab.Builder hideTitle() {
         this.showTitle = false;
         return this;
      }

      public net.minecraft.world.item.CreativeModeTab.Builder noScrollBar() {
         this.canScroll = false;
         return this;
      }

      protected net.minecraft.world.item.CreativeModeTab.Builder type(net.minecraft.world.item.CreativeModeTab.Type $$0) {
         this.type = $$0;
         return this;
      }

      public net.minecraft.world.item.CreativeModeTab.Builder backgroundTexture(Identifier $$0) {
         this.backgroundTexture = $$0;
         return this;
      }

      public net.minecraft.world.item.CreativeModeTab build() {
         if ((this.type == net.minecraft.world.item.CreativeModeTab.Type.HOTBAR || this.type == net.minecraft.world.item.CreativeModeTab.Type.INVENTORY)
            && this.displayItemsGenerator != EMPTY_GENERATOR) {
            throw new IllegalStateException("Special tabs can't have display items");
         } else {
            net.minecraft.world.item.CreativeModeTab $$0 = new net.minecraft.world.item.CreativeModeTab(
               this.row, this.column, this.type, this.displayName, this.iconGenerator, this.displayItemsGenerator
            );
            $$0.alignedRight = this.alignedRight;
            $$0.showTitle = this.showTitle;
            $$0.canScroll = this.canScroll;
            $$0.backgroundTexture = this.backgroundTexture;
            return $$0;
         }
      }
   }

   @FunctionalInterface
   public interface DisplayItemsGenerator {
      void accept(net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters var1, net.minecraft.world.item.CreativeModeTab.Output var2);
   }

   static class ItemDisplayBuilder implements net.minecraft.world.item.CreativeModeTab.Output {
      public final Collection<net.minecraft.world.item.ItemStack> tabContents = net.minecraft.world.item.ItemStackLinkedSet.createTypeAndComponentsSet();
      public final Set<net.minecraft.world.item.ItemStack> searchTabContents = net.minecraft.world.item.ItemStackLinkedSet.createTypeAndComponentsSet();
      private final net.minecraft.world.item.CreativeModeTab tab;
      private final FeatureFlagSet featureFlagSet;

      public ItemDisplayBuilder(net.minecraft.world.item.CreativeModeTab $$0, FeatureFlagSet $$1) {
         this.tab = $$0;
         this.featureFlagSet = $$1;
      }

      @Override
      public void accept(net.minecraft.world.item.ItemStack $$0, net.minecraft.world.item.CreativeModeTab.TabVisibility $$1) {
         if ($$0.getCount() != 1) {
            throw new IllegalArgumentException("Stack size must be exactly 1");
         } else {
            boolean $$2 = this.tabContents.contains($$0) && $$1 != net.minecraft.world.item.CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY;
            if ($$2) {
               throw new IllegalStateException(
                  "Accidentally adding the same item stack twice "
                     + $$0.getDisplayName().getString()
                     + " to a Creative Mode Tab: "
                     + this.tab.getDisplayName().getString()
               );
            } else {
               if ($$0.getItem().isEnabled(this.featureFlagSet)) {
                  switch ($$1) {
                     case PARENT_AND_SEARCH_TABS:
                        this.tabContents.add($$0);
                        this.searchTabContents.add($$0);
                        break;
                     case PARENT_TAB_ONLY:
                        this.tabContents.add($$0);
                        break;
                     case SEARCH_TAB_ONLY:
                        this.searchTabContents.add($$0);
                  }
               }
            }
         }
      }
   }

   public record ItemDisplayParameters(FeatureFlagSet enabledFeatures, boolean hasPermissions, Provider holders) {

      public boolean needsUpdate(FeatureFlagSet $$0, boolean $$1, Provider $$2) {
         return !this.enabledFeatures.equals($$0) || this.hasPermissions != $$1 || this.holders != $$2;
      }
   }

   public interface Output {
      void accept(net.minecraft.world.item.ItemStack var1, net.minecraft.world.item.CreativeModeTab.TabVisibility var2);

      default void accept(net.minecraft.world.item.ItemStack $$0) {
         this.accept($$0, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
      }

      default void accept(ItemLike $$0, net.minecraft.world.item.CreativeModeTab.TabVisibility $$1) {
         this.accept(new net.minecraft.world.item.ItemStack($$0), $$1);
      }

      default void accept(ItemLike $$0) {
         this.accept(new net.minecraft.world.item.ItemStack($$0), net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
      }

      default void acceptAll(Collection<net.minecraft.world.item.ItemStack> $$0, net.minecraft.world.item.CreativeModeTab.TabVisibility $$1) {
         $$0.forEach($$1x -> this.accept($$1x, $$1));
      }

      default void acceptAll(Collection<net.minecraft.world.item.ItemStack> $$0) {
         this.acceptAll($$0, net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
      }
   }

   public static enum Row {
      TOP,
      BOTTOM;
   }

   protected static enum TabVisibility {
      PARENT_AND_SEARCH_TABS,
      PARENT_TAB_ONLY,
      SEARCH_TAB_ONLY;
   }

   public static enum Type {
      CATEGORY,
      INVENTORY,
      HOTBAR,
      SEARCH;
   }
}
