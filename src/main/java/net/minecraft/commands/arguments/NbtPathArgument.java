package net.minecraft.commands.arguments;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NbtPathArgument implements ArgumentType<NbtPathArgument.NbtPath> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar", "foo[0]", "[0]", "[]", "{foo=bar}");
   public static final SimpleCommandExceptionType ERROR_INVALID_NODE = new SimpleCommandExceptionType(Component.translatable("arguments.nbtpath.node.invalid"));
   public static final SimpleCommandExceptionType ERROR_DATA_TOO_DEEP = new SimpleCommandExceptionType(Component.translatable("arguments.nbtpath.too_deep"));
   public static final DynamicCommandExceptionType ERROR_NOTHING_FOUND = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("arguments.nbtpath.nothing_found", new Object[]{$$0})
   );
   static final DynamicCommandExceptionType ERROR_EXPECTED_LIST = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.data.modify.expected_list", new Object[]{$$0})
   );
   static final DynamicCommandExceptionType ERROR_INVALID_INDEX = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.data.modify.invalid_index", new Object[]{$$0})
   );
   private static final char INDEX_MATCH_START = '[';
   private static final char INDEX_MATCH_END = ']';
   private static final char KEY_MATCH_START = '{';
   private static final char KEY_MATCH_END = '}';
   private static final char QUOTED_KEY_START = '"';
   private static final char SINGLE_QUOTED_KEY_START = '\'';

   public static NbtPathArgument nbtPath() {
      return new NbtPathArgument();
   }

   public static NbtPathArgument.NbtPath getPath(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (NbtPathArgument.NbtPath)$$0.getArgument($$1, NbtPathArgument.NbtPath.class);
   }

   public NbtPathArgument.NbtPath parse(StringReader $$0) throws CommandSyntaxException {
      List<NbtPathArgument.Node> $$1 = Lists.newArrayList();
      int $$2 = $$0.getCursor();
      Object2IntMap<NbtPathArgument.Node> $$3 = new Object2IntOpenHashMap();
      boolean $$4 = true;

      while ($$0.canRead() && $$0.peek() != ' ') {
         NbtPathArgument.Node $$5 = parseNode($$0, $$4);
         $$1.add($$5);
         $$3.put($$5, $$0.getCursor() - $$2);
         $$4 = false;
         if ($$0.canRead()) {
            char $$6 = $$0.peek();
            if ($$6 != ' ' && $$6 != '[' && $$6 != '{') {
               $$0.expect('.');
            }
         }
      }

      return new NbtPathArgument.NbtPath($$0.getString().substring($$2, $$0.getCursor()), $$1.toArray(new NbtPathArgument.Node[0]), $$3);
   }

   private static NbtPathArgument.Node parseNode(StringReader $$0, boolean $$1) throws CommandSyntaxException {
      return (NbtPathArgument.Node)(switch ($$0.peek()) {
         case '"', '\'' -> readObjectNode($$0, $$0.readString());
         case '[' -> {
            $$0.skip();
            int $$3 = $$0.peek();
            if ($$3 == 123) {
               CompoundTag $$4 = TagParser.parseCompoundAsArgument($$0);
               $$0.expect(']');
               yield new NbtPathArgument.MatchElementNode($$4);
            } else if ($$3 == 93) {
               $$0.skip();
               yield NbtPathArgument.AllElementsNode.INSTANCE;
            } else {
               int $$5 = $$0.readInt();
               $$0.expect(']');
               yield new NbtPathArgument.IndexedElementNode($$5);
            }
         }
         case '{' -> {
            if (!$$1) {
               throw ERROR_INVALID_NODE.createWithContext($$0);
            }

            CompoundTag $$2 = TagParser.parseCompoundAsArgument($$0);
            yield new NbtPathArgument.MatchRootObjectNode($$2);
         }
         default -> readObjectNode($$0, readUnquotedName($$0));
      });
   }

   private static NbtPathArgument.Node readObjectNode(StringReader $$0, String $$1) throws CommandSyntaxException {
      if ($$1.isEmpty()) {
         throw ERROR_INVALID_NODE.createWithContext($$0);
      } else if ($$0.canRead() && $$0.peek() == '{') {
         CompoundTag $$2 = TagParser.parseCompoundAsArgument($$0);
         return new NbtPathArgument.MatchObjectNode($$1, $$2);
      } else {
         return new NbtPathArgument.CompoundChildNode($$1);
      }
   }

   private static String readUnquotedName(StringReader $$0) throws CommandSyntaxException {
      int $$1 = $$0.getCursor();

      while ($$0.canRead() && isAllowedInUnquotedName($$0.peek())) {
         $$0.skip();
      }

      if ($$0.getCursor() == $$1) {
         throw ERROR_INVALID_NODE.createWithContext($$0);
      } else {
         return $$0.getString().substring($$1, $$0.getCursor());
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   private static boolean isAllowedInUnquotedName(char $$0) {
      return $$0 != ' ' && $$0 != '"' && $$0 != '\'' && $$0 != '[' && $$0 != ']' && $$0 != '.' && $$0 != '{' && $$0 != '}';
   }

   static Predicate<Tag> createTagPredicate(CompoundTag $$0) {
      return $$1 -> NbtUtils.compareNbt($$0, $$1, true);
   }

   static class AllElementsNode implements NbtPathArgument.Node {
      public static final NbtPathArgument.AllElementsNode INSTANCE = new NbtPathArgument.AllElementsNode();

      private AllElementsNode() {
      }

      @Override
      public void getTag(Tag $$0, List<Tag> $$1) {
         if ($$0 instanceof CollectionTag $$2) {
            Iterables.addAll($$1, $$2);
         }
      }

      @Override
      public void getOrCreateTag(Tag $$0, Supplier<Tag> $$1, List<Tag> $$2) {
         if ($$0 instanceof CollectionTag $$3) {
            if ($$3.isEmpty()) {
               Tag $$4 = $$1.get();
               if ($$3.addTag(0, $$4)) {
                  $$2.add($$4);
               }
            } else {
               Iterables.addAll($$2, $$3);
            }
         }
      }

      @Override
      public Tag createPreferredParentTag() {
         return new ListTag();
      }

      @Override
      public int setTag(Tag $$0, Supplier<Tag> $$1) {
         if (!($$0 instanceof CollectionTag $$2)) {
            return 0;
         } else {
            int $$3 = $$2.size();
            if ($$3 == 0) {
               $$2.addTag(0, $$1.get());
               return 1;
            } else {
               Tag $$4 = $$1.get();
               int $$5 = $$3 - (int)$$2.stream().filter($$4::equals).count();
               if ($$5 == 0) {
                  return 0;
               } else {
                  $$2.clear();
                  if (!$$2.addTag(0, $$4)) {
                     return 0;
                  } else {
                     for (int $$6 = 1; $$6 < $$3; $$6++) {
                        $$2.addTag($$6, $$1.get());
                     }

                     return $$5;
                  }
               }
            }
         }
      }

      @Override
      public int removeTag(Tag $$0) {
         if ($$0 instanceof CollectionTag $$1) {
            int $$2 = $$1.size();
            if ($$2 > 0) {
               $$1.clear();
               return $$2;
            }
         }

         return 0;
      }
   }

   static class CompoundChildNode implements NbtPathArgument.Node {
      private final String name;

      public CompoundChildNode(String $$0) {
         this.name = $$0;
      }

      @Override
      public void getTag(Tag $$0, List<Tag> $$1) {
         if ($$0 instanceof CompoundTag) {
            Tag $$2 = ((CompoundTag)$$0).get(this.name);
            if ($$2 != null) {
               $$1.add($$2);
            }
         }
      }

      @Override
      public void getOrCreateTag(Tag $$0, Supplier<Tag> $$1, List<Tag> $$2) {
         if ($$0 instanceof CompoundTag $$3) {
            Tag $$4;
            if ($$3.contains(this.name)) {
               $$4 = $$3.get(this.name);
            } else {
               $$4 = $$1.get();
               $$3.put(this.name, $$4);
            }

            $$2.add($$4);
         }
      }

      @Override
      public Tag createPreferredParentTag() {
         return new CompoundTag();
      }

      @Override
      public int setTag(Tag $$0, Supplier<Tag> $$1) {
         if ($$0 instanceof CompoundTag $$2) {
            Tag $$3 = $$1.get();
            Tag $$4 = $$2.put(this.name, $$3);
            if (!$$3.equals($$4)) {
               return 1;
            }
         }

         return 0;
      }

      @Override
      public int removeTag(Tag $$0) {
         if ($$0 instanceof CompoundTag $$1 && $$1.contains(this.name)) {
            $$1.remove(this.name);
            return 1;
         } else {
            return 0;
         }
      }
   }

   static class IndexedElementNode implements NbtPathArgument.Node {
      private final int index;

      public IndexedElementNode(int $$0) {
         this.index = $$0;
      }

      @Override
      public void getTag(Tag $$0, List<Tag> $$1) {
         if ($$0 instanceof CollectionTag $$2) {
            int $$3 = $$2.size();
            int $$4 = this.index < 0 ? $$3 + this.index : this.index;
            if (0 <= $$4 && $$4 < $$3) {
               $$1.add($$2.get($$4));
            }
         }
      }

      @Override
      public void getOrCreateTag(Tag $$0, Supplier<Tag> $$1, List<Tag> $$2) {
         this.getTag($$0, $$2);
      }

      @Override
      public Tag createPreferredParentTag() {
         return new ListTag();
      }

      @Override
      public int setTag(Tag $$0, Supplier<Tag> $$1) {
         if ($$0 instanceof CollectionTag $$2) {
            int $$3 = $$2.size();
            int $$4 = this.index < 0 ? $$3 + this.index : this.index;
            if (0 <= $$4 && $$4 < $$3) {
               Tag $$5 = $$2.get($$4);
               Tag $$6 = $$1.get();
               if (!$$6.equals($$5) && $$2.setTag($$4, $$6)) {
                  return 1;
               }
            }
         }

         return 0;
      }

      @Override
      public int removeTag(Tag $$0) {
         if ($$0 instanceof CollectionTag $$1) {
            int $$2 = $$1.size();
            int $$3 = this.index < 0 ? $$2 + this.index : this.index;
            if (0 <= $$3 && $$3 < $$2) {
               $$1.remove($$3);
               return 1;
            }
         }

         return 0;
      }
   }

   static class MatchElementNode implements NbtPathArgument.Node {
      private final CompoundTag pattern;
      private final Predicate<Tag> predicate;

      public MatchElementNode(CompoundTag $$0) {
         this.pattern = $$0;
         this.predicate = NbtPathArgument.createTagPredicate($$0);
      }

      @Override
      public void getTag(Tag $$0, List<Tag> $$1) {
         if ($$0 instanceof ListTag $$2) {
            $$2.stream().filter(this.predicate).forEach($$1::add);
         }
      }

      @Override
      public void getOrCreateTag(Tag $$0, Supplier<Tag> $$1, List<Tag> $$2) {
         MutableBoolean $$3 = new MutableBoolean();
         if ($$0 instanceof ListTag $$4) {
            $$4.stream().filter(this.predicate).forEach($$2x -> {
               $$2.add($$2x);
               $$3.setTrue();
            });
            if ($$3.isFalse()) {
               CompoundTag $$5 = this.pattern.copy();
               $$4.add($$5);
               $$2.add($$5);
            }
         }
      }

      @Override
      public Tag createPreferredParentTag() {
         return new ListTag();
      }

      @Override
      public int setTag(Tag $$0, Supplier<Tag> $$1) {
         int $$2 = 0;
         if ($$0 instanceof ListTag $$3) {
            int $$4 = $$3.size();
            if ($$4 == 0) {
               $$3.add($$1.get());
               $$2++;
            } else {
               for (int $$5 = 0; $$5 < $$4; $$5++) {
                  Tag $$6 = $$3.get($$5);
                  if (this.predicate.test($$6)) {
                     Tag $$7 = $$1.get();
                     if (!$$7.equals($$6) && $$3.setTag($$5, $$7)) {
                        $$2++;
                     }
                  }
               }
            }
         }

         return $$2;
      }

      @Override
      public int removeTag(Tag $$0) {
         int $$1 = 0;
         if ($$0 instanceof ListTag $$2) {
            for (int $$3 = $$2.size() - 1; $$3 >= 0; $$3--) {
               if (this.predicate.test($$2.get($$3))) {
                  $$2.remove($$3);
                  $$1++;
               }
            }
         }

         return $$1;
      }
   }

   static class MatchObjectNode implements NbtPathArgument.Node {
      private final String name;
      private final CompoundTag pattern;
      private final Predicate<Tag> predicate;

      public MatchObjectNode(String $$0, CompoundTag $$1) {
         this.name = $$0;
         this.pattern = $$1;
         this.predicate = NbtPathArgument.createTagPredicate($$1);
      }

      @Override
      public void getTag(Tag $$0, List<Tag> $$1) {
         if ($$0 instanceof CompoundTag) {
            Tag $$2 = ((CompoundTag)$$0).get(this.name);
            if (this.predicate.test($$2)) {
               $$1.add($$2);
            }
         }
      }

      @Override
      public void getOrCreateTag(Tag $$0, Supplier<Tag> $$1, List<Tag> $$2) {
         if ($$0 instanceof CompoundTag $$3) {
            Tag $$4 = $$3.get(this.name);
            if ($$4 == null) {
               Tag var6 = this.pattern.copy();
               $$3.put(this.name, var6);
               $$2.add(var6);
            } else if (this.predicate.test($$4)) {
               $$2.add($$4);
            }
         }
      }

      @Override
      public Tag createPreferredParentTag() {
         return new CompoundTag();
      }

      @Override
      public int setTag(Tag $$0, Supplier<Tag> $$1) {
         if ($$0 instanceof CompoundTag $$2) {
            Tag $$3 = $$2.get(this.name);
            if (this.predicate.test($$3)) {
               Tag $$4 = $$1.get();
               if (!$$4.equals($$3)) {
                  $$2.put(this.name, $$4);
                  return 1;
               }
            }
         }

         return 0;
      }

      @Override
      public int removeTag(Tag $$0) {
         if ($$0 instanceof CompoundTag $$1) {
            Tag $$2 = $$1.get(this.name);
            if (this.predicate.test($$2)) {
               $$1.remove(this.name);
               return 1;
            }
         }

         return 0;
      }
   }

   static class MatchRootObjectNode implements NbtPathArgument.Node {
      private final Predicate<Tag> predicate;

      public MatchRootObjectNode(CompoundTag $$0) {
         this.predicate = NbtPathArgument.createTagPredicate($$0);
      }

      @Override
      public void getTag(Tag $$0, List<Tag> $$1) {
         if ($$0 instanceof CompoundTag && this.predicate.test($$0)) {
            $$1.add($$0);
         }
      }

      @Override
      public void getOrCreateTag(Tag $$0, Supplier<Tag> $$1, List<Tag> $$2) {
         this.getTag($$0, $$2);
      }

      @Override
      public Tag createPreferredParentTag() {
         return new CompoundTag();
      }

      @Override
      public int setTag(Tag $$0, Supplier<Tag> $$1) {
         return 0;
      }

      @Override
      public int removeTag(Tag $$0) {
         return 0;
      }
   }

   public static class NbtPath {
      private final String original;
      private final Object2IntMap<NbtPathArgument.Node> nodeToOriginalPosition;
      private final NbtPathArgument.Node[] nodes;
      public static final Codec<NbtPathArgument.NbtPath> CODEC = Codec.STRING.comapFlatMap($$0 -> {
         try {
            NbtPathArgument.NbtPath $$1 = new NbtPathArgument().parse(new StringReader($$0));
            return DataResult.success($$1);
         } catch (CommandSyntaxException var2) {
            return DataResult.error(() -> "Failed to parse path " + $$0 + ": " + var2.getMessage());
         }
      }, NbtPathArgument.NbtPath::asString);

      public static NbtPathArgument.NbtPath of(String $$0) throws CommandSyntaxException {
         return new NbtPathArgument().parse(new StringReader($$0));
      }

      public NbtPath(String $$0, NbtPathArgument.Node[] $$1, Object2IntMap<NbtPathArgument.Node> $$2) {
         this.original = $$0;
         this.nodes = $$1;
         this.nodeToOriginalPosition = $$2;
      }

      public List<Tag> get(Tag $$0) throws CommandSyntaxException {
         List<Tag> $$1 = Collections.singletonList($$0);

         for (NbtPathArgument.Node $$2 : this.nodes) {
            $$1 = $$2.get($$1);
            if ($$1.isEmpty()) {
               throw this.createNotFoundException($$2);
            }
         }

         return $$1;
      }

      public int countMatching(Tag $$0) {
         List<Tag> $$1 = Collections.singletonList($$0);

         for (NbtPathArgument.Node $$2 : this.nodes) {
            $$1 = $$2.get($$1);
            if ($$1.isEmpty()) {
               return 0;
            }
         }

         return $$1.size();
      }

      private List<Tag> getOrCreateParents(Tag $$0) throws CommandSyntaxException {
         List<Tag> $$1 = Collections.singletonList($$0);

         for (int $$2 = 0; $$2 < this.nodes.length - 1; $$2++) {
            NbtPathArgument.Node $$3 = this.nodes[$$2];
            int $$4 = $$2 + 1;
            $$1 = $$3.getOrCreate($$1, this.nodes[$$4]::createPreferredParentTag);
            if ($$1.isEmpty()) {
               throw this.createNotFoundException($$3);
            }
         }

         return $$1;
      }

      public List<Tag> getOrCreate(Tag $$0, Supplier<Tag> $$1) throws CommandSyntaxException {
         List<Tag> $$2 = this.getOrCreateParents($$0);
         NbtPathArgument.Node $$3 = this.nodes[this.nodes.length - 1];
         return $$3.getOrCreate($$2, $$1);
      }

      private static int apply(List<Tag> $$0, Function<Tag, Integer> $$1) {
         return $$0.stream().map($$1).reduce(0, ($$0x, $$1x) -> $$0x + $$1x);
      }

      public static boolean isTooDeep(Tag $$0, int $$1) {
         if ($$1 >= 512) {
            return true;
         } else {
            if ($$0 instanceof CompoundTag $$2) {
               for (Tag $$3 : $$2.values()) {
                  if (isTooDeep($$3, $$1 + 1)) {
                     return true;
                  }
               }
            } else if ($$0 instanceof ListTag) {
               for (Tag $$5 : (ListTag)$$0) {
                  if (isTooDeep($$5, $$1 + 1)) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      public int set(Tag $$0, Tag $$1) throws CommandSyntaxException {
         if (isTooDeep($$1, this.estimatePathDepth())) {
            throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
         } else {
            Tag $$2 = $$1.copy();
            List<Tag> $$3 = this.getOrCreateParents($$0);
            if ($$3.isEmpty()) {
               return 0;
            } else {
               NbtPathArgument.Node $$4 = this.nodes[this.nodes.length - 1];
               MutableBoolean $$5 = new MutableBoolean(false);
               return apply($$3, $$3x -> $$4.setTag($$3x, () -> {
                  if ($$5.isFalse()) {
                     $$5.setTrue();
                     return $$2;
                  } else {
                     return $$2.copy();
                  }
               }));
            }
         }
      }

      private int estimatePathDepth() {
         return this.nodes.length;
      }

      public int insert(int $$0, CompoundTag $$1, List<Tag> $$2) throws CommandSyntaxException {
         List<Tag> $$3 = new ArrayList<>($$2.size());

         for (Tag $$4 : $$2) {
            Tag $$5 = $$4.copy();
            $$3.add($$5);
            if (isTooDeep($$5, this.estimatePathDepth())) {
               throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
            }
         }

         Collection<Tag> $$6 = this.getOrCreate($$1, ListTag::new);
         int $$7 = 0;
         boolean $$8 = false;

         for (Tag $$9 : $$6) {
            if (!($$9 instanceof CollectionTag $$10)) {
               throw NbtPathArgument.ERROR_EXPECTED_LIST.create($$9);
            }

            boolean $$12 = false;
            int $$13 = $$0 < 0 ? $$10.size() + $$0 + 1 : $$0;

            for (Tag $$14 : $$3) {
               try {
                  if ($$10.addTag($$13, $$8 ? $$14.copy() : $$14)) {
                     $$13++;
                     $$12 = true;
                  }
               } catch (IndexOutOfBoundsException var16) {
                  throw NbtPathArgument.ERROR_INVALID_INDEX.create($$13);
               }
            }

            $$8 = true;
            $$7 += $$12 ? 1 : 0;
         }

         return $$7;
      }

      public int remove(Tag $$0) {
         List<Tag> $$1 = Collections.singletonList($$0);

         for (int $$2 = 0; $$2 < this.nodes.length - 1; $$2++) {
            $$1 = this.nodes[$$2].get($$1);
         }

         NbtPathArgument.Node $$3 = this.nodes[this.nodes.length - 1];
         return apply($$1, $$3::removeTag);
      }

      private CommandSyntaxException createNotFoundException(NbtPathArgument.Node $$0) {
         int $$1 = this.nodeToOriginalPosition.getInt($$0);
         return NbtPathArgument.ERROR_NOTHING_FOUND.create(this.original.substring(0, $$1));
      }

      @Override
      public String toString() {
         return this.original;
      }

      public String asString() {
         return this.original;
      }
   }

   interface Node {
      void getTag(Tag var1, List<Tag> var2);

      void getOrCreateTag(Tag var1, Supplier<Tag> var2, List<Tag> var3);

      Tag createPreferredParentTag();

      int setTag(Tag var1, Supplier<Tag> var2);

      int removeTag(Tag var1);

      default List<Tag> get(List<Tag> $$0) {
         return this.collect($$0, this::getTag);
      }

      default List<Tag> getOrCreate(List<Tag> $$0, Supplier<Tag> $$1) {
         return this.collect($$0, ($$1x, $$2) -> this.getOrCreateTag($$1x, $$1, $$2));
      }

      default List<Tag> collect(List<Tag> $$0, BiConsumer<Tag, List<Tag>> $$1) {
         List<Tag> $$2 = Lists.newArrayList();

         for (Tag $$3 : $$0) {
            $$1.accept($$3, $$2);
         }

         return $$2;
      }
   }
}
