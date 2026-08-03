package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.JigsawBlockEntity.JointType;

public class ServerboundSetJigsawBlockPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundSetJigsawBlockPacket> STREAM_CODEC = Packet.codec(
      ServerboundSetJigsawBlockPacket::write, ServerboundSetJigsawBlockPacket::new
   );
   private final BlockPos pos;
   private final Identifier name;
   private final Identifier target;
   private final Identifier pool;
   private final String finalState;
   private final JointType joint;
   private final int selectionPriority;
   private final int placementPriority;

   public ServerboundSetJigsawBlockPacket(BlockPos $$0, Identifier $$1, Identifier $$2, Identifier $$3, String $$4, JointType $$5, int $$6, int $$7) {
      this.pos = $$0;
      this.name = $$1;
      this.target = $$2;
      this.pool = $$3;
      this.finalState = $$4;
      this.joint = $$5;
      this.selectionPriority = $$6;
      this.placementPriority = $$7;
   }

   private ServerboundSetJigsawBlockPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.pos = $$0.readBlockPos();
      this.name = $$0.readIdentifier();
      this.target = $$0.readIdentifier();
      this.pool = $$0.readIdentifier();
      this.finalState = $$0.readUtf();
      this.joint = (JointType)JointType.CODEC.byName($$0.readUtf(), JointType.ALIGNED);
      this.selectionPriority = $$0.readVarInt();
      this.placementPriority = $$0.readVarInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeBlockPos(this.pos);
      $$0.writeIdentifier(this.name);
      $$0.writeIdentifier(this.target);
      $$0.writeIdentifier(this.pool);
      $$0.writeUtf(this.finalState);
      $$0.writeUtf(this.joint.getSerializedName());
      $$0.writeVarInt(this.selectionPriority);
      $$0.writeVarInt(this.placementPriority);
   }

   @Override
   public PacketType<ServerboundSetJigsawBlockPacket> type() {
      return GamePacketTypes.SERVERBOUND_SET_JIGSAW_BLOCK;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleSetJigsawBlock(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Identifier getName() {
      return this.name;
   }

   public Identifier getTarget() {
      return this.target;
   }

   public Identifier getPool() {
      return this.pool;
   }

   public String getFinalState() {
      return this.finalState;
   }

   public JointType getJoint() {
      return this.joint;
   }

   public int getSelectionPriority() {
      return this.selectionPriority;
   }

   public int getPlacementPriority() {
      return this.placementPriority;
   }
}
