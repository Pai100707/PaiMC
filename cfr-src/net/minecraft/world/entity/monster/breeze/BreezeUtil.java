/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster.breeze;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BreezeUtil {
    private static final double MAX_LINE_OF_SIGHT_TEST_RANGE = 50.0;

    public static Vec3 randomPointBehindTarget(LivingEntity $$0, RandomSource $$1) {
        int $$2 = 90;
        float $$3 = $$0.yHeadRot + 180.0f + (float)$$1.nextGaussian() * 90.0f / 2.0f;
        float $$4 = Mth.lerp($$1.nextFloat(), 4.0f, 8.0f);
        Vec3 $$5 = Vec3.directionFromRotation(0.0f, $$3).scale($$4);
        return $$0.position().add($$5);
    }

    public static boolean hasLineOfSight(Breeze $$0, Vec3 $$1) {
        Vec3 $$2 = new Vec3($$0.getX(), $$0.getY(), $$0.getZ());
        if ($$1.distanceTo($$2) > BreezeUtil.getMaxLineOfSightTestRange($$0)) {
            return false;
        }
        return $$0.level().clip(new ClipContext($$2, $$1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, $$0)).getType() == HitResult.Type.MISS;
    }

    private static double getMaxLineOfSightTestRange(Breeze $$0) {
        return Math.max(50.0, $$0.getAttributeValue(Attributes.FOLLOW_RANGE));
    }
}

