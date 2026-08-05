/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  com.mojang.logging.LogUtils
 *  io.netty.bootstrap.Bootstrap
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelException
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelFutureListener
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandler
 *  io.netty.channel.ChannelInitializer
 *  io.netty.channel.ChannelOption
 *  io.netty.channel.ChannelOutboundHandler
 *  io.netty.channel.ChannelOutboundHandlerAdapter
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.ChannelPromise
 *  io.netty.channel.SimpleChannelInboundHandler
 *  io.netty.channel.local.LocalChannel
 *  io.netty.channel.local.LocalServerChannel
 *  io.netty.handler.flow.FlowControlHandler
 *  io.netty.handler.timeout.ReadTimeoutHandler
 *  io.netty.handler.timeout.TimeoutException
 *  io.netty.util.concurrent.GenericFutureListener
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 *  org.slf4j.Marker
 *  org.slf4j.MarkerFactory
 */
package net.minecraft.network;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.handler.flow.FlowControlHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import javax.crypto.Cipher;
import net.minecraft.SharedConstants;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.CipherDecoder;
import net.minecraft.network.CipherEncoder;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.CompressionEncoder;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.LocalFrameDecoder;
import net.minecraft.network.LocalFrameEncoder;
import net.minecraft.network.MonitoredLocalFrameDecoder;
import net.minecraft.network.PacketBundlePacker;
import net.minecraft.network.PacketBundleUnpacker;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.SkipPacketException;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.UnconfiguredPipelineHandler;
import net.minecraft.network.Varint21FrameDecoder;
import net.minecraft.network.Varint21LengthFieldPrepender;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.StatusProtocols;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.debugchart.LocalSampleLogger;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Connection
extends SimpleChannelInboundHandler<Packet<?>> {
    private static final float AVERAGE_PACKETS_SMOOTHING = 0.75f;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Marker ROOT_MARKER = MarkerFactory.getMarker((String)"NETWORK");
    public static final Marker PACKET_MARKER = Util.make(MarkerFactory.getMarker((String)"NETWORK_PACKETS"), $$0 -> $$0.add(ROOT_MARKER));
    public static final Marker PACKET_RECEIVED_MARKER = Util.make(MarkerFactory.getMarker((String)"PACKET_RECEIVED"), $$0 -> $$0.add(PACKET_MARKER));
    public static final Marker PACKET_SENT_MARKER = Util.make(MarkerFactory.getMarker((String)"PACKET_SENT"), $$0 -> $$0.add(PACKET_MARKER));
    private static final ProtocolInfo<ServerHandshakePacketListener> INITIAL_PROTOCOL = HandshakeProtocols.SERVERBOUND;
    private final PacketFlow receiving;
    private volatile boolean sendLoginDisconnect = true;
    private final Queue<Consumer<Connection>> pendingActions = Queues.newConcurrentLinkedQueue();
    private Channel channel;
    private SocketAddress address;
    private volatile @Nullable PacketListener disconnectListener;
    private volatile @Nullable PacketListener packetListener;
    private @Nullable DisconnectionDetails disconnectionDetails;
    private boolean encrypted;
    private boolean disconnectionHandled;
    private int receivedPackets;
    private int sentPackets;
    private float averageReceivedPackets;
    private float averageSentPackets;
    private int tickCount;
    private boolean handlingFault;
    private volatile @Nullable DisconnectionDetails delayedDisconnect;
    @Nullable BandwidthDebugMonitor bandwidthDebugMonitor;

    public Connection(PacketFlow $$0) {
        this.receiving = $$0;
    }

    public void channelActive(ChannelHandlerContext $$0) throws Exception {
        super.channelActive($$0);
        this.channel = $$0.channel();
        this.address = this.channel.remoteAddress();
        if (this.delayedDisconnect != null) {
            this.disconnect(this.delayedDisconnect);
        }
    }

    public void channelInactive(ChannelHandlerContext $$0) {
        this.disconnect(Component.translatable("disconnect.endOfStream"));
    }

    public void exceptionCaught(ChannelHandlerContext $$0, Throwable $$1) {
        if ($$1 instanceof SkipPacketException) {
            LOGGER.debug("Skipping packet due to errors", $$1.getCause());
            return;
        }
        boolean $$2 = !this.handlingFault;
        this.handlingFault = true;
        if (!this.channel.isOpen()) {
            return;
        }
        if ($$1 instanceof TimeoutException) {
            LOGGER.debug("Timeout", $$1);
            this.disconnect(Component.translatable("disconnect.timeout"));
        } else {
            DisconnectionDetails $$6;
            MutableComponent $$3 = Component.translatable("disconnect.genericReason", "Internal Exception: " + String.valueOf($$1));
            PacketListener $$4 = this.packetListener;
            if ($$4 != null) {
                DisconnectionDetails $$5 = $$4.createDisconnectionInfo($$3, $$1);
            } else {
                $$6 = new DisconnectionDetails($$3);
            }
            if ($$2) {
                LOGGER.debug("Failed to sent packet", $$1);
                if (this.getSending() == PacketFlow.CLIENTBOUND) {
                    Record $$7 = this.sendLoginDisconnect ? new ClientboundLoginDisconnectPacket($$3) : new ClientboundDisconnectPacket($$3);
                    this.send((Packet<?>)((Object)$$7), PacketSendListener.thenRun(() -> this.disconnect($$6)));
                } else {
                    this.disconnect($$6);
                }
                this.setReadOnly();
            } else {
                LOGGER.debug("Double fault", $$1);
                this.disconnect($$6);
            }
        }
    }

    protected void channelRead0(ChannelHandlerContext $$0, Packet<?> $$1) {
        if (!this.channel.isOpen()) {
            return;
        }
        PacketListener $$2 = this.packetListener;
        if ($$2 == null) {
            throw new IllegalStateException("Received a packet before the packet listener was initialized");
        }
        if ($$2.shouldHandleMessage($$1)) {
            try {
                Connection.genericsFtw($$1, $$2);
            }
            catch (RunningOnDifferentThreadException runningOnDifferentThreadException) {
            }
            catch (RejectedExecutionException $$3) {
                this.disconnect(Component.translatable("multiplayer.disconnect.server_shutdown"));
            }
            catch (ClassCastException $$4) {
                LOGGER.error("Received {} that couldn't be processed", $$1.getClass(), (Object)$$4);
                this.disconnect(Component.translatable("multiplayer.disconnect.invalid_packet"));
            }
            ++this.receivedPackets;
        }
    }

    private static <T extends PacketListener> void genericsFtw(Packet<T> $$0, PacketListener $$1) {
        $$0.handle($$1);
    }

    private void validateListener(ProtocolInfo<?> $$0, PacketListener $$1) {
        Objects.requireNonNull($$1, "packetListener");
        PacketFlow $$2 = $$1.flow();
        if ($$2 != this.receiving) {
            throw new IllegalStateException("Trying to set listener for wrong side: connection is " + String.valueOf((Object)this.receiving) + ", but listener is " + String.valueOf((Object)$$2));
        }
        ConnectionProtocol $$3 = $$1.protocol();
        if ($$0.id() != $$3) {
            throw new IllegalStateException("Listener protocol (" + String.valueOf((Object)$$3) + ") does not match requested one " + String.valueOf($$0));
        }
    }

    private static void syncAfterConfigurationChange(ChannelFuture $$0) {
        try {
            $$0.syncUninterruptibly();
        }
        catch (Exception $$1) {
            if ($$1 instanceof ClosedChannelException) {
                LOGGER.info("Connection closed during protocol change");
                return;
            }
            throw $$1;
        }
    }

    public <T extends PacketListener> void setupInboundProtocol(ProtocolInfo<T> $$0, T $$12) {
        this.validateListener($$0, $$12);
        if ($$0.flow() != this.getReceiving()) {
            throw new IllegalStateException("Invalid inbound protocol: " + String.valueOf((Object)$$0.id()));
        }
        this.packetListener = $$12;
        this.disconnectListener = null;
        UnconfiguredPipelineHandler.InboundConfigurationTask $$2 = UnconfiguredPipelineHandler.setupInboundProtocol($$0);
        BundlerInfo $$3 = $$0.bundlerInfo();
        if ($$3 != null) {
            PacketBundlePacker $$4 = new PacketBundlePacker($$3);
            $$2 = $$2.andThen($$1 -> $$1.pipeline().addAfter("decoder", "bundler", (ChannelHandler)$$4));
        }
        Connection.syncAfterConfigurationChange(this.channel.writeAndFlush((Object)$$2));
    }

    public void setupOutboundProtocol(ProtocolInfo<?> $$0) {
        if ($$0.flow() != this.getSending()) {
            throw new IllegalStateException("Invalid outbound protocol: " + String.valueOf((Object)$$0.id()));
        }
        UnconfiguredPipelineHandler.OutboundConfigurationTask $$12 = UnconfiguredPipelineHandler.setupOutboundProtocol($$0);
        BundlerInfo $$2 = $$0.bundlerInfo();
        if ($$2 != null) {
            PacketBundleUnpacker $$3 = new PacketBundleUnpacker($$2);
            $$12 = $$12.andThen($$1 -> $$1.pipeline().addAfter("encoder", "unbundler", (ChannelHandler)$$3));
        }
        boolean $$4 = $$0.id() == ConnectionProtocol.LOGIN;
        Connection.syncAfterConfigurationChange(this.channel.writeAndFlush((Object)$$12.andThen($$1 -> {
            this.sendLoginDisconnect = $$4;
        })));
    }

    public void setListenerForServerboundHandshake(PacketListener $$0) {
        if (this.packetListener != null) {
            throw new IllegalStateException("Listener already set");
        }
        if (this.receiving != PacketFlow.SERVERBOUND || $$0.flow() != PacketFlow.SERVERBOUND || $$0.protocol() != INITIAL_PROTOCOL.id()) {
            throw new IllegalStateException("Invalid initial listener");
        }
        this.packetListener = $$0;
    }

    public void initiateServerboundStatusConnection(String $$0, int $$1, ClientStatusPacketListener $$2) {
        this.initiateServerboundConnection($$0, $$1, StatusProtocols.SERVERBOUND, StatusProtocols.CLIENTBOUND, $$2, ClientIntent.STATUS);
    }

    public void initiateServerboundPlayConnection(String $$0, int $$1, ClientLoginPacketListener $$2) {
        this.initiateServerboundConnection($$0, $$1, LoginProtocols.SERVERBOUND, LoginProtocols.CLIENTBOUND, $$2, ClientIntent.LOGIN);
    }

    public <S extends ServerboundPacketListener, C extends ClientboundPacketListener> void initiateServerboundPlayConnection(String $$0, int $$1, ProtocolInfo<S> $$2, ProtocolInfo<C> $$3, C $$4, boolean $$5) {
        this.initiateServerboundConnection($$0, $$1, $$2, $$3, $$4, $$5 ? ClientIntent.TRANSFER : ClientIntent.LOGIN);
    }

    private <S extends ServerboundPacketListener, C extends ClientboundPacketListener> void initiateServerboundConnection(String $$0, int $$1, ProtocolInfo<S> $$2, ProtocolInfo<C> $$3, C $$4, ClientIntent $$5) {
        if ($$2.id() != $$3.id()) {
            throw new IllegalStateException("Mismatched initial protocols");
        }
        this.disconnectListener = $$4;
        this.runOnceConnected($$6 -> {
            this.setupInboundProtocol($$3, $$4);
            $$6.sendPacket(new ClientIntentionPacket(SharedConstants.getCurrentVersion().protocolVersion(), $$0, $$1, $$5), null, true);
            this.setupOutboundProtocol($$2);
        });
    }

    public void send(Packet<?> $$0) {
        this.send($$0, null);
    }

    public void send(Packet<?> $$0, @Nullable ChannelFutureListener $$1) {
        this.send($$0, $$1, true);
    }

    public void send(Packet<?> $$0, @Nullable ChannelFutureListener $$1, boolean $$2) {
        if (this.isConnected()) {
            this.flushQueue();
            this.sendPacket($$0, $$1, $$2);
        } else {
            this.pendingActions.add($$3 -> $$3.sendPacket($$0, $$1, $$2));
        }
    }

    public void runOnceConnected(Consumer<Connection> $$0) {
        if (this.isConnected()) {
            this.flushQueue();
            $$0.accept(this);
        } else {
            this.pendingActions.add($$0);
        }
    }

    private void sendPacket(Packet<?> $$0, @Nullable ChannelFutureListener $$1, boolean $$2) {
        ++this.sentPackets;
        if (this.channel.eventLoop().inEventLoop()) {
            this.doSendPacket($$0, $$1, $$2);
        } else {
            this.channel.eventLoop().execute(() -> this.doSendPacket($$0, $$1, $$2));
        }
    }

    private void doSendPacket(Packet<?> $$0, @Nullable ChannelFutureListener $$1, boolean $$2) {
        if ($$1 != null) {
            ChannelFuture $$3 = $$2 ? this.channel.writeAndFlush($$0) : this.channel.write($$0);
            $$3.addListener((GenericFutureListener)$$1);
        } else if ($$2) {
            this.channel.writeAndFlush($$0, this.channel.voidPromise());
        } else {
            this.channel.write($$0, this.channel.voidPromise());
        }
    }

    public void flushChannel() {
        if (this.isConnected()) {
            this.flush();
        } else {
            this.pendingActions.add(Connection::flush);
        }
    }

    private void flush() {
        if (this.channel.eventLoop().inEventLoop()) {
            this.channel.flush();
        } else {
            this.channel.eventLoop().execute(() -> this.channel.flush());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void flushQueue() {
        if (this.channel == null || !this.channel.isOpen()) {
            return;
        }
        Queue<Consumer<Connection>> queue = this.pendingActions;
        synchronized (queue) {
            Consumer<Connection> $$0;
            while (($$0 = this.pendingActions.poll()) != null) {
                $$0.accept(this);
            }
        }
    }

    public void tick() {
        this.flushQueue();
        PacketListener packetListener = this.packetListener;
        if (packetListener instanceof TickablePacketListener) {
            TickablePacketListener $$0 = (TickablePacketListener)packetListener;
            $$0.tick();
        }
        if (!this.isConnected() && !this.disconnectionHandled) {
            this.handleDisconnection();
        }
        if (this.channel != null) {
            this.channel.flush();
        }
        if (this.tickCount++ % 20 == 0) {
            this.tickSecond();
        }
        if (this.bandwidthDebugMonitor != null) {
            this.bandwidthDebugMonitor.tick();
        }
    }

    protected void tickSecond() {
        this.averageSentPackets = Mth.lerp(0.75f, this.sentPackets, this.averageSentPackets);
        this.averageReceivedPackets = Mth.lerp(0.75f, this.receivedPackets, this.averageReceivedPackets);
        this.sentPackets = 0;
        this.receivedPackets = 0;
    }

    public SocketAddress getRemoteAddress() {
        return this.address;
    }

    public String getLoggableAddress(boolean $$0) {
        if (this.address == null) {
            return "local";
        }
        if ($$0) {
            return this.address.toString();
        }
        return "IP hidden";
    }

    public void disconnect(Component $$0) {
        this.disconnect(new DisconnectionDetails($$0));
    }

    public void disconnect(DisconnectionDetails $$0) {
        if (this.channel == null) {
            this.delayedDisconnect = $$0;
        }
        if (this.isConnected()) {
            this.channel.close().awaitUninterruptibly();
            this.disconnectionDetails = $$0;
        }
    }

    public boolean isMemoryConnection() {
        return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
    }

    public PacketFlow getReceiving() {
        return this.receiving;
    }

    public PacketFlow getSending() {
        return this.receiving.getOpposite();
    }

    public static Connection connectToServer(InetSocketAddress $$0, EventLoopGroupHolder $$1, @Nullable LocalSampleLogger $$2) {
        Connection $$3 = new Connection(PacketFlow.CLIENTBOUND);
        if ($$2 != null) {
            $$3.setBandwidthLogger($$2);
        }
        ChannelFuture $$4 = Connection.connect($$0, $$1, $$3);
        $$4.syncUninterruptibly();
        return $$3;
    }

    public static ChannelFuture connect(InetSocketAddress $$0, EventLoopGroupHolder $$1, final Connection $$2) {
        return ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group($$1.eventLoopGroup())).handler((ChannelHandler)new ChannelInitializer<Channel>(){

            protected void initChannel(Channel $$0) {
                try {
                    $$0.config().setOption(ChannelOption.TCP_NODELAY, (Object)true);
                }
                catch (ChannelException channelException) {
                    // empty catch block
                }
                ChannelPipeline $$1 = $$0.pipeline().addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30));
                Connection.configureSerialization($$1, PacketFlow.CLIENTBOUND, false, $$2.bandwidthDebugMonitor);
                $$2.configurePacketHandler($$1);
            }
        })).channel($$1.channelCls())).connect($$0.getAddress(), $$0.getPort());
    }

    private static String outboundHandlerName(boolean $$0) {
        return $$0 ? "encoder" : "outbound_config";
    }

    private static String inboundHandlerName(boolean $$0) {
        return $$0 ? "decoder" : "inbound_config";
    }

    public void configurePacketHandler(ChannelPipeline $$0) {
        $$0.addLast("hackfix", (ChannelHandler)new ChannelOutboundHandlerAdapter(this){

            public void write(ChannelHandlerContext $$0, Object $$1, ChannelPromise $$2) throws Exception {
                super.write($$0, $$1, $$2);
            }
        }).addLast("packet_handler", (ChannelHandler)this);
    }

    public static void configureSerialization(ChannelPipeline $$0, PacketFlow $$1, boolean $$2, @Nullable BandwidthDebugMonitor $$3) {
        PacketFlow $$4 = $$1.getOpposite();
        boolean $$5 = $$1 == PacketFlow.SERVERBOUND;
        boolean $$6 = $$4 == PacketFlow.SERVERBOUND;
        $$0.addLast("splitter", (ChannelHandler)Connection.createFrameDecoder($$3, $$2)).addLast(new ChannelHandler[]{new FlowControlHandler()}).addLast(Connection.inboundHandlerName($$5), $$5 ? new PacketDecoder<ServerHandshakePacketListener>(INITIAL_PROTOCOL) : new UnconfiguredPipelineHandler.Inbound()).addLast("prepender", (ChannelHandler)Connection.createFrameEncoder($$2)).addLast(Connection.outboundHandlerName($$6), $$6 ? new PacketEncoder<ServerHandshakePacketListener>(INITIAL_PROTOCOL) : new UnconfiguredPipelineHandler.Outbound());
    }

    private static ChannelOutboundHandler createFrameEncoder(boolean $$0) {
        return $$0 ? new LocalFrameEncoder() : new Varint21LengthFieldPrepender();
    }

    private static ChannelInboundHandler createFrameDecoder(@Nullable BandwidthDebugMonitor $$0, boolean $$1) {
        if (!$$1) {
            return new Varint21FrameDecoder($$0);
        }
        if ($$0 != null) {
            return new MonitoredLocalFrameDecoder($$0);
        }
        return new LocalFrameDecoder();
    }

    public static void configureInMemoryPipeline(ChannelPipeline $$0, PacketFlow $$1) {
        Connection.configureSerialization($$0, $$1, true, null);
    }

    public static Connection connectToLocalServer(SocketAddress $$0) {
        final Connection $$1 = new Connection(PacketFlow.CLIENTBOUND);
        ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group(EventLoopGroupHolder.local().eventLoopGroup())).handler((ChannelHandler)new ChannelInitializer<Channel>(){

            protected void initChannel(Channel $$0) {
                ChannelPipeline $$12 = $$0.pipeline();
                Connection.configureInMemoryPipeline($$12, PacketFlow.CLIENTBOUND);
                $$1.configurePacketHandler($$12);
            }
        })).channel(EventLoopGroupHolder.local().channelCls())).connect($$0).syncUninterruptibly();
        return $$1;
    }

    public void setEncryptionKey(Cipher $$0, Cipher $$1) {
        this.encrypted = true;
        this.channel.pipeline().addBefore("splitter", "decrypt", (ChannelHandler)new CipherDecoder($$0));
        this.channel.pipeline().addBefore("prepender", "encrypt", (ChannelHandler)new CipherEncoder($$1));
    }

    public boolean isEncrypted() {
        return this.encrypted;
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen();
    }

    public boolean isConnecting() {
        return this.channel == null;
    }

    public @Nullable PacketListener getPacketListener() {
        return this.packetListener;
    }

    public @Nullable DisconnectionDetails getDisconnectionDetails() {
        return this.disconnectionDetails;
    }

    public void setReadOnly() {
        if (this.channel != null) {
            this.channel.config().setAutoRead(false);
        }
    }

    public void setupCompression(int $$0, boolean $$1) {
        if ($$0 >= 0) {
            ChannelHandler channelHandler = this.channel.pipeline().get("decompress");
            if (channelHandler instanceof CompressionDecoder) {
                CompressionDecoder $$2 = (CompressionDecoder)channelHandler;
                $$2.setThreshold($$0, $$1);
            } else {
                this.channel.pipeline().addAfter("splitter", "decompress", (ChannelHandler)new CompressionDecoder($$0, $$1));
            }
            channelHandler = this.channel.pipeline().get("compress");
            if (channelHandler instanceof CompressionEncoder) {
                CompressionEncoder $$3 = (CompressionEncoder)channelHandler;
                $$3.setThreshold($$0);
            } else {
                this.channel.pipeline().addAfter("prepender", "compress", (ChannelHandler)new CompressionEncoder($$0));
            }
        } else {
            if (this.channel.pipeline().get("decompress") instanceof CompressionDecoder) {
                this.channel.pipeline().remove("decompress");
            }
            if (this.channel.pipeline().get("compress") instanceof CompressionEncoder) {
                this.channel.pipeline().remove("compress");
            }
        }
    }

    public void handleDisconnection() {
        PacketListener $$1;
        if (this.channel == null || this.channel.isOpen()) {
            return;
        }
        if (this.disconnectionHandled) {
            LOGGER.warn("handleDisconnection() called twice");
            return;
        }
        this.disconnectionHandled = true;
        PacketListener $$0 = this.getPacketListener();
        PacketListener packetListener = $$1 = $$0 != null ? $$0 : this.disconnectListener;
        if ($$1 != null) {
            DisconnectionDetails $$2 = Objects.requireNonNullElseGet(this.getDisconnectionDetails(), () -> new DisconnectionDetails(Component.translatable("multiplayer.disconnect.generic")));
            $$1.onDisconnect($$2);
        }
    }

    public float getAverageReceivedPackets() {
        return this.averageReceivedPackets;
    }

    public float getAverageSentPackets() {
        return this.averageSentPackets;
    }

    public void setBandwidthLogger(LocalSampleLogger $$0) {
        this.bandwidthDebugMonitor = new BandwidthDebugMonitor($$0);
    }

    protected /* synthetic */ void channelRead0(ChannelHandlerContext channelHandlerContext, Object object) throws Exception {
        this.channelRead0(channelHandlerContext, (Packet)object);
    }
}

