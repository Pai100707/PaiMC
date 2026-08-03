package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetChunkCacheRadiusPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundSetChunkCacheRadiusPacket> STREAM_CODEC = Packet.codec(
      ClientboundSetChunkCacheRadiusPacket::write, ClientboundSetChunkCacheRadiusPacket::new
   );
   private final int radius;

   public ClientboundSetChunkCacheRadiusPacket(int $$0) {
      this.radius = $$0;
   }

   private ClientboundSetChunkCacheRadiusPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.radius = $$0.readVarInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.radius);
   }

   @Override
   public PacketType<ClientboundSetChunkCacheRadiusPacket> type() {
      return GamePacketTypes.CLIENTBOUND_SET_CHUNK_CACHE_RADIUS;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleSetChunkCacheRadius(this);
   }

   public int getRadius() {
      return this.radius;
   }
}
