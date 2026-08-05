/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.player;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;

public class Abilities {
    private static final boolean DEFAULT_INVULNERABLE = false;
    private static final boolean DEFAULY_FLYING = false;
    private static final boolean DEFAULT_MAY_FLY = false;
    private static final boolean DEFAULT_INSTABUILD = false;
    private static final boolean DEFAULT_MAY_BUILD = true;
    private static final float DEFAULT_FLYING_SPEED = 0.05f;
    private static final float DEFAULT_WALKING_SPEED = 0.1f;
    public boolean invulnerable;
    public boolean flying;
    public boolean mayfly;
    public boolean instabuild;
    public boolean mayBuild = true;
    private float flyingSpeed = 0.05f;
    private float walkingSpeed = 0.1f;

    public float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    public void setFlyingSpeed(float $$0) {
        this.flyingSpeed = $$0;
    }

    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }

    public void setWalkingSpeed(float $$0) {
        this.walkingSpeed = $$0;
    }

    public Packed pack() {
        return new Packed(this.invulnerable, this.flying, this.mayfly, this.instabuild, this.mayBuild, this.flyingSpeed, this.walkingSpeed);
    }

    public void apply(Packed $$0) {
        this.invulnerable = $$0.invulnerable;
        this.flying = $$0.flying;
        this.mayfly = $$0.mayFly;
        this.instabuild = $$0.instabuild;
        this.mayBuild = $$0.mayBuild;
        this.flyingSpeed = $$0.flyingSpeed;
        this.walkingSpeed = $$0.walkingSpeed;
    }

    public static final class Packed
    extends Record {
        final boolean invulnerable;
        final boolean flying;
        final boolean mayFly;
        final boolean instabuild;
        final boolean mayBuild;
        final float flyingSpeed;
        final float walkingSpeed;
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Codec.BOOL.fieldOf("invulnerable").orElse((Object)false).forGetter(Packed::invulnerable), (App)Codec.BOOL.fieldOf("flying").orElse((Object)false).forGetter(Packed::flying), (App)Codec.BOOL.fieldOf("mayfly").orElse((Object)false).forGetter(Packed::mayFly), (App)Codec.BOOL.fieldOf("instabuild").orElse((Object)false).forGetter(Packed::instabuild), (App)Codec.BOOL.fieldOf("mayBuild").orElse((Object)true).forGetter(Packed::mayBuild), (App)Codec.FLOAT.fieldOf("flySpeed").orElse((Object)Float.valueOf(0.05f)).forGetter(Packed::flyingSpeed), (App)Codec.FLOAT.fieldOf("walkSpeed").orElse((Object)Float.valueOf(0.1f)).forGetter(Packed::walkingSpeed)).apply((Applicative)$$0, Packed::new));

        public Packed(boolean $$0, boolean $$1, boolean $$2, boolean $$3, boolean $$4, float $$5, float $$6) {
            this.invulnerable = $$0;
            this.flying = $$1;
            this.mayFly = $$2;
            this.instabuild = $$3;
            this.mayBuild = $$4;
            this.flyingSpeed = $$5;
            this.walkingSpeed = $$6;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "invulnerable;flying;mayFly;instabuild;mayBuild;flyingSpeed;walkingSpeed", "invulnerable", "flying", "mayFly", "instabuild", "mayBuild", "flyingSpeed", "walkingSpeed"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "invulnerable;flying;mayFly;instabuild;mayBuild;flyingSpeed;walkingSpeed", "invulnerable", "flying", "mayFly", "instabuild", "mayBuild", "flyingSpeed", "walkingSpeed"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "invulnerable;flying;mayFly;instabuild;mayBuild;flyingSpeed;walkingSpeed", "invulnerable", "flying", "mayFly", "instabuild", "mayBuild", "flyingSpeed", "walkingSpeed"}, this, $$0);
        }

        public boolean invulnerable() {
            return this.invulnerable;
        }

        public boolean flying() {
            return this.flying;
        }

        public boolean mayFly() {
            return this.mayFly;
        }

        public boolean instabuild() {
            return this.instabuild;
        }

        public boolean mayBuild() {
            return this.mayBuild;
        }

        public float flyingSpeed() {
            return this.flyingSpeed;
        }

        public float walkingSpeed() {
            return this.walkingSpeed;
        }
    }
}

