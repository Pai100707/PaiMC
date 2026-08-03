package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RandomBlockMatchTest extends RuleTest {
   public static final MapCodec<RandomBlockMatchTest> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter($$0x -> $$0x.block),
            Codec.FLOAT.fieldOf("probability").forGetter($$0x -> $$0x.probability)
         )
         .apply($$0, RandomBlockMatchTest::new)
   );
   private final Block block;
   private final float probability;

   public RandomBlockMatchTest(Block $$0, float $$1) {
      this.block = $$0;
      this.probability = $$1;
   }

   @Override
   public boolean test(BlockState $$0, RandomSource $$1) {
      return $$0.is(this.block) && $$1.nextFloat() < this.probability;
   }

   @Override
   protected RuleTestType<?> getType() {
      return RuleTestType.RANDOM_BLOCK_TEST;
   }
}
