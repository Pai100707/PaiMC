package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextArg;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyNameFunction extends LootItemConditionalFunction {
   public static final MapCodec<CopyNameFunction> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0).and(LootContextArg.ENTITY_OR_BLOCK.fieldOf("source").forGetter($$0x -> $$0x.source)).apply($$0, CopyNameFunction::new)
   );
   private final LootContextArg<Object> source;

   private CopyNameFunction(List<LootItemCondition> $$0, LootContextArg<?> $$1) {
      super($$0);
      this.source = LootContextArg.cast((LootContextArg<? extends Object>)$$1);
   }

   @Override
   public LootItemFunctionType<CopyNameFunction> getType() {
      return LootItemFunctions.COPY_NAME;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Set.of(this.source.contextParam());
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      if (this.source.get($$1) instanceof Nameable $$3) {
         $$0.set(DataComponents.CUSTOM_NAME, $$3.getCustomName());
      }

      return $$0;
   }

   public static LootItemConditionalFunction.Builder<?> copyName(LootContextArg<?> $$0) {
      return simpleBuilder($$1 -> new CopyNameFunction($$1, $$0));
   }
}
