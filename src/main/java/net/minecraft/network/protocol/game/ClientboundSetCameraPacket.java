package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ClientboundSetCameraPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundSetCameraPacket> STREAM_CODEC = Packet.codec(
      ClientboundSetCameraPacket::write, ClientboundSetCameraPacket::new
   );
   private final int cameraId;

   public ClientboundSetCameraPacket(Entity $$0) {
      this.cameraId = $$0.getId();
   }

   private ClientboundSetCameraPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.cameraId = $$0.readVarInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.cameraId);
   }

   @Override
   public PacketType<ClientboundSetCameraPacket> type() {
      return GamePacketTypes.CLIENTBOUND_SET_CAMERA;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleSetCamera(this);
   }

   @Nullable
   public Entity getEntity(Level $$0) {
      return $$0.getEntity(this.cameraId);
   }
}
