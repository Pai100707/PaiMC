/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  it.unimi.dsi.fastutil.ints.Int2IntFunction
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;

@FunctionalInterface
public interface FormattedCharSequence {
    public static final FormattedCharSequence EMPTY = $$0 -> true;

    public boolean accept(FormattedCharSink var1);

    public static FormattedCharSequence codepoint(int $$0, Style $$1) {
        return $$2 -> $$2.accept(0, $$1, $$0);
    }

    public static FormattedCharSequence forward(String $$0, Style $$1) {
        if ($$0.isEmpty()) {
            return EMPTY;
        }
        return $$2 -> StringDecomposer.iterate($$0, $$1, $$2);
    }

    public static FormattedCharSequence forward(String $$0, Style $$1, Int2IntFunction $$2) {
        if ($$0.isEmpty()) {
            return EMPTY;
        }
        return $$3 -> StringDecomposer.iterate($$0, $$1, FormattedCharSequence.decorateOutput($$3, $$2));
    }

    public static FormattedCharSequence backward(String $$0, Style $$1) {
        if ($$0.isEmpty()) {
            return EMPTY;
        }
        return $$2 -> StringDecomposer.iterateBackwards($$0, $$1, $$2);
    }

    public static FormattedCharSequence backward(String $$0, Style $$1, Int2IntFunction $$2) {
        if ($$0.isEmpty()) {
            return EMPTY;
        }
        return $$3 -> StringDecomposer.iterateBackwards($$0, $$1, FormattedCharSequence.decorateOutput($$3, $$2));
    }

    public static FormattedCharSink decorateOutput(FormattedCharSink $$0, Int2IntFunction $$1) {
        return ($$2, $$3, $$4) -> $$0.accept($$2, $$3, (Integer)$$1.apply((Object)$$4));
    }

    public static FormattedCharSequence composite() {
        return EMPTY;
    }

    public static FormattedCharSequence composite(FormattedCharSequence $$0) {
        return $$0;
    }

    public static FormattedCharSequence composite(FormattedCharSequence $$0, FormattedCharSequence $$1) {
        return FormattedCharSequence.fromPair($$0, $$1);
    }

    public static FormattedCharSequence composite(FormattedCharSequence ... $$0) {
        return FormattedCharSequence.fromList((List<FormattedCharSequence>)ImmutableList.copyOf((Object[])$$0));
    }

    public static FormattedCharSequence composite(List<FormattedCharSequence> $$0) {
        int $$1 = $$0.size();
        switch ($$1) {
            case 0: {
                return EMPTY;
            }
            case 1: {
                return $$0.get(0);
            }
            case 2: {
                return FormattedCharSequence.fromPair($$0.get(0), $$0.get(1));
            }
        }
        return FormattedCharSequence.fromList((List<FormattedCharSequence>)ImmutableList.copyOf($$0));
    }

    public static FormattedCharSequence fromPair(FormattedCharSequence $$0, FormattedCharSequence $$1) {
        return $$2 -> $$0.accept($$2) && $$1.accept($$2);
    }

    public static FormattedCharSequence fromList(List<FormattedCharSequence> $$0) {
        return $$1 -> {
            for (FormattedCharSequence $$2 : $$0) {
                if ($$2.accept($$1)) continue;
                return false;
            }
            return true;
        };
    }
}

