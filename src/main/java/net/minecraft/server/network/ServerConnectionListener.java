package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.local.LocalAddress;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.RateKickingConnection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import org.slf4j.Logger;

public class ServerConnectionListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   final net.minecraft.server.MinecraftServer server;
   public volatile boolean running;
   private final List<ChannelFuture> channels = Collections.synchronizedList(Lists.newArrayList());
   final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

   public ServerConnectionListener(net.minecraft.server.MinecraftServer $$0) {
      this.server = $$0;
      this.running = true;
   }

   public void startTcpServerListener(InetAddress $$0, int $$1) throws IOException {
      synchronized (this.channels) {
         EventLoopGroupHolder $$2 = EventLoopGroupHolder.remote(this.server.useNativeTransport());
         this.channels
            .add(((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().channel($$2.serverChannelCls())).childHandler(new ChannelInitializer<Channel>() {
               protected void initChannel(Channel $$0) {
                  try {
                     $$0.config().setOption(ChannelOption.TCP_NODELAY, true);
                  } catch (ChannelException var5) {
                  }

                  ChannelPipeline $$1x = $$0.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
                  if (ServerConnectionListener.this.server.repliesToStatus()) {
                     $$1x.addLast("legacy_query", new LegacyQueryHandler(ServerConnectionListener.this.getServer()));
                  }

                  Connection.configureSerialization($$1x, PacketFlow.SERVERBOUND, false, null);
                  int $$2x = ServerConnectionListener.this.server.getRateLimitPacketsPerSecond();
                  Connection $$3 = (Connection)($$2x > 0 ? new RateKickingConnection($$2x) : new Connection(PacketFlow.SERVERBOUND));
                  ServerConnectionListener.this.connections.add($$3);
                  $$3.configurePacketHandler($$1x);
                  $$3.setListenerForServerboundHandshake(new ServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, $$3));
               }
            }).group($$2.eventLoopGroup()).localAddress($$0, $$1)).bind().syncUninterruptibly());
      }
   }

   public SocketAddress startMemoryChannel() {
      ChannelFuture $$0;
      synchronized (this.channels) {
         $$0 = ((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().channel(EventLoopGroupHolder.local().serverChannelCls()))
               .childHandler(
                  new ChannelInitializer<Channel>() {
                     protected void initChannel(Channel $$0) {
                        Connection $$1 = new Connection(PacketFlow.SERVERBOUND);
                        $$1.setListenerForServerboundHandshake(new MemoryServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, $$1));
                        ServerConnectionListener.this.connections.add($$1);
                        ChannelPipeline $$2 = $$0.pipeline();
                        Connection.configureInMemoryPipeline($$2, PacketFlow.SERVERBOUND);
                        if (SharedConstants.DEBUG_FAKE_LATENCY_MS > 0) {
                           $$2.addLast(
                              "latency",
                              new ServerConnectionListener.LatencySimulator(SharedConstants.DEBUG_FAKE_LATENCY_MS, SharedConstants.DEBUG_FAKE_JITTER_MS)
                           );
                        }

                        $$1.configurePacketHandler($$2);
                     }
                  }
               )
               .group(EventLoopGroupHolder.local().eventLoopGroup())
               .localAddress(LocalAddress.ANY))
            .bind()
            .syncUninterruptibly();
         this.channels.add($$0);
      }

      return $$0.channel().localAddress();
   }

   public void stop() {
      this.running = false;

      for (ChannelFuture $$0 : this.channels) {
         try {
            $$0.channel().close().sync();
         } catch (InterruptedException var4) {
            LOGGER.error("Interrupted whilst closing channel");
         }
      }
   }

   public void tick() {
      synchronized (this.connections) {
         Iterator<Connection> $$0 = this.connections.iterator();

         while ($$0.hasNext()) {
            Connection $$1 = $$0.next();
            if (!$$1.isConnecting()) {
               if ($$1.isConnected()) {
                  try {
                     $$1.tick();
                  } catch (Exception var7) {
                     if ($$1.isMemoryConnection()) {
                        throw new ReportedException(CrashReport.forThrowable(var7, "Ticking memory connection"));
                     }

                     LOGGER.warn("Failed to handle packet for {}", $$1.getLoggableAddress(this.server.logIPs()), var7);
                     Component $$3 = Component.literal("Internal server error");
                     $$1.send(new ClientboundDisconnectPacket($$3), PacketSendListener.thenRun(() -> $$1.disconnect($$3)));
                     $$1.setReadOnly();
                  }
               } else {
                  $$0.remove();
                  $$1.handleDisconnection();
               }
            }
         }
      }
   }

   public net.minecraft.server.MinecraftServer getServer() {
      return this.server;
   }

   public List<Connection> getConnections() {
      return this.connections;
   }

   static class LatencySimulator extends ChannelInboundHandlerAdapter {
      private static final Timer TIMER = new HashedWheelTimer();
      private final int delay;
      private final int jitter;
      private final List<ServerConnectionListener.LatencySimulator.DelayedMessage> queuedMessages = Lists.newArrayList();

      public LatencySimulator(int $$0, int $$1) {
         this.delay = $$0;
         this.jitter = $$1;
      }

      public void channelRead(ChannelHandlerContext $$0, Object $$1) {
         this.delayDownstream($$0, $$1);
      }

      private void delayDownstream(ChannelHandlerContext $$0, Object $$1) {
         int $$2 = this.delay + (int)(Math.random() * this.jitter);
         this.queuedMessages.add(new ServerConnectionListener.LatencySimulator.DelayedMessage($$0, $$1));
         TIMER.newTimeout(this::onTimeout, $$2, TimeUnit.MILLISECONDS);
      }

      private void onTimeout(Timeout $$0) {
         ServerConnectionListener.LatencySimulator.DelayedMessage $$1 = this.queuedMessages.remove(0);
         $$1.ctx.fireChannelRead($$1.msg);
      }

      static class DelayedMessage {
         public final ChannelHandlerContext ctx;
         public final Object msg;

         public DelayedMessage(ChannelHandlerContext $$0, Object $$1) {
            this.ctx = $$0;
            this.msg = $$1;
         }
      }
   }
}
