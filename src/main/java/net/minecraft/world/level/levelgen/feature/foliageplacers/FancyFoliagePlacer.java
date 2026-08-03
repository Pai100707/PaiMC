package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class FancyFoliagePlacer extends BlobFoliagePlacer {
   public static final MapCodec<FancyFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec($$0 -> blobParts($$0).apply($$0, FancyFoliagePlacer::new));

   public FancyFoliagePlacer(IntProvider $$0, IntProvider $$1, int $$2) {
      super($$0, $$1, $$2);
   }

   @Override
   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.FANCY_FOLIAGE_PLACER;
   }

   @Override
   protected void createFoliage(
      net.minecraft.world.level.LevelSimulatedReader $$0,
      FoliagePlacer.FoliageSetter $$1,
      RandomSource $$2,
      TreeConfiguration $$3,
      int $$4,
      FoliagePlacer.FoliageAttachment $$5,
      int $$6,
      int $$7,
      int $$8
   ) {
      for (int $$9 = $$8; $$9 >= $$8 - $$6; $$9--) {
         int $$10 = $$7 + ($$9 != $$8 && $$9 != $$8 - $$6 ? 1 : 0);
         this.placeLeavesRow($$0, $$1, $$2, $$3, $$5.pos(), $$10, $$9, $$5.doubleTrunk());
      }
   }

   @Override
   protected boolean shouldSkipLocation(RandomSource $$0, int $$1, int $$2, int $$3, int $$4, boolean $$5) {
      return Mth.square($$1 + 0.5F) + Mth.square($$3 + 0.5F) > $$4 * $$4;
   }
}
