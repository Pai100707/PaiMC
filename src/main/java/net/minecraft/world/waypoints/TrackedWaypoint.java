package net.minecraft.world.waypoints;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;

public abstract class TrackedWaypoint implements net.minecraft.world.waypoints.Waypoint {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final StreamCodec<ByteBuf, net.minecraft.world.waypoints.TrackedWaypoint> STREAM_CODEC = StreamCodec.ofMember(
      net.minecraft.world.waypoints.TrackedWaypoint::write, net.minecraft.world.waypoints.TrackedWaypoint::read
   );
   protected final Either<UUID, String> identifier;
   private final net.minecraft.world.waypoints.Waypoint.Icon icon;
   private final net.minecraft.world.waypoints.TrackedWaypoint.Type type;

   TrackedWaypoint(Either<UUID, String> $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, net.minecraft.world.waypoints.TrackedWaypoint.Type $$2) {
      this.identifier = $$0;
      this.icon = $$1;
      this.type = $$2;
   }

   public Either<UUID, String> id() {
      return this.identifier;
   }

   public abstract void update(net.minecraft.world.waypoints.TrackedWaypoint var1);

   public void write(ByteBuf $$0) {
      FriendlyByteBuf $$1 = new FriendlyByteBuf($$0);
      $$1.writeEither(this.identifier, UUIDUtil.STREAM_CODEC, FriendlyByteBuf::writeUtf);
      net.minecraft.world.waypoints.Waypoint.Icon.STREAM_CODEC.encode($$1, this.icon);
      $$1.writeEnum(this.type);
      this.writeContents($$0);
   }

   public abstract void writeContents(ByteBuf var1);

   private static net.minecraft.world.waypoints.TrackedWaypoint read(ByteBuf $$0) {
      FriendlyByteBuf $$1 = new FriendlyByteBuf($$0);
      Either<UUID, String> $$2 = $$1.readEither(UUIDUtil.STREAM_CODEC, FriendlyByteBuf::readUtf);
      net.minecraft.world.waypoints.Waypoint.Icon $$3 = (net.minecraft.world.waypoints.Waypoint.Icon)net.minecraft.world.waypoints.Waypoint.Icon.STREAM_CODEC
         .decode($$1);
      net.minecraft.world.waypoints.TrackedWaypoint.Type $$4 = (net.minecraft.world.waypoints.TrackedWaypoint.Type)$$1.readEnum(
         net.minecraft.world.waypoints.TrackedWaypoint.Type.class
      );
      return (net.minecraft.world.waypoints.TrackedWaypoint)$$4.constructor.apply($$2, $$3, $$1);
   }

   public static net.minecraft.world.waypoints.TrackedWaypoint setPosition(UUID $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, Vec3i $$2) {
      return new net.minecraft.world.waypoints.TrackedWaypoint.Vec3iWaypoint($$0, $$1, $$2);
   }

   public static net.minecraft.world.waypoints.TrackedWaypoint setChunk(UUID $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, ChunkPos $$2) {
      return new net.minecraft.world.waypoints.TrackedWaypoint.ChunkWaypoint($$0, $$1, $$2);
   }

   public static net.minecraft.world.waypoints.TrackedWaypoint setAzimuth(UUID $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, float $$2) {
      return new net.minecraft.world.waypoints.TrackedWaypoint.AzimuthWaypoint($$0, $$1, $$2);
   }

   public static net.minecraft.world.waypoints.TrackedWaypoint empty(UUID $$0) {
      return new net.minecraft.world.waypoints.TrackedWaypoint.EmptyWaypoint($$0);
   }

   public abstract double yawAngleToCamera(
      Level var1, net.minecraft.world.waypoints.TrackedWaypoint.Camera var2, net.minecraft.world.waypoints.PartialTickSupplier var3
   );

   public abstract net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection pitchDirectionToCamera(
      Level var1, net.minecraft.world.waypoints.TrackedWaypoint.Projector var2, net.minecraft.world.waypoints.PartialTickSupplier var3
   );

   public abstract double distanceSquared(Entity var1);

   public net.minecraft.world.waypoints.Waypoint.Icon icon() {
      return this.icon;
   }

   static class AzimuthWaypoint extends net.minecraft.world.waypoints.TrackedWaypoint {
      private float angle;

      public AzimuthWaypoint(UUID $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, float $$2) {
         super(Either.left($$0), $$1, net.minecraft.world.waypoints.TrackedWaypoint.Type.AZIMUTH);
         this.angle = $$2;
      }

      public AzimuthWaypoint(Either<UUID, String> $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, FriendlyByteBuf $$2) {
         super($$0, $$1, net.minecraft.world.waypoints.TrackedWaypoint.Type.AZIMUTH);
         this.angle = $$2.readFloat();
      }

      @Override
      public void update(net.minecraft.world.waypoints.TrackedWaypoint $$0) {
         if ($$0 instanceof net.minecraft.world.waypoints.TrackedWaypoint.AzimuthWaypoint $$1) {
            this.angle = $$1.angle;
         } else {
            net.minecraft.world.waypoints.TrackedWaypoint.LOGGER.warn("Unsupported Waypoint update operation: {}", $$0.getClass());
         }
      }

