/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.parsing.packrat;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.util.Util;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.ErrorCollector;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import org.jspecify.annotations.Nullable;

public abstract class CachedParseState<S>
implements ParseState<S> {
    private @Nullable PositionCache[] positionCache = new PositionCache[256];
    private final ErrorCollector<S> errorCollector;
    private final Scope scope = new Scope();
    private @Nullable SimpleControl[] controlCache = new SimpleControl[16];
    private int nextControlToReturn;
    private final Silent silent = new Silent();

    protected CachedParseState(ErrorCollector<S> $$0) {
        this.errorCollector = $$0;
    }

    @Override
    public Scope scope() {
        return this.scope;
    }

    @Override
    public ErrorCollector<S> errorCollector() {
        return this.errorCollector;
    }

    @Override
    public <T> @Nullable T parse(NamedRule<S, T> $$0) {
        CacheEntry<T> $$8;
        T $$5;
        int $$1 = this.mark();
        PositionCache $$2 = this.getCacheForPosition($$1);
        int $$3 = $$2.findKeyIndex($$0.name());
        if ($$3 != -1) {
            CacheEntry $$4 = $$2.getValue($$3);
            if ($$4 != null) {
                if ($$4 == CacheEntry.NEGATIVE) {
                    return null;
                }
                this.restore($$4.markAfterParse);
                return $$4.value;
            }
        } else {
            $$3 = $$2.allocateNewEntry($$0.name());
        }
        if (($$5 = $$0.value().parse(this)) == null) {
            CacheEntry $$6 = CacheEntry.negativeEntry();
        } else {
            int $$7 = this.mark();
            $$8 = new CacheEntry<T>($$5, $$7);
        }
        $$2.setValue($$3, $$8);
        return $$5;
    }

    private PositionCache getCacheForPosition(int $$0) {
        PositionCache $$4;
        int $$1 = this.positionCache.length;
        if ($$0 >= $$1) {
            int $$2 = Util.growByHalf($$1, $$0 + 1);
            PositionCache[] $$3 = new PositionCache[$$2];
            System.arraycopy(this.positionCache, 0, $$3, 0, $$1);
            this.positionCache = $$3;
        }
        if (($$4 = this.positionCache[$$0]) == null) {
            this.positionCache[$$0] = $$4 = new PositionCache();
        }
        return $$4;
    }

    @Override
    public Control acquireControl() {
        int $$3;
        SimpleControl $$4;
        int $$0 = this.controlCache.length;
        if (this.nextControlToReturn >= $$0) {
            int $$1 = Util.growByHalf($$0, this.nextControlToReturn + 1);
            SimpleControl[] $$2 = new SimpleControl[$$1];
            System.arraycopy(this.controlCache, 0, $$2, 0, $$0);
            this.controlCache = $$2;
        }
        if (($$4 = this.controlCache[$$3 = this.nextControlToReturn++]) == null) {
            this.controlCache[$$3] = $$4 = new SimpleControl();
        } else {
            $$4.reset();
        }
        return $$4;
    }

    @Override
    public void releaseControl() {
        --this.nextControlToReturn;
    }

    @Override
    public ParseState<S> silent() {
        return this.silent;
    }

    static class PositionCache {
        public static final int ENTRY_STRIDE = 2;
        private static final int NOT_FOUND = -1;
        private Object[] atomCache = new Object[16];
        private int nextKey;

        PositionCache() {
        }

        public int findKeyIndex(Atom<?> $$0) {
            for (int $$1 = 0; $$1 < this.nextKey; $$1 += 2) {
                if (this.atomCache[$$1] != $$0) continue;
                return $$1;
            }
            return -1;
        }

        public int allocateNewEntry(Atom<?> $$0) {
            int $$1 = this.nextKey;
            this.nextKey += 2;
            int $$2 = $$1 + 1;
            int $$3 = this.atomCache.length;
            if ($$2 >= $$3) {
                int $$4 = Util.growByHalf($$3, $$2 + 1);
                Object[] $$5 = new Object[$$4];
                System.arraycopy(this.atomCache, 0, $$5, 0, $$3);
                this.atomCache = $$5;
            }
            this.atomCache[$$1] = $$0;
            return $$1;
        }

        public <T> @Nullable CacheEntry<T> getValue(int $$0) {
            return (CacheEntry)this.atomCache[$$0 + 1];
        }

        public void setValue(int $$0, CacheEntry<?> $$1) {
            this.atomCache[$$0 + 1] = $$1;
        }
    }

    static class SimpleControl
    implements Control {
        private boolean hasCut;

        SimpleControl() {
        }

        @Override
        public void cut() {
            this.hasCut = true;
        }

        @Override
        public boolean hasCut() {
            return this.hasCut;
        }

        public void reset() {
            this.hasCut = false;
        }
    }

    class Silent
    implements ParseState<S> {
        private final ErrorCollector<S> silentCollector = new ErrorCollector.Nop();

        Silent() {
        }

        @Override
        public ErrorCollector<S> errorCollector() {
            return this.silentCollector;
        }

        @Override
        public Scope scope() {
            return CachedParseState.this.scope();
        }

        @Override
        public <T> @Nullable T parse(NamedRule<S, T> $$0) {
            return CachedParseState.this.parse($$0);
        }

        @Override
        public S input() {
            return CachedParseState.this.input();
        }

        @Override
        public int mark() {
            return CachedParseState.this.mark();
        }

        @Override
        public void restore(int $$0) {
            CachedParseState.this.restore($$0);
        }

        @Override
        public Control acquireControl() {
            return CachedParseState.this.acquireControl();
        }

        @Override
        public void releaseControl() {
            CachedParseState.this.releaseControl();
        }

        @Override
        public ParseState<S> silent() {
            return this;
        }
    }

    static final class CacheEntry<T>
    extends Record {
        final @Nullable T value;
        final int markAfterParse;
        public static final CacheEntry<?> NEGATIVE = new CacheEntry<Object>(null, -1);

        CacheEntry(@Nullable T $$0, int $$1) {
            this.value = $$0;
            this.markAfterParse = $$1;
        }

        public static <T> CacheEntry<T> negativeEntry() {
            return NEGATIVE;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CacheEntry.class, "value;markAfterParse", "value", "markAfterParse"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CacheEntry.class, "value;markAfterParse", "value", "markAfterParse"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CacheEntry.class, "value;markAfterParse", "value", "markAfterParse"}, this, $$0);
        }

        public @Nullable T value() {
            return this.value;
        }

        public int markAfterParse() {
            return this.markAfterParse;
        }
    }
}

