/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record TypedDataComponent<T>(DataComponentType<T> type, T value) {
    public static final StreamCodec<RegistryFriendlyByteBuf, TypedDataComponent<?>> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, TypedDataComponent<?>>(){

        @Override
        public TypedDataComponent<?> decode(RegistryFriendlyByteBuf $$0) {
            DataComponentType $$1 = (DataComponentType)DataComponentType.STREAM_CODEC.decode($$0);
            return 1.decodeTyped($$0, $$1);
        }

        private static <T> TypedDataComponent<T> decodeTyped(RegistryFriendlyByteBuf $$0, DataComponentType<T> $$1) {
            return new TypedDataComponent<T>($$1, $$1.streamCodec().decode($$0));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf $$0, TypedDataComponent<?> $$1) {
            1.encodeCap($$0, $$1);
        }

        private static <T> void encodeCap(RegistryFriendlyByteBuf $$0, TypedDataComponent<T> $$1) {
            DataComponentType.STREAM_CODEC.encode($$0, $$1.type());
            $$1.type().streamCodec().encode($$0, $$1.value());
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((RegistryFriendlyByteBuf)((Object)object), (TypedDataComponent)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((RegistryFriendlyByteBuf)((Object)object));
        }
    };

    static TypedDataComponent<?> fromEntryUnchecked(Map.Entry<DataComponentType<?>, Object> $$0) {
        return TypedDataComponent.createUnchecked($$0.getKey(), $$0.getValue());
    }

    public static <T> TypedDataComponent<T> createUnchecked(DataComponentType<T> $$0, Object $$1) {
        return new TypedDataComponent<Object>($$0, $$1);
    }

    public void applyTo(PatchedDataComponentMap $$0) {
        $$0.set(this.type, this.value);
    }

    public <D> DataResult<D> encodeValue(DynamicOps<D> $$0) {
        Codec<T> $$1 = this.type.codec();
        if ($$1 == null) {
            return DataResult.error(() -> "Component of type " + String.valueOf(this.type) + " is not encodable");
        }
        return $$1.encodeStart($$0, this.value);
    }

    @Override
    public String toString() {
        return String.valueOf(this.type) + "=>" + String.valueOf(this.value);
    }
}

