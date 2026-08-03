package net.minecraft.advancements.criterion;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.component.predicates.DataComponentPredicate.AnyValueType;
import net.minecraft.core.component.predicates.DataComponentPredicate.Type;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record DataComponentMatchers(DataComponentExactPredicate exact, Map<Type<?>, DataComponentPredicate> partial) implements Predicate<DataComponentGetter> {
   public static final DataComponentMatchers ANY = new DataComponentMatchers(DataComponentExactPredicate.EMPTY, Map.of());
   public static final MapCodec<DataComponentMatchers> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            DataComponentExactPredicate.CODEC.optionalFieldOf("components", DataComponentExactPredicate.EMPTY).forGetter(DataComponentMatchers::exact),
            DataComponentPredicate.CODEC.optionalFieldOf("predicates", Map.of()).forGetter(DataComponentMatchers::partial)
         )
         .apply($$0, DataComponentMatchers::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentMatchers> STREAM_CODEC = StreamCodec.composite(
      DataComponentExactPredicate.STREAM_CODEC,
      DataComponentMatchers::exact,
      DataComponentPredicate.STREAM_CODEC,
      DataComponentMatchers::partial,
      DataComponentMatchers::new
   );

   public boolean test(DataComponentGetter $$0) {
      if (!this.exact.test($$0)) {
         return false;
      } else {
         for (DataComponentPredicate $$1 : this.partial.values()) {
            if (!$$1.matches($$0)) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isEmpty() {
      return this.exact.isEmpty() && this.partial.isEmpty();
   }

   public static class Builder {
      private DataComponentExactPredicate exact = DataComponentExactPredicate.EMPTY;
      private final com.google.common.collect.ImmutableMap.Builder<Type<?>, DataComponentPredicate> partial = ImmutableMap.builder();

      private Builder() {
      }

      public static DataComponentMatchers.Builder components() {
         return new DataComponentMatchers.Builder();
      }

      public <T extends DataComponentType<?>> DataComponentMatchers.Builder any(DataComponentType<?> $$0) {
         AnyValueType $$1 = AnyValueType.create($$0);
         this.partial.put($$1, $$1.predicate());
         return this;
      }

      public <T extends DataComponentPredicate> DataComponentMatchers.Builder partial(Type<T> $$0, T $$1) {
         this.partial.put($$0, $$1);
         return this;
      }

      public DataComponentMatchers.Builder exact(DataComponentExactPredicate $$0) {
         this.exact = $$0;
         return this;
      }

      public DataComponentMatchers build() {
         return new DataComponentMatchers(this.exact, this.partial.buildOrThrow());
      }
   }
}
