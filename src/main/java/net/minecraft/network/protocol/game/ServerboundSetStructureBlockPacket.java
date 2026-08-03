package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity.UpdateType;
import net.minecraft.world.level.block.state.properties.StructureMode;

public class ServerboundSetStructureBlockPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundSetStructureBlockPacket> STREAM_CODEC = Packet.codec(
      ServerboundSetStructureBlockPacket::write, ServerboundSetStructureBlockPacket::new
   );
   private static final int FLAG_IGNORE_ENTITIES = 1;
   private static final int FLAG_SHOW_AIR = 2;
   private static final int FLAG_SHOW_BOUNDING_BOX = 4;
   private static final int FLAG_STRICT = 8;
   private final BlockPos pos;
   private final UpdateType updateType;
   private final StructureMode mode;
   private final String name;
   private final BlockPos offset;
   private final Vec3i size;
   private final Mirror mirror;
   private final Rotation rotation;
   private final String data;
   private final boolean ignoreEntities;
   private final boolean strict;
   private final boolean showAir;
   private final boolean showBoundingBox;
   private final float integrity;
   private final long seed;

   public ServerboundSetStructureBlockPacket(
      BlockPos $$0,
      UpdateType $$1,
      StructureMode $$2,
      String $$3,
      BlockPos $$4,
      Vec3i $$5,
      Mirror $$6,
      Rotation $$7,
      String $$8,
      boolean $$9,
      boolean $$10,
      boolean $$11,
      boolean $$12,
      float $$13,
      long $$14
   ) {
      this.pos = $$0;
      this.updateType = $$1;
      this.mode = $$2;
      this.name = $$3;
      this.offset = $$4;
      this.size = $$5;
      this.mirror = $$6;
      this.rotation = $$7;
      this.data = $$8;
      this.ignoreEntities = $$9;
      this.strict = $$10;
      this.showAir = $$11;
      this.showBoundingBox = $$12;
      this.integrity = $$13;
      this.seed = $$14;
   }

   private ServerboundSetStructureBlockPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.pos = $$0.readBlockPos();
      this.updateType = $$0.readEnum(UpdateType.class);
      this.mode = $$0.readEnum(StructureMode.class);
      this.name = $$0.readUtf();
      int $$1 = 48;
      this.offset = new BlockPos(Mth.clamp($$0.readByte(), -48, 48), Mth.clamp($$0.readByte(), -48, 48), Mth.clamp($$0.readByte(), -48, 48));
      int $$2 = 48;
      this.size = new Vec3i(Mth.clamp($$0.readByte(), 0, 48), Mth.clamp($$0.readByte(), 0, 48), Mth.clamp($$0.readByte(), 0, 48));
      this.mirror = $$0.readEnum(Mirror.class);
      this.rotation = $$0.readEnum(Rotation.class);
      this.data = $$0.readUtf(128);
      this.integrity = Mth.clamp($$0.readFloat(), 0.0F, 1.0F);
      this.seed = $$0.readVarLong();
      int $$3 = $$0.readByte();
      this.ignoreEntities = ($$3 & 1) != 0;
      this.strict = ($$3 & 8) != 0;
      this.showAir = ($$3 & 2) != 0;
      this.showBoundingBox = ($$3 & 4) != 0;
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeBlockPos(this.pos);
      $$0.writeEnum(this.updateType);
      $$0.writeEnum(this.mode);
      $$0.writeUtf(this.name);
      $$0.writeByte(this.offset.getX());
      $$0.writeByte(this.offset.getY());
      $$0.writeByte(this.offset.getZ());
      $$0.writeByte(this.size.getX());
      $$0.writeByte(this.size.getY());
      $$0.writeByte(this.size.getZ());
      $$0.writeEnum(this.mirror);
      $$0.writeEnum(this.rotation);
      $$0.writeUtf(this.data);
      $$0.writeFloat(this.integrity);
      $$0.writeVarLong(this.seed);
      int $$1 = 0;
      if (this.ignoreEntities) {
         $$1 |= 1;
      }

      if (this.showAir) {
         $$1 |= 2;
      }

      if (this.showBoundingBox) {
         $$1 |= 4;
      }

      if (this.strict) {
         $$1 |= 8;
      }

      $$0.writeByte($$1);
   }

   @Override
   public PacketType<ServerboundSetStructureBlockPacket> type() {
      return GamePacketTypes.SERVERBOUND_SET_STRUCTURE_BLOCK;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleSetStructureBlock(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public UpdateType getUpdateType() {
      return this.updateType;
   }

   public StructureMode getMode() {
      return this.mode;
   }

   public String getName() {
      return this.name;
   }

   public BlockPos getOffset() {
      return this.offset;
   }

   public Vec3i getSize() {
      return this.size;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public String getData() {
      return this.data;
   }

   public boolean isIgnoreEntities() {
      return this.ignoreEntities;
   }

   public boolean isStrict() {
      return this.strict;
   }

   public boolean isShowAir() {
      return this.showAir;
   }

   public boolean isShowBoundingBox() {
      return this.showBoundingBox;
   }

   public float getIntegrity() {
      return this.integrity;
   }

   public long getSeed() {
      return this.seed;
   }
}
