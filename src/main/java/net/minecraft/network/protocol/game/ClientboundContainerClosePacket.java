package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundContainerClosePacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundContainerClosePacket> STREAM_CODEC = Packet.codec(
      ClientboundContainerClosePacket::write, ClientboundContainerClosePacket::new
   );
   private final int containerId;

   public ClientboundContainerClosePacket(int $$0) {
      this.containerId = $$0;
   }

   private ClientboundContainerClosePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.containerId = $$0.readContainerId();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeContainerId(this.containerId);
   }

   @Override
   public PacketType<ClientboundContainerClosePacket> type() {
      return GamePacketTypes.CLIENTBOUND_CONTAINER_CLOSE;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleContainerClose(this);
   }

   public int getContainerId() {
      return this.containerId;
   }
}
