/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.server.level.ClientInformation;

public record ServerboundClientInformationPacket(ClientInformation information) implements Packet<ServerCommonPacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundClientInformationPacket> STREAM_CODEC = Packet.codec(ServerboundClientInformationPacket::write, ServerboundClientInformationPacket::new);

    private ServerboundClientInformationPacket(FriendlyByteBuf $$0) {
        this(new ClientInformation($$0));
    }

    private void write(FriendlyByteBuf $$0) {
        this.information.write($$0);
    }

    @Override
    public PacketType<ServerboundClientInformationPacket> type() {
        return CommonPacketTypes.SERVERBOUND_CLIENT_INFORMATION;
    }

    @Override
    public void handle(ServerCommonPacketListener $$0) {
        $$0.handleClientInformation(this);
    }
}

