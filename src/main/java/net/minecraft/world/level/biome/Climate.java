package net.minecraft.world.level.biome;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.jspecify.annotations.Nullable;

public class Climate {
   private static final boolean DEBUG_SLOW_BIOME_SEARCH = false;
   private static final float QUANTIZATION_FACTOR = 10000.0F;
   @VisibleForTesting
   protected static final int PARAMETER_COUNT = 7;

   public static Climate.TargetPoint target(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5) {
      return new Climate.TargetPoint(quantizeCoord($$0), quantizeCoord($$1), quantizeCoord($$2), quantizeCoord($$3), quantizeCoord($$4), quantizeCoord($$5));
   }

   public static Climate.ParameterPoint parameters(float $$0, float $$1, float $$2, float $$3, float $$4, float $$5, float $$6) {
      return new Climate.ParameterPoint(
         Climate.Parameter.point($$0),
         Climate.Parameter.point($$1),
         Climate.Parameter.point($$2),
         Climate.Parameter.point($$3),
         Climate.Parameter.point($$4),
         Climate.Parameter.point($$5),
         quantizeCoord($$6)
      );
   }

   public static Climate.ParameterPoint parameters(
      Climate.Parameter $$0, Climate.Parameter $$1, Climate.Parameter $$2, Climate.Parameter $$3, Climate.Parameter $$4, Climate.Parameter $$5, float $$6
   ) {
      return new Climate.ParameterPoint($$0, $$1, $$2, $$3, $$4, $$5, quantizeCoord($$6));
   }

   public static long quantizeCoord(float $$0) {
      return (long)($$0 * 10000.0F);
   }

   public static float unquantizeCoord(long $$0) {
      return (float)$$0 / 10000.0F;
   }

   public static Climate.Sampler empty() {
      DensityFunction $$0 = DensityFunctions.zero();
      return new Climate.Sampler($$0, $$0, $$0, $$0, $$0, $$0, List.of());
   }

   public static BlockPos findSpawnPosition(List<Climate.ParameterPoint> $$0, Climate.Sampler $$1) {
      return (new Climate.SpawnFinder($$0, $$1)).result.location();
   }

   interface DistanceMetric<T> {
      long distance(Climate.RTree.Node<T> var1, long[] var2);
   }

   public record Parameter(long min, long max) {
      public static final Codec<Climate.Parameter> CODEC = ExtraCodecs.intervalCodec(
         Codec.floatRange(-2.0F, 2.0F),
         "min",
         "max",
         ($$0, $$1) -> $$0.compareTo($$1) > 0
            ? DataResult.error(() -> "Cannon construct interval, min > max (" + $$0 + " > " + $$1 + ")")
            : DataResult.success(new Climate.Parameter(Climate.quantizeCoord($$0), Climate.quantizeCoord($$1))),
         $$0 -> Climate.unquantizeCoord($$0.min()),
         $$0 -> Climate.unquantizeCoord($$0.max())
      );

      public static Climate.Parameter point(float $$0) {
         return span($$0, $$0);
      }

      public static Climate.Parameter span(float $$0, float $$1) {
         if ($$0 > $$1) {
            throw new IllegalArgumentException("min > max: " + $$0 + " " + $$1);
         } else {
            return new Climate.Parameter(Climate.quantizeCoord($$0), Climate.quantizeCoord($$1));
         }
      }

      public static Climate.Parameter span(Climate.Parameter $$0, Climate.Parameter $$1) {
         if ($$0.min() > $$1.max()) {
            throw new IllegalArgumentException("min > max: " + $$0 + " " + $$1);
         } else {
            return new Climate.Parameter($$0.min(), $$1.max());
         }
      }

      @Override
      public String toString() {
         return this.min == this.max ? String.format(Locale.ROOT, "%d", this.min) : String.format(Locale.ROOT, "[%d-%d]", this.min, this.max);
      }

      public long distance(long $$0) {
         long $$1 = $$0 - this.max;
         long $$2 = this.min - $$0;
         return $$1 > 0L ? $$1 : Math.max($$2, 0L);
      }

