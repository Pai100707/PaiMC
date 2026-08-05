/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public class ClientboundSetChunkCacheCenterPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetChunkCacheCenterPacket> STREAM_CODEC = Packet.codec(ClientboundSetChunkCacheCenterPacket::write, ClientboundSetChunkCacheCenterPacket::new);
    private final int x;
    private final int z;

    public ClientboundSetChunkCacheCenterPacket(int $$0, int $$1) {
        this.x = $$0;
        this.z = $$1;
    }

    private ClientboundSetChunkCacheCenterPacket(FriendlyByteBuf $$0) {
        this.x = $$0.readVarInt();
        this.z = $$0.readVarInt();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.x);
        $$0.writeVarInt(this.z);
    }

    @Override
    public PacketType<ClientboundSetChunkCacheCenterPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_CHUNK_CACHE_CENTER;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleSetChunkCacheCenter(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }
}

