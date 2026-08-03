package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.inventory.RecipeBookType;

public class ServerboundRecipeBookChangeSettingsPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundRecipeBookChangeSettingsPacket> STREAM_CODEC = Packet.codec(
      ServerboundRecipeBookChangeSettingsPacket::write, ServerboundRecipeBookChangeSettingsPacket::new
   );
   private final RecipeBookType bookType;
   private final boolean isOpen;
   private final boolean isFiltering;

   public ServerboundRecipeBookChangeSettingsPacket(RecipeBookType $$0, boolean $$1, boolean $$2) {
      this.bookType = $$0;
      this.isOpen = $$1;
      this.isFiltering = $$2;
   }

   private ServerboundRecipeBookChangeSettingsPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.bookType = $$0.readEnum(RecipeBookType.class);
      this.isOpen = $$0.readBoolean();
      this.isFiltering = $$0.readBoolean();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeEnum(this.bookType);
      $$0.writeBoolean(this.isOpen);
      $$0.writeBoolean(this.isFiltering);
   }

   @Override
   public PacketType<ServerboundRecipeBookChangeSettingsPacket> type() {
      return GamePacketTypes.SERVERBOUND_RECIPE_BOOK_CHANGE_SETTINGS;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleRecipeBookChangeSettingsPacket(this);
   }

   public RecipeBookType getBookType() {
      return this.bookType;
   }

   public boolean isOpen() {
      return this.isOpen;
   }

   public boolean isFiltering() {
      return this.isFiltering;
   }
}
