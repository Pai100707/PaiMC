package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundSetSimulationDistancePacket(int simulationDistance) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundSetSimulationDistancePacket> STREAM_CODEC = Packet.codec(
      ClientboundSetSimulationDistancePacket::write, ClientboundSetSimulationDistancePacket::new
   );

   private ClientboundSetSimulationDistancePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readVarInt());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.simulationDistance);
   }

   @Override
   public PacketType<ClientboundSetSimulationDistancePacket> type() {
      return GamePacketTypes.CLIENTBOUND_SET_SIMULATION_DISTANCE;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleSetSimulationDistance(this);
   }
}
