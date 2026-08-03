package net.minecraft.server.packs.metadata;

import com.mojang.serialization.Codec;
import java.util.Optional;

public record MetadataSectionType<T>(String name, Codec<T> codec) {
   public MetadataSectionType.WithValue<T> withValue(T $$0) {
      return new MetadataSectionType.WithValue<>(this, $$0);
   }

   public record WithValue<T>(MetadataSectionType<T> type, T value) {
      public <U> Optional<U> unwrapToType(MetadataSectionType<U> $$0) {
         return $$0 == this.type ? Optional.of(this.value) : Optional.empty();
      }
   }
}
