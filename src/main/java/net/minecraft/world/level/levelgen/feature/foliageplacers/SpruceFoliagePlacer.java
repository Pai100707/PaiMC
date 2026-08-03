package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class SpruceFoliagePlacer extends FoliagePlacer {
   public static final MapCodec<SpruceFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> foliagePlacerParts($$0)
         .and(IntProvider.codec(0, 24).fieldOf("trunk_height").forGetter($$0x -> $$0x.trunkHeight))
         .apply($$0, SpruceFoliagePlacer::new)
   );
   private final IntProvider trunkHeight;

   public SpruceFoliagePlacer(IntProvider $$0, IntProvider $$1, IntProvider $$2) {
      super($$0, $$1);
      this.trunkHeight = $$2;
   }

   @Override
   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.SPRUCE_FOLIAGE_PLACER;
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
      BlockPos $$9 = $$5.pos();
      int $$10 = $$2.nextInt(2);
      int $$11 = 1;
      int $$12 = 0;

      for (int $$13 = $$8; $$13 >= -$$6; $$13--) {
         this.placeLeavesRow($$0, $$1, $$2, $$3, $$9, $$10, $$13, $$5.doubleTrunk());
         if ($$10 >= $$11) {
            $$10 = $$12;
            $$12 = 1;
            $$11 = Math.min($$11 + 1, $$7 + $$5.radiusOffset());
         } else {
            $$10++;
         }
      }
   }

   @Override
   public int foliageHeight(RandomSource $$0, int $$1, TreeConfiguration $$2) {
      return Math.max(4, $$1 - this.trunkHeight.sample($$0));
   }

   @Override
   protected boolean shouldSkipLocation(RandomSource $$0, int $$1, int $$2, int $$3, int $$4, boolean $$5) {
      return $$1 == $$4 && $$3 == $$4 && $$4 > 0;
   }
}