      @Override
      public void writeContents(ByteBuf $$0) {
         $$0.writeFloat(this.angle);
      }

      @Override
      public double yawAngleToCamera(Level $$0, net.minecraft.world.waypoints.TrackedWaypoint.Camera $$1, net.minecraft.world.waypoints.PartialTickSupplier $$2) {
         return Mth.degreesDifference($$1.yaw(), this.angle * (180.0F / (float)Math.PI));
      }

      @Override
      public net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection pitchDirectionToCamera(
         Level $$0, net.minecraft.world.waypoints.TrackedWaypoint.Projector $$1, net.minecraft.world.waypoints.PartialTickSupplier $$2
      ) {
         double $$3 = $$1.projectHorizonToScreen();
         if ($$3 < -1.0) {
            return net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection.DOWN;
         } else {
            return $$3 > 1.0
               ? net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection.UP
               : net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection.NONE;
         }
      }

      @Override
      public double distanceSquared(Entity $$0) {
         return Double.POSITIVE_INFINITY;
      }
   }

   public interface Camera {
      float yaw();

      Vec3 position();
   }

   static class ChunkWaypoint extends net.minecraft.world.waypoints.TrackedWaypoint {
      private ChunkPos chunkPos;

      public ChunkWaypoint(UUID $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, ChunkPos $$2) {
         super(Either.left($$0), $$1, net.minecraft.world.waypoints.TrackedWaypoint.Type.CHUNK);
         this.chunkPos = $$2;
      }

      public ChunkWaypoint(Either<UUID, String> $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, FriendlyByteBuf $$2) {
         super($$0, $$1, net.minecraft.world.waypoints.TrackedWaypoint.Type.CHUNK);
         this.chunkPos = new ChunkPos($$2.readVarInt(), $$2.readVarInt());
      }

      @Override
      public void update(net.minecraft.world.waypoints.TrackedWaypoint $$0) {
         if ($$0 instanceof net.minecraft.world.waypoints.TrackedWaypoint.ChunkWaypoint $$1) {
            this.chunkPos = $$1.chunkPos;
         } else {
            net.minecraft.world.waypoints.TrackedWaypoint.LOGGER.warn("Unsupported Waypoint update operation: {}", $$0.getClass());
         }
      }

      @Override
      public void writeContents(ByteBuf $$0) {
         VarInt.write($$0, this.chunkPos.x);
         VarInt.write($$0, this.chunkPos.z);
      }

      private Vec3 position(double $$0) {
         return Vec3.atCenterOf(this.chunkPos.getMiddleBlockPosition((int)$$0));
      }

      @Override
      public double yawAngleToCamera(Level $$0, net.minecraft.world.waypoints.TrackedWaypoint.Camera $$1, net.minecraft.world.waypoints.PartialTickSupplier $$2) {
         Vec3 $$3 = $$1.position();
         Vec3 $$4 = $$3.subtract(this.position($$3.y())).rotateClockwise90();
         float $$5 = (float)Mth.atan2($$4.z(), $$4.x()) * (180.0F / (float)Math.PI);
         return Mth.degreesDifference($$1.yaw(), $$5);
      }

      @Override
      public net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection pitchDirectionToCamera(
         Level $$0, net.minecraft.world.waypoints.TrackedWaypoint.Projector $$1, net.minecraft.world.waypoints.PartialTickSupplier $$2
      ) {
         double $$3 = $$1.projectHorizonToScreen();
         if ($$3 < -1.0) {
            return net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection.DOWN;
         } else {
            return $$3 > 1.0
               ? net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection.UP
               : net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection.NONE;
         }
      }

      @Override
      public double distanceSquared(Entity $$0) {
         return $$0.distanceToSqr(Vec3.atCenterOf(this.chunkPos.getMiddleBlockPosition($$0.getBlockY())));
      }
   }

   static class EmptyWaypoint extends net.minecraft.world.waypoints.TrackedWaypoint {
      private EmptyWaypoint(Either<UUID, String> $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, FriendlyByteBuf $$2) {
         super($$0, $$1, net.minecraft.world.waypoints.TrackedWaypoint.Type.EMPTY);
      }

      EmptyWaypoint(UUID $$0) {
         super(Either.left($$0), net.minecraft.world.waypoints.Waypoint.Icon.NULL, net.minecraft.world.waypoints.TrackedWaypoint.Type.EMPTY);
      }

      @Override
      public void update(net.minecraft.world.waypoints.TrackedWaypoint $$0) {
      }

      @Override
      public void writeContents(ByteBuf $$0) {
      }

      @Override
      public double yawAngleToCamera(Level $$0, net.minecraft.world.waypoints.TrackedWaypoint.Camera $$1, net.minecraft.world.waypoints.PartialTickSupplier $$2) {
         return Double.NaN;
      }

