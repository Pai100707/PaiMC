package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;

public record WeatherCheck(Optional<Boolean> isRaining, Optional<Boolean> isThundering) implements LootItemCondition {
   public static final MapCodec<WeatherCheck> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.BOOL.optionalFieldOf("raining").forGetter(WeatherCheck::isRaining),
            Codec.BOOL.optionalFieldOf("thundering").forGetter(WeatherCheck::isThundering)
         )
         .apply($$0, WeatherCheck::new)
   );

   @Override
   public LootItemConditionType getType() {
      return LootItemConditions.WEATHER_CHECK;
   }

   public boolean test(LootContext $$0) {
      ServerLevel $$1 = $$0.getLevel();
      return this.isRaining.isPresent() && this.isRaining.get() != $$1.isRaining()
         ? false
         : !this.isThundering.isPresent() || this.isThundering.get() == $$1.isThundering();
   }

   public static WeatherCheck.Builder weather() {
      return new WeatherCheck.Builder();
   }

   public static class Builder implements LootItemCondition.Builder {
      private Optional<Boolean> isRaining = Optional.empty();
      private Optional<Boolean> isThundering = Optional.empty();

      public WeatherCheck.Builder setRaining(boolean $$0) {
         this.isRaining = Optional.of($$0);
         return this;
      }

      public WeatherCheck.Builder setThundering(boolean $$0) {
         this.isThundering = Optional.of($$0);
         return this;
      }

      public WeatherCheck build() {
         return new WeatherCheck(this.isRaining, this.isThundering);
      }
   }
}
