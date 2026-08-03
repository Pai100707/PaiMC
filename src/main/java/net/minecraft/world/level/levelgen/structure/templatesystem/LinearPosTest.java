package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class LinearPosTest extends PosRuleTest {
   public static final MapCodec<LinearPosTest> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.FLOAT.fieldOf("min_chance").orElse(0.0F).forGetter($$0x -> $$0x.minChance),
            Codec.FLOAT.fieldOf("max_chance").orElse(0.0F).forGetter($$0x -> $$0x.maxChance),
            Codec.INT.fieldOf("min_dist").orElse(0).forGetter($$0x -> $$0x.minDist),
            Codec.INT.fieldOf("max_dist").orElse(0).forGetter($$0x -> $$0x.maxDist)
         )
         .apply($$0, LinearPosTest::new)
   );
   private final float minChance;
   private final float maxChance;
   private final int minDist;
   private final int maxDist;

   public LinearPosTest(float $$0, float $$1, int $$2, int $$3) {
      if ($$2 >= $$3) {
         throw new IllegalArgumentException("Invalid range: [" + $$2 + "," + $$3 + "]");
      } else {
         this.minChance = $$0;
         this.maxChance = $$1;
         this.minDist = $$2;
         this.maxDist = $$3;
      }
   }

   @Override
   public boolean test(BlockPos $$0, BlockPos $$1, BlockPos $$2, RandomSource $$3) {
      int $$4 = $$1.distManhattan($$2);
      float $$5 = $$3.nextFloat();
      return $$5 <= Mth.clampedLerp(Mth.inverseLerp($$4, this.minDist, this.maxDist), this.minChance, this.maxChance);
   }

   @Override
   protected PosRuleTestType<?> getType() {
      return PosRuleTestType.LINEAR_POS_TEST;
   }
}
