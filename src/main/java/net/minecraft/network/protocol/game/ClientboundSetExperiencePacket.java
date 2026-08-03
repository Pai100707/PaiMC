package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetExperiencePacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundSetExperiencePacket> STREAM_CODEC = Packet.codec(
      ClientboundSetExperiencePacket::write, ClientboundSetExperiencePacket::new
   );
   private final float experienceProgress;
   private final int totalExperience;
   private final int experienceLevel;

   public ClientboundSetExperiencePacket(float $$0, int $$1, int $$2) {
      this.experienceProgress = $$0;
      this.totalExperience = $$1;
      this.experienceLevel = $$2;
   }

   private ClientboundSetExperiencePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.experienceProgress = $$0.readFloat();
      this.experienceLevel = $$0.readVarInt();
      this.totalExperience = $$0.readVarInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeFloat(this.experienceProgress);
      $$0.writeVarInt(this.experienceLevel);
      $$0.writeVarInt(this.totalExperience);
   }

   @Override
   public PacketType<ClientboundSetExperiencePacket> type() {
      return GamePacketTypes.CLIENTBOUND_SET_EXPERIENCE;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleSetExperience(this);
   }

   public float getExperienceProgress() {
      return this.experienceProgress;
   }

   public int getTotalExperience() {
      return this.totalExperience;
   }

   public int getExperienceLevel() {
      return this.experienceLevel;
   }
}
