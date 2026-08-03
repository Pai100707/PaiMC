package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class GeodeBlockSettings {
   public final BlockStateProvider fillingProvider;
   public final BlockStateProvider innerLayerProvider;
   public final BlockStateProvider alternateInnerLayerProvider;
   public final BlockStateProvider middleLayerProvider;
   public final BlockStateProvider outerLayerProvider;
   public final List<BlockState> innerPlacements;
   public final TagKey<Block> cannotReplace;
   public final TagKey<Block> invalidBlocks;
   public static final Codec<GeodeBlockSettings> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            BlockStateProvider.CODEC.fieldOf("filling_provider").forGetter($$0x -> $$0x.fillingProvider),
            BlockStateProvider.CODEC.fieldOf("inner_layer_provider").forGetter($$0x -> $$0x.innerLayerProvider),
            BlockStateProvider.CODEC.fieldOf("alternate_inner_layer_provider").forGetter($$0x -> $$0x.alternateInnerLayerProvider),
            BlockStateProvider.CODEC.fieldOf("middle_layer_provider").forGetter($$0x -> $$0x.middleLayerProvider),
            BlockStateProvider.CODEC.fieldOf("outer_layer_provider").forGetter($$0x -> $$0x.outerLayerProvider),
            ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("inner_placements").forGetter($$0x -> $$0x.innerPlacements),
            TagKey.hashedCodec(Registries.BLOCK).fieldOf("cannot_replace").forGetter($$0x -> $$0x.cannotReplace),
            TagKey.hashedCodec(Registries.BLOCK).fieldOf("invalid_blocks").forGetter($$0x -> $$0x.invalidBlocks)
         )
         .apply($$0, GeodeBlockSettings::new)
   );

   public GeodeBlockSettings(
      BlockStateProvider $$0,
      BlockStateProvider $$1,
      BlockStateProvider $$2,
      BlockStateProvider $$3,
      BlockStateProvider $$4,
      List<BlockState> $$5,
      TagKey<Block> $$6,
      TagKey<Block> $$7
   ) {
      this.fillingProvider = $$0;
      this.innerLayerProvider = $$1;
      this.alternateInnerLayerProvider = $$2;
      this.middleLayerProvider = $$3;
      this.outerLayerProvider = $$4;
      this.innerPlacements = $$5;
      this.cannotReplace = $$6;
      this.invalidBlocks = $$7;
   }
}