      public long distance(Climate.Parameter $$0) {
         long $$1 = $$0.min() - this.max;
         long $$2 = this.min - $$0.max();
         return $$1 > 0L ? $$1 : Math.max($$2, 0L);
      }

      public Climate.Parameter span(@Nullable Climate.Parameter $$0) {
         return $$0 == null ? this : new Climate.Parameter(Math.min(this.min, $$0.min()), Math.max(this.max, $$0.max()));
      }
   }

   public static class ParameterList<T> {
      private final List<Pair<Climate.ParameterPoint, T>> values;
      private final Climate.RTree<T> index;

      public static <T> Codec<Climate.ParameterList<T>> codec(MapCodec<T> $$0) {
         return ExtraCodecs.nonEmptyList(
               RecordCodecBuilder.create(
                     $$1 -> $$1.group(Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), $$0.forGetter(Pair::getSecond))
                        .apply($$1, Pair::of)
                  )
                  .listOf()
            )
            .xmap(Climate.ParameterList::new, Climate.ParameterList::values);
      }

      public ParameterList(List<Pair<Climate.ParameterPoint, T>> $$0) {
         this.values = $$0;
         this.index = Climate.RTree.create($$0);
      }

      public List<Pair<Climate.ParameterPoint, T>> values() {
         return this.values;
      }

      public T findValue(Climate.TargetPoint $$0) {
         return this.findValueIndex($$0);
      }

      @VisibleForTesting
      public T findValueBruteForce(Climate.TargetPoint $$0) {
         Iterator<Pair<Climate.ParameterPoint, T>> $$1 = this.values().iterator();
         Pair<Climate.ParameterPoint, T> $$2 = $$1.next();
         long $$3 = ((Climate.ParameterPoint)$$2.getFirst()).fitness($$0);
         T $$4 = (T)$$2.getSecond();

         while ($$1.hasNext()) {
            Pair<Climate.ParameterPoint, T> $$5 = $$1.next();
            long $$6 = ((Climate.ParameterPoint)$$5.getFirst()).fitness($$0);
            if ($$6 < $$3) {
               $$3 = $$6;
               $$4 = (T)$$5.getSecond();
            }
         }

         return $$4;
      }

      public T findValueIndex(Climate.TargetPoint $$0) {
         return this.findValueIndex($$0, Climate.RTree.Node::distance);
      }

