package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public interface StatFormatter {
   DecimalFormat DECIMAL_FORMAT = new DecimalFormat("########0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));
   net.minecraft.stats.StatFormatter DEFAULT = NumberFormat.getIntegerInstance(Locale.US)::format;
   net.minecraft.stats.StatFormatter DIVIDE_BY_TEN = $$0 -> DECIMAL_FORMAT.format($$0 * 0.1);
   net.minecraft.stats.StatFormatter DISTANCE = $$0 -> {
      double $$1 = $$0 / 100.0;
      double $$2 = $$1 / 1000.0;
      if ($$2 > 0.5) {
         return DECIMAL_FORMAT.format($$2) + " km";
      } else {
         return $$1 > 0.5 ? DECIMAL_FORMAT.format($$1) + " m" : $$0 + " cm";
      }
   };
   net.minecraft.stats.StatFormatter TIME = $$0 -> {
      double $$1 = $$0 / 20.0;
      double $$2 = $$1 / 60.0;
      double $$3 = $$2 / 60.0;
      double $$4 = $$3 / 24.0;
      double $$5 = $$4 / 365.0;
      if ($$5 > 0.5) {
         return DECIMAL_FORMAT.format($$5) + " y";
      } else if ($$4 > 0.5) {
         return DECIMAL_FORMAT.format($$4) + " d";
      } else if ($$3 > 0.5) {
         return DECIMAL_FORMAT.format($$3) + " h";
      } else {
         return $$2 > 0.5 ? DECIMAL_FORMAT.format($$2) + " min" : $$1 + " s";
      }
   };

   String format(int var1);
}
