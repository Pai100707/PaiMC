package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;

public record UniformGenerator(NumberProvider min, NumberProvider max) implements NumberProvider {
   public static final MapCodec<UniformGenerator> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            NumberProviders.CODEC.fieldOf("min").forGetter(UniformGenerator::min), NumberProviders.CODEC.fieldOf("max").forGetter(UniformGenerator::max)
         )
         .apply($$0, UniformGenerator::new)
   );

   @Override
   public LootNumberProviderType getType() {
      return NumberProviders.UNIFORM;
   }

   public static UniformGenerator between(float $$0, float $$1) {
      return new UniformGenerator(ConstantValue.exactly($$0), ConstantValue.exactly($$1));
   }

   @Override
   public int getInt(LootContext $$0) {
      return Mth.nextInt($$0.getRandom(), this.min.getInt($$0), this.max.getInt($$0));
   }

   @Override
   public float getFloat(LootContext $$0) {
      return Mth.nextFloat($$0.getRandom(), this.min.getFloat($$0), this.max.getFloat($$0));
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return Sets.union(this.min.getReferencedContextParams(), this.max.getReferencedContextParams());
   }
}
