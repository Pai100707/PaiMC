package net.minecraft.world.scores;

import java.util.Objects;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;

public interface ReadOnlyScoreInfo {
   int value();

   boolean isLocked();

   
   NumberFormat numberFormat();

   default MutableComponent formatValue(NumberFormat $$0) {
      return Objects.requireNonNullElse(this.numberFormat(), $$0).format(this.value());
   }

   static MutableComponent safeFormatValue(net.minecraft.world.scores.ReadOnlyScoreInfo $$0, NumberFormat $$1) {
      return $$0 != null ? $$0.formatValue($$1) : $$1.format(0);
   }
}
