package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class BushFoliagePlacer extends BlobFoliagePlacer {
   public static final MapCodec<BushFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec($$0 -> blobParts($$0).apply($$0, BushFoliagePlacer::new));

   public BushFoliagePlacer(IntProvider $$0, IntProvider $$1, int $$2) {
      super($$0, $$1, $$2);
   }

   @Override
   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.BUSH_FOLIAGE_PLACER;
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
         int $$10 = $$7 + $$5.radiusOffset() - 1 - $$9;
         this.placeLeavesRow($$0, $$1, $$2, $$3, $$5.pos(), $$10, $$9, $$5.doubleTrunk());
      }
   }

   @Override
   protected boolean shouldSkipLocation(RandomSource $$0, int $$1, int $$2, int $$3, int $$4, boolean $$5) {
      return $$1 == $$4 && $$3 == $$4 && $$0.nextInt(2) == 0;
   }
}
