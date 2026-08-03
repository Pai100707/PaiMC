package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.world.level.dimension.DimensionType;

public interface VerticalAnchor {
   Codec<VerticalAnchor> CODEC = Codec.xor(VerticalAnchor.Absolute.CODEC, Codec.xor(VerticalAnchor.AboveBottom.CODEC, VerticalAnchor.BelowTop.CODEC))
      .xmap(VerticalAnchor::merge, VerticalAnchor::split);
   VerticalAnchor BOTTOM = aboveBottom(0);
   VerticalAnchor TOP = belowTop(0);

   static VerticalAnchor absolute(int $$0) {
      return new VerticalAnchor.Absolute($$0);
   }

   static VerticalAnchor aboveBottom(int $$0) {
      return new VerticalAnchor.AboveBottom($$0);
   }

   static VerticalAnchor belowTop(int $$0) {
      return new VerticalAnchor.BelowTop($$0);
   }

   static VerticalAnchor bottom() {
      return BOTTOM;
   }

   static VerticalAnchor top() {
      return TOP;
   }

   private static VerticalAnchor merge(Either<VerticalAnchor.Absolute, Either<VerticalAnchor.AboveBottom, VerticalAnchor.BelowTop>> $$0) {
      return (VerticalAnchor)$$0.map(Function.identity(), Either::unwrap);
   }

   private static Either<VerticalAnchor.Absolute, Either<VerticalAnchor.AboveBottom, VerticalAnchor.BelowTop>> split(VerticalAnchor $$0) {
      return $$0 instanceof VerticalAnchor.Absolute
         ? Either.left((VerticalAnchor.Absolute)$$0)
         : Either.right($$0 instanceof VerticalAnchor.AboveBottom ? Either.left((VerticalAnchor.AboveBottom)$$0) : Either.right((VerticalAnchor.BelowTop)$$0));
   }

   int resolveY(WorldGenerationContext var1);

   public record AboveBottom(int offset) implements VerticalAnchor {
      public static final Codec<VerticalAnchor.AboveBottom> CODEC = Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)
         .fieldOf("above_bottom")
         .xmap(VerticalAnchor.AboveBottom::new, VerticalAnchor.AboveBottom::offset)
         .codec();

      @Override
      public int resolveY(WorldGenerationContext $$0) {
         return $$0.getMinGenY() + this.offset;
      }

      @Override
      public String toString() {
         return this.offset + " above bottom";
      }
   }

   public record Absolute(int y) implements VerticalAnchor {
      public static final Codec<VerticalAnchor.Absolute> CODEC = Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)
         .fieldOf("absolute")
         .xmap(VerticalAnchor.Absolute::new, VerticalAnchor.Absolute::y)
         .codec();

      @Override
      public int resolveY(WorldGenerationContext $$0) {
         return this.y;
      }

      @Override
      public String toString() {
         return this.y + " absolute";
      }
   }

   public record BelowTop(int offset) implements VerticalAnchor {
      public static final Codec<VerticalAnchor.BelowTop> CODEC = Codec.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)
         .fieldOf("below_top")
         .xmap(VerticalAnchor.BelowTop::new, VerticalAnchor.BelowTop::offset)
         .codec();

      @Override
      public int resolveY(WorldGenerationContext $$0) {
         return $$0.getGenDepth() - 1 + $$0.getMinGenY() - this.offset;
      }

      @Override
      public String toString() {
         return this.offset + " below top";
      }
   }
}
