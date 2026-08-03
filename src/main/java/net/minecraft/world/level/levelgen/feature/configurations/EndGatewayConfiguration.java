package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;

public class EndGatewayConfiguration implements FeatureConfiguration {
   public static final Codec<EndGatewayConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(BlockPos.CODEC.optionalFieldOf("exit").forGetter($$0x -> $$0x.exit), Codec.BOOL.fieldOf("exact").forGetter($$0x -> $$0x.exact))
         .apply($$0, EndGatewayConfiguration::new)
   );
   private final Optional<BlockPos> exit;
   private final boolean exact;

   private EndGatewayConfiguration(Optional<BlockPos> $$0, boolean $$1) {
      this.exit = $$0;
      this.exact = $$1;
   }

   public static EndGatewayConfiguration knownExit(BlockPos $$0, boolean $$1) {
      return new EndGatewayConfiguration(Optional.of($$0), $$1);
   }

   public static EndGatewayConfiguration delayedExitSearch() {
      return new EndGatewayConfiguration(Optional.empty(), false);
   }

   public Optional<BlockPos> getExit() {
      return this.exit;
   }

   public boolean isExitExact() {
      return this.exact;
   }
}
