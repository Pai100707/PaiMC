package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VegetationPatchConfiguration implements FeatureConfiguration {
   public static final Codec<VegetationPatchConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            TagKey.hashedCodec(Registries.BLOCK).fieldOf("replaceable").forGetter($$0x -> $$0x.replaceable),
            BlockStateProvider.CODEC.fieldOf("ground_state").forGetter($$0x -> $$0x.groundState),
            PlacedFeature.CODEC.fieldOf("vegetation_feature").forGetter($$0x -> $$0x.vegetationFeature),
            CaveSurface.CODEC.fieldOf("surface").forGetter($$0x -> $$0x.surface),
            IntProvider.codec(1, 128).fieldOf("depth").forGetter($$0x -> $$0x.depth),
            Codec.floatRange(0.0F, 1.0F).fieldOf("extra_bottom_block_chance").forGetter($$0x -> $$0x.extraBottomBlockChance),
            Codec.intRange(1, 256).fieldOf("vertical_range").forGetter($$0x -> $$0x.verticalRange),
            Codec.floatRange(0.0F, 1.0F).fieldOf("vegetation_chance").forGetter($$0x -> $$0x.vegetationChance),
            IntProvider.CODEC.fieldOf("xz_radius").forGetter($$0x -> $$0x.xzRadius),
            Codec.floatRange(0.0F, 1.0F).fieldOf("extra_edge_column_chance").forGetter($$0x -> $$0x.extraEdgeColumnChance)
         )
         .apply($$0, VegetationPatchConfiguration::new)
   );
   public final TagKey<Block> replaceable;
   public final BlockStateProvider groundState;
   public final Holder<PlacedFeature> vegetationFeature;
   public final CaveSurface surface;
   public final IntProvider depth;
   public final float extraBottomBlockChance;
   public final int verticalRange;
   public final float vegetationChance;
   public final IntProvider xzRadius;
   public final float extraEdgeColumnChance;

   public VegetationPatchConfiguration(
      TagKey<Block> $$0,
      BlockStateProvider $$1,
      Holder<PlacedFeature> $$2,
      CaveSurface $$3,
      IntProvider $$4,
      float $$5,
      int $$6,
      float $$7,
      IntProvider $$8,
      float $$9
   ) {
      this.replaceable = $$0;
      this.groundState = $$1;
      this.vegetationFeature = $$2;
      this.surface = $$3;
      this.depth = $$4;
      this.extraBottomBlockChance = $$5;
      this.verticalRange = $$6;
      this.vegetationChance = $$7;
      this.xzRadius = $$8;
      this.extraEdgeColumnChance = $$9;
   }
}
