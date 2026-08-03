package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class OreConfiguration implements FeatureConfiguration {
   public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter($$0x -> $$0x.targetStates),
            Codec.intRange(0, 64).fieldOf("size").forGetter($$0x -> $$0x.size),
            Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter($$0x -> $$0x.discardChanceOnAirExposure)
         )
         .apply($$0, OreConfiguration::new)
   );
   public final List<OreConfiguration.TargetBlockState> targetStates;
   public final int size;
   public final float discardChanceOnAirExposure;

   public OreConfiguration(List<OreConfiguration.TargetBlockState> $$0, int $$1, float $$2) {
      this.size = $$1;
      this.targetStates = $$0;
      this.discardChanceOnAirExposure = $$2;
   }

   public OreConfiguration(List<OreConfiguration.TargetBlockState> $$0, int $$1) {
      this($$0, $$1, 0.0F);
   }

   public OreConfiguration(RuleTest $$0, BlockState $$1, int $$2, float $$3) {
      this(ImmutableList.of(new OreConfiguration.TargetBlockState($$0, $$1)), $$2, $$3);
   }

   public OreConfiguration(RuleTest $$0, BlockState $$1, int $$2) {
      this(ImmutableList.of(new OreConfiguration.TargetBlockState($$0, $$1)), $$2, 0.0F);
   }

   public static OreConfiguration.TargetBlockState target(RuleTest $$0, BlockState $$1) {
      return new OreConfiguration.TargetBlockState($$0, $$1);
   }

   public static class TargetBlockState {
      public static final Codec<OreConfiguration.TargetBlockState> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(RuleTest.CODEC.fieldOf("target").forGetter($$0x -> $$0x.target), BlockState.CODEC.fieldOf("state").forGetter($$0x -> $$0x.state))
            .apply($$0, OreConfiguration.TargetBlockState::new)
      );
      public final RuleTest target;
      public final BlockState state;

      TargetBlockState(RuleTest $$0, BlockState $$1) {
         this.target = $$0;
         this.state = $$1;
      }
   }
}
