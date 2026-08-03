package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundChunkBatchFinishedPacket(int batchSize) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundChunkBatchFinishedPacket> STREAM_CODEC = Packet.codec(
      ClientboundChunkBatchFinishedPacket::write, ClientboundChunkBatchFinishedPacket::new
   );

   private ClientboundChunkBatchFinishedPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readVarInt());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.batchSize);
   }

   @Override
   public PacketType<ClientboundChunkBatchFinishedPacket> type() {
      return GamePacketTypes.CLIENTBOUND_CHUNK_BATCH_FINISHED;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleChunkBatchFinished(this);
   }
}
