package net.minecraft.server.jsonrpc;

import com.google.common.collect.Sets;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;
import net.minecraft.server.jsonrpc.security.AuthenticationHandler;
import net.minecraft.server.jsonrpc.websocket.JsonToWebSocketEncoder;
import net.minecraft.server.jsonrpc.websocket.WebSocketToJsonCodec;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ManagementServer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final HostAndPort hostAndPort;
   final AuthenticationHandler authenticationHandler;
   @Nullable
   private Channel serverChannel;
   private final NioEventLoopGroup nioEventLoopGroup;
   private final Set<Connection> connections = Sets.newIdentityHashSet();

   public ManagementServer(HostAndPort $$0, AuthenticationHandler $$1) {
      this.hostAndPort = $$0;
      this.authenticationHandler = $$1;
      this.nioEventLoopGroup = new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Management server IO #%d").setDaemon(true).build());
   }

   public ManagementServer(HostAndPort $$0, AuthenticationHandler $$1, NioEventLoopGroup $$2) {
      this.hostAndPort = $$0;
      this.authenticationHandler = $$1;
      this.nioEventLoopGroup = $$2;
   }

   public void onConnected(Connection $$0) {
      synchronized (this.connections) {
         this.connections.add($$0);
      }
   }

   public void onDisconnected(Connection $$0) {
      synchronized (this.connections) {
         this.connections.remove($$0);
      }
   }

   public void startWithoutTls(MinecraftApi $$0) {
      this.start($$0, null);
   }

   public void startWithTls(MinecraftApi $$0, SslContext $$1) {
      this.start($$0, $$1);
   }

   private void start(final MinecraftApi $$0, @Nullable final SslContext $$1) {
      final JsonRpcLogger $$2 = new JsonRpcLogger();
      ChannelFuture $$3 = ((ServerBootstrap)((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().handler(new LoggingHandler(LogLevel.DEBUG)))
               .channel(NioServerSocketChannel.class))
            .childHandler(
               new ChannelInitializer<Channel>() {
                  protected void initChannel(Channel $$0x) {
                     try {
                        $$0.config().setOption(ChannelOption.TCP_NODELAY, true);
                     } catch (ChannelException var3) {
                     }

                     ChannelPipeline $$1x = $$0.pipeline();
                     if ($$1 != null) {
                        $$1x.addLast(new ChannelHandler[]{$$1.newHandler($$0.alloc())});
                     }

                     $$1x.addLast(new ChannelHandler[]{new HttpServerCodec()})
                        .addLast(new ChannelHandler[]{new HttpObjectAggregator(65536)})
                        .addLast(new ChannelHandler[]{ManagementServer.this.authenticationHandler})
                        .addLast(new ChannelHandler[]{new WebSocketServerProtocolHandler("/")})
                        .addLast(new ChannelHandler[]{new WebSocketToJsonCodec()})
                        .addLast(new ChannelHandler[]{new JsonToWebSocketEncoder()})
                        .addLast(new ChannelHandler[]{new Connection($$0, ManagementServer.this, $$0, $$2)});
                  }
               }
            )
            .group(this.nioEventLoopGroup)
            .localAddress(this.hostAndPort.getHost(), this.hostAndPort.getPort()))
         .bind();
      this.serverChannel = $$3.channel();
      $$3.syncUninterruptibly();
      LOGGER.info("Json-RPC Management connection listening on {}:{}", this.hostAndPort.getHost(), this.getPort());
   }

   public void stop(boolean $$0) throws InterruptedException {
      if (this.serverChannel != null) {
         this.serverChannel.close().sync();
         this.serverChannel = null;
      }

      this.connections.clear();
      if ($$0) {
         this.nioEventLoopGroup.shutdownGracefully().sync();
      }
   }

   public void tick() {
      this.forEachConnection(Connection::tick);
   }

   public int getPort() {
      return this.serverChannel != null ? ((InetSocketAddress)this.serverChannel.localAddress()).getPort() : this.hostAndPort.getPort();
   }

   void forEachConnection(Consumer<Connection> $$0) {
      synchronized (this.connections) {
         this.connections.forEach($$0);
      }
   }
}
