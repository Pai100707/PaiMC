package net.minecraft.server.network;

import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.StatusProtocols;

public class ServerHandshakePacketListenerImpl implements ServerHandshakePacketListener {
   private static final Component IGNORE_STATUS_REASON = Component.translatable("disconnect.ignoring_status_request");
   private final net.minecraft.server.MinecraftServer server;
   private final Connection connection;

   public ServerHandshakePacketListenerImpl(net.minecraft.server.MinecraftServer $$0, Connection $$1) {
      this.server = $$0;
      this.connection = $$1;
   }

   public void handleIntention(ClientIntentionPacket $$0) {
      switch ($$0.intention()) {
         case LOGIN:
            this.beginLogin($$0, false);
            break;
         case STATUS:
            ServerStatus $$1 = this.server.getStatus();
            this.connection.setupOutboundProtocol(StatusProtocols.CLIENTBOUND);
            if (this.server.repliesToStatus() && $$1 != null) {
               this.connection.setupInboundProtocol(StatusProtocols.SERVERBOUND, new ServerStatusPacketListenerImpl($$1, this.connection));
            } else {
               this.connection.disconnect(IGNORE_STATUS_REASON);
            }
            break;
         case TRANSFER:
            if (!this.server.acceptsTransfers()) {
               this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
               Component $$2 = Component.translatable("multiplayer.disconnect.transfers_disabled");
               this.connection.send(new ClientboundLoginDisconnectPacket($$2));
               this.connection.disconnect($$2);
            } else {
               this.beginLogin($$0, true);
            }
            break;
         default:
            throw new UnsupportedOperationException("Invalid intention " + $$0.intention());
      }
   }

   private void beginLogin(ClientIntentionPacket $$0, boolean $$1) {
      this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
      if ($$0.protocolVersion() != SharedConstants.getCurrentVersion().protocolVersion()) {
         Component $$2;
         if ($$0.protocolVersion() < 754) {
            $$2 = Component.translatable("multiplayer.disconnect.outdated_client", new Object[]{SharedConstants.getCurrentVersion().name()});
         } else {
            $$2 = Component.translatable("multiplayer.disconnect.incompatible", new Object[]{SharedConstants.getCurrentVersion().name()});
         }

         this.connection.send(new ClientboundLoginDisconnectPacket($$2));
         this.connection.disconnect($$2);
      } else {
         this.connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, new ServerLoginPacketListenerImpl(this.server, this.connection, $$1));
      }
   }

   public void onDisconnect(DisconnectionDetails $$0) {
   }

   public boolean isAcceptingMessages() {
      return this.connection.isConnected();
   }
}
