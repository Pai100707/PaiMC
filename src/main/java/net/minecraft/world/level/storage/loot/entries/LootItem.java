package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItem extends LootPoolSingletonContainer {
   public static final MapCodec<LootItem> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Item.CODEC.fieldOf("name").forGetter($$0x -> $$0x.item)).and(singletonFields($$0)).apply($$0, LootItem::new)
   );
   private final Holder<Item> item;

   private LootItem(Holder<Item> $$0, int $$1, int $$2, List<LootItemCondition> $$3, List<LootItemFunction> $$4) {
      super($$1, $$2, $$3, $$4);
      this.item = $$0;
   }

   @Override
   public LootPoolEntryType getType() {
      return LootPoolEntries.ITEM;
   }

   @Override
   public void createItemStack(Consumer<ItemStack> $$0, LootContext $$1) {
      $$0.accept(new ItemStack(this.item));
   }

   public static LootPoolSingletonContainer.Builder<?> lootTableItem(net.minecraft.world.level.ItemLike $$0) {
      return simpleBuilder(($$1, $$2, $$3, $$4) -> new LootItem($$0.asItem().builtInRegistryHolder(), $$1, $$2, $$3, $$4));
   }
}
