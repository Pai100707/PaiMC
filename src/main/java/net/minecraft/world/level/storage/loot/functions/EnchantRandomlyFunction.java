package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class EnchantRandomlyFunction extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final MapCodec<EnchantRandomlyFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("options").forGetter($$0x -> $$0x.options),
               Codec.BOOL.optionalFieldOf("only_compatible", true).forGetter($$0x -> $$0x.onlyCompatible)
            )
         )
         .apply($$0, EnchantRandomlyFunction::new)
   );
   private final Optional<HolderSet<Enchantment>> options;
   private final boolean onlyCompatible;

   EnchantRandomlyFunction(List<LootItemCondition> $$0, Optional<HolderSet<Enchantment>> $$1, boolean $$2) {
      super($$0);
      this.options = $$1;
      this.onlyCompatible = $$2;
   }

   @Override
   public LootItemFunctionType<EnchantRandomlyFunction> getType() {
      return LootItemFunctions.ENCHANT_RANDOMLY;
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      RandomSource $$2 = $$1.getRandom();
      boolean $$3 = $$0.is(Items.BOOK);
      boolean $$4 = !$$3 && this.onlyCompatible;
      Stream<Holder<Enchantment>> $$5 = this.options
         .<Stream<Holder<Enchantment>>>map(HolderSet::stream)
         .orElseGet(() -> $$1.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements().map(Function.identity()))
         .filter($$2x -> !$$4 || ((Enchantment)$$2x.value()).canEnchant($$0));
      List<Holder<Enchantment>> $$6 = $$5.toList();
      Optional<Holder<Enchantment>> $$7 = Util.getRandomSafe($$6, $$2);
      if ($$7.isEmpty()) {
         LOGGER.warn("Couldn't find a compatible enchantment for {}", $$0);
         return $$0;
      } else {
         return enchantItem($$0, $$7.get(), $$2);
      }
   }

   private static ItemStack enchantItem(ItemStack $$0, Holder<Enchantment> $$1, RandomSource $$2) {
      int $$3 = Mth.nextInt($$2, ((Enchantment)$$1.value()).getMinLevel(), ((Enchantment)$$1.value()).getMaxLevel());
      if ($$0.is(Items.BOOK)) {
         $$0 = new ItemStack(Items.ENCHANTED_BOOK);
      }

      $$0.enchant($$1, $$3);
      return $$0;
   }

   public static EnchantRandomlyFunction.Builder randomEnchantment() {
      return new EnchantRandomlyFunction.Builder();
   }

   public static EnchantRandomlyFunction.Builder randomApplicableEnchantment(Provider $$0) {
      return randomEnchantment().withOneOf($$0.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.ON_RANDOM_LOOT));
   }

   public static class Builder extends LootItemConditionalFunction.Builder<EnchantRandomlyFunction.Builder> {
      private Optional<HolderSet<Enchantment>> options = Optional.empty();
      private boolean onlyCompatible = true;

      protected EnchantRandomlyFunction.Builder getThis() {
         return this;
      }

      public EnchantRandomlyFunction.Builder withEnchantment(Holder<Enchantment> $$0) {
         this.options = Optional.of(HolderSet.direct(new Holder[]{$$0}));
         return this;
      }

      public EnchantRandomlyFunction.Builder withOneOf(HolderSet<Enchantment> $$0) {
         this.options = Optional.of($$0);
         return this;
      }

      public EnchantRandomlyFunction.Builder allowingIncompatibleEnchantments() {
         this.onlyCompatible = false;
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new EnchantRandomlyFunction(this.getConditions(), this.options, this.onlyCompatible);
      }
   }
}
