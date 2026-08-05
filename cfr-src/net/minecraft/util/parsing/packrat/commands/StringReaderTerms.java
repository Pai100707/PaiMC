/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  it.unimi.dsi.fastutil.chars.CharList
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.chars.CharList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;
import net.minecraft.util.parsing.packrat.Term;

public interface StringReaderTerms {
    public static Term<StringReader> word(String $$0) {
        return new TerminalWord($$0);
    }

    public static Term<StringReader> character(final char $$0) {
        return new TerminalCharacters(CharList.of((char)$$0)){

            @Override
            protected boolean isAccepted(char $$02) {
                return $$0 == $$02;
            }
        };
    }

    public static Term<StringReader> characters(final char $$0, final char $$1) {
        return new TerminalCharacters(CharList.of((char)$$0, (char)$$1)){

            @Override
            protected boolean isAccepted(char $$02) {
                return $$02 == $$0 || $$02 == $$1;
            }
        };
    }

    public static StringReader createReader(String $$0, int $$1) {
        StringReader $$2 = new StringReader($$0);
        $$2.setCursor($$1);
        return $$2;
    }

    public static final class TerminalWord
    implements Term<StringReader> {
        private final String value;
        private final DelayedException<CommandSyntaxException> error;
        private final SuggestionSupplier<StringReader> suggestions;

        public TerminalWord(String $$0) {
            this.value = $$0;
            this.error = DelayedException.create(CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), $$0);
            this.suggestions = $$1 -> Stream.of($$0);
        }

        @Override
        public boolean parse(ParseState<StringReader> $$0, Scope $$1, Control $$2) {
            $$0.input().skipWhitespace();
            int $$3 = $$0.mark();
            String $$4 = $$0.input().readUnquotedString();
            if (!$$4.equals(this.value)) {
                $$0.errorCollector().store($$3, this.suggestions, this.error);
                return false;
            }
            return true;
        }

        public String toString() {
            return "terminal[" + this.value + "]";
        }
    }

    public static abstract class TerminalCharacters
    implements Term<StringReader> {
        private final DelayedException<CommandSyntaxException> error;
        private final SuggestionSupplier<StringReader> suggestions;

        public TerminalCharacters(CharList $$0) {
            String $$12 = $$0.intStream().mapToObj(Character::toString).collect(Collectors.joining("|"));
            this.error = DelayedException.create(CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), $$12);
            this.suggestions = $$1 -> $$0.intStream().mapToObj(Character::toString);
        }

        @Override
        public boolean parse(ParseState<StringReader> $$0, Scope $$1, Control $$2) {
            $$0.input().skipWhitespace();
            int $$3 = $$0.mark();
            if (!$$0.input().canRead() || !this.isAccepted($$0.input().read())) {
                $$0.errorCollector().store($$3, this.suggestions, this.error);
                return false;
            }
            return true;
        }

        protected abstract boolean isAccepted(char var1);
    }
}

