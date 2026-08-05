/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.cookie;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.cookie.CookiePacketTypes;
import net.minecraft.network.protocol.cookie.ServerCookiePacketListener;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record ServerboundCookieResponsePacket(Identifier key, byte @Nullable [] payload) implements Packet<ServerCookiePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundCookieResponsePacket> STREAM_CODEC = Packet.codec(ServerboundCookieResponsePacket::write, ServerboundCookieResponsePacket::new);

    private ServerboundCookieResponsePacket(FriendlyByteBuf $$0) {
        this($$0.readIdentifier(), $$0.readNullable(ClientboundStoreCookiePacket.PAYLOAD_STREAM_CODEC));
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeIdentifier(this.key);
        $$0.writeNullable(this.payload, ClientboundStoreCookiePacket.PAYLOAD_STREAM_CODEC);
    }

    @Override
    public PacketType<ServerboundCookieResponsePacket> type() {
        return CookiePacketTypes.SERVERBOUND_COOKIE_RESPONSE;
    }

    @Override
    public void handle(ServerCookiePacketListener $$0) {
        $$0.handleCookieResponse(this);
    }
}

