package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class EnchantedCountIncreaseFunction extends LootItemConditionalFunction {
   public static final int NO_LIMIT = 0;
   public static final MapCodec<EnchantedCountIncreaseFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               Enchantment.CODEC.fieldOf("enchantment").forGetter($$0x -> $$0x.enchantment),
               NumberProviders.CODEC.fieldOf("count").forGetter($$0x -> $$0x.value),
               Codec.INT.optionalFieldOf("limit", 0).forGetter($$0x -> $$0x.limit)
            )
         )
         .apply($$0, EnchantedCountIncreaseFunction::new)
   );
   private final Holder<Enchantment> enchantment;
   private final NumberProvider value;
   private final int limit;

   EnchantedCountIncreaseFunction(List<LootItemCondition> $$0, Holder<Enchantment> $$1, NumberProvider $$2, int $$3) {
      super($$0);
      this.enchantment = $$1;
      this.value = $$2;
      this.limit = $$3;
   }

   @Override
   public LootItemFunctionType<EnchantedCountIncreaseFunction> getType() {
      return LootItemFunctions.ENCHANTED_COUNT_INCREASE;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Sets.union(ImmutableSet.of(LootContextParams.ATTACKING_ENTITY), this.value.getReferencedContextParams());
   }

   private boolean hasLimit() {
      return this.limit > 0;
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      Entity $$2 = $$1.getOptionalParameter(LootContextParams.ATTACKING_ENTITY);
      if ($$2 instanceof LivingEntity $$3) {
         int $$4 = EnchantmentHelper.getEnchantmentLevel(this.enchantment, $$3);
         if ($$4 == 0) {
            return $$0;
         }

         float $$5 = $$4 * this.value.getFloat($$1);
         $$0.grow(Math.round($$5));
         if (this.hasLimit()) {
            $$0.limitSize(this.limit);
         }
      }

      return $$0;
   }

   public static EnchantedCountIncreaseFunction.Builder lootingMultiplier(Provider $$0, NumberProvider $$1) {
      RegistryLookup<Enchantment> $$2 = $$0.lookupOrThrow(Registries.ENCHANTMENT);
      return new EnchantedCountIncreaseFunction.Builder($$2.getOrThrow(Enchantments.LOOTING), $$1);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<EnchantedCountIncreaseFunction.Builder> {
      private final Holder<Enchantment> enchantment;
      private final NumberProvider count;
      private int limit = 0;

      public Builder(Holder<Enchantment> $$0, NumberProvider $$1) {
         this.enchantment = $$0;
         this.count = $$1;
      }

      protected EnchantedCountIncreaseFunction.Builder getThis() {
         return this;
      }

      public EnchantedCountIncreaseFunction.Builder setLimit(int $$0) {
         this.limit = $$0;
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new EnchantedCountIncreaseFunction(this.getConditions(), this.enchantment, this.count, this.limit);
      }
   }
}
