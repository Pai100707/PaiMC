package net.minecraft.world.level.levelgen;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class RandomState {
   final PositionalRandomFactory random;
   private final HolderGetter<NormalNoise.NoiseParameters> noises;
   private final NoiseRouter router;
   private final Climate.Sampler sampler;
   private final SurfaceSystem surfaceSystem;
   private final PositionalRandomFactory aquiferRandom;
   private final PositionalRandomFactory oreRandom;
   private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances;
   private final Map<Identifier, PositionalRandomFactory> positionalRandoms;

   public static RandomState create(Provider $$0, ResourceKey<NoiseGeneratorSettings> $$1, long $$2) {
      return create((NoiseGeneratorSettings)$$0.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow($$1).value(), $$0.lookupOrThrow(Registries.NOISE), $$2);
   }

   public static RandomState create(NoiseGeneratorSettings $$0, HolderGetter<NormalNoise.NoiseParameters> $$1, long $$2) {
      return new RandomState($$0, $$1, $$2);
   }

   private RandomState(NoiseGeneratorSettings $$0, HolderGetter<NormalNoise.NoiseParameters> $$1, final long $$2) {
      this.random = $$0.getRandomSource().newInstance($$2).forkPositional();
      this.noises = $$1;
      this.aquiferRandom = this.random.fromHashOf(Identifier.withDefaultNamespace("aquifer")).forkPositional();
      this.oreRandom = this.random.fromHashOf(Identifier.withDefaultNamespace("ore")).forkPositional();
      this.noiseIntances = new ConcurrentHashMap<>();
      this.positionalRandoms = new ConcurrentHashMap<>();
      this.surfaceSystem = new SurfaceSystem(this, $$0.defaultBlock(), $$0.seaLevel(), this.random);
      final boolean $$3 = $$0.useLegacyRandomSource();

      class NoiseWiringHelper implements DensityFunction.Visitor {
         private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();

         private RandomSource newLegacyInstance(long $$0) {
            return new LegacyRandomSource($$2 + $$0);
         }

         @Override
         public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder $$0) {
            Holder<NormalNoise.NoiseParameters> $$1 = $$0.noiseData();
            if ($$3) {
               if ($$1.is(Noises.TEMPERATURE)) {
                  NormalNoise $$2 = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(0L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
                  return new DensityFunction.NoiseHolder($$1, $$2);
               }

               if ($$1.is(Noises.VEGETATION)) {
                  NormalNoise $$3 = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(1L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
                  return new DensityFunction.NoiseHolder($$1, $$3);
               }

               if ($$1.is(Noises.SHIFT)) {
                  NormalNoise $$4 = NormalNoise.create(RandomState.this.random.fromHashOf(Noises.SHIFT.identifier()), new NormalNoise.NoiseParameters(0, 0.0));
                  return new DensityFunction.NoiseHolder($$1, $$4);
               }
            }

            NormalNoise $$5 = RandomState.this.getOrCreateNoise((ResourceKey<NormalNoise.NoiseParameters>)$$1.unwrapKey().orElseThrow());
            return new DensityFunction.NoiseHolder($$1, $$5);
         }

         private DensityFunction wrapNew(DensityFunction $$0) {
            if ($$0 instanceof BlendedNoise $$1) {
               RandomSource $$2 = $$3 ? this.newLegacyInstance(0L) : RandomState.this.random.fromHashOf(Identifier.withDefaultNamespace("terrain"));
               return $$1.withNewRandom($$2);
            } else {
               return (DensityFunction)($$0 instanceof DensityFunctions.EndIslandDensityFunction ? new DensityFunctions.EndIslandDensityFunction($$2) : $$0);
            }
         }

         @Override
         public DensityFunction apply(DensityFunction $$0) {
            return this.wrapped.computeIfAbsent($$0, this::wrapNew);
         }
      }

      this.router = $$0.noiseRouter().mapAll(new NoiseWiringHelper());
      DensityFunction.Visitor $$4 = new DensityFunction.Visitor() {
         private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();

         private DensityFunction wrapNew(DensityFunction $$0) {
            if ($$0 instanceof DensityFunctions.HolderHolder $$1x) {
               return (DensityFunction)$$1x.function().value();
            } else {
               return $$0 instanceof DensityFunctions.Marker $$2x ? $$2x.wrapped() : $$0;
            }
         }

         @Override
         public DensityFunction apply(DensityFunction $$0) {
            return this.wrapped.computeIfAbsent($$0, this::wrapNew);
         }
      };
      this.sampler = new Climate.Sampler(
         this.router.temperature().mapAll($$4),
         this.router.vegetation().mapAll($$4),
         this.router.continents().mapAll($$4),
         this.router.erosion().mapAll($$4),
         this.router.depth().mapAll($$4),
         this.router.ridges().mapAll($$4),
         $$0.spawnTarget()
      );
   }

   public NormalNoise getOrCreateNoise(ResourceKey<NormalNoise.NoiseParameters> $$0) {
      return this.noiseIntances.computeIfAbsent($$0, $$1 -> Noises.instantiate(this.noises, this.random, $$0));
   }

   public PositionalRandomFactory getOrCreateRandomFactory(Identifier $$0) {
      return this.positionalRandoms.computeIfAbsent($$0, $$1 -> this.random.fromHashOf($$0).forkPositional());
   }

   public NoiseRouter router() {
      return this.router;
   }

   public Climate.Sampler sampler() {
      return this.sampler;
   }

   public SurfaceSystem surfaceSystem() {
      return this.surfaceSystem;
   }

   public PositionalRandomFactory aquiferRandom() {
      return this.aquiferRandom;
   }

   public PositionalRandomFactory oreRandom() {
      return this.oreRandom;
   }
}
