package net.minecraft.network.protocol.game;

import java.util.BitSet;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;

public class ClientboundLightUpdatePacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundLightUpdatePacket> STREAM_CODEC = Packet.codec(
      ClientboundLightUpdatePacket::write, ClientboundLightUpdatePacket::new
   );
   private final int x;
   private final int z;
   private final ClientboundLightUpdatePacketData lightData;

   public ClientboundLightUpdatePacket(ChunkPos $$0, LevelLightEngine $$1, @Nullable BitSet $$2, @Nullable BitSet $$3) {
      this.x = $$0.x;
      this.z = $$0.z;
      this.lightData = new ClientboundLightUpdatePacketData($$0, $$1, $$2, $$3);
   }

   private ClientboundLightUpdatePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.x = $$0.readVarInt();
      this.z = $$0.readVarInt();
      this.lightData = new ClientboundLightUpdatePacketData($$0, this.x, this.z);
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.x);
      $$0.writeVarInt(this.z);
      this.lightData.write($$0);
   }

   @Override
   public PacketType<ClientboundLightUpdatePacket> type() {
      return GamePacketTypes.CLIENTBOUND_LIGHT_UPDATE;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleLightUpdatePacket(this);
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public ClientboundLightUpdatePacketData getLightData() {
      return this.lightData;
   }
}
