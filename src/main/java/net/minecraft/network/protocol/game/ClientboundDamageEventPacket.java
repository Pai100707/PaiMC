package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ClientboundDamageEventPacket(int entityId, Holder<DamageType> sourceType, int sourceCauseId, int sourceDirectId, Optional<Vec3> sourcePosition)
   implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundDamageEventPacket> STREAM_CODEC = Packet.codec(
      ClientboundDamageEventPacket::write, ClientboundDamageEventPacket::new
   );

   public ClientboundDamageEventPacket(Entity $$0, DamageSource $$1) {
      this(
         $$0.getId(),
         $$1.typeHolder(),
         $$1.getEntity() != null ? $$1.getEntity().getId() : -1,
         $$1.getDirectEntity() != null ? $$1.getDirectEntity().getId() : -1,
         Optional.ofNullable($$1.sourcePositionRaw())
      );
   }

   private ClientboundDamageEventPacket(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      this(
         $$0.readVarInt(),
         (Holder<DamageType>)DamageType.STREAM_CODEC.decode($$0),
         readOptionalEntityId($$0),
         readOptionalEntityId($$0),
         $$0.readOptional($$0x -> new Vec3($$0x.readDouble(), $$0x.readDouble(), $$0x.readDouble()))
      );
   }

   private static void writeOptionalEntityId(net.minecraft.network.FriendlyByteBuf $$0, int $$1) {
      $$0.writeVarInt($$1 + 1);
   }

   private static int readOptionalEntityId(net.minecraft.network.FriendlyByteBuf $$0) {
      return $$0.readVarInt() - 1;
   }

   private void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      $$0.writeVarInt(this.entityId);
      DamageType.STREAM_CODEC.encode($$0, this.sourceType);
      writeOptionalEntityId($$0, this.sourceCauseId);
      writeOptionalEntityId($$0, this.sourceDirectId);
      $$0.writeOptional(this.sourcePosition, ($$0x, $$1) -> {
         $$0x.writeDouble($$1.x());
         $$0x.writeDouble($$1.y());
         $$0x.writeDouble($$1.z());
      });
   }

   @Override
   public PacketType<ClientboundDamageEventPacket> type() {
      return GamePacketTypes.CLIENTBOUND_DAMAGE_EVENT;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleDamageEvent(this);
   }

   public DamageSource getSource(Level $$0) {
      if (this.sourcePosition.isPresent()) {
         return new DamageSource(this.sourceType, this.sourcePosition.get());
      } else {
         Entity $$1 = $$0.getEntity(this.sourceCauseId);
         Entity $$2 = $$0.getEntity(this.sourceDirectId);
         return new DamageSource(this.sourceType, $$2, $$1);
      }
   }
}
