package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundBlockChangedAckPacket(int sequence) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundBlockChangedAckPacket> STREAM_CODEC = Packet.codec(
      ClientboundBlockChangedAckPacket::write, ClientboundBlockChangedAckPacket::new
   );

   private ClientboundBlockChangedAckPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readVarInt());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.sequence);
   }

   @Override
   public PacketType<ClientboundBlockChangedAckPacket> type() {
      return GamePacketTypes.CLIENTBOUND_BLOCK_CHANGED_ACK;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleBlockChangedAck(this);
   }
}
