package net.minecraft.network.protocol.common;

import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundResourcePackPacket(UUID id, ServerboundResourcePackPacket.Action action) implements Packet<ServerCommonPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundResourcePackPacket> STREAM_CODEC = Packet.codec(
      ServerboundResourcePackPacket::write, ServerboundResourcePackPacket::new
   );

   private ServerboundResourcePackPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readUUID(), $$0.readEnum(ServerboundResourcePackPacket.Action.class));
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeUUID(this.id);
      $$0.writeEnum(this.action);
   }

   @Override
   public PacketType<ServerboundResourcePackPacket> type() {
      return CommonPacketTypes.SERVERBOUND_RESOURCE_PACK;
   }

   public void handle(ServerCommonPacketListener $$0) {
      $$0.handleResourcePackResponse(this);
   }

   public static enum Action {
      SUCCESSFULLY_LOADED,
      DECLINED,
      FAILED_DOWNLOAD,
      ACCEPTED,
      DOWNLOADED,
      INVALID_URL,
      FAILED_RELOAD,
      DISCARDED;

      public boolean isTerminal() {
         return this != ACCEPTED && this != DOWNLOADED;
      }
   }
}
