package net.minecraft.util.profiling.metrics;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;
import org.jspecify.annotations.Nullable;

public class MetricSampler {
   private final String name;
   private final MetricCategory category;
   private final DoubleSupplier sampler;
   private final ByteBuf ticks;
   private final ByteBuf values;
   private volatile boolean isRunning;
   @Nullable
   private final Runnable beforeTick;
   @Nullable
   final MetricSampler.ThresholdTest thresholdTest;
   private double currentValue;

   protected MetricSampler(String $$0, MetricCategory $$1, DoubleSupplier $$2, @Nullable Runnable $$3, @Nullable MetricSampler.ThresholdTest $$4) {
      this.name = $$0;
      this.category = $$1;
      this.beforeTick = $$3;
      this.sampler = $$2;
      this.thresholdTest = $$4;
      this.values = ByteBufAllocator.DEFAULT.buffer();
      this.ticks = ByteBufAllocator.DEFAULT.buffer();
      this.isRunning = true;
   }

   public static MetricSampler create(String $$0, MetricCategory $$1, DoubleSupplier $$2) {
      return new MetricSampler($$0, $$1, $$2, null, null);
   }

   public static <T> MetricSampler create(String $$0, MetricCategory $$1, T $$2, ToDoubleFunction<T> $$3) {
      return builder($$0, $$1, $$3, $$2).build();
   }

   public static <T> MetricSampler.MetricSamplerBuilder<T> builder(String $$0, MetricCategory $$1, ToDoubleFunction<T> $$2, T $$3) {
      if ($$2 == null) {
         throw new IllegalStateException();
      } else {
         return new MetricSampler.MetricSamplerBuilder<>($$0, $$1, $$2, $$3);
      }
   }

   public void onStartTick() {
      if (!this.isRunning) {
         throw new IllegalStateException("Not running");
      } else {
         if (this.beforeTick != null) {
            this.beforeTick.run();
         }
      }
   }

   public void onEndTick(int $$0) {
      this.verifyRunning();
      this.currentValue = this.sampler.getAsDouble();
      this.values.writeDouble(this.currentValue);
      this.ticks.writeInt($$0);
   }

   public void onFinished() {
      this.verifyRunning();
      this.values.release();
      this.ticks.release();
      this.isRunning = false;
   }

   private void verifyRunning() {
      if (!this.isRunning) {
         throw new IllegalStateException(String.format(Locale.ROOT, "Sampler for metric %s not started!", this.name));
      }
   }

   DoubleSupplier getSampler() {
      return this.sampler;
   }

   public String getName() {
      return this.name;
   }

   public MetricCategory getCategory() {
      return this.category;
   }

   public MetricSampler.SamplerResult result() {
      Int2DoubleMap $$0 = new Int2DoubleOpenHashMap();
      int $$1 = Integer.MIN_VALUE;
      int $$2 = Integer.MIN_VALUE;

      while (this.values.isReadable(8)) {
         int $$3 = this.ticks.readInt();
         if ($$1 == Integer.MIN_VALUE) {
            $$1 = $$3;
         }

         $$0.put($$3, this.values.readDouble());
         $$2 = $$3;
      }

      return new MetricSampler.SamplerResult($$1, $$2, $$0);
   }

   public boolean triggersThreshold() {
      return this.thresholdTest != null && this.thresholdTest.test(this.currentValue);
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else if ($$0 != null && this.getClass() == $$0.getClass()) {
         MetricSampler $$1 = (MetricSampler)$$0;
         return this.name.equals($$1.name) && this.category.equals($$1.category);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   public static class MetricSamplerBuilder<T> {
      private final String name;
      private final MetricCategory category;
      private final DoubleSupplier sampler;
      private final T context;
      @Nullable
      private Runnable beforeTick;
      @Nullable
      private MetricSampler.ThresholdTest thresholdTest;

      public MetricSamplerBuilder(String $$0, MetricCategory $$1, ToDoubleFunction<T> $$2, T $$3) {
         this.name = $$0;
         this.category = $$1;
         this.sampler = () -> $$2.applyAsDouble($$3);
         this.context = $$3;
      }

      public MetricSampler.MetricSamplerBuilder<T> withBeforeTick(Consumer<T> $$0) {
         this.beforeTick = () -> $$0.accept(this.context);
         return this;
      }

      public MetricSampler.MetricSamplerBuilder<T> withThresholdAlert(MetricSampler.ThresholdTest $$0) {
         this.thresholdTest = $$0;
         return this;
      }

      public MetricSampler build() {
         return new MetricSampler(this.name, this.category, this.sampler, this.beforeTick, this.thresholdTest);
      }
   }

   public static class SamplerResult {
      private final Int2DoubleMap recording;
      private final int firstTick;
      private final int lastTick;

      public SamplerResult(int $$0, int $$1, Int2DoubleMap $$2) {
         this.firstTick = $$0;
         this.lastTick = $$1;
         this.recording = $$2;
      }

      public double valueAtTick(int $$0) {
         return this.recording.get($$0);
      }

      public int getFirstTick() {
         return this.firstTick;
      }

      public int getLastTick() {
         return this.lastTick;
      }
   }

   public interface ThresholdTest {
      boolean test(double var1);
   }

   public static class ValueIncreasedByPercentage implements MetricSampler.ThresholdTest {
      private final float percentageIncreaseThreshold;
      private double previousValue = Double.MIN_VALUE;

      public ValueIncreasedByPercentage(float $$0) {
         this.percentageIncreaseThreshold = $$0;
      }

      @Override
      public boolean test(double $$0) {
         boolean $$2;
         if (this.previousValue != Double.MIN_VALUE && !($$0 <= this.previousValue)) {
            $$2 = ($$0 - this.previousValue) / this.previousValue >= this.percentageIncreaseThreshold;
         } else {
            $$2 = false;
         }

         this.previousValue = $$0;
         return $$2;
      }
   }
}
