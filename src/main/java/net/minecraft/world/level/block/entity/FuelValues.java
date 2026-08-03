package net.minecraft.world.level.block.entity;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import java.util.Collections;
import java.util.SequencedSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class FuelValues {
   private final Object2IntSortedMap<Item> values;

   FuelValues(Object2IntSortedMap<Item> $$0) {
      this.values = $$0;
   }

   public boolean isFuel(ItemStack $$0) {
      return this.values.containsKey($$0.getItem());
   }

   public SequencedSet<Item> fuelItems() {
      return Collections.unmodifiableSequencedSet(this.values.keySet());
   }

   public int burnDuration(ItemStack $$0) {
      return $$0.isEmpty() ? 0 : this.values.getInt($$0.getItem());
   }

   public static FuelValues vanillaBurnTimes(Provider $$0, FeatureFlagSet $$1) {
      return vanillaBurnTimes($$0, $$1, 200);
   }

   public static FuelValues vanillaBurnTimes(Provider $$0, FeatureFlagSet $$1, int $$2) {
      return new FuelValues.Builder($$0, $$1)
         .add(Items.LAVA_BUCKET, $$2 * 100)
         .add(Blocks.COAL_BLOCK, $$2 * 8 * 10)
         .add(Items.BLAZE_ROD, $$2 * 12)
         .add(Items.COAL, $$2 * 8)
         .add(Items.CHARCOAL, $$2 * 8)
         .add(ItemTags.LOGS, $$2 * 3 / 2)
         .add(ItemTags.BAMBOO_BLOCKS, $$2 * 3 / 2)
         .add(ItemTags.PLANKS, $$2 * 3 / 2)
         .add(Blocks.BAMBOO_MOSAIC, $$2 * 3 / 2)
         .add(ItemTags.WOODEN_STAIRS, $$2 * 3 / 2)
         .add(Blocks.BAMBOO_MOSAIC_STAIRS, $$2 * 3 / 2)
         .add(ItemTags.WOODEN_SLABS, $$2 * 3 / 4)
         .add(Blocks.BAMBOO_MOSAIC_SLAB, $$2 * 3 / 4)
         .add(ItemTags.WOODEN_TRAPDOORS, $$2 * 3 / 2)
         .add(ItemTags.WOODEN_PRESSURE_PLATES, $$2 * 3 / 2)
         .add(ItemTags.WOODEN_SHELVES, $$2 * 3 / 2)
         .add(ItemTags.WOODEN_FENCES, $$2 * 3 / 2)
         .add(ItemTags.FENCE_GATES, $$2 * 3 / 2)
         .add(Blocks.NOTE_BLOCK, $$2 * 3 / 2)
         .add(Blocks.BOOKSHELF, $$2 * 3 / 2)
         .add(Blocks.CHISELED_BOOKSHELF, $$2 * 3 / 2)
         .add(Blocks.LECTERN, $$2 * 3 / 2)
         .add(Blocks.JUKEBOX, $$2 * 3 / 2)
         .add(Blocks.CHEST, $$2 * 3 / 2)
         .add(Blocks.TRAPPED_CHEST, $$2 * 3 / 2)
         .add(Blocks.CRAFTING_TABLE, $$2 * 3 / 2)
         .add(Blocks.DAYLIGHT_DETECTOR, $$2 * 3 / 2)
         .add(ItemTags.BANNERS, $$2 * 3 / 2)
         .add(Items.BOW, $$2 * 3 / 2)
         .add(Items.FISHING_ROD, $$2 * 3 / 2)
         .add(Blocks.LADDER, $$2 * 3 / 2)
         .add(ItemTags.SIGNS, $$2)
         .add(ItemTags.HANGING_SIGNS, $$2 * 4)
         .add(Items.WOODEN_SHOVEL, $$2)
         .add(Items.WOODEN_SWORD, $$2)
         .add(Items.WOODEN_SPEAR, $$2)
         .add(Items.WOODEN_HOE, $$2)
         .add(Items.WOODEN_AXE, $$2)
         .add(Items.WOODEN_PICKAXE, $$2)
         .add(ItemTags.WOODEN_DOORS, $$2)
         .add(ItemTags.BOATS, $$2 * 6)
         .add(ItemTags.WOOL, $$2 / 2)
         .add(ItemTags.WOODEN_BUTTONS, $$2 / 2)
         .add(Items.STICK, $$2 / 2)
         .add(ItemTags.SAPLINGS, $$2 / 2)
         .add(Items.BOWL, $$2 / 2)
         .add(ItemTags.WOOL_CARPETS, 1 + $$2 / 3)
         .add(Blocks.DRIED_KELP_BLOCK, 1 + $$2 * 20)
         .add(Items.CROSSBOW, $$2 * 3 / 2)
         .add(Blocks.BAMBOO, $$2 / 4)
         .add(Blocks.DEAD_BUSH, $$2 / 2)
         .add(Blocks.SHORT_DRY_GRASS, $$2 / 2)
         .add(Blocks.TALL_DRY_GRASS, $$2 / 2)
         .add(Blocks.SCAFFOLDING, $$2 / 4)
         .add(Blocks.LOOM, $$2 * 3 / 2)
         .add(Blocks.BARREL, $$2 * 3 / 2)
         .add(Blocks.CARTOGRAPHY_TABLE, $$2 * 3 / 2)
         .add(Blocks.FLETCHING_TABLE, $$2 * 3 / 2)
         .add(Blocks.SMITHING_TABLE, $$2 * 3 / 2)
         .add(Blocks.COMPOSTER, $$2 * 3 / 2)
         .add(Blocks.AZALEA, $$2 / 2)
         .add(Blocks.FLOWERING_AZALEA, $$2 / 2)
         .add(Blocks.MANGROVE_ROOTS, $$2 * 3 / 2)
         .add(Blocks.LEAF_LITTER, $$2 / 2)
         .remove(ItemTags.NON_FLAMMABLE_WOOD)
         .build();
   }

   public static class Builder {
      private final HolderLookup<Item> items;
      private final FeatureFlagSet enabledFeatures;
      private final Object2IntSortedMap<Item> values = new Object2IntLinkedOpenHashMap();

      public Builder(Provider $$0, FeatureFlagSet $$1) {
         this.items = $$0.lookupOrThrow(Registries.ITEM);
         this.enabledFeatures = $$1;
      }

      public FuelValues build() {
         return new FuelValues(this.values);
      }

      public FuelValues.Builder remove(TagKey<Item> $$0) {
         this.values.keySet().removeIf($$1 -> $$1.builtInRegistryHolder().is($$0));
         return this;
      }

      public FuelValues.Builder add(TagKey<Item> $$0, int $$1) {
         this.items.get($$0).ifPresent($$1x -> {
            for (Holder<Item> $$2 : $$1x) {
               this.putInternal($$1, (Item)$$2.value());
            }
         });
         return this;
      }

      public FuelValues.Builder add(net.minecraft.world.level.ItemLike $$0, int $$1) {
         Item $$2 = $$0.asItem();
         this.putInternal($$1, $$2);
         return this;
      }

      private void putInternal(int $$0, Item $$1) {
         if ($$1.isEnabled(this.enabledFeatures)) {
            this.values.put($$1, $$0);
         }
      }
   }
}
