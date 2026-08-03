package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;

public class SpringConfiguration implements FeatureConfiguration {
   public static final Codec<SpringConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            FluidState.CODEC.fieldOf("state").forGetter($$0x -> $$0x.state),
            Codec.BOOL.fieldOf("requires_block_below").orElse(true).forGetter($$0x -> $$0x.requiresBlockBelow),
            Codec.INT.fieldOf("rock_count").orElse(4).forGetter($$0x -> $$0x.rockCount),
            Codec.INT.fieldOf("hole_count").orElse(1).forGetter($$0x -> $$0x.holeCount),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("valid_blocks").forGetter($$0x -> $$0x.validBlocks)
         )
         .apply($$0, SpringConfiguration::new)
   );
   public final FluidState state;
   public final boolean requiresBlockBelow;
   public final int rockCount;
   public final int holeCount;
   public final HolderSet<Block> validBlocks;

   public SpringConfiguration(FluidState $$0, boolean $$1, int $$2, int $$3, HolderSet<Block> $$4) {
      this.state = $$0;
      this.requiresBlockBelow = $$1;
      this.rockCount = $$2;
      this.holeCount = $$3;
      this.validBlocks = $$4;
   }
}
