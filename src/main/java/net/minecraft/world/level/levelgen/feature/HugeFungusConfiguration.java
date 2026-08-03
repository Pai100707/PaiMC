package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class HugeFungusConfiguration implements FeatureConfiguration {
   public static final Codec<HugeFungusConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            BlockState.CODEC.fieldOf("valid_base_block").forGetter($$0x -> $$0x.validBaseState),
            BlockState.CODEC.fieldOf("stem_state").forGetter($$0x -> $$0x.stemState),
            BlockState.CODEC.fieldOf("hat_state").forGetter($$0x -> $$0x.hatState),
            BlockState.CODEC.fieldOf("decor_state").forGetter($$0x -> $$0x.decorState),
            BlockPredicate.CODEC.fieldOf("replaceable_blocks").forGetter($$0x -> $$0x.replaceableBlocks),
            Codec.BOOL.fieldOf("planted").orElse(false).forGetter($$0x -> $$0x.planted)
         )
         .apply($$0, HugeFungusConfiguration::new)
   );
   public final BlockState validBaseState;
   public final BlockState stemState;
   public final BlockState hatState;
   public final BlockState decorState;
   public final BlockPredicate replaceableBlocks;
   public final boolean planted;

   public HugeFungusConfiguration(BlockState $$0, BlockState $$1, BlockState $$2, BlockState $$3, BlockPredicate $$4, boolean $$5) {
      this.validBaseState = $$0;
      this.stemState = $$1;
      this.hatState = $$2;
      this.decorState = $$3;
      this.replaceableBlocks = $$4;
      this.planted = $$5;
   }
}
