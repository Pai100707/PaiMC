package net.minecraft.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.LoginProtocols;

public class MemoryServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {
   private final net.minecraft.server.MinecraftServer server;
   private final Connection connection;

   public MemoryServerHandshakePacketListenerImpl(net.minecraft.server.MinecraftServer $$0, Connection $$1) {
      this.server = $$0;
      this.connection = $$1;
   }

   public void handleIntention(ClientIntentionPacket $$0) {
      if ($$0.intention() != ClientIntent.LOGIN) {
         throw new UnsupportedOperationException("Invalid intention " + $$0.intention());
      } else {
         this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new ServerLoginPacketListenerImpl(this.server, this.connection, false));
         this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
      }
   }

   public void onDisconnect(DisconnectionDetails $$0) {
   }

   public boolean isAcceptingMessages() {
      return this.connection.isConnected();
   }
}
