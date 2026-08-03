package net.minecraft.world.entity;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public record PositionMoveRotation(Vec3 position, Vec3 deltaMovement, float yRot, float xRot) {
   public static final StreamCodec<FriendlyByteBuf, net.minecraft.world.entity.PositionMoveRotation> STREAM_CODEC = StreamCodec.composite(
      Vec3.STREAM_CODEC,
      net.minecraft.world.entity.PositionMoveRotation::position,
      Vec3.STREAM_CODEC,
      net.minecraft.world.entity.PositionMoveRotation::deltaMovement,
      ByteBufCodecs.FLOAT,
      net.minecraft.world.entity.PositionMoveRotation::yRot,
      ByteBufCodecs.FLOAT,
      net.minecraft.world.entity.PositionMoveRotation::xRot,
      net.minecraft.world.entity.PositionMoveRotation::new
   );

   public static net.minecraft.world.entity.PositionMoveRotation of(net.minecraft.world.entity.Entity $$0) {
      return $$0.isInterpolating()
         ? new net.minecraft.world.entity.PositionMoveRotation(
            $$0.getInterpolation().position(), $$0.getKnownMovement(), $$0.getInterpolation().yRot(), $$0.getInterpolation().xRot()
         )
         : new net.minecraft.world.entity.PositionMoveRotation($$0.position(), $$0.getKnownMovement(), $$0.getYRot(), $$0.getXRot());
   }

   public net.minecraft.world.entity.PositionMoveRotation withRotation(float $$0, float $$1) {
      return new net.minecraft.world.entity.PositionMoveRotation(this.position(), this.deltaMovement(), $$0, $$1);
   }

   public static net.minecraft.world.entity.PositionMoveRotation of(TeleportTransition $$0) {
      return new net.minecraft.world.entity.PositionMoveRotation($$0.position(), $$0.deltaMovement(), $$0.yRot(), $$0.xRot());
   }

   public static net.minecraft.world.entity.PositionMoveRotation calculateAbsolute(
      net.minecraft.world.entity.PositionMoveRotation $$0, net.minecraft.world.entity.PositionMoveRotation $$1, Set<net.minecraft.world.entity.Relative> $$2
   ) {
      double $$3 = $$2.contains(net.minecraft.world.entity.Relative.X) ? $$0.position.x : 0.0;
      double $$4 = $$2.contains(net.minecraft.world.entity.Relative.Y) ? $$0.position.y : 0.0;
      double $$5 = $$2.contains(net.minecraft.world.entity.Relative.Z) ? $$0.position.z : 0.0;
      float $$6 = $$2.contains(net.minecraft.world.entity.Relative.Y_ROT) ? $$0.yRot : 0.0F;
      float $$7 = $$2.contains(net.minecraft.world.entity.Relative.X_ROT) ? $$0.xRot : 0.0F;
      Vec3 $$8 = new Vec3($$3 + $$1.position.x, $$4 + $$1.position.y, $$5 + $$1.position.z);
      float $$9 = $$6 + $$1.yRot;
      float $$10 = Mth.clamp($$7 + $$1.xRot, -90.0F, 90.0F);
      Vec3 $$11 = $$0.deltaMovement;
      if ($$2.contains(net.minecraft.world.entity.Relative.ROTATE_DELTA)) {
         float $$12 = $$0.yRot - $$9;
         float $$13 = $$0.xRot - $$10;
         $$11 = $$11.xRot((float)Math.toRadians($$13));
         $$11 = $$11.yRot((float)Math.toRadians($$12));
      }

      Vec3 $$14 = new Vec3(
         calculateDelta($$11.x, $$1.deltaMovement.x, $$2, net.minecraft.world.entity.Relative.DELTA_X),
         calculateDelta($$11.y, $$1.deltaMovement.y, $$2, net.minecraft.world.entity.Relative.DELTA_Y),
         calculateDelta($$11.z, $$1.deltaMovement.z, $$2, net.minecraft.world.entity.Relative.DELTA_Z)
      );
      return new net.minecraft.world.entity.PositionMoveRotation($$8, $$14, $$9, $$10);
   }

   private static double calculateDelta(double $$0, double $$1, Set<net.minecraft.world.entity.Relative> $$2, net.minecraft.world.entity.Relative $$3) {
      return $$2.contains($$3) ? $$0 + $$1 : $$1;
   }
}
