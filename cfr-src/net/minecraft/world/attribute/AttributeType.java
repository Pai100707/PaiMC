/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableBiMap
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.attribute;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.LerpFunction;
import net.minecraft.world.attribute.modifier.AttributeModifier;

public record AttributeType<Value>(Codec<Value> valueCodec, Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> modifierLibrary, Codec<AttributeModifier<Value, ?>> modifierCodec, LerpFunction<Value> keyframeLerp, LerpFunction<Value> stateChangeLerp, LerpFunction<Value> spatialLerp, LerpFunction<Value> partialTickLerp) {
    public static <Value> AttributeType<Value> ofInterpolated(Codec<Value> $$0, Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> $$1, LerpFunction<Value> $$2) {
        return AttributeType.ofInterpolated($$0, $$1, $$2, $$2);
    }

    public static <Value> AttributeType<Value> ofInterpolated(Codec<Value> $$0, Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> $$1, LerpFunction<Value> $$2, LerpFunction<Value> $$3) {
        return new AttributeType<Value>($$0, $$1, AttributeType.createModifierCodec($$1), $$2, $$2, $$2, $$3);
    }

    public static <Value> AttributeType<Value> ofNotInterpolated(Codec<Value> $$0, Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> $$1) {
        return new AttributeType<Value>($$0, $$1, AttributeType.createModifierCodec($$1), LerpFunction.ofStep(1.0f), LerpFunction.ofStep(0.0f), LerpFunction.ofStep(0.5f), LerpFunction.ofStep(0.0f));
    }

    public static <Value> AttributeType<Value> ofNotInterpolated(Codec<Value> $$0) {
        return AttributeType.ofNotInterpolated($$0, Map.of());
    }

    private static <Value> Codec<AttributeModifier<Value, ?>> createModifierCodec(Map<AttributeModifier.OperationId, AttributeModifier<Value, ?>> $$0) {
        ImmutableBiMap $$1 = ImmutableBiMap.builder().put((Object)AttributeModifier.OperationId.OVERRIDE, AttributeModifier.override()).putAll($$0).buildOrThrow();
        return ExtraCodecs.idResolverCodec(AttributeModifier.OperationId.CODEC, arg_0 -> ((ImmutableBiMap)$$1).get(arg_0), arg_0 -> ((ImmutableBiMap)$$1.inverse()).get(arg_0));
    }

    public void checkAllowedModifier(AttributeModifier<Value, ?> $$0) {
        if ($$0 != AttributeModifier.override() && !this.modifierLibrary.containsValue($$0)) {
            throw new IllegalArgumentException("Modifier " + String.valueOf($$0) + " is not valid for " + String.valueOf(this));
        }
    }

    @Override
    public String toString() {
        return Util.getRegisteredName(BuiltInRegistries.ATTRIBUTE_TYPE, this);
    }
}

