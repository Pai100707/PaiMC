package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerContents extends LootItemConditionalFunction {
   public static final MapCodec<SetContainerContents> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(
               ContainerComponentManipulators.CODEC.fieldOf("component").forGetter($$0x -> $$0x.component),
               LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter($$0x -> $$0x.entries)
            )
         )
         .apply($$0, SetContainerContents::new)
   );
   private final ContainerComponentManipulator<?> component;
   private final List<LootPoolEntryContainer> entries;

   SetContainerContents(List<LootItemCondition> $$0, ContainerComponentManipulator<?> $$1, List<LootPoolEntryContainer> $$2) {
      super($$0);
      this.component = $$1;
      this.entries = List.copyOf($$2);
   }

   @Override
   public LootItemFunctionType<SetContainerContents> getType() {
      return LootItemFunctions.SET_CONTENTS;
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      if ($$0.isEmpty()) {
         return $$0;
      } else {
         Stream.Builder<ItemStack> $$2 = Stream.builder();
         this.entries.forEach($$2x -> $$2x.expand($$1, $$2xx -> $$2xx.createItemStack(LootTable.createStackSplitter($$1.getLevel(), $$2::add), $$1)));
         this.component.setContents($$0, $$2.build());
         return $$0;
      }
   }

   @Override
   public void validate(ValidationContext $$0) {
      super.validate($$0);

      for (int $$1 = 0; $$1 < this.entries.size(); $$1++) {
         this.entries.get($$1).validate($$0.forChild(new IndexedFieldPathElement("entries", $$1)));
      }
   }

   public static SetContainerContents.Builder setContents(ContainerComponentManipulator<?> $$0) {
      return new SetContainerContents.Builder($$0);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetContainerContents.Builder> {
      private final com.google.common.collect.ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();
      private final ContainerComponentManipulator<?> component;

      public Builder(ContainerComponentManipulator<?> $$0) {
         this.component = $$0;
      }

      protected SetContainerContents.Builder getThis() {
         return this;
      }

      public SetContainerContents.Builder withEntry(LootPoolEntryContainer.Builder<?> $$0) {
         this.entries.add($$0.build());
         return this;
      }

      @Override
      public LootItemFunction build() {
         return new SetContainerContents(this.getConditions(), this.component, this.entries.build());
      }
   }
}
