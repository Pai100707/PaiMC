package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderLerpSizePacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundSetBorderLerpSizePacket> STREAM_CODEC = Packet.codec(
      ClientboundSetBorderLerpSizePacket::write, ClientboundSetBorderLerpSizePacket::new
   );
   private final double oldSize;
   private final double newSize;
   private final long lerpTime;

   public ClientboundSetBorderLerpSizePacket(WorldBorder $$0) {
      this.oldSize = $$0.getSize();
      this.newSize = $$0.getLerpTarget();
      this.lerpTime = $$0.getLerpTime();
   }

   private ClientboundSetBorderLerpSizePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.oldSize = $$0.readDouble();
      this.newSize = $$0.readDouble();
      this.lerpTime = $$0.readVarLong();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeDouble(this.oldSize);
      $$0.writeDouble(this.newSize);
      $$0.writeVarLong(this.lerpTime);
   }

   @Override
   public PacketType<ClientboundSetBorderLerpSizePacket> type() {
      return GamePacketTypes.CLIENTBOUND_SET_BORDER_LERP_SIZE;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleSetBorderLerpSize(this);
   }

   public double getOldSize() {
      return this.oldSize;
   }

   public double getNewSize() {
      return this.newSize;
   }

   public long getLerpTime() {
      return this.lerpTime;
   }
}
