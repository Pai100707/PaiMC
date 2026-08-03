package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.player.Input;

public record ServerboundPlayerInputPacket(Input input) implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundPlayerInputPacket> STREAM_CODEC = StreamCodec.composite(
      Input.STREAM_CODEC, ServerboundPlayerInputPacket::input, ServerboundPlayerInputPacket::new
   );

   @Override
   public PacketType<ServerboundPlayerInputPacket> type() {
      return GamePacketTypes.SERVERBOUND_PLAYER_INPUT;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handlePlayerInput(this);
   }
}
