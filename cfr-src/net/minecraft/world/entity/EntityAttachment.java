/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.List;
import net.minecraft.world.phys.Vec3;

public enum EntityAttachment {
    PASSENGER(Fallback.AT_HEIGHT),
    VEHICLE(Fallback.AT_FEET),
    NAME_TAG(Fallback.AT_HEIGHT),
    WARDEN_CHEST(Fallback.AT_CENTER);

    private final Fallback fallback;

    private EntityAttachment(Fallback $$0) {
        this.fallback = $$0;
    }

    public List<Vec3> createFallbackPoints(float $$0, float $$1) {
        return this.fallback.create($$0, $$1);
    }

    public static interface Fallback {
        public static final List<Vec3> ZERO = List.of(Vec3.ZERO);
        public static final Fallback AT_FEET = ($$0, $$1) -> ZERO;
        public static final Fallback AT_HEIGHT = ($$0, $$1) -> List.of(new Vec3(0.0, $$1, 0.0));
        public static final Fallback AT_CENTER = ($$0, $$1) -> List.of(new Vec3(0.0, (double)$$1 / 2.0, 0.0));

        public List<Vec3> create(float var1, float var2);
    }
}

