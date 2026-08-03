package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundRespawnPacket(CommonPlayerSpawnInfo commonPlayerSpawnInfo, byte dataToKeep) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundRespawnPacket> STREAM_CODEC = Packet.codec(
      ClientboundRespawnPacket::write, ClientboundRespawnPacket::new
   );
   public static final byte KEEP_ATTRIBUTE_MODIFIERS = 1;
   public static final byte KEEP_ENTITY_DATA = 2;
   public static final byte KEEP_ALL_DATA = 3;

   private ClientboundRespawnPacket(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      this(new CommonPlayerSpawnInfo($$0), $$0.readByte());
   }

   private void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      this.commonPlayerSpawnInfo.write($$0);
      $$0.writeByte(this.dataToKeep);
   }

   @Override
   public PacketType<ClientboundRespawnPacket> type() {
      return GamePacketTypes.CLIENTBOUND_RESPAWN;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleRespawn(this);
   }

   public boolean shouldKeep(byte $$0) {
      return (this.dataToKeep & $$0) != 0;
   }
}
