package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundCommandSuggestionPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundCommandSuggestionPacket> STREAM_CODEC = Packet.codec(
      ServerboundCommandSuggestionPacket::write, ServerboundCommandSuggestionPacket::new
   );
   private final int id;
   private final String command;

   public ServerboundCommandSuggestionPacket(int $$0, String $$1) {
      this.id = $$0;
      this.command = $$1;
   }

   private ServerboundCommandSuggestionPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.id = $$0.readVarInt();
      this.command = $$0.readUtf(32500);
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.id);
      $$0.writeUtf(this.command, 32500);
   }

   @Override
   public PacketType<ServerboundCommandSuggestionPacket> type() {
      return GamePacketTypes.SERVERBOUND_COMMAND_SUGGESTION;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleCustomCommandSuggestions(this);
   }

   public int getId() {
      return this.id;
   }

   public String getCommand() {
      return this.command;
   }
}
