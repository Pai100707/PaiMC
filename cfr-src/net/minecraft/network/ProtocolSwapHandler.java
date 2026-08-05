/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 */
package net.minecraft.network;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.UnconfiguredPipelineHandler;
import net.minecraft.network.protocol.Packet;

public interface ProtocolSwapHandler {
    public static void handleInboundTerminalPacket(ChannelHandlerContext $$0, Packet<?> $$1) {
        if ($$1.isTerminal()) {
            $$0.channel().config().setAutoRead(false);
            $$0.pipeline().addBefore($$0.name(), "inbound_config", (ChannelHandler)new UnconfiguredPipelineHandler.Inbound());
            $$0.pipeline().remove($$0.name());
        }
    }

    public static void handleOutboundTerminalPacket(ChannelHandlerContext $$0, Packet<?> $$1) {
        if ($$1.isTerminal()) {
            $$0.pipeline().addAfter($$0.name(), "outbound_config", (ChannelHandler)new UnconfiguredPipelineHandler.Outbound());
            $$0.pipeline().remove($$0.name());
        }
    }
}

