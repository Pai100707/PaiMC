/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public record ServerboundChatCommandSignedPacket(String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundChatCommandSignedPacket> STREAM_CODEC = Packet.codec(ServerboundChatCommandSignedPacket::write, ServerboundChatCommandSignedPacket::new);

    private ServerboundChatCommandSignedPacket(FriendlyByteBuf $$0) {
        this($$0.readUtf(), $$0.readInstant(), $$0.readLong(), new ArgumentSignatures($$0), new LastSeenMessages.Update($$0));
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeUtf(this.command);
        $$0.writeInstant(this.timeStamp);
        $$0.writeLong(this.salt);
        this.argumentSignatures.write($$0);
        this.lastSeenMessages.write($$0);
    }

    @Override
    public PacketType<ServerboundChatCommandSignedPacket> type() {
        return GamePacketTypes.SERVERBOUND_CHAT_COMMAND_SIGNED;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleSignedChatCommand(this);
    }
}

