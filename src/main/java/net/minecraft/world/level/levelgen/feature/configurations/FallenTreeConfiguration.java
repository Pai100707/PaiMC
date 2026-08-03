package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public class FallenTreeConfiguration implements FeatureConfiguration {
   public static final Codec<FallenTreeConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter($$0x -> $$0x.trunkProvider),
            IntProvider.codec(0, 16).fieldOf("log_length").forGetter($$0x -> $$0x.logLength),
            TreeDecorator.CODEC.listOf().fieldOf("stump_decorators").forGetter($$0x -> $$0x.stumpDecorators),
            TreeDecorator.CODEC.listOf().fieldOf("log_decorators").forGetter($$0x -> $$0x.logDecorators)
         )
         .apply($$0, FallenTreeConfiguration::new)
   );
   public final BlockStateProvider trunkProvider;
   public final IntProvider logLength;
   public final List<TreeDecorator> stumpDecorators;
   public final List<TreeDecorator> logDecorators;

   protected FallenTreeConfiguration(BlockStateProvider $$0, IntProvider $$1, List<TreeDecorator> $$2, List<TreeDecorator> $$3) {
      this.trunkProvider = $$0;
      this.logLength = $$1;
      this.stumpDecorators = $$2;
      this.logDecorators = $$3;
   }

   public static class FallenTreeConfigurationBuilder {
      private final BlockStateProvider trunkProvider;
      private final IntProvider logLength;
      private List<TreeDecorator> stumpDecorators = new ArrayList<>();
      private List<TreeDecorator> logDecorators = new ArrayList<>();

      public FallenTreeConfigurationBuilder(BlockStateProvider $$0, IntProvider $$1) {
         this.trunkProvider = $$0;
         this.logLength = $$1;
      }

      public FallenTreeConfiguration.FallenTreeConfigurationBuilder stumpDecorators(List<TreeDecorator> $$0) {
         this.stumpDecorators = $$0;
         return this;
      }

      public FallenTreeConfiguration.FallenTreeConfigurationBuilder logDecorators(List<TreeDecorator> $$0) {
         this.logDecorators = $$0;
         return this;
      }

      public FallenTreeConfiguration build() {
         return new FallenTreeConfiguration(this.trunkProvider, this.logLength, this.stumpDecorators, this.logDecorators);
      }
   }
}
