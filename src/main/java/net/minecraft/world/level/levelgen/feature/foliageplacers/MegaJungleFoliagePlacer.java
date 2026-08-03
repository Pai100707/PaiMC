package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MegaJungleFoliagePlacer extends FoliagePlacer {
   public static final MapCodec<MegaJungleFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> foliagePlacerParts($$0).and(Codec.intRange(0, 16).fieldOf("height").forGetter($$0x -> $$0x.height)).apply($$0, MegaJungleFoliagePlacer::new)
   );
   protected final int height;

   public MegaJungleFoliagePlacer(IntProvider $$0, IntProvider $$1, int $$2) {
      super($$0, $$1);
      this.height = $$2;
   }

   @Override
   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.MEGA_JUNGLE_FOLIAGE_PLACER;
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
      int $$9 = $$5.doubleTrunk() ? $$6 : 1 + $$2.nextInt(2);

      for (int $$10 = $$8; $$10 >= $$8 - $$9; $$10--) {
         int $$11 = $$7 + $$5.radiusOffset() + 1 - $$10;
         this.placeLeavesRow($$0, $$1, $$2, $$3, $$5.pos(), $$11, $$10, $$5.doubleTrunk());
      }
   }

   @Override
   public int foliageHeight(RandomSource $$0, int $$1, TreeConfiguration $$2) {
      return this.height;
   }

   @Override
   protected boolean shouldSkipLocation(RandomSource $$0, int $$1, int $$2, int $$3, int $$4, boolean $$5) {
      return $$1 + $$3 >= 7 ? true : $$1 * $$1 + $$3 * $$3 > $$4 * $$4;
   }
}
