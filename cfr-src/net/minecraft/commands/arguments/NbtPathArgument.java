/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.commands.arguments;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
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
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NbtPathArgument
implements ArgumentType<NbtPath> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar", "foo[0]", "[0]", "[]", "{foo=bar}");
    public static final SimpleCommandExceptionType ERROR_INVALID_NODE = new SimpleCommandExceptionType((Message)Component.translatable("arguments.nbtpath.node.invalid"));
    public static final SimpleCommandExceptionType ERROR_DATA_TOO_DEEP = new SimpleCommandExceptionType((Message)Component.translatable("arguments.nbtpath.too_deep"));
    public static final DynamicCommandExceptionType ERROR_NOTHING_FOUND = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("arguments.nbtpath.nothing_found", $$0));
    static final DynamicCommandExceptionType ERROR_EXPECTED_LIST = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.data.modify.expected_list", $$0));
    static final DynamicCommandExceptionType ERROR_INVALID_INDEX = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("commands.data.modify.invalid_index", $$0));
    private static final char INDEX_MATCH_START = '[';
    private static final char INDEX_MATCH_END = ']';
    private static final char KEY_MATCH_START = '{';
    private static final char KEY_MATCH_END = '}';
    private static final char QUOTED_KEY_START = '\"';
    private static final char SINGLE_QUOTED_KEY_START = '\'';

    public static NbtPathArgument nbtPath() {
        return new NbtPathArgument();
    }

    public static NbtPath getPath(CommandContext<CommandSourceStack> $$0, String $$1) {
        return (NbtPath)$$0.getArgument($$1, NbtPath.class);
    }

    public NbtPath parse(StringReader $$0) throws CommandSyntaxException {
        ArrayList $$1 = Lists.newArrayList();
        int $$2 = $$0.getCursor();
        Object2IntOpenHashMap $$3 = new Object2IntOpenHashMap();
        boolean $$4 = true;
        while ($$0.canRead() && $$0.peek() != ' ') {
            char $$6;
            Node $$5 = NbtPathArgument.parseNode($$0, $$4);
            $$1.add($$5);
            $$3.put((Object)$$5, $$0.getCursor() - $$2);
            $$4 = false;
            if (!$$0.canRead() || ($$6 = $$0.peek()) == ' ' || $$6 == '[' || $$6 == '{') continue;
            $$0.expect('.');
        }
        return new NbtPath($$0.getString().substring($$2, $$0.getCursor()), $$1.toArray(new Node[0]), (Object2IntMap<Node>)$$3);
    }

    private static Node parseNode(StringReader $$0, boolean $$1) throws CommandSyntaxException {
        return switch ($$0.peek()) {
            case '{' -> {
                if (!$$1) {
                    throw ERROR_INVALID_NODE.createWithContext((ImmutableStringReader)$$0);
                }
                CompoundTag $$2 = TagParser.parseCompoundAsArgument($$0);
                yield new MatchRootObjectNode($$2);
            }
            case '[' -> {
                $$0.skip();
                char $$3 = $$0.peek();
                if ($$3 == '{') {
                    CompoundTag $$4 = TagParser.parseCompoundAsArgument($$0);
                    $$0.expect(']');
                    yield new MatchElementNode($$4);
                }
                if ($$3 == ']') {
                    $$0.skip();
                    yield AllElementsNode.INSTANCE;
                }
                int $$5 = $$0.readInt();
                $$0.expect(']');
                yield new IndexedElementNode($$5);
            }
            case '\"', '\'' -> NbtPathArgument.readObjectNode($$0, $$0.readString());
            default -> NbtPathArgument.readObjectNode($$0, NbtPathArgument.readUnquotedName($$0));
        };
    }

    private static Node readObjectNode(StringReader $$0, String $$1) throws CommandSyntaxException {
        if ($$1.isEmpty()) {
            throw ERROR_INVALID_NODE.createWithContext((ImmutableStringReader)$$0);
        }
        if ($$0.canRead() && $$0.peek() == '{') {
            CompoundTag $$2 = TagParser.parseCompoundAsArgument($$0);
            return new MatchObjectNode($$1, $$2);
        }
        return new CompoundChildNode($$1);
    }

    private static String readUnquotedName(StringReader $$0) throws CommandSyntaxException {
        int $$1 = $$0.getCursor();
        while ($$0.canRead() && NbtPathArgument.isAllowedInUnquotedName($$0.peek())) {
            $$0.skip();
        }
        if ($$0.getCursor() == $$1) {
            throw ERROR_INVALID_NODE.createWithContext((ImmutableStringReader)$$0);
        }
        return $$0.getString().substring($$1, $$0.getCursor());
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static boolean isAllowedInUnquotedName(char $$0) {
        return $$0 != ' ' && $$0 != '\"' && $$0 != '\'' && $$0 != '[' && $$0 != ']' && $$0 != '.' && $$0 != '{' && $$0 != '}';
    }

    static Predicate<Tag> createTagPredicate(CompoundTag $$0) {
        return $$1 -> NbtUtils.compareNbt($$0, $$1, true);
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static class NbtPath {
        private final String original;
        private final Object2IntMap<Node> nodeToOriginalPosition;
        private final Node[] nodes;
        public static final Codec<NbtPath> CODEC = Codec.STRING.comapFlatMap($$0 -> {
            try {
                NbtPath $$1 = new NbtPathArgument().parse(new StringReader($$0));
                return DataResult.success((Object)$$1);
            }
            catch (CommandSyntaxException $$2) {
                return DataResult.error(() -> "Failed to parse path " + $$0 + ": " + $$2.getMessage());
            }
        }, NbtPath::asString);

        public static NbtPath of(String $$0) throws CommandSyntaxException {
            return new NbtPathArgument().parse(new StringReader($$0));
        }

        public NbtPath(String $$0, Node[] $$1, Object2IntMap<Node> $$2) {
            this.original = $$0;
            this.nodes = $$1;
            this.nodeToOriginalPosition = $$2;
        }

        public List<Tag> get(Tag $$0) throws CommandSyntaxException {
            List<Tag> $$1 = Collections.singletonList($$0);
            for (Node $$2 : this.nodes) {
                if (!($$1 = $$2.get($$1)).isEmpty()) continue;
                throw this.createNotFoundException($$2);
            }
            return $$1;
        }

        public int countMatching(Tag $$0) {
            List<Tag> $$1 = Collections.singletonList($$0);
            for (Node $$2 : this.nodes) {
                if (!($$1 = $$2.get($$1)).isEmpty()) continue;
                return 0;
            }
            return $$1.size();
        }

        private List<Tag> getOrCreateParents(Tag $$0) throws CommandSyntaxException {
            List<Tag> $$1 = Collections.singletonList($$0);
            for (int $$2 = 0; $$2 < this.nodes.length - 1; ++$$2) {
                Node $$3 = this.nodes[$$2];
                int $$4 = $$2 + 1;
                if (!($$1 = $$3.getOrCreate($$1, this.nodes[$$4]::createPreferredParentTag)).isEmpty()) continue;
                throw this.createNotFoundException($$3);
            }
            return $$1;
        }

        public List<Tag> getOrCreate(Tag $$0, Supplier<Tag> $$1) throws CommandSyntaxException {
            List<Tag> $$2 = this.getOrCreateParents($$0);
            Node $$3 = this.nodes[this.nodes.length - 1];
            return $$3.getOrCreate($$2, $$1);
        }

        private static int apply(List<Tag> $$02, Function<Tag, Integer> $$12) {
            return $$02.stream().map($$12).reduce(0, ($$0, $$1) -> $$0 + $$1);
        }

        public static boolean isTooDeep(Tag $$0, int $$1) {
            block4: {
                block3: {
                    if ($$1 >= 512) {
                        return true;
                    }
                    if (!($$0 instanceof CompoundTag)) break block3;
                    CompoundTag $$2 = (CompoundTag)$$0;
                    for (Tag $$3 : $$2.values()) {
                        if (!NbtPath.isTooDeep($$3, $$1 + 1)) continue;
                        return true;
                    }
                    break block4;
                }
                if (!($$0 instanceof ListTag)) break block4;
                ListTag $$4 = (ListTag)$$0;
                for (Tag $$5 : $$4) {
                    if (!NbtPath.isTooDeep($$5, $$1 + 1)) continue;
                    return true;
                }
            }
            return false;
        }

        public int set(Tag $$0, Tag $$1) throws CommandSyntaxException {
            if (NbtPath.isTooDeep($$1, this.estimatePathDepth())) {
                throw ERROR_DATA_TOO_DEEP.create();
            }
            Tag $$2 = $$1.copy();
            List<Tag> $$32 = this.getOrCreateParents($$0);
            if ($$32.isEmpty()) {
                return 0;
            }
            Node $$4 = this.nodes[this.nodes.length - 1];
            MutableBoolean $$5 = new MutableBoolean(false);
            return NbtPath.apply($$32, $$3 -> $$4.setTag((Tag)$$3, () -> {
                if ($$5.isFalse()) {
                    $$5.setTrue();
                    return $$2;
                }
                return $$2.copy();
            }));
        }

        private int estimatePathDepth() {
            return this.nodes.length;
        }

        /*
         * WARNING - void declaration
         */
        public int insert(int $$0, CompoundTag $$1, List<Tag> $$2) throws CommandSyntaxException {
            ArrayList<Tag> $$3 = new ArrayList<Tag>($$2.size());
            for (Tag $$4 : $$2) {
                Tag $$5 = $$4.copy();
                $$3.add($$5);
                if (!NbtPath.isTooDeep($$5, this.estimatePathDepth())) continue;
                throw ERROR_DATA_TOO_DEEP.create();
            }
            List<Tag> $$6 = this.getOrCreate($$1, ListTag::new);
            int $$7 = 0;
            boolean $$8 = false;
            for (Tag $$9 : $$6) {
                void $$11;
                if (!($$9 instanceof CollectionTag)) {
                    throw ERROR_EXPECTED_LIST.create((Object)$$9);
                }
                CollectionTag $$10 = (CollectionTag)$$9;
                boolean $$12 = false;
                int $$13 = $$0 < 0 ? $$11.size() + $$0 + 1 : $$0;
                for (Tag $$14 : $$3) {
                    try {
                        if (!$$11.addTag($$13, $$8 ? $$14.copy() : $$14)) continue;
                        ++$$13;
                        $$12 = true;
                    }
                    catch (IndexOutOfBoundsException $$15) {
                        throw ERROR_INVALID_INDEX.create((Object)$$13);
                    }
                }
                $$8 = true;
                $$7 += $$12 ? 1 : 0;
            }
            return $$7;
        }

        public int remove(Tag $$0) {
            List<Tag> $$1 = Collections.singletonList($$0);
            for (int $$2 = 0; $$2 < this.nodes.length - 1; ++$$2) {
                $$1 = this.nodes[$$2].get($$1);
            }
            Node $$3 = this.nodes[this.nodes.length - 1];
            return NbtPath.apply($$1, $$3::removeTag);
        }

        private CommandSyntaxException createNotFoundException(Node $$0) {
            int $$1 = this.nodeToOriginalPosition.getInt((Object)$$0);
            return ERROR_NOTHING_FOUND.create((Object)this.original.substring(0, $$1));
        }

        public String toString() {
            return this.original;
        }

        public String asString() {
            return this.original;
        }
    }

    static interface Node {
        public void getTag(Tag var1, List<Tag> var2);

        public void getOrCreateTag(Tag var1, Supplier<Tag> var2, List<Tag> var3);

        public Tag createPreferredParentTag();

        public int setTag(Tag var1, Supplier<Tag> var2);

        public int removeTag(Tag var1);

        default public List<Tag> get(List<Tag> $$0) {
            return this.collect($$0, this::getTag);
        }

        default public List<Tag> getOrCreate(List<Tag> $$0, Supplier<Tag> $$12) {
            return this.collect($$0, ($$1, $$2) -> this.getOrCreateTag((Tag)$$1, $$12, (List<Tag>)$$2));
        }

        default public List<Tag> collect(List<Tag> $$0, BiConsumer<Tag, List<Tag>> $$1) {
            ArrayList $$2 = Lists.newArrayList();
            for (Tag $$3 : $$0) {
                $$1.accept($$3, $$2);
            }
            return $$2;
        }
    }

    static class MatchRootObjectNode
    implements Node {
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

    static class MatchElementNode
    implements Node {
        private final CompoundTag pattern;
        private final Predicate<Tag> predicate;

        public MatchElementNode(CompoundTag $$0) {
            this.pattern = $$0;
            this.predicate = NbtPathArgument.createTagPredicate($$0);
        }

        @Override
        public void getTag(Tag $$0, List<Tag> $$1) {
            if ($$0 instanceof ListTag) {
                ListTag $$2 = (ListTag)$$0;
                $$2.stream().filter(this.predicate).forEach($$1::add);
            }
        }

        @Override
        public void getOrCreateTag(Tag $$0, Supplier<Tag> $$1, List<Tag> $$22) {
            MutableBoolean $$3 = new MutableBoolean();
            if ($$0 instanceof ListTag) {
                ListTag $$4 = (ListTag)$$0;
                $$4.stream().filter(this.predicate).forEach($$2 -> {
                    $$22.add((Tag)$$2);
                    $$3.setTrue();
                });
                if ($$3.isFalse()) {
                    CompoundTag $$5 = this.pattern.copy();
                    $$4.add($$5);
                    $$22.add($$5);
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
            if ($$0 instanceof ListTag) {
                ListTag $$3 = (ListTag)$$0;
                int $$4 = $$3.size();
                if ($$4 == 0) {
                    $$3.add($$1.get());
                    ++$$2;
                } else {
                    for (int $$5 = 0; $$5 < $$4; ++$$5) {
                        Tag $$7;
                        Tag $$6 = $$3.get($$5);
                        if (!this.predicate.test($$6) || ($$7 = $$1.get()).equals($$6) || !$$3.setTag($$5, $$7)) continue;
                        ++$$2;
                    }
                }
            }
            return $$2;
        }

        @Override
        public int removeTag(Tag $$0) {
            int $$1 = 0;
            if ($$0 instanceof ListTag) {
                ListTag $$2 = (ListTag)$$0;
                for (int $$3 = $$2.size() - 1; $$3 >= 0; --$$3) {
                    if (!this.predicate.test($$2.get($$3))) continue;
                    $$2.remove($$3);
                    ++$$1;
                }
            }
            return $$1;
        }
    }

    static class AllElementsNode
    implements Node {
        public static final AllElementsNode INSTANCE = new AllElementsNode();

        private AllElementsNode() {
        }

        @Override
        public void getTag(Tag $$0, List<Tag> $$1) {
            if ($$0 instanceof CollectionTag) {
                CollectionTag $$2 = (CollectionTag)$$0;
                Iterables.addAll($$1, (Iterable)$$2);
            }
        }

        @Override
        public void getOrCreateTag(Tag $$0, Supplier<Tag> $$1, List<Tag> $$2) {
            if ($$0 instanceof CollectionTag) {
                CollectionTag $$3 = (CollectionTag)$$0;
                if ($$3.isEmpty()) {
                    Tag $$4 = $$1.get();
                    if ($$3.addTag(0, $$4)) {
                        $$2.add($$4);
                    }
                } else {
                    Iterables.addAll($$2, (Iterable)$$3);
                }
            }
        }

        @Override
        public Tag createPreferredParentTag() {
            return new ListTag();
        }

        @Override
        public int setTag(Tag $$0, Supplier<Tag> $$1) {
            if ($$0 instanceof CollectionTag) {
                CollectionTag $$2 = (CollectionTag)$$0;
                int $$3 = $$2.size();
                if ($$3 == 0) {
                    $$2.addTag(0, $$1.get());
                    return 1;
                }
                Tag $$4 = $$1.get();
                int $$5 = $$3 - (int)$$2.stream().filter((Predicate<Tag>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Z, equals(java.lang.Object ), (Lnet/minecraft/nbt/Tag;)Z)((Tag)$$4)).count();
                if ($$5 == 0) {
                    return 0;
                }
                $$2.clear();
                if (!$$2.addTag(0, $$4)) {
                    return 0;
                }
                for (int $$6 = 1; $$6 < $$3; ++$$6) {
                    $$2.addTag($$6, $$1.get());
                }
                return $$5;
            }
            return 0;
        }

        @Override
        public int removeTag(Tag $$0) {
            CollectionTag $$1;
            int $$2;
            if ($$0 instanceof CollectionTag && ($$2 = ($$1 = (CollectionTag)$$0).size()) > 0) {
                $$1.clear();
                return $$2;
            }
            return 0;
        }
    }

    static class IndexedElementNode
    implements Node {
        private final int index;

        public IndexedElementNode(int $$0) {
            this.index = $$0;
        }

        @Override
        public void getTag(Tag $$0, List<Tag> $$1) {
            if ($$0 instanceof CollectionTag) {
                int $$4;
                CollectionTag $$2 = (CollectionTag)$$0;
                int $$3 = $$2.size();
                int n = $$4 = this.index < 0 ? $$3 + this.index : this.index;
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
            if ($$0 instanceof CollectionTag) {
                int $$4;
                CollectionTag $$2 = (CollectionTag)$$0;
                int $$3 = $$2.size();
                int n = $$4 = this.index < 0 ? $$3 + this.index : this.index;
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
            if ($$0 instanceof CollectionTag) {
                int $$3;
                CollectionTag $$1 = (CollectionTag)$$0;
                int $$2 = $$1.size();
                int n = $$3 = this.index < 0 ? $$2 + this.index : this.index;
                if (0 <= $$3 && $$3 < $$2) {
                    $$1.remove($$3);
                    return 1;
                }
            }
            return 0;
        }
    }

    static class MatchObjectNode
    implements Node {
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
            Tag $$2;
            if ($$0 instanceof CompoundTag && this.predicate.test($$2 = ((CompoundTag)$$0).get(this.name))) {
                $$1.add($$2);
            }
        }

        @Override
        public void getOrCreateTag(Tag $$0, Supplier<Tag> $$1, List<Tag> $$2) {
            if ($$0 instanceof CompoundTag) {
                CompoundTag $$3 = (CompoundTag)$$0;
                Tag $$4 = $$3.get(this.name);
                if ($$4 == null) {
                    $$4 = this.pattern.copy();
                    $$3.put(this.name, $$4);
                    $$2.add($$4);
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
            Tag $$4;
            CompoundTag $$2;
            Tag $$3;
            if ($$0 instanceof CompoundTag && this.predicate.test($$3 = ($$2 = (CompoundTag)$$0).get(this.name)) && !($$4 = $$1.get()).equals($$3)) {
                $$2.put(this.name, $$4);
                return 1;
            }
            return 0;
        }

        @Override
        public int removeTag(Tag $$0) {
            CompoundTag $$1;
            Tag $$2;
            if ($$0 instanceof CompoundTag && this.predicate.test($$2 = ($$1 = (CompoundTag)$$0).get(this.name))) {
                $$1.remove(this.name);
                return 1;
            }
            return 0;
        }
    }

    static class CompoundChildNode
    implements Node {
        private final String name;

        public CompoundChildNode(String $$0) {
            this.name = $$0;
        }

        @Override
        public void getTag(Tag $$0, List<Tag> $$1) {
            Tag $$2;
            if ($$0 instanceof CompoundTag && ($$2 = ((CompoundTag)$$0).get(this.name)) != null) {
                $$1.add($$2);
            }
        }

        @Override
        public void getOrCreateTag(Tag $$0, Supplier<Tag> $$1, List<Tag> $$2) {
            if ($$0 instanceof CompoundTag) {
                Tag $$5;
                CompoundTag $$3 = (CompoundTag)$$0;
                if ($$3.contains(this.name)) {
                    Tag $$4 = $$3.get(this.name);
                } else {
                    $$5 = $$1.get();
                    $$3.put(this.name, $$5);
                }
                $$2.add($$5);
            }
        }

        @Override
        public Tag createPreferredParentTag() {
            return new CompoundTag();
        }

        @Override
        public int setTag(Tag $$0, Supplier<Tag> $$1) {
            if ($$0 instanceof CompoundTag) {
                Tag $$4;
                CompoundTag $$2 = (CompoundTag)$$0;
                Tag $$3 = $$1.get();
                if (!$$3.equals($$4 = $$2.put(this.name, $$3))) {
                    return 1;
                }
            }
            return 0;
        }

        @Override
        public int removeTag(Tag $$0) {
            CompoundTag $$1;
            if ($$0 instanceof CompoundTag && ($$1 = (CompoundTag)$$0).contains(this.name)) {
                $$1.remove(this.name);
                return 1;
            }
            return 0;
        }
    }
}

