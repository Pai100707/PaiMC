package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundBlockDestructionPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundBlockDestructionPacket> STREAM_CODEC = Packet.codec(
      ClientboundBlockDestructionPacket::write, ClientboundBlockDestructionPacket::new
   );
   private final int id;
   private final BlockPos pos;
   private final int progress;

   public ClientboundBlockDestructionPacket(int $$0, BlockPos $$1, int $$2) {
      this.id = $$0;
      this.pos = $$1;
      this.progress = $$2;
   }

   private ClientboundBlockDestructionPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.id = $$0.readVarInt();
      this.pos = $$0.readBlockPos();
      this.progress = $$0.readUnsignedByte();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.id);
      $$0.writeBlockPos(this.pos);
      $$0.writeByte(this.progress);
   }

   @Override
   public PacketType<ClientboundBlockDestructionPacket> type() {
      return GamePacketTypes.CLIENTBOUND_BLOCK_DESTRUCTION;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleBlockDestruction(this);
   }

   public int getId() {
      return this.id;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int getProgress() {
      return this.progress;
   }
}
