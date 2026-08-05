/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public record ClientboundDeleteChatPacket(MessageSignature.Packed messageSignature) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundDeleteChatPacket> STREAM_CODEC = Packet.codec(ClientboundDeleteChatPacket::write, ClientboundDeleteChatPacket::new);

    private ClientboundDeleteChatPacket(FriendlyByteBuf $$0) {
        this(MessageSignature.Packed.read($$0));
    }

    private void write(FriendlyByteBuf $$0) {
        MessageSignature.Packed.write($$0, this.messageSignature);
    }

    @Override
    public PacketType<ClientboundDeleteChatPacket> type() {
        return GamePacketTypes.CLIENTBOUND_DELETE_CHAT;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleDeleteChat(this);
    }
}

