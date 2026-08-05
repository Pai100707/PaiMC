/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.net.HostAndPort
 *  com.google.common.util.concurrent.ThreadFactoryBuilder
 *  com.mojang.logging.LogUtils
 *  io.netty.bootstrap.ServerBootstrap
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelException
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelInitializer
 *  io.netty.channel.ChannelOption
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.EventLoopGroup
 *  io.netty.channel.nio.NioEventLoopGroup
 *  io.netty.channel.socket.nio.NioServerSocketChannel
 *  io.netty.handler.codec.http.HttpObjectAggregator
 *  io.netty.handler.codec.http.HttpServerCodec
 *  io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
 *  io.netty.handler.logging.LogLevel
 *  io.netty.handler.logging.LoggingHandler
 *  io.netty.handler.ssl.SslContext
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
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
import io.netty.channel.EventLoopGroup;
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
import net.minecraft.server.jsonrpc.Connection;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
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
    private @Nullable Channel serverChannel;
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void onConnected(Connection $$0) {
        Set<Connection> set = this.connections;
        synchronized (set) {
            this.connections.add($$0);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void onDisconnected(Connection $$0) {
        Set<Connection> set = this.connections;
        synchronized (set) {
            this.connections.remove((Object)$$0);
        }
    }

    public void startWithoutTls(MinecraftApi $$0) {
        this.start($$0, null);
    }

    public void startWithTls(MinecraftApi $$0, SslContext $$1) {
        this.start($$0, $$1);
    }

    private void start(final MinecraftApi $$0, final @Nullable SslContext $$1) {
        final JsonRpcLogger $$2 = new JsonRpcLogger();
        ChannelFuture $$3 = ((ServerBootstrap)((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().handler((ChannelHandler)new LoggingHandler(LogLevel.DEBUG))).channel(NioServerSocketChannel.class)).childHandler((ChannelHandler)new ChannelInitializer<Channel>(){

            protected void initChannel(Channel $$02) {
                try {
                    $$02.config().setOption(ChannelOption.TCP_NODELAY, (Object)true);
                }
                catch (ChannelException channelException) {
                    // empty catch block
                }
                ChannelPipeline $$12 = $$02.pipeline();
                if ($$1 != null) {
                    $$12.addLast(new ChannelHandler[]{$$1.newHandler($$02.alloc())});
                }
                $$12.addLast(new ChannelHandler[]{new HttpServerCodec()}).addLast(new ChannelHandler[]{new HttpObjectAggregator(65536)}).addLast(new ChannelHandler[]{ManagementServer.this.authenticationHandler}).addLast(new ChannelHandler[]{new WebSocketServerProtocolHandler("/")}).addLast(new ChannelHandler[]{new WebSocketToJsonCodec()}).addLast(new ChannelHandler[]{new JsonToWebSocketEncoder()}).addLast(new ChannelHandler[]{new Connection($$02, ManagementServer.this, $$0, $$2)});
            }
        }).group((EventLoopGroup)this.nioEventLoopGroup).localAddress(this.hostAndPort.getHost(), this.hostAndPort.getPort())).bind();
        this.serverChannel = $$3.channel();
        $$3.syncUninterruptibly();
        LOGGER.info("Json-RPC Management connection listening on {}:{}", (Object)this.hostAndPort.getHost(), (Object)this.getPort());
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void forEachConnection(Consumer<Connection> $$0) {
        Set<Connection> set = this.connections;
        synchronized (set) {
            this.connections.forEach($$0);
        }
    }
}

