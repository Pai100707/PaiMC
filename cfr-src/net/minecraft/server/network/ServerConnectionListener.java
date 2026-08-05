/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  io.netty.bootstrap.ServerBootstrap
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelException
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandlerAdapter
 *  io.netty.channel.ChannelInitializer
 *  io.netty.channel.ChannelOption
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.local.LocalAddress
 *  io.netty.handler.timeout.ReadTimeoutHandler
 *  io.netty.util.HashedWheelTimer
 *  io.netty.util.Timeout
 *  io.netty.util.Timer
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.server.network.LegacyQueryHandler;
import net.minecraft.server.network.MemoryServerHandshakePacketListenerImpl;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerConnectionListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    final MinecraftServer server;
    public volatile boolean running;
    private final List<ChannelFuture> channels = Collections.synchronizedList(Lists.newArrayList());
    final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

    public ServerConnectionListener(MinecraftServer $$0) {
        this.server = $$0;
        this.running = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void startTcpServerListener(@Nullable InetAddress $$0, int $$1) throws IOException {
        List<ChannelFuture> list = this.channels;
        synchronized (list) {
            EventLoopGroupHolder $$2 = EventLoopGroupHolder.remote(this.server.useNativeTransport());
            this.channels.add(((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().channel($$2.serverChannelCls())).childHandler((ChannelHandler)new ChannelInitializer<Channel>(){

                protected void initChannel(Channel $$0) {
                    try {
                        $$0.config().setOption(ChannelOption.TCP_NODELAY, (Object)true);
                    }
                    catch (ChannelException channelException) {
                        // empty catch block
                    }
                    ChannelPipeline $$1 = $$0.pipeline().addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30));
                    if (ServerConnectionListener.this.server.repliesToStatus()) {
                        $$1.addLast("legacy_query", (ChannelHandler)new LegacyQueryHandler(ServerConnectionListener.this.getServer()));
                    }
                    Connection.configureSerialization($$1, PacketFlow.SERVERBOUND, false, null);
                    int $$2 = ServerConnectionListener.this.server.getRateLimitPacketsPerSecond();
                    Connection $$3 = $$2 > 0 ? new RateKickingConnection($$2) : new Connection(PacketFlow.SERVERBOUND);
                    ServerConnectionListener.this.connections.add($$3);
                    $$3.configurePacketHandler($$1);
                    $$3.setListenerForServerboundHandshake(new ServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, $$3));
                }
            }).group($$2.eventLoopGroup()).localAddress($$0, $$1)).bind().syncUninterruptibly());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    public SocketAddress startMemoryChannel() {
        void $$1;
        List<ChannelFuture> list = this.channels;
        synchronized (list) {
            ChannelFuture $$0 = ((ServerBootstrap)((ServerBootstrap)new ServerBootstrap().channel(EventLoopGroupHolder.local().serverChannelCls())).childHandler((ChannelHandler)new ChannelInitializer<Channel>(){

                protected void initChannel(Channel $$0) {
                    Connection $$1 = new Connection(PacketFlow.SERVERBOUND);
                    $$1.setListenerForServerboundHandshake(new MemoryServerHandshakePacketListenerImpl(ServerConnectionListener.this.server, $$1));
                    ServerConnectionListener.this.connections.add($$1);
                    ChannelPipeline $$2 = $$0.pipeline();
                    Connection.configureInMemoryPipeline($$2, PacketFlow.SERVERBOUND);
                    if (SharedConstants.DEBUG_FAKE_LATENCY_MS > 0) {
                        $$2.addLast("latency", (ChannelHandler)new LatencySimulator(SharedConstants.DEBUG_FAKE_LATENCY_MS, SharedConstants.DEBUG_FAKE_JITTER_MS));
                    }
                    $$1.configurePacketHandler($$2);
                }
            }).group(EventLoopGroupHolder.local().eventLoopGroup()).localAddress((SocketAddress)LocalAddress.ANY)).bind().syncUninterruptibly();
            this.channels.add($$0);
        }
        return $$1.channel().localAddress();
    }

    public void stop() {
        this.running = false;
        for (ChannelFuture $$0 : this.channels) {
            try {
                $$0.channel().close().sync();
            }
            catch (InterruptedException $$1) {
                LOGGER.error("Interrupted whilst closing channel");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick() {
        List<Connection> list = this.connections;
        synchronized (list) {
            Iterator<Connection> $$0 = this.connections.iterator();
            while ($$0.hasNext()) {
                Connection $$1 = $$0.next();
                if ($$1.isConnecting()) continue;
                if ($$1.isConnected()) {
                    try {
                        $$1.tick();
                    }
                    catch (Exception $$2) {
                        if ($$1.isMemoryConnection()) {
                            throw new ReportedException(CrashReport.forThrowable($$2, "Ticking memory connection"));
                        }
                        LOGGER.warn("Failed to handle packet for {}", (Object)$$1.getLoggableAddress(this.server.logIPs()), (Object)$$2);
                        MutableComponent $$3 = Component.literal("Internal server error");
                        $$1.send(new ClientboundDisconnectPacket($$3), PacketSendListener.thenRun(() -> $$1.disconnect($$3)));
                        $$1.setReadOnly();
                    }
                    continue;
                }
                $$0.remove();
                $$1.handleDisconnection();
            }
        }
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public List<Connection> getConnections() {
        return this.connections;
    }

    static class LatencySimulator
    extends ChannelInboundHandlerAdapter {
        private static final Timer TIMER = new HashedWheelTimer();
        private final int delay;
        private final int jitter;
        private final List<DelayedMessage> queuedMessages = Lists.newArrayList();

        public LatencySimulator(int $$0, int $$1) {
            this.delay = $$0;
            this.jitter = $$1;
        }

        public void channelRead(ChannelHandlerContext $$0, Object $$1) {
            this.delayDownstream($$0, $$1);
        }

        private void delayDownstream(ChannelHandlerContext $$0, Object $$1) {
            int $$2 = this.delay + (int)(Math.random() * (double)this.jitter);
            this.queuedMessages.add(new DelayedMessage($$0, $$1));
            TIMER.newTimeout(this::onTimeout, (long)$$2, TimeUnit.MILLISECONDS);
        }

        private void onTimeout(Timeout $$0) {
            DelayedMessage $$1 = this.queuedMessages.remove(0);
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

