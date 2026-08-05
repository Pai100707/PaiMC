package net.minecraft.server.network;

import com.google.common.primitives.Ints;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.server.notifications.ServerActivityMonitor;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class ServerLoginPacketListenerImpl implements ServerLoginPacketListener, TickablePacketListener {
   private static final AtomicInteger UNIQUE_THREAD_ID = new AtomicInteger(0);
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAX_TICKS_BEFORE_LOGIN = 600;
   private final byte[] challenge;
   final net.minecraft.server.MinecraftServer server;
   final Connection connection;
   final ServerActivityMonitor serverActivityMonitor;
   private volatile ServerLoginPacketListenerImpl.State state = ServerLoginPacketListenerImpl.State.HELLO;
   private int tick;
   
   String requestedUsername;
   
   private GameProfile authenticatedProfile;
   private final String serverId = "";
   private final boolean transferred;

   public ServerLoginPacketListenerImpl(net.minecraft.server.MinecraftServer $$0, Connection $$1, boolean $$2) {
      this.server = $$0;
      this.connection = $$1;
      this.serverActivityMonitor = this.server.getServerActivityMonitor();
      this.challenge = Ints.toByteArray(RandomSource.create().nextInt());
      this.transferred = $$2;
   }

   public void tick() {
      if (this.state == ServerLoginPacketListenerImpl.State.VERIFYING) {
         this.verifyLoginAndFinishConnectionSetup(Objects.requireNonNull(this.authenticatedProfile));
      }

      if (this.state == ServerLoginPacketListenerImpl.State.WAITING_FOR_DUPE_DISCONNECT
         && !this.isPlayerAlreadyInWorld(Objects.requireNonNull(this.authenticatedProfile))) {
         this.finishLoginAndWaitForClient(this.authenticatedProfile);
      }

      if (this.tick++ == 600) {
         this.disconnect(Component.translatable("multiplayer.disconnect.slow_login"));
      }
   }

   public boolean isAcceptingMessages() {
      return this.connection.isConnected();
   }

   public void disconnect(Component $$0) {
      try {
         LOGGER.info("Disconnecting {}: {}", this.getUserName(), $$0.getString());
         this.connection.send(new ClientboundLoginDisconnectPacket($$0));
         this.connection.disconnect($$0);
      } catch (Exception var3) {
         LOGGER.error("Error whilst disconnecting player", var3);
      }
   }

   private boolean isPlayerAlreadyInWorld(GameProfile $$0) {
      return this.server.getPlayerList().getPlayer($$0.id()) != null;
   }

   public void onDisconnect(DisconnectionDetails $$0) {
      LOGGER.info("{} lost connection: {}", this.getUserName(), $$0.reason().getString());
   }

   public String getUserName() {
      String $$0 = this.connection.getLoggableAddress(this.server.logIPs());
      return this.requestedUsername != null ? this.requestedUsername + " (" + $$0 + ")" : $$0;
   }

   public void handleHello(ServerboundHelloPacket $$0) {
      Validate.validState(this.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet", new Object[0]);
      Validate.validState(StringUtil.isValidPlayerName($$0.name()), "Invalid characters in username", new Object[0]);
      this.requestedUsername = $$0.name();
      GameProfile $$1 = this.server.getSingleplayerProfile();
      if ($$1 != null && this.requestedUsername.equalsIgnoreCase($$1.name())) {
         this.startClientVerification($$1);
      } else {
         if (this.server.usesAuthentication() && !this.connection.isMemoryConnection()) {
            this.state = ServerLoginPacketListenerImpl.State.KEY;
            this.connection.send(new ClientboundHelloPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.challenge, true));
         } else {
            this.startClientVerification(UUIDUtil.createOfflineProfile(this.requestedUsername));
         }
      }
   }

   void startClientVerification(GameProfile $$0) {
      this.authenticatedProfile = $$0;
      this.state = ServerLoginPacketListenerImpl.State.VERIFYING;
   }

   private void verifyLoginAndFinishConnectionSetup(GameProfile $$0) {
      PlayerList $$1 = this.server.getPlayerList();
      Component $$2 = $$1.canPlayerLogin(this.connection.getRemoteAddress(), new NameAndId($$0));
      if ($$2 != null) {
         this.disconnect($$2);
      } else {
         if (this.server.getCompressionThreshold() >= 0 && !this.connection.isMemoryConnection()) {
            this.connection
               .send(
                  new ClientboundLoginCompressionPacket(this.server.getCompressionThreshold()),
                  PacketSendListener.thenRun(() -> this.connection.setupCompression(this.server.getCompressionThreshold(), true))
               );
         }

         boolean $$3 = $$1.disconnectAllPlayersWithProfile($$0.id());
         if ($$3) {
            this.state = ServerLoginPacketListenerImpl.State.WAITING_FOR_DUPE_DISCONNECT;
         } else {
            this.finishLoginAndWaitForClient($$0);
         }
      }
   }

   private void finishLoginAndWaitForClient(GameProfile $$0) {
      this.state = ServerLoginPacketListenerImpl.State.PROTOCOL_SWITCHING;
      this.connection.send(new ClientboundLoginFinishedPacket($$0));
   }

   public void handleKey(ServerboundKeyPacket $$0) {
      Validate.validState(this.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet", new Object[0]);

      final String $$5;
      try {
         PrivateKey $$1 = this.server.getKeyPair().getPrivate();
         if (!$$0.isChallengeValid(this.challenge, $$1)) {
            throw new IllegalStateException("Protocol error");
         }

         SecretKey $$2 = $$0.getSecretKey($$1);
         Cipher $$3 = Crypt.getCipher(2, $$2);
         Cipher $$4 = Crypt.getCipher(1, $$2);
         $$5 = new BigInteger(Crypt.digestData("", this.server.getKeyPair().getPublic(), $$2)).toString(16);
         this.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
         this.connection.setEncryptionKey($$3, $$4);
      } catch (CryptException var7) {
         throw new IllegalStateException("Protocol error", var7);
      }

      Thread $$8 = new Thread("User Authenticator #" + UNIQUE_THREAD_ID.incrementAndGet()) {
         @Override
         public void run() {
            String $$0x = Objects.requireNonNull(ServerLoginPacketListenerImpl.this.requestedUsername, "Player name not initialized");

            try {
               ProfileResult $$1 = ServerLoginPacketListenerImpl.this.server.services().sessionService().hasJoinedServer($$0x, $$5, this.getAddress());
               if ($$1 != null) {
                  GameProfile $$2 = $$1.profile();
                  ServerLoginPacketListenerImpl.LOGGER.info("UUID of player {} is {}", $$2.name(), $$2.id());
                  ServerLoginPacketListenerImpl.this.serverActivityMonitor.reportLoginActivity();
                  ServerLoginPacketListenerImpl.this.startClientVerification($$2);
               } else if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                  ServerLoginPacketListenerImpl.LOGGER.warn("Failed to verify username but will let them in anyway!");
                  ServerLoginPacketListenerImpl.this.startClientVerification(UUIDUtil.createOfflineProfile($$0x));
               } else {
                  ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                  ServerLoginPacketListenerImpl.LOGGER.error("Username '{}' tried to join with an invalid session", $$0x);
               }
            } catch (AuthenticationUnavailableException var4) {
               if (ServerLoginPacketListenerImpl.this.server.isSingleplayer()) {
                  ServerLoginPacketListenerImpl.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                  ServerLoginPacketListenerImpl.this.startClientVerification(UUIDUtil.createOfflineProfile($$0x));
               } else {
                  ServerLoginPacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.authservers_down"));
                  ServerLoginPacketListenerImpl.LOGGER.error("Couldn't verify username because servers are unavailable");
               }
            }
         }

         
         private InetAddress getAddress() {
            SocketAddress $$0x = ServerLoginPacketListenerImpl.this.connection.getRemoteAddress();
            return ServerLoginPacketListenerImpl.this.server.getPreventProxyConnections() && $$0x instanceof InetSocketAddress
               ? ((InetSocketAddress)$$0x).getAddress()
               : null;
         }
      };
      $$8.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
      $$8.start();
   }

   public void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket $$0) {
      this.disconnect(ServerCommonPacketListenerImpl.DISCONNECT_UNEXPECTED_QUERY);
   }

   public void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket $$0) {
      Validate.validState(this.state == ServerLoginPacketListenerImpl.State.PROTOCOL_SWITCHING, "Unexpected login acknowledgement packet", new Object[0]);
      this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
      CommonListenerCookie $$1 = CommonListenerCookie.createInitial(Objects.requireNonNull(this.authenticatedProfile), this.transferred);
      ServerConfigurationPacketListenerImpl $$2 = new ServerConfigurationPacketListenerImpl(this.server, this.connection, $$1);
      this.connection.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, $$2);
      $$2.startConfiguration();
      this.state = ServerLoginPacketListenerImpl.State.ACCEPTED;
   }

   public void fillListenerSpecificCrashDetails(CrashReport $$0, CrashReportCategory $$1) {
      $$1.setDetail("Login phase", () -> this.state.toString());
   }

   public void handleCookieResponse(ServerboundCookieResponsePacket $$0) {
      this.disconnect(ServerCommonPacketListenerImpl.DISCONNECT_UNEXPECTED_QUERY);
   }

   static enum State {
      HELLO,
      KEY,
      AUTHENTICATING,
      NEGOTIATING,
      VERIFYING,
      WAITING_FOR_DUPE_DISCONNECT,
      PROTOCOL_SWITCHING,
      ACCEPTED;
   }
}
