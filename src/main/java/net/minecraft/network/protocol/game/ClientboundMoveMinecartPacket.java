package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior.MinecartStep;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public record ClientboundMoveMinecartPacket(int entityId, List<MinecartStep> lerpSteps) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundMoveMinecartPacket> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      ClientboundMoveMinecartPacket::entityId,
      MinecartStep.STREAM_CODEC.apply(ByteBufCodecs.list()),
      ClientboundMoveMinecartPacket::lerpSteps,
      ClientboundMoveMinecartPacket::new
   );

   @Override
   public PacketType<ClientboundMoveMinecartPacket> type() {
      return GamePacketTypes.CLIENTBOUND_MOVE_MINECART_ALONG_TRACK;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleMinecartAlongTrack(this);
   }

   @Nullable
   public Entity getEntity(Level $$0) {
      return $$0.getEntity(this.entityId);
   }
}
