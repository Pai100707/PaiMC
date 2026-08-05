/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;

public interface Term<S> {
    public boolean parse(ParseState<S> var1, Scope var2, Control var3);

    public static <S, T> Term<S> marker(Atom<T> $$0, T $$1) {
        return new Marker($$0, $$1);
    }

    @SafeVarargs
    public static <S> Term<S> sequence(Term<S> ... $$0) {
        return new Sequence<S>($$0);
    }

    @SafeVarargs
    public static <S> Term<S> alternative(Term<S> ... $$0) {
        return new Alternative<S>($$0);
    }

    public static <S> Term<S> optional(Term<S> $$0) {
        return new Maybe<S>($$0);
    }

    public static <S, T> Term<S> repeated(NamedRule<S, T> $$0, Atom<List<T>> $$1) {
        return Term.repeated($$0, $$1, 0);
    }

    public static <S, T> Term<S> repeated(NamedRule<S, T> $$0, Atom<List<T>> $$1, int $$2) {
        return new Repeated<S, T>($$0, $$1, $$2);
    }

    public static <S, T> Term<S> repeatedWithTrailingSeparator(NamedRule<S, T> $$0, Atom<List<T>> $$1, Term<S> $$2) {
        return Term.repeatedWithTrailingSeparator($$0, $$1, $$2, 0);
    }

    public static <S, T> Term<S> repeatedWithTrailingSeparator(NamedRule<S, T> $$0, Atom<List<T>> $$1, Term<S> $$2, int $$3) {
        return new RepeatedWithSeparator<S, T>($$0, $$1, $$2, $$3, true);
    }

    public static <S, T> Term<S> repeatedWithoutTrailingSeparator(NamedRule<S, T> $$0, Atom<List<T>> $$1, Term<S> $$2) {
        return Term.repeatedWithoutTrailingSeparator($$0, $$1, $$2, 0);
    }

    public static <S, T> Term<S> repeatedWithoutTrailingSeparator(NamedRule<S, T> $$0, Atom<List<T>> $$1, Term<S> $$2, int $$3) {
        return new RepeatedWithSeparator<S, T>($$0, $$1, $$2, $$3, false);
    }

    public static <S> Term<S> positiveLookahead(Term<S> $$0) {
        return new LookAhead<S>($$0, true);
    }

    public static <S> Term<S> negativeLookahead(Term<S> $$0) {
        return new LookAhead<S>($$0, false);
    }

    public static <S> Term<S> cut() {
        return new Term<S>(){

            @Override
            public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
                $$2.cut();
                return true;
            }

            public String toString() {
                return "\u2191";
            }
        };
    }

    public static <S> Term<S> empty() {
        return new Term<S>(){

            @Override
            public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
                return true;
            }

            public String toString() {
                return "\u03b5";
            }
        };
    }

    public static <S> Term<S> fail(final Object $$0) {
        return new Term<S>(){

            @Override
            public boolean parse(ParseState<S> $$02, Scope $$1, Control $$2) {
                $$02.errorCollector().store($$02.mark(), $$0);
                return false;
            }

            public String toString() {
                return "fail";
            }
        };
    }

    public record Marker<S, T>(Atom<T> name, T value) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
            $$1.put(this.name, this.value);
            return true;
        }
    }

    public record Sequence<S>(Term<S>[] elements) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
            int $$3 = $$0.mark();
            for (Term<S> $$4 : this.elements) {
                if ($$4.parse($$0, $$1, $$2)) continue;
                $$0.restore($$3);
                return false;
            }
            return true;
        }
    }

    public record Alternative<S>(Term<S>[] elements) implements Term<S>
    {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
            Control $$3 = $$0.acquireControl();
            try {
                int $$4 = $$0.mark();
                $$1.splitFrame();
                for (Term<S> $$5 : this.elements) {
                    if ($$5.parse($$0, $$1, $$3)) {
                        $$1.mergeFrame();
                        boolean bl = true;
                        return bl;
                    }
                    $$1.clearFrameValues();
                    $$0.restore($$4);
                    if ($$3.hasCut()) break;
                }
                $$1.popFrame();
                boolean bl = false;
                return bl;
            }
            finally {
                $$0.releaseControl();
            }
        }
    }

    public record Maybe<S>(Term<S> term) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
            int $$3 = $$0.mark();
            if (!this.term.parse($$0, $$1, $$2)) {
                $$0.restore($$3);
            }
            return true;
        }
    }

    public record Repeated<S, T>(NamedRule<S, T> element, Atom<List<T>> listName, int minRepetitions) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
            int $$5;
            int $$3 = $$0.mark();
            ArrayList<T> $$4 = new ArrayList<T>(this.minRepetitions);
            while (true) {
                $$5 = $$0.mark();
                T $$6 = $$0.parse(this.element);
                if ($$6 == null) break;
                $$4.add($$6);
            }
            $$0.restore($$5);
            if ($$4.size() < this.minRepetitions) {
                $$0.restore($$3);
                return false;
            }
            $$1.put(this.listName, $$4);
            return true;
        }
    }

    public record RepeatedWithSeparator<S, T>(NamedRule<S, T> element, Atom<List<T>> listName, Term<S> separator, int minRepetitions, boolean allowTrailingSeparator) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
            int $$3 = $$0.mark();
            ArrayList<T> $$4 = new ArrayList<T>(this.minRepetitions);
            boolean $$5 = true;
            while (true) {
                int $$6 = $$0.mark();
                if (!$$5 && !this.separator.parse($$0, $$1, $$2)) {
                    $$0.restore($$6);
                    break;
                }
                int $$7 = $$0.mark();
                T $$8 = $$0.parse(this.element);
                if ($$8 == null) {
                    if ($$5) {
                        $$0.restore($$7);
                        break;
                    }
                    if (this.allowTrailingSeparator) {
                        $$0.restore($$7);
                        break;
                    }
                    $$0.restore($$3);
                    return false;
                }
                $$4.add($$8);
                $$5 = false;
            }
            if ($$4.size() < this.minRepetitions) {
                $$0.restore($$3);
                return false;
            }
            $$1.put(this.listName, $$4);
            return true;
        }
    }

    public record LookAhead<S>(Term<S> term, boolean positive) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
            int $$3 = $$0.mark();
            boolean $$4 = this.term.parse($$0.silent(), $$1, $$2);
            $$0.restore($$3);
            return this.positive == $$4;
        }
    }
}

