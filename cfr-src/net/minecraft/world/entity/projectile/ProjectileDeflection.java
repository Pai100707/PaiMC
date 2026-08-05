/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ProjectileDeflection {
    public static final ProjectileDeflection NONE = ($$0, $$1, $$2) -> {};
    public static final ProjectileDeflection REVERSE = ($$0, $$1, $$2) -> {
        float $$3 = 170.0f + $$2.nextFloat() * 20.0f;
        $$0.setDeltaMovement($$0.getDeltaMovement().scale(-0.5));
        $$0.setYRot($$0.getYRot() + $$3);
        $$0.yRotO += $$3;
        $$0.needsSync = true;
    };
    public static final ProjectileDeflection AIM_DEFLECT = ($$0, $$1, $$2) -> {
        if ($$1 != null) {
            Vec3 $$3 = $$1.getLookAngle();
            $$0.setDeltaMovement($$3);
            $$0.needsSync = true;
        }
    };
    public static final ProjectileDeflection MOMENTUM_DEFLECT = ($$0, $$1, $$2) -> {
        if ($$1 != null) {
            Vec3 $$3 = $$1.getDeltaMovement().normalize();
            $$0.setDeltaMovement($$3);
            $$0.needsSync = true;
        }
    };

    public void deflect(Projectile var1, @Nullable Entity var2, RandomSource var3);
}

