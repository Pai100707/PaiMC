/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 */
package net.minecraft.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;

public class IdDispatchCodec<B extends ByteBuf, V, T>
implements StreamCodec<B, V> {
    private static final int UNKNOWN_TYPE = -1;
    private final Function<V, ? extends T> typeGetter;
    private final List<Entry<B, V, T>> byId;
    private final Object2IntMap<T> toId;

    IdDispatchCodec(Function<V, ? extends T> $$0, List<Entry<B, V, T>> $$1, Object2IntMap<T> $$2) {
        this.typeGetter = $$0;
        this.byId = $$1;
        this.toId = $$2;
    }

    @Override
    public V decode(B $$0) {
        int $$1 = VarInt.read($$0);
        if ($$1 < 0 || $$1 >= this.byId.size()) {
            throw new DecoderException("Received unknown packet id " + $$1);
        }
        Entry<B, V, T> $$2 = this.byId.get($$1);
        try {
            return (V)$$2.serializer.decode($$0);
        }
        catch (Exception $$3) {
            if ($$3 instanceof DontDecorateException) {
                throw $$3;
            }
            throw new DecoderException("Failed to decode packet '" + String.valueOf($$2.type) + "'", (Throwable)$$3);
        }
    }

    @Override
    public void encode(B $$0, V $$1) {
        T $$2 = this.typeGetter.apply($$1);
        int $$3 = this.toId.getOrDefault($$2, -1);
        if ($$3 == -1) {
            throw new EncoderException("Sending unknown packet '" + String.valueOf($$2) + "'");
        }
        VarInt.write($$0, $$3);
        Entry<B, V, T> $$4 = this.byId.get($$3);
        try {
            StreamCodec $$5 = $$4.serializer;
            $$5.encode($$0, $$1);
        }
        catch (Exception $$6) {
            if ($$6 instanceof DontDecorateException) {
                throw $$6;
            }
            throw new EncoderException("Failed to encode packet '" + String.valueOf($$2) + "'", (Throwable)$$6);
        }
    }

    public static <B extends ByteBuf, V, T> Builder<B, V, T> builder(Function<V, ? extends T> $$0) {
        return new Builder($$0);
    }

    @Override
    public /* synthetic */ void encode(Object object, Object object2) {
        this.encode((B)((ByteBuf)object), (V)object2);
    }

    @Override
    public /* synthetic */ Object decode(Object object) {
        return this.decode((B)((ByteBuf)object));
    }

    static final class Entry<B, V, T>
    extends Record {
        final StreamCodec<? super B, ? extends V> serializer;
        final T type;

        Entry(StreamCodec<? super B, ? extends V> $$0, T $$1) {
            this.serializer = $$0;
            this.type = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "serializer;type", "serializer", "type"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "serializer;type", "serializer", "type"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "serializer;type", "serializer", "type"}, this, $$0);
        }

        public StreamCodec<? super B, ? extends V> serializer() {
            return this.serializer;
        }

        public T type() {
            return this.type;
        }
    }

    public static interface DontDecorateException {
    }

    public static class Builder<B extends ByteBuf, V, T> {
        private final List<Entry<B, V, T>> entries = new ArrayList<Entry<B, V, T>>();
        private final Function<V, ? extends T> typeGetter;

        Builder(Function<V, ? extends T> $$0) {
            this.typeGetter = $$0;
        }

        public Builder<B, V, T> add(T $$0, StreamCodec<? super B, ? extends V> $$1) {
            this.entries.add(new Entry<B, V, T>($$1, $$0));
            return this;
        }

        public IdDispatchCodec<B, V, T> build() {
            Object2IntOpenHashMap $$0 = new Object2IntOpenHashMap();
            $$0.defaultReturnValue(-2);
            for (Entry<B, V, T> $$1 : this.entries) {
                int $$2 = $$0.size();
                int $$3 = $$0.putIfAbsent($$1.type, $$2);
                if ($$3 == -2) continue;
                throw new IllegalStateException("Duplicate registration for type " + String.valueOf($$1.type));
            }
            return new IdDispatchCodec<B, V, T>(this.typeGetter, List.copyOf(this.entries), $$0);
        }
    }
}

