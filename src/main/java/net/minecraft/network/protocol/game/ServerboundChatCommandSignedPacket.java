package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundChatCommandSignedPacket(
   String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, LastSeenMessages.Update lastSeenMessages
) implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundChatCommandSignedPacket> STREAM_CODEC = Packet.codec(
      ServerboundChatCommandSignedPacket::write, ServerboundChatCommandSignedPacket::new
   );

   private ServerboundChatCommandSignedPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readUtf(), $$0.readInstant(), $$0.readLong(), new ArgumentSignatures($$0), new LastSeenMessages.Update($$0));
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeUtf(this.command);
      $$0.writeInstant(this.timeStamp);
      $$0.writeLong(this.salt);
      this.argumentSignatures.write($$0);
      this.lastSeenMessages.write($$0);
   }

   @Override
   public PacketType<ServerboundChatCommandSignedPacket> type() {
      return GamePacketTypes.SERVERBOUND_CHAT_COMMAND_SIGNED;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleSignedChatCommand(this);
   }
}
