package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;

public class ClientboundSetPassengersPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundSetPassengersPacket> STREAM_CODEC = Packet.codec(
      ClientboundSetPassengersPacket::write, ClientboundSetPassengersPacket::new
   );
   private final int vehicle;
   private final int[] passengers;

   public ClientboundSetPassengersPacket(Entity $$0) {
      this.vehicle = $$0.getId();
      List<Entity> $$1 = $$0.getPassengers();
      this.passengers = new int[$$1.size()];

      for (int $$2 = 0; $$2 < $$1.size(); $$2++) {
         this.passengers[$$2] = $$1.get($$2).getId();
      }
   }

   private ClientboundSetPassengersPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.vehicle = $$0.readVarInt();
      this.passengers = $$0.readVarIntArray();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.vehicle);
      $$0.writeVarIntArray(this.passengers);
   }

   @Override
   public PacketType<ClientboundSetPassengersPacket> type() {
      return GamePacketTypes.CLIENTBOUND_SET_PASSENGERS;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleSetEntityPassengersPacket(this);
   }

   public int[] getPassengers() {
      return this.passengers;
   }

   public int getVehicle() {
      return this.vehicle;
   }
}
