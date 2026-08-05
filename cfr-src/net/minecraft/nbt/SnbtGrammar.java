/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.primitives.UnsignedBytes
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  it.unimi.dsi.fastutil.bytes.ByteArrayList
 *  it.unimi.dsi.fastutil.chars.CharList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedBytes;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import net.minecraft.nbt.SnbtOperations;
import net.minecraft.network.chat.Component;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.GreedyPatternParseRule;
import net.minecraft.util.parsing.packrat.commands.GreedyPredicateParseRule;
import net.minecraft.util.parsing.packrat.commands.NumberRunParseRule;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;
import net.minecraft.util.parsing.packrat.commands.UnquotedStringParseRule;
import org.jspecify.annotations.Nullable;

public class SnbtGrammar {
    private static final DynamicCommandExceptionType ERROR_NUMBER_PARSE_FAILURE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("snbt.parser.number_parse_failure", $$0));
    static final DynamicCommandExceptionType ERROR_EXPECTED_HEX_ESCAPE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("snbt.parser.expected_hex_escape", $$0));
    private static final DynamicCommandExceptionType ERROR_INVALID_CODEPOINT = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("snbt.parser.invalid_codepoint", $$0));
    private static final DynamicCommandExceptionType ERROR_NO_SUCH_OPERATION = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("snbt.parser.no_such_operation", $$0));
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_INTEGER_TYPE = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_integer_type")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_FLOAT_TYPE = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_float_type")));
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_NON_NEGATIVE_NUMBER = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_non_negative_number")));
    private static final DelayedException<CommandSyntaxException> ERROR_INVALID_CHARACTER_NAME = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.invalid_character_name")));
    static final DelayedException<CommandSyntaxException> ERROR_INVALID_ARRAY_ELEMENT_TYPE = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.invalid_array_element_type")));
    private static final DelayedException<CommandSyntaxException> ERROR_INVALID_UNQUOTED_START = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.invalid_unquoted_start")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_UNQUOTED_STRING = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_unquoted_string")));
    private static final DelayedException<CommandSyntaxException> ERROR_INVALID_STRING_CONTENTS = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.invalid_string_contents")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_BINARY_NUMERAL = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_binary_numeral")));
    private static final DelayedException<CommandSyntaxException> ERROR_UNDESCORE_NOT_ALLOWED = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.underscore_not_allowed")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_DECIMAL_NUMERAL = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_decimal_numeral")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_HEX_NUMERAL = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_hex_numeral")));
    private static final DelayedException<CommandSyntaxException> ERROR_EMPTY_KEY = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.empty_key")));
    private static final DelayedException<CommandSyntaxException> ERROR_LEADING_ZERO_NOT_ALLOWED = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.leading_zero_not_allowed")));
    private static final DelayedException<CommandSyntaxException> ERROR_INFINITY_NOT_ALLOWED = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.infinity_not_allowed")));
    private static final HexFormat HEX_ESCAPE = HexFormat.of().withUpperCase();
    private static final NumberRunParseRule BINARY_NUMERAL = new NumberRunParseRule((DelayedException)ERROR_EXPECTED_BINARY_NUMERAL, (DelayedException)ERROR_UNDESCORE_NOT_ALLOWED){

        @Override
        protected boolean isAccepted(char $$0) {
            return switch ($$0) {
                case '0', '1', '_' -> true;
                default -> false;
            };
        }
    };
    private static final NumberRunParseRule DECIMAL_NUMERAL = new NumberRunParseRule((DelayedException)ERROR_EXPECTED_DECIMAL_NUMERAL, (DelayedException)ERROR_UNDESCORE_NOT_ALLOWED){

        @Override
        protected boolean isAccepted(char $$0) {
            return switch ($$0) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' -> true;
                default -> false;
            };
        }
    };
    private static final NumberRunParseRule HEX_NUMERAL = new NumberRunParseRule((DelayedException)ERROR_EXPECTED_HEX_NUMERAL, (DelayedException)ERROR_UNDESCORE_NOT_ALLOWED){

        @Override
        protected boolean isAccepted(char $$0) {
            return switch ($$0) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', '_', 'a', 'b', 'c', 'd', 'e', 'f' -> true;
                default -> false;
            };
        }
    };
    private static final GreedyPredicateParseRule PLAIN_STRING_CHUNK = new GreedyPredicateParseRule(1, (DelayedException)ERROR_INVALID_STRING_CONTENTS){

        @Override
        protected boolean isAccepted(char $$0) {
            return switch ($$0) {
                case '\"', '\'', '\\' -> false;
                default -> true;
            };
        }
    };
    private static final StringReaderTerms.TerminalCharacters NUMBER_LOOKEAHEAD = new StringReaderTerms.TerminalCharacters(CharList.of()){

        @Override
        protected boolean isAccepted(char $$0) {
            return SnbtGrammar.canStartNumber($$0);
        }
    };
    private static final Pattern UNICODE_NAME = Pattern.compile("[-a-zA-Z0-9 ]+");

    static DelayedException<CommandSyntaxException> createNumberParseError(NumberFormatException $$0) {
        return DelayedException.create(ERROR_NUMBER_PARSE_FAILURE, $$0.getMessage());
    }

    public static @Nullable String escapeControlCharacters(char $$0) {
        return switch ($$0) {
            case '\b' -> "b";
            case '\t' -> "t";
            case '\n' -> "n";
            case '\f' -> "f";
            case '\r' -> "r";
            default -> $$0 < ' ' ? "x" + HEX_ESCAPE.toHexDigits((byte)$$0) : null;
        };
    }

    private static boolean isAllowedToStartUnquotedString(char $$0) {
        return !SnbtGrammar.canStartNumber($$0);
    }

    static boolean canStartNumber(char $$0) {
        return switch ($$0) {
            case '+', '-', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> true;
            default -> false;
        };
    }

    static boolean needsUnderscoreRemoval(String $$0) {
        return $$0.indexOf(95) != -1;
    }

    private static void cleanAndAppend(StringBuilder $$0, String $$1) {
        SnbtGrammar.cleanAndAppend($$0, $$1, SnbtGrammar.needsUnderscoreRemoval($$1));
    }

    static void cleanAndAppend(StringBuilder $$0, String $$1, boolean $$2) {
        if ($$2) {
            for (char $$3 : $$1.toCharArray()) {
                if ($$3 == '_') continue;
                $$0.append($$3);
            }
        } else {
            $$0.append($$1);
        }
    }

    static short parseUnsignedShort(String $$0, int $$1) {
        int $$2 = Integer.parseInt($$0, $$1);
        if ($$2 >> 16 == 0) {
            return (short)$$2;
        }
        throw new NumberFormatException("out of range: " + $$2);
    }

    private static <T> @Nullable T createFloat(DynamicOps<T> $$0, Sign $$1, @Nullable String $$2, @Nullable String $$3, @Nullable Signed<String> $$4, @Nullable TypeSuffix $$5, ParseState<?> $$6) {
        StringBuilder $$7 = new StringBuilder();
        $$1.append($$7);
        if ($$2 != null) {
            SnbtGrammar.cleanAndAppend($$7, $$2);
        }
        if ($$3 != null) {
            $$7.append('.');
            SnbtGrammar.cleanAndAppend($$7, $$3);
        }
        if ($$4 != null) {
            $$7.append('e');
            $$4.sign().append($$7);
            SnbtGrammar.cleanAndAppend($$7, (String)$$4.value);
        }
        try {
            String $$8 = $$7.toString();
            TypeSuffix typeSuffix = $$5;
            int n = 0;
            return switch (SwitchBootstraps.enumSwitch("enumSwitch", new Object[]{"FLOAT", "DOUBLE"}, (TypeSuffix)typeSuffix, n)) {
                case 0 -> SnbtGrammar.convertFloat($$0, $$6, $$8);
                case 1 -> SnbtGrammar.convertDouble($$0, $$6, $$8);
                case -1 -> SnbtGrammar.convertDouble($$0, $$6, $$8);
                default -> {
                    $$6.errorCollector().store($$6.mark(), ERROR_EXPECTED_FLOAT_TYPE);
                    yield null;
                }
            };
        }
        catch (NumberFormatException $$9) {
            $$6.errorCollector().store($$6.mark(), SnbtGrammar.createNumberParseError($$9));
            return null;
        }
    }

    private static <T> @Nullable T convertFloat(DynamicOps<T> $$0, ParseState<?> $$1, String $$2) {
        float $$3 = Float.parseFloat($$2);
        if (!Float.isFinite($$3)) {
            $$1.errorCollector().store($$1.mark(), ERROR_INFINITY_NOT_ALLOWED);
            return null;
        }
        return (T)$$0.createFloat($$3);
    }

    private static <T> @Nullable T convertDouble(DynamicOps<T> $$0, ParseState<?> $$1, String $$2) {
        double $$3 = Double.parseDouble($$2);
        if (!Double.isFinite($$3)) {
            $$1.errorCollector().store($$1.mark(), ERROR_INFINITY_NOT_ALLOWED);
            return null;
        }
        return (T)$$0.createDouble($$3);
    }

    private static String joinList(List<String> $$0) {
        return switch ($$0.size()) {
            case 0 -> "";
            case 1 -> $$0.getFirst();
            default -> String.join((CharSequence)"", $$0);
        };
    }

    public static <T> Grammar<T> createParser(DynamicOps<T> $$0) {
        Object $$12 = $$0.createBoolean(true);
        Object $$22 = $$0.createBoolean(false);
        Object $$32 = $$0.emptyMap();
        Object $$42 = $$0.emptyList();
        Dictionary<StringReader> $$52 = new Dictionary<StringReader>();
        Atom $$62 = Atom.of("sign");
        $$52.put($$62, Term.alternative(Term.sequence(StringReaderTerms.character('+'), Term.marker($$62, Sign.PLUS)), Term.sequence(StringReaderTerms.character('-'), Term.marker($$62, Sign.MINUS))), $$1 -> (Sign)((Object)((Object)$$1.getOrThrow($$62))));
        Atom $$7 = Atom.of("integer_suffix");
        $$52.put($$7, Term.alternative(Term.sequence(StringReaderTerms.characters('u', 'U'), Term.alternative(Term.sequence(StringReaderTerms.characters('b', 'B'), Term.marker($$7, new IntegerSuffix(SignedPrefix.UNSIGNED, TypeSuffix.BYTE))), Term.sequence(StringReaderTerms.characters('s', 'S'), Term.marker($$7, new IntegerSuffix(SignedPrefix.UNSIGNED, TypeSuffix.SHORT))), Term.sequence(StringReaderTerms.characters('i', 'I'), Term.marker($$7, new IntegerSuffix(SignedPrefix.UNSIGNED, TypeSuffix.INT))), Term.sequence(StringReaderTerms.characters('l', 'L'), Term.marker($$7, new IntegerSuffix(SignedPrefix.UNSIGNED, TypeSuffix.LONG))))), Term.sequence(StringReaderTerms.characters('s', 'S'), Term.alternative(Term.sequence(StringReaderTerms.characters('b', 'B'), Term.marker($$7, new IntegerSuffix(SignedPrefix.SIGNED, TypeSuffix.BYTE))), Term.sequence(StringReaderTerms.characters('s', 'S'), Term.marker($$7, new IntegerSuffix(SignedPrefix.SIGNED, TypeSuffix.SHORT))), Term.sequence(StringReaderTerms.characters('i', 'I'), Term.marker($$7, new IntegerSuffix(SignedPrefix.SIGNED, TypeSuffix.INT))), Term.sequence(StringReaderTerms.characters('l', 'L'), Term.marker($$7, new IntegerSuffix(SignedPrefix.SIGNED, TypeSuffix.LONG))))), Term.sequence(StringReaderTerms.characters('b', 'B'), Term.marker($$7, new IntegerSuffix(null, TypeSuffix.BYTE))), Term.sequence(StringReaderTerms.characters('s', 'S'), Term.marker($$7, new IntegerSuffix(null, TypeSuffix.SHORT))), Term.sequence(StringReaderTerms.characters('i', 'I'), Term.marker($$7, new IntegerSuffix(null, TypeSuffix.INT))), Term.sequence(StringReaderTerms.characters('l', 'L'), Term.marker($$7, new IntegerSuffix(null, TypeSuffix.LONG)))), $$1 -> (IntegerSuffix)$$1.getOrThrow($$7));
        Atom $$8 = Atom.of("binary_numeral");
        $$52.put($$8, BINARY_NUMERAL);
        Atom $$9 = Atom.of("decimal_numeral");
        $$52.put($$9, DECIMAL_NUMERAL);
        Atom $$10 = Atom.of("hex_numeral");
        $$52.put($$10, HEX_NUMERAL);
        Atom $$11 = Atom.of("integer_literal");
        NamedRule $$122 = $$52.put($$11, Term.sequence(Term.optional($$52.named($$62)), Term.alternative(Term.sequence(StringReaderTerms.character('0'), Term.cut(), Term.alternative(Term.sequence(StringReaderTerms.characters('x', 'X'), Term.cut(), $$52.named($$10)), Term.sequence(StringReaderTerms.characters('b', 'B'), $$52.named($$8)), Term.sequence($$52.named($$9), Term.cut(), Term.fail(ERROR_LEADING_ZERO_NOT_ALLOWED)), Term.marker($$9, "0"))), $$52.named($$9)), Term.optional($$52.named($$7))), $$5 -> {
            IntegerSuffix $$6 = $$5.getOrDefault($$7, IntegerSuffix.EMPTY);
            Sign $$7 = $$5.getOrDefault($$62, Sign.PLUS);
            String $$8 = (String)$$5.get($$9);
            if ($$8 != null) {
                return new IntegerLiteral($$7, Base.DECIMAL, $$8, $$6);
            }
            String $$9 = (String)$$5.get($$10);
            if ($$9 != null) {
                return new IntegerLiteral($$7, Base.HEX, $$9, $$6);
            }
            String $$10 = (String)$$5.getOrThrow($$8);
            return new IntegerLiteral($$7, Base.BINARY, $$10, $$6);
        });
        Atom $$13 = Atom.of("float_type_suffix");
        $$52.put($$13, Term.alternative(Term.sequence(StringReaderTerms.characters('f', 'F'), Term.marker($$13, TypeSuffix.FLOAT)), Term.sequence(StringReaderTerms.characters('d', 'D'), Term.marker($$13, TypeSuffix.DOUBLE))), $$1 -> (TypeSuffix)((Object)((Object)$$1.getOrThrow($$13))));
        Atom $$14 = Atom.of("float_exponent_part");
        $$52.put($$14, Term.sequence(StringReaderTerms.characters('e', 'E'), Term.optional($$52.named($$62)), $$52.named($$9)), $$2 -> new Signed<String>($$2.getOrDefault($$62, Sign.PLUS), (String)$$2.getOrThrow($$9)));
        Atom $$15 = Atom.of("float_whole_part");
        Atom $$16 = Atom.of("float_fraction_part");
        Atom $$17 = Atom.of("float_literal");
        $$52.putComplex($$17, Term.sequence(Term.optional($$52.named($$62)), Term.alternative(Term.sequence($$52.namedWithAlias($$9, $$15), StringReaderTerms.character('.'), Term.cut(), Term.optional($$52.namedWithAlias($$9, $$16)), Term.optional($$52.named($$14)), Term.optional($$52.named($$13))), Term.sequence(StringReaderTerms.character('.'), Term.cut(), $$52.namedWithAlias($$9, $$16), Term.optional($$52.named($$14)), Term.optional($$52.named($$13))), Term.sequence($$52.namedWithAlias($$9, $$15), $$52.named($$14), Term.cut(), Term.optional($$52.named($$13))), Term.sequence($$52.namedWithAlias($$9, $$15), Term.optional($$52.named($$14)), $$52.named($$13)))), $$6 -> {
            Scope $$7 = $$6.scope();
            Sign $$8 = $$7.getOrDefault($$62, Sign.PLUS);
            String $$9 = (String)$$7.get($$15);
            String $$10 = (String)$$7.get($$16);
            Signed $$11 = (Signed)$$7.get($$14);
            TypeSuffix $$12 = (TypeSuffix)((Object)((Object)$$7.get($$13)));
            return SnbtGrammar.createFloat($$0, $$8, $$9, $$10, $$11, $$12, $$6);
        });
        Atom $$18 = Atom.of("string_hex_2");
        $$52.put($$18, new SimpleHexLiteralParseRule(2));
        Atom $$19 = Atom.of("string_hex_4");
        $$52.put($$19, new SimpleHexLiteralParseRule(4));
        Atom $$20 = Atom.of("string_hex_8");
        $$52.put($$20, new SimpleHexLiteralParseRule(8));
        Atom $$21 = Atom.of("string_unicode_name");
        $$52.put($$21, new GreedyPatternParseRule(UNICODE_NAME, ERROR_INVALID_CHARACTER_NAME));
        Atom $$222 = Atom.of("string_escape_sequence");
        $$52.putComplex($$222, Term.alternative(Term.sequence(StringReaderTerms.character('b'), Term.marker($$222, "\b")), Term.sequence(StringReaderTerms.character('s'), Term.marker($$222, " ")), Term.sequence(StringReaderTerms.character('t'), Term.marker($$222, "\t")), Term.sequence(StringReaderTerms.character('n'), Term.marker($$222, "\n")), Term.sequence(StringReaderTerms.character('f'), Term.marker($$222, "\f")), Term.sequence(StringReaderTerms.character('r'), Term.marker($$222, "\r")), Term.sequence(StringReaderTerms.character('\\'), Term.marker($$222, "\\")), Term.sequence(StringReaderTerms.character('\''), Term.marker($$222, "'")), Term.sequence(StringReaderTerms.character('\"'), Term.marker($$222, "\"")), Term.sequence(StringReaderTerms.character('x'), $$52.named($$18)), Term.sequence(StringReaderTerms.character('u'), $$52.named($$19)), Term.sequence(StringReaderTerms.character('U'), $$52.named($$20)), Term.sequence(StringReaderTerms.character('N'), StringReaderTerms.character('{'), $$52.named($$21), StringReaderTerms.character('}'))), $$5 -> {
            void $$13;
            Scope $$6 = $$5.scope();
            String $$7 = (String)$$6.getAny($$222);
            if ($$7 != null) {
                return $$7;
            }
            String $$8 = (String)$$6.getAny($$18, $$19, $$20);
            if ($$8 != null) {
                int $$9 = HexFormat.fromHexDigits($$8);
                if (!Character.isValidCodePoint($$9)) {
                    $$5.errorCollector().store($$5.mark(), DelayedException.create(ERROR_INVALID_CODEPOINT, String.format(Locale.ROOT, "U+%08X", $$9)));
                    return null;
                }
                return Character.toString($$9);
            }
            String $$10 = (String)$$6.getOrThrow($$21);
            try {
                int $$11 = Character.codePointOf($$10);
            }
            catch (IllegalArgumentException $$12) {
                $$5.errorCollector().store($$5.mark(), ERROR_INVALID_CHARACTER_NAME);
                return null;
            }
            return Character.toString((int)$$13);
        });
        Atom $$23 = Atom.of("string_plain_contents");
        $$52.put($$23, PLAIN_STRING_CHUNK);
        Atom $$24 = Atom.of("string_chunks");
        Atom $$25 = Atom.of("string_contents");
        Atom $$26 = Atom.of("single_quoted_string_chunk");
        NamedRule $$27 = $$52.put($$26, Term.alternative($$52.namedWithAlias($$23, $$25), Term.sequence(StringReaderTerms.character('\\'), $$52.namedWithAlias($$222, $$25)), Term.sequence(StringReaderTerms.character('\"'), Term.marker($$25, "\""))), $$1 -> (String)$$1.getOrThrow($$25));
        Atom $$28 = Atom.of("single_quoted_string_contents");
        $$52.put($$28, Term.repeated($$27, $$24), $$1 -> SnbtGrammar.joinList((List)$$1.getOrThrow($$24)));
        Atom $$29 = Atom.of("double_quoted_string_chunk");
        NamedRule $$30 = $$52.put($$29, Term.alternative($$52.namedWithAlias($$23, $$25), Term.sequence(StringReaderTerms.character('\\'), $$52.namedWithAlias($$222, $$25)), Term.sequence(StringReaderTerms.character('\''), Term.marker($$25, "'"))), $$1 -> (String)$$1.getOrThrow($$25));
        Atom $$31 = Atom.of("double_quoted_string_contents");
        $$52.put($$31, Term.repeated($$30, $$24), $$1 -> SnbtGrammar.joinList((List)$$1.getOrThrow($$24)));
        Atom $$322 = Atom.of("quoted_string_literal");
        $$52.put($$322, Term.alternative(Term.sequence(StringReaderTerms.character('\"'), Term.cut(), Term.optional($$52.namedWithAlias($$31, $$25)), StringReaderTerms.character('\"')), Term.sequence(StringReaderTerms.character('\''), Term.optional($$52.namedWithAlias($$28, $$25)), StringReaderTerms.character('\''))), $$1 -> (String)$$1.getOrThrow($$25));
        Atom $$33 = Atom.of("unquoted_string");
        $$52.put($$33, new UnquotedStringParseRule(1, ERROR_EXPECTED_UNQUOTED_STRING));
        Atom $$34 = Atom.of("literal");
        Atom $$35 = Atom.of("arguments");
        $$52.put($$35, Term.repeatedWithTrailingSeparator($$52.forward($$34), $$35, StringReaderTerms.character(',')), $$1 -> (List)$$1.getOrThrow($$35));
        Atom $$36 = Atom.of("unquoted_string_or_builtin");
        $$52.putComplex($$36, Term.sequence($$52.named($$33), Term.optional(Term.sequence(StringReaderTerms.character('('), $$52.named($$35), StringReaderTerms.character(')')))), $$5 -> {
            Scope $$6 = $$5.scope();
            String $$7 = (String)$$6.getOrThrow($$33);
            if ($$7.isEmpty() || !SnbtGrammar.isAllowedToStartUnquotedString($$7.charAt(0))) {
                $$5.errorCollector().store($$5.mark(), SnbtOperations.BUILTIN_IDS, ERROR_INVALID_UNQUOTED_START);
                return null;
            }
            List $$8 = (List)$$6.get($$35);
            if ($$8 != null) {
                SnbtOperations.BuiltinKey $$9 = new SnbtOperations.BuiltinKey($$7, $$8.size());
                SnbtOperations.BuiltinOperation $$10 = SnbtOperations.BUILTIN_OPERATIONS.get($$9);
                if ($$10 != null) {
                    return $$10.run($$0, $$8, $$5);
                }
                $$5.errorCollector().store($$5.mark(), DelayedException.create(ERROR_NO_SUCH_OPERATION, $$9.toString()));
                return null;
            }
            if ($$7.equalsIgnoreCase("true")) {
                return $$12;
            }
            if ($$7.equalsIgnoreCase("false")) {
                return $$22;
            }
            return $$0.createString($$7);
        });
        Atom $$37 = Atom.of("map_key");
        $$52.put($$37, Term.alternative($$52.named($$322), $$52.named($$33)), $$2 -> (String)$$2.getAnyOrThrow($$322, $$33));
        Atom $$38 = Atom.of("map_entry");
        NamedRule $$39 = $$52.putComplex($$38, Term.sequence($$52.named($$37), StringReaderTerms.character(':'), $$52.named($$34)), $$2 -> {
            Scope $$3 = $$2.scope();
            String $$4 = (String)$$3.getOrThrow($$37);
            if ($$4.isEmpty()) {
                $$2.errorCollector().store($$2.mark(), ERROR_EMPTY_KEY);
                return null;
            }
            Object $$5 = $$3.getOrThrow($$34);
            return Map.entry($$4, $$5);
        });
        Atom $$40 = Atom.of("map_entries");
        $$52.put($$40, Term.repeatedWithTrailingSeparator($$39, $$40, StringReaderTerms.character(',')), $$1 -> (List)$$1.getOrThrow($$40));
        Atom $$41 = Atom.of("map_literal");
        $$52.put($$41, Term.sequence(StringReaderTerms.character('{'), $$52.named($$40), StringReaderTerms.character('}')), $$3 -> {
            List $$4 = (List)$$3.getOrThrow($$40);
            if ($$4.isEmpty()) {
                return $$32;
            }
            ImmutableMap.Builder $$5 = ImmutableMap.builderWithExpectedSize((int)$$4.size());
            for (Map.Entry $$6 : $$4) {
                $$5.put($$0.createString((String)$$6.getKey()), $$6.getValue());
            }
            return $$0.createMap((Map)$$5.buildKeepingLast());
        });
        Atom $$422 = Atom.of("list_entries");
        $$52.put($$422, Term.repeatedWithTrailingSeparator($$52.forward($$34), $$422, StringReaderTerms.character(',')), $$1 -> (List)$$1.getOrThrow($$422));
        Atom $$43 = Atom.of("array_prefix");
        $$52.put($$43, Term.alternative(Term.sequence(StringReaderTerms.character('B'), Term.marker($$43, ArrayPrefix.BYTE)), Term.sequence(StringReaderTerms.character('L'), Term.marker($$43, ArrayPrefix.LONG)), Term.sequence(StringReaderTerms.character('I'), Term.marker($$43, ArrayPrefix.INT))), $$1 -> (ArrayPrefix)((Object)((Object)$$1.getOrThrow($$43))));
        Atom $$44 = Atom.of("int_array_entries");
        $$52.put($$44, Term.repeatedWithTrailingSeparator($$122, $$44, StringReaderTerms.character(',')), $$1 -> (List)$$1.getOrThrow($$44));
        Atom $$45 = Atom.of("list_literal");
        $$52.putComplex($$45, Term.sequence(StringReaderTerms.character('['), Term.alternative(Term.sequence($$52.named($$43), StringReaderTerms.character(';'), $$52.named($$44)), $$52.named($$422)), StringReaderTerms.character(']')), $$5 -> {
            Scope $$6 = $$5.scope();
            ArrayPrefix $$7 = (ArrayPrefix)((Object)((Object)$$6.get($$43)));
            if ($$7 != null) {
                List $$8 = (List)$$6.getOrThrow($$44);
                return $$8.isEmpty() ? $$7.create($$0) : $$7.create($$0, $$8, $$5);
            }
            List $$9 = (List)$$6.getOrThrow($$422);
            return $$9.isEmpty() ? $$42 : $$0.createList($$9.stream());
        });
        NamedRule $$46 = $$52.putComplex($$34, Term.alternative(Term.sequence(Term.positiveLookahead(NUMBER_LOOKEAHEAD), Term.alternative($$52.namedWithAlias($$17, $$34), $$52.named($$11))), Term.sequence(Term.positiveLookahead(StringReaderTerms.characters('\"', '\'')), Term.cut(), $$52.named($$322)), Term.sequence(Term.positiveLookahead(StringReaderTerms.character('{')), Term.cut(), $$52.namedWithAlias($$41, $$34)), Term.sequence(Term.positiveLookahead(StringReaderTerms.character('[')), Term.cut(), $$52.namedWithAlias($$45, $$34)), $$52.namedWithAlias($$36, $$34)), $$4 -> {
            Scope $$5 = $$4.scope();
            String $$6 = (String)$$5.get($$322);
            if ($$6 != null) {
                return $$0.createString($$6);
            }
            IntegerLiteral $$7 = (IntegerLiteral)$$5.get($$11);
            if ($$7 != null) {
                return $$7.create($$0, $$4);
            }
            return $$5.getOrThrow($$34);
        });
        return new Grammar<Object>($$52, $$46);
    }

    static enum Sign {
        PLUS,
        MINUS;


        public void append(StringBuilder $$0) {
            if (this == MINUS) {
                $$0.append("-");
            }
        }
    }

    static final class Signed<T>
    extends Record {
        private final Sign sign;
        final T value;

        Signed(Sign $$0, T $$1) {
            this.sign = $$0;
            this.value = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Signed.class, "sign;value", "sign", "value"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Signed.class, "sign;value", "sign", "value"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Signed.class, "sign;value", "sign", "value"}, this, $$0);
        }

        public Sign sign() {
            return this.sign;
        }

        public T value() {
            return this.value;
        }
    }

    static enum TypeSuffix {
        FLOAT,
        DOUBLE,
        BYTE,
        SHORT,
        INT,
        LONG;

    }

    static final class IntegerSuffix
    extends Record {
        final @Nullable SignedPrefix signed;
        final @Nullable TypeSuffix type;
        public static final IntegerSuffix EMPTY = new IntegerSuffix(null, null);

        IntegerSuffix(@Nullable SignedPrefix $$0, @Nullable TypeSuffix $$1) {
            this.signed = $$0;
            this.type = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{IntegerSuffix.class, "signed;type", "signed", "type"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{IntegerSuffix.class, "signed;type", "signed", "type"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{IntegerSuffix.class, "signed;type", "signed", "type"}, this, $$0);
        }

        public @Nullable SignedPrefix signed() {
            return this.signed;
        }

        public @Nullable TypeSuffix type() {
            return this.type;
        }
    }

    static enum SignedPrefix {
        SIGNED,
        UNSIGNED;

    }

    static class SimpleHexLiteralParseRule
    extends GreedyPredicateParseRule {
        public SimpleHexLiteralParseRule(int $$0) {
            super($$0, $$0, DelayedException.create(ERROR_EXPECTED_HEX_ESCAPE, String.valueOf($$0)));
        }

        @Override
        protected boolean isAccepted(char $$0) {
            return switch ($$0) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f' -> true;
                default -> false;
            };
        }
    }

    static enum ArrayPrefix {
        BYTE(TypeSuffix.BYTE, new TypeSuffix[0]){
            private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);

            @Override
            public <T> T create(DynamicOps<T> $$0) {
                return (T)$$0.createByteList(EMPTY_BUFFER);
            }

            @Override
            public <T> @Nullable T create(DynamicOps<T> $$0, List<IntegerLiteral> $$1, ParseState<?> $$2) {
                ByteArrayList $$3 = new ByteArrayList();
                for (IntegerLiteral $$4 : $$1) {
                    Number $$5 = this.buildNumber($$4, $$2);
                    if ($$5 == null) {
                        return null;
                    }
                    $$3.add($$5.byteValue());
                }
                return (T)$$0.createByteList(ByteBuffer.wrap($$3.toByteArray()));
            }
        }
        ,
        INT(TypeSuffix.INT, new TypeSuffix[]{TypeSuffix.BYTE, TypeSuffix.SHORT}){

            @Override
            public <T> T create(DynamicOps<T> $$0) {
                return (T)$$0.createIntList(IntStream.empty());
            }

            @Override
            public <T> @Nullable T create(DynamicOps<T> $$0, List<IntegerLiteral> $$1, ParseState<?> $$2) {
                IntStream.Builder $$3 = IntStream.builder();
                for (IntegerLiteral $$4 : $$1) {
                    Number $$5 = this.buildNumber($$4, $$2);
                    if ($$5 == null) {
                        return null;
                    }
                    $$3.add($$5.intValue());
                }
                return (T)$$0.createIntList($$3.build());
            }
        }
        ,
        LONG(TypeSuffix.LONG, new TypeSuffix[]{TypeSuffix.BYTE, TypeSuffix.SHORT, TypeSuffix.INT}){

            @Override
            public <T> T create(DynamicOps<T> $$0) {
                return (T)$$0.createLongList(LongStream.empty());
            }

            @Override
            public <T> @Nullable T create(DynamicOps<T> $$0, List<IntegerLiteral> $$1, ParseState<?> $$2) {
                LongStream.Builder $$3 = LongStream.builder();
                for (IntegerLiteral $$4 : $$1) {
                    Number $$5 = this.buildNumber($$4, $$2);
                    if ($$5 == null) {
                        return null;
                    }
                    $$3.add($$5.longValue());
                }
                return (T)$$0.createLongList($$3.build());
            }
        };

        private final TypeSuffix defaultType;
        private final Set<TypeSuffix> additionalTypes;

        ArrayPrefix(TypeSuffix $$0, TypeSuffix ... $$1) {
            this.additionalTypes = Set.of($$1);
            this.defaultType = $$0;
        }

        public boolean isAllowed(TypeSuffix $$0) {
            return $$0 == this.defaultType || this.additionalTypes.contains((Object)$$0);
        }

        public abstract <T> T create(DynamicOps<T> var1);

        public abstract <T> @Nullable T create(DynamicOps<T> var1, List<IntegerLiteral> var2, ParseState<?> var3);

        protected @Nullable Number buildNumber(IntegerLiteral $$0, ParseState<?> $$1) {
            TypeSuffix $$2 = this.computeType($$0.suffix);
            if ($$2 == null) {
                $$1.errorCollector().store($$1.mark(), ERROR_INVALID_ARRAY_ELEMENT_TYPE);
                return null;
            }
            return (Number)$$0.create(JavaOps.INSTANCE, $$2, $$1);
        }

        private @Nullable TypeSuffix computeType(IntegerSuffix $$0) {
            TypeSuffix $$1 = $$0.type();
            if ($$1 == null) {
                return this.defaultType;
            }
            if (!this.isAllowed($$1)) {
                return null;
            }
            return $$1;
        }
    }

    static final class IntegerLiteral
    extends Record {
        private final Sign sign;
        private final Base base;
        private final String digits;
        final IntegerSuffix suffix;

        IntegerLiteral(Sign $$0, Base $$1, String $$2, IntegerSuffix $$3) {
            this.sign = $$0;
            this.base = $$1;
            this.digits = $$2;
            this.suffix = $$3;
        }

        private SignedPrefix signedOrDefault() {
            if (this.suffix.signed != null) {
                return this.suffix.signed;
            }
            return switch (this.base.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 2 -> SignedPrefix.UNSIGNED;
                case 1 -> SignedPrefix.SIGNED;
            };
        }

        private String cleanupDigits(Sign $$0) {
            boolean $$1 = SnbtGrammar.needsUnderscoreRemoval(this.digits);
            if ($$0 == Sign.MINUS || $$1) {
                StringBuilder $$2 = new StringBuilder();
                $$0.append($$2);
                SnbtGrammar.cleanAndAppend($$2, this.digits, $$1);
                return $$2.toString();
            }
            return this.digits;
        }

        public <T> @Nullable T create(DynamicOps<T> $$0, ParseState<?> $$1) {
            return this.create($$0, Objects.requireNonNullElse(this.suffix.type, TypeSuffix.INT), $$1);
        }

        public <T> @Nullable T create(DynamicOps<T> $$0, TypeSuffix $$1, ParseState<?> $$2) {
            boolean $$3;
            boolean bl = $$3 = this.signedOrDefault() == SignedPrefix.SIGNED;
            if (!$$3 && this.sign == Sign.MINUS) {
                $$2.errorCollector().store($$2.mark(), ERROR_EXPECTED_NON_NEGATIVE_NUMBER);
                return null;
            }
            String $$4 = this.cleanupDigits(this.sign);
            int $$5 = switch (this.base.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> 2;
                case 1 -> 10;
                case 2 -> 16;
            };
            try {
                if ($$3) {
                    return (T)(switch ($$1.ordinal()) {
                        case 2 -> $$0.createByte(Byte.parseByte($$4, $$5));
                        case 3 -> $$0.createShort(Short.parseShort($$4, $$5));
                        case 4 -> $$0.createInt(Integer.parseInt($$4, $$5));
                        case 5 -> $$0.createLong(Long.parseLong($$4, $$5));
                        default -> {
                            $$2.errorCollector().store($$2.mark(), ERROR_EXPECTED_INTEGER_TYPE);
                            yield null;
                        }
                    });
                }
                return (T)(switch ($$1.ordinal()) {
                    case 2 -> $$0.createByte(UnsignedBytes.parseUnsignedByte((String)$$4, (int)$$5));
                    case 3 -> $$0.createShort(SnbtGrammar.parseUnsignedShort($$4, $$5));
                    case 4 -> $$0.createInt(Integer.parseUnsignedInt($$4, $$5));
                    case 5 -> $$0.createLong(Long.parseUnsignedLong($$4, $$5));
                    default -> {
                        $$2.errorCollector().store($$2.mark(), ERROR_EXPECTED_INTEGER_TYPE);
                        yield null;
                    }
                });
            }
            catch (NumberFormatException $$6) {
                $$2.errorCollector().store($$2.mark(), SnbtGrammar.createNumberParseError($$6));
                return null;
            }
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{IntegerLiteral.class, "sign;base;digits;suffix", "sign", "base", "digits", "suffix"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{IntegerLiteral.class, "sign;base;digits;suffix", "sign", "base", "digits", "suffix"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{IntegerLiteral.class, "sign;base;digits;suffix", "sign", "base", "digits", "suffix"}, this, $$0);
        }

        public Sign sign() {
            return this.sign;
        }

        public Base base() {
            return this.base;
        }

        public String digits() {
            return this.digits;
        }

        public IntegerSuffix suffix() {
            return this.suffix;
        }
    }

    static enum Base {
        BINARY,
        DECIMAL,
        HEX;

    }
}

