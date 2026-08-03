package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public enum Mirror implements StringRepresentable {
   NONE("none", OctahedralGroup.IDENTITY),
   LEFT_RIGHT("left_right", OctahedralGroup.INVERT_Z),
   FRONT_BACK("front_back", OctahedralGroup.INVERT_X);

   public static final Codec<Mirror> CODEC = StringRepresentable.fromEnum(Mirror::values);
   @Deprecated
   public static final Codec<Mirror> LEGACY_CODEC = ExtraCodecs.legacyEnum(Mirror::valueOf);
   private final String id;
   private final Component symbol;
   private final OctahedralGroup rotation;

   private Mirror(final String $$0, final OctahedralGroup $$1) {
      this.id = $$0;
      this.symbol = Component.translatable("mirror." + $$0);
      this.rotation = $$1;
   }

   public int mirror(int $$0, int $$1) {
      int $$2 = $$1 / 2;
      int $$3 = $$0 > $$2 ? $$0 - $$1 : $$0;
      switch (this) {
         case LEFT_RIGHT:
            return ($$2 - $$3 + $$1) % $$1;
         case FRONT_BACK:
            return ($$1 - $$3) % $$1;
         default:
            return $$0;
      }
   }

   public Rotation getRotation(Direction $$0) {
      Axis $$1 = $$0.getAxis();
      return (this != LEFT_RIGHT || $$1 != Axis.Z) && (this != FRONT_BACK || $$1 != Axis.X) ? Rotation.NONE : Rotation.CLOCKWISE_180;
   }

   public Direction mirror(Direction $$0) {
      if (this == FRONT_BACK && $$0.getAxis() == Axis.X) {
         return $$0.getOpposite();
      } else {
         return this == LEFT_RIGHT && $$0.getAxis() == Axis.Z ? $$0.getOpposite() : $$0;
      }
   }

   public OctahedralGroup rotation() {
      return this.rotation;
   }

   public Component symbol() {
      return this.symbol;
   }

   public String getSerializedName() {
      return this.id;
   }
}
