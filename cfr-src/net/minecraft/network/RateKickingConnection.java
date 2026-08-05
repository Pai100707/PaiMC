/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import org.slf4j.Logger;

public class RateKickingConnection
extends Connection {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component EXCEED_REASON = Component.translatable("disconnect.exceeded_packet_rate");
    private final int rateLimitPacketsPerSecond;

    public RateKickingConnection(int $$0) {
        super(PacketFlow.SERVERBOUND);
        this.rateLimitPacketsPerSecond = $$0;
    }

    @Override
    protected void tickSecond() {
        super.tickSecond();
        float $$0 = this.getAverageReceivedPackets();
        if ($$0 > (float)this.rateLimitPacketsPerSecond) {
            LOGGER.warn("Player exceeded rate-limit (sent {} packets per second)", (Object)Float.valueOf($$0));
            this.send(new ClientboundDisconnectPacket(EXCEED_REASON), PacketSendListener.thenRun(() -> this.disconnect(EXCEED_REASON)));
            this.setReadOnly();
        }
    }
}

