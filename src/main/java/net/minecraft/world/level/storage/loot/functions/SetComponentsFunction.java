package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetComponentsFunction extends LootItemConditionalFunction {
   public static final MapCodec<SetComponentsFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0).and(DataComponentPatch.CODEC.fieldOf("components").forGetter($$0x -> $$0x.components)).apply($$0, SetComponentsFunction::new)
   );
   private final DataComponentPatch components;

   private SetComponentsFunction(List<LootItemCondition> $$0, DataComponentPatch $$1) {
      super($$0);
      this.components = $$1;
   }

   @Override
   public LootItemFunctionType<SetComponentsFunction> getType() {
      return LootItemFunctions.SET_COMPONENTS;
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      $$0.applyComponentsAndValidate(this.components);
      return $$0;
   }

   public static <T> LootItemConditionalFunction.Builder<?> setComponent(DataComponentType<T> $$0, T $$1) {
      return simpleBuilder($$2 -> new SetComponentsFunction($$2, DataComponentPatch.builder().set($$0, $$1).build()));
   }
}
