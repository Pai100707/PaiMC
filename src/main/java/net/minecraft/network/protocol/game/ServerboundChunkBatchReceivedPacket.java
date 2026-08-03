package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundChunkBatchReceivedPacket(float desiredChunksPerTick) implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundChunkBatchReceivedPacket> STREAM_CODEC = Packet.codec(
      ServerboundChunkBatchReceivedPacket::write, ServerboundChunkBatchReceivedPacket::new
   );

   private ServerboundChunkBatchReceivedPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readFloat());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeFloat(this.desiredChunksPerTick);
   }

   @Override
   public PacketType<ServerboundChunkBatchReceivedPacket> type() {
      return GamePacketTypes.SERVERBOUND_CHUNK_BATCH_RECEIVED;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleChunkBatchReceived(this);
   }
}
