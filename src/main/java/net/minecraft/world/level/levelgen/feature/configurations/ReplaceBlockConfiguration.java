package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockStateMatchTest;

public class ReplaceBlockConfiguration implements FeatureConfiguration {
   public static final Codec<ReplaceBlockConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter($$0x -> $$0x.targetStates))
         .apply($$0, ReplaceBlockConfiguration::new)
   );
   public final List<OreConfiguration.TargetBlockState> targetStates;

   public ReplaceBlockConfiguration(BlockState $$0, BlockState $$1) {
      this(ImmutableList.of(OreConfiguration.target(new BlockStateMatchTest($$0), $$1)));
   }

   public ReplaceBlockConfiguration(List<OreConfiguration.TargetBlockState> $$0) {
      this.targetStates = $$0;
   }
}
