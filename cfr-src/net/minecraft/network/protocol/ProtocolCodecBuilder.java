/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.codec.IdDispatchCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;

public class ProtocolCodecBuilder<B extends ByteBuf, L extends PacketListener> {
    private final IdDispatchCodec.Builder<B, Packet<? super L>, PacketType<? extends Packet<? super L>>> dispatchBuilder = IdDispatchCodec.builder(Packet::type);
    private final PacketFlow flow;

    public ProtocolCodecBuilder(PacketFlow $$0) {
        this.flow = $$0;
    }

    public <T extends Packet<? super L>> ProtocolCodecBuilder<B, L> add(PacketType<T> $$0, StreamCodec<? super B, T> $$1) {
        if ($$0.flow() != this.flow) {
            throw new IllegalArgumentException("Invalid packet flow for packet " + String.valueOf($$0) + ", expected " + this.flow.name());
        }
        this.dispatchBuilder.add($$0, $$1);
        return this;
    }

    public StreamCodec<B, Packet<? super L>> build() {
        return this.dispatchBuilder.build();
    }
}

