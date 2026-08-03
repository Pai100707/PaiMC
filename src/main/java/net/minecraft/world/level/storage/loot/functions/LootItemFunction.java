package net.minecraft.world.level.storage.loot.functions;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextUser;

public interface LootItemFunction extends LootContextUser, BiFunction<ItemStack, LootContext, ItemStack> {
   LootItemFunctionType<? extends LootItemFunction> getType();

   static Consumer<ItemStack> decorate(BiFunction<ItemStack, LootContext, ItemStack> $$0, Consumer<ItemStack> $$1, LootContext $$2) {
      return $$3 -> $$1.accept($$0.apply($$3, $$2));
   }

   public interface Builder {
      LootItemFunction build();
   }
}
