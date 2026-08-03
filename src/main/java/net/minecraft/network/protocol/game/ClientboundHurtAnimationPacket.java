package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.LivingEntity;

public record ClientboundHurtAnimationPacket(int id, float yaw) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundHurtAnimationPacket> STREAM_CODEC = Packet.codec(
      ClientboundHurtAnimationPacket::write, ClientboundHurtAnimationPacket::new
   );

   public ClientboundHurtAnimationPacket(LivingEntity $$0) {
      this($$0.getId(), $$0.getHurtDir());
   }

   private ClientboundHurtAnimationPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readVarInt(), $$0.readFloat());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.id);
      $$0.writeFloat(this.yaw);
   }

   @Override
   public PacketType<ClientboundHurtAnimationPacket> type() {
      return GamePacketTypes.CLIENTBOUND_HURT_ANIMATION;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleHurtAnimation(this);
   }
}
