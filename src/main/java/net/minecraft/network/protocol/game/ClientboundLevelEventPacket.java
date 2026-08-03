package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundLevelEventPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundLevelEventPacket> STREAM_CODEC = Packet.codec(
      ClientboundLevelEventPacket::write, ClientboundLevelEventPacket::new
   );
   private final int type;
   private final BlockPos pos;
   private final int data;
   private final boolean globalEvent;

   public ClientboundLevelEventPacket(int $$0, BlockPos $$1, int $$2, boolean $$3) {
      this.type = $$0;
      this.pos = $$1.immutable();
      this.data = $$2;
      this.globalEvent = $$3;
   }

   private ClientboundLevelEventPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.type = $$0.readInt();
      this.pos = $$0.readBlockPos();
      this.data = $$0.readInt();
      this.globalEvent = $$0.readBoolean();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeInt(this.type);
      $$0.writeBlockPos(this.pos);
      $$0.writeInt(this.data);
      $$0.writeBoolean(this.globalEvent);
   }

   @Override
   public PacketType<ClientboundLevelEventPacket> type() {
      return GamePacketTypes.CLIENTBOUND_LEVEL_EVENT;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleLevelEvent(this);
   }

   public boolean isGlobalEvent() {
      return this.globalEvent;
   }

   public int getType() {
      return this.type;
   }

   public int getData() {
      return this.data;
   }

   public BlockPos getPos() {
      return this.pos;
   }
}
