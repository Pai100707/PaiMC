package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class BlobFoliagePlacer extends FoliagePlacer {
   public static final MapCodec<BlobFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec($$0 -> blobParts($$0).apply($$0, BlobFoliagePlacer::new));
   protected final int height;

   protected static <P extends BlobFoliagePlacer> P3<Mu<P>, IntProvider, IntProvider, Integer> blobParts(Instance<P> $$0) {
      return foliagePlacerParts($$0).and(Codec.intRange(0, 16).fieldOf("height").forGetter($$0x -> $$0x.height));
   }

   public BlobFoliagePlacer(IntProvider $$0, IntProvider $$1, int $$2) {
      super($$0, $$1);
      this.height = $$2;
   }

   @Override
   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
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
         int $$10 = Math.max($$7 + $$5.radiusOffset() - 1 - $$9 / 2, 0);
         this.placeLeavesRow($$0, $$1, $$2, $$3, $$5.pos(), $$10, $$9, $$5.doubleTrunk());
      }
   }

   @Override
   public int foliageHeight(RandomSource $$0, int $$1, TreeConfiguration $$2) {
      return this.height;
   }

   @Override
   protected boolean shouldSkipLocation(RandomSource $$0, int $$1, int $$2, int $$3, int $$4, boolean $$5) {
      return $$1 == $$4 && $$3 == $$4 && ($$0.nextInt(2) == 0 || $$2 == 0);
   }
}
