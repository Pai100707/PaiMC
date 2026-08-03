package net.minecraft.advancements.criterion;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public record StatePropertiesPredicate(List<StatePropertiesPredicate.PropertyMatcher> properties) {
   private static final Codec<List<StatePropertiesPredicate.PropertyMatcher>> PROPERTIES_CODEC = Codec.unboundedMap(
         Codec.STRING, StatePropertiesPredicate.ValueMatcher.CODEC
      )
      .xmap(
         $$0 -> $$0.entrySet()
            .stream()
            .map($$0x -> new StatePropertiesPredicate.PropertyMatcher((String)$$0x.getKey(), (StatePropertiesPredicate.ValueMatcher)$$0x.getValue()))
            .toList(),
         $$0 -> $$0.stream().collect(Collectors.toMap(StatePropertiesPredicate.PropertyMatcher::name, StatePropertiesPredicate.PropertyMatcher::valueMatcher))
      );
   public static final Codec<StatePropertiesPredicate> CODEC = PROPERTIES_CODEC.xmap(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);
   public static final StreamCodec<ByteBuf, StatePropertiesPredicate> STREAM_CODEC = StatePropertiesPredicate.PropertyMatcher.STREAM_CODEC
      .apply(ByteBufCodecs.list())
      .map(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);

   public <S extends StateHolder<?, S>> boolean matches(StateDefinition<?, S> $$0, S $$1) {
      for (StatePropertiesPredicate.PropertyMatcher $$2 : this.properties) {
         if (!$$2.match($$0, $$1)) {
            return false;
         }
      }

      return true;
   }

   public boolean matches(BlockState $$0) {
      return this.matches($$0.getBlock().getStateDefinition(), $$0);
   }

   public boolean matches(FluidState $$0) {
      return this.matches($$0.getType().getStateDefinition(), $$0);
   }

   public Optional<String> checkState(StateDefinition<?, ?> $$0) {
      for (StatePropertiesPredicate.PropertyMatcher $$1 : this.properties) {
         Optional<String> $$2 = $$1.checkState($$0);
         if ($$2.isPresent()) {
            return $$2;
         }
      }

      return Optional.empty();
   }

   public static class Builder {
      private final com.google.common.collect.ImmutableList.Builder<StatePropertiesPredicate.PropertyMatcher> matchers = ImmutableList.builder();

      private Builder() {
      }

      public static StatePropertiesPredicate.Builder properties() {
         return new StatePropertiesPredicate.Builder();
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<?> $$0, String $$1) {
         this.matchers.add(new StatePropertiesPredicate.PropertyMatcher($$0.getName(), new StatePropertiesPredicate.ExactMatcher($$1)));
         return this;
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<Integer> $$0, int $$1) {
         return this.hasProperty($$0, Integer.toString($$1));
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<Boolean> $$0, boolean $$1) {
         return this.hasProperty($$0, Boolean.toString($$1));
      }

      public <T extends Comparable<T> & StringRepresentable> StatePropertiesPredicate.Builder hasProperty(Property<T> $$0, T $$1) {
         return this.hasProperty($$0, $$1.getSerializedName());
      }

      public Optional<StatePropertiesPredicate> build() {
         return Optional.of(new StatePropertiesPredicate(this.matchers.build()));
      }
   }

   record ExactMatcher(String value) implements StatePropertiesPredicate.ValueMatcher {
      public static final Codec<StatePropertiesPredicate.ExactMatcher> CODEC = Codec.STRING
         .xmap(StatePropertiesPredicate.ExactMatcher::new, StatePropertiesPredicate.ExactMatcher::value);
      public static final StreamCodec<ByteBuf, StatePropertiesPredicate.ExactMatcher> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
         .map(StatePropertiesPredicate.ExactMatcher::new, StatePropertiesPredicate.ExactMatcher::value);

      @Override
      public <T extends Comparable<T>> boolean match(StateHolder<?, ?> $$0, Property<T> $$1) {
         T $$2 = (T)$$0.getValue($$1);
         Optional<T> $$3 = $$1.getValue(this.value);
         return $$3.isPresent() && $$2.compareTo($$3.get()) == 0;
      }
   }

   record PropertyMatcher(String name, StatePropertiesPredicate.ValueMatcher valueMatcher) {
      public static final StreamCodec<ByteBuf, StatePropertiesPredicate.PropertyMatcher> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.STRING_UTF8,
         StatePropertiesPredicate.PropertyMatcher::name,
         StatePropertiesPredicate.ValueMatcher.STREAM_CODEC,
         StatePropertiesPredicate.PropertyMatcher::valueMatcher,
         StatePropertiesPredicate.PropertyMatcher::new
      );

      public <S extends StateHolder<?, S>> boolean match(StateDefinition<?, S> $$0, S $$1) {
         Property<?> $$2 = $$0.getProperty(this.name);
         return $$2 != null && this.valueMatcher.match($$1, $$2);
      }

      public Optional<String> checkState(StateDefinition<?, ?> $$0) {
         Property<?> $$1 = $$0.getProperty(this.name);
         return $$1 != null ? Optional.empty() : Optional.of(this.name);
      }
   }

   record RangedMatcher(Optional<String> minValue, Optional<String> maxValue) implements StatePropertiesPredicate.ValueMatcher {
      public static final Codec<StatePropertiesPredicate.RangedMatcher> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.STRING.optionalFieldOf("min").forGetter(StatePropertiesPredicate.RangedMatcher::minValue),
               Codec.STRING.optionalFieldOf("max").forGetter(StatePropertiesPredicate.RangedMatcher::maxValue)
            )
            .apply($$0, StatePropertiesPredicate.RangedMatcher::new)
      );
      public static final StreamCodec<ByteBuf, StatePropertiesPredicate.RangedMatcher> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
         StatePropertiesPredicate.RangedMatcher::minValue,
         ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
         StatePropertiesPredicate.RangedMatcher::maxValue,
         StatePropertiesPredicate.RangedMatcher::new
      );

      @Override
      public <T extends Comparable<T>> boolean match(StateHolder<?, ?> $$0, Property<T> $$1) {
         T $$2 = (T)$$0.getValue($$1);
         if (this.minValue.isPresent()) {
            Optional<T> $$3 = $$1.getValue(this.minValue.get());
            if ($$3.isEmpty() || $$2.compareTo($$3.get()) < 0) {
               return false;
            }
         }

         if (this.maxValue.isPresent()) {
            Optional<T> $$4 = $$1.getValue(this.maxValue.get());
            if ($$4.isEmpty() || $$2.compareTo($$4.get()) > 0) {
               return false;
            }
         }

         return true;
      }
   }

   interface ValueMatcher {
      Codec<StatePropertiesPredicate.ValueMatcher> CODEC = Codec.either(
            StatePropertiesPredicate.ExactMatcher.CODEC, StatePropertiesPredicate.RangedMatcher.CODEC
         )
         .xmap(Either::unwrap, $$0 -> {
            if ($$0 instanceof StatePropertiesPredicate.ExactMatcher $$1) {
               return Either.left($$1);
            } else if ($$0 instanceof StatePropertiesPredicate.RangedMatcher $$2) {
               return Either.right($$2);
            } else {
               throw new UnsupportedOperationException();
            }
         });
      StreamCodec<ByteBuf, StatePropertiesPredicate.ValueMatcher> STREAM_CODEC = ByteBufCodecs.either(
            StatePropertiesPredicate.ExactMatcher.STREAM_CODEC, StatePropertiesPredicate.RangedMatcher.STREAM_CODEC
         )
         .map(Either::unwrap, $$0 -> {
            if ($$0 instanceof StatePropertiesPredicate.ExactMatcher $$1) {
               return Either.left($$1);
            } else if ($$0 instanceof StatePropertiesPredicate.RangedMatcher $$2) {
               return Either.right($$2);
            } else {
               throw new UnsupportedOperationException();
            }
         });

      <T extends Comparable<T>> boolean match(StateHolder<?, ?> var1, Property<T> var2);
   }
}
