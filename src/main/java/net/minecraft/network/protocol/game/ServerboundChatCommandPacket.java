package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundChatCommandPacket(String command) implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundChatCommandPacket> STREAM_CODEC = Packet.codec(
      ServerboundChatCommandPacket::write, ServerboundChatCommandPacket::new
   );

   private ServerboundChatCommandPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readUtf());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeUtf(this.command);
   }

   @Override
   public PacketType<ServerboundChatCommandPacket> type() {
      return GamePacketTypes.SERVERBOUND_CHAT_COMMAND;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleChatCommand(this);
   }
}
