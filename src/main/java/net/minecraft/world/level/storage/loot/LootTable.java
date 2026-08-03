package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<ResourceKey<LootTable>> KEY_CODEC = ResourceKey.codec(Registries.LOOT_TABLE);
   public static final ContextKeySet DEFAULT_PARAM_SET = LootContextParamSets.ALL_PARAMS;
   public static final long RANDOMIZE_SEED = 0L;
   public static final Codec<LootTable> DIRECT_CODEC = Codec.lazyInitialized(
      () -> RecordCodecBuilder.create(
         $$0 -> $$0.group(
               LootContextParamSets.CODEC.lenientOptionalFieldOf("type", DEFAULT_PARAM_SET).forGetter($$0x -> $$0x.paramSet),
               Identifier.CODEC.optionalFieldOf("random_sequence").forGetter($$0x -> $$0x.randomSequence),
               LootPool.CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter($$0x -> $$0x.pools),
               LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter($$0x -> $$0x.functions)
            )
            .apply($$0, LootTable::new)
      )
   );
   public static final Codec<Holder<LootTable>> CODEC = RegistryFileCodec.create(Registries.LOOT_TABLE, DIRECT_CODEC);
   public static final LootTable EMPTY = new LootTable(LootContextParamSets.EMPTY, Optional.empty(), List.of(), List.of());
   private final ContextKeySet paramSet;
   private final Optional<Identifier> randomSequence;
   private final List<LootPool> pools;
   private final List<LootItemFunction> functions;
   private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

   LootTable(ContextKeySet $$0, Optional<Identifier> $$1, List<LootPool> $$2, List<LootItemFunction> $$3) {
      this.paramSet = $$0;
      this.randomSequence = $$1;
      this.pools = $$2;
      this.functions = $$3;
      this.compositeFunction = LootItemFunctions.compose($$3);
   }

   public static Consumer<ItemStack> createStackSplitter(ServerLevel $$0, Consumer<ItemStack> $$1) {
      return $$2 -> {
         if ($$2.isItemEnabled($$0.enabledFeatures())) {
            if ($$2.getCount() < $$2.getMaxStackSize()) {
               $$1.accept($$2);
            } else {
               int $$3 = $$2.getCount();

               while ($$3 > 0) {
                  ItemStack $$4 = $$2.copyWithCount(Math.min($$2.getMaxStackSize(), $$3));
                  $$3 -= $$4.getCount();
                  $$1.accept($$4);
               }
            }
         }
      };
   }

   public void getRandomItemsRaw(LootParams $$0, Consumer<ItemStack> $$1) {
      this.getRandomItemsRaw(new LootContext.Builder($$0).create(this.randomSequence), $$1);
   }

   public void getRandomItemsRaw(LootContext $$0, Consumer<ItemStack> $$1) {
      LootContext.VisitedEntry<?> $$2 = LootContext.createVisitedEntry(this);
      if ($$0.pushVisitedElement($$2)) {
         Consumer<ItemStack> $$3 = LootItemFunction.decorate(this.compositeFunction, $$1, $$0);

         for (LootPool $$4 : this.pools) {
            $$4.addRandomItems($$3, $$0);
         }

         $$0.popVisitedElement($$2);
      } else {
         LOGGER.warn("Detected infinite loop in loot tables");
      }
   }

   public void getRandomItems(LootParams $$0, long $$1, Consumer<ItemStack> $$2) {
      this.getRandomItemsRaw(new LootContext.Builder($$0).withOptionalRandomSeed($$1).create(this.randomSequence), createStackSplitter($$0.getLevel(), $$2));
   }

   public void getRandomItems(LootParams $$0, Consumer<ItemStack> $$1) {
      this.getRandomItemsRaw($$0, createStackSplitter($$0.getLevel(), $$1));
   }

   public void getRandomItems(LootContext $$0, Consumer<ItemStack> $$1) {
      this.getRandomItemsRaw($$0, createStackSplitter($$0.getLevel(), $$1));
   }

   public ObjectArrayList<ItemStack> getRandomItems(LootParams $$0, RandomSource $$1) {
      return this.getRandomItems(new LootContext.Builder($$0).withOptionalRandomSource($$1).create(this.randomSequence));
   }

   public ObjectArrayList<ItemStack> getRandomItems(LootParams $$0, long $$1) {
      return this.getRandomItems(new LootContext.Builder($$0).withOptionalRandomSeed($$1).create(this.randomSequence));
   }

   public ObjectArrayList<ItemStack> getRandomItems(LootParams $$0) {
      return this.getRandomItems(new LootContext.Builder($$0).create(this.randomSequence));
   }

   private ObjectArrayList<ItemStack> getRandomItems(LootContext $$0) {
      ObjectArrayList<ItemStack> $$1 = new ObjectArrayList();
      this.getRandomItems($$0, $$1::add);
      return $$1;
   }

   public ContextKeySet getParamSet() {
      return this.paramSet;
   }

   public void validate(ValidationContext $$0) {
      for (int $$1 = 0; $$1 < this.pools.size(); $$1++) {
         this.pools.get($$1).validate($$0.forChild(new IndexedFieldPathElement("pools", $$1)));
      }

      for (int $$2 = 0; $$2 < this.functions.size(); $$2++) {
         this.functions.get($$2).validate($$0.forChild(new IndexedFieldPathElement("functions", $$2)));
      }
   }

   public void fill(Container $$0, LootParams $$1, long $$2) {
      LootContext $$3 = new LootContext.Builder($$1).withOptionalRandomSeed($$2).create(this.randomSequence);
      ObjectArrayList<ItemStack> $$4 = this.getRandomItems($$3);
      RandomSource $$5 = $$3.getRandom();
      List<Integer> $$6 = this.getAvailableSlots($$0, $$5);
      this.shuffleAndSplitItems($$4, $$6.size(), $$5);
      ObjectListIterator var9 = $$4.iterator();

      while (var9.hasNext()) {
         ItemStack $$7 = (ItemStack)var9.next();
         if ($$6.isEmpty()) {
            LOGGER.warn("Tried to over-fill a container");
            return;
         }

         if ($$7.isEmpty()) {
            $$0.setItem($$6.remove($$6.size() - 1), ItemStack.EMPTY);
         } else {
            $$0.setItem($$6.remove($$6.size() - 1), $$7);
         }
      }
   }

   private void shuffleAndSplitItems(ObjectArrayList<ItemStack> $$0, int $$1, RandomSource $$2) {
      List<ItemStack> $$3 = Lists.newArrayList();
      Iterator<ItemStack> $$4 = $$0.iterator();

      while ($$4.hasNext()) {
         ItemStack $$5 = $$4.next();
         if ($$5.isEmpty()) {
            $$4.remove();
         } else if ($$5.getCount() > 1) {
            $$3.add($$5);
            $$4.remove();
         }
      }

      while ($$1 - $$0.size() - $$3.size() > 0 && !$$3.isEmpty()) {
         ItemStack $$6 = $$3.remove(Mth.nextInt($$2, 0, $$3.size() - 1));
         int $$7 = Mth.nextInt($$2, 1, $$6.getCount() / 2);
         ItemStack $$8 = $$6.split($$7);
         if ($$6.getCount() > 1 && $$2.nextBoolean()) {
            $$3.add($$6);
         } else {
            $$0.add($$6);
         }

         if ($$8.getCount() > 1 && $$2.nextBoolean()) {
            $$3.add($$8);
         } else {
            $$0.add($$8);
         }
      }

      $$0.addAll($$3);
      Util.shuffle($$0, $$2);
   }

   private List<Integer> getAvailableSlots(Container $$0, RandomSource $$1) {
      ObjectArrayList<Integer> $$2 = new ObjectArrayList();

      for (int $$3 = 0; $$3 < $$0.getContainerSize(); $$3++) {
         if ($$0.getItem($$3).isEmpty()) {
            $$2.add($$3);
         }
      }

      Util.shuffle($$2, $$1);
      return $$2;
   }

   public static LootTable.Builder lootTable() {
      return new LootTable.Builder();
   }

   public static class Builder implements FunctionUserBuilder<LootTable.Builder> {
      private final com.google.common.collect.ImmutableList.Builder<LootPool> pools = ImmutableList.builder();
      private final com.google.common.collect.ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();
      private ContextKeySet paramSet = LootTable.DEFAULT_PARAM_SET;
      private Optional<Identifier> randomSequence = Optional.empty();

      public LootTable.Builder withPool(LootPool.Builder $$0) {
         this.pools.add($$0.build());
         return this;
      }

      public LootTable.Builder setParamSet(ContextKeySet $$0) {
         this.paramSet = $$0;
         return this;
      }

      public LootTable.Builder setRandomSequence(Identifier $$0) {
         this.randomSequence = Optional.of($$0);
         return this;
      }

      public LootTable.Builder apply(LootItemFunction.Builder $$0) {
         this.functions.add($$0.build());
         return this;
      }

      public LootTable.Builder unwrap() {
         return this;
      }

      public LootTable build() {
         return new LootTable(this.paramSet, this.randomSequence, this.pools.build(), this.functions.build());
      }
   }
}
