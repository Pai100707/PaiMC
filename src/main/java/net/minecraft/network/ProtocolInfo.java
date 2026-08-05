package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.VisibleForDebug;

public interface ProtocolInfo<T extends net.minecraft.network.PacketListener> {
   net.minecraft.network.ConnectionProtocol id();

   PacketFlow flow();

   StreamCodec<ByteBuf, Packet<? super T>> codec();

   
   BundlerInfo bundlerInfo();

   public interface Details {
      net.minecraft.network.ConnectionProtocol id();

      PacketFlow flow();

      @VisibleForDebug
      void listPackets(net.minecraft.network.ProtocolInfo.Details.PacketVisitor var1);

      @FunctionalInterface
      public interface PacketVisitor {
         void accept(PacketType<?> var1, int var2);
      }
   }

   public interface DetailsProvider {
      net.minecraft.network.ProtocolInfo.Details details();
   }
}
