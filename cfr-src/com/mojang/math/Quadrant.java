/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParseException
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package com.mojang.math;

import com.google.gson.JsonParseException;
import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.Mth;

public enum Quadrant {
    R0(0, OctahedralGroup.IDENTITY, OctahedralGroup.IDENTITY, OctahedralGroup.IDENTITY),
    R90(1, OctahedralGroup.BLOCK_ROT_X_90, OctahedralGroup.BLOCK_ROT_Y_90, OctahedralGroup.BLOCK_ROT_Z_90),
    R180(2, OctahedralGroup.BLOCK_ROT_X_180, OctahedralGroup.BLOCK_ROT_Y_180, OctahedralGroup.BLOCK_ROT_Z_180),
    R270(3, OctahedralGroup.BLOCK_ROT_X_270, OctahedralGroup.BLOCK_ROT_Y_270, OctahedralGroup.BLOCK_ROT_Z_270);

    public static final Codec<Quadrant> CODEC;
    public final int shift;
    public final OctahedralGroup rotationX;
    public final OctahedralGroup rotationY;
    public final OctahedralGroup rotationZ;

    private Quadrant(int $$0, OctahedralGroup $$1, OctahedralGroup $$2, OctahedralGroup $$3) {
        this.shift = $$0;
        this.rotationX = $$1;
        this.rotationY = $$2;
        this.rotationZ = $$3;
    }

    @Deprecated
    public static Quadrant parseJson(int $$0) {
        return switch (Mth.positiveModulo($$0, 360)) {
            case 0 -> R0;
            case 90 -> R90;
            case 180 -> R180;
            case 270 -> R270;
            default -> throw new JsonParseException("Invalid rotation " + $$0 + " found, only 0/90/180/270 allowed");
        };
    }

    public static OctahedralGroup fromXYAngles(Quadrant $$0, Quadrant $$1) {
        return $$1.rotationY.compose($$0.rotationX);
    }

    public static OctahedralGroup fromXYZAngles(Quadrant $$0, Quadrant $$1, Quadrant $$2) {
        return $$2.rotationZ.compose($$1.rotationY.compose($$0.rotationX));
    }

    public int rotateVertexIndex(int $$0) {
        return ($$0 + this.shift) % 4;
    }

    static {
        CODEC = Codec.INT.comapFlatMap($$0 -> switch (Mth.positiveModulo($$0, 360)) {
            case 0 -> DataResult.success((Object)((Object)R0));
            case 90 -> DataResult.success((Object)((Object)R90));
            case 180 -> DataResult.success((Object)((Object)R180));
            case 270 -> DataResult.success((Object)((Object)R270));
            default -> DataResult.error(() -> "Invalid rotation " + $$0 + " found, only 0/90/180/270 allowed");
        }, $$0 -> switch ($$0.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> 0;
            case 1 -> 90;
            case 2 -> 180;
            case 3 -> 270;
        });
    }
}

