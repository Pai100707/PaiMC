package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ClientboundBlockUpdatePacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundBlockUpdatePacket> STREAM_CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC,
      ClientboundBlockUpdatePacket::getPos,
      ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY),
      ClientboundBlockUpdatePacket::getBlockState,
      ClientboundBlockUpdatePacket::new
   );
   private final BlockPos pos;
   private final BlockState blockState;

   public ClientboundBlockUpdatePacket(BlockPos $$0, BlockState $$1) {
      this.pos = $$0;
      this.blockState = $$1;
   }

   public ClientboundBlockUpdatePacket(BlockGetter $$0, BlockPos $$1) {
      this($$1, $$0.getBlockState($$1));
   }

   @Override
   public PacketType<ClientboundBlockUpdatePacket> type() {
      return GamePacketTypes.CLIENTBOUND_BLOCK_UPDATE;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleBlockUpdate(this);
   }

   public BlockState getBlockState() {
      return this.blockState;
   }

   public BlockPos getPos() {
      return this.pos;
   }
}