      protected T findValueIndex(Climate.TargetPoint $$0, Climate.DistanceMetric<T> $$1) {
         return this.index.search($$0, $$1);
      }
   }

   public record ParameterPoint(
      Climate.Parameter temperature,
      Climate.Parameter humidity,
      Climate.Parameter continentalness,
      Climate.Parameter erosion,
      Climate.Parameter depth,
      Climate.Parameter weirdness,
      long offset
   ) {
      public static final Codec<Climate.ParameterPoint> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Climate.Parameter.CODEC.fieldOf("temperature").forGetter($$0x -> $$0x.temperature),
               Climate.Parameter.CODEC.fieldOf("humidity").forGetter($$0x -> $$0x.humidity),
               Climate.Parameter.CODEC.fieldOf("continentalness").forGetter($$0x -> $$0x.continentalness),
               Climate.Parameter.CODEC.fieldOf("erosion").forGetter($$0x -> $$0x.erosion),
               Climate.Parameter.CODEC.fieldOf("depth").forGetter($$0x -> $$0x.depth),
               Climate.Parameter.CODEC.fieldOf("weirdness").forGetter($$0x -> $$0x.weirdness),
               Codec.floatRange(0.0F, 1.0F).fieldOf("offset").xmap(Climate::quantizeCoord, Climate::unquantizeCoord).forGetter($$0x -> $$0x.offset)
            )
            .apply($$0, Climate.ParameterPoint::new)
      );

      long fitness(Climate.TargetPoint $$0) {
         return Mth.square(this.temperature.distance($$0.temperature))
            + Mth.square(this.humidity.distance($$0.humidity))
            + Mth.square(this.continentalness.distance($$0.continentalness))
            + Mth.square(this.erosion.distance($$0.erosion))
            + Mth.square(this.depth.distance($$0.depth))
            + Mth.square(this.weirdness.distance($$0.weirdness))
            + Mth.square(this.offset);
      }

      protected List<Climate.Parameter> parameterSpace() {
         return ImmutableList.of(
            this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, new Climate.Parameter(this.offset, this.offset)
         );
      }
   }

   protected static final class RTree<T> {
      private static final int CHILDREN_PER_NODE = 6;
      private final Climate.RTree.Node<T> root;
      private final ThreadLocal<Climate.RTree.Leaf<T>> lastResult = new ThreadLocal<>();

      private RTree(Climate.RTree.Node<T> $$0) {
         this.root = $$0;
      }

      public static <T> Climate.RTree<T> create(List<Pair<Climate.ParameterPoint, T>> $$0) {
         if ($$0.isEmpty()) {
            throw new IllegalArgumentException("Need at least one value to build the search tree.");
         } else {
            int $$1 = ((Climate.ParameterPoint)$$0.get(0).getFirst()).parameterSpace().size();
            if ($$1 != 7) {
               throw new IllegalStateException("Expecting parameter space to be 7, got " + $$1);
            } else {
               List<Climate.RTree.Leaf<T>> $$2 = $$0.stream()
                  .map($$0x -> new Climate.RTree.Leaf<>((Climate.ParameterPoint)$$0x.getFirst(), $$0x.getSecond()))
                  .collect(Collectors.toCollection(ArrayList::new));
               return new Climate.RTree<>(build($$1, $$2));
            }
         }
      }

      private static <T> Climate.RTree.Node<T> build(int $$0, List<? extends Climate.RTree.Node<T>> $$1) {
         if ($$1.isEmpty()) {
            throw new IllegalStateException("Need at least one child to build a node");
         } else if ($$1.size() == 1) {
            return (Climate.RTree.Node<T>)$$1.get(0);
         } else if ($$1.size() <= 6) {
            $$1.sort(Comparator.comparingLong($$1x -> {
               long $$2x = 0L;

               for (int $$3x = 0; $$3x < $$0; $$3x++) {
                  Climate.Parameter $$4x = $$1x.parameterSpace[$$3x];
                  $$2x += Math.abs(($$4x.min() + $$4x.max()) / 2L);
               }

               return $$2x;
            }));
            return new Climate.RTree.SubTree<>($$1);
         } else {
            long $$2 = Long.MAX_VALUE;
            int $$3 = -1;
            List<Climate.RTree.SubTree<T>> $$4 = null;

            for (int $$5 = 0; $$5 < $$0; $$5++) {
               sort($$1, $$0, $$5, false);
               List<Climate.RTree.SubTree<T>> $$6 = bucketize($$1);
               long $$7 = 0L;

               for (Climate.RTree.SubTree<T> $$8 : $$6) {
                  $$7 += cost($$8.parameterSpace);
               }

               if ($$2 > $$7) {
                  $$2 = $$7;
                  $$3 = $$5;
                  $$4 = $$6;
               }
            }

            sort($$4, $$0, $$3, true);
            return new Climate.RTree.SubTree<>($$4.stream().map($$1x -> build($$0, Arrays.asList($$1x.children))).collect(Collectors.toList()));
         }
      }

      private static <T> void sort(List<? extends Climate.RTree.Node<T>> $$0, int $$1, int $$2, boolean $$3) {
         Comparator<Climate.RTree.Node<T>> $$4 = comparator($$2, $$3);

         for (int $$5 = 1; $$5 < $$1; $$5++) {
            $$4 = $$4.thenComparing(comparator(($$2 + $$5) % $$1, $$3));
         }

         $$0.sort($$4);
      }

      private static <T> Comparator<Climate.RTree.Node<T>> comparator(int $$0, boolean $$1) {
         return Comparator.comparingLong($$2 -> {
            Climate.Parameter $$3 = $$2.parameterSpace[$$0];
            long $$4 = ($$3.min() + $$3.max()) / 2L;
            return $$1 ? Math.abs($$4) : $$4;
         });
      }

      private static <T> List<Climate.RTree.SubTree<T>> bucketize(List<? extends Climate.RTree.Node<T>> $$0) {
         List<Climate.RTree.SubTree<T>> $$1 = Lists.newArrayList();
         List<Climate.RTree.Node<T>> $$2 = Lists.newArrayList();
         int $$3 = (int)Math.pow(6.0, Math.floor(Math.log($$0.size() - 0.01) / Math.log(6.0)));

         for (Climate.RTree.Node<T> $$4 : $$0) {
            $$2.add($$4);
            if ($$2.size() >= $$3) {
               $$1.add(new Climate.RTree.SubTree<>($$2));
               $$2 = Lists.newArrayList();
            }
         }

         if (!$$2.isEmpty()) {
            $$1.add(new Climate.RTree.SubTree<>($$2));
         }

         return $$1;
      }

      private static long cost(Climate.Parameter[] $$0) {
         long $$1 = 0L;

         for (Climate.Parameter $$2 : $$0) {
            $$1 += Math.abs($$2.max() - $$2.min());
         }

         return $$1;
      }

      static <T> List<Climate.Parameter> buildParameterSpace(List<? extends Climate.RTree.Node<T>> $$0) {
         if ($$0.isEmpty()) {
            throw new IllegalArgumentException("SubTree needs at least one child");
         } else {
            int $$1 = 7;
            List<Climate.Parameter> $$2 = Lists.newArrayList();

            for (int $$3 = 0; $$3 < 7; $$3++) {
               $$2.add(null);
            }

            for (Climate.RTree.Node<T> $$4 : $$0) {
               for (int $$5 = 0; $$5 < 7; $$5++) {
                  $$2.set($$5, $$4.parameterSpace[$$5].span($$2.get($$5)));
               }
            }

            return $$2;
         }
      }

      public T search(Climate.TargetPoint $$0, Climate.DistanceMetric<T> $$1) {
         long[] $$2 = $$0.toParameterArray();
         Climate.RTree.Leaf<T> $$3 = this.root.search($$2, this.lastResult.get(), $$1);
         this.lastResult.set($$3);
         return $$3.value;
      }

      static final class Leaf<T> extends Climate.RTree.Node<T> {
         final T value;

         Leaf(Climate.ParameterPoint $$0, T $$1) {
            super($$0.parameterSpace());
            this.value = $$1;
         }

         @Override
         protected Climate.RTree.Leaf<T> search(long[] $$0, @Nullable Climate.RTree.Leaf<T> $$1, Climate.DistanceMetric<T> $$2) {
            return this;
         }
      }

      abstract static class Node<T> {
         protected final Climate.Parameter[] parameterSpace;

         protected Node(List<Climate.Parameter> $$0) {
            this.parameterSpace = $$0.toArray(new Climate.Parameter[0]);
         }

         protected abstract Climate.RTree.Leaf<T> search(long[] var1, @Nullable Climate.RTree.Leaf<T> var2, Climate.DistanceMetric<T> var3);

         protected long distance(long[] $$0) {
            long $$1 = 0L;

            for (int $$2 = 0; $$2 < 7; $$2++) {
               $$1 += Mth.square(this.parameterSpace[$$2].distance($$0[$$2]));
            }

            return $$1;
         }

         @Override
         public String toString() {
            return Arrays.toString((Object[])this.parameterSpace);
         }
      }

      static final class SubTree<T> extends Climate.RTree.Node<T> {
         final Climate.RTree.Node<T>[] children;

         protected SubTree(List<? extends Climate.RTree.Node<T>> $$0) {
            this(Climate.RTree.buildParameterSpace($$0), $$0);
         }

         protected SubTree(List<Climate.Parameter> $$0, List<? extends Climate.RTree.Node<T>> $$1) {
            super($$0);
            this.children = $$1.toArray(new Climate.RTree.Node[0]);
         }

         @Override
         protected Climate.RTree.Leaf<T> search(long[] $$0, @Nullable Climate.RTree.Leaf<T> $$1, Climate.DistanceMetric<T> $$2) {
            long $$3 = $$1 == null ? Long.MAX_VALUE : $$2.distance($$1, $$0);
            Climate.RTree.Leaf<T> $$4 = $$1;

            for (Climate.RTree.Node<T> $$5 : this.children) {
               long $$6 = $$2.distance($$5, $$0);
               if ($$3 > $$6) {
                  Climate.RTree.Leaf<T> $$7 = $$5.search($$0, $$4, $$2);
                  long $$8 = $$5 == $$7 ? $$6 : $$2.distance($$7, $$0);
                  if ($$3 > $$8) {
                     $$3 = $$8;
                     $$4 = $$7;
                  }
               }
            }

            return $$4;
         }
      }
   }

   public record Sampler(
      DensityFunction temperature,
      DensityFunction humidity,
      DensityFunction continentalness,
      DensityFunction erosion,
      DensityFunction depth,
      DensityFunction weirdness,
      List<Climate.ParameterPoint> spawnTarget
   ) {
      public Climate.TargetPoint sample(int $$0, int $$1, int $$2) {
         int $$3 = QuartPos.toBlock($$0);
         int $$4 = QuartPos.toBlock($$1);
         int $$5 = QuartPos.toBlock($$2);
         DensityFunction.SinglePointContext $$6 = new DensityFunction.SinglePointContext($$3, $$4, $$5);
         return Climate.target(
            (float)this.temperature.compute($$6),
            (float)this.humidity.compute($$6),
            (float)this.continentalness.compute($$6),
            (float)this.erosion.compute($$6),
            (float)this.depth.compute($$6),
            (float)this.weirdness.compute($$6)
         );
      }

      public BlockPos findSpawnPosition() {
         return this.spawnTarget.isEmpty() ? BlockPos.ZERO : Climate.findSpawnPosition(this.spawnTarget, this);
      }
   }

   static class SpawnFinder {
      private static final long MAX_RADIUS = 2048L;
      Climate.SpawnFinder.Result result;

      SpawnFinder(List<Climate.ParameterPoint> $$0, Climate.Sampler $$1) {
         this.result = getSpawnPositionAndFitness($$0, $$1, 0, 0);
         this.radialSearch($$0, $$1, 2048.0F, 512.0F);
         this.radialSearch($$0, $$1, 512.0F, 32.0F);
      }

      private void radialSearch(List<Climate.ParameterPoint> $$0, Climate.Sampler $$1, float $$2, float $$3) {
         float $$4 = 0.0F;
         float $$5 = $$3;
         BlockPos $$6 = this.result.location();

         while ($$5 <= $$2) {
            int $$7 = $$6.getX() + (int)(Math.sin($$4) * $$5);
            int $$8 = $$6.getZ() + (int)(Math.cos($$4) * $$5);
            Climate.SpawnFinder.Result $$9 = getSpawnPositionAndFitness($$0, $$1, $$7, $$8);
            if ($$9.fitness() < this.result.fitness()) {
               this.result = $$9;
            }

            $$4 += $$3 / $$5;
            if ($$4 > Math.PI * 2) {
               $$4 = 0.0F;
               $$5 += $$3;
            }
         }
      }

      private static Climate.SpawnFinder.Result getSpawnPositionAndFitness(List<Climate.ParameterPoint> $$0, Climate.Sampler $$1, int $$2, int $$3) {
         Climate.TargetPoint $$4 = $$1.sample(QuartPos.fromBlock($$2), 0, QuartPos.fromBlock($$3));
         Climate.TargetPoint $$5 = new Climate.TargetPoint($$4.temperature(), $$4.humidity(), $$4.continentalness(), $$4.erosion(), 0L, $$4.weirdness());
         long $$6 = Long.MAX_VALUE;

         for (Climate.ParameterPoint $$7 : $$0) {
            $$6 = Math.min($$6, $$7.fitness($$5));
         }

         long $$8 = Mth.square($$2) + Mth.square($$3);
         long $$9 = $$6 * Mth.square(2048L) + $$8;
         return new Climate.SpawnFinder.Result(new BlockPos($$2, 0, $$3), $$9);
      }

      record Result(BlockPos location, long fitness) {
      }
   }

   public record TargetPoint(long temperature, long humidity, long continentalness, long erosion, long depth, long weirdness) {

      @VisibleForTesting
      protected long[] toParameterArray() {
         return new long[]{this.temperature, this.humidity, this.continentalness, this.erosion, this.depth, this.weirdness, 0L};
      }
   }
}
