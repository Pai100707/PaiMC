package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ClientboundLoginPacket(
   int playerId,
   boolean hardcore,
   Set<ResourceKey<Level>> levels,
   int maxPlayers,
   int chunkRadius,
   int simulationDistance,
   boolean reducedDebugInfo,
   boolean showDeathScreen,
   boolean doLimitedCrafting,
   CommonPlayerSpawnInfo commonPlayerSpawnInfo,
   boolean enforcesSecureChat
) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundLoginPacket> STREAM_CODEC = Packet.codec(
      ClientboundLoginPacket::write, ClientboundLoginPacket::new
   );

   private ClientboundLoginPacket(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      this(
         $$0.readInt(),
         $$0.readBoolean(),
         $$0.readCollection(Sets::newHashSetWithExpectedSize, $$0x -> $$0x.readResourceKey(Registries.DIMENSION)),
         $$0.readVarInt(),
         $$0.readVarInt(),
         $$0.readVarInt(),
         $$0.readBoolean(),
         $$0.readBoolean(),
         $$0.readBoolean(),
         new CommonPlayerSpawnInfo($$0),
         $$0.readBoolean()
      );
   }

   private void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      $$0.writeInt(this.playerId);
      $$0.writeBoolean(this.hardcore);
      $$0.writeCollection(this.levels, net.minecraft.network.FriendlyByteBuf::writeResourceKey);
      $$0.writeVarInt(this.maxPlayers);
      $$0.writeVarInt(this.chunkRadius);
      $$0.writeVarInt(this.simulationDistance);
      $$0.writeBoolean(this.reducedDebugInfo);
      $$0.writeBoolean(this.showDeathScreen);
      $$0.writeBoolean(this.doLimitedCrafting);
      this.commonPlayerSpawnInfo.write($$0);
      $$0.writeBoolean(this.enforcesSecureChat);
   }

   @Override
   public PacketType<ClientboundLoginPacket> type() {
      return GamePacketTypes.CLIENTBOUND_LOGIN;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleLogin(this);
   }
}
