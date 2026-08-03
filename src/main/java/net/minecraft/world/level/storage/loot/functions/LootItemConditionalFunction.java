package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.util.Util;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootItemConditionalFunction implements LootItemFunction {
   protected final List<LootItemCondition> predicates;
   private final Predicate<LootContext> compositePredicates;

   protected LootItemConditionalFunction(List<LootItemCondition> $$0) {
      this.predicates = $$0;
      this.compositePredicates = Util.allOf($$0);
   }

   @Override
   public abstract LootItemFunctionType<? extends LootItemConditionalFunction> getType();

   protected static <T extends LootItemConditionalFunction> P1<Mu<T>, List<LootItemCondition>> commonFields(Instance<T> $$0) {
      return $$0.group(LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter($$0x -> $$0x.predicates));
   }

   public final ItemStack apply(ItemStack $$0, LootContext $$1) {
      return this.compositePredicates.test($$1) ? this.run($$0, $$1) : $$0;
   }

   protected abstract ItemStack run(ItemStack var1, LootContext var2);

   @Override
   public void validate(ValidationContext $$0) {
      LootItemFunction.super.validate($$0);

      for (int $$1 = 0; $$1 < this.predicates.size(); $$1++) {
         this.predicates.get($$1).validate($$0.forChild(new IndexedFieldPathElement("conditions", $$1)));
      }
   }

   protected static LootItemConditionalFunction.Builder<?> simpleBuilder(Function<List<LootItemCondition>, LootItemFunction> $$0) {
      return new LootItemConditionalFunction.DummyBuilder($$0);
   }

   public abstract static class Builder<T extends LootItemConditionalFunction.Builder<T>> implements LootItemFunction.Builder, ConditionUserBuilder<T> {
      private final com.google.common.collect.ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();

      public T when(LootItemCondition.Builder $$0) {
         this.conditions.add($$0.build());
         return this.getThis();
      }

      public final T unwrap() {
         return this.getThis();
      }

      protected abstract T getThis();

      protected List<LootItemCondition> getConditions() {
         return this.conditions.build();
      }
   }

   static final class DummyBuilder extends LootItemConditionalFunction.Builder<LootItemConditionalFunction.DummyBuilder> {
      private final Function<List<LootItemCondition>, LootItemFunction> constructor;

      public DummyBuilder(Function<List<LootItemCondition>, LootItemFunction> $$0) {
         this.constructor = $$0;
      }

      protected LootItemConditionalFunction.DummyBuilder getThis() {
         return this;
      }

      @Override
      public LootItemFunction build() {
         return this.constructor.apply(this.getConditions());
      }
   }
}
