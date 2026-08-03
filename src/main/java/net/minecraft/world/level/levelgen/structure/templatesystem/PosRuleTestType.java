package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public interface PosRuleTestType<P extends PosRuleTest> {
   PosRuleTestType<PosAlwaysTrueTest> ALWAYS_TRUE_TEST = register("always_true", PosAlwaysTrueTest.CODEC);
   PosRuleTestType<LinearPosTest> LINEAR_POS_TEST = register("linear_pos", LinearPosTest.CODEC);
   PosRuleTestType<AxisAlignedLinearPosTest> AXIS_ALIGNED_LINEAR_POS_TEST = register("axis_aligned_linear_pos", AxisAlignedLinearPosTest.CODEC);

   MapCodec<P> codec();

   static <P extends PosRuleTest> PosRuleTestType<P> register(String $$0, MapCodec<P> $$1) {
      return (PosRuleTestType<P>)Registry.register(BuiltInRegistries.POS_RULE_TEST, $$0, (PosRuleTestType<>)() -> $$1);
   }
}
