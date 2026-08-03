package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SequentialEntry extends CompositeEntryBase {
   public static final MapCodec<SequentialEntry> CODEC = createCodec(SequentialEntry::new);

   SequentialEntry(List<LootPoolEntryContainer> $$0, List<LootItemCondition> $$1) {
      super($$0, $$1);
   }

   @Override
   public LootPoolEntryType getType() {
      return LootPoolEntries.SEQUENCE;
   }

   @Override
   protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> $$0) {
      return switch ($$0.size()) {
         case 0 -> ALWAYS_TRUE;
         case 1 -> (ComposableEntryContainer)$$0.get(0);
         case 2 -> $$0.get(0).and($$0.get(1));
         default -> ($$1, $$2) -> {
            for (ComposableEntryContainer $$3 : $$0) {
               if (!$$3.expand($$1, $$2)) {
                  return false;
               }
            }

            return true;
         };
      };
   }

   public static SequentialEntry.Builder sequential(LootPoolEntryContainer.Builder<?>... $$0) {
      return new SequentialEntry.Builder($$0);
   }

   public static class Builder extends LootPoolEntryContainer.Builder<SequentialEntry.Builder> {
      private final com.google.common.collect.ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();

      public Builder(LootPoolEntryContainer.Builder<?>... $$0) {
         for (LootPoolEntryContainer.Builder<?> $$1 : $$0) {
            this.entries.add($$1.build());
         }
      }

      protected SequentialEntry.Builder getThis() {
         return this;
      }

      @Override
      public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> $$0) {
         this.entries.add($$0.build());
         return this;
      }

      @Override
      public LootPoolEntryContainer build() {
         return new SequentialEntry(this.entries.build(), this.getConditions());
      }
   }
}
