package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundRemoveEntitiesPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundRemoveEntitiesPacket> STREAM_CODEC = Packet.codec(
      ClientboundRemoveEntitiesPacket::write, ClientboundRemoveEntitiesPacket::new
   );
   private final IntList entityIds;

   public ClientboundRemoveEntitiesPacket(IntList $$0) {
      this.entityIds = new IntArrayList($$0);
   }

   public ClientboundRemoveEntitiesPacket(int... $$0) {
      this.entityIds = new IntArrayList($$0);
   }

   private ClientboundRemoveEntitiesPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.entityIds = $$0.readIntIdList();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeIntIdList(this.entityIds);
   }

   @Override
   public PacketType<ClientboundRemoveEntitiesPacket> type() {
      return GamePacketTypes.CLIENTBOUND_REMOVE_ENTITIES;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleRemoveEntities(this);
   }

   public IntList getEntityIds() {
      return this.entityIds;
   }
}
