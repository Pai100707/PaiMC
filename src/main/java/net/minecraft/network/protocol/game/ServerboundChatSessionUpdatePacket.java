package net.minecraft.network.protocol.game;

import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundChatSessionUpdatePacket(RemoteChatSession.Data chatSession) implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundChatSessionUpdatePacket> STREAM_CODEC = Packet.codec(
      ServerboundChatSessionUpdatePacket::write, ServerboundChatSessionUpdatePacket::new
   );

   private ServerboundChatSessionUpdatePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this(RemoteChatSession.Data.read($$0));
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      RemoteChatSession.Data.write($$0, this.chatSession);
   }

   @Override
   public PacketType<ServerboundChatSessionUpdatePacket> type() {
      return GamePacketTypes.SERVERBOUND_CHAT_SESSION_UPDATE;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleChatSessionUpdate(this);
   }
}
