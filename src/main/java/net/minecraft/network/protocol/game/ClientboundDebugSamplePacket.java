package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.debugchart.RemoteDebugSampleType;

public record ClientboundDebugSamplePacket(long[] sample, RemoteDebugSampleType debugSampleType) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundDebugSamplePacket> STREAM_CODEC = Packet.codec(
      ClientboundDebugSamplePacket::write, ClientboundDebugSamplePacket::new
   );

   private ClientboundDebugSamplePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readLongArray(), $$0.readEnum(RemoteDebugSampleType.class));
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeLongArray(this.sample);
      $$0.writeEnum(this.debugSampleType);
   }

   @Override
   public PacketType<ClientboundDebugSamplePacket> type() {
      return GamePacketTypes.CLIENTBOUND_DEBUG_SAMPLE;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleDebugSample(this);
   }
}
