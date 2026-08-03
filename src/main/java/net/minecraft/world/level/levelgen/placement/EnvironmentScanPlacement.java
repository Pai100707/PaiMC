package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public class EnvironmentScanPlacement extends PlacementModifier {
   private final Direction directionOfSearch;
   private final BlockPredicate targetCondition;
   private final BlockPredicate allowedSearchCondition;
   private final int maxSteps;
   public static final MapCodec<EnvironmentScanPlacement> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Direction.VERTICAL_CODEC.fieldOf("direction_of_search").forGetter($$0x -> $$0x.directionOfSearch),
            BlockPredicate.CODEC.fieldOf("target_condition").forGetter($$0x -> $$0x.targetCondition),
            BlockPredicate.CODEC.optionalFieldOf("allowed_search_condition", BlockPredicate.alwaysTrue()).forGetter($$0x -> $$0x.allowedSearchCondition),
            Codec.intRange(1, 32).fieldOf("max_steps").forGetter($$0x -> $$0x.maxSteps)
         )
         .apply($$0, EnvironmentScanPlacement::new)
   );

   private EnvironmentScanPlacement(Direction $$0, BlockPredicate $$1, BlockPredicate $$2, int $$3) {
      this.directionOfSearch = $$0;
      this.targetCondition = $$1;
      this.allowedSearchCondition = $$2;
      this.maxSteps = $$3;
   }

   public static EnvironmentScanPlacement scanningFor(Direction $$0, BlockPredicate $$1, BlockPredicate $$2, int $$3) {
      return new EnvironmentScanPlacement($$0, $$1, $$2, $$3);
   }

   public static EnvironmentScanPlacement scanningFor(Direction $$0, BlockPredicate $$1, int $$2) {
      return scanningFor($$0, $$1, BlockPredicate.alwaysTrue(), $$2);
   }

   @Override
   public Stream<BlockPos> getPositions(PlacementContext $$0, RandomSource $$1, BlockPos $$2) {
      MutableBlockPos $$3 = $$2.mutable();
      net.minecraft.world.level.WorldGenLevel $$4 = $$0.getLevel();
      if (!this.allowedSearchCondition.test($$4, $$3)) {
         return Stream.of();
      } else {
         for (int $$5 = 0; $$5 < this.maxSteps; $$5++) {
            if (this.targetCondition.test($$4, $$3)) {
               return Stream.of($$3);
            }

            $$3.move(this.directionOfSearch);
            if ($$4.isOutsideBuildHeight($$3.getY())) {
               return Stream.of();
            }

            if (!this.allowedSearchCondition.test($$4, $$3)) {
               break;
            }
         }

         return this.targetCondition.test($$4, $$3) ? Stream.of($$3) : Stream.of();
      }
   }

   @Override
   public PlacementModifierType<?> type() {
      return PlacementModifierType.ENVIRONMENT_SCAN;
   }
}
