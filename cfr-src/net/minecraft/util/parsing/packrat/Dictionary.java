/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.parsing.packrat;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;
import org.jspecify.annotations.Nullable;

public class Dictionary<S> {
    private final Map<Atom<?>, Entry<S, ?>> terms = new IdentityHashMap();

    public <T> NamedRule<S, T> put(Atom<T> $$0, Rule<S, T> $$1) {
        Entry $$2 = this.terms.computeIfAbsent($$0, Entry::new);
        if ($$2.value != null) {
            throw new IllegalArgumentException("Trying to override rule: " + String.valueOf($$0));
        }
        $$2.value = $$1;
        return $$2;
    }

    public <T> NamedRule<S, T> putComplex(Atom<T> $$0, Term<S> $$1, Rule.RuleAction<S, T> $$2) {
        return this.put($$0, Rule.fromTerm($$1, $$2));
    }

    public <T> NamedRule<S, T> put(Atom<T> $$0, Term<S> $$1, Rule.SimpleRuleAction<S, T> $$2) {
        return this.put($$0, Rule.fromTerm($$1, $$2));
    }

    public void checkAllBound() {
        List<Atom> $$02 = this.terms.entrySet().stream().filter($$0 -> ((Entry)$$0.getValue()).value == null).map(Map.Entry::getKey).toList();
        if (!$$02.isEmpty()) {
            throw new IllegalStateException("Unbound names: " + String.valueOf($$02));
        }
    }

    public <T> NamedRule<S, T> getOrThrow(Atom<T> $$0) {
        return Objects.requireNonNull(this.terms.get($$0), () -> "No rule called " + String.valueOf($$0));
    }

    public <T> NamedRule<S, T> forward(Atom<T> $$0) {
        return this.getOrCreateEntry($$0);
    }

    private <T> Entry<S, T> getOrCreateEntry(Atom<T> $$0) {
        return this.terms.computeIfAbsent($$0, Entry::new);
    }

    public <T> Term<S> named(Atom<T> $$0) {
        return new Reference<S, T>(this.getOrCreateEntry($$0), $$0);
    }

    public <T> Term<S> namedWithAlias(Atom<T> $$0, Atom<T> $$1) {
        return new Reference<S, T>(this.getOrCreateEntry($$0), $$1);
    }

    static class Entry<S, T>
    implements NamedRule<S, T>,
    Supplier<String> {
        private final Atom<T> name;
        @Nullable Rule<S, T> value;

        private Entry(Atom<T> $$0) {
            this.name = $$0;
        }

        @Override
        public Atom<T> name() {
            return this.name;
        }

        @Override
        public Rule<S, T> value() {
            return Objects.requireNonNull(this.value, this);
        }

        @Override
        public String get() {
            return "Unbound rule " + String.valueOf(this.name);
        }

        @Override
        public /* synthetic */ Object get() {
            return this.get();
        }
    }

    record Reference<S, T>(Entry<S, T> ruleToParse, Atom<T> nameToStore) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
            T $$3 = $$0.parse(this.ruleToParse);
            if ($$3 == null) {
                return false;
            }
            $$1.put(this.nameToStore, $$3);
            return true;
        }
    }
}

