package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products.P4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootPoolSingletonContainer extends LootPoolEntryContainer {
   public static final int DEFAULT_WEIGHT = 1;
   public static final int DEFAULT_QUALITY = 0;
   protected final int weight;
   protected final int quality;
   protected final List<LootItemFunction> functions;
   final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
   private final LootPoolEntry entry = new LootPoolSingletonContainer.EntryBase() {
      @Override
      public void createItemStack(Consumer<ItemStack> $$0, LootContext $$1) {
         LootPoolSingletonContainer.this.createItemStack(LootItemFunction.decorate(LootPoolSingletonContainer.this.compositeFunction, $$0, $$1), $$1);
      }
   };

   protected LootPoolSingletonContainer(int $$0, int $$1, List<LootItemCondition> $$2, List<LootItemFunction> $$3) {
      super($$2);
      this.weight = $$0;
      this.quality = $$1;
      this.functions = $$3;
      this.compositeFunction = LootItemFunctions.compose($$3);
   }

   protected static <T extends LootPoolSingletonContainer> P4<Mu<T>, Integer, Integer, List<LootItemCondition>, List<LootItemFunction>> singletonFields(
      Instance<T> $$0
   ) {
      return $$0.group(
            Codec.INT.optionalFieldOf("weight", 1).forGetter($$0x -> $$0x.weight), Codec.INT.optionalFieldOf("quality", 0).forGetter($$0x -> $$0x.quality)
         )
         .and(commonFields($$0).t1())
         .and(LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter($$0x -> $$0x.functions));
   }

   @Override
   public void validate(ValidationContext $$0) {
      super.validate($$0);

      for (int $$1 = 0; $$1 < this.functions.size(); $$1++) {
         this.functions.get($$1).validate($$0.forChild(new IndexedFieldPathElement("functions", $$1)));
      }
   }

   protected abstract void createItemStack(Consumer<ItemStack> var1, LootContext var2);

   @Override
   public boolean expand(LootContext $$0, Consumer<LootPoolEntry> $$1) {
      if (this.canRun($$0)) {
         $$1.accept(this.entry);
         return true;
      } else {
         return false;
      }
   }

   public static LootPoolSingletonContainer.Builder<?> simpleBuilder(LootPoolSingletonContainer.EntryConstructor $$0) {
      return new LootPoolSingletonContainer.DummyBuilder($$0);
   }

   public abstract static class Builder<T extends LootPoolSingletonContainer.Builder<T>>
      extends LootPoolEntryContainer.Builder<T>
      implements FunctionUserBuilder<T> {
      protected int weight = 1;
      protected int quality = 0;
      private final com.google.common.collect.ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();

      public T apply(LootItemFunction.Builder $$0) {
         this.functions.add($$0.build());
         return this.getThis();
      }

      protected List<LootItemFunction> getFunctions() {
         return this.functions.build();
      }

      public T setWeight(int $$0) {
         this.weight = $$0;
         return this.getThis();
      }

      public T setQuality(int $$0) {
         this.quality = $$0;
         return this.getThis();
      }
   }

   static class DummyBuilder extends LootPoolSingletonContainer.Builder<LootPoolSingletonContainer.DummyBuilder> {
      private final LootPoolSingletonContainer.EntryConstructor constructor;

      public DummyBuilder(LootPoolSingletonContainer.EntryConstructor $$0) {
         this.constructor = $$0;
      }

      protected LootPoolSingletonContainer.DummyBuilder getThis() {
         return this;
      }

      @Override
      public LootPoolEntryContainer build() {
         return this.constructor.build(this.weight, this.quality, this.getConditions(), this.getFunctions());
      }
   }

   protected abstract class EntryBase implements LootPoolEntry {
      @Override
      public int getWeight(float $$0) {
         return Math.max(Mth.floor(LootPoolSingletonContainer.this.weight + LootPoolSingletonContainer.this.quality * $$0), 0);
      }
   }

   @FunctionalInterface
   protected interface EntryConstructor {
      LootPoolSingletonContainer build(int var1, int var2, List<LootItemCondition> var3, List<LootItemFunction> var4);
   }
}
