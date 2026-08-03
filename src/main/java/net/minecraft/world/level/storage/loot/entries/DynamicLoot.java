package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DynamicLoot extends LootPoolSingletonContainer {
   public static final MapCodec<DynamicLoot> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Identifier.CODEC.fieldOf("name").forGetter($$0x -> $$0x.name)).and(singletonFields($$0)).apply($$0, DynamicLoot::new)
   );
   private final Identifier name;

   private DynamicLoot(Identifier $$0, int $$1, int $$2, List<LootItemCondition> $$3, List<LootItemFunction> $$4) {
      super($$1, $$2, $$3, $$4);
      this.name = $$0;
   }

   @Override
   public LootPoolEntryType getType() {
      return LootPoolEntries.DYNAMIC;
   }

   @Override
   public void createItemStack(Consumer<ItemStack> $$0, LootContext $$1) {
      $$1.addDynamicDrops(this.name, $$0);
   }

   public static LootPoolSingletonContainer.Builder<?> dynamicEntry(Identifier $$0) {
      return simpleBuilder(($$1, $$2, $$3, $$4) -> new DynamicLoot($$0, $$1, $$2, $$3, $$4));
   }
}
