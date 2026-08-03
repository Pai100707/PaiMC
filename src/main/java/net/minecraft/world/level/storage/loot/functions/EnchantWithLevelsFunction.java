package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class EnchantWithLevelsFunction extends LootItemConditionalFunction {
   public static final MapCodec<EnchantWithLevelsFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               NumberProviders.CODEC.fieldOf("levels").forGetter($$0x -> $$0x.levels),
               RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("options").forGetter($$0x -> $$0x.options)
            )
         )
         .apply($$0, EnchantWithLevelsFunction::new)
   );
   private final NumberProvider levels;
   private final Optional<HolderSet<Enchantment>> options;

   EnchantWithLevelsFunction(List<LootItemCondition> $$0, NumberProvider $$1, Optional<HolderSet<Enchantment>> $$2) {
      super($$0);
      this.levels = $$1;
      this.options = $$2;
   }

   @Override
   public LootItemFunctionType<EnchantWithLevelsFunction> getType() {
      return LootItemFunctions.ENCHANT_WITH_LEVELS;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.levels.getReferencedContextParams();
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      RandomSource $$2 = $$1.getRandom();
      RegistryAccess $$3 = $$1.getLevel().registryAccess();
      return EnchantmentHelper.enchantItem($$2, $$0, this.levels.getInt($$1), $$3, this.options);
   }

   public static EnchantWithLevelsFunction.Builder enchantWithLevels(Provider $$0, NumberProvider $$1) {
      return new EnchantWithLevelsFunction.Builder($$1).fromOptions($$0.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.ON_RANDOM_LOOT));
   }

   public static class Builder extends LootItemConditionalFunction.Builder<EnchantWithLevelsFunction.Builder> {
      private final NumberProvider levels;
      private Optional<HolderSet<Enchantment>> options = Optional.empty();

      public Builder(NumberProvider $$0) {
         this.levels = $$0;
      }

      protected EnchantWithLevelsFunction.Builder getThis() {
         return this;
      }

      public EnchantWithLevelsFunction.Builder fromOptions(HolderSet<Enchantment> $$0) {
         this.options = Optional.of($$0);
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new EnchantWithLevelsFunction(this.getConditions(), this.levels, this.options);
      }
   }
}
