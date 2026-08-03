package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.stats.RecipeBookSettings;

public record ClientboundRecipeBookSettingsPacket(RecipeBookSettings bookSettings) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundRecipeBookSettingsPacket> STREAM_CODEC = StreamCodec.composite(
      RecipeBookSettings.STREAM_CODEC, ClientboundRecipeBookSettingsPacket::bookSettings, ClientboundRecipeBookSettingsPacket::new
   );

   @Override
   public PacketType<ClientboundRecipeBookSettingsPacket> type() {
      return GamePacketTypes.CLIENTBOUND_RECIPE_BOOK_SETTINGS;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleRecipeBookSettings(this);
   }
}
