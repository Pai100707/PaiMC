package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public abstract class ClientboundMoveEntityPacket implements Packet<ClientGamePacketListener> {
   protected final int entityId;
   protected final short xa;
   protected final short ya;
   protected final short za;
   protected final byte yRot;
   protected final byte xRot;
   protected final boolean onGround;
   protected final boolean hasRot;
   protected final boolean hasPos;

   protected ClientboundMoveEntityPacket(int $$0, short $$1, short $$2, short $$3, byte $$4, byte $$5, boolean $$6, boolean $$7, boolean $$8) {
      this.entityId = $$0;
      this.xa = $$1;
      this.ya = $$2;
      this.za = $$3;
      this.yRot = $$4;
      this.xRot = $$5;
      this.onGround = $$6;
      this.hasRot = $$7;
      this.hasPos = $$8;
   }

   @Override
   public abstract PacketType<? extends ClientboundMoveEntityPacket> type();

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleMoveEntity(this);
   }

   @Override
   public String toString() {
      return "Entity_" + super.toString();
   }

   @Nullable
   public Entity getEntity(Level $$0) {
      return $$0.getEntity(this.entityId);
   }

   public short getXa() {
      return this.xa;
   }

   public short getYa() {
      return this.ya;
   }

   public short getZa() {
      return this.za;
   }

   public float getYRot() {
      return Mth.unpackDegrees(this.yRot);
   }

   public float getXRot() {
      return Mth.unpackDegrees(this.xRot);
   }

   public boolean hasRotation() {
      return this.hasRot;
   }

   public boolean hasPosition() {
      return this.hasPos;
   }

   public boolean isOnGround() {
      return this.onGround;
   }

   public static class Pos extends ClientboundMoveEntityPacket {
      public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundMoveEntityPacket.Pos> STREAM_CODEC = Packet.codec(
         ClientboundMoveEntityPacket.Pos::write, ClientboundMoveEntityPacket.Pos::read
      );

      public Pos(int $$0, short $$1, short $$2, short $$3, boolean $$4) {
         super($$0, $$1, $$2, $$3, (byte)0, (byte)0, $$4, false, true);
      }

      private static ClientboundMoveEntityPacket.Pos read(net.minecraft.network.FriendlyByteBuf $$0) {
         int $$1 = $$0.readVarInt();
         short $$2 = $$0.readShort();
         short $$3 = $$0.readShort();
         short $$4 = $$0.readShort();
         boolean $$5 = $$0.readBoolean();
         return new ClientboundMoveEntityPacket.Pos($$1, $$2, $$3, $$4, $$5);
      }

      private void write(net.minecraft.network.FriendlyByteBuf $$0) {
         $$0.writeVarInt(this.entityId);
         $$0.writeShort(this.xa);
         $$0.writeShort(this.ya);
         $$0.writeShort(this.za);
         $$0.writeBoolean(this.onGround);
      }

      @Override
      public PacketType<ClientboundMoveEntityPacket.Pos> type() {
         return GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_POS;
      }
   }

   public static class PosRot extends ClientboundMoveEntityPacket {
      public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundMoveEntityPacket.PosRot> STREAM_CODEC = Packet.codec(
         ClientboundMoveEntityPacket.PosRot::write, ClientboundMoveEntityPacket.PosRot::read
      );

      public PosRot(int $$0, short $$1, short $$2, short $$3, byte $$4, byte $$5, boolean $$6) {
         super($$0, $$1, $$2, $$3, $$4, $$5, $$6, true, true);
      }

      private static ClientboundMoveEntityPacket.PosRot read(net.minecraft.network.FriendlyByteBuf $$0) {
         int $$1 = $$0.readVarInt();
         short $$2 = $$0.readShort();
         short $$3 = $$0.readShort();
         short $$4 = $$0.readShort();
         byte $$5 = $$0.readByte();
         byte $$6 = $$0.readByte();
         boolean $$7 = $$0.readBoolean();
         return new ClientboundMoveEntityPacket.PosRot($$1, $$2, $$3, $$4, $$5, $$6, $$7);
      }

      private void write(net.minecraft.network.FriendlyByteBuf $$0) {
         $$0.writeVarInt(this.entityId);
         $$0.writeShort(this.xa);
         $$0.writeShort(this.ya);
         $$0.writeShort(this.za);
         $$0.writeByte(this.yRot);
         $$0.writeByte(this.xRot);
         $$0.writeBoolean(this.onGround);
      }

      @Override
      public PacketType<ClientboundMoveEntityPacket.PosRot> type() {
         return GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_POS_ROT;
      }
   }

   public static class Rot extends ClientboundMoveEntityPacket {
      public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundMoveEntityPacket.Rot> STREAM_CODEC = Packet.codec(
         ClientboundMoveEntityPacket.Rot::write, ClientboundMoveEntityPacket.Rot::read
      );

      public Rot(int $$0, byte $$1, byte $$2, boolean $$3) {
         super($$0, (short)0, (short)0, (short)0, $$1, $$2, $$3, true, false);
      }

      private static ClientboundMoveEntityPacket.Rot read(net.minecraft.network.FriendlyByteBuf $$0) {
         int $$1 = $$0.readVarInt();
         byte $$2 = $$0.readByte();
         byte $$3 = $$0.readByte();
         boolean $$4 = $$0.readBoolean();
         return new ClientboundMoveEntityPacket.Rot($$1, $$2, $$3, $$4);
      }

      private void write(net.minecraft.network.FriendlyByteBuf $$0) {
         $$0.writeVarInt(this.entityId);
         $$0.writeByte(this.yRot);
         $$0.writeByte(this.xRot);
         $$0.writeBoolean(this.onGround);
      }

      @Override
      public PacketType<ClientboundMoveEntityPacket.Rot> type() {
         return GamePacketTypes.CLIENTBOUND_MOVE_ENTITY_ROT;
      }
   }
}
