package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.ProblemReporter.FieldPathElement;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool {
   public static final Codec<LootPool> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter($$0x -> $$0x.entries),
            LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter($$0x -> $$0x.conditions),
            LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter($$0x -> $$0x.functions),
            NumberProviders.CODEC.fieldOf("rolls").forGetter($$0x -> $$0x.rolls),
            NumberProviders.CODEC.fieldOf("bonus_rolls").orElse(ConstantValue.exactly(0.0F)).forGetter($$0x -> $$0x.bonusRolls)
         )
         .apply($$0, LootPool::new)
   );
   private final List<LootPoolEntryContainer> entries;
   private final List<LootItemCondition> conditions;
   private final Predicate<LootContext> compositeCondition;
   private final List<LootItemFunction> functions;
   private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
   private final NumberProvider rolls;
   private final NumberProvider bonusRolls;

   LootPool(List<LootPoolEntryContainer> $$0, List<LootItemCondition> $$1, List<LootItemFunction> $$2, NumberProvider $$3, NumberProvider $$4) {
      this.entries = $$0;
      this.conditions = $$1;
      this.compositeCondition = Util.allOf($$1);
      this.functions = $$2;
      this.compositeFunction = LootItemFunctions.compose($$2);
      this.rolls = $$3;
      this.bonusRolls = $$4;
   }

   private void addRandomItem(Consumer<ItemStack> $$0, LootContext $$1) {
      RandomSource $$2 = $$1.getRandom();
      List<LootPoolEntry> $$3 = Lists.newArrayList();
      MutableInt $$4 = new MutableInt();

      for (LootPoolEntryContainer $$5 : this.entries) {
         $$5.expand($$1, $$3x -> {
            int $$4x = $$3x.getWeight($$1.getLuck());
            if ($$4x > 0) {
               $$3.add($$3x);
               $$4.add($$4x);
            }
         });
      }

      int $$6 = $$3.size();
      if ($$4.intValue() != 0 && $$6 != 0) {
         if ($$6 == 1) {
            $$3.get(0).createItemStack($$0, $$1);
         } else {
            int $$7 = $$2.nextInt($$4.intValue());

            for (LootPoolEntry $$8 : $$3) {
               $$7 -= $$8.getWeight($$1.getLuck());
               if ($$7 < 0) {
                  $$8.createItemStack($$0, $$1);
                  return;
               }
            }
         }
      }
   }

   public void addRandomItems(Consumer<ItemStack> $$0, LootContext $$1) {
      if (this.compositeCondition.test($$1)) {
         Consumer<ItemStack> $$2 = LootItemFunction.decorate(this.compositeFunction, $$0, $$1);
         int $$3 = this.rolls.getInt($$1) + Mth.floor(this.bonusRolls.getFloat($$1) * $$1.getLuck());

         for (int $$4 = 0; $$4 < $$3; $$4++) {
            this.addRandomItem($$2, $$1);
         }
      }
   }

   public void validate(ValidationContext $$0) {
      for (int $$1 = 0; $$1 < this.conditions.size(); $$1++) {
         this.conditions.get($$1).validate($$0.forChild(new IndexedFieldPathElement("conditions", $$1)));
      }

      for (int $$2 = 0; $$2 < this.functions.size(); $$2++) {
         this.functions.get($$2).validate($$0.forChild(new IndexedFieldPathElement("functions", $$2)));
      }

      for (int $$3 = 0; $$3 < this.entries.size(); $$3++) {
         this.entries.get($$3).validate($$0.forChild(new IndexedFieldPathElement("entries", $$3)));
      }

      this.rolls.validate($$0.forChild(new FieldPathElement("rolls")));
      this.bonusRolls.validate($$0.forChild(new FieldPathElement("bonus_rolls")));
   }

   public static LootPool.Builder lootPool() {
      return new LootPool.Builder();
   }

   public static class Builder implements FunctionUserBuilder<LootPool.Builder>, ConditionUserBuilder<LootPool.Builder> {
      private final com.google.common.collect.ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();
      private final com.google.common.collect.ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();
      private final com.google.common.collect.ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();
      private NumberProvider rolls = ConstantValue.exactly(1.0F);
      private NumberProvider bonusRolls = ConstantValue.exactly(0.0F);

      public LootPool.Builder setRolls(NumberProvider $$0) {
         this.rolls = $$0;
         return this;
      }

      public LootPool.Builder unwrap() {
         return this;
      }

      public LootPool.Builder setBonusRolls(NumberProvider $$0) {
         this.bonusRolls = $$0;
         return this;
      }

      public LootPool.Builder add(LootPoolEntryContainer.Builder<?> $$0) {
         this.entries.add($$0.build());
         return this;
      }

      public LootPool.Builder when(LootItemCondition.Builder $$0) {
         this.conditions.add($$0.build());
         return this;
      }

      public LootPool.Builder apply(LootItemFunction.Builder $$0) {
         this.functions.add($$0.build());
         return this;
      }

      public LootPool build() {
         return new LootPool(this.entries.build(), this.conditions.build(), this.functions.build(), this.rolls, this.bonusRolls);
      }
   }
}
