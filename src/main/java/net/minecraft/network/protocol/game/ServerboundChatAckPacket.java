package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundChatAckPacket(int offset) implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundChatAckPacket> STREAM_CODEC = Packet.codec(
      ServerboundChatAckPacket::write, ServerboundChatAckPacket::new
   );

   private ServerboundChatAckPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readVarInt());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.offset);
   }

   @Override
   public PacketType<ServerboundChatAckPacket> type() {
      return GamePacketTypes.SERVERBOUND_CHAT_ACK;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleChatAck(this);
   }
}
