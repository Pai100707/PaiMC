package net.minecraft.network;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import net.minecraft.ReportedException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import org.slf4j.Logger;

public class PacketProcessor implements AutoCloseable {
   static final Logger LOGGER = LogUtils.getLogger();
   private final Queue<net.minecraft.network.PacketProcessor.ListenerAndPacket<?>> packetsToBeHandled = Queues.newConcurrentLinkedQueue();
   private final Thread runningThread;
   private boolean closed;

   public PacketProcessor(Thread $$0) {
      this.runningThread = $$0;
   }

   public boolean isSameThread() {
      return Thread.currentThread() == this.runningThread;
   }

   public <T extends net.minecraft.network.PacketListener> void scheduleIfPossible(T $$0, Packet<T> $$1) {
      if (this.closed) {
         throw new RejectedExecutionException("Server already shutting down");
      } else {
         this.packetsToBeHandled.add(new net.minecraft.network.PacketProcessor.ListenerAndPacket<>($$0, $$1));
      }
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

   record ListenerAndPacket<T extends net.minecraft.network.PacketListener>(T listener, Packet<T> packet) {
      public void handle() {
         if (this.listener.shouldHandleMessage(this.packet)) {
            try {
               this.packet.handle(this.listener);
            } catch (Exception var3) {
               if (var3 instanceof ReportedException $$1 && $$1.getCause() instanceof OutOfMemoryError) {
                  throw PacketUtils.makeReportedException(var3, this.packet, this.listener);
               }

               this.listener.onPacketError(this.packet, var3);
            }
         } else {
            net.minecraft.network.PacketProcessor.LOGGER.debug("Ignoring packet due to disconnection: {}", this.packet);
         }
      }
   }
}
