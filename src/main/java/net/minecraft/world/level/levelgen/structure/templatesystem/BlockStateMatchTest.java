package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateMatchTest extends RuleTest {
   public static final MapCodec<BlockStateMatchTest> CODEC = BlockState.CODEC.fieldOf("block_state").xmap(BlockStateMatchTest::new, $$0 -> $$0.blockState);
   private final BlockState blockState;

   public BlockStateMatchTest(BlockState $$0) {
      this.blockState = $$0;
   }

   @Override
   public boolean test(BlockState $$0, RandomSource $$1) {
      return $$0 == this.blockState;
   }

   @Override
   protected RuleTestType<?> getType() {
      return RuleTestType.BLOCKSTATE_TEST;
   }
}
