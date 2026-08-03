package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class NetherForestVegetationConfig extends BlockPileConfiguration {
   public static final Codec<NetherForestVegetationConfig> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            BlockStateProvider.CODEC.fieldOf("state_provider").forGetter($$0x -> $$0x.stateProvider),
            ExtraCodecs.POSITIVE_INT.fieldOf("spread_width").forGetter($$0x -> $$0x.spreadWidth),
            ExtraCodecs.POSITIVE_INT.fieldOf("spread_height").forGetter($$0x -> $$0x.spreadHeight)
         )
         .apply($$0, NetherForestVegetationConfig::new)
   );
   public final int spreadWidth;
   public final int spreadHeight;

   public NetherForestVegetationConfig(BlockStateProvider $$0, int $$1, int $$2) {
      super($$0);
      this.spreadWidth = $$1;
      this.spreadHeight = $$2;
   }
}
