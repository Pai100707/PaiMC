package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMatchTest extends RuleTest {
   public static final MapCodec<BlockMatchTest> CODEC = BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").xmap(BlockMatchTest::new, $$0 -> $$0.block);
   private final Block block;

   public BlockMatchTest(Block $$0) {
      this.block = $$0;
   }

   @Override
   public boolean test(BlockState $$0, RandomSource $$1) {
      return $$0.is(this.block);
   }

   @Override
   protected RuleTestType<?> getType() {
      return RuleTestType.BLOCK_TEST;
   }
}
