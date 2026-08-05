/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInboundHandlerAdapter
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.HiddenByteBuf;

public class MonitoredLocalFrameDecoder
extends ChannelInboundHandlerAdapter {
    private final BandwidthDebugMonitor monitor;

    public MonitoredLocalFrameDecoder(BandwidthDebugMonitor $$0) {
        this.monitor = $$0;
    }

    public void channelRead(ChannelHandlerContext $$0, Object $$1) {
        if (($$1 = HiddenByteBuf.unpack($$1)) instanceof ByteBuf) {
            ByteBuf $$2 = (ByteBuf)$$1;
            this.monitor.onReceive($$2.readableBytes());
        }
        $$0.fireChannelRead($$1);
    }
}

