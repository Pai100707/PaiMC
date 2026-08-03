package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntryGroup extends CompositeEntryBase {
   public static final MapCodec<EntryGroup> CODEC = createCodec(EntryGroup::new);

   EntryGroup(List<LootPoolEntryContainer> $$0, List<LootItemCondition> $$1) {
      super($$0, $$1);
   }

   @Override
   public LootPoolEntryType getType() {
      return LootPoolEntries.GROUP;
   }

   @Override
   protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> $$0) {
      return switch ($$0.size()) {
         case 0 -> ALWAYS_TRUE;
         case 1 -> (ComposableEntryContainer)$$0.get(0);
         case 2 -> {
            ComposableEntryContainer $$1 = $$0.get(0);
            ComposableEntryContainer $$2 = $$0.get(1);
            yield ($$2x, $$3) -> {
               $$1.expand($$2x, $$3);
               $$2.expand($$2x, $$3);
               return true;
            };
         }
         default -> ($$1x, $$2x) -> {
            for (ComposableEntryContainer $$3 : $$0) {
               $$3.expand($$1x, $$2x);
            }

            return true;
         };
      };
   }

   public static EntryGroup.Builder list(LootPoolEntryContainer.Builder<?>... $$0) {
      return new EntryGroup.Builder($$0);
   }

   public static class Builder extends LootPoolEntryContainer.Builder<EntryGroup.Builder> {
      private final com.google.common.collect.ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();

      public Builder(LootPoolEntryContainer.Builder<?>... $$0) {
         for (LootPoolEntryContainer.Builder<?> $$1 : $$0) {
            this.entries.add($$1.build());
         }
      }

      protected EntryGroup.Builder getThis() {
         return this;
      }

      @Override
      public EntryGroup.Builder append(LootPoolEntryContainer.Builder<?> $$0) {
         this.entries.add($$0.build());
         return this;
      }

      @Override
      public LootPoolEntryContainer build() {
         return new EntryGroup(this.entries.build(), this.getConditions());
      }
   }
}
