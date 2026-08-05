/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public record ClientboundCustomChatCompletionsPacket(Action action, List<String> entries) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundCustomChatCompletionsPacket> STREAM_CODEC = Packet.codec(ClientboundCustomChatCompletionsPacket::write, ClientboundCustomChatCompletionsPacket::new);

    private ClientboundCustomChatCompletionsPacket(FriendlyByteBuf $$0) {
        this($$0.readEnum(Action.class), $$0.readList(FriendlyByteBuf::readUtf));
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeEnum(this.action);
        $$0.writeCollection(this.entries, FriendlyByteBuf::writeUtf);
    }

    @Override
    public PacketType<ClientboundCustomChatCompletionsPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CUSTOM_CHAT_COMPLETIONS;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleCustomChatCompletions(this);
    }

    public static enum Action {
        ADD,
        REMOVE,
        SET;

    }
}

