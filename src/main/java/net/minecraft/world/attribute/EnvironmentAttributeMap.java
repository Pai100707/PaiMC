package net.minecraft.world.attribute;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.modifier.AttributeModifier;

public final class EnvironmentAttributeMap {
   public static final net.minecraft.world.attribute.EnvironmentAttributeMap EMPTY = new net.minecraft.world.attribute.EnvironmentAttributeMap(Map.of());
   public static final Codec<net.minecraft.world.attribute.EnvironmentAttributeMap> CODEC = Codec.lazyInitialized(
      () -> Codec.dispatchedMap(
            net.minecraft.world.attribute.EnvironmentAttributes.CODEC, Util.memoize(net.minecraft.world.attribute.EnvironmentAttributeMap.Entry::createCodec)
         )
         .xmap(net.minecraft.world.attribute.EnvironmentAttributeMap::new, $$0 -> $$0.entries)
   );
   public static final Codec<net.minecraft.world.attribute.EnvironmentAttributeMap> NETWORK_CODEC = CODEC.xmap(
      net.minecraft.world.attribute.EnvironmentAttributeMap::filterSyncable, net.minecraft.world.attribute.EnvironmentAttributeMap::filterSyncable
   );
   public static final Codec<net.minecraft.world.attribute.EnvironmentAttributeMap> CODEC_ONLY_POSITIONAL = CODEC.validate($$0 -> {
      List<net.minecraft.world.attribute.EnvironmentAttribute<?>> $$1 = $$0.keySet().stream().filter($$0x -> !$$0x.isPositional()).toList();
      return !$$1.isEmpty() ? DataResult.error(() -> "The following attributes cannot be positional: " + $$1) : DataResult.success($$0);
   });
   final Map<net.minecraft.world.attribute.EnvironmentAttribute<?>, net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<?, ?>> entries;

   private static net.minecraft.world.attribute.EnvironmentAttributeMap filterSyncable(net.minecraft.world.attribute.EnvironmentAttributeMap $$0) {
      return new net.minecraft.world.attribute.EnvironmentAttributeMap(
         Map.copyOf(Maps.filterKeys($$0.entries, net.minecraft.world.attribute.EnvironmentAttribute::isSyncable))
      );
   }

   EnvironmentAttributeMap(Map<net.minecraft.world.attribute.EnvironmentAttribute<?>, net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<?, ?>> $$0) {
      this.entries = $$0;
   }

   public static net.minecraft.world.attribute.EnvironmentAttributeMap.Builder builder() {
      return new net.minecraft.world.attribute.EnvironmentAttributeMap.Builder();
   }

   
   public <Value> net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<Value, ?> get(net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0) {
      return (net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<Value, ?>)this.entries.get($$0);
   }

   public <Value> Value applyModifier(net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, Value $$1) {
      net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<Value, ?> $$2 = this.get($$0);
      return $$2 != null ? $$2.applyModifier($$1) : $$1;
   }

   public boolean contains(net.minecraft.world.attribute.EnvironmentAttribute<?> $$0) {
      return this.entries.containsKey($$0);
   }

   public Set<net.minecraft.world.attribute.EnvironmentAttribute<?>> keySet() {
      return this.entries.keySet();
   }

   @Override
   public boolean equals(Object $$0) {
      return $$0 == this ? true : $$0 instanceof net.minecraft.world.attribute.EnvironmentAttributeMap $$1 && this.entries.equals($$1.entries);
   }

   @Override
   public int hashCode() {
      return this.entries.hashCode();
   }

   @Override
   public String toString() {
      return this.entries.toString();
   }

   public static class Builder {
      private final Map<net.minecraft.world.attribute.EnvironmentAttribute<?>, net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<?, ?>> entries = new HashMap<>();

      Builder() {
      }

      public net.minecraft.world.attribute.EnvironmentAttributeMap.Builder putAll(net.minecraft.world.attribute.EnvironmentAttributeMap $$0) {
         this.entries.putAll($$0.entries);
         return this;
      }

      public <Value, Parameter> net.minecraft.world.attribute.EnvironmentAttributeMap.Builder modify(
         net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, AttributeModifier<Value, Parameter> $$1, Parameter $$2
      ) {
         $$0.type().checkAllowedModifier($$1);
         this.entries.put($$0, new net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<>($$2, $$1));
         return this;
      }

      public <Value> net.minecraft.world.attribute.EnvironmentAttributeMap.Builder set(net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, Value $$1) {
         return this.modify($$0, AttributeModifier.override(), $$1);
      }

      public net.minecraft.world.attribute.EnvironmentAttributeMap build() {
         return this.entries.isEmpty()
            ? net.minecraft.world.attribute.EnvironmentAttributeMap.EMPTY
            : new net.minecraft.world.attribute.EnvironmentAttributeMap(Map.copyOf(this.entries));
      }
   }

   public record Entry<Value, Argument>(Argument argument, AttributeModifier<Value, Argument> modifier) {
      private static <Value> Codec<net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<Value, ?>> createCodec(
         net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0
      ) {
         Codec<net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<Value, ?>> $$1 = $$0.type()
            .modifierCodec()
            .dispatch("modifier", net.minecraft.world.attribute.EnvironmentAttributeMap.Entry::modifier, Util.memoize($$1x -> createFullCodec($$0, $$1x)));
         return Codec.either($$0.valueCodec(), $$1)
            .xmap(
               $$0x -> (net.minecraft.world.attribute.EnvironmentAttributeMap.Entry)$$0x.map(
                  $$0xx -> new net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<>($$0xx, AttributeModifier.override()), $$0xx -> $$0xx
               ),
               $$0x -> $$0x.modifier == AttributeModifier.override() ? Either.left($$0x.argument()) : Either.right($$0x)
            );
      }

      private static <Value, Argument> MapCodec<net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<Value, Argument>> createFullCodec(
         net.minecraft.world.attribute.EnvironmentAttribute<Value> $$0, AttributeModifier<Value, Argument> $$1
      ) {
         return RecordCodecBuilder.mapCodec(
            $$2 -> $$2.group($$1.argumentCodec($$0).fieldOf("argument").forGetter(net.minecraft.world.attribute.EnvironmentAttributeMap.Entry::argument))
               .apply($$2, $$1xx -> new net.minecraft.world.attribute.EnvironmentAttributeMap.Entry<>($$1xx, $$1))
         );
      }

      public Value applyModifier(Value $$0) {
         return this.modifier.apply($$0, this.argument);
      }
   }
}
