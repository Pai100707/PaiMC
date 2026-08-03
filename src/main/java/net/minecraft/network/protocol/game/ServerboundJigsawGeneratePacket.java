package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundJigsawGeneratePacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundJigsawGeneratePacket> STREAM_CODEC = Packet.codec(
      ServerboundJigsawGeneratePacket::write, ServerboundJigsawGeneratePacket::new
   );
   private final BlockPos pos;
   private final int levels;
   private final boolean keepJigsaws;

   public ServerboundJigsawGeneratePacket(BlockPos $$0, int $$1, boolean $$2) {
      this.pos = $$0;
      this.levels = $$1;
      this.keepJigsaws = $$2;
   }

   private ServerboundJigsawGeneratePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.pos = $$0.readBlockPos();
      this.levels = $$0.readVarInt();
      this.keepJigsaws = $$0.readBoolean();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeBlockPos(this.pos);
      $$0.writeVarInt(this.levels);
      $$0.writeBoolean(this.keepJigsaws);
   }

   @Override
   public PacketType<ServerboundJigsawGeneratePacket> type() {
      return GamePacketTypes.SERVERBOUND_JIGSAW_GENERATE;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleJigsawGenerate(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public int levels() {
      return this.levels;
   }

   public boolean keepJigsaws() {
      return this.keepJigsaws;
   }
}
