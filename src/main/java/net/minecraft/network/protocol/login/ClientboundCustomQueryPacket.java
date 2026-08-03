package net.minecraft.network.protocol.login;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import net.minecraft.network.protocol.login.custom.DiscardedQueryPayload;
import net.minecraft.resources.Identifier;

public record ClientboundCustomQueryPacket(int transactionId, CustomQueryPayload payload) implements Packet<ClientLoginPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundCustomQueryPacket> STREAM_CODEC = Packet.codec(
      ClientboundCustomQueryPacket::write, ClientboundCustomQueryPacket::new
   );
   private static final int MAX_PAYLOAD_SIZE = 1048576;

   private ClientboundCustomQueryPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readVarInt(), readPayload($$0.readIdentifier(), $$0));
   }

   private static CustomQueryPayload readPayload(Identifier $$0, net.minecraft.network.FriendlyByteBuf $$1) {
      return readUnknownPayload($$0, $$1);
   }

   private static DiscardedQueryPayload readUnknownPayload(Identifier $$0, net.minecraft.network.FriendlyByteBuf $$1) {
      int $$2 = $$1.readableBytes();
      if ($$2 >= 0 && $$2 <= 1048576) {
         $$1.skipBytes($$2);
         return new DiscardedQueryPayload($$0);
      } else {
         throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
      }
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.transactionId);
      $$0.writeIdentifier(this.payload.id());
      this.payload.write($$0);
   }

   @Override
   public PacketType<ClientboundCustomQueryPacket> type() {
      return LoginPacketTypes.CLIENTBOUND_CUSTOM_QUERY;
   }

   public void handle(ClientLoginPacketListener $$0) {
      $$0.handleCustomQuery(this);
   }
}
