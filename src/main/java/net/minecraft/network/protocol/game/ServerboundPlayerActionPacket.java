package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundPlayerActionPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundPlayerActionPacket> STREAM_CODEC = Packet.codec(
      ServerboundPlayerActionPacket::write, ServerboundPlayerActionPacket::new
   );
   private final BlockPos pos;
   private final Direction direction;
   private final ServerboundPlayerActionPacket.Action action;
   private final int sequence;

   public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action $$0, BlockPos $$1, Direction $$2, int $$3) {
      this.action = $$0;
      this.pos = $$1.immutable();
      this.direction = $$2;
      this.sequence = $$3;
   }

   public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action $$0, BlockPos $$1, Direction $$2) {
      this($$0, $$1, $$2, 0);
   }

   private ServerboundPlayerActionPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.action = $$0.readEnum(ServerboundPlayerActionPacket.Action.class);
      this.pos = $$0.readBlockPos();
      this.direction = Direction.from3DDataValue($$0.readUnsignedByte());
      this.sequence = $$0.readVarInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeEnum(this.action);
      $$0.writeBlockPos(this.pos);
      $$0.writeByte(this.direction.get3DDataValue());
      $$0.writeVarInt(this.sequence);
   }

   @Override
   public PacketType<ServerboundPlayerActionPacket> type() {
      return GamePacketTypes.SERVERBOUND_PLAYER_ACTION;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handlePlayerAction(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public ServerboundPlayerActionPacket.Action getAction() {
      return this.action;
   }

   public int getSequence() {
      return this.sequence;
   }

   public static enum Action {
      START_DESTROY_BLOCK,
      ABORT_DESTROY_BLOCK,
      STOP_DESTROY_BLOCK,
      DROP_ALL_ITEMS,
      DROP_ITEM,
      RELEASE_USE_ITEM,
      SWAP_ITEM_WITH_OFFHAND,
      STAB;
   }
}
