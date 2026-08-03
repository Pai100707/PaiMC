package net.minecraft.util.profiling.jfr.stats;

import jdk.jfr.consumer.RecordedEvent;

public record FpsStat(int fps) {
   public static FpsStat from(RecordedEvent $$0, String $$1) {
      return new FpsStat($$0.getInt($$1));
   }
}
