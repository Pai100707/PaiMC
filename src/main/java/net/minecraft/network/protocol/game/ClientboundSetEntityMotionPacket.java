package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class ClientboundSetEntityMotionPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundSetEntityMotionPacket> STREAM_CODEC = Packet.codec(
      ClientboundSetEntityMotionPacket::write, ClientboundSetEntityMotionPacket::new
   );
   private final int id;
   private final Vec3 movement;

   public ClientboundSetEntityMotionPacket(Entity $$0) {
      this($$0.getId(), $$0.getDeltaMovement());
   }

   public ClientboundSetEntityMotionPacket(int $$0, Vec3 $$1) {
      this.id = $$0;
      this.movement = $$1;
   }

   private ClientboundSetEntityMotionPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.id = $$0.readVarInt();
      this.movement = $$0.readLpVec3();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.id);
      $$0.writeLpVec3(this.movement);
   }

   @Override
   public PacketType<ClientboundSetEntityMotionPacket> type() {
      return GamePacketTypes.CLIENTBOUND_SET_ENTITY_MOTION;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleSetEntityMotion(this);
   }

   public int getId() {
      return this.id;
   }

   public Vec3 getMovement() {
      return this.movement;
   }
}
