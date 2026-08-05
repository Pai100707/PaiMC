package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Direct;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface DensityFunction {
   Codec<DensityFunction> DIRECT_CODEC = DensityFunctions.DIRECT_CODEC;
   Codec<Holder<DensityFunction>> CODEC = RegistryFileCodec.create(Registries.DENSITY_FUNCTION, DIRECT_CODEC);
   Codec<DensityFunction> HOLDER_HELPER_CODEC = CODEC.xmap(
      DensityFunctions.HolderHolder::new, $$0 -> (Holder)($$0 instanceof DensityFunctions.HolderHolder $$1 ? $$1.function() : new Direct($$0))
   );

   double compute(DensityFunction.FunctionContext var1);

   void fillArray(double[] var1, DensityFunction.ContextProvider var2);

   DensityFunction mapAll(DensityFunction.Visitor var1);

   double minValue();

   double maxValue();

   KeyDispatchDataCodec<? extends DensityFunction> codec();

   default DensityFunction clamp(double $$0, double $$1) {
      return new DensityFunctions.Clamp(this, $$0, $$1);
   }

   default DensityFunction abs() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.ABS);
   }

   default DensityFunction square() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUARE);
   }

   default DensityFunction cube() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.CUBE);
   }

   default DensityFunction halfNegative() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.HALF_NEGATIVE);
   }

   default DensityFunction quarterNegative() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.QUARTER_NEGATIVE);
   }

   default DensityFunction invert() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.INVERT);
   }

   default DensityFunction squeeze() {
      return DensityFunctions.map(this, DensityFunctions.Mapped.Type.SQUEEZE);
   }

   public interface ContextProvider {
      DensityFunction.FunctionContext forIndex(int var1);

      void fillAllDirectly(double[] var1, DensityFunction var2);
   }

   public interface FunctionContext {
      int blockX();

      int blockY();

      int blockZ();

      default Blender getBlender() {
         return Blender.empty();
      }
   }

   public record NoiseHolder(Holder<NormalNoise.NoiseParameters> noiseData, NormalNoise noise) {
      public static final Codec<DensityFunction.NoiseHolder> CODEC = NormalNoise.NoiseParameters.CODEC
         .xmap($$0 -> new DensityFunction.NoiseHolder($$0, null), DensityFunction.NoiseHolder::noiseData);

      public NoiseHolder(Holder<NormalNoise.NoiseParameters> $$0) {
         this($$0, null);
      }

      public double getValue(double $$0, double $$1, double $$2) {
         return this.noise == null ? 0.0 : this.noise.getValue($$0, $$1, $$2);
      }

      public double maxValue() {
         return this.noise == null ? 2.0 : this.noise.maxValue();
      }
   }

   public interface SimpleFunction extends DensityFunction {
      @Override
      default void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
         $$1.fillAllDirectly($$0, this);
      }

      @Override
      default DensityFunction mapAll(DensityFunction.Visitor $$0) {
         return $$0.apply(this);
      }
   }

   public record SinglePointContext(int blockX, int blockY, int blockZ) implements DensityFunction.FunctionContext {
   }

   public interface Visitor {
      DensityFunction apply(DensityFunction var1);

      default DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder $$0) {
         return $$0;
      }
   }
}
