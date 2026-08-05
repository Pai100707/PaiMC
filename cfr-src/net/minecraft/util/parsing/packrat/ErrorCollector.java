/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.util.parsing.packrat.ErrorEntry;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;
import org.jspecify.annotations.Nullable;

public interface ErrorCollector<S> {
    public void store(int var1, SuggestionSupplier<S> var2, Object var3);

    default public void store(int $$0, Object $$1) {
        this.store($$0, SuggestionSupplier.empty(), $$1);
    }

    public void finish(int var1);

    public static class LongestOnly<S>
    implements ErrorCollector<S> {
        private @Nullable MutableErrorEntry<S>[] entries = new MutableErrorEntry[16];
        private int nextErrorEntry;
        private int lastCursor = -1;

        private void discardErrorsFromShorterParse(int $$0) {
            if ($$0 > this.lastCursor) {
                this.lastCursor = $$0;
                this.nextErrorEntry = 0;
            }
        }

        @Override
        public void finish(int $$0) {
            this.discardErrorsFromShorterParse($$0);
        }

        @Override
        public void store(int $$0, SuggestionSupplier<S> $$1, Object $$2) {
            this.discardErrorsFromShorterParse($$0);
            if ($$0 == this.lastCursor) {
                this.addErrorEntry($$1, $$2);
            }
        }

        private void addErrorEntry(SuggestionSupplier<S> $$0, Object $$1) {
            int $$5;
            MutableErrorEntry<S> $$6;
            int $$2 = this.entries.length;
            if (this.nextErrorEntry >= $$2) {
                int $$3 = Util.growByHalf($$2, this.nextErrorEntry + 1);
                MutableErrorEntry[] $$4 = new MutableErrorEntry[$$3];
                System.arraycopy(this.entries, 0, $$4, 0, $$2);
                this.entries = $$4;
            }
            if (($$6 = this.entries[$$5 = this.nextErrorEntry++]) == null) {
                this.entries[$$5] = $$6 = new MutableErrorEntry();
            }
            $$6.suggestions = $$0;
            $$6.reason = $$1;
        }

        public List<ErrorEntry<S>> entries() {
            int $$0 = this.nextErrorEntry;
            if ($$0 == 0) {
                return List.of();
            }
            ArrayList<ErrorEntry<S>> $$1 = new ArrayList<ErrorEntry<S>>($$0);
            for (int $$2 = 0; $$2 < $$0; ++$$2) {
                MutableErrorEntry<S> $$3 = this.entries[$$2];
                $$1.add(new ErrorEntry(this.lastCursor, $$3.suggestions, $$3.reason));
            }
            return $$1;
        }

        public int cursor() {
            return this.lastCursor;
        }

        static class MutableErrorEntry<S> {
            SuggestionSupplier<S> suggestions = SuggestionSupplier.empty();
            Object reason = "empty";

            MutableErrorEntry() {
            }
        }
    }

    public static class Nop<S>
    implements ErrorCollector<S> {
        @Override
        public void store(int $$0, SuggestionSupplier<S> $$1, Object $$2) {
        }

        @Override
        public void finish(int $$0) {
        }
    }
}

