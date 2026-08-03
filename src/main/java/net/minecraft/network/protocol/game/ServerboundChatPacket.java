package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.jspecify.annotations.Nullable;

public record ServerboundChatPacket(String message, Instant timeStamp, long salt, @Nullable MessageSignature signature, LastSeenMessages.Update lastSeenMessages)
   implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundChatPacket> STREAM_CODEC = Packet.codec(
      ServerboundChatPacket::write, ServerboundChatPacket::new
   );

   private ServerboundChatPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readUtf(256), $$0.readInstant(), $$0.readLong(), $$0.readNullable(MessageSignature::read), new LastSeenMessages.Update($$0));
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeUtf(this.message, 256);
      $$0.writeInstant(this.timeStamp);
      $$0.writeLong(this.salt);
      $$0.writeNullable(this.signature, MessageSignature::write);
      this.lastSeenMessages.write($$0);
   }

   @Override
   public PacketType<ServerboundChatPacket> type() {
      return GamePacketTypes.SERVERBOUND_CHAT;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleChat(this);
   }
}
