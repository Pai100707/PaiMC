package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.storage.LevelData.RespawnData;

public record ClientboundSetDefaultSpawnPositionPacket(RespawnData respawnData) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundSetDefaultSpawnPositionPacket> STREAM_CODEC = StreamCodec.composite(
      RespawnData.STREAM_CODEC, ClientboundSetDefaultSpawnPositionPacket::respawnData, ClientboundSetDefaultSpawnPositionPacket::new
   );

   @Override
   public PacketType<ClientboundSetDefaultSpawnPositionPacket> type() {
      return GamePacketTypes.CLIENTBOUND_SET_DEFAULT_SPAWN_POSITION;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleSetSpawn(this);
   }
}
