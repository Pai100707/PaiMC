package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;

public enum Rotation implements StringRepresentable {
   NONE(0, "none", OctahedralGroup.IDENTITY),
   CLOCKWISE_90(1, "clockwise_90", OctahedralGroup.ROT_90_Y_NEG),
   CLOCKWISE_180(2, "180", OctahedralGroup.ROT_180_FACE_XZ),
   COUNTERCLOCKWISE_90(3, "counterclockwise_90", OctahedralGroup.ROT_90_Y_POS);

   public static final IntFunction<Rotation> BY_ID = ByIdMap.continuous(Rotation::getIndex, values(), OutOfBoundsStrategy.WRAP);
   public static final Codec<Rotation> CODEC = StringRepresentable.fromEnum(Rotation::values);
   public static final StreamCodec<ByteBuf, Rotation> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Rotation::getIndex);
   @Deprecated
   public static final Codec<Rotation> LEGACY_CODEC = ExtraCodecs.legacyEnum(Rotation::valueOf);
   private final int index;
   private final String id;
   private final OctahedralGroup rotation;

   private Rotation(final int $$0, final String $$1, final OctahedralGroup $$2) {
      this.index = $$0;
      this.id = $$1;
      this.rotation = $$2;
   }

   public Rotation getRotated(Rotation $$0) {
      return switch ($$0) {
         case CLOCKWISE_90 -> {
            switch (this) {
               case NONE:
                  yield CLOCKWISE_90;
               case CLOCKWISE_90:
                  yield CLOCKWISE_180;
               case CLOCKWISE_180:
                  yield COUNTERCLOCKWISE_90;
               case COUNTERCLOCKWISE_90:
                  yield NONE;
               default:
                  throw new MatchException(null, null);
            }
         }
         case CLOCKWISE_180 -> {
            switch (this) {
               case NONE:
                  yield CLOCKWISE_180;
               case CLOCKWISE_90:
                  yield COUNTERCLOCKWISE_90;
               case CLOCKWISE_180:
                  yield NONE;
               case COUNTERCLOCKWISE_90:
                  yield CLOCKWISE_90;
               default:
                  throw new MatchException(null, null);
            }
         }
         case COUNTERCLOCKWISE_90 -> {
            switch (this) {
               case NONE:
                  yield COUNTERCLOCKWISE_90;
               case CLOCKWISE_90:
                  yield NONE;
               case CLOCKWISE_180:
                  yield CLOCKWISE_90;
               case COUNTERCLOCKWISE_90:
                  yield CLOCKWISE_180;
               default:
                  throw new MatchException(null, null);
            }
         }
         default -> this;
      };
   }

   public OctahedralGroup rotation() {
      return this.rotation;
   }

   public Direction rotate(Direction $$0) {
      if ($$0.getAxis() == Axis.Y) {
         return $$0;
      } else {
         return switch (this) {
            case CLOCKWISE_90 -> $$0.getClockWise();
            case CLOCKWISE_180 -> $$0.getOpposite();
            case COUNTERCLOCKWISE_90 -> $$0.getCounterClockWise();
            default -> $$0;
         };
      }
   }

   public int rotate(int $$0, int $$1) {
      return switch (this) {
         case CLOCKWISE_90 -> ($$0 + $$1 / 4) % $$1;
         case CLOCKWISE_180 -> ($$0 + $$1 / 2) % $$1;
         case COUNTERCLOCKWISE_90 -> ($$0 + $$1 * 3 / 4) % $$1;
         default -> $$0;
      };
   }

   public static Rotation getRandom(RandomSource $$0) {
      return (Rotation)Util.getRandom(values(), $$0);
   }

   public static List<Rotation> getShuffled(RandomSource $$0) {
      return Util.shuffledCopy(values(), $$0);
   }

   public String getSerializedName() {
      return this.id;
   }

   private int getIndex() {
      return this.index;
   }
}
