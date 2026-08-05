/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;

public final class Rotations
extends Record {
    final float x;
    final float y;
    final float z;
    public static final Codec<Rotations> CODEC = Codec.FLOAT.listOf().comapFlatMap($$02 -> Util.fixedSize($$02, 3).map($$0 -> new Rotations(((Float)$$0.get(0)).floatValue(), ((Float)$$0.get(1)).floatValue(), ((Float)$$0.get(2)).floatValue())), $$0 -> List.of(Float.valueOf($$0.x()), Float.valueOf($$0.y()), Float.valueOf($$0.z())));
    public static final StreamCodec<ByteBuf, Rotations> STREAM_CODEC = new StreamCodec<ByteBuf, Rotations>(){

        @Override
        public Rotations decode(ByteBuf $$0) {
            return new Rotations($$0.readFloat(), $$0.readFloat(), $$0.readFloat());
        }

        @Override
        public void encode(ByteBuf $$0, Rotations $$1) {
            $$0.writeFloat($$1.x);
            $$0.writeFloat($$1.y);
            $$0.writeFloat($$1.z);
        }

        @Override
        public /* synthetic */ void encode(Object object, Object object2) {
            this.encode((ByteBuf)object, (Rotations)object2);
        }

        @Override
        public /* synthetic */ Object decode(Object object) {
            return this.decode((ByteBuf)object);
        }
    };

    public Rotations(float $$0, float $$1, float $$2) {
        $$0 = Float.isInfinite($$0) || Float.isNaN($$0) ? 0.0f : $$0 % 360.0f;
        $$1 = Float.isInfinite($$1) || Float.isNaN($$1) ? 0.0f : $$1 % 360.0f;
        $$2 = Float.isInfinite($$2) || Float.isNaN($$2) ? 0.0f : $$2 % 360.0f;
        this.x = $$0;
        this.y = $$1;
        this.z = $$2;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{Rotations.class, "x;y;z", "x", "y", "z"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Rotations.class, "x;y;z", "x", "y", "z"}, this);
    }

    @Override
    public final boolean equals(Object $$0) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Rotations.class, "x;y;z", "x", "y", "z"}, this, $$0);
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public float z() {
        return this.z;
    }
}

