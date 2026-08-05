/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.login.LoginPacketTypes;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.minecraft.network.protocol.login.custom.DiscardedQueryAnswerPayload;
import org.jspecify.annotations.Nullable;

public record ServerboundCustomQueryAnswerPacket(int transactionId, @Nullable CustomQueryAnswerPayload payload) implements Packet<ServerLoginPacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundCustomQueryAnswerPacket> STREAM_CODEC = Packet.codec(ServerboundCustomQueryAnswerPacket::write, ServerboundCustomQueryAnswerPacket::read);
    private static final int MAX_PAYLOAD_SIZE = 0x100000;

    private static ServerboundCustomQueryAnswerPacket read(FriendlyByteBuf $$0) {
        int $$1 = $$0.readVarInt();
        return new ServerboundCustomQueryAnswerPacket($$1, ServerboundCustomQueryAnswerPacket.readPayload($$1, $$0));
    }

    private static CustomQueryAnswerPayload readPayload(int $$0, FriendlyByteBuf $$1) {
        return ServerboundCustomQueryAnswerPacket.readUnknownPayload($$1);
    }

    private static CustomQueryAnswerPayload readUnknownPayload(FriendlyByteBuf $$0) {
        int $$1 = $$0.readableBytes();
        if ($$1 < 0 || $$1 > 0x100000) {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
        $$0.skipBytes($$1);
        return DiscardedQueryAnswerPayload.INSTANCE;
    }

    private void write(FriendlyByteBuf $$02) {
        $$02.writeVarInt(this.transactionId);
        $$02.writeNullable(this.payload, ($$0, $$1) -> $$1.write((FriendlyByteBuf)((Object)$$0)));
    }

    @Override
    public PacketType<ServerboundCustomQueryAnswerPacket> type() {
        return LoginPacketTypes.SERVERBOUND_CUSTOM_QUERY_ANSWER;
    }

    @Override
    public void handle(ServerLoginPacketListener $$0) {
        $$0.handleCustomQueryPacket(this);
    }
}

