package com.mojang.math;

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

public final class Transformation {
   private final Matrix4fc matrix;
   public static final Codec<Transformation> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ExtraCodecs.VECTOR3F.fieldOf("translation").forGetter($$0x -> $$0x.translation),
            ExtraCodecs.QUATERNIONF.fieldOf("left_rotation").forGetter($$0x -> $$0x.leftRotation),
            ExtraCodecs.VECTOR3F.fieldOf("scale").forGetter($$0x -> $$0x.scale),
            ExtraCodecs.QUATERNIONF.fieldOf("right_rotation").forGetter($$0x -> $$0x.rightRotation)
         )
         .apply($$0, Transformation::new)
   );
   public static final Codec<Transformation> EXTENDED_CODEC = Codec.withAlternative(
      CODEC, ExtraCodecs.MATRIX4F.xmap(Transformation::new, Transformation::getMatrix)
   );
   private boolean decomposed;
   
   private Vector3fc translation;
   
   private Quaternionfc leftRotation;
   
   private Vector3fc scale;
   
   private Quaternionfc rightRotation;
   private static final Transformation IDENTITY = (Transformation)Util.make(() -> {
      Transformation $$0 = new Transformation(new Matrix4f());
      $$0.translation = new Vector3f();
      $$0.leftRotation = new Quaternionf();
      $$0.scale = new Vector3f(1.0F, 1.0F, 1.0F);
      $$0.rightRotation = new Quaternionf();
      $$0.decomposed = true;
      return $$0;
   });

   public Transformation(Matrix4fc $$0) {
      if ($$0 == null) {
         this.matrix = new Matrix4f();
      } else {
         this.matrix = $$0;
      }
   }

   public Transformation(Vector3fc $$0, Quaternionfc $$1, Vector3fc $$2, Quaternionfc $$3) {
      this.matrix = compose($$0, $$1, $$2, $$3);
      this.translation = (Vector3fc)($$0 != null ? $$0 : new Vector3f());
      this.leftRotation = (Quaternionfc)($$1 != null ? $$1 : new Quaternionf());
      this.scale = (Vector3fc)($$2 != null ? $$2 : new Vector3f(1.0F, 1.0F, 1.0F));
      this.rightRotation = (Quaternionfc)($$3 != null ? $$3 : new Quaternionf());
      this.decomposed = true;
   }

   public static Transformation identity() {
      return IDENTITY;
   }

   public Transformation compose(Transformation $$0) {
      Matrix4f $$1 = this.getMatrixCopy();
      $$1.mul($$0.getMatrix());
      return new Transformation($$1);
   }

   
   public Transformation inverse() {
      if (this == IDENTITY) {
         return this;
      } else {
         Matrix4f $$0 = this.getMatrixCopy().invertAffine();
         return $$0.isFinite() ? new Transformation($$0) : null;
      }
   }

   private void ensureDecomposed() {
      if (!this.decomposed) {
         float $$0 = 1.0F / this.matrix.m33();
         Triple<Quaternionf, Vector3f, Quaternionf> $$1 = MatrixUtil.svdDecompose(new Matrix3f(this.matrix).scale($$0));
         this.translation = this.matrix.getTranslation(new Vector3f()).mul($$0);
         this.leftRotation = new Quaternionf((Quaternionfc)$$1.getLeft());
         this.scale = new Vector3f((Vector3fc)$$1.getMiddle());
         this.rightRotation = new Quaternionf((Quaternionfc)$$1.getRight());
         this.decomposed = true;
      }
   }

   private static Matrix4f compose(Vector3fc $$0, Quaternionfc $$1, Vector3fc $$2, Quaternionfc $$3) {
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

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else if ($$0 != null && this.getClass() == $$0.getClass()) {
         Transformation $$1 = (Transformation)$$0;
         return Objects.equals(this.matrix, $$1.matrix);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.matrix);
   }

   public Transformation slerp(Transformation $$0, float $$1) {
      return new Transformation(
         this.getTranslation().lerp($$0.getTranslation(), $$1, new Vector3f()),
         this.getLeftRotation().slerp($$0.getLeftRotation(), $$1, new Quaternionf()),
         this.getScale().lerp($$0.getScale(), $$1, new Vector3f()),
         this.getRightRotation().slerp($$0.getRightRotation(), $$1, new Quaternionf())
      );
   }
}
