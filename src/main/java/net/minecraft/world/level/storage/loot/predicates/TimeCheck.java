package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;

public record TimeCheck(Optional<Long> period, IntRange value) implements LootItemCondition {
   public static final MapCodec<TimeCheck> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(Codec.LONG.optionalFieldOf("period").forGetter(TimeCheck::period), IntRange.CODEC.fieldOf("value").forGetter(TimeCheck::value))
         .apply($$0, TimeCheck::new)
   );

   @Override
   public LootItemConditionType getType() {
      return LootItemConditions.TIME_CHECK;
   }

   @Override
   public Set<ContextKey<?>> getReferencedContextParams() {
      return this.value.getReferencedContextParams();
   }

   public boolean test(LootContext $$0) {
      ServerLevel $$1 = $$0.getLevel();
      long $$2 = $$1.getDayTime();
      if (this.period.isPresent()) {
         $$2 %= this.period.get();
      }

      return this.value.test($$0, (int)$$2);
   }

   public static TimeCheck.Builder time(IntRange $$0) {
      return new TimeCheck.Builder($$0);
   }

   public static class Builder implements LootItemCondition.Builder {
      private Optional<Long> period = Optional.empty();
      private final IntRange value;

      public Builder(IntRange $$0) {
         this.value = $$0;
      }

      public TimeCheck.Builder setPeriod(long $$0) {
         this.period = Optional.of($$0);
         return this;
      }

      public TimeCheck build() {
         return new TimeCheck(this.period, this.value);
      }
   }
}
