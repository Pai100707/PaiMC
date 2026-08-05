/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.minecraft.core;

import com.google.common.collect.Maps;
import com.mojang.math.MatrixUtil;
import com.mojang.math.Transformation;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class BlockMath {
    private static final Map<Direction, Transformation> VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL = Maps.newEnumMap(Map.of(Direction.SOUTH, Transformation.identity(), Direction.EAST, new Transformation(null, (Quaternionfc)new Quaternionf().rotateY(1.5707964f), null, null), Direction.WEST, new Transformation(null, (Quaternionfc)new Quaternionf().rotateY(-1.5707964f), null, null), Direction.NORTH, new Transformation(null, (Quaternionfc)new Quaternionf().rotateY((float)Math.PI), null, null), Direction.UP, new Transformation(null, (Quaternionfc)new Quaternionf().rotateX(-1.5707964f), null, null), Direction.DOWN, new Transformation(null, (Quaternionfc)new Quaternionf().rotateX(1.5707964f), null, null)));
    private static final Map<Direction, Transformation> VANILLA_UV_TRANSFORM_GLOBAL_TO_LOCAL = Maps.newEnumMap(Util.mapValues(VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL, Transformation::inverse));

    public static Transformation blockCenterToCorner(Transformation $$0) {
        Matrix4f $$1 = new Matrix4f().translation(0.5f, 0.5f, 0.5f);
        $$1.mul($$0.getMatrix());
        $$1.translate(-0.5f, -0.5f, -0.5f);
        return new Transformation((Matrix4fc)$$1);
    }

    public static Transformation blockCornerToCenter(Transformation $$0) {
        Matrix4f $$1 = new Matrix4f().translation(-0.5f, -0.5f, -0.5f);
        $$1.mul($$0.getMatrix());
        $$1.translate(0.5f, 0.5f, 0.5f);
        return new Transformation((Matrix4fc)$$1);
    }

    public static Transformation getFaceTransformation(Transformation $$0, Direction $$1) {
        if (MatrixUtil.isIdentity($$0.getMatrix())) {
            return $$0;
        }
        Transformation $$2 = VANILLA_UV_TRANSFORM_LOCAL_TO_GLOBAL.get($$1);
        $$2 = $$0.compose($$2);
        Vector3f $$3 = $$2.getMatrix().transformDirection(new Vector3f(0.0f, 0.0f, 1.0f));
        Direction $$4 = Direction.getApproximateNearest($$3.x, $$3.y, $$3.z);
        return VANILLA_UV_TRANSFORM_GLOBAL_TO_LOCAL.get($$4).compose($$2);
    }
}

