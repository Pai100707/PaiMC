package net.minecraft.network.protocol.login;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.minecraft.network.protocol.login.custom.DiscardedQueryAnswerPayload;
import org.jspecify.annotations.Nullable;

public record ServerboundCustomQueryAnswerPacket(int transactionId, @Nullable CustomQueryAnswerPayload payload) implements Packet<ServerLoginPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundCustomQueryAnswerPacket> STREAM_CODEC = Packet.codec(
      ServerboundCustomQueryAnswerPacket::write, ServerboundCustomQueryAnswerPacket::read
   );
   private static final int MAX_PAYLOAD_SIZE = 1048576;

   private static ServerboundCustomQueryAnswerPacket read(net.minecraft.network.FriendlyByteBuf $$0) {
      int $$1 = $$0.readVarInt();
      return new ServerboundCustomQueryAnswerPacket($$1, readPayload($$1, $$0));
   }

   private static CustomQueryAnswerPayload readPayload(int $$0, net.minecraft.network.FriendlyByteBuf $$1) {
      return readUnknownPayload($$1);
   }

   private static CustomQueryAnswerPayload readUnknownPayload(net.minecraft.network.FriendlyByteBuf $$0) {
      int $$1 = $$0.readableBytes();
      if ($$1 >= 0 && $$1 <= 1048576) {
         $$0.skipBytes($$1);
         return DiscardedQueryAnswerPayload.INSTANCE;
      } else {
         throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
      }
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.transactionId);
      $$0.writeNullable(this.payload, ($$0x, $$1) -> $$1.write($$0x));
   }

   @Override
   public PacketType<ServerboundCustomQueryAnswerPacket> type() {
      return LoginPacketTypes.SERVERBOUND_CUSTOM_QUERY_ANSWER;
   }

   public void handle(ServerLoginPacketListener $$0) {
      $$0.handleCustomQueryPacket(this);
   }
}
