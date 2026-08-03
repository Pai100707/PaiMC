package net.minecraft.server.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Filterable<T>(T raw, Optional<T> filtered) {
   public static <T> Codec<Filterable<T>> codec(Codec<T> $$0) {
      Codec<Filterable<T>> $$1 = RecordCodecBuilder.create(
         $$1x -> $$1x.group($$0.fieldOf("raw").forGetter(Filterable::raw), $$0.optionalFieldOf("filtered").forGetter(Filterable::filtered))
            .apply($$1x, Filterable::new)
      );
      Codec<Filterable<T>> $$2 = $$0.xmap(Filterable::passThrough, Filterable::raw);
      return Codec.withAlternative($$1, $$2);
   }

   public static <B extends ByteBuf, T> StreamCodec<B, Filterable<T>> streamCodec(StreamCodec<B, T> $$0) {
      return StreamCodec.composite($$0, Filterable::raw, $$0.apply(ByteBufCodecs::optional), Filterable::filtered, Filterable::new);
   }

   public static <T> Filterable<T> passThrough(T $$0) {
      return new Filterable<>($$0, Optional.empty());
   }

   public static Filterable<String> from(FilteredText $$0) {
      return new Filterable<>($$0.raw(), $$0.isFiltered() ? Optional.of($$0.filteredOrEmpty()) : Optional.empty());
   }

   public T get(boolean $$0) {
      return $$0 ? this.filtered.orElse(this.raw) : this.raw;
   }

   public <U> Filterable<U> map(Function<T, U> $$0) {
      return new Filterable<>($$0.apply(this.raw), this.filtered.map($$0));
   }

   public <U> Optional<Filterable<U>> resolve(Function<T, Optional<U>> $$0) {
      Optional<U> $$1 = $$0.apply(this.raw);
      if ($$1.isEmpty()) {
         return Optional.empty();
      } else if (this.filtered.isPresent()) {
         Optional<U> $$2 = $$0.apply(this.filtered.get());
         return $$2.isEmpty() ? Optional.empty() : Optional.of(new Filterable<>($$1.get(), $$2));
      } else {
         return Optional.of(new Filterable<>($$1.get(), Optional.empty()));
      }
   }
}
