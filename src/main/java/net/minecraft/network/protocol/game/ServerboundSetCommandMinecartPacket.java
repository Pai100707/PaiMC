package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;

public class ServerboundSetCommandMinecartPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundSetCommandMinecartPacket> STREAM_CODEC = Packet.codec(
      ServerboundSetCommandMinecartPacket::write, ServerboundSetCommandMinecartPacket::new
   );
   private final int entity;
   private final String command;
   private final boolean trackOutput;

   public ServerboundSetCommandMinecartPacket(int $$0, String $$1, boolean $$2) {
      this.entity = $$0;
      this.command = $$1;
      this.trackOutput = $$2;
   }

   private ServerboundSetCommandMinecartPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.entity = $$0.readVarInt();
      this.command = $$0.readUtf();
      this.trackOutput = $$0.readBoolean();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.entity);
      $$0.writeUtf(this.command);
      $$0.writeBoolean(this.trackOutput);
   }

   @Override
   public PacketType<ServerboundSetCommandMinecartPacket> type() {
      return GamePacketTypes.SERVERBOUND_SET_COMMAND_MINECART;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleSetCommandMinecart(this);
   }

   
   public BaseCommandBlock getCommandBlock(Level $$0) {
      Entity $$1 = $$0.getEntity(this.entity);
      return $$1 instanceof MinecartCommandBlock ? ((MinecartCommandBlock)$$1).getCommandBlock() : null;
   }

   public String getCommand() {
      return this.command;
   }

   public boolean isTrackOutput() {
      return this.trackOutput;
   }
}
