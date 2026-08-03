package com.mojang.math;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Vector3i;
import org.jspecify.annotations.Nullable;

public enum OctahedralGroup implements StringRepresentable {
   IDENTITY("identity", SymmetricGroup3.P123, false, false, false),
   ROT_180_FACE_XY("rot_180_face_xy", SymmetricGroup3.P123, true, true, false),
   ROT_180_FACE_XZ("rot_180_face_xz", SymmetricGroup3.P123, true, false, true),
   ROT_180_FACE_YZ("rot_180_face_yz", SymmetricGroup3.P123, false, true, true),
   ROT_120_NNN("rot_120_nnn", SymmetricGroup3.P231, false, false, false),
   ROT_120_NNP("rot_120_nnp", SymmetricGroup3.P312, true, false, true),
   ROT_120_NPN("rot_120_npn", SymmetricGroup3.P312, false, true, true),
   ROT_120_NPP("rot_120_npp", SymmetricGroup3.P231, true, false, true),
   ROT_120_PNN("rot_120_pnn", SymmetricGroup3.P312, true, true, false),
   ROT_120_PNP("rot_120_pnp", SymmetricGroup3.P231, true, true, false),
   ROT_120_PPN("rot_120_ppn", SymmetricGroup3.P231, false, true, true),
   ROT_120_PPP("rot_120_ppp", SymmetricGroup3.P312, false, false, false),
   ROT_180_EDGE_XY_NEG("rot_180_edge_xy_neg", SymmetricGroup3.P213, true, true, true),
   ROT_180_EDGE_XY_POS("rot_180_edge_xy_pos", SymmetricGroup3.P213, false, false, true),
   ROT_180_EDGE_XZ_NEG("rot_180_edge_xz_neg", SymmetricGroup3.P321, true, true, true),
   ROT_180_EDGE_XZ_POS("rot_180_edge_xz_pos", SymmetricGroup3.P321, false, true, false),
   ROT_180_EDGE_YZ_NEG("rot_180_edge_yz_neg", SymmetricGroup3.P132, true, true, true),
   ROT_180_EDGE_YZ_POS("rot_180_edge_yz_pos", SymmetricGroup3.P132, true, false, false),
   ROT_90_X_NEG("rot_90_x_neg", SymmetricGroup3.P132, false, false, true),
   ROT_90_X_POS("rot_90_x_pos", SymmetricGroup3.P132, false, true, false),
   ROT_90_Y_NEG("rot_90_y_neg", SymmetricGroup3.P321, true, false, false),
   ROT_90_Y_POS("rot_90_y_pos", SymmetricGroup3.P321, false, false, true),
   ROT_90_Z_NEG("rot_90_z_neg", SymmetricGroup3.P213, false, true, false),
   ROT_90_Z_POS("rot_90_z_pos", SymmetricGroup3.P213, true, false, false),
   INVERSION("inversion", SymmetricGroup3.P123, true, true, true),
   INVERT_X("invert_x", SymmetricGroup3.P123, true, false, false),
   INVERT_Y("invert_y", SymmetricGroup3.P123, false, true, false),
   INVERT_Z("invert_z", SymmetricGroup3.P123, false, false, true),
   ROT_60_REF_NNN("rot_60_ref_nnn", SymmetricGroup3.P312, true, true, true),
   ROT_60_REF_NNP("rot_60_ref_nnp", SymmetricGroup3.P231, true, false, false),
   ROT_60_REF_NPN("rot_60_ref_npn", SymmetricGroup3.P231, false, false, true),
   ROT_60_REF_NPP("rot_60_ref_npp", SymmetricGroup3.P312, false, false, true),
   ROT_60_REF_PNN("rot_60_ref_pnn", SymmetricGroup3.P231, false, true, false),
   ROT_60_REF_PNP("rot_60_ref_pnp", SymmetricGroup3.P312, true, false, false),
   ROT_60_REF_PPN("rot_60_ref_ppn", SymmetricGroup3.P312, false, true, false),
   ROT_60_REF_PPP("rot_60_ref_ppp", SymmetricGroup3.P231, true, true, true),
   SWAP_XY("swap_xy", SymmetricGroup3.P213, false, false, false),
   SWAP_YZ("swap_yz", SymmetricGroup3.P132, false, false, false),
   SWAP_XZ("swap_xz", SymmetricGroup3.P321, false, false, false),
   SWAP_NEG_XY("swap_neg_xy", SymmetricGroup3.P213, true, true, false),
   SWAP_NEG_YZ("swap_neg_yz", SymmetricGroup3.P132, false, true, true),
   SWAP_NEG_XZ("swap_neg_xz", SymmetricGroup3.P321, true, false, true),
   ROT_90_REF_X_NEG("rot_90_ref_x_neg", SymmetricGroup3.P132, true, false, true),
   ROT_90_REF_X_POS("rot_90_ref_x_pos", SymmetricGroup3.P132, true, true, false),
   ROT_90_REF_Y_NEG("rot_90_ref_y_neg", SymmetricGroup3.P321, true, true, false),
   ROT_90_REF_Y_POS("rot_90_ref_y_pos", SymmetricGroup3.P321, false, true, true),
   ROT_90_REF_Z_NEG("rot_90_ref_z_neg", SymmetricGroup3.P213, false, true, true),
   ROT_90_REF_Z_POS("rot_90_ref_z_pos", SymmetricGroup3.P213, true, false, true);

