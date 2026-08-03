package net.minecraft.world.item.enchantment;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.Nullable;

public class ItemEnchantments implements TooltipProvider {
   public static final ItemEnchantments EMPTY = new ItemEnchantments(new Object2IntOpenHashMap());
   private static final Codec<Integer> LEVEL_CODEC = Codec.intRange(1, 255);
   public static final Codec<ItemEnchantments> CODEC = Codec.unboundedMap(Enchantment.CODEC, LEVEL_CODEC)
      .xmap($$0 -> new ItemEnchantments(new Object2IntOpenHashMap($$0)), $$0 -> $$0.enchantments);
   public static final StreamCodec<RegistryFriendlyByteBuf, ItemEnchantments> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.map(Object2IntOpenHashMap::new, Enchantment.STREAM_CODEC, ByteBufCodecs.VAR_INT), $$0 -> $$0.enchantments, ItemEnchantments::new
   );
   final Object2IntOpenHashMap<Holder<Enchantment>> enchantments;

   ItemEnchantments(Object2IntOpenHashMap<Holder<Enchantment>> $$0) {
      this.enchantments = $$0;
      ObjectIterator var2 = $$0.object2IntEntrySet().iterator();

      while (var2.hasNext()) {
         Entry<Holder<Enchantment>> $$1 = (Entry<Holder<Enchantment>>)var2.next();
         int $$2 = $$1.getIntValue();
         if ($$2 < 0 || $$2 > 255) {
            throw new IllegalArgumentException("Enchantment " + $$1.getKey() + " has invalid level " + $$2);
         }
      }
   }

   public int getLevel(Holder<Enchantment> $$0) {
      return this.enchantments.getInt($$0);
   }

   @Override
   public void addToTooltip(
      net.minecraft.world.item.Item.TooltipContext $$0, Consumer<Component> $$1, net.minecraft.world.item.TooltipFlag $$2, DataComponentGetter $$3
   ) {
      Provider $$4 = $$0.registries();
      HolderSet<Enchantment> $$5 = getTagOrEmpty($$4, Registries.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);

      for (Holder<Enchantment> $$6 : $$5) {
         int $$7 = this.enchantments.getInt($$6);
         if ($$7 > 0) {
            $$1.accept(Enchantment.getFullname($$6, $$7));
         }
      }

      ObjectIterator var10 = this.enchantments.object2IntEntrySet().iterator();

      while (var10.hasNext()) {
         Entry<Holder<Enchantment>> $$8 = (Entry<Holder<Enchantment>>)var10.next();
         Holder<Enchantment> $$9 = (Holder<Enchantment>)$$8.getKey();
         if (!$$5.contains($$9)) {
            $$1.accept(Enchantment.getFullname((Holder<Enchantment>)$$8.getKey(), $$8.getIntValue()));
         }
      }
   }

   private static <T> HolderSet<T> getTagOrEmpty(@Nullable Provider $$0, ResourceKey<Registry<T>> $$1, TagKey<T> $$2) {
      if ($$0 != null) {
         Optional<Named<T>> $$3 = $$0.lookupOrThrow($$1).get($$2);
         if ($$3.isPresent()) {
            return (HolderSet<T>)$$3.get();
         }
      }

      return HolderSet.direct(new Holder[0]);
   }

   public Set<Holder<Enchantment>> keySet() {
      return Collections.unmodifiableSet(this.enchantments.keySet());
   }

   public Set<Entry<Holder<Enchantment>>> entrySet() {
      return Collections.unmodifiableSet(this.enchantments.object2IntEntrySet());
   }

   public int size() {
      return this.enchantments.size();
   }

   public boolean isEmpty() {
      return this.enchantments.isEmpty();
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return $$0 instanceof ItemEnchantments $$1 ? this.enchantments.equals($$1.enchantments) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.enchantments.hashCode();
   }

   @Override
   public String toString() {
      return "ItemEnchantments{enchantments=" + this.enchantments + "}";
   }

   public static class Mutable {
      private final Object2IntOpenHashMap<Holder<Enchantment>> enchantments = new Object2IntOpenHashMap();

      public Mutable(ItemEnchantments $$0) {
         this.enchantments.putAll($$0.enchantments);
      }

      public void set(Holder<Enchantment> $$0, int $$1) {
         if ($$1 <= 0) {
            this.enchantments.removeInt($$0);
         } else {
            this.enchantments.put($$0, Math.min($$1, 255));
         }
      }

      public void upgrade(Holder<Enchantment> $$0, int $$1) {
         if ($$1 > 0) {
            this.enchantments.merge($$0, Math.min($$1, 255), Integer::max);
         }
      }

      public void removeIf(Predicate<Holder<Enchantment>> $$0) {
         this.enchantments.keySet().removeIf($$0);
      }

      public int getLevel(Holder<Enchantment> $$0) {
         return this.enchantments.getOrDefault($$0, 0);
      }

      public Set<Holder<Enchantment>> keySet() {
         return this.enchantments.keySet();
      }

      public ItemEnchantments toImmutable() {
         return new ItemEnchantments(this.enchantments);
      }
   }
}
