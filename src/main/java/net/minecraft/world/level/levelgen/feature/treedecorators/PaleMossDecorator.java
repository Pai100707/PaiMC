package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class PaleMossDecorator extends TreeDecorator {
   public static final MapCodec<PaleMossDecorator> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("leaves_probability").forGetter($$0x -> $$0x.leavesProbability),
            Codec.floatRange(0.0F, 1.0F).fieldOf("trunk_probability").forGetter($$0x -> $$0x.trunkProbability),
            Codec.floatRange(0.0F, 1.0F).fieldOf("ground_probability").forGetter($$0x -> $$0x.groundProbability)
         )
         .apply($$0, PaleMossDecorator::new)
   );
   private final float leavesProbability;
   private final float trunkProbability;
   private final float groundProbability;

   @Override
   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.PALE_MOSS;
   }

   public PaleMossDecorator(float $$0, float $$1, float $$2) {
      this.leavesProbability = $$0;
      this.trunkProbability = $$1;
      this.groundProbability = $$2;
   }

   @Override
   public void place(TreeDecorator.Context $$0) {
      RandomSource $$1 = $$0.random();
      net.minecraft.world.level.WorldGenLevel $$2 = (net.minecraft.world.level.WorldGenLevel)$$0.level();
      List<BlockPos> $$3 = Util.shuffledCopy($$0.logs(), $$1);
      if (!$$3.isEmpty()) {
         BlockPos $$4 = Collections.min($$3, Comparator.comparingInt(Vec3i::getY));
         if ($$1.nextFloat() < this.groundProbability) {
            $$2.registryAccess()
               .lookup(Registries.CONFIGURED_FEATURE)
               .flatMap($$0x -> $$0x.get(VegetationFeatures.PALE_MOSS_PATCH))
               .ifPresent($$3x -> ((ConfiguredFeature)$$3x.value()).place($$2, $$2.getLevel().getChunkSource().getGenerator(), $$1, $$4.above()));
         }

         $$0.logs().forEach($$2x -> {
            if ($$1.nextFloat() < this.trunkProbability) {
               BlockPos $$3x = $$2x.below();
               if ($$0.isAir($$3x)) {
                  addMossHanger($$3x, $$0);
               }
            }
         });
         $$0.leaves().forEach($$2x -> {
            if ($$1.nextFloat() < this.leavesProbability) {
               BlockPos $$3x = $$2x.below();
               if ($$0.isAir($$3x)) {
                  addMossHanger($$3x, $$0);
               }
            }
         });
      }
   }

   private static void addMossHanger(BlockPos $$0, TreeDecorator.Context $$1) {
      while ($$1.isAir($$0.below()) && !($$1.random().nextFloat() < 0.5)) {
         $$1.setBlock($$0, Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, false));
         $$0 = $$0.below();
      }

      $$1.setBlock($$0, Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, true));
   }
}
