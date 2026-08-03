package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RootSystemConfiguration implements FeatureConfiguration {
   public static final Codec<RootSystemConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            PlacedFeature.CODEC.fieldOf("feature").forGetter($$0x -> $$0x.treeFeature),
            Codec.intRange(1, 64).fieldOf("required_vertical_space_for_tree").forGetter($$0x -> $$0x.requiredVerticalSpaceForTree),
            Codec.intRange(1, 64).fieldOf("root_radius").forGetter($$0x -> $$0x.rootRadius),
            TagKey.hashedCodec(Registries.BLOCK).fieldOf("root_replaceable").forGetter($$0x -> $$0x.rootReplaceable),
            BlockStateProvider.CODEC.fieldOf("root_state_provider").forGetter($$0x -> $$0x.rootStateProvider),
            Codec.intRange(1, 256).fieldOf("root_placement_attempts").forGetter($$0x -> $$0x.rootPlacementAttempts),
            Codec.intRange(1, 4096).fieldOf("root_column_max_height").forGetter($$0x -> $$0x.rootColumnMaxHeight),
            Codec.intRange(1, 64).fieldOf("hanging_root_radius").forGetter($$0x -> $$0x.hangingRootRadius),
            Codec.intRange(1, 16).fieldOf("hanging_roots_vertical_span").forGetter($$0x -> $$0x.hangingRootsVerticalSpan),
            BlockStateProvider.CODEC.fieldOf("hanging_root_state_provider").forGetter($$0x -> $$0x.hangingRootStateProvider),
            Codec.intRange(1, 256).fieldOf("hanging_root_placement_attempts").forGetter($$0x -> $$0x.hangingRootPlacementAttempts),
            Codec.intRange(1, 64).fieldOf("allowed_vertical_water_for_tree").forGetter($$0x -> $$0x.allowedVerticalWaterForTree),
            BlockPredicate.CODEC.fieldOf("allowed_tree_position").forGetter($$0x -> $$0x.allowedTreePosition)
         )
         .apply($$0, RootSystemConfiguration::new)
   );
   public final Holder<PlacedFeature> treeFeature;
   public final int requiredVerticalSpaceForTree;
   public final int rootRadius;
   public final TagKey<Block> rootReplaceable;
   public final BlockStateProvider rootStateProvider;
   public final int rootPlacementAttempts;
   public final int rootColumnMaxHeight;
   public final int hangingRootRadius;
   public final int hangingRootsVerticalSpan;
   public final BlockStateProvider hangingRootStateProvider;
   public final int hangingRootPlacementAttempts;
   public final int allowedVerticalWaterForTree;
   public final BlockPredicate allowedTreePosition;

   public RootSystemConfiguration(
      Holder<PlacedFeature> $$0,
      int $$1,
      int $$2,
      TagKey<Block> $$3,
      BlockStateProvider $$4,
      int $$5,
      int $$6,
      int $$7,
      int $$8,
      BlockStateProvider $$9,
      int $$10,
      int $$11,
      BlockPredicate $$12
   ) {
      this.treeFeature = $$0;
      this.requiredVerticalSpaceForTree = $$1;
      this.rootRadius = $$2;
      this.rootReplaceable = $$3;
      this.rootStateProvider = $$4;
      this.rootPlacementAttempts = $$5;
      this.rootColumnMaxHeight = $$6;
      this.hangingRootRadius = $$7;
      this.hangingRootsVerticalSpan = $$8;
      this.hangingRootStateProvider = $$9;
      this.hangingRootPlacementAttempts = $$10;
      this.allowedVerticalWaterForTree = $$11;
      this.allowedTreePosition = $$12;
   }
}
