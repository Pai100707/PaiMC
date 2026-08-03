package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount extends LootItemConditionalFunction {
   public static final MapCodec<LimitCount> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> commonFields($$0).and(IntRange.CODEC.fieldOf("limit").forGetter($$0x -> $$0x.limiter)).apply($$0, LimitCount::new)
   );
   private final IntRange limiter;

   private LimitCount(List<LootItemCondition> $$0, IntRange $$1) {
      super($$0);
      this.limiter = $$1;
   }

   @Override
   public LootItemFunctionType<LimitCount> getType() {
      return LootItemFunctions.LIMIT_COUNT;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.limiter.getReferencedContextParams();
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      int $$2 = this.limiter.clamp($$1, $$0.getCount());
      $$0.setCount($$2);
      return $$0;
   }

   public static LootItemConditionalFunction.Builder<?> limitCount(IntRange $$0) {
      return simpleBuilder($$1 -> new LimitCount($$1, $$0));
   }
}
