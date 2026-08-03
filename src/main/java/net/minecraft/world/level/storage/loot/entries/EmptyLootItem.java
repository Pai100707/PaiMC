package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EmptyLootItem extends LootPoolSingletonContainer {
   public static final MapCodec<EmptyLootItem> CODEC = RecordCodecBuilder.mapCodec($$0 -> singletonFields($$0).apply($$0, EmptyLootItem::new));

   private EmptyLootItem(int $$0, int $$1, List<LootItemCondition> $$2, List<LootItemFunction> $$3) {
      super($$0, $$1, $$2, $$3);
   }

   @Override
   public LootPoolEntryType getType() {
      return LootPoolEntries.EMPTY;
   }

   @Override
   public void createItemStack(Consumer<ItemStack> $$0, LootContext $$1) {
   }

   public static LootPoolSingletonContainer.Builder<?> emptyItem() {
      return simpleBuilder(EmptyLootItem::new);
   }
}
