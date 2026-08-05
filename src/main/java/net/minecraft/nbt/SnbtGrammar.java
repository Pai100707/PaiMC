package net.minecraft.nbt;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.primitives.UnsignedBytes;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.chars.CharList;
import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
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
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms.TerminalCharacters;

public class SnbtGrammar {
   private static final DynamicCommandExceptionType ERROR_NUMBER_PARSE_FAILURE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("snbt.parser.number_parse_failure", new Object[]{$$0})
   );
   static final DynamicCommandExceptionType ERROR_EXPECTED_HEX_ESCAPE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("snbt.parser.expected_hex_escape", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_INVALID_CODEPOINT = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("snbt.parser.invalid_codepoint", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_NO_SUCH_OPERATION = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("snbt.parser.no_such_operation", new Object[]{$$0})
   );
   static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_INTEGER_TYPE = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.expected_integer_type"))
   );
   private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_FLOAT_TYPE = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.expected_float_type"))
   );
   static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_NON_NEGATIVE_NUMBER = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.expected_non_negative_number"))
   );
   private static final DelayedException<CommandSyntaxException> ERROR_INVALID_CHARACTER_NAME = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.invalid_character_name"))
   );
   static final DelayedException<CommandSyntaxException> ERROR_INVALID_ARRAY_ELEMENT_TYPE = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.invalid_array_element_type"))
   );
   private static final DelayedException<CommandSyntaxException> ERROR_INVALID_UNQUOTED_START = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.invalid_unquoted_start"))
   );
   private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_UNQUOTED_STRING = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.expected_unquoted_string"))
   );
   private static final DelayedException<CommandSyntaxException> ERROR_INVALID_STRING_CONTENTS = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.invalid_string_contents"))
   );
   private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_BINARY_NUMERAL = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.expected_binary_numeral"))
   );
   private static final DelayedException<CommandSyntaxException> ERROR_UNDESCORE_NOT_ALLOWED = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.underscore_not_allowed"))
   );
   private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_DECIMAL_NUMERAL = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.expected_decimal_numeral"))
   );
   private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_HEX_NUMERAL = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.expected_hex_numeral"))
   );
   private static final DelayedException<CommandSyntaxException> ERROR_EMPTY_KEY = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.empty_key"))
   );
   private static final DelayedException<CommandSyntaxException> ERROR_LEADING_ZERO_NOT_ALLOWED = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.leading_zero_not_allowed"))
   );
   private static final DelayedException<CommandSyntaxException> ERROR_INFINITY_NOT_ALLOWED = DelayedException.create(
      new SimpleCommandExceptionType(Component.translatable("snbt.parser.infinity_not_allowed"))
   );
   private static final HexFormat HEX_ESCAPE = HexFormat.of().withUpperCase();
   private static final NumberRunParseRule BINARY_NUMERAL = new NumberRunParseRule(ERROR_EXPECTED_BINARY_NUMERAL, ERROR_UNDESCORE_NOT_ALLOWED) {
      protected boolean isAccepted(char $$0) {
         return switch ($$0) {
            case '0', '1', '_' -> true;
            default -> false;
         };
      }
   };
   private static final NumberRunParseRule DECIMAL_NUMERAL = new NumberRunParseRule(ERROR_EXPECTED_DECIMAL_NUMERAL, ERROR_UNDESCORE_NOT_ALLOWED) {
      protected boolean isAccepted(char $$0) {
         return switch ($$0) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' -> true;
            default -> false;
         };
      }
   };
   private static final NumberRunParseRule HEX_NUMERAL = new NumberRunParseRule(ERROR_EXPECTED_HEX_NUMERAL, ERROR_UNDESCORE_NOT_ALLOWED) {
      protected boolean isAccepted(char $$0) {
         return switch ($$0) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', '_', 'a', 'b', 'c', 'd', 'e', 'f' -> true;
            default -> false;
         };
      }
   };
   private static final GreedyPredicateParseRule PLAIN_STRING_CHUNK = new GreedyPredicateParseRule(1, ERROR_INVALID_STRING_CONTENTS) {
      protected boolean isAccepted(char $$0) {
         return switch ($$0) {
            case '"', '\'', '\\' -> false;
            default -> true;
         };
      }
   };
   private static final TerminalCharacters NUMBER_LOOKEAHEAD = new TerminalCharacters(CharList.of()) {
      protected boolean isAccepted(char $$0) {
         return net.minecraft.nbt.SnbtGrammar.canStartNumber($$0);
      }
   };
   private static final Pattern UNICODE_NAME = Pattern.compile("[-a-zA-Z0-9 ]+");

   static DelayedException<CommandSyntaxException> createNumberParseError(NumberFormatException $$0) {
      return DelayedException.create(ERROR_NUMBER_PARSE_FAILURE, $$0.getMessage());
   }

   
   public static String escapeControlCharacters(char $$0) {
      return switch ($$0) {
         case '\b' -> "b";
         case '\t' -> "t";
         case '\n' -> "n";
         default -> $$0 < ' ' ? "x" + HEX_ESCAPE.toHexDigits((byte)$$0) : null;
         case '\f' -> "f";
         case '\r' -> "r";
      };
   }

   private static boolean isAllowedToStartUnquotedString(char $$0) {
      return !canStartNumber($$0);
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
      cleanAndAppend($$0, $$1, needsUnderscoreRemoval($$1));
   }

   static void cleanAndAppend(StringBuilder $$0, String $$1, boolean $$2) {
      if ($$2) {
         for (char $$3 : $$1.toCharArray()) {
            if ($$3 != '_') {
               $$0.append($$3);
            }
         }
      } else {
         $$0.append($$1);
      }
   }

   static short parseUnsignedShort(String $$0, int $$1) {
      int $$2 = Integer.parseInt($$0, $$1);
      if ($$2 >> 16 == 0) {
         return (short)$$2;
      } else {
         throw new NumberFormatException("out of range: " + $$2);
      }
   }

   
   private static <T> T createFloat(
      DynamicOps<T> $$0,
      net.minecraft.nbt.SnbtGrammar.Sign $$1,
      String $$2,
      String $$3,
      net.minecraft.nbt.SnbtGrammar.Signed<String> $$4,
      net.minecraft.nbt.SnbtGrammar.TypeSuffix $$5,
      ParseState<?> $$6
   ) {
      StringBuilder $$7 = new StringBuilder();
      $$1.append($$7);
      if ($$2 != null) {
         cleanAndAppend($$7, $$2);
      }

      if ($$3 != null) {
         $$7.append('.');
         cleanAndAppend($$7, $$3);
      }

      if ($$4 != null) {
         $$7.append('e');
         $$4.sign().append($$7);
         cleanAndAppend($$7, $$4.value);
      }

      try {
         String $$8 = $$7.toString();

         return (T)(switch ($$5) {
            case null -> (Object)convertDouble($$0, $$6, $$8);
            case FLOAT -> (Object)convertFloat($$0, $$6, $$8);
            case DOUBLE -> (Object)convertDouble($$0, $$6, $$8);
            default -> {
               $$6.errorCollector().store($$6.mark(), ERROR_EXPECTED_FLOAT_TYPE);
               yield null;
            }
         });
      } catch (NumberFormatException var11) {
         $$6.errorCollector().store($$6.mark(), createNumberParseError(var11));
         return null;
      }
   }

   
   private static <T> T convertFloat(DynamicOps<T> $$0, ParseState<?> $$1, String $$2) {
      float $$3 = Float.parseFloat($$2);
      if (!Float.isFinite($$3)) {
         $$1.errorCollector().store($$1.mark(), ERROR_INFINITY_NOT_ALLOWED);
         return null;
      } else {
         return (T)$$0.createFloat($$3);
      }
   }

   
   private static <T> T convertDouble(DynamicOps<T> $$0, ParseState<?> $$1, String $$2) {
      double $$3 = Double.parseDouble($$2);
      if (!Double.isFinite($$3)) {
         $$1.errorCollector().store($$1.mark(), ERROR_INFINITY_NOT_ALLOWED);
         return null;
      } else {
         return (T)$$0.createDouble($$3);
      }
   }

   private static String joinList(List<String> $$0) {
      return switch ($$0.size()) {
         case 0 -> "";
         case 1 -> (String)$$0.getFirst();
         default -> String.join("", $$0);
      };
   }

   public static <T> Grammar<T> createParser(DynamicOps<T> $$0) {
      T $$1 = (T)$$0.createBoolean(true);
      T $$2 = (T)$$0.createBoolean(false);
      T $$3 = (T)$$0.emptyMap();
      T $$4 = (T)$$0.emptyList();
      Dictionary<StringReader> $$5 = new Dictionary();
      Atom<net.minecraft.nbt.SnbtGrammar.Sign> $$6 = Atom.of("sign");
      $$5.put(
         $$6,
         Term.alternative(
            new Term[]{
               Term.sequence(new Term[]{StringReaderTerms.character('+'), Term.marker($$6, net.minecraft.nbt.SnbtGrammar.Sign.PLUS)}),
               Term.sequence(new Term[]{StringReaderTerms.character('-'), Term.marker($$6, net.minecraft.nbt.SnbtGrammar.Sign.MINUS)})
            }
         ),
         $$1x -> (net.minecraft.nbt.SnbtGrammar.Sign)$$1x.getOrThrow($$6)
      );
      Atom<net.minecraft.nbt.SnbtGrammar.IntegerSuffix> $$7 = Atom.of("integer_suffix");
      $$5.put(
         $$7,
         Term.alternative(
            new Term[]{
               Term.sequence(
                  new Term[]{
                     StringReaderTerms.characters('u', 'U'),
                     Term.alternative(
                        new Term[]{
                           Term.sequence(
                              new Term[]{
                                 StringReaderTerms.characters('b', 'B'),
                                 Term.marker(
                                    $$7,
                                    new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(
                                       net.minecraft.nbt.SnbtGrammar.SignedPrefix.UNSIGNED, net.minecraft.nbt.SnbtGrammar.TypeSuffix.BYTE
                                    )
                                 )
                              }
                           ),
                           Term.sequence(
                              new Term[]{
                                 StringReaderTerms.characters('s', 'S'),
                                 Term.marker(
                                    $$7,
                                    new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(
                                       net.minecraft.nbt.SnbtGrammar.SignedPrefix.UNSIGNED, net.minecraft.nbt.SnbtGrammar.TypeSuffix.SHORT
                                    )
                                 )
                              }
                           ),
                           Term.sequence(
                              new Term[]{
                                 StringReaderTerms.characters('i', 'I'),
                                 Term.marker(
                                    $$7,
                                    new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(
                                       net.minecraft.nbt.SnbtGrammar.SignedPrefix.UNSIGNED, net.minecraft.nbt.SnbtGrammar.TypeSuffix.INT
                                    )
                                 )
                              }
                           ),
                           Term.sequence(
                              new Term[]{
                                 StringReaderTerms.characters('l', 'L'),
                                 Term.marker(
                                    $$7,
                                    new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(
                                       net.minecraft.nbt.SnbtGrammar.SignedPrefix.UNSIGNED, net.minecraft.nbt.SnbtGrammar.TypeSuffix.LONG
                                    )
                                 )
                              }
                           )
                        }
                     )
                  }
               ),
               Term.sequence(
                  new Term[]{
                     StringReaderTerms.characters('s', 'S'),
                     Term.alternative(
                        new Term[]{
                           Term.sequence(
                              new Term[]{
                                 StringReaderTerms.characters('b', 'B'),
                                 Term.marker(
                                    $$7,
                                    new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(
                                       net.minecraft.nbt.SnbtGrammar.SignedPrefix.SIGNED, net.minecraft.nbt.SnbtGrammar.TypeSuffix.BYTE
                                    )
                                 )
                              }
                           ),
                           Term.sequence(
                              new Term[]{
                                 StringReaderTerms.characters('s', 'S'),
                                 Term.marker(
                                    $$7,
                                    new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(
                                       net.minecraft.nbt.SnbtGrammar.SignedPrefix.SIGNED, net.minecraft.nbt.SnbtGrammar.TypeSuffix.SHORT
                                    )
                                 )
                              }
                           ),
                           Term.sequence(
                              new Term[]{
                                 StringReaderTerms.characters('i', 'I'),
                                 Term.marker(
                                    $$7,
                                    new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(
                                       net.minecraft.nbt.SnbtGrammar.SignedPrefix.SIGNED, net.minecraft.nbt.SnbtGrammar.TypeSuffix.INT
                                    )
                                 )
                              }
                           ),
                           Term.sequence(
                              new Term[]{
                                 StringReaderTerms.characters('l', 'L'),
                                 Term.marker(
                                    $$7,
                                    new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(
                                       net.minecraft.nbt.SnbtGrammar.SignedPrefix.SIGNED, net.minecraft.nbt.SnbtGrammar.TypeSuffix.LONG
                                    )
                                 )
                              }
                           )
                        }
                     )
                  }
               ),
               Term.sequence(
                  new Term[]{
                     StringReaderTerms.characters('b', 'B'),
                     Term.marker($$7, new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(null, net.minecraft.nbt.SnbtGrammar.TypeSuffix.BYTE))
                  }
               ),
               Term.sequence(
                  new Term[]{
                     StringReaderTerms.characters('s', 'S'),
                     Term.marker($$7, new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(null, net.minecraft.nbt.SnbtGrammar.TypeSuffix.SHORT))
                  }
               ),
               Term.sequence(
                  new Term[]{
                     StringReaderTerms.characters('i', 'I'),
                     Term.marker($$7, new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(null, net.minecraft.nbt.SnbtGrammar.TypeSuffix.INT))
                  }
               ),
               Term.sequence(
                  new Term[]{
                     StringReaderTerms.characters('l', 'L'),
                     Term.marker($$7, new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(null, net.minecraft.nbt.SnbtGrammar.TypeSuffix.LONG))
                  }
               )
            }
         ),
         $$1x -> (net.minecraft.nbt.SnbtGrammar.IntegerSuffix)$$1x.getOrThrow($$7)
      );
      Atom<String> $$8 = Atom.of("binary_numeral");
      $$5.put($$8, BINARY_NUMERAL);
      Atom<String> $$9 = Atom.of("decimal_numeral");
      $$5.put($$9, DECIMAL_NUMERAL);
      Atom<String> $$10 = Atom.of("hex_numeral");
      $$5.put($$10, HEX_NUMERAL);
      Atom<net.minecraft.nbt.SnbtGrammar.IntegerLiteral> $$11 = Atom.of("integer_literal");
      NamedRule<StringReader, net.minecraft.nbt.SnbtGrammar.IntegerLiteral> $$12 = $$5.put(
         $$11,
         Term.sequence(
            new Term[]{
               Term.optional($$5.named($$6)),
               Term.alternative(
                  new Term[]{
                     Term.sequence(
                        new Term[]{
                           StringReaderTerms.character('0'),
                           Term.cut(),
                           Term.alternative(
                              new Term[]{
                                 Term.sequence(new Term[]{StringReaderTerms.characters('x', 'X'), Term.cut(), $$5.named($$10)}),
                                 Term.sequence(new Term[]{StringReaderTerms.characters('b', 'B'), $$5.named($$8)}),
                                 Term.sequence(new Term[]{$$5.named($$9), Term.cut(), Term.fail(ERROR_LEADING_ZERO_NOT_ALLOWED)}),
                                 Term.marker($$9, "0")
                              }
                           )
                        }
                     ),
                     $$5.named($$9)
                  }
               ),
               Term.optional($$5.named($$7))
            }
         ),
         $$5x -> {
            net.minecraft.nbt.SnbtGrammar.IntegerSuffix $$6x = (net.minecraft.nbt.SnbtGrammar.IntegerSuffix)$$5x.getOrDefault(
               $$7, net.minecraft.nbt.SnbtGrammar.IntegerSuffix.EMPTY
            );
            net.minecraft.nbt.SnbtGrammar.Sign $$7x = (net.minecraft.nbt.SnbtGrammar.Sign)$$5x.getOrDefault($$6, net.minecraft.nbt.SnbtGrammar.Sign.PLUS);
            String $$8x = (String)$$5x.get($$9);
            if ($$8x != null) {
               return new net.minecraft.nbt.SnbtGrammar.IntegerLiteral($$7x, net.minecraft.nbt.SnbtGrammar.Base.DECIMAL, $$8x, $$6x);
            } else {
               String $$9x = (String)$$5x.get($$10);
               if ($$9x != null) {
                  return new net.minecraft.nbt.SnbtGrammar.IntegerLiteral($$7x, net.minecraft.nbt.SnbtGrammar.Base.HEX, $$9x, $$6x);
               } else {
                  String $$10x = (String)$$5x.getOrThrow($$8);
                  return new net.minecraft.nbt.SnbtGrammar.IntegerLiteral($$7x, net.minecraft.nbt.SnbtGrammar.Base.BINARY, $$10x, $$6x);
               }
            }
         }
      );
      Atom<net.minecraft.nbt.SnbtGrammar.TypeSuffix> $$13 = Atom.of("float_type_suffix");
      $$5.put(
         $$13,
         Term.alternative(
            new Term[]{
               Term.sequence(new Term[]{StringReaderTerms.characters('f', 'F'), Term.marker($$13, net.minecraft.nbt.SnbtGrammar.TypeSuffix.FLOAT)}),
               Term.sequence(new Term[]{StringReaderTerms.characters('d', 'D'), Term.marker($$13, net.minecraft.nbt.SnbtGrammar.TypeSuffix.DOUBLE)})
            }
         ),
         $$1x -> (net.minecraft.nbt.SnbtGrammar.TypeSuffix)$$1x.getOrThrow($$13)
      );
      Atom<net.minecraft.nbt.SnbtGrammar.Signed<String>> $$14 = Atom.of("float_exponent_part");
      $$5.put(
         $$14,
         Term.sequence(new Term[]{StringReaderTerms.characters('e', 'E'), Term.optional($$5.named($$6)), $$5.named($$9)}),
         $$2x -> new net.minecraft.nbt.SnbtGrammar.Signed<>(
            (net.minecraft.nbt.SnbtGrammar.Sign)$$2x.getOrDefault($$6, net.minecraft.nbt.SnbtGrammar.Sign.PLUS), (String)$$2x.getOrThrow($$9)
         )
      );
      Atom<String> $$15 = Atom.of("float_whole_part");
      Atom<String> $$16 = Atom.of("float_fraction_part");
      Atom<T> $$17 = Atom.of("float_literal");
      $$5.putComplex(
         $$17,
         Term.sequence(
            new Term[]{
               Term.optional($$5.named($$6)),
               Term.alternative(
                  new Term[]{
                     Term.sequence(
                        new Term[]{
                           $$5.namedWithAlias($$9, $$15),
                           StringReaderTerms.character('.'),
                           Term.cut(),
                           Term.optional($$5.namedWithAlias($$9, $$16)),
                           Term.optional($$5.named($$14)),
                           Term.optional($$5.named($$13))
                        }
                     ),
                     Term.sequence(
                        new Term[]{
                           StringReaderTerms.character('.'),
                           Term.cut(),
                           $$5.namedWithAlias($$9, $$16),
                           Term.optional($$5.named($$14)),
                           Term.optional($$5.named($$13))
                        }
                     ),
                     Term.sequence(new Term[]{$$5.namedWithAlias($$9, $$15), $$5.named($$14), Term.cut(), Term.optional($$5.named($$13))}),
                     Term.sequence(new Term[]{$$5.namedWithAlias($$9, $$15), Term.optional($$5.named($$14)), $$5.named($$13)})
                  }
               )
            }
         ),
         $$6x -> {
            Scope $$7x = $$6x.scope();
            net.minecraft.nbt.SnbtGrammar.Sign $$8x = (net.minecraft.nbt.SnbtGrammar.Sign)$$7x.getOrDefault($$6, net.minecraft.nbt.SnbtGrammar.Sign.PLUS);
            String $$9x = (String)$$7x.get($$15);
            String $$10x = (String)$$7x.get($$16);
            net.minecraft.nbt.SnbtGrammar.Signed<String> $$11x = (net.minecraft.nbt.SnbtGrammar.Signed<String>)$$7x.get($$14);
            net.minecraft.nbt.SnbtGrammar.TypeSuffix $$12x = (net.minecraft.nbt.SnbtGrammar.TypeSuffix)$$7x.get($$13);
            return createFloat($$0, $$8x, $$9x, $$10x, $$11x, $$12x, $$6x);
         }
      );
      Atom<String> $$18 = Atom.of("string_hex_2");
      $$5.put($$18, new net.minecraft.nbt.SnbtGrammar.SimpleHexLiteralParseRule(2));
      Atom<String> $$19 = Atom.of("string_hex_4");
      $$5.put($$19, new net.minecraft.nbt.SnbtGrammar.SimpleHexLiteralParseRule(4));
      Atom<String> $$20 = Atom.of("string_hex_8");
      $$5.put($$20, new net.minecraft.nbt.SnbtGrammar.SimpleHexLiteralParseRule(8));
      Atom<String> $$21 = Atom.of("string_unicode_name");
      $$5.put($$21, new GreedyPatternParseRule(UNICODE_NAME, ERROR_INVALID_CHARACTER_NAME));
      Atom<String> $$22 = Atom.of("string_escape_sequence");
      $$5.putComplex(
         $$22,
         Term.alternative(
            new Term[]{
               Term.sequence(new Term[]{StringReaderTerms.character('b'), Term.marker($$22, "\b")}),
               Term.sequence(new Term[]{StringReaderTerms.character('s'), Term.marker($$22, " ")}),
               Term.sequence(new Term[]{StringReaderTerms.character('t'), Term.marker($$22, "\t")}),
               Term.sequence(new Term[]{StringReaderTerms.character('n'), Term.marker($$22, "\n")}),
               Term.sequence(new Term[]{StringReaderTerms.character('f'), Term.marker($$22, "\f")}),
               Term.sequence(new Term[]{StringReaderTerms.character('r'), Term.marker($$22, "\r")}),
               Term.sequence(new Term[]{StringReaderTerms.character('\\'), Term.marker($$22, "\\")}),
               Term.sequence(new Term[]{StringReaderTerms.character('\''), Term.marker($$22, "'")}),
               Term.sequence(new Term[]{StringReaderTerms.character('"'), Term.marker($$22, "\"")}),
               Term.sequence(new Term[]{StringReaderTerms.character('x'), $$5.named($$18)}),
               Term.sequence(new Term[]{StringReaderTerms.character('u'), $$5.named($$19)}),
               Term.sequence(new Term[]{StringReaderTerms.character('U'), $$5.named($$20)}),
               Term.sequence(new Term[]{StringReaderTerms.character('N'), StringReaderTerms.character('{'), $$5.named($$21), StringReaderTerms.character('}')})
            }
         ),
         $$5x -> {
            Scope $$6x = $$5x.scope();
            String $$7x = (String)$$6x.getAny(new Atom[]{$$22});
            if ($$7x != null) {
               return $$7x;
            } else {
               String $$8x = (String)$$6x.getAny(new Atom[]{$$18, $$19, $$20});
               if ($$8x != null) {
                  int $$9x = HexFormat.fromHexDigits($$8x);
                  if (!Character.isValidCodePoint($$9x)) {
                     $$5x.errorCollector().store($$5x.mark(), DelayedException.create(ERROR_INVALID_CODEPOINT, String.format(Locale.ROOT, "U+%08X", $$9x)));
                     return null;
                  } else {
                     return Character.toString($$9x);
                  }
               } else {
                  String $$10x = (String)$$6x.getOrThrow($$21);

                  int $$11x;
                  try {
                     $$11x = Character.codePointOf($$10x);
                  } catch (IllegalArgumentException var12x) {
                     $$5x.errorCollector().store($$5x.mark(), ERROR_INVALID_CHARACTER_NAME);
                     return null;
                  }

                  return Character.toString($$11x);
               }
            }
         }
      );
      Atom<String> $$23 = Atom.of("string_plain_contents");
      $$5.put($$23, PLAIN_STRING_CHUNK);
      Atom<List<String>> $$24 = Atom.of("string_chunks");
      Atom<String> $$25 = Atom.of("string_contents");
      Atom<String> $$26 = Atom.of("single_quoted_string_chunk");
      NamedRule<StringReader, String> $$27 = $$5.put(
         $$26,
         Term.alternative(
            new Term[]{
               $$5.namedWithAlias($$23, $$25),
               Term.sequence(new Term[]{StringReaderTerms.character('\\'), $$5.namedWithAlias($$22, $$25)}),
               Term.sequence(new Term[]{StringReaderTerms.character('"'), Term.marker($$25, "\"")})
            }
         ),
         $$1x -> (String)$$1x.getOrThrow($$25)
      );
      Atom<String> $$28 = Atom.of("single_quoted_string_contents");
      $$5.put($$28, Term.repeated($$27, $$24), $$1x -> joinList((List<String>)$$1x.getOrThrow($$24)));
      Atom<String> $$29 = Atom.of("double_quoted_string_chunk");
      NamedRule<StringReader, String> $$30 = $$5.put(
         $$29,
         Term.alternative(
            new Term[]{
               $$5.namedWithAlias($$23, $$25),
               Term.sequence(new Term[]{StringReaderTerms.character('\\'), $$5.namedWithAlias($$22, $$25)}),
               Term.sequence(new Term[]{StringReaderTerms.character('\''), Term.marker($$25, "'")})
            }
         ),
         $$1x -> (String)$$1x.getOrThrow($$25)
      );
      Atom<String> $$31 = Atom.of("double_quoted_string_contents");
      $$5.put($$31, Term.repeated($$30, $$24), $$1x -> joinList((List<String>)$$1x.getOrThrow($$24)));
      Atom<String> $$32 = Atom.of("quoted_string_literal");
      $$5.put(
         $$32,
         Term.alternative(
            new Term[]{
               Term.sequence(
                  new Term[]{StringReaderTerms.character('"'), Term.cut(), Term.optional($$5.namedWithAlias($$31, $$25)), StringReaderTerms.character('"')}
               ),
               Term.sequence(new Term[]{StringReaderTerms.character('\''), Term.optional($$5.namedWithAlias($$28, $$25)), StringReaderTerms.character('\'')})
            }
         ),
         $$1x -> (String)$$1x.getOrThrow($$25)
      );
      Atom<String> $$33 = Atom.of("unquoted_string");
      $$5.put($$33, new UnquotedStringParseRule(1, ERROR_EXPECTED_UNQUOTED_STRING));
      Atom<T> $$34 = Atom.of("literal");
      Atom<List<T>> $$35 = Atom.of("arguments");
      $$5.put($$35, Term.repeatedWithTrailingSeparator($$5.forward($$34), $$35, StringReaderTerms.character(',')), $$1x -> (List)$$1x.getOrThrow($$35));
      Atom<T> $$36 = Atom.of("unquoted_string_or_builtin");
      $$5.putComplex(
         $$36,
         Term.sequence(
            new Term[]{
               $$5.named($$33), Term.optional(Term.sequence(new Term[]{StringReaderTerms.character('('), $$5.named($$35), StringReaderTerms.character(')')}))
            }
         ),
         $$5x -> {
            Scope $$6x = $$5x.scope();
            String $$7x = (String)$$6x.getOrThrow($$33);
            if (!$$7x.isEmpty() && isAllowedToStartUnquotedString($$7x.charAt(0))) {
               List<T> $$8x = (List<T>)$$6x.get($$35);
               if ($$8x != null) {
                  net.minecraft.nbt.SnbtOperations.BuiltinKey $$9x = new net.minecraft.nbt.SnbtOperations.BuiltinKey($$7x, $$8x.size());
                  net.minecraft.nbt.SnbtOperations.BuiltinOperation $$10x = net.minecraft.nbt.SnbtOperations.BUILTIN_OPERATIONS.get($$9x);
                  if ($$10x != null) {
                     return $$10x.run($$0, $$8x, $$5x);
                  } else {
                     $$5x.errorCollector().store($$5x.mark(), DelayedException.create(ERROR_NO_SUCH_OPERATION, $$9x.toString()));
                     return null;
                  }
               } else if ($$7x.equalsIgnoreCase("true")) {
                  return $$1;
               } else {
                  return $$7x.equalsIgnoreCase("false") ? $$2 : $$0.createString($$7x);
               }
            } else {
               $$5x.errorCollector().store($$5x.mark(), net.minecraft.nbt.SnbtOperations.BUILTIN_IDS, ERROR_INVALID_UNQUOTED_START);
               return null;
            }
         }
      );
      Atom<String> $$37 = Atom.of("map_key");
      $$5.put($$37, Term.alternative(new Term[]{$$5.named($$32), $$5.named($$33)}), $$2x -> (String)$$2x.getAnyOrThrow(new Atom[]{$$32, $$33}));
      Atom<Entry<String, T>> $$38 = Atom.of("map_entry");
      NamedRule<StringReader, Entry<String, T>> $$39 = $$5.putComplex(
         $$38, Term.sequence(new Term[]{$$5.named($$37), StringReaderTerms.character(':'), $$5.named($$34)}), $$2x -> {
            Scope $$3x = $$2x.scope();
            String $$4x = (String)$$3x.getOrThrow($$37);
            if ($$4x.isEmpty()) {
               $$2x.errorCollector().store($$2x.mark(), ERROR_EMPTY_KEY);
               return null;
            } else {
               T $$5x = (T)$$3x.getOrThrow($$34);
               return Map.entry($$4x, $$5x);
            }
         }
      );
      Atom<List<Entry<String, T>>> $$40 = Atom.of("map_entries");
      $$5.put($$40, Term.repeatedWithTrailingSeparator($$39, $$40, StringReaderTerms.character(',')), $$1x -> (List)$$1x.getOrThrow($$40));
      Atom<T> $$41 = Atom.of("map_literal");
      $$5.put($$41, Term.sequence(new Term[]{StringReaderTerms.character('{'), $$5.named($$40), StringReaderTerms.character('}')}), $$3x -> {
         List<Entry<String, T>> $$4x = (List<Entry<String, T>>)$$3x.getOrThrow($$40);
         if ($$4x.isEmpty()) {
            return $$3;
         } else {
            Builder<T, T> $$5x = ImmutableMap.builderWithExpectedSize($$4x.size());

            for (Entry<String, T> $$6x : $$4x) {
               $$5x.put($$0.createString($$6x.getKey()), $$6x.getValue());
            }

            return $$0.createMap($$5x.buildKeepingLast());
         }
      });
      Atom<List<T>> $$42 = Atom.of("list_entries");
      $$5.put($$42, Term.repeatedWithTrailingSeparator($$5.forward($$34), $$42, StringReaderTerms.character(',')), $$1x -> (List)$$1x.getOrThrow($$42));
      Atom<net.minecraft.nbt.SnbtGrammar.ArrayPrefix> $$43 = Atom.of("array_prefix");
      $$5.put(
         $$43,
         Term.alternative(
            new Term[]{
               Term.sequence(new Term[]{StringReaderTerms.character('B'), Term.marker($$43, net.minecraft.nbt.SnbtGrammar.ArrayPrefix.BYTE)}),
               Term.sequence(new Term[]{StringReaderTerms.character('L'), Term.marker($$43, net.minecraft.nbt.SnbtGrammar.ArrayPrefix.LONG)}),
               Term.sequence(new Term[]{StringReaderTerms.character('I'), Term.marker($$43, net.minecraft.nbt.SnbtGrammar.ArrayPrefix.INT)})
            }
         ),
         $$1x -> (net.minecraft.nbt.SnbtGrammar.ArrayPrefix)$$1x.getOrThrow($$43)
      );
      Atom<List<net.minecraft.nbt.SnbtGrammar.IntegerLiteral>> $$44 = Atom.of("int_array_entries");
      $$5.put($$44, Term.repeatedWithTrailingSeparator($$12, $$44, StringReaderTerms.character(',')), $$1x -> (List)$$1x.getOrThrow($$44));
      Atom<T> $$45 = Atom.of("list_literal");
      $$5.putComplex(
         $$45,
         Term.sequence(
            new Term[]{
               StringReaderTerms.character('['),
               Term.alternative(new Term[]{Term.sequence(new Term[]{$$5.named($$43), StringReaderTerms.character(';'), $$5.named($$44)}), $$5.named($$42)}),
               StringReaderTerms.character(']')
            }
         ),
         $$5x -> {
            Scope $$6x = $$5x.scope();
            net.minecraft.nbt.SnbtGrammar.ArrayPrefix $$7x = (net.minecraft.nbt.SnbtGrammar.ArrayPrefix)$$6x.get($$43);
            if ($$7x != null) {
               List<net.minecraft.nbt.SnbtGrammar.IntegerLiteral> $$8x = (List<net.minecraft.nbt.SnbtGrammar.IntegerLiteral>)$$6x.getOrThrow($$44);
               return $$8x.isEmpty() ? $$7x.create($$0) : $$7x.create($$0, $$8x, $$5x);
            } else {
               List<T> $$9x = (List<T>)$$6x.getOrThrow($$42);
               return $$9x.isEmpty() ? $$4 : $$0.createList($$9x.stream());
            }
         }
      );
      NamedRule<StringReader, T> $$46 = $$5.putComplex(
         $$34,
         Term.alternative(
            new Term[]{
               Term.sequence(
                  new Term[]{Term.positiveLookahead(NUMBER_LOOKEAHEAD), Term.alternative(new Term[]{$$5.namedWithAlias($$17, $$34), $$5.named($$11)})}
               ),
               Term.sequence(new Term[]{Term.positiveLookahead(StringReaderTerms.characters('"', '\'')), Term.cut(), $$5.named($$32)}),
               Term.sequence(new Term[]{Term.positiveLookahead(StringReaderTerms.character('{')), Term.cut(), $$5.namedWithAlias($$41, $$34)}),
               Term.sequence(new Term[]{Term.positiveLookahead(StringReaderTerms.character('[')), Term.cut(), $$5.namedWithAlias($$45, $$34)}),
               $$5.namedWithAlias($$36, $$34)
            }
         ),
         $$4x -> {
            Scope $$5x = $$4x.scope();
            String $$6x = (String)$$5x.get($$32);
            if ($$6x != null) {
               return $$0.createString($$6x);
            } else {
               net.minecraft.nbt.SnbtGrammar.IntegerLiteral $$7x = (net.minecraft.nbt.SnbtGrammar.IntegerLiteral)$$5x.get($$11);
               return $$7x != null ? $$7x.create($$0, $$4x) : $$5x.getOrThrow($$34);
            }
         }
      );
      return new Grammar($$5, $$46);
   }

   static enum ArrayPrefix {
      BYTE(net.minecraft.nbt.SnbtGrammar.TypeSuffix.BYTE) {
         private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);

         @Override
         public <T> T create(DynamicOps<T> $$0) {
            return (T)$$0.createByteList(EMPTY_BUFFER);
         }

         
         @Override
         public <T> T create(DynamicOps<T> $$0, List<net.minecraft.nbt.SnbtGrammar.IntegerLiteral> $$1, ParseState<?> $$2) {
            ByteList $$3 = new ByteArrayList();

            for (net.minecraft.nbt.SnbtGrammar.IntegerLiteral $$4 : $$1) {
               Number $$5 = this.buildNumber($$4, $$2);
               if ($$5 == null) {
                  return null;
               }

               $$3.add($$5.byteValue());
            }

            return (T)$$0.createByteList(ByteBuffer.wrap($$3.toByteArray()));
         }
      },
      INT(net.minecraft.nbt.SnbtGrammar.TypeSuffix.INT, net.minecraft.nbt.SnbtGrammar.TypeSuffix.BYTE, net.minecraft.nbt.SnbtGrammar.TypeSuffix.SHORT) {
         @Override
         public <T> T create(DynamicOps<T> $$0) {
            return (T)$$0.createIntList(IntStream.empty());
         }

         
         @Override
         public <T> T create(DynamicOps<T> $$0, List<net.minecraft.nbt.SnbtGrammar.IntegerLiteral> $$1, ParseState<?> $$2) {
            java.util.stream.IntStream.Builder $$3 = IntStream.builder();

            for (net.minecraft.nbt.SnbtGrammar.IntegerLiteral $$4 : $$1) {
               Number $$5 = this.buildNumber($$4, $$2);
               if ($$5 == null) {
                  return null;
               }

               $$3.add($$5.intValue());
            }

            return (T)$$0.createIntList($$3.build());
         }
      },
      LONG(
         net.minecraft.nbt.SnbtGrammar.TypeSuffix.LONG,
         net.minecraft.nbt.SnbtGrammar.TypeSuffix.BYTE,
         net.minecraft.nbt.SnbtGrammar.TypeSuffix.SHORT,
         net.minecraft.nbt.SnbtGrammar.TypeSuffix.INT
      ) {
         @Override
         public <T> T create(DynamicOps<T> $$0) {
            return (T)$$0.createLongList(LongStream.empty());
         }

         
         @Override
         public <T> T create(DynamicOps<T> $$0, List<net.minecraft.nbt.SnbtGrammar.IntegerLiteral> $$1, ParseState<?> $$2) {
            java.util.stream.LongStream.Builder $$3 = LongStream.builder();

            for (net.minecraft.nbt.SnbtGrammar.IntegerLiteral $$4 : $$1) {
               Number $$5 = this.buildNumber($$4, $$2);
               if ($$5 == null) {
                  return null;
               }

               $$3.add($$5.longValue());
            }

            return (T)$$0.createLongList($$3.build());
         }
      };

      private final net.minecraft.nbt.SnbtGrammar.TypeSuffix defaultType;
      private final Set<net.minecraft.nbt.SnbtGrammar.TypeSuffix> additionalTypes;

      ArrayPrefix(final net.minecraft.nbt.SnbtGrammar.TypeSuffix $$0, final net.minecraft.nbt.SnbtGrammar.TypeSuffix... $$1) {
         this.additionalTypes = Set.of($$1);
         this.defaultType = $$0;
      }

      public boolean isAllowed(net.minecraft.nbt.SnbtGrammar.TypeSuffix $$0) {
         return $$0 == this.defaultType || this.additionalTypes.contains($$0);
      }

      public abstract <T> T create(DynamicOps<T> var1);

      
      public abstract <T> T create(DynamicOps<T> var1, List<net.minecraft.nbt.SnbtGrammar.IntegerLiteral> var2, ParseState<?> var3);

      
      protected Number buildNumber(net.minecraft.nbt.SnbtGrammar.IntegerLiteral $$0, ParseState<?> $$1) {
         net.minecraft.nbt.SnbtGrammar.TypeSuffix $$2 = this.computeType($$0.suffix);
         if ($$2 == null) {
            $$1.errorCollector().store($$1.mark(), net.minecraft.nbt.SnbtGrammar.ERROR_INVALID_ARRAY_ELEMENT_TYPE);
            return null;
         } else {
            return $$0.create(JavaOps.INSTANCE, $$2, $$1);
         }
      }

      
      private net.minecraft.nbt.SnbtGrammar.TypeSuffix computeType(net.minecraft.nbt.SnbtGrammar.IntegerSuffix $$0) {
         net.minecraft.nbt.SnbtGrammar.TypeSuffix $$1 = $$0.type();
         if ($$1 == null) {
            return this.defaultType;
         } else {
            return !this.isAllowed($$1) ? null : $$1;
         }
      }
   }

   static enum Base {
      BINARY,
      DECIMAL,
      HEX;
   }

   record IntegerLiteral(
      net.minecraft.nbt.SnbtGrammar.Sign sign, net.minecraft.nbt.SnbtGrammar.Base base, String digits, net.minecraft.nbt.SnbtGrammar.IntegerSuffix suffix
   ) {

      private net.minecraft.nbt.SnbtGrammar.SignedPrefix signedOrDefault() {
         if (this.suffix.signed != null) {
            return this.suffix.signed;
         } else {
            return switch (this.base) {
               case BINARY, HEX -> net.minecraft.nbt.SnbtGrammar.SignedPrefix.UNSIGNED;
               case DECIMAL -> net.minecraft.nbt.SnbtGrammar.SignedPrefix.SIGNED;
            };
         }
      }

      private String cleanupDigits(net.minecraft.nbt.SnbtGrammar.Sign $$0) {
         boolean $$1 = net.minecraft.nbt.SnbtGrammar.needsUnderscoreRemoval(this.digits);
         if ($$0 != net.minecraft.nbt.SnbtGrammar.Sign.MINUS && !$$1) {
            return this.digits;
         } else {
            StringBuilder $$2 = new StringBuilder();
            $$0.append($$2);
            net.minecraft.nbt.SnbtGrammar.cleanAndAppend($$2, this.digits, $$1);
            return $$2.toString();
         }
      }

      
      public <T> T create(DynamicOps<T> $$0, ParseState<?> $$1) {
         return this.create($$0, Objects.requireNonNullElse(this.suffix.type, net.minecraft.nbt.SnbtGrammar.TypeSuffix.INT), $$1);
      }

      
      public <T> T create(DynamicOps<T> $$0, net.minecraft.nbt.SnbtGrammar.TypeSuffix $$1, ParseState<?> $$2) {
         boolean $$3 = this.signedOrDefault() == net.minecraft.nbt.SnbtGrammar.SignedPrefix.SIGNED;
         if (!$$3 && this.sign == net.minecraft.nbt.SnbtGrammar.Sign.MINUS) {
            $$2.errorCollector().store($$2.mark(), net.minecraft.nbt.SnbtGrammar.ERROR_EXPECTED_NON_NEGATIVE_NUMBER);
            return null;
         } else {
            String $$4 = this.cleanupDigits(this.sign);

            int $$5 = switch (this.base) {
               case BINARY -> 2;
               case DECIMAL -> 10;
               case HEX -> 16;
            };

            try {
               if ($$3) {
                  return (T)(switch ($$1) {
                     case BYTE -> (Object)$$0.createByte(Byte.parseByte($$4, $$5));
                     case SHORT -> (Object)$$0.createShort(Short.parseShort($$4, $$5));
                     case INT -> (Object)$$0.createInt(Integer.parseInt($$4, $$5));
                     case LONG -> (Object)$$0.createLong(Long.parseLong($$4, $$5));
                     default -> {
                        $$2.errorCollector().store($$2.mark(), net.minecraft.nbt.SnbtGrammar.ERROR_EXPECTED_INTEGER_TYPE);
                        yield null;
                     }
                  });
               } else {
                  return (T)(switch ($$1) {
                     case BYTE -> (Object)$$0.createByte(UnsignedBytes.parseUnsignedByte($$4, $$5));
                     case SHORT -> (Object)$$0.createShort(net.minecraft.nbt.SnbtGrammar.parseUnsignedShort($$4, $$5));
                     case INT -> (Object)$$0.createInt(Integer.parseUnsignedInt($$4, $$5));
                     case LONG -> (Object)$$0.createLong(Long.parseUnsignedLong($$4, $$5));
                     default -> {
                        $$2.errorCollector().store($$2.mark(), net.minecraft.nbt.SnbtGrammar.ERROR_EXPECTED_INTEGER_TYPE);
                        yield null;
                     }
                  });
               }
            } catch (NumberFormatException var8) {
               $$2.errorCollector().store($$2.mark(), net.minecraft.nbt.SnbtGrammar.createNumberParseError(var8));
               return null;
            }
         }
      }
   }

   record IntegerSuffix(net.minecraft.nbt.SnbtGrammar.SignedPrefix signed, net.minecraft.nbt.SnbtGrammar.TypeSuffix type) {
      public static final net.minecraft.nbt.SnbtGrammar.IntegerSuffix EMPTY = new net.minecraft.nbt.SnbtGrammar.IntegerSuffix(null, null);
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

   record Signed<T>(net.minecraft.nbt.SnbtGrammar.Sign sign, T value) {
   }

   static enum SignedPrefix {
      SIGNED,
      UNSIGNED;
   }

   static class SimpleHexLiteralParseRule extends GreedyPredicateParseRule {
      public SimpleHexLiteralParseRule(int $$0) {
         super($$0, $$0, DelayedException.create(net.minecraft.nbt.SnbtGrammar.ERROR_EXPECTED_HEX_ESCAPE, String.valueOf($$0)));
      }

      protected boolean isAccepted(char $$0) {
         return switch ($$0) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f' -> true;
            default -> false;
         };
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
}
