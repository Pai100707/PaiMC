package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundEntityTagQueryPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundEntityTagQueryPacket> STREAM_CODEC = Packet.codec(
      ServerboundEntityTagQueryPacket::write, ServerboundEntityTagQueryPacket::new
   );
   private final int transactionId;
   private final int entityId;

   public ServerboundEntityTagQueryPacket(int $$0, int $$1) {
      this.transactionId = $$0;
      this.entityId = $$1;
   }

   private ServerboundEntityTagQueryPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.transactionId = $$0.readVarInt();
      this.entityId = $$0.readVarInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.transactionId);
      $$0.writeVarInt(this.entityId);
   }

   @Override
   public PacketType<ServerboundEntityTagQueryPacket> type() {
      return GamePacketTypes.SERVERBOUND_ENTITY_TAG_QUERY;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleEntityTagQuery(this);
   }

   public int getTransactionId() {
      return this.transactionId;
   }

   public int getEntityId() {
      return this.entityId;
   }
}
