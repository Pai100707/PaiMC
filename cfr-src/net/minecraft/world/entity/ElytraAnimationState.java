/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ElytraAnimationState {
    private static final float DEFAULT_X_ROT = 0.2617994f;
    private static final float DEFAULT_Z_ROT = -0.2617994f;
    private float rotX;
    private float rotY;
    private float rotZ;
    private float rotXOld;
    private float rotYOld;
    private float rotZOld;
    private final LivingEntity entity;

    public ElytraAnimationState(LivingEntity $$0) {
        this.entity = $$0;
    }

    public void tick() {
        float $$11;
        float $$10;
        float $$9;
        this.rotXOld = this.rotX;
        this.rotYOld = this.rotY;
        this.rotZOld = this.rotZ;
        if (this.entity.isFallFlying()) {
            float $$0 = 1.0f;
            Vec3 $$1 = this.entity.getDeltaMovement();
            if ($$1.y < 0.0) {
                Vec3 $$2 = $$1.normalize();
                $$0 = 1.0f - (float)Math.pow(-$$2.y, 1.5);
            }
            float $$3 = Mth.lerp($$0, 0.2617994f, 0.34906584f);
            float $$4 = Mth.lerp($$0, -0.2617994f, -1.5707964f);
            float $$5 = 0.0f;
        } else if (this.entity.isCrouching()) {
            float $$6 = 0.6981317f;
            float $$7 = -0.7853982f;
            float $$8 = 0.08726646f;
        } else {
            $$9 = 0.2617994f;
            $$10 = -0.2617994f;
            $$11 = 0.0f;
        }
        this.rotX += ($$9 - this.rotX) * 0.3f;
        this.rotY += ($$11 - this.rotY) * 0.3f;
        this.rotZ += ($$10 - this.rotZ) * 0.3f;
    }

    public float getRotX(float $$0) {
        return Mth.lerp($$0, this.rotXOld, this.rotX);
    }

    public float getRotY(float $$0) {
        return Mth.lerp($$0, this.rotYOld, this.rotY);
    }

    public float getRotZ(float $$0) {
        return Mth.lerp($$0, this.rotZOld, this.rotZ);
    }
}

