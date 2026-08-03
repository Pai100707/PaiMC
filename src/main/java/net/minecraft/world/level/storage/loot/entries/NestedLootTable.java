package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter.ElementReferencePathElement;
import net.minecraft.util.ProblemReporter.PathElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class NestedLootTable extends LootPoolSingletonContainer {
   public static final MapCodec<NestedLootTable> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Codec.either(LootTable.KEY_CODEC, LootTable.DIRECT_CODEC).fieldOf("value").forGetter($$0x -> $$0x.contents))
         .and(singletonFields($$0))
         .apply($$0, NestedLootTable::new)
   );
   public static final PathElement INLINE_LOOT_TABLE_PATH_ELEMENT = new PathElement() {
      public String get() {
         return "->{inline}";
      }
   };
   private final Either<ResourceKey<LootTable>, LootTable> contents;

   private NestedLootTable(Either<ResourceKey<LootTable>, LootTable> $$0, int $$1, int $$2, List<LootItemCondition> $$3, List<LootItemFunction> $$4) {
      super($$1, $$2, $$3, $$4);
      this.contents = $$0;
   }

   @Override
   public LootPoolEntryType getType() {
      return LootPoolEntries.LOOT_TABLE;
   }

   @Override
   public void createItemStack(Consumer<ItemStack> $$0, LootContext $$1) {
      ((LootTable)this.contents.map($$1x -> $$1.getResolver().get($$1x).<LootTable>map(Holder::value).orElse(LootTable.EMPTY), $$0x -> $$0x))
         .getRandomItemsRaw($$1, $$0);
   }

   @Override
   public void validate(ValidationContext $$0) {
      Optional<ResourceKey<LootTable>> $$1 = this.contents.left();
      if ($$1.isPresent()) {
         ResourceKey<LootTable> $$2 = $$1.get();
         if (!$$0.allowsReferences()) {
            $$0.reportProblem(new ValidationContext.ReferenceNotAllowedProblem($$2));
            return;
         }

         if ($$0.hasVisitedElement($$2)) {
            $$0.reportProblem(new ValidationContext.RecursiveReferenceProblem($$2));
            return;
         }
      }

      super.validate($$0);
      this.contents
         .ifLeft(
            $$1x -> $$0.resolver()
               .get($$1x)
               .ifPresentOrElse(
                  $$2x -> ((LootTable)$$2x.value()).validate($$0.enterElement(new ElementReferencePathElement($$1x), $$1x)),
                  () -> $$0.reportProblem(new ValidationContext.MissingReferenceProblem($$1x))
               )
         )
         .ifRight($$1x -> $$1x.validate($$0.forChild(INLINE_LOOT_TABLE_PATH_ELEMENT)));
   }

   public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceKey<LootTable> $$0) {
      return simpleBuilder(($$1, $$2, $$3, $$4) -> new NestedLootTable(Either.left($$0), $$1, $$2, $$3, $$4));
   }

   public static LootPoolSingletonContainer.Builder<?> inlineLootTable(LootTable $$0) {
      return simpleBuilder(($$1, $$2, $$3, $$4) -> new NestedLootTable(Either.right($$0), $$1, $$2, $$3, $$4));
   }
}
