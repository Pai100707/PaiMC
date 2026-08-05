/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft;

import java.util.Objects;

@FunctionalInterface
public interface CharPredicate {
    public boolean test(char var1);

    default public CharPredicate and(CharPredicate $$0) {
        Objects.requireNonNull($$0);
        return $$1 -> this.test($$1) && $$0.test($$1);
    }

    default public CharPredicate negate() {
        return $$0 -> !this.test($$0);
    }

    default public CharPredicate or(CharPredicate $$0) {
        Objects.requireNonNull($$0);
        return $$1 -> this.test($$1) || $$0.test($$1);
    }
}

