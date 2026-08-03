package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundLockDifficultyPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundLockDifficultyPacket> STREAM_CODEC = Packet.codec(
      ServerboundLockDifficultyPacket::write, ServerboundLockDifficultyPacket::new
   );
   private final boolean locked;

   public ServerboundLockDifficultyPacket(boolean $$0) {
      this.locked = $$0;
   }

   private ServerboundLockDifficultyPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.locked = $$0.readBoolean();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeBoolean(this.locked);
   }

   @Override
   public PacketType<ServerboundLockDifficultyPacket> type() {
      return GamePacketTypes.SERVERBOUND_LOCK_DIFFICULTY;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleLockDifficulty(this);
   }

   public boolean isLocked() {
      return this.locked;
   }
}
