/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record EntityDimensions(float width, float height, float eyeHeight, EntityAttachments attachments, boolean fixed) {
    private EntityDimensions(float $$0, float $$1, boolean $$2) {
        this($$0, $$1, EntityDimensions.defaultEyeHeight($$1), EntityAttachments.createDefault($$0, $$1), $$2);
    }

    private static float defaultEyeHeight(float $$0) {
        return $$0 * 0.85f;
    }

    public AABB makeBoundingBox(Vec3 $$0) {
        return this.makeBoundingBox($$0.x, $$0.y, $$0.z);
    }

    public AABB makeBoundingBox(double $$0, double $$1, double $$2) {
        float $$3 = this.width / 2.0f;
        float $$4 = this.height;
        return new AABB($$0 - (double)$$3, $$1, $$2 - (double)$$3, $$0 + (double)$$3, $$1 + (double)$$4, $$2 + (double)$$3);
    }

    public EntityDimensions scale(float $$0) {
        return this.scale($$0, $$0);
    }

    public EntityDimensions scale(float $$0, float $$1) {
        if (this.fixed || $$0 == 1.0f && $$1 == 1.0f) {
            return this;
        }
        return new EntityDimensions(this.width * $$0, this.height * $$1, this.eyeHeight * $$1, this.attachments.scale($$0, $$1, $$0), false);
    }

    public static EntityDimensions scalable(float $$0, float $$1) {
        return new EntityDimensions($$0, $$1, false);
    }

    public static EntityDimensions fixed(float $$0, float $$1) {
        return new EntityDimensions($$0, $$1, true);
    }

    public EntityDimensions withEyeHeight(float $$0) {
        return new EntityDimensions(this.width, this.height, $$0, this.attachments, this.fixed);
    }

    public EntityDimensions withAttachments(EntityAttachments.Builder $$0) {
        return new EntityDimensions(this.width, this.height, this.eyeHeight, $$0.build(this.width, this.height), this.fixed);
    }
}

