/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.common;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;

public record ServerboundResourcePackPacket(UUID id, Action action) implements Packet<ServerCommonPacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundResourcePackPacket> STREAM_CODEC = Packet.codec(ServerboundResourcePackPacket::write, ServerboundResourcePackPacket::new);

    private ServerboundResourcePackPacket(FriendlyByteBuf $$0) {
        this($$0.readUUID(), $$0.readEnum(Action.class));
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeUUID(this.id);
        $$0.writeEnum(this.action);
    }

    @Override
    public PacketType<ServerboundResourcePackPacket> type() {
        return CommonPacketTypes.SERVERBOUND_RESOURCE_PACK;
    }

    @Override
    public void handle(ServerCommonPacketListener $$0) {
        $$0.handleResourcePackResponse(this);
    }

    public static enum Action {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED,
        DOWNLOADED,
        INVALID_URL,
        FAILED_RELOAD,
        DISCARDED;


        public boolean isTerminal() {
            return this != ACCEPTED && this != DOWNLOADED;
        }
    }
}

