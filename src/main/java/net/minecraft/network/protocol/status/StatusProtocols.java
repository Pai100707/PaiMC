package net.minecraft.network.protocol.status;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.protocol.ProtocolInfoBuilder;
import net.minecraft.network.protocol.SimpleUnboundProtocol;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.PingPacketTypes;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;

public class StatusProtocols {
   public static final SimpleUnboundProtocol<ServerStatusPacketListener, ByteBuf> SERVERBOUND_TEMPLATE = ProtocolInfoBuilder.serverboundProtocol(
      net.minecraft.network.ConnectionProtocol.STATUS,
      $$0 -> $$0.addPacket(StatusPacketTypes.SERVERBOUND_STATUS_REQUEST, ServerboundStatusRequestPacket.STREAM_CODEC)
         .addPacket(PingPacketTypes.SERVERBOUND_PING_REQUEST, ServerboundPingRequestPacket.STREAM_CODEC)
   );
   public static final net.minecraft.network.ProtocolInfo<ServerStatusPacketListener> SERVERBOUND = SERVERBOUND_TEMPLATE.bind($$0 -> $$0);
   public static final SimpleUnboundProtocol<ClientStatusPacketListener, net.minecraft.network.FriendlyByteBuf> CLIENTBOUND_TEMPLATE = ProtocolInfoBuilder.clientboundProtocol(
      net.minecraft.network.ConnectionProtocol.STATUS,
      $$0 -> $$0.addPacket(StatusPacketTypes.CLIENTBOUND_STATUS_RESPONSE, ClientboundStatusResponsePacket.STREAM_CODEC)
         .addPacket(PingPacketTypes.CLIENTBOUND_PONG_RESPONSE, ClientboundPongResponsePacket.STREAM_CODEC)
   );
   public static final net.minecraft.network.ProtocolInfo<ClientStatusPacketListener> CLIENTBOUND = CLIENTBOUND_TEMPLATE.bind(
      net.minecraft.network.FriendlyByteBuf::new
   );
}
