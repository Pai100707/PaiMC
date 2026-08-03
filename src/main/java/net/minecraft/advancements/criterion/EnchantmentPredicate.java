package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record EnchantmentPredicate(Optional<HolderSet<Enchantment>> enchantments, MinMaxBounds.Ints level) {
   public static final Codec<EnchantmentPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("enchantments").forGetter(EnchantmentPredicate::enchantments),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("levels", MinMaxBounds.Ints.ANY).forGetter(EnchantmentPredicate::level)
         )
         .apply($$0, EnchantmentPredicate::new)
   );

   public EnchantmentPredicate(Holder<Enchantment> $$0, MinMaxBounds.Ints $$1) {
      this(Optional.of(HolderSet.direct(new Holder[]{$$0})), $$1);
   }

   public EnchantmentPredicate(HolderSet<Enchantment> $$0, MinMaxBounds.Ints $$1) {
      this(Optional.of($$0), $$1);
   }

   public boolean containedIn(ItemEnchantments $$0) {
      if (this.enchantments.isPresent()) {
         for (Holder<Enchantment> $$1 : this.enchantments.get()) {
            if (this.matchesEnchantment($$0, $$1)) {
               return true;
            }
         }

         return false;
      } else if (this.level != MinMaxBounds.Ints.ANY) {
         for (Entry<Holder<Enchantment>> $$2 : $$0.entrySet()) {
            if (this.level.matches($$2.getIntValue())) {
               return true;
            }
         }

         return false;
      } else {
         return !$$0.isEmpty();
      }
   }

   private boolean matchesEnchantment(ItemEnchantments $$0, Holder<Enchantment> $$1) {
      int $$2 = $$0.getLevel($$1);
      if ($$2 == 0) {
         return false;
      } else {
         return this.level == MinMaxBounds.Ints.ANY ? true : this.level.matches($$2);
      }
   }
}
