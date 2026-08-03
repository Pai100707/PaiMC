package net.minecraft.world.attribute;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.modifier.AttributeModifier;

public record AttributeType<Value>(
   Codec<Value> valueCodec,
   Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> modifierLibrary,
   Codec<AttributeModifier<Value, ?>> modifierCodec,
   net.minecraft.world.attribute.LerpFunction<Value> keyframeLerp,
   net.minecraft.world.attribute.LerpFunction<Value> stateChangeLerp,
   net.minecraft.world.attribute.LerpFunction<Value> spatialLerp,
   net.minecraft.world.attribute.LerpFunction<Value> partialTickLerp
) {
   public static <Value> net.minecraft.world.attribute.AttributeType<Value> ofInterpolated(
      Codec<Value> $$0, Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> $$1, net.minecraft.world.attribute.LerpFunction<Value> $$2
   ) {
      return ofInterpolated($$0, $$1, $$2, $$2);
   }

   public static <Value> net.minecraft.world.attribute.AttributeType<Value> ofInterpolated(
      Codec<Value> $$0,
      Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> $$1,
      net.minecraft.world.attribute.LerpFunction<Value> $$2,
      net.minecraft.world.attribute.LerpFunction<Value> $$3
   ) {
      return new net.minecraft.world.attribute.AttributeType<>($$0, $$1, createModifierCodec($$1), $$2, $$2, $$2, $$3);
   }

   public static <Value> net.minecraft.world.attribute.AttributeType<Value> ofNotInterpolated(
      Codec<Value> $$0, Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> $$1
   ) {
      return new net.minecraft.world.attribute.AttributeType<>(
         $$0,
         $$1,
         createModifierCodec($$1),
         net.minecraft.world.attribute.LerpFunction.ofStep(1.0F),
         net.minecraft.world.attribute.LerpFunction.ofStep(0.0F),
         net.minecraft.world.attribute.LerpFunction.ofStep(0.5F),
         net.minecraft.world.attribute.LerpFunction.ofStep(0.0F)
      );
   }

   public static <Value> net.minecraft.world.attribute.AttributeType<Value> ofNotInterpolated(Codec<Value> $$0) {
      return ofNotInterpolated($$0, Map.of());
   }

   private static <Value> Codec<AttributeModifier<Value, ?>> createModifierCodec(Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> $$0) {
      ImmutableBiMap<AttributeModifier.OperationId, AttributeModifier<Value, ?>> $$1 = ImmutableBiMap.builder()
         .put(AttributeModifier.OperationId.OVERRIDE, AttributeModifier.override())
         .putAll($$0)
         .buildOrThrow();
      return ExtraCodecs.idResolverCodec(AttributeModifier.OperationId.CODEC, $$1::get, $$1.inverse()::get);
   }

   public void checkAllowedModifier(AttributeModifier<Value, ?> $$0) {
      if ($$0 != AttributeModifier.override() && !this.modifierLibrary.containsValue($$0)) {
         throw new IllegalArgumentException("Modifier " + $$0 + " is not valid for " + this);
      }
   }

   @Override
   public String toString() {
      return Util.getRegisteredName(BuiltInRegistries.ATTRIBUTE_TYPE, this);
   }
}
