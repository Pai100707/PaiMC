package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;

public class ServerboundPlayerCommandPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundPlayerCommandPacket> STREAM_CODEC = Packet.codec(
      ServerboundPlayerCommandPacket::write, ServerboundPlayerCommandPacket::new
   );
   private final int id;
   private final ServerboundPlayerCommandPacket.Action action;
   private final int data;

   public ServerboundPlayerCommandPacket(Entity $$0, ServerboundPlayerCommandPacket.Action $$1) {
      this($$0, $$1, 0);
   }

   public ServerboundPlayerCommandPacket(Entity $$0, ServerboundPlayerCommandPacket.Action $$1, int $$2) {
      this.id = $$0.getId();
      this.action = $$1;
      this.data = $$2;
   }

   private ServerboundPlayerCommandPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.id = $$0.readVarInt();
      this.action = $$0.readEnum(ServerboundPlayerCommandPacket.Action.class);
      this.data = $$0.readVarInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.id);
      $$0.writeEnum(this.action);
      $$0.writeVarInt(this.data);
   }

   @Override
   public PacketType<ServerboundPlayerCommandPacket> type() {
      return GamePacketTypes.SERVERBOUND_PLAYER_COMMAND;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handlePlayerCommand(this);
   }

   public int getId() {
      return this.id;
   }

   public ServerboundPlayerCommandPacket.Action getAction() {
      return this.action;
   }

   public int getData() {
      return this.data;
   }

   public static enum Action {
      STOP_SLEEPING,
      START_SPRINTING,
      STOP_SPRINTING,
      START_RIDING_JUMP,
      STOP_RIDING_JUMP,
      OPEN_INVENTORY,
      START_FALL_FLYING;
   }
}
