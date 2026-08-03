package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public abstract class TrunkPlacer {
   public static final Codec<TrunkPlacer> CODEC = BuiltInRegistries.TRUNK_PLACER_TYPE.byNameCodec().dispatch(TrunkPlacer::type, TrunkPlacerType::codec);
   private static final int MAX_BASE_HEIGHT = 32;
   private static final int MAX_RAND = 24;
   public static final int MAX_HEIGHT = 80;
   protected final int baseHeight;
   protected final int heightRandA;
   protected final int heightRandB;

   protected static <P extends TrunkPlacer> P3<Mu<P>, Integer, Integer, Integer> trunkPlacerParts(Instance<P> $$0) {
      return $$0.group(
         Codec.intRange(0, 32).fieldOf("base_height").forGetter($$0x -> $$0x.baseHeight),
         Codec.intRange(0, 24).fieldOf("height_rand_a").forGetter($$0x -> $$0x.heightRandA),
         Codec.intRange(0, 24).fieldOf("height_rand_b").forGetter($$0x -> $$0x.heightRandB)
      );
   }

   public TrunkPlacer(int $$0, int $$1, int $$2) {
      this.baseHeight = $$0;
      this.heightRandA = $$1;
      this.heightRandB = $$2;
   }

   protected abstract TrunkPlacerType<?> type();

   public abstract List<FoliagePlacer.FoliageAttachment> placeTrunk(
      net.minecraft.world.level.LevelSimulatedReader var1,
      BiConsumer<BlockPos, BlockState> var2,
      RandomSource var3,
      int var4,
      BlockPos var5,
      TreeConfiguration var6
   );

   public int getTreeHeight(RandomSource $$0) {
      return this.baseHeight + $$0.nextInt(this.heightRandA + 1) + $$0.nextInt(this.heightRandB + 1);
   }

   private static boolean isDirt(net.minecraft.world.level.LevelSimulatedReader $$0, BlockPos $$1) {
      return $$0.isStateAtPosition($$1, $$0x -> Feature.isDirt($$0x) && !$$0x.is(Blocks.GRASS_BLOCK) && !$$0x.is(Blocks.MYCELIUM));
   }

   protected static void setDirtAt(
      net.minecraft.world.level.LevelSimulatedReader $$0, BiConsumer<BlockPos, BlockState> $$1, RandomSource $$2, BlockPos $$3, TreeConfiguration $$4
   ) {
      if ($$4.forceDirt || !isDirt($$0, $$3)) {
         $$1.accept($$3, $$4.dirtProvider.getState($$2, $$3));
      }
   }

   protected boolean placeLog(
      net.minecraft.world.level.LevelSimulatedReader $$0, BiConsumer<BlockPos, BlockState> $$1, RandomSource $$2, BlockPos $$3, TreeConfiguration $$4
   ) {
      return this.placeLog($$0, $$1, $$2, $$3, $$4, Function.identity());
   }

   protected boolean placeLog(
      net.minecraft.world.level.LevelSimulatedReader $$0,
      BiConsumer<BlockPos, BlockState> $$1,
      RandomSource $$2,
      BlockPos $$3,
      TreeConfiguration $$4,
      Function<BlockState, BlockState> $$5
   ) {
      if (this.validTreePos($$0, $$3)) {
         $$1.accept($$3, $$5.apply($$4.trunkProvider.getState($$2, $$3)));
         return true;
      } else {
         return false;
      }
   }

   protected void placeLogIfFree(
      net.minecraft.world.level.LevelSimulatedReader $$0, BiConsumer<BlockPos, BlockState> $$1, RandomSource $$2, MutableBlockPos $$3, TreeConfiguration $$4
   ) {
      if (this.isFree($$0, $$3)) {
         this.placeLog($$0, $$1, $$2, $$3, $$4);
      }
   }

   protected boolean validTreePos(net.minecraft.world.level.LevelSimulatedReader $$0, BlockPos $$1) {
      return TreeFeature.validTreePos($$0, $$1);
   }

   public boolean isFree(net.minecraft.world.level.LevelSimulatedReader $$0, BlockPos $$1) {
      return this.validTreePos($$0, $$1) || $$0.isStateAtPosition($$1, $$0x -> $$0x.is(BlockTags.LOGS));
   }
}
