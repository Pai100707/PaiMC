package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundSelectBundleItemPacket(int slotId, int selectedItemIndex) implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundSelectBundleItemPacket> STREAM_CODEC = Packet.codec(
      ServerboundSelectBundleItemPacket::write, ServerboundSelectBundleItemPacket::new
   );

   private ServerboundSelectBundleItemPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readVarInt(), $$0.readVarInt());
      if (this.selectedItemIndex < 0 && this.selectedItemIndex != -1) {
         throw new IllegalArgumentException("Invalid selectedItemIndex: " + this.selectedItemIndex);
      }
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.slotId);
      $$0.writeVarInt(this.selectedItemIndex);
   }

   @Override
   public PacketType<ServerboundSelectBundleItemPacket> type() {
      return GamePacketTypes.SERVERBOUND_BUNDLE_ITEM_SELECTED;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleBundleItemSelectedPacket(this);
   }
}
