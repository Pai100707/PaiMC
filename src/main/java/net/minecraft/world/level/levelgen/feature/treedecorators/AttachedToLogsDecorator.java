package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class AttachedToLogsDecorator extends TreeDecorator {
   public static final MapCodec<AttachedToLogsDecorator> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter($$0x -> $$0x.probability),
            BlockStateProvider.CODEC.fieldOf("block_provider").forGetter($$0x -> $$0x.blockProvider),
            ExtraCodecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions").forGetter($$0x -> $$0x.directions)
         )
         .apply($$0, AttachedToLogsDecorator::new)
   );
   private final float probability;
   private final BlockStateProvider blockProvider;
   private final List<Direction> directions;

   public AttachedToLogsDecorator(float $$0, BlockStateProvider $$1, List<Direction> $$2) {
      this.probability = $$0;
      this.blockProvider = $$1;
      this.directions = $$2;
   }

   @Override
   public void place(TreeDecorator.Context $$0) {
      RandomSource $$1 = $$0.random();

      for (BlockPos $$2 : Util.shuffledCopy($$0.logs(), $$1)) {
         Direction $$3 = (Direction)Util.getRandom(this.directions, $$1);
         BlockPos $$4 = $$2.relative($$3);
         if ($$1.nextFloat() <= this.probability && $$0.isAir($$4)) {
            $$0.setBlock($$4, this.blockProvider.getState($$1, $$4));
         }
      }
   }

   @Override
   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.ATTACHED_TO_LOGS;
   }
}
