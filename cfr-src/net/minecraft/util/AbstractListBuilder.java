/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.ListBuilder
 */
package net.minecraft.util;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import java.util.function.UnaryOperator;

abstract class AbstractListBuilder<T, B>
implements ListBuilder<T> {
    private final DynamicOps<T> ops;
    protected DataResult<B> builder = DataResult.success(this.initBuilder(), (Lifecycle)Lifecycle.stable());

    protected AbstractListBuilder(DynamicOps<T> $$0) {
        this.ops = $$0;
    }

    public DynamicOps<T> ops() {
        return this.ops;
    }

    protected abstract B initBuilder();

    protected abstract B append(B var1, T var2);

    protected abstract DataResult<T> build(B var1, T var2);

    public ListBuilder<T> add(T $$0) {
        this.builder = this.builder.map($$1 -> this.append($$1, $$0));
        return this;
    }

    public ListBuilder<T> add(DataResult<T> $$0) {
        this.builder = this.builder.apply2stable(this::append, $$0);
        return this;
    }

    public ListBuilder<T> withErrorsFrom(DataResult<?> $$0) {
        this.builder = this.builder.flatMap($$12 -> $$0.map($$1 -> $$12));
        return this;
    }

    public ListBuilder<T> mapError(UnaryOperator<String> $$0) {
        this.builder = this.builder.mapError($$0);
        return this;
    }

    public DataResult<T> build(T $$0) {
        DataResult $$12 = this.builder.flatMap($$1 -> this.build($$1, $$0));
        this.builder = DataResult.success(this.initBuilder(), (Lifecycle)Lifecycle.stable());
        return $$12;
    }
}

