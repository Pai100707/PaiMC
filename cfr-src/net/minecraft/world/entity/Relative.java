/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.entity;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum Relative {
    X(0),
    Y(1),
    Z(2),
    Y_ROT(3),
    X_ROT(4),
    DELTA_X(5),
    DELTA_Y(6),
    DELTA_Z(7),
    ROTATE_DELTA(8);

    public static final Set<Relative> ALL;
    public static final Set<Relative> ROTATION;
    public static final Set<Relative> DELTA;
    public static final StreamCodec<ByteBuf, Set<Relative>> SET_STREAM_CODEC;
    private final int bit;

    @SafeVarargs
    public static Set<Relative> union(Set<Relative> ... $$0) {
        HashSet<Relative> $$1 = new HashSet<Relative>();
        for (Set<Relative> $$2 : $$0) {
            $$1.addAll($$2);
        }
        return $$1;
    }

    public static Set<Relative> rotation(boolean $$0, boolean $$1) {
        EnumSet<Relative> $$2 = EnumSet.noneOf(Relative.class);
        if ($$0) {
            $$2.add(Y_ROT);
        }
        if ($$1) {
            $$2.add(X_ROT);
        }
        return $$2;
    }

    public static Set<Relative> position(boolean $$0, boolean $$1, boolean $$2) {
        EnumSet<Relative> $$3 = EnumSet.noneOf(Relative.class);
        if ($$0) {
            $$3.add(X);
        }
        if ($$1) {
            $$3.add(Y);
        }
        if ($$2) {
            $$3.add(Z);
        }
        return $$3;
    }

    public static Set<Relative> direction(boolean $$0, boolean $$1, boolean $$2) {
        EnumSet<Relative> $$3 = EnumSet.noneOf(Relative.class);
        if ($$0) {
            $$3.add(DELTA_X);
        }
        if ($$1) {
            $$3.add(DELTA_Y);
        }
        if ($$2) {
            $$3.add(DELTA_Z);
        }
        return $$3;
    }

    private Relative(int $$0) {
        this.bit = $$0;
    }

    private int getMask() {
        return 1 << this.bit;
    }

    private boolean isSet(int $$0) {
        return ($$0 & this.getMask()) == this.getMask();
    }

    public static Set<Relative> unpack(int $$0) {
        EnumSet<Relative> $$1 = EnumSet.noneOf(Relative.class);
        for (Relative $$2 : Relative.values()) {
            if (!$$2.isSet($$0)) continue;
            $$1.add($$2);
        }
        return $$1;
    }

    public static int pack(Set<Relative> $$0) {
        int $$1 = 0;
        for (Relative $$2 : $$0) {
            $$1 |= $$2.getMask();
        }
        return $$1;
    }

    static {
        ALL = Set.of(Relative.values());
        ROTATION = Set.of(X_ROT, Y_ROT);
        DELTA = Set.of(DELTA_X, DELTA_Y, DELTA_Z, ROTATE_DELTA);
        SET_STREAM_CODEC = ByteBufCodecs.INT.map(Relative::unpack, Relative::pack);
    }
}

