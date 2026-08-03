package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.valueproviders.IntProvider;

public class CappedProcessor extends StructureProcessor {
   public static final MapCodec<CappedProcessor> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            StructureProcessorType.SINGLE_CODEC.fieldOf("delegate").forGetter($$0x -> $$0x.delegate),
            IntProvider.POSITIVE_CODEC.fieldOf("limit").forGetter($$0x -> $$0x.limit)
         )
         .apply($$0, CappedProcessor::new)
   );
   private final StructureProcessor delegate;
   private final IntProvider limit;

   public CappedProcessor(StructureProcessor $$0, IntProvider $$1) {
      this.delegate = $$0;
      this.limit = $$1;
   }

   @Override
   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.CAPPED;
   }

   @Override
   public final List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
      net.minecraft.world.level.ServerLevelAccessor $$0,
      BlockPos $$1,
      BlockPos $$2,
      List<StructureTemplate.StructureBlockInfo> $$3,
      List<StructureTemplate.StructureBlockInfo> $$4,
      StructurePlaceSettings $$5
   ) {
      if (this.limit.getMaxValue() != 0 && !$$4.isEmpty()) {
         if ($$3.size() != $$4.size()) {
            Util.logAndPauseIfInIde(
               "Original block info list not in sync with processed list, skipping processing. Original size: "
                  + $$3.size()
                  + ", Processed size: "
                  + $$4.size()
            );
            return $$4;
         } else {
            RandomSource $$6 = RandomSource.create($$0.getLevel().getSeed()).forkPositional().at($$1);
            int $$7 = Math.min(this.limit.sample($$6), $$4.size());
            if ($$7 < 1) {
               return $$4;
            } else {
               IntArrayList $$8 = Util.toShuffledList(IntStream.range(0, $$4.size()), $$6);
               IntIterator $$9 = $$8.intIterator();
               int $$10 = 0;

               while ($$9.hasNext() && $$10 < $$7) {
                  int $$11 = $$9.nextInt();
                  StructureTemplate.StructureBlockInfo $$12 = $$3.get($$11);
                  StructureTemplate.StructureBlockInfo $$13 = $$4.get($$11);
                  StructureTemplate.StructureBlockInfo $$14 = this.delegate.processBlock($$0, $$1, $$2, $$12, $$13, $$5);
                  if ($$14 != null && !$$13.equals($$14)) {
                     $$10++;
                     $$4.set($$11, $$14);
                  }
               }

               return $$4;
            }
         }
      } else {
         return $$4;
      }
   }
}
