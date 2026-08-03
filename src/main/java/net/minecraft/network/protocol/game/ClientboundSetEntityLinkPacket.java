package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class ClientboundSetEntityLinkPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundSetEntityLinkPacket> STREAM_CODEC = Packet.codec(
      ClientboundSetEntityLinkPacket::write, ClientboundSetEntityLinkPacket::new
   );
   private final int sourceId;
   private final int destId;

   public ClientboundSetEntityLinkPacket(Entity $$0, @Nullable Entity $$1) {
      this.sourceId = $$0.getId();
      this.destId = $$1 != null ? $$1.getId() : 0;
   }

   private ClientboundSetEntityLinkPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.sourceId = $$0.readInt();
      this.destId = $$0.readInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeInt(this.sourceId);
      $$0.writeInt(this.destId);
   }

   @Override
   public PacketType<ClientboundSetEntityLinkPacket> type() {
      return GamePacketTypes.CLIENTBOUND_SET_ENTITY_LINK;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleEntityLinkPacket(this);
   }

   public int getSourceId() {
      return this.sourceId;
   }

   public int getDestId() {
      return this.destId;
   }
}
