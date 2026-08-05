/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.BundleDelimiterPacket;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.CodecModifier;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.ProtocolCodecBuilder;
import net.minecraft.network.protocol.SimpleUnboundProtocol;
import net.minecraft.network.protocol.UnboundProtocol;
import net.minecraft.util.Unit;
import org.jspecify.annotations.Nullable;

public class ProtocolInfoBuilder<T extends PacketListener, B extends ByteBuf, C> {
    final ConnectionProtocol protocol;
    final PacketFlow flow;
    private final List<CodecEntry<T, ?, B, C>> codecs = new ArrayList();
    private @Nullable BundlerInfo bundlerInfo;

    public ProtocolInfoBuilder(ConnectionProtocol $$0, PacketFlow $$1) {
        this.protocol = $$0;
        this.flow = $$1;
    }

    public <P extends Packet<? super T>> ProtocolInfoBuilder<T, B, C> addPacket(PacketType<P> $$0, StreamCodec<? super B, P> $$1) {
        this.codecs.add(new CodecEntry($$0, $$1, null));
        return this;
    }

    public <P extends Packet<? super T>> ProtocolInfoBuilder<T, B, C> addPacket(PacketType<P> $$0, StreamCodec<? super B, P> $$1, CodecModifier<B, P, C> $$2) {
        this.codecs.add(new CodecEntry($$0, $$1, $$2));
        return this;
    }

    public <P extends BundlePacket<? super T>, D extends BundleDelimiterPacket<? super T>> ProtocolInfoBuilder<T, B, C> withBundlePacket(PacketType<P> $$0, Function<Iterable<Packet<? super T>>, P> $$1, D $$2) {
        StreamCodec $$3 = StreamCodec.unit($$2);
        PacketType<BundleDelimiterPacket<? super T>> $$4 = $$2.type();
        this.codecs.add(new CodecEntry($$4, $$3, null));
        this.bundlerInfo = BundlerInfo.createForPacket($$0, $$1, $$2);
        return this;
    }

    StreamCodec<ByteBuf, Packet<? super T>> buildPacketCodec(Function<ByteBuf, B> $$0, List<CodecEntry<T, ?, B, C>> $$1, C $$2) {
        ProtocolCodecBuilder $$3 = new ProtocolCodecBuilder(this.flow);
        for (CodecEntry codecEntry : $$1) {
            codecEntry.addToBuilder($$3, $$0, $$2);
        }
        return $$3.build();
    }

    private static ProtocolInfo.Details buildDetails(final ConnectionProtocol $$0, final PacketFlow $$1, final List<? extends CodecEntry<?, ?, ?, ?>> $$2) {
        return new ProtocolInfo.Details(){

            @Override
            public ConnectionProtocol id() {
                return $$0;
            }

            @Override
            public PacketFlow flow() {
                return $$1;
            }

            @Override
            public void listPackets(ProtocolInfo.Details.PacketVisitor $$02) {
                for (int $$12 = 0; $$12 < $$2.size(); ++$$12) {
                    CodecEntry $$22 = (CodecEntry)$$2.get($$12);
                    $$02.accept($$22.type, $$12);
                }
            }
        };
    }

    public SimpleUnboundProtocol<T, B> buildUnbound(final C $$0) {
        final List<CodecEntry<T, ?, B, C>> $$1 = List.copyOf(this.codecs);
        final BundlerInfo $$2 = this.bundlerInfo;
        final ProtocolInfo.Details $$3 = ProtocolInfoBuilder.buildDetails(this.protocol, this.flow, $$1);
        return new SimpleUnboundProtocol<T, B>(){

            @Override
            public ProtocolInfo<T> bind(Function<ByteBuf, B> $$02) {
                return new Implementation(ProtocolInfoBuilder.this.protocol, ProtocolInfoBuilder.this.flow, ProtocolInfoBuilder.this.buildPacketCodec($$02, $$1, $$0), $$2);
            }

            @Override
            public ProtocolInfo.Details details() {
                return $$3;
            }
        };
    }

    public UnboundProtocol<T, B, C> buildUnbound() {
        final List<CodecEntry<T, ?, B, C>> $$0 = List.copyOf(this.codecs);
        final BundlerInfo $$1 = this.bundlerInfo;
        final ProtocolInfo.Details $$2 = ProtocolInfoBuilder.buildDetails(this.protocol, this.flow, $$0);
        return new UnboundProtocol<T, B, C>(){

            @Override
            public ProtocolInfo<T> bind(Function<ByteBuf, B> $$02, C $$12) {
                return new Implementation(ProtocolInfoBuilder.this.protocol, ProtocolInfoBuilder.this.flow, ProtocolInfoBuilder.this.buildPacketCodec($$02, $$0, $$12), $$1);
            }

            @Override
            public ProtocolInfo.Details details() {
                return $$2;
            }
        };
    }

