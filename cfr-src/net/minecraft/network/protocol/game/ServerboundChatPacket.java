/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import org.jspecify.annotations.Nullable;

public record ServerboundChatPacket(String message, Instant timeStamp, long salt, @Nullable MessageSignature signature, LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundChatPacket> STREAM_CODEC = Packet.codec(ServerboundChatPacket::write, ServerboundChatPacket::new);

    private ServerboundChatPacket(FriendlyByteBuf $$0) {
        this($$0.readUtf(256), $$0.readInstant(), $$0.readLong(), $$0.readNullable(MessageSignature::read), new LastSeenMessages.Update($$0));
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeUtf(this.message, 256);
        $$0.writeInstant(this.timeStamp);
        $$0.writeLong(this.salt);
        $$0.writeNullable(this.signature, MessageSignature::write);
        this.lastSeenMessages.write($$0);
    }

    @Override
    public PacketType<ServerboundChatPacket> type() {
        return GamePacketTypes.SERVERBOUND_CHAT;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleChat(this);
    }
}

