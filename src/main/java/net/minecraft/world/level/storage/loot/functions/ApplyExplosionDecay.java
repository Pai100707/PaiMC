package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ApplyExplosionDecay extends LootItemConditionalFunction {
   public static final MapCodec<ApplyExplosionDecay> CODEC = RecordCodecBuilder.mapCodec($$0 -> commonFields($$0).apply($$0, ApplyExplosionDecay::new));

   private ApplyExplosionDecay(List<LootItemCondition> $$0) {
      super($$0);
   }

   @Override
   public LootItemFunctionType<ApplyExplosionDecay> getType() {
      return LootItemFunctions.EXPLOSION_DECAY;
   }

   @Override
   public ItemStack run(ItemStack $$0, LootContext $$1) {
      Float $$2 = $$1.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
      if ($$2 != null) {
         RandomSource $$3 = $$1.getRandom();
         float $$4 = 1.0F / $$2;
         int $$5 = $$0.getCount();
         int $$6 = 0;

         for (int $$7 = 0; $$7 < $$5; $$7++) {
            if ($$3.nextFloat() <= $$4) {
               $$6++;
            }
         }

         $$0.setCount($$6);
      }

      return $$0;
   }

   public static LootItemConditionalFunction.Builder<?> explosionDecay() {
      return simpleBuilder(ApplyExplosionDecay::new);
   }
}
