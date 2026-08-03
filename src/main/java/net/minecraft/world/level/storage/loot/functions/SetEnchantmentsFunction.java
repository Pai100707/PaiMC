package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetEnchantmentsFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetEnchantmentsFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               Codec.unboundedMap(Enchantment.CODEC, NumberProviders.CODEC).optionalFieldOf("enchantments", Map.of()).forGetter($$0x -> $$0x.enchantments),
               Codec.BOOL.fieldOf("add").orElse(false).forGetter($$0x -> $$0x.add)
            )
         )
         .apply($$0, SetEnchantmentsFunction::new)
   );
   private final Map<Holder<Enchantment>, NumberProvider> enchantments;
   private final boolean add;

   SetEnchantmentsFunction(List<LootItemCondition> $$0, Map<Holder<Enchantment>, NumberProvider> $$1, boolean $$2) {
      super($$0);
      this.enchantments = Map.copyOf($$1);
      this.add = $$2;
   }

   @Override
   public LootItemFunctionType<SetEnchantmentsFunction> getType() {
      return LootItemFunctions.SET_ENCHANTMENTS;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.enchantments.values().stream().flatMap($$0 -> $$0.getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      if ($$0.is(Items.BOOK)) {
         $$0 = $$0.transmuteCopy(Items.ENCHANTED_BOOK);
      }

      EnchantmentHelper.updateEnchantments($$0, $$1x -> {
         if (this.add) {
            this.enchantments.forEach(($$2, $$3) -> $$1x.set($$2, Mth.clamp($$1x.getLevel($$2) + $$3.getInt($$1), 0, 255)));
         } else {
            this.enchantments.forEach(($$2, $$3) -> $$1x.set($$2, Mth.clamp($$3.getInt($$1), 0, 255)));
         }
      });
      return $$0;
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetEnchantmentsFunction.Builder> {
      private final com.google.common.collect.ImmutableMap.Builder<Holder<Enchantment>, NumberProvider> enchantments = ImmutableMap.builder();
      private final boolean add;

      public Builder() {
         this(false);
      }

      public Builder(boolean $$0) {
         this.add = $$0;
      }

      protected SetEnchantmentsFunction.Builder getThis() {
         return this;
      }

      public SetEnchantmentsFunction.Builder withEnchantment(Holder<Enchantment> $$0, NumberProvider $$1) {
         this.enchantments.put($$0, $$1);
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new SetEnchantmentsFunction(this.getConditions(), this.enchantments.build(), this.add);
      }
   }
}
