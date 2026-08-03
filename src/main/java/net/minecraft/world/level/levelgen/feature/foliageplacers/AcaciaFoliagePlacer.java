package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class AcaciaFoliagePlacer extends FoliagePlacer {
   public static final MapCodec<AcaciaFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec($$0 -> foliagePlacerParts($$0).apply($$0, AcaciaFoliagePlacer::new));

   public AcaciaFoliagePlacer(IntProvider $$0, IntProvider $$1) {
      super($$0, $$1);
   }

   @Override
   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.ACACIA_FOLIAGE_PLACER;
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
      boolean $$9 = $$5.doubleTrunk();
      BlockPos $$10 = $$5.pos().above($$8);
      this.placeLeavesRow($$0, $$1, $$2, $$3, $$10, $$7 + $$5.radiusOffset(), -1 - $$6, $$9);
      this.placeLeavesRow($$0, $$1, $$2, $$3, $$10, $$7 - 1, -$$6, $$9);
      this.placeLeavesRow($$0, $$1, $$2, $$3, $$10, $$7 + $$5.radiusOffset() - 1, 0, $$9);
   }

   @Override
   public int foliageHeight(RandomSource $$0, int $$1, TreeConfiguration $$2) {
      return 0;
   }

   @Override
   protected boolean shouldSkipLocation(RandomSource $$0, int $$1, int $$2, int $$3, int $$4, boolean $$5) {
      return $$2 == 0 ? ($$1 > 1 || $$3 > 1) && $$1 != 0 && $$3 != 0 : $$1 == $$4 && $$3 == $$4 && $$4 > 0;
   }
}
