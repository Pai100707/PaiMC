package net.minecraft.util.profiling;

import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public class Zone implements AutoCloseable {
   public static final Zone INACTIVE = new Zone(null);
   @Nullable
   private final ProfilerFiller profiler;

   Zone(@Nullable ProfilerFiller $$0) {
      this.profiler = $$0;
   }

   public Zone addText(String $$0) {
      if (this.profiler != null) {
         this.profiler.addZoneText($$0);
      }

      return this;
   }

   public Zone addText(Supplier<String> $$0) {
      if (this.profiler != null) {
         this.profiler.addZoneText($$0.get());
      }

      return this;
   }

   public Zone addValue(long $$0) {
      if (this.profiler != null) {
         this.profiler.addZoneValue($$0);
      }

      return this;
   }

   public Zone setColor(int $$0) {
      if (this.profiler != null) {
         this.profiler.setZoneColor($$0);
      }

      return this;
   }

   @Override
   public void close() {
      if (this.profiler != null) {
         this.profiler.pop();
      }
   }
}
