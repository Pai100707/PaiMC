package net.minecraft.world.waypoints;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public interface WaypointTransmitter extends net.minecraft.world.waypoints.Waypoint {
   int REALLY_FAR_DISTANCE = 332;

   boolean isTransmittingWaypoint();

   Optional<net.minecraft.world.waypoints.WaypointTransmitter.Connection> makeWaypointConnectionWith(ServerPlayer var1);

   net.minecraft.world.waypoints.Waypoint.Icon waypointIcon();

   static boolean doesSourceIgnoreReceiver(LivingEntity $$0, ServerPlayer $$1) {
      if ($$1.isSpectator()) {
         return false;
      } else if (!$$0.isSpectator() && !$$0.hasIndirectPassenger($$1)) {
         double $$2 = Math.min($$0.getAttributeValue(Attributes.WAYPOINT_TRANSMIT_RANGE), $$1.getAttributeValue(Attributes.WAYPOINT_RECEIVE_RANGE));
         return $$0.distanceTo($$1) >= $$2;
      } else {
         return true;
      }
   }

   static boolean isChunkVisible(ChunkPos $$0, ServerPlayer $$1) {
      return $$1.getChunkTrackingView().isInViewDistance($$0.x, $$0.z);
   }

   static boolean isReallyFar(LivingEntity $$0, ServerPlayer $$1) {
      return $$0.distanceTo($$1) > 332.0F;
   }

   public interface BlockConnection extends net.minecraft.world.waypoints.WaypointTransmitter.Connection {
      int distanceManhattan();

      @Override
      default boolean isBroken() {
         return this.distanceManhattan() > 1;
      }
   }

   public interface ChunkConnection extends net.minecraft.world.waypoints.WaypointTransmitter.Connection {
      int distanceChessboard();

      @Override
      default boolean isBroken() {
         return this.distanceChessboard() > 1;
      }
   }

   public interface Connection {
      void connect();

      void disconnect();

      void update();

      boolean isBroken();
   }

   public static class EntityAzimuthConnection implements net.minecraft.world.waypoints.WaypointTransmitter.Connection {
      private final LivingEntity source;
      private final net.minecraft.world.waypoints.Waypoint.Icon icon;
      private final ServerPlayer receiver;
      private float lastAngle;

      public EntityAzimuthConnection(LivingEntity $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, ServerPlayer $$2) {
         this.source = $$0;
         this.icon = $$1;
         this.receiver = $$2;
         Vec3 $$3 = $$2.position().subtract($$0.position()).rotateClockwise90();
         this.lastAngle = (float)Mth.atan2($$3.z(), $$3.x());
      }

      @Override
      public boolean isBroken() {
         return net.minecraft.world.waypoints.WaypointTransmitter.doesSourceIgnoreReceiver(this.source, this.receiver)
            || net.minecraft.world.waypoints.WaypointTransmitter.isChunkVisible(this.source.chunkPosition(), this.receiver)
            || !net.minecraft.world.waypoints.WaypointTransmitter.isReallyFar(this.source, this.receiver);
      }

      @Override
      public void connect() {
         this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointAzimuth(this.source.getUUID(), this.icon, this.lastAngle));
      }

      @Override
      public void disconnect() {
         this.receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(this.source.getUUID()));
      }

      @Override
      public void update() {
         Vec3 $$0 = this.receiver.position().subtract(this.source.position()).rotateClockwise90();
         float $$1 = (float)Mth.atan2($$0.z(), $$0.x());
         if (Mth.abs($$1 - this.lastAngle) > 0.008726646F) {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointAzimuth(this.source.getUUID(), this.icon, $$1));
            this.lastAngle = $$1;
         }
      }
   }

   public static class EntityBlockConnection implements net.minecraft.world.waypoints.WaypointTransmitter.BlockConnection {
      private final LivingEntity source;
      private final net.minecraft.world.waypoints.Waypoint.Icon icon;
      private final ServerPlayer receiver;
      private BlockPos lastPosition;

      public EntityBlockConnection(LivingEntity $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, ServerPlayer $$2) {
         this.source = $$0;
         this.receiver = $$2;
         this.icon = $$1;
         this.lastPosition = $$0.blockPosition();
      }

      @Override
      public void connect() {
         this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointPosition(this.source.getUUID(), this.icon, this.lastPosition));
      }

      @Override
      public void disconnect() {
         this.receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(this.source.getUUID()));
      }

      @Override
      public void update() {
         BlockPos $$0 = this.source.blockPosition();
         if ($$0.distManhattan(this.lastPosition) > 0) {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointPosition(this.source.getUUID(), this.icon, $$0));
            this.lastPosition = $$0;
         }
      }

      @Override
      public int distanceManhattan() {
         return this.lastPosition.distManhattan(this.source.blockPosition());
      }

      @Override
      public boolean isBroken() {
         return net.minecraft.world.waypoints.WaypointTransmitter.BlockConnection.super.isBroken()
            || net.minecraft.world.waypoints.WaypointTransmitter.doesSourceIgnoreReceiver(this.source, this.receiver);
      }
   }

   public static class EntityChunkConnection implements net.minecraft.world.waypoints.WaypointTransmitter.ChunkConnection {
      private final LivingEntity source;
      private final net.minecraft.world.waypoints.Waypoint.Icon icon;
      private final ServerPlayer receiver;
      private ChunkPos lastPosition;

      public EntityChunkConnection(LivingEntity $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, ServerPlayer $$2) {
         this.source = $$0;
         this.icon = $$1;
         this.receiver = $$2;
         this.lastPosition = $$0.chunkPosition();
      }

      @Override
      public int distanceChessboard() {
         return this.lastPosition.getChessboardDistance(this.source.chunkPosition());
      }

      @Override
      public void connect() {
         this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointChunk(this.source.getUUID(), this.icon, this.lastPosition));
      }

      @Override
      public void disconnect() {
         this.receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(this.source.getUUID()));
      }

      @Override
      public void update() {
         ChunkPos $$0 = this.source.chunkPosition();
         if ($$0.getChessboardDistance(this.lastPosition) > 0) {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointChunk(this.source.getUUID(), this.icon, $$0));
            this.lastPosition = $$0;
         }
      }

      @Override
      public boolean isBroken() {
         return !net.minecraft.world.waypoints.WaypointTransmitter.ChunkConnection.super.isBroken()
               && !net.minecraft.world.waypoints.WaypointTransmitter.doesSourceIgnoreReceiver(this.source, this.receiver)
            ? net.minecraft.world.waypoints.WaypointTransmitter.isChunkVisible(this.lastPosition, this.receiver)
            : true;
      }
   }
}
