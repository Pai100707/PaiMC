package net.minecraft.network.protocol.handshake;

import net.minecraft.network.protocol.ProtocolInfoBuilder;
import net.minecraft.network.protocol.SimpleUnboundProtocol;

public class HandshakeProtocols {
   public static final SimpleUnboundProtocol<ServerHandshakePacketListener, net.minecraft.network.FriendlyByteBuf> SERVERBOUND_TEMPLATE = ProtocolInfoBuilder.serverboundProtocol(
      net.minecraft.network.ConnectionProtocol.HANDSHAKING, $$0 -> $$0.addPacket(HandshakePacketTypes.CLIENT_INTENTION, ClientIntentionPacket.STREAM_CODEC)
   );
   public static final net.minecraft.network.ProtocolInfo<ServerHandshakePacketListener> SERVERBOUND = SERVERBOUND_TEMPLATE.bind(
      net.minecraft.network.FriendlyByteBuf::new
   );
}
