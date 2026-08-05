package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;

public class SpikeConfiguration implements FeatureConfiguration {
   public static final Codec<SpikeConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.BOOL.fieldOf("crystal_invulnerable").orElse(false).forGetter($$0x -> $$0x.crystalInvulnerable),
            SpikeFeature.EndSpike.CODEC.listOf().fieldOf("spikes").forGetter($$0x -> $$0x.spikes),
            BlockPos.CODEC.optionalFieldOf("crystal_beam_target").forGetter($$0x -> Optional.ofNullable($$0x.crystalBeamTarget))
         )
         .apply($$0, SpikeConfiguration::new)
   );
   private final boolean crystalInvulnerable;
   private final List<SpikeFeature.EndSpike> spikes;
   
   private final BlockPos crystalBeamTarget;

   public SpikeConfiguration(boolean $$0, List<SpikeFeature.EndSpike> $$1, BlockPos $$2) {
      this($$0, $$1, Optional.ofNullable($$2));
   }

   private SpikeConfiguration(boolean $$0, List<SpikeFeature.EndSpike> $$1, Optional<BlockPos> $$2) {
      this.crystalInvulnerable = $$0;
      this.spikes = $$1;
      this.crystalBeamTarget = $$2.orElse(null);
   }

   public boolean isCrystalInvulnerable() {
      return this.crystalInvulnerable;
   }

   public List<SpikeFeature.EndSpike> getSpikes() {
      return this.spikes;
   }

   
   public BlockPos getCrystalBeamTarget() {
      return this.crystalBeamTarget;
   }
}
