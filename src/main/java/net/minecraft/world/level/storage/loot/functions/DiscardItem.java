package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DiscardItem extends LootItemConditionalFunction {
   public static final MapCodec<DiscardItem> CODEC = RecordCodecBuilder.mapCodec($$0 -> commonFields($$0).apply($$0, DiscardItem::new));

   protected DiscardItem(List<LootItemCondition> $$0) {
      super($$0);
   }

   @Override
   public LootItemFunctionType<DiscardItem> getType() {
      return LootItemFunctions.DISCARD;
   }

   @Override
   protected ItemStack run(ItemStack $$0, LootContext $$1) {
      return ItemStack.EMPTY;
   }

   public static LootItemConditionalFunction.Builder<?> discardItem() {
      return simpleBuilder(DiscardItem::new);
   }
}
