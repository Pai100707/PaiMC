/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.apache.commons.lang3.tuple.Triple
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.math;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.math.MatrixUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public final class Transformation {
    private final Matrix4fc matrix;
    public static final Codec<Transformation> CODEC = RecordCodecBuilder.create($$02 -> $$02.group((App)ExtraCodecs.VECTOR3F.fieldOf("translation").forGetter($$0 -> $$0.translation), (App)ExtraCodecs.QUATERNIONF.fieldOf("left_rotation").forGetter($$0 -> $$0.leftRotation), (App)ExtraCodecs.VECTOR3F.fieldOf("scale").forGetter($$0 -> $$0.scale), (App)ExtraCodecs.QUATERNIONF.fieldOf("right_rotation").forGetter($$0 -> $$0.rightRotation)).apply((Applicative)$$02, Transformation::new));
    public static final Codec<Transformation> EXTENDED_CODEC = Codec.withAlternative(CODEC, (Codec)ExtraCodecs.MATRIX4F.xmap(Transformation::new, Transformation::getMatrix));
    private boolean decomposed;
    private @Nullable Vector3fc translation;
    private @Nullable Quaternionfc leftRotation;
    private @Nullable Vector3fc scale;
    private @Nullable Quaternionfc rightRotation;
    private static final Transformation IDENTITY = Util.make(() -> {
        Transformation $$0 = new Transformation((Matrix4fc)new Matrix4f());
        $$0.translation = new Vector3f();
        $$0.leftRotation = new Quaternionf();
        $$0.scale = new Vector3f(1.0f, 1.0f, 1.0f);
        $$0.rightRotation = new Quaternionf();
        $$0.decomposed = true;
        return $$0;
    });

    public Transformation(@Nullable Matrix4fc $$0) {
        this.matrix = $$0 == null ? new Matrix4f() : $$0;
    }

    public Transformation(@Nullable Vector3fc $$0, @Nullable Quaternionfc $$1, @Nullable Vector3fc $$2, @Nullable Quaternionfc $$3) {
        this.matrix = Transformation.compose($$0, $$1, $$2, $$3);
        this.translation = $$0 != null ? $$0 : new Vector3f();
        this.leftRotation = $$1 != null ? $$1 : new Quaternionf();
        this.scale = $$2 != null ? $$2 : new Vector3f(1.0f, 1.0f, 1.0f);
        this.rightRotation = $$3 != null ? $$3 : new Quaternionf();
        this.decomposed = true;
    }

    public static Transformation identity() {
        return IDENTITY;
    }

    public Transformation compose(Transformation $$0) {
        Matrix4f $$1 = this.getMatrixCopy();
        $$1.mul($$0.getMatrix());
        return new Transformation((Matrix4fc)$$1);
    }

    public @Nullable Transformation inverse() {
        if (this == IDENTITY) {
            return this;
        }
        Matrix4f $$0 = this.getMatrixCopy().invertAffine();
        if ($$0.isFinite()) {
            return new Transformation((Matrix4fc)$$0);
        }
        return null;
    }

    private void ensureDecomposed() {
        if (!this.decomposed) {
            float $$0 = 1.0f / this.matrix.m33();
            Triple<Quaternionf, Vector3f, Quaternionf> $$1 = MatrixUtil.svdDecompose(new Matrix3f(this.matrix).scale($$0));
            this.translation = this.matrix.getTranslation(new Vector3f()).mul($$0);
            this.leftRotation = new Quaternionf((Quaternionfc)$$1.getLeft());
            this.scale = new Vector3f((Vector3fc)$$1.getMiddle());
            this.rightRotation = new Quaternionf((Quaternionfc)$$1.getRight());
            this.decomposed = true;
        }
    }

    private static Matrix4f compose(@Nullable Vector3fc $$0, @Nullable Quaternionfc $$1, @Nullable Vector3fc $$2, @Nullable Quaternionfc $$3) {
        Matrix4f $$4 = new Matrix4f();
        if ($$0 != null) {
            $$4.translation($$0);
        }
        if ($$1 != null) {
            $$4.rotate($$1);
        }
        if ($$2 != null) {
            $$4.scale($$2);
        }
        if ($$3 != null) {
            $$4.rotate($$3);
        }
        return $$4;
    }

    public Matrix4fc getMatrix() {
        return this.matrix;
    }

    public Matrix4f getMatrixCopy() {
        return new Matrix4f(this.matrix);
    }

    public Vector3fc getTranslation() {
        this.ensureDecomposed();
        return this.translation;
    }

    public Quaternionfc getLeftRotation() {
        this.ensureDecomposed();
        return this.leftRotation;
    }

    public Vector3fc getScale() {
        this.ensureDecomposed();
        return this.scale;
    }

    public Quaternionfc getRightRotation() {
        this.ensureDecomposed();
        return this.rightRotation;
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if ($$0 == null || this.getClass() != $$0.getClass()) {
            return false;
        }
        Transformation $$1 = (Transformation)$$0;
        return Objects.equals(this.matrix, $$1.matrix);
    }

    public int hashCode() {
        return Objects.hash(this.matrix);
    }

    public Transformation slerp(Transformation $$0, float $$1) {
        return new Transformation((Vector3fc)this.getTranslation().lerp($$0.getTranslation(), $$1, new Vector3f()), (Quaternionfc)this.getLeftRotation().slerp($$0.getLeftRotation(), $$1, new Quaternionf()), (Vector3fc)this.getScale().lerp($$0.getScale(), $$1, new Vector3f()), (Quaternionfc)this.getRightRotation().slerp($$0.getRightRotation(), $$1, new Quaternionf()));
    }
}

