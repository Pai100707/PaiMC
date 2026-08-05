package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class WorldCarver<C extends CarverConfiguration> {
   public static final WorldCarver<CaveCarverConfiguration> CAVE = register("cave", new CaveWorldCarver(CaveCarverConfiguration.CODEC));
   public static final WorldCarver<CaveCarverConfiguration> NETHER_CAVE = register("nether_cave", new NetherWorldCarver(CaveCarverConfiguration.CODEC));
   public static final WorldCarver<CanyonCarverConfiguration> CANYON = register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
   protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
   protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
   protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
   protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
   protected Set<Fluid> liquids = ImmutableSet.of(Fluids.WATER);
   private final MapCodec<ConfiguredWorldCarver<C>> configuredCodec;

   private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String $$0, F $$1) {
      return (F)Registry.register(BuiltInRegistries.CARVER, $$0, $$1);
   }

   public WorldCarver(Codec<C> $$0) {
      this.configuredCodec = $$0.fieldOf("config").xmap(this::configured, ConfiguredWorldCarver::config);
   }

   public ConfiguredWorldCarver<C> configured(C $$0) {
      return new ConfiguredWorldCarver<>(this, $$0);
   }

   public MapCodec<ConfiguredWorldCarver<C>> configuredCodec() {
      return this.configuredCodec;
   }

   public int getRange() {
      return 4;
   }

   protected boolean carveEllipsoid(
      CarvingContext $$0,
      C $$1,
      ChunkAccess $$2,
      Function<BlockPos, Holder<Biome>> $$3,
      Aquifer $$4,
      double $$5,
      double $$6,
      double $$7,
      double $$8,
      double $$9,
      CarvingMask $$10,
      WorldCarver.CarveSkipChecker $$11
   ) {
      net.minecraft.world.level.ChunkPos $$12 = $$2.getPos();
      double $$13 = $$12.getMiddleBlockX();
      double $$14 = $$12.getMiddleBlockZ();
      double $$15 = 16.0 + $$8 * 2.0;
      if (!(Math.abs($$5 - $$13) > $$15) && !(Math.abs($$7 - $$14) > $$15)) {
         int $$16 = $$12.getMinBlockX();
         int $$17 = $$12.getMinBlockZ();
         int $$18 = Math.max(Mth.floor($$5 - $$8) - $$16 - 1, 0);
         int $$19 = Math.min(Mth.floor($$5 + $$8) - $$16, 15);
         int $$20 = Math.max(Mth.floor($$6 - $$9) - 1, $$0.getMinGenY() + 1);
         int $$21 = $$2.isUpgrading() ? 0 : 7;
         int $$22 = Math.min(Mth.floor($$6 + $$9) + 1, $$0.getMinGenY() + $$0.getGenDepth() - 1 - $$21);
         int $$23 = Math.max(Mth.floor($$7 - $$8) - $$17 - 1, 0);
         int $$24 = Math.min(Mth.floor($$7 + $$8) - $$17, 15);
         boolean $$25 = false;
         MutableBlockPos $$26 = new MutableBlockPos();
         MutableBlockPos $$27 = new MutableBlockPos();

         for (int $$28 = $$18; $$28 <= $$19; $$28++) {
            int $$29 = $$12.getBlockX($$28);
            double $$30 = ($$29 + 0.5 - $$5) / $$8;

            for (int $$31 = $$23; $$31 <= $$24; $$31++) {
               int $$32 = $$12.getBlockZ($$31);
               double $$33 = ($$32 + 0.5 - $$7) / $$8;
               if (!($$30 * $$30 + $$33 * $$33 >= 1.0)) {
                  MutableBoolean $$34 = new MutableBoolean(false);

                  for (int $$35 = $$22; $$35 > $$20; $$35--) {
                     double $$36 = ($$35 - 0.5 - $$6) / $$9;
                     if (!$$11.shouldSkip($$0, $$30, $$36, $$33, $$35) && (!$$10.get($$28, $$35, $$31) || isDebugEnabled($$1))) {
                        $$10.set($$28, $$35, $$31);
                        $$26.set($$29, $$35, $$32);
                        $$25 |= this.carveBlock($$0, $$1, $$2, $$3, $$10, $$26, $$27, $$4, $$34);
                     }
                  }
               }
            }
         }

         return $$25;
      } else {
         return false;
      }
   }

   protected boolean carveBlock(
      CarvingContext $$0,
      C $$1,
      ChunkAccess $$2,
      Function<BlockPos, Holder<Biome>> $$3,
      CarvingMask $$4,
      MutableBlockPos $$5,
      MutableBlockPos $$6,
      Aquifer $$7,
      MutableBoolean $$8
   ) {
      BlockState $$9 = $$2.getBlockState($$5);
      if ($$9.is(Blocks.GRASS_BLOCK) || $$9.is(Blocks.MYCELIUM)) {
         $$8.setTrue();
      }

      if (!this.canReplaceBlock($$1, $$9) && !isDebugEnabled($$1)) {
         return false;
      } else {
         BlockState $$10 = this.getCarveState($$0, $$1, $$5, $$7);
         if ($$10 == null) {
            return false;
         } else {
            $$2.setBlockState($$5, $$10);
            if ($$7.shouldScheduleFluidUpdate() && !$$10.getFluidState().isEmpty()) {
               $$2.markPosForPostprocessing($$5);
            }

            if ($$8.isTrue()) {
               $$6.setWithOffset($$5, Direction.DOWN);
               if ($$2.getBlockState($$6).is(Blocks.DIRT)) {
                  $$0.topMaterial($$3, $$2, $$6, !$$10.getFluidState().isEmpty()).ifPresent($$2x -> {
                     $$2.setBlockState($$6, $$2x);
                     if (!$$2x.getFluidState().isEmpty()) {
                        $$2.markPosForPostprocessing($$6);
                     }
                  });
               }
            }

            return true;
         }
      }
   }

   
   private BlockState getCarveState(CarvingContext $$0, C $$1, BlockPos $$2, Aquifer $$3) {
      if ($$2.getY() <= $$1.lavaLevel.resolveY($$0)) {
         return LAVA.createLegacyBlock();
      } else {
         BlockState $$4 = $$3.computeSubstance(new DensityFunction.SinglePointContext($$2.getX(), $$2.getY(), $$2.getZ()), 0.0);
         if ($$4 == null) {
            return isDebugEnabled($$1) ? $$1.debugSettings.getBarrierState() : null;
         } else {
            return isDebugEnabled($$1) ? getDebugState($$1, $$4) : $$4;
         }
      }
   }

   private static BlockState getDebugState(CarverConfiguration $$0, BlockState $$1) {
      if ($$1.is(Blocks.AIR)) {
         return $$0.debugSettings.getAirState();
      } else if ($$1.is(Blocks.WATER)) {
         BlockState $$2 = $$0.debugSettings.getWaterState();
         return $$2.hasProperty(BlockStateProperties.WATERLOGGED) ? $$2.setValue(BlockStateProperties.WATERLOGGED, true) : $$2;
      } else {
         return $$1.is(Blocks.LAVA) ? $$0.debugSettings.getLavaState() : $$1;
      }
   }

   public abstract boolean carve(
      CarvingContext var1,
      C var2,
      ChunkAccess var3,
      Function<BlockPos, Holder<Biome>> var4,
      RandomSource var5,
      Aquifer var6,
      net.minecraft.world.level.ChunkPos var7,
      CarvingMask var8
   );

   public abstract boolean isStartChunk(C var1, RandomSource var2);

   protected boolean canReplaceBlock(C $$0, BlockState $$1) {
      return $$1.is($$0.replaceable);
   }

   protected static boolean canReach(net.minecraft.world.level.ChunkPos $$0, double $$1, double $$2, int $$3, int $$4, float $$5) {
      double $$6 = $$0.getMiddleBlockX();
      double $$7 = $$0.getMiddleBlockZ();
      double $$8 = $$1 - $$6;
      double $$9 = $$2 - $$7;
      double $$10 = $$4 - $$3;
      double $$11 = $$5 + 2.0F + 16.0F;
      return $$8 * $$8 + $$9 * $$9 - $$10 * $$10 <= $$11 * $$11;
   }

   private static boolean isDebugEnabled(CarverConfiguration $$0) {
      return SharedConstants.DEBUG_CARVERS || $$0.debugSettings.isDebugMode();
   }

   public interface CarveSkipChecker {
      boolean shouldSkip(CarvingContext var1, double var2, double var4, double var6, int var8);
   }
}
