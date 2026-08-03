package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetItemCountFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetItemCountFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0)
         .and(
            $$0.group(NumberProviders.CODEC.fieldOf("count").forGetter($$0x -> $$0x.value), Codec.BOOL.fieldOf("add").orElse(false).forGetter($$0x -> $$0x.add))
         )
         .apply($$0, SetItemCountFunction::new)
   );
   private final NumberProvider value;
   private final boolean add;

   private SetItemCountFunction(List<LootItemCondition> $$0, NumberProvider $$1, boolean $$2) {
      super($$0);
      this.value = $$1;
      this.add = $$2;
   }

   @Override
   public LootItemFunctionType<SetItemCountFunction> getType() {
      return LootItemFunctions.SET_COUNT;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.value.getReferencedContextParams();
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      int $$2 = this.add ? $$0.getCount() : 0;
      $$0.setCount($$2 + this.value.getInt($$1));
      return $$0;
   }

   public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider $$0) {
      return simpleBuilder($$1 -> new SetItemCountFunction($$1, $$0, false));
   }

   public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider $$0, boolean $$1) {
      return simpleBuilder($$2 -> new SetItemCountFunction($$2, $$0, $$1));
   }
}
