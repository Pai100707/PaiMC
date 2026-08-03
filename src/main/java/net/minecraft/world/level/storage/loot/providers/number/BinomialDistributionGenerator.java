package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;

public record BinomialDistributionGenerator(NumberProvider n, NumberProvider p) implements NumberProvider {
   public static final MapCodec<BinomialDistributionGenerator> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            NumberProviders.CODEC.fieldOf("n").forGetter(BinomialDistributionGenerator::n),
            NumberProviders.CODEC.fieldOf("p").forGetter(BinomialDistributionGenerator::p)
         )
         .apply($$0, BinomialDistributionGenerator::new)
   );

   @Override
   public LootNumberProviderType getType() {
      return NumberProviders.BINOMIAL;
   }

   @Override
   public int getInt(LootContext $$0) {
      int $$1 = this.n.getInt($$0);
      float $$2 = this.p.getFloat($$0);
      RandomSource $$3 = $$0.getRandom();
      int $$4 = 0;

      for (int $$5 = 0; $$5 < $$1; $$5++) {
         if ($$3.nextFloat() < $$2) {
            $$4++;
         }
      }

      return $$4;
   }

   @Override
   public float getFloat(LootContext $$0) {
      return this.getInt($$0);
   }

   public static BinomialDistributionGenerator binomial(int $$0, float $$1) {
      return new BinomialDistributionGenerator(ConstantValue.exactly($$0), ConstantValue.exactly($$1));
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Sets.union(this.n.getReferencedContextParams(), this.p.getReferencedContextParams());
   }
}