    private static <L extends PacketListener, B extends ByteBuf> SimpleUnboundProtocol<L, B> protocol(ConnectionProtocol $$0, PacketFlow $$1, Consumer<ProtocolInfoBuilder<L, B, Unit>> $$2) {
        ProtocolInfoBuilder $$3 = new ProtocolInfoBuilder($$0, $$1);
        $$2.accept($$3);
        return $$3.buildUnbound(Unit.INSTANCE);
    }

    public static <T extends ServerboundPacketListener, B extends ByteBuf> SimpleUnboundProtocol<T, B> serverboundProtocol(ConnectionProtocol $$0, Consumer<ProtocolInfoBuilder<T, B, Unit>> $$1) {
        return ProtocolInfoBuilder.protocol($$0, PacketFlow.SERVERBOUND, $$1);
    }

    public static <T extends ClientboundPacketListener, B extends ByteBuf> SimpleUnboundProtocol<T, B> clientboundProtocol(ConnectionProtocol $$0, Consumer<ProtocolInfoBuilder<T, B, Unit>> $$1) {
        return ProtocolInfoBuilder.protocol($$0, PacketFlow.CLIENTBOUND, $$1);
    }

    private static <L extends PacketListener, B extends ByteBuf, C> UnboundProtocol<L, B, C> contextProtocol(ConnectionProtocol $$0, PacketFlow $$1, Consumer<ProtocolInfoBuilder<L, B, C>> $$2) {
        ProtocolInfoBuilder $$3 = new ProtocolInfoBuilder($$0, $$1);
        $$2.accept($$3);
        return $$3.buildUnbound();
    }

    public static <T extends ServerboundPacketListener, B extends ByteBuf, C> UnboundProtocol<T, B, C> contextServerboundProtocol(ConnectionProtocol $$0, Consumer<ProtocolInfoBuilder<T, B, C>> $$1) {
        return ProtocolInfoBuilder.contextProtocol($$0, PacketFlow.SERVERBOUND, $$1);
    }

    public static <T extends ClientboundPacketListener, B extends ByteBuf, C> UnboundProtocol<T, B, C> contextClientboundProtocol(ConnectionProtocol $$0, Consumer<ProtocolInfoBuilder<T, B, C>> $$1) {
        return ProtocolInfoBuilder.contextProtocol($$0, PacketFlow.CLIENTBOUND, $$1);
    }

    static final class CodecEntry<T extends PacketListener, P extends Packet<? super T>, B extends ByteBuf, C>
    extends Record {
        final PacketType<P> type;
        private final StreamCodec<? super B, P> serializer;
        private final @Nullable CodecModifier<B, P, C> modifier;

        CodecEntry(PacketType<P> $$0, StreamCodec<? super B, P> $$1, @Nullable CodecModifier<B, P, C> $$2) {
            this.type = $$0;
            this.serializer = $$1;
            this.modifier = $$2;
        }

        public void addToBuilder(ProtocolCodecBuilder<ByteBuf, T> $$0, Function<ByteBuf, B> $$1, C $$2) {
            StreamCodec<B, P> $$4;
            if (this.modifier != null) {
                StreamCodec<? super B, P> $$3 = this.modifier.apply(this.serializer, $$2);
            } else {
                $$4 = this.serializer;
            }
            StreamCodec<ByteBuf, P> $$5 = $$4.mapStream($$1);
            $$0.add(this.type, $$5);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CodecEntry.class, "type;serializer;modifier", "type", "serializer", "modifier"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CodecEntry.class, "type;serializer;modifier", "type", "serializer", "modifier"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CodecEntry.class, "type;serializer;modifier", "type", "serializer", "modifier"}, this, $$0);
        }

        public PacketType<P> type() {
            return this.type;
        }

        public StreamCodec<? super B, P> serializer() {
            return this.serializer;
        }

        public @Nullable CodecModifier<B, P, C> modifier() {
            return this.modifier;
        }
    }

    record Implementation<L extends PacketListener>(ConnectionProtocol id, PacketFlow flow, StreamCodec<ByteBuf, Packet<? super L>> codec, @Nullable BundlerInfo bundlerInfo) implements ProtocolInfo<L>
    {
    }
}

