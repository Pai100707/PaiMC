/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.common.custom;

import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamMemberEncoder;
import net.minecraft.resources.Identifier;

public interface CustomPacketPayload {
    public Type<? extends CustomPacketPayload> type();

    public static <B extends ByteBuf, T extends CustomPacketPayload> StreamCodec<B, T> codec(StreamMemberEncoder<B, T> $$0, StreamDecoder<B, T> $$1) {
        return StreamCodec.ofMember($$0, $$1);
    }

    public static <T extends CustomPacketPayload> Type<T> createType(String $$0) {
        return new Type(Identifier.withDefaultNamespace($$0));
    }

    public static <B extends FriendlyByteBuf> StreamCodec<B, CustomPacketPayload> codec(final FallbackProvider<B> $$02, List<TypeAndCodec<? super B, ?>> $$1) {
        final Map<Identifier, StreamCodec> $$2 = $$1.stream().collect(Collectors.toUnmodifiableMap($$0 -> $$0.type().id(), TypeAndCodec::codec));
        return new StreamCodec<B, CustomPacketPayload>(){

            private StreamCodec<? super B, ? extends CustomPacketPayload> findCodec(Identifier $$0) {
                StreamCodec $$1 = (StreamCodec)$$2.get($$0);
                if ($$1 != null) {
                    return $$1;
                }
                return $$02.create($$0);
            }

            private <T extends CustomPacketPayload> void writeCap(B $$0, Type<T> $$1, CustomPacketPayload $$22) {
                ((FriendlyByteBuf)((Object)$$0)).writeIdentifier($$1.id());
                StreamCodec $$3 = this.findCodec($$1.id);
                $$3.encode($$0, $$22);
            }

            @Override
            public void encode(B $$0, CustomPacketPayload $$1) {
                this.writeCap($$0, $$1.type(), $$1);
            }

            @Override
            public CustomPacketPayload decode(B $$0) {
                Identifier $$1 = ((FriendlyByteBuf)((Object)$$0)).readIdentifier();
                return (CustomPacketPayload)this.findCodec($$1).decode($$0);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((Object)((FriendlyByteBuf)((Object)object))), (CustomPacketPayload)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((Object)((FriendlyByteBuf)((Object)object))));
            }
        };
    }

    public static final class Type<T extends CustomPacketPayload>
    extends Record {
        final Identifier id;

        public Type(Identifier $$0) {
            this.id = $$0;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Type.class, "id", "id"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Type.class, "id", "id"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Type.class, "id", "id"}, this, $$0);
        }

        public Identifier id() {
            return this.id;
        }
    }

    public static interface FallbackProvider<B extends FriendlyByteBuf> {
        public StreamCodec<B, ? extends CustomPacketPayload> create(Identifier var1);
    }

    public record TypeAndCodec<B extends FriendlyByteBuf, T extends CustomPacketPayload>(Type<T> type, StreamCodec<B, T> codec) {
    }
}