      @Override
      public net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection pitchDirectionToCamera(
         Level $$0, net.minecraft.world.waypoints.TrackedWaypoint.Projector $$1, net.minecraft.world.waypoints.PartialTickSupplier $$2
      ) {
         return net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection.NONE;
      }

      @Override
      public double distanceSquared(Entity $$0) {
         return Double.POSITIVE_INFINITY;
      }
   }

   public static enum PitchDirection {
      NONE,
      UP,
      DOWN;
   }

   public interface Projector {
      Vec3 projectPointToScreen(Vec3 var1);

      double projectHorizonToScreen();
   }

   static enum Type {
      EMPTY(net.minecraft.world.waypoints.TrackedWaypoint.EmptyWaypoint::new),
      VEC3I(net.minecraft.world.waypoints.TrackedWaypoint.Vec3iWaypoint::new),
      CHUNK(net.minecraft.world.waypoints.TrackedWaypoint.ChunkWaypoint::new),
      AZIMUTH(net.minecraft.world.waypoints.TrackedWaypoint.AzimuthWaypoint::new);

      final TriFunction<Either<UUID, String>, net.minecraft.world.waypoints.Waypoint.Icon, FriendlyByteBuf, net.minecraft.world.waypoints.TrackedWaypoint> constructor;

      private Type(
         final TriFunction<Either<UUID, String>, net.minecraft.world.waypoints.Waypoint.Icon, FriendlyByteBuf, net.minecraft.world.waypoints.TrackedWaypoint> $$0
      ) {
         this.constructor = $$0;
      }
   }

   static class Vec3iWaypoint extends net.minecraft.world.waypoints.TrackedWaypoint {
      private Vec3i vector;

      public Vec3iWaypoint(UUID $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, Vec3i $$2) {
         super(Either.left($$0), $$1, net.minecraft.world.waypoints.TrackedWaypoint.Type.VEC3I);
         this.vector = $$2;
      }

      public Vec3iWaypoint(Either<UUID, String> $$0, net.minecraft.world.waypoints.Waypoint.Icon $$1, FriendlyByteBuf $$2) {
         super($$0, $$1, net.minecraft.world.waypoints.TrackedWaypoint.Type.VEC3I);
         this.vector = new Vec3i($$2.readVarInt(), $$2.readVarInt(), $$2.readVarInt());
      }

      @Override
      public void update(net.minecraft.world.waypoints.TrackedWaypoint $$0) {
         if ($$0 instanceof net.minecraft.world.waypoints.TrackedWaypoint.Vec3iWaypoint $$1) {
            this.vector = $$1.vector;
         } else {
            net.minecraft.world.waypoints.TrackedWaypoint.LOGGER.warn("Unsupported Waypoint update operation: {}", $$0.getClass());
         }
      }

      @Override
      public void writeContents(ByteBuf $$0) {
         VarInt.write($$0, this.vector.getX());
         VarInt.write($$0, this.vector.getY());
         VarInt.write($$0, this.vector.getZ());
      }

      private Vec3 position(Level $$0, net.minecraft.world.waypoints.PartialTickSupplier $$1) {
         return this.identifier
            .left()
            .<Entity>map($$0::getEntity)
            .map($$1x -> $$1x.blockPosition().distManhattan(this.vector) > 3 ? null : $$1x.getEyePosition($$1.apply($$1x)))
            .orElseGet(() -> Vec3.atCenterOf(this.vector));
      }

      @Override
      public double yawAngleToCamera(Level $$0, net.minecraft.world.waypoints.TrackedWaypoint.Camera $$1, net.minecraft.world.waypoints.PartialTickSupplier $$2) {
         Vec3 $$3 = $$1.position().subtract(this.position($$0, $$2)).rotateClockwise90();
         float $$4 = (float)Mth.atan2($$3.z(), $$3.x()) * (180.0F / (float)Math.PI);
         return Mth.degreesDifference($$1.yaw(), $$4);
      }

      @Override
      public net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection pitchDirectionToCamera(
         Level $$0, net.minecraft.world.waypoints.TrackedWaypoint.Projector $$1, net.minecraft.world.waypoints.PartialTickSupplier $$2
      ) {
         Vec3 $$3 = $$1.projectPointToScreen(this.position($$0, $$2));
         boolean $$4 = $$3.z > 1.0;
         double $$5 = $$4 ? -$$3.y : $$3.y;
         if ($$5 < -1.0) {
            return net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection.DOWN;
         } else if ($$5 > 1.0) {
            return net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection.UP;
         } else {
            if ($$4) {
               if ($$3.y > 0.0) {
                  return net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection.UP;
               }

               if ($$3.y < 0.0) {
                  return net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection.DOWN;
               }
            }

            return net.minecraft.world.waypoints.TrackedWaypoint.PitchDirection.NONE;
         }
      }

      @Override
      public double distanceSquared(Entity $$0) {
         return $$0.distanceToSqr(Vec3.atCenterOf(this.vector));
      }
   }
}
