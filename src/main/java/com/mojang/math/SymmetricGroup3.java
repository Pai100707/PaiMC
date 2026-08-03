package com.mojang.math;

import java.util.Arrays;
import net.minecraft.util.Util;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Vector3f;
import org.joml.Vector3i;

public enum SymmetricGroup3 {
   P123(0, 1, 2),
   P213(1, 0, 2),
   P132(0, 2, 1),
   P312(2, 0, 1),
   P231(1, 2, 0),
   P321(2, 1, 0);

   private final int p0;
   private final int p1;
   private final int p2;
   private final Matrix3fc transformation;
   private static final SymmetricGroup3[][] CAYLEY_TABLE = (SymmetricGroup3[][])Util.make(() -> {
      SymmetricGroup3[] $$0 = values();
      SymmetricGroup3[][] $$1 = new SymmetricGroup3[$$0.length][$$0.length];

      for (SymmetricGroup3 $$2 : $$0) {
         for (SymmetricGroup3 $$3 : $$0) {
            int $$4 = $$2.permute($$3.p0);
            int $$5 = $$2.permute($$3.p1);
            int $$6 = $$2.permute($$3.p2);
            SymmetricGroup3 $$7 = Arrays.stream($$0).filter($$3x -> $$3x.p0 == $$4 && $$3x.p1 == $$5 && $$3x.p2 == $$6).findFirst().get();
            $$1[$$2.ordinal()][$$3.ordinal()] = $$7;
         }
      }

      return $$1;
   });
   private static final SymmetricGroup3[] INVERSE_TABLE = (SymmetricGroup3[])Util.make(() -> {
      SymmetricGroup3[] $$0 = values();
      return Arrays.stream($$0).map($$0x -> Arrays.stream(values()).filter($$1 -> $$0x.compose($$1) == P123).findAny().get()).toArray(SymmetricGroup3[]::new);
   });

   private SymmetricGroup3(final int $$0, final int $$1, final int $$2) {
      this.p0 = $$0;
      this.p1 = $$1;
      this.p2 = $$2;
      this.transformation = new Matrix3f().zero().set(this.permute(0), 0, 1.0F).set(this.permute(1), 1, 1.0F).set(this.permute(2), 2, 1.0F);
   }

   public SymmetricGroup3 compose(SymmetricGroup3 $$0) {
      return CAYLEY_TABLE[this.ordinal()][$$0.ordinal()];
   }

   public SymmetricGroup3 inverse() {
      return INVERSE_TABLE[this.ordinal()];
   }

   public int permute(int $$0) {
      return switch ($$0) {
         case 0 -> this.p0;
         case 1 -> this.p1;
         case 2 -> this.p2;
         default -> throw new IllegalArgumentException("Must be 0, 1 or 2, but got " + $$0);
      };
   }

   public net.minecraft.core.Direction.Axis permuteAxis(net.minecraft.core.Direction.Axis $$0) {
      return net.minecraft.core.Direction.Axis.VALUES[this.permute($$0.ordinal())];
   }

   public Vector3f permuteVector(Vector3f $$0) {
      float $$1 = $$0.get(this.p0);
      float $$2 = $$0.get(this.p1);
      float $$3 = $$0.get(this.p2);
      return $$0.set($$1, $$2, $$3);
   }

   public Vector3i permuteVector(Vector3i $$0) {
      int $$1 = $$0.get(this.p0);
      int $$2 = $$0.get(this.p1);
      int $$3 = $$0.get(this.p2);
      return $$0.set($$1, $$2, $$3);
   }

   public Matrix3fc transformation() {
      return this.transformation;
   }
}
