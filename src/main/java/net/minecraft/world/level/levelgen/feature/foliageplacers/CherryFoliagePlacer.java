package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class CherryFoliagePlacer extends FoliagePlacer {
   public static final MapCodec<CherryFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> foliagePlacerParts($$0)
         .and(
            $$0.group(
               IntProvider.codec(4, 16).fieldOf("height").forGetter($$0x -> $$0x.height),
               Codec.floatRange(0.0F, 1.0F).fieldOf("wide_bottom_layer_hole_chance").forGetter($$0x -> $$0x.wideBottomLayerHoleChance),
               Codec.floatRange(0.0F, 1.0F).fieldOf("corner_hole_chance").forGetter($$0x -> $$0x.wideBottomLayerHoleChance),
               Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_chance").forGetter($$0x -> $$0x.hangingLeavesChance),
               Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_extension_chance").forGetter($$0x -> $$0x.hangingLeavesExtensionChance)
            )
         )
         .apply($$0, CherryFoliagePlacer::new)
   );
   private final IntProvider height;
   private final float wideBottomLayerHoleChance;
   private final float cornerHoleChance;
   private final float hangingLeavesChance;
   private final float hangingLeavesExtensionChance;

   public CherryFoliagePlacer(IntProvider $$0, IntProvider $$1, IntProvider $$2, float $$3, float $$4, float $$5, float $$6) {
      super($$0, $$1);
      this.height = $$2;
      this.wideBottomLayerHoleChance = $$3;
      this.cornerHoleChance = $$4;
      this.hangingLeavesChance = $$5;
      this.hangingLeavesExtensionChance = $$6;
   }

   @Override
   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.CHERRY_FOLIAGE_PLACER;
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
      int $$11 = $$7 + $$5.radiusOffset() - 1;
      this.placeLeavesRow($$0, $$1, $$2, $$3, $$10, $$11 - 2, $$6 - 3, $$9);
      this.placeLeavesRow($$0, $$1, $$2, $$3, $$10, $$11 - 1, $$6 - 4, $$9);

      for (int $$12 = $$6 - 5; $$12 >= 0; $$12--) {
         this.placeLeavesRow($$0, $$1, $$2, $$3, $$10, $$11, $$12, $$9);
      }

      this.placeLeavesRowWithHangingLeavesBelow($$0, $$1, $$2, $$3, $$10, $$11, -1, $$9, this.hangingLeavesChance, this.hangingLeavesExtensionChance);
      this.placeLeavesRowWithHangingLeavesBelow($$0, $$1, $$2, $$3, $$10, $$11 - 1, -2, $$9, this.hangingLeavesChance, this.hangingLeavesExtensionChance);
   }

   @Override
   public int foliageHeight(RandomSource $$0, int $$1, TreeConfiguration $$2) {
      return this.height.sample($$0);
   }

   @Override
   protected boolean shouldSkipLocation(RandomSource $$0, int $$1, int $$2, int $$3, int $$4, boolean $$5) {
      if ($$2 == -1 && ($$1 == $$4 || $$3 == $$4) && $$0.nextFloat() < this.wideBottomLayerHoleChance) {
         return true;
      } else {
         boolean $$6 = $$1 == $$4 && $$3 == $$4;
         boolean $$7 = $$4 > 2;
         return $$7 ? $$6 || $$1 + $$3 > $$4 * 2 - 2 && $$0.nextFloat() < this.cornerHoleChance : $$6 && $$0.nextFloat() < this.cornerHoleChance;
      }
   }
}
