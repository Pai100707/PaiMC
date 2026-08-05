/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.tuple.Triple
 *  org.joml.Math
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package com.mojang.math;

import com.mojang.math.GivensParameters;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class MatrixUtil {
    private static final float G = 3.0f + 2.0f * Math.sqrt((float)2.0f);
    private static final GivensParameters PI_4 = GivensParameters.fromPositiveAngle(0.7853982f);

    private MatrixUtil() {
    }

    public static Matrix4f mulComponentWise(Matrix4f $$0, float $$1) {
        return $$0.set($$0.m00() * $$1, $$0.m01() * $$1, $$0.m02() * $$1, $$0.m03() * $$1, $$0.m10() * $$1, $$0.m11() * $$1, $$0.m12() * $$1, $$0.m13() * $$1, $$0.m20() * $$1, $$0.m21() * $$1, $$0.m22() * $$1, $$0.m23() * $$1, $$0.m30() * $$1, $$0.m31() * $$1, $$0.m32() * $$1, $$0.m33() * $$1);
    }

    private static GivensParameters approxGivensQuat(float $$0, float $$1, float $$2) {
        float $$4 = $$1;
        float $$3 = 2.0f * ($$0 - $$2);
        if (G * $$4 * $$4 < $$3 * $$3) {
            return GivensParameters.fromUnnormalized($$4, $$3);
        }
        return PI_4;
    }

    private static GivensParameters qrGivensQuat(float $$0, float $$1) {
        float $$2 = (float)java.lang.Math.hypot($$0, $$1);
        float $$3 = $$2 > 1.0E-6f ? $$1 : 0.0f;
        float $$4 = Math.abs((float)$$0) + Math.max((float)$$2, (float)1.0E-6f);
        if ($$0 < 0.0f) {
            float $$5 = $$3;
            $$3 = $$4;
            $$4 = $$5;
        }
        return GivensParameters.fromUnnormalized($$3, $$4);
    }

    private static void similarityTransform(Matrix3f $$0, Matrix3f $$1) {
        $$0.mul((Matrix3fc)$$1);
        $$1.transpose();
        $$1.mul((Matrix3fc)$$0);
        $$0.set((Matrix3fc)$$1);
    }

    private static void stepJacobi(Matrix3f $$0, Matrix3f $$1, Quaternionf $$2, Quaternionf $$3) {
        if ($$0.m01 * $$0.m01 + $$0.m10 * $$0.m10 > 1.0E-6f) {
            GivensParameters $$4 = MatrixUtil.approxGivensQuat($$0.m00, 0.5f * ($$0.m01 + $$0.m10), $$0.m11);
            Quaternionf $$5 = $$4.aroundZ($$2);
            $$3.mul((Quaternionfc)$$5);
            $$4.aroundZ($$1);
            MatrixUtil.similarityTransform($$0, $$1);
        }
        if ($$0.m02 * $$0.m02 + $$0.m20 * $$0.m20 > 1.0E-6f) {
            GivensParameters $$6 = MatrixUtil.approxGivensQuat($$0.m00, 0.5f * ($$0.m02 + $$0.m20), $$0.m22).inverse();
            Quaternionf $$7 = $$6.aroundY($$2);
            $$3.mul((Quaternionfc)$$7);
            $$6.aroundY($$1);
            MatrixUtil.similarityTransform($$0, $$1);
        }
        if ($$0.m12 * $$0.m12 + $$0.m21 * $$0.m21 > 1.0E-6f) {
            GivensParameters $$8 = MatrixUtil.approxGivensQuat($$0.m11, 0.5f * ($$0.m12 + $$0.m21), $$0.m22);
            Quaternionf $$9 = $$8.aroundX($$2);
            $$3.mul((Quaternionfc)$$9);
            $$8.aroundX($$1);
            MatrixUtil.similarityTransform($$0, $$1);
        }
    }

    public static Quaternionf eigenvalueJacobi(Matrix3f $$0, int $$1) {
        Quaternionf $$2 = new Quaternionf();
        Matrix3f $$3 = new Matrix3f();
        Quaternionf $$4 = new Quaternionf();
        for (int $$5 = 0; $$5 < $$1; ++$$5) {
            MatrixUtil.stepJacobi($$0, $$3, $$4, $$2);
        }
        $$2.normalize();
        return $$2;
    }

    public static Triple<Quaternionf, Vector3f, Quaternionf> svdDecompose(Matrix3f $$0) {
        GivensParameters $$12;
        Matrix3f $$1 = new Matrix3f((Matrix3fc)$$0);
        $$1.transpose();
        $$1.mul((Matrix3fc)$$0);
        Quaternionf $$2 = MatrixUtil.eigenvalueJacobi($$1, 5);
        float $$3 = $$1.m00;
        float $$4 = $$1.m11;
        boolean $$5 = (double)$$3 < 1.0E-6;
        boolean $$6 = (double)$$4 < 1.0E-6;
        Matrix3f $$7 = $$1;
        Matrix3f $$8 = $$0.rotate((Quaternionfc)$$2);
        Quaternionf $$9 = new Quaternionf();
        Quaternionf $$10 = new Quaternionf();
        if ($$5) {
            GivensParameters $$11 = MatrixUtil.qrGivensQuat($$8.m11, -$$8.m10);
        } else {
            $$12 = MatrixUtil.qrGivensQuat($$8.m00, $$8.m01);
        }
        Quaternionf $$13 = $$12.aroundZ($$10);
        Matrix3f $$14 = $$12.aroundZ($$7);
        $$9.mul((Quaternionfc)$$13);
        $$14.transpose().mul((Matrix3fc)$$8);
        $$7 = $$8;
        $$12 = $$5 ? MatrixUtil.qrGivensQuat($$14.m22, -$$14.m20) : MatrixUtil.qrGivensQuat($$14.m00, $$14.m02);
        $$12 = $$12.inverse();
        Quaternionf $$15 = $$12.aroundY($$10);
        Matrix3f $$16 = $$12.aroundY($$7);
        $$9.mul((Quaternionfc)$$15);
        $$16.transpose().mul((Matrix3fc)$$14);
        $$7 = $$14;
        $$12 = $$6 ? MatrixUtil.qrGivensQuat($$16.m22, -$$16.m21) : MatrixUtil.qrGivensQuat($$16.m11, $$16.m12);
        Quaternionf $$17 = $$12.aroundX($$10);
        Matrix3f $$18 = $$12.aroundX($$7);
        $$9.mul((Quaternionfc)$$17);
        $$18.transpose().mul((Matrix3fc)$$16);
        Vector3f $$19 = new Vector3f($$18.m00, $$18.m11, $$18.m22);
        return Triple.of((Object)$$9, (Object)$$19, (Object)$$2.conjugate());
    }

    private static boolean checkPropertyRaw(Matrix4fc $$0, int $$1) {
        return ($$0.properties() & $$1) != 0;
    }

    public static boolean checkProperty(Matrix4fc $$0, int $$1) {
        if (MatrixUtil.checkPropertyRaw($$0, $$1)) {
            return true;
        }
        if ($$0 instanceof Matrix4f) {
            Matrix4f $$2 = (Matrix4f)$$0;
            $$2.determineProperties();
            return MatrixUtil.checkPropertyRaw($$0, $$1);
        }
        return false;
    }

    public static boolean isIdentity(Matrix4fc $$0) {
        return MatrixUtil.checkProperty($$0, 4);
    }

    public static boolean isPureTranslation(Matrix4fc $$0) {
        return MatrixUtil.checkProperty($$0, 8);
    }

    public static boolean isOrthonormal(Matrix4fc $$0) {
        return MatrixUtil.checkProperty($$0, 16);
    }
}

