package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Util;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.Profiler;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ServerCommonPacketListenerImpl implements ServerCommonPacketListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int LATENCY_CHECK_INTERVAL = 15000;
   private static final int CLOSED_LISTENER_TIMEOUT = 15000;
   private static final Component TIMEOUT_DISCONNECTION_MESSAGE = Component.translatable("disconnect.timeout");
   static final Component DISCONNECT_UNEXPECTED_QUERY = Component.translatable("multiplayer.disconnect.unexpected_query_response");
   protected final net.minecraft.server.MinecraftServer server;
   protected final Connection connection;
   private final boolean transferred;
   private long keepAliveTime;
   private boolean keepAlivePending;
   private long keepAliveChallenge;
   private long closedListenerTime;
   private boolean closed = false;
   private int latency;
   private volatile boolean suspendFlushingOnServerThread = false;

   public ServerCommonPacketListenerImpl(net.minecraft.server.MinecraftServer $$0, Connection $$1, CommonListenerCookie $$2) {
      this.server = $$0;
      this.connection = $$1;
      this.keepAliveTime = Util.getMillis();
      this.latency = $$2.latency();
      this.transferred = $$2.transferred();
   }

   private void close() {
      if (!this.closed) {
         this.closedListenerTime = Util.getMillis();
         this.closed = true;
      }
   }

   public void onDisconnect(DisconnectionDetails $$0) {
      if (this.isSingleplayerOwner()) {
         LOGGER.info("Stopping singleplayer server as player logged out");
         this.server.halt(false);
      }
   }

   public void onPacketError(Packet $$0, Exception $$1) throws ReportedException {
      super.onPacketError($$0, $$1);
      this.server.reportPacketHandlingException($$1, $$0.type());
   }

   public void handleKeepAlive(ServerboundKeepAlivePacket $$0) {
      if (this.keepAlivePending && $$0.getId() == this.keepAliveChallenge) {
         int $$1 = (int)(Util.getMillis() - this.keepAliveTime);
         this.latency = (this.latency * 3 + $$1) / 4;
         this.keepAlivePending = false;
      } else if (!this.isSingleplayerOwner()) {
         this.disconnect(TIMEOUT_DISCONNECTION_MESSAGE);
      }
   }

   public void handlePong(ServerboundPongPacket $$0) {
   }

   public void handleCustomPayload(ServerboundCustomPayloadPacket $$0) {
   }

   public void handleCustomClickAction(ServerboundCustomClickActionPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.server.packetProcessor());
      this.server.handleCustomClickAction($$0.id(), $$0.payload());
   }

   public void handleResourcePackResponse(ServerboundResourcePackPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.server.packetProcessor());
      if ($$0.action() == Action.DECLINED && this.server.isResourcePackRequired()) {
         LOGGER.info("Disconnecting {} due to resource pack {} rejection", this.playerProfile().name(), $$0.id());
         this.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
      }
   }

   public void handleCookieResponse(ServerboundCookieResponsePacket $$0) {
      this.disconnect(DISCONNECT_UNEXPECTED_QUERY);
   }

   protected void keepConnectionAlive() {
      Profiler.get().push("keepAlive");
      long $$0 = Util.getMillis();
      if (!this.isSingleplayerOwner() && $$0 - this.keepAliveTime >= 15000L) {
         if (this.keepAlivePending) {
            this.disconnect(TIMEOUT_DISCONNECTION_MESSAGE);
         } else if (this.checkIfClosed($$0)) {
            this.keepAlivePending = true;
            this.keepAliveTime = $$0;
            this.keepAliveChallenge = $$0;
            this.send(new ClientboundKeepAlivePacket(this.keepAliveChallenge));
         }
      }

      Profiler.get().pop();
   }

   private boolean checkIfClosed(long $$0) {
      if (this.closed) {
         if ($$0 - this.closedListenerTime >= 15000L) {
            this.disconnect(TIMEOUT_DISCONNECTION_MESSAGE);
         }

         return false;
      } else {
         return true;
      }
   }

   public void suspendFlushing() {
      this.suspendFlushingOnServerThread = true;
   }

   public void resumeFlushing() {
      this.suspendFlushingOnServerThread = false;
      this.connection.flushChannel();
   }

   public void send(Packet<?> $$0) {
      this.send($$0, null);
   }

   public void send(Packet<?> $$0, @Nullable ChannelFutureListener $$1) {
      if ($$0.isTerminal()) {
         this.close();
      }

      boolean $$2 = !this.suspendFlushingOnServerThread || !this.server.isSameThread();

      try {
         this.connection.send($$0, $$1, $$2);
      } catch (Throwable var7) {
         CrashReport $$4 = CrashReport.forThrowable(var7, "Sending packet");
         CrashReportCategory $$5 = $$4.addCategory("Packet being sent");
         $$5.setDetail("Packet class", () -> $$0.getClass().getCanonicalName());
         throw new ReportedException($$4);
      }
   }

   public void disconnect(Component $$0) {
      this.disconnect(new DisconnectionDetails($$0));
   }

   public void disconnect(DisconnectionDetails $$0) {
      this.connection.send(new ClientboundDisconnectPacket($$0.reason()), PacketSendListener.thenRun(() -> this.connection.disconnect($$0)));
      this.connection.setReadOnly();
      this.server.executeBlocking(this.connection::handleDisconnection);
   }

   protected boolean isSingleplayerOwner() {
      return this.server.isSingleplayerOwner(new NameAndId(this.playerProfile()));
   }

   protected abstract GameProfile playerProfile();

   @VisibleForDebug
   public GameProfile getOwner() {
      return this.playerProfile();
   }

   public int latency() {
      return this.latency;
   }

   protected CommonListenerCookie createCookie(ClientInformation $$0) {
      return new CommonListenerCookie(this.playerProfile(), this.latency, $$0, this.transferred);
   }
}
