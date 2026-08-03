package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundInitializeBorderPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundInitializeBorderPacket> STREAM_CODEC = Packet.codec(
      ClientboundInitializeBorderPacket::write, ClientboundInitializeBorderPacket::new
   );
   private final double newCenterX;
   private final double newCenterZ;
   private final double oldSize;
   private final double newSize;
   private final long lerpTime;
   private final int newAbsoluteMaxSize;
   private final int warningBlocks;
   private final int warningTime;

   private ClientboundInitializeBorderPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.newCenterX = $$0.readDouble();
      this.newCenterZ = $$0.readDouble();
      this.oldSize = $$0.readDouble();
      this.newSize = $$0.readDouble();
      this.lerpTime = $$0.readVarLong();
      this.newAbsoluteMaxSize = $$0.readVarInt();
      this.warningBlocks = $$0.readVarInt();
      this.warningTime = $$0.readVarInt();
   }

   public ClientboundInitializeBorderPacket(WorldBorder $$0) {
      this.newCenterX = $$0.getCenterX();
      this.newCenterZ = $$0.getCenterZ();
      this.oldSize = $$0.getSize();
      this.newSize = $$0.getLerpTarget();
      this.lerpTime = $$0.getLerpTime();
      this.newAbsoluteMaxSize = $$0.getAbsoluteMaxSize();
      this.warningBlocks = $$0.getWarningBlocks();
      this.warningTime = $$0.getWarningTime();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeDouble(this.newCenterX);
      $$0.writeDouble(this.newCenterZ);
      $$0.writeDouble(this.oldSize);
      $$0.writeDouble(this.newSize);
      $$0.writeVarLong(this.lerpTime);
      $$0.writeVarInt(this.newAbsoluteMaxSize);
      $$0.writeVarInt(this.warningBlocks);
      $$0.writeVarInt(this.warningTime);
   }

   @Override
   public PacketType<ClientboundInitializeBorderPacket> type() {
      return GamePacketTypes.CLIENTBOUND_INITIALIZE_BORDER;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleInitializeBorder(this);
   }

   public double getNewCenterX() {
      return this.newCenterX;
   }

   public double getNewCenterZ() {
      return this.newCenterZ;
   }

   public double getNewSize() {
      return this.newSize;
   }

   public double getOldSize() {
      return this.oldSize;
   }

   public long getLerpTime() {
      return this.lerpTime;
   }

   public int getNewAbsoluteMaxSize() {
      return this.newAbsoluteMaxSize;
   }

   public int getWarningTime() {
      return this.warningTime;
   }

   public int getWarningBlocks() {
      return this.warningBlocks;
   }
}
