package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class ServerboundUseItemOnPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundUseItemOnPacket> STREAM_CODEC = Packet.codec(
      ServerboundUseItemOnPacket::write, ServerboundUseItemOnPacket::new
   );
   private final BlockHitResult blockHit;
   private final InteractionHand hand;
   private final int sequence;

   public ServerboundUseItemOnPacket(InteractionHand $$0, BlockHitResult $$1, int $$2) {
      this.hand = $$0;
      this.blockHit = $$1;
      this.sequence = $$2;
   }

   private ServerboundUseItemOnPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.hand = $$0.readEnum(InteractionHand.class);
      this.blockHit = $$0.readBlockHitResult();
      this.sequence = $$0.readVarInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeEnum(this.hand);
      $$0.writeBlockHitResult(this.blockHit);
      $$0.writeVarInt(this.sequence);
   }

   @Override
   public PacketType<ServerboundUseItemOnPacket> type() {
      return GamePacketTypes.SERVERBOUND_USE_ITEM_ON;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleUseItemOn(this);
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   public BlockHitResult getHitResult() {
      return this.blockHit;
   }

   public int getSequence() {
      return this.sequence;
   }
}
