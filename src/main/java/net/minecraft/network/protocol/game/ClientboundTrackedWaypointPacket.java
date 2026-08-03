package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.TrackedWaypointManager;
import net.minecraft.world.waypoints.WaypointManager;
import net.minecraft.world.waypoints.Waypoint.Icon;

public record ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.Operation operation, TrackedWaypoint waypoint)
   implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundTrackedWaypointPacket> STREAM_CODEC = StreamCodec.composite(
      ClientboundTrackedWaypointPacket.Operation.STREAM_CODEC,
      ClientboundTrackedWaypointPacket::operation,
      TrackedWaypoint.STREAM_CODEC,
      ClientboundTrackedWaypointPacket::waypoint,
      ClientboundTrackedWaypointPacket::new
   );

   public static ClientboundTrackedWaypointPacket removeWaypoint(UUID $$0) {
      return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.Operation.UNTRACK, TrackedWaypoint.empty($$0));
   }

   public static ClientboundTrackedWaypointPacket addWaypointPosition(UUID $$0, Icon $$1, Vec3i $$2) {
      return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.Operation.TRACK, TrackedWaypoint.setPosition($$0, $$1, $$2));
   }

   public static ClientboundTrackedWaypointPacket updateWaypointPosition(UUID $$0, Icon $$1, Vec3i $$2) {
      return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.Operation.UPDATE, TrackedWaypoint.setPosition($$0, $$1, $$2));
   }

   public static ClientboundTrackedWaypointPacket addWaypointChunk(UUID $$0, Icon $$1, ChunkPos $$2) {
      return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.Operation.TRACK, TrackedWaypoint.setChunk($$0, $$1, $$2));
   }

   public static ClientboundTrackedWaypointPacket updateWaypointChunk(UUID $$0, Icon $$1, ChunkPos $$2) {
      return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.Operation.UPDATE, TrackedWaypoint.setChunk($$0, $$1, $$2));
   }

   public static ClientboundTrackedWaypointPacket addWaypointAzimuth(UUID $$0, Icon $$1, float $$2) {
      return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.Operation.TRACK, TrackedWaypoint.setAzimuth($$0, $$1, $$2));
   }

   public static ClientboundTrackedWaypointPacket updateWaypointAzimuth(UUID $$0, Icon $$1, float $$2) {
      return new ClientboundTrackedWaypointPacket(ClientboundTrackedWaypointPacket.Operation.UPDATE, TrackedWaypoint.setAzimuth($$0, $$1, $$2));
   }

   @Override
   public PacketType<ClientboundTrackedWaypointPacket> type() {
      return GamePacketTypes.CLIENTBOUND_WAYPOINT;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleWaypoint(this);
   }

   public void apply(TrackedWaypointManager $$0) {
      this.operation.action.accept($$0, this.waypoint);
   }

   static enum Operation {
      TRACK(WaypointManager::trackWaypoint),
      UNTRACK(WaypointManager::untrackWaypoint),
      UPDATE(WaypointManager::updateWaypoint);

      final BiConsumer<TrackedWaypointManager, TrackedWaypoint> action;
      public static final IntFunction<ClientboundTrackedWaypointPacket.Operation> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.WRAP);
      public static final StreamCodec<ByteBuf, ClientboundTrackedWaypointPacket.Operation> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

      private Operation(final BiConsumer<TrackedWaypointManager, TrackedWaypoint> $$0) {
         this.action = $$0;
      }
   }
}
