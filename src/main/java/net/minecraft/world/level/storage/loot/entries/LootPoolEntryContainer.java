package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.Util;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootPoolEntryContainer implements ComposableEntryContainer {
   protected final List<LootItemCondition> conditions;
   private final Predicate<LootContext> compositeCondition;

   protected LootPoolEntryContainer(List<LootItemCondition> $$0) {
      this.conditions = $$0;
      this.compositeCondition = Util.allOf($$0);
   }

   protected static <T extends LootPoolEntryContainer> P1<Mu<T>, List<LootItemCondition>> commonFields(Instance<T> $$0) {
      return $$0.group(LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter($$0x -> $$0x.conditions));
   }

   public void validate(ValidationContext $$0) {
      for (int $$1 = 0; $$1 < this.conditions.size(); $$1++) {
         this.conditions.get($$1).validate($$0.forChild(new IndexedFieldPathElement("conditions", $$1)));
      }
   }

   protected final boolean canRun(LootContext $$0) {
      return this.compositeCondition.test($$0);
   }

   public abstract LootPoolEntryType getType();

   public abstract static class Builder<T extends LootPoolEntryContainer.Builder<T>> implements ConditionUserBuilder<T> {
      private final com.google.common.collect.ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();

      protected abstract T getThis();

      public T when(LootItemCondition.Builder $$0) {
         this.conditions.add($$0.build());
         return this.getThis();
      }

      public final T unwrap() {
         return this.getThis();
      }

      protected List<LootItemCondition> getConditions() {
         return this.conditions.build();
      }

      public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> $$0) {
         return new AlternativesEntry.Builder(this, $$0);
      }

      public EntryGroup.Builder append(LootPoolEntryContainer.Builder<?> $$0) {
         return new EntryGroup.Builder(this, $$0);
      }

      public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> $$0) {
         return new SequentialEntry.Builder(this, $$0);
      }

      public abstract LootPoolEntryContainer build();
   }
}
