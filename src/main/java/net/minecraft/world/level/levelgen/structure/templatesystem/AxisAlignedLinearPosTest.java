package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class AxisAlignedLinearPosTest extends PosRuleTest {
   public static final MapCodec<AxisAlignedLinearPosTest> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.FLOAT.fieldOf("min_chance").orElse(0.0F).forGetter($$0x -> $$0x.minChance),
            Codec.FLOAT.fieldOf("max_chance").orElse(0.0F).forGetter($$0x -> $$0x.maxChance),
            Codec.INT.fieldOf("min_dist").orElse(0).forGetter($$0x -> $$0x.minDist),
            Codec.INT.fieldOf("max_dist").orElse(0).forGetter($$0x -> $$0x.maxDist),
            Axis.CODEC.fieldOf("axis").orElse(Axis.Y).forGetter($$0x -> $$0x.axis)
         )
         .apply($$0, AxisAlignedLinearPosTest::new)
   );
   private final float minChance;
   private final float maxChance;
   private final int minDist;
   private final int maxDist;
   private final Axis axis;

   public AxisAlignedLinearPosTest(float $$0, float $$1, int $$2, int $$3, Axis $$4) {
      if ($$2 >= $$3) {
         throw new IllegalArgumentException("Invalid range: [" + $$2 + "," + $$3 + "]");
      } else {
         this.minChance = $$0;
         this.maxChance = $$1;
         this.minDist = $$2;
         this.maxDist = $$3;
         this.axis = $$4;
      }
   }

   @Override
   public boolean test(BlockPos $$0, BlockPos $$1, BlockPos $$2, RandomSource $$3) {
      Direction $$4 = Direction.get(AxisDirection.POSITIVE, this.axis);
      float $$5 = Math.abs(($$1.getX() - $$2.getX()) * $$4.getStepX());
      float $$6 = Math.abs(($$1.getY() - $$2.getY()) * $$4.getStepY());
      float $$7 = Math.abs(($$1.getZ() - $$2.getZ()) * $$4.getStepZ());
      int $$8 = (int)($$5 + $$6 + $$7);
      float $$9 = $$3.nextFloat();
      return $$9 <= Mth.clampedLerp(Mth.inverseLerp($$8, this.minDist, this.maxDist), this.minChance, this.maxChance);
   }

   @Override
   protected PosRuleTestType<?> getType() {
      return PosRuleTestType.AXIS_ALIGNED_LINEAR_POS_TEST;
   }
}
