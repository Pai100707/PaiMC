/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.network;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import org.slf4j.Logger;

public class PacketProcessor
implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    private final Queue<ListenerAndPacket<?>> packetsToBeHandled = Queues.newConcurrentLinkedQueue();
    private final Thread runningThread;
    private boolean closed;

    public PacketProcessor(Thread $$0) {
        this.runningThread = $$0;
    }

    public boolean isSameThread() {
        return Thread.currentThread() == this.runningThread;
    }

    public <T extends PacketListener> void scheduleIfPossible(T $$0, Packet<T> $$1) {
        if (this.closed) {
            throw new RejectedExecutionException("Server already shutting down");
        }
        this.packetsToBeHandled.add(new ListenerAndPacket<T>($$0, $$1));
    }

    public void processQueuedPackets() {
        if (!this.closed) {
            while (!this.packetsToBeHandled.isEmpty()) {
                this.packetsToBeHandled.poll().handle();
            }
        }
    }

    @Override
    public void close() {
        this.closed = true;
    }

    record ListenerAndPacket<T extends PacketListener>(T listener, Packet<T> packet) {
        public void handle() {
            if (this.listener.shouldHandleMessage(this.packet)) {
                try {
                    this.packet.handle(this.listener);
                }
                catch (Exception $$0) {
                    ReportedException $$1;
                    if ($$0 instanceof ReportedException && ($$1 = (ReportedException)$$0).getCause() instanceof OutOfMemoryError) {
                        throw PacketUtils.makeReportedException($$0, this.packet, this.listener);
                    }
                    this.listener.onPacketError(this.packet, $$0);
                }
            } else {
                LOGGER.debug("Ignoring packet due to disconnection: {}", this.packet);
            }
        }
    }
}

