/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityAttachments {
    private final Map<EntityAttachment, List<Vec3>> attachments;

    EntityAttachments(Map<EntityAttachment, List<Vec3>> $$0) {
        this.attachments = $$0;
    }

    public static EntityAttachments createDefault(float $$0, float $$1) {
        return EntityAttachments.builder().build($$0, $$1);
    }

    public static Builder builder() {
        return new Builder();
    }

    public EntityAttachments scale(float $$0, float $$1, float $$2) {
        return new EntityAttachments(Util.makeEnumMap(EntityAttachment.class, $$3 -> {
            ArrayList<Vec3> $$4 = new ArrayList<Vec3>();
            for (Vec3 $$5 : this.attachments.get($$3)) {
                $$4.add($$5.multiply($$0, $$1, $$2));
            }
            return $$4;
        }));
    }

    public @Nullable Vec3 getNullable(EntityAttachment $$0, int $$1, float $$2) {
        List<Vec3> $$3 = this.attachments.get((Object)$$0);
        if ($$1 < 0 || $$1 >= $$3.size()) {
            return null;
        }
        return EntityAttachments.transformPoint($$3.get($$1), $$2);
    }

    public Vec3 get(EntityAttachment $$0, int $$1, float $$2) {
        Vec3 $$3 = this.getNullable($$0, $$1, $$2);
        if ($$3 == null) {
            throw new IllegalStateException("Had no attachment point of type: " + String.valueOf((Object)$$0) + " for index: " + $$1);
        }
        return $$3;
    }

    public Vec3 getAverage(EntityAttachment $$0) {
        List<Vec3> $$1 = this.attachments.get((Object)$$0);
        if ($$1 == null || $$1.isEmpty()) {
            throw new IllegalStateException("No attachment points of type: PASSENGER");
        }
        Vec3 $$2 = Vec3.ZERO;
        for (Vec3 $$3 : $$1) {
            $$2 = $$2.add($$3);
        }
        return $$2.scale(1.0f / (float)$$1.size());
    }

    public Vec3 getClamped(EntityAttachment $$0, int $$1, float $$2) {
        List<Vec3> $$3 = this.attachments.get((Object)$$0);
        if ($$3.isEmpty()) {
            throw new IllegalStateException("Had no attachment points of type: " + String.valueOf((Object)$$0));
        }
        Vec3 $$4 = $$3.get(Mth.clamp($$1, 0, $$3.size() - 1));
        return EntityAttachments.transformPoint($$4, $$2);
    }

    private static Vec3 transformPoint(Vec3 $$0, float $$1) {
        return $$0.yRot(-$$1 * ((float)Math.PI / 180));
    }

    public static class Builder {
        private final Map<EntityAttachment, List<Vec3>> attachments = new EnumMap<EntityAttachment, List<Vec3>>(EntityAttachment.class);

        Builder() {
        }

        public Builder attach(EntityAttachment $$0, float $$1, float $$2, float $$3) {
            return this.attach($$0, new Vec3($$1, $$2, $$3));
        }

        public Builder attach(EntityAttachment $$02, Vec3 $$1) {
            this.attachments.computeIfAbsent($$02, $$0 -> new ArrayList(1)).add($$1);
            return this;
        }

        public EntityAttachments build(float $$0, float $$1) {
            Map<EntityAttachment, List<Vec3>> $$22 = Util.makeEnumMap(EntityAttachment.class, $$2 -> {
                List<Vec3> $$3 = this.attachments.get($$2);
                return $$3 == null ? $$2.createFallbackPoints($$0, $$1) : List.copyOf($$3);
            });
            return new EntityAttachments($$22);
        }
    }
}

