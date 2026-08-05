package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record ClientboundRemoveMobEffectPacket(int entityId, Holder<MobEffect> effect) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundRemoveMobEffectPacket> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      ClientboundRemoveMobEffectPacket::entityId,
      MobEffect.STREAM_CODEC,
      ClientboundRemoveMobEffectPacket::effect,
      ClientboundRemoveMobEffectPacket::new
   );

   @Override
   public PacketType<ClientboundRemoveMobEffectPacket> type() {
      return GamePacketTypes.CLIENTBOUND_REMOVE_MOB_EFFECT;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleRemoveMobEffect(this);
   }

   
   public Entity getEntity(Level $$0) {
      return $$0.getEntity(this.entityId);
   }
}
