package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.jspecify.annotations.Nullable;

public class IntRange {
   private static final Codec<IntRange> RECORD_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            NumberProviders.CODEC.optionalFieldOf("min").forGetter($$0x -> Optional.ofNullable($$0x.min)),
            NumberProviders.CODEC.optionalFieldOf("max").forGetter($$0x -> Optional.ofNullable($$0x.max))
         )
         .apply($$0, IntRange::new)
   );
   public static final Codec<IntRange> CODEC = Codec.either(Codec.INT, RECORD_CODEC)
      .xmap($$0 -> (IntRange)$$0.map(IntRange::exact, Function.identity()), $$0 -> {
         OptionalInt $$1 = $$0.unpackExact();
         return $$1.isPresent() ? Either.left($$1.getAsInt()) : Either.right($$0);
      });
   @Nullable
   private final NumberProvider min;
   @Nullable
   private final NumberProvider max;
   private final IntRange.IntLimiter limiter;
   private final IntRange.IntChecker predicate;

   public Set<ContextKey<?>> getReferencedContextParams() {
      Builder<ContextKey<?>> $$0 = ImmutableSet.builder();
      if (this.min != null) {
         $$0.addAll(this.min.getReferencedContextParams());
      }

      if (this.max != null) {
         $$0.addAll(this.max.getReferencedContextParams());
      }

      return $$0.build();
   }

   private IntRange(Optional<NumberProvider> $$0, Optional<NumberProvider> $$1) {
      this($$0.orElse(null), $$1.orElse(null));
   }

   private IntRange(@Nullable NumberProvider $$0, @Nullable NumberProvider $$1) {
      this.min = $$0;
      this.max = $$1;
      if ($$0 == null) {
         if ($$1 == null) {
            this.limiter = ($$0x, $$1x) -> $$1x;
            this.predicate = ($$0x, $$1x) -> true;
         } else {
            this.limiter = ($$1x, $$2) -> Math.min($$1.getInt($$1x), $$2);
            this.predicate = ($$1x, $$2) -> $$2 <= $$1.getInt($$1x);
         }
      } else if ($$1 == null) {
         this.limiter = ($$1x, $$2) -> Math.max($$0.getInt($$1x), $$2);
         this.predicate = ($$1x, $$2) -> $$2 >= $$0.getInt($$1x);
      } else {
         this.limiter = ($$2, $$3) -> Mth.clamp($$3, $$0.getInt($$2), $$1.getInt($$2));
         this.predicate = ($$2, $$3) -> $$3 >= $$0.getInt($$2) && $$3 <= $$1.getInt($$2);
      }
   }

   public static IntRange exact(int $$0) {
      ConstantValue $$1 = ConstantValue.exactly($$0);
      return new IntRange(Optional.of($$1), Optional.of($$1));
   }

   public static IntRange range(int $$0, int $$1) {
      return new IntRange(Optional.of(ConstantValue.exactly($$0)), Optional.of(ConstantValue.exactly($$1)));
   }

   public static IntRange lowerBound(int $$0) {
      return new IntRange(Optional.of(ConstantValue.exactly($$0)), Optional.empty());
   }

   public static IntRange upperBound(int $$0) {
      return new IntRange(Optional.empty(), Optional.of(ConstantValue.exactly($$0)));
   }

   public int clamp(LootContext $$0, int $$1) {
      return this.limiter.apply($$0, $$1);
   }

   public boolean test(LootContext $$0, int $$1) {
      return this.predicate.test($$0, $$1);
   }

   private OptionalInt unpackExact() {
      return Objects.equals(this.min, this.max) && this.min instanceof ConstantValue $$0 && Math.floor($$0.value()) == $$0.value()
         ? OptionalInt.of((int)$$0.value())
         : OptionalInt.empty();
   }

   @FunctionalInterface
   interface IntChecker {
      boolean test(LootContext var1, int var2);
   }

   @FunctionalInterface
   interface IntLimiter {
      int apply(LootContext var1, int var2);
   }
}
