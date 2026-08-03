package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.damagesource.CombatTracker;

public class ClientboundPlayerCombatEndPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundPlayerCombatEndPacket> STREAM_CODEC = Packet.codec(
      ClientboundPlayerCombatEndPacket::write, ClientboundPlayerCombatEndPacket::new
   );
   private final int duration;

   public ClientboundPlayerCombatEndPacket(CombatTracker $$0) {
      this($$0.getCombatDuration());
   }

   public ClientboundPlayerCombatEndPacket(int $$0) {
      this.duration = $$0;
   }

   private ClientboundPlayerCombatEndPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.duration = $$0.readVarInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.duration);
   }

   @Override
   public PacketType<ClientboundPlayerCombatEndPacket> type() {
      return GamePacketTypes.CLIENTBOUND_PLAYER_COMBAT_END;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handlePlayerCombatEnd(this);
   }
}