   public static final OctahedralGroup BLOCK_ROT_X_270 = ROT_90_X_POS;
   public static final OctahedralGroup BLOCK_ROT_X_180 = ROT_180_FACE_YZ;
   public static final OctahedralGroup BLOCK_ROT_X_90 = ROT_90_X_NEG;
   public static final OctahedralGroup BLOCK_ROT_Y_270 = ROT_90_Y_POS;
   public static final OctahedralGroup BLOCK_ROT_Y_180 = ROT_180_FACE_XZ;
   public static final OctahedralGroup BLOCK_ROT_Y_90 = ROT_90_Y_NEG;
   public static final OctahedralGroup BLOCK_ROT_Z_270 = ROT_90_Z_POS;
   public static final OctahedralGroup BLOCK_ROT_Z_180 = ROT_180_FACE_XY;
   public static final OctahedralGroup BLOCK_ROT_Z_90 = ROT_90_Z_NEG;
   private final Matrix3fc transformation;
   private final String name;
   @Nullable
   private Map<Direction, Direction> rotatedDirections;
   private final boolean invertX;
   private final boolean invertY;
   private final boolean invertZ;
   private final SymmetricGroup3 permutation;
   private static final OctahedralGroup[][] CAYLEY_TABLE = (OctahedralGroup[][])Util.make(() -> {
      OctahedralGroup[] $$0 = values();
      OctahedralGroup[][] $$1 = new OctahedralGroup[$$0.length][$$0.length];
      Map<Integer, OctahedralGroup> $$2 = Arrays.stream($$0).collect(Collectors.toMap(OctahedralGroup::trace, $$0x -> $$0x));

      for (OctahedralGroup $$3 : $$0) {
         for (OctahedralGroup $$4 : $$0) {
            SymmetricGroup3 $$5 = $$4.permutation.compose($$3.permutation);
            boolean $$6 = $$3.inverts(net.minecraft.core.Direction.Axis.X) ^ $$4.inverts($$3.permutation.permuteAxis(net.minecraft.core.Direction.Axis.X));
            boolean $$7 = $$3.inverts(net.minecraft.core.Direction.Axis.Y) ^ $$4.inverts($$3.permutation.permuteAxis(net.minecraft.core.Direction.Axis.Y));
            boolean $$8 = $$3.inverts(net.minecraft.core.Direction.Axis.Z) ^ $$4.inverts($$3.permutation.permuteAxis(net.minecraft.core.Direction.Axis.Z));
            $$1[$$3.ordinal()][$$4.ordinal()] = $$2.get(trace($$6, $$7, $$8, $$5));
         }
      }

      return $$1;
   });
   private static final OctahedralGroup[] INVERSE_TABLE = Arrays.stream(values())
      .map($$0 -> Arrays.stream(values()).filter($$1 -> $$0.compose($$1) == IDENTITY).findAny().get())
      .toArray(OctahedralGroup[]::new);

   private OctahedralGroup(final String $$0, final SymmetricGroup3 $$1, final boolean $$2, final boolean $$3, final boolean $$4) {
      this.name = $$0;
      this.invertX = $$2;
      this.invertY = $$3;
      this.invertZ = $$4;
      this.permutation = $$1;
      this.transformation = new Matrix3f().scaling($$2 ? -1.0F : 1.0F, $$3 ? -1.0F : 1.0F, $$4 ? -1.0F : 1.0F).mul($$1.transformation());
   }

   private static int trace(boolean $$0, boolean $$1, boolean $$2, SymmetricGroup3 $$3) {
      int $$4 = ($$2 ? 4 : 0) + ($$1 ? 2 : 0) + ($$0 ? 1 : 0);
      return $$3.ordinal() << 3 | $$4;
   }

   private int trace() {
      return trace(this.invertX, this.invertY, this.invertZ, this.permutation);
   }

   public OctahedralGroup compose(OctahedralGroup $$0) {
      return CAYLEY_TABLE[this.ordinal()][$$0.ordinal()];
   }

   public OctahedralGroup inverse() {
      return INVERSE_TABLE[this.ordinal()];
   }

   public Matrix3fc transformation() {
      return this.transformation;
   }

   @Override
   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }

   public Direction rotate(Direction $$0) {
      if (this.rotatedDirections == null) {
         this.rotatedDirections = Util.makeEnumMap(Direction.class, $$0x -> {
            net.minecraft.core.Direction.Axis $$1 = $$0x.getAxis();
            AxisDirection $$2 = $$0x.getAxisDirection();
            net.minecraft.core.Direction.Axis $$3 = this.permutation.inverse().permuteAxis($$1);
            AxisDirection $$4 = this.inverts($$3) ? $$2.opposite() : $$2;
            return Direction.fromAxisAndDirection($$3, $$4);
         });
      }

      return this.rotatedDirections.get($$0);
   }

   public Vector3i rotate(Vector3i $$0) {
      this.permutation.permuteVector($$0);
      $$0.x = $$0.x * (this.invertX ? -1 : 1);
      $$0.y = $$0.y * (this.invertY ? -1 : 1);
      $$0.z = $$0.z * (this.invertZ ? -1 : 1);
      return $$0;
   }

   public boolean inverts(net.minecraft.core.Direction.Axis $$0) {
      return switch ($$0) {
         case X -> this.invertX;
         case Y -> this.invertY;
         case Z -> this.invertZ;
         default -> throw new MatchException(null, null);
      };
   }

   public SymmetricGroup3 permutation() {
      return this.permutation;
   }

   public FrontAndTop rotate(FrontAndTop $$0) {
      return FrontAndTop.fromFrontAndTop(this.rotate($$0.front()), this.rotate($$0.top()));
   }
}
