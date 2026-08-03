package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.ChunkPos;

public record ClientboundForgetLevelChunkPacket(ChunkPos pos) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundForgetLevelChunkPacket> STREAM_CODEC = Packet.codec(
      ClientboundForgetLevelChunkPacket::write, ClientboundForgetLevelChunkPacket::new
   );

   private ClientboundForgetLevelChunkPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readChunkPos());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeChunkPos(this.pos);
   }

   @Override
   public PacketType<ClientboundForgetLevelChunkPacket> type() {
      return GamePacketTypes.CLIENTBOUND_FORGET_LEVEL_CHUNK;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleForgetLevelChunk(this);
   }
}
