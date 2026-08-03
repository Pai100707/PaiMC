package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class RandomBlockStateMatchTest extends RuleTest {
   public static final MapCodec<RandomBlockStateMatchTest> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            BlockState.CODEC.fieldOf("block_state").forGetter($$0x -> $$0x.blockState), Codec.FLOAT.fieldOf("probability").forGetter($$0x -> $$0x.probability)
         )
         .apply($$0, RandomBlockStateMatchTest::new)
   );
   private final BlockState blockState;
   private final float probability;

   public RandomBlockStateMatchTest(BlockState $$0, float $$1) {
      this.blockState = $$0;
      this.probability = $$1;
   }

   @Override
   public boolean test(BlockState $$0, RandomSource $$1) {
      return $$0 == this.blockState && $$1.nextFloat() < this.probability;
   }

   @Override
   protected RuleTestType<?> getType() {
      return RuleTestType.RANDOM_BLOCKSTATE_TEST;
   }
}
