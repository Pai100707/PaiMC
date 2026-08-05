/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public record PositionMoveRotation(Vec3 position, Vec3 deltaMovement, float yRot, float xRot) {
    public static final StreamCodec<FriendlyByteBuf, PositionMoveRotation> STREAM_CODEC = StreamCodec.composite(Vec3.STREAM_CODEC, PositionMoveRotation::position, Vec3.STREAM_CODEC, PositionMoveRotation::deltaMovement, ByteBufCodecs.FLOAT, PositionMoveRotation::yRot, ByteBufCodecs.FLOAT, PositionMoveRotation::xRot, PositionMoveRotation::new);

    public static PositionMoveRotation of(Entity $$0) {
        if ($$0.isInterpolating()) {
            return new PositionMoveRotation($$0.getInterpolation().position(), $$0.getKnownMovement(), $$0.getInterpolation().yRot(), $$0.getInterpolation().xRot());
        }
        return new PositionMoveRotation($$0.position(), $$0.getKnownMovement(), $$0.getYRot(), $$0.getXRot());
    }

    public PositionMoveRotation withRotation(float $$0, float $$1) {
        return new PositionMoveRotation(this.position(), this.deltaMovement(), $$0, $$1);
    }

    public static PositionMoveRotation of(TeleportTransition $$0) {
        return new PositionMoveRotation($$0.position(), $$0.deltaMovement(), $$0.yRot(), $$0.xRot());
    }

    public static PositionMoveRotation calculateAbsolute(PositionMoveRotation $$0, PositionMoveRotation $$1, Set<Relative> $$2) {
        double $$3 = $$2.contains((Object)Relative.X) ? $$0.position.x : 0.0;
        double $$4 = $$2.contains((Object)Relative.Y) ? $$0.position.y : 0.0;
        double $$5 = $$2.contains((Object)Relative.Z) ? $$0.position.z : 0.0;
        float $$6 = $$2.contains((Object)Relative.Y_ROT) ? $$0.yRot : 0.0f;
        float $$7 = $$2.contains((Object)Relative.X_ROT) ? $$0.xRot : 0.0f;
        Vec3 $$8 = new Vec3($$3 + $$1.position.x, $$4 + $$1.position.y, $$5 + $$1.position.z);
        float $$9 = $$6 + $$1.yRot;
        float $$10 = Mth.clamp($$7 + $$1.xRot, -90.0f, 90.0f);
        Vec3 $$11 = $$0.deltaMovement;
        if ($$2.contains((Object)Relative.ROTATE_DELTA)) {
            float $$12 = $$0.yRot - $$9;
            float $$13 = $$0.xRot - $$10;
            $$11 = $$11.xRot((float)Math.toRadians($$13));
            $$11 = $$11.yRot((float)Math.toRadians($$12));
        }
        Vec3 $$14 = new Vec3(PositionMoveRotation.calculateDelta($$11.x, $$1.deltaMovement.x, $$2, Relative.DELTA_X), PositionMoveRotation.calculateDelta($$11.y, $$1.deltaMovement.y, $$2, Relative.DELTA_Y), PositionMoveRotation.calculateDelta($$11.z, $$1.deltaMovement.z, $$2, Relative.DELTA_Z));
        return new PositionMoveRotation($$8, $$14, $$9, $$10);
    }

    private static double calculateDelta(double $$0, double $$1, Set<Relative> $$2, Relative $$3) {
        return $$2.contains((Object)$$3) ? $$0 + $$1 : $$1;
    }
}

