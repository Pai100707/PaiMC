package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public class PosAlwaysTrueTest extends PosRuleTest {
   public static final MapCodec<PosAlwaysTrueTest> CODEC = MapCodec.unit(() -> PosAlwaysTrueTest.INSTANCE);
   public static final PosAlwaysTrueTest INSTANCE = new PosAlwaysTrueTest();

   private PosAlwaysTrueTest() {
   }

   @Override
   public boolean test(BlockPos $$0, BlockPos $$1, BlockPos $$2, RandomSource $$3) {
      return true;
   }

   @Override
   protected PosRuleTestType<?> getType() {
      return PosRuleTestType.ALWAYS_TRUE_TEST;
   }
}
