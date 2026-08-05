/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundClientCommandPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundClientCommandPacket> STREAM_CODEC = Packet.codec(ServerboundClientCommandPacket::write, ServerboundClientCommandPacket::new);
    private final Action action;

    public ServerboundClientCommandPacket(Action $$0) {
        this.action = $$0;
    }

    private ServerboundClientCommandPacket(FriendlyByteBuf $$0) {
        this.action = $$0.readEnum(Action.class);
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeEnum(this.action);
    }

    @Override
    public PacketType<ServerboundClientCommandPacket> type() {
        return GamePacketTypes.SERVERBOUND_CLIENT_COMMAND;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleClientCommand(this);
    }

    public Action getAction() {
        return this.action;
    }

    public static enum Action {
        PERFORM_RESPAWN,
        REQUEST_STATS;

    }
}

