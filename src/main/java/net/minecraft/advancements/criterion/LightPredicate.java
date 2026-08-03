package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public record LightPredicate(MinMaxBounds.Ints composite) {
   public static final Codec<LightPredicate> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("light", MinMaxBounds.Ints.ANY).forGetter(LightPredicate::composite))
         .apply($$0, LightPredicate::new)
   );

   public boolean matches(ServerLevel $$0, BlockPos $$1) {
      return !$$0.isLoaded($$1) ? false : this.composite.matches($$0.getMaxLocalRawBrightness($$1));
   }

   public static class Builder {
      private MinMaxBounds.Ints composite = MinMaxBounds.Ints.ANY;

      public static LightPredicate.Builder light() {
         return new LightPredicate.Builder();
      }

      public LightPredicate.Builder setComposite(MinMaxBounds.Ints $$0) {
         this.composite = $$0;
         return this;
      }

      public LightPredicate build() {
         return new LightPredicate(this.composite);
      }
   }
}
