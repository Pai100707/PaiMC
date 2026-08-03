package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;

public class MultifaceGrowthConfiguration implements FeatureConfiguration {
   public static final Codec<MultifaceGrowthConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            BuiltInRegistries.BLOCK
               .byNameCodec()
               .fieldOf("block")
               .flatXmap(MultifaceGrowthConfiguration::apply, DataResult::success)
               .orElse((MultifaceSpreadeableBlock)Blocks.GLOW_LICHEN)
               .forGetter($$0x -> $$0x.placeBlock),
            Codec.intRange(1, 64).fieldOf("search_range").orElse(10).forGetter($$0x -> $$0x.searchRange),
            Codec.BOOL.fieldOf("can_place_on_floor").orElse(false).forGetter($$0x -> $$0x.canPlaceOnFloor),
            Codec.BOOL.fieldOf("can_place_on_ceiling").orElse(false).forGetter($$0x -> $$0x.canPlaceOnCeiling),
            Codec.BOOL.fieldOf("can_place_on_wall").orElse(false).forGetter($$0x -> $$0x.canPlaceOnWall),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spreading").orElse(0.5F).forGetter($$0x -> $$0x.chanceOfSpreading),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_be_placed_on").forGetter($$0x -> $$0x.canBePlacedOn)
         )
         .apply($$0, MultifaceGrowthConfiguration::new)
   );
   public final MultifaceSpreadeableBlock placeBlock;
   public final int searchRange;
   public final boolean canPlaceOnFloor;
   public final boolean canPlaceOnCeiling;
   public final boolean canPlaceOnWall;
   public final float chanceOfSpreading;
   public final HolderSet<Block> canBePlacedOn;
   private final ObjectArrayList<Direction> validDirections;

   private static DataResult<MultifaceSpreadeableBlock> apply(Block $$0) {
      return $$0 instanceof MultifaceSpreadeableBlock $$1
         ? DataResult.success($$1)
         : DataResult.error(() -> "Growth block should be a multiface spreadeable block");
   }

   public MultifaceGrowthConfiguration(MultifaceSpreadeableBlock $$0, int $$1, boolean $$2, boolean $$3, boolean $$4, float $$5, HolderSet<Block> $$6) {
      this.placeBlock = $$0;
      this.searchRange = $$1;
      this.canPlaceOnFloor = $$2;
      this.canPlaceOnCeiling = $$3;
      this.canPlaceOnWall = $$4;
      this.chanceOfSpreading = $$5;
      this.canBePlacedOn = $$6;
      this.validDirections = new ObjectArrayList(6);
      if ($$3) {
         this.validDirections.add(Direction.UP);
      }

      if ($$2) {
         this.validDirections.add(Direction.DOWN);
      }

      if ($$4) {
         Plane.HORIZONTAL.forEach(this.validDirections::add);
      }
   }

   public List<Direction> getShuffledDirectionsExcept(RandomSource $$0, Direction $$1) {
      return Util.toShuffledList(this.validDirections.stream().filter($$1x -> $$1x != $$1), $$0);
   }

   public List<Direction> getShuffledDirections(RandomSource $$0) {
      return Util.shuffledCopy(this.validDirections, $$0);
   }
}
