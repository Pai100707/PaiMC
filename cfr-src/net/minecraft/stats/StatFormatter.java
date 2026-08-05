/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public interface StatFormatter {
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("########0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));
    public static final StatFormatter DEFAULT = NumberFormat.getIntegerInstance(Locale.US)::format;
    public static final StatFormatter DIVIDE_BY_TEN = $$0 -> DECIMAL_FORMAT.format((double)$$0 * 0.1);
    public static final StatFormatter DISTANCE = $$0 -> {
        double $$1 = (double)$$0 / 100.0;
        double $$2 = $$1 / 1000.0;
        if ($$2 > 0.5) {
            return DECIMAL_FORMAT.format($$2) + " km";
        }
        if ($$1 > 0.5) {
            return DECIMAL_FORMAT.format($$1) + " m";
        }
        return $$0 + " cm";
    };
    public static final StatFormatter TIME = $$0 -> {
        double $$1 = (double)$$0 / 20.0;
        double $$2 = $$1 / 60.0;
        double $$3 = $$2 / 60.0;
        double $$4 = $$3 / 24.0;
        double $$5 = $$4 / 365.0;
        if ($$5 > 0.5) {
            return DECIMAL_FORMAT.format($$5) + " y";
        }
        if ($$4 > 0.5) {
            return DECIMAL_FORMAT.format($$4) + " d";
        }
        if ($$3 > 0.5) {
            return DECIMAL_FORMAT.format($$3) + " h";
        }
        if ($$2 > 0.5) {
            return DECIMAL_FORMAT.format($$2) + " min";
        }
        return $$1 + " s";
    };

    public String format(int var1);
}

