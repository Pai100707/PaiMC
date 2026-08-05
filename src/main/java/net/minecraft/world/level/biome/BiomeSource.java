package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public abstract class BiomeSource implements BiomeResolver {
   public static final Codec<BiomeSource> CODEC = BuiltInRegistries.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
   private final Supplier<Set<Holder<Biome>>> possibleBiomes = Suppliers.memoize(
      () -> this.collectPossibleBiomes().distinct().collect(ImmutableSet.toImmutableSet())
   );

   protected BiomeSource() {
   }

   protected abstract MapCodec<? extends BiomeSource> codec();

   protected abstract Stream<Holder<Biome>> collectPossibleBiomes();

   public Set<Holder<Biome>> possibleBiomes() {
      return this.possibleBiomes.get();
   }

   public Set<Holder<Biome>> getBiomesWithin(int $$0, int $$1, int $$2, int $$3, Climate.Sampler $$4) {
      int $$5 = QuartPos.fromBlock($$0 - $$3);
      int $$6 = QuartPos.fromBlock($$1 - $$3);
      int $$7 = QuartPos.fromBlock($$2 - $$3);
      int $$8 = QuartPos.fromBlock($$0 + $$3);
      int $$9 = QuartPos.fromBlock($$1 + $$3);
      int $$10 = QuartPos.fromBlock($$2 + $$3);
      int $$11 = $$8 - $$5 + 1;
      int $$12 = $$9 - $$6 + 1;
      int $$13 = $$10 - $$7 + 1;
      Set<Holder<Biome>> $$14 = Sets.newHashSet();

      for (int $$15 = 0; $$15 < $$13; $$15++) {
         for (int $$16 = 0; $$16 < $$11; $$16++) {
            for (int $$17 = 0; $$17 < $$12; $$17++) {
               int $$18 = $$5 + $$16;
               int $$19 = $$6 + $$17;
               int $$20 = $$7 + $$15;
               $$14.add(this.getNoiseBiome($$18, $$19, $$20, $$4));
            }
         }
      }

      return $$14;
   }

   
   public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(
      int $$0, int $$1, int $$2, int $$3, Predicate<Holder<Biome>> $$4, RandomSource $$5, Climate.Sampler $$6
   ) {
      return this.findBiomeHorizontal($$0, $$1, $$2, $$3, 1, $$4, $$5, false, $$6);
   }

   
   public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(
      BlockPos $$0, int $$1, int $$2, int $$3, Predicate<Holder<Biome>> $$4, Climate.Sampler $$5, net.minecraft.world.level.LevelReader $$6
   ) {
      Set<Holder<Biome>> $$7 = this.possibleBiomes().stream().filter($$4).collect(Collectors.toUnmodifiableSet());
      if ($$7.isEmpty()) {
         return null;
      } else {
         int $$8 = Math.floorDiv($$1, $$2);
         int[] $$9 = Mth.outFromOrigin($$0.getY(), $$6.getMinY() + 1, $$6.getMaxY() + 1, $$3).toArray();

         for (MutableBlockPos $$10 : BlockPos.spiralAround(BlockPos.ZERO, $$8, Direction.EAST, Direction.SOUTH)) {
            int $$11 = $$0.getX() + $$10.getX() * $$2;
            int $$12 = $$0.getZ() + $$10.getZ() * $$2;
            int $$13 = QuartPos.fromBlock($$11);
            int $$14 = QuartPos.fromBlock($$12);

            for (int $$15 : $$9) {
               int $$16 = QuartPos.fromBlock($$15);
               Holder<Biome> $$17 = this.getNoiseBiome($$13, $$16, $$14, $$5);
               if ($$7.contains($$17)) {
                  return Pair.of(new BlockPos($$11, $$15, $$12), $$17);
               }
            }
         }

         return null;
      }
   }

   
   public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(
      int $$0, int $$1, int $$2, int $$3, int $$4, Predicate<Holder<Biome>> $$5, RandomSource $$6, boolean $$7, Climate.Sampler $$8
   ) {
      int $$9 = QuartPos.fromBlock($$0);
      int $$10 = QuartPos.fromBlock($$2);
      int $$11 = QuartPos.fromBlock($$3);
      int $$12 = QuartPos.fromBlock($$1);
      Pair<BlockPos, Holder<Biome>> $$13 = null;
      int $$14 = 0;
      int $$15 = $$7 ? 0 : $$11;
      int $$16 = $$15;

      while ($$16 <= $$11) {
         for (int $$17 = !SharedConstants.DEBUG_ONLY_GENERATE_HALF_THE_WORLD && !SharedConstants.debugGenerateSquareTerrainWithoutNoise ? -$$16 : 0;
            $$17 <= $$16;
            $$17 += $$4
         ) {
            boolean $$18 = Math.abs($$17) == $$16;

            for (int $$19 = -$$16; $$19 <= $$16; $$19 += $$4) {
               if ($$7) {
                  boolean $$20 = Math.abs($$19) == $$16;
                  if (!$$20 && !$$18) {
                     continue;
                  }
               }

               int $$21 = $$9 + $$19;
               int $$22 = $$10 + $$17;
               Holder<Biome> $$23 = this.getNoiseBiome($$21, $$12, $$22, $$8);
               if ($$5.test($$23)) {
                  if ($$13 == null || $$6.nextInt($$14 + 1) == 0) {
                     BlockPos $$24 = new BlockPos(QuartPos.toBlock($$21), $$1, QuartPos.toBlock($$22));
                     if ($$7) {
                        return Pair.of($$24, $$23);
                     }

                     $$13 = Pair.of($$24, $$23);
                  }

                  $$14++;
               }
            }
         }

         $$16 += $$4;
      }

      return $$13;
   }

   @Override
   public abstract Holder<Biome> getNoiseBiome(int var1, int var2, int var3, Climate.Sampler var4);

   public void addDebugInfo(List<String> $$0, BlockPos $$1, Climate.Sampler $$2) {
   }
}
