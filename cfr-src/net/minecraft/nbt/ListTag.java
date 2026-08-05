/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtFormatException;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.nbt.TagVisitor;
import org.jspecify.annotations.Nullable;

public final class ListTag
extends AbstractList<Tag>
implements CollectionTag {
    private static final String WRAPPER_MARKER = "";
    private static final int SELF_SIZE_IN_BYTES = 36;
    public static final TagType<ListTag> TYPE = new TagType.VariableSize<ListTag>(){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public ListTag load(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$1.pushDepth();
            try {
                ListTag listTag = 1.loadList($$0, $$1);
                return listTag;
            }
            finally {
                $$1.popDepth();
            }
        }

        private static ListTag loadList(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$1.accountBytes(36L);
            byte $$2 = $$0.readByte();
            int $$3 = 1.readListCount($$0);
            if ($$2 == 0 && $$3 > 0) {
                throw new NbtFormatException("Missing type on ListTag");
            }
            $$1.accountBytes(4L, $$3);
            TagType<?> $$4 = TagTypes.getType($$2);
            ListTag $$5 = new ListTag(new ArrayList<Tag>($$3));
            for (int $$6 = 0; $$6 < $$3; ++$$6) {
                $$5.addAndUnwrap((Tag)$$4.load($$0, $$1));
            }
            return $$5;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public StreamTagVisitor.ValueResult parse(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
            $$2.pushDepth();
            try {
                StreamTagVisitor.ValueResult valueResult = 1.parseList($$0, $$1, $$2);
                return valueResult;
            }
            finally {
                $$2.popDepth();
            }
        }

        /*
         * Exception decompiling
         */
        private static StreamTagVisitor.ValueResult parseList(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [4[SWITCH], 8[CASE]], but top level block is 9[SWITCH]
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
             *     at org.benf.cfr.reader.Main.main(Main.java:54)
             */
            throw new IllegalStateException("Decompilation failed");
        }

        private static int readListCount(DataInput $$0) throws IOException {
            int $$1 = $$0.readInt();
            if ($$1 < 0) {
                throw new NbtFormatException("ListTag length cannot be negative: " + $$1);
            }
            return $$1;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void skip(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$1.pushDepth();
            try {
                TagType<?> $$2 = TagTypes.getType($$0.readByte());
                int $$3 = $$0.readInt();
                $$2.skip($$0, $$3, $$1);
            }
            finally {
                $$1.popDepth();
            }
        }

        @Override
        public String getName() {
            return "LIST";
        }

        @Override
        public String getPrettyName() {
            return "TAG_List";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private final List<Tag> list;

    public ListTag() {
        this(new ArrayList<Tag>());
    }

    ListTag(List<Tag> $$0) {
        this.list = $$0;
    }

    private static Tag tryUnwrap(CompoundTag $$0) {
        Tag $$1;
        if ($$0.size() == 1 && ($$1 = $$0.get(WRAPPER_MARKER)) != null) {
            return $$1;
        }
        return $$0;
    }

    private static boolean isWrapper(CompoundTag $$0) {
        return $$0.size() == 1 && $$0.contains(WRAPPER_MARKER);
    }

    private static Tag wrapIfNeeded(byte $$0, Tag $$1) {
        CompoundTag $$2;
        if ($$0 != 10) {
            return $$1;
        }
        if ($$1 instanceof CompoundTag && !ListTag.isWrapper($$2 = (CompoundTag)$$1)) {
            return $$2;
        }
        return ListTag.wrapElement($$1);
    }

    private static CompoundTag wrapElement(Tag $$0) {
        return new CompoundTag(Map.of(WRAPPER_MARKER, $$0));
    }

    @Override
    public void write(DataOutput $$0) throws IOException {
        byte $$1 = this.identifyRawElementType();
        $$0.writeByte($$1);
        $$0.writeInt(this.list.size());
        for (Tag $$2 : this.list) {
            ListTag.wrapIfNeeded($$1, $$2).write($$0);
        }
    }

    @VisibleForTesting
    byte identifyRawElementType() {
        byte $$0 = 0;
        for (Tag $$1 : this.list) {
            byte $$2 = $$1.getId();
            if ($$0 == 0) {
                $$0 = $$2;
                continue;
            }
            if ($$0 == $$2) continue;
            return 10;
        }
        return $$0;
    }

    public void addAndUnwrap(Tag $$0) {
        if ($$0 instanceof CompoundTag) {
            CompoundTag $$1 = (CompoundTag)$$0;
            this.add(ListTag.tryUnwrap($$1));
        } else {
            this.add($$0);
        }
    }

    @Override
    public int sizeInBytes() {
        int $$0 = 36;
        $$0 += 4 * this.list.size();
        for (Tag $$1 : this.list) {
            $$0 += $$1.sizeInBytes();
        }
        return $$0;
    }

    @Override
    public byte getId() {
        return 9;
    }

    public TagType<ListTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor $$0 = new StringTagVisitor();
        $$0.visitList(this);
        return $$0.build();
    }

    @Override
    public Tag remove(int $$0) {
        return this.list.remove($$0);
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public Optional<CompoundTag> getCompound(int $$0) {
        Tag tag = this.getNullable($$0);
        if (tag instanceof CompoundTag) {
            CompoundTag $$1 = (CompoundTag)tag;
            return Optional.of($$1);
        }
        return Optional.empty();
    }

    public CompoundTag getCompoundOrEmpty(int $$0) {
        return this.getCompound($$0).orElseGet(CompoundTag::new);
    }

    public Optional<ListTag> getList(int $$0) {
        Tag tag = this.getNullable($$0);
        if (tag instanceof ListTag) {
            ListTag $$1 = (ListTag)tag;
            return Optional.of($$1);
        }
        return Optional.empty();
    }

    public ListTag getListOrEmpty(int $$0) {
        return this.getList($$0).orElseGet(ListTag::new);
    }

    public Optional<Short> getShort(int $$0) {
        return this.getOptional($$0).flatMap(Tag::asShort);
    }

    public short getShortOr(int $$0, short $$1) {
        Tag tag = this.getNullable($$0);
        if (tag instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)tag;
            return $$2.shortValue();
        }
        return $$1;
    }

    public Optional<Integer> getInt(int $$0) {
        return this.getOptional($$0).flatMap(Tag::asInt);
    }

    public int getIntOr(int $$0, int $$1) {
        Tag tag = this.getNullable($$0);
        if (tag instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)tag;
            return $$2.intValue();
        }
        return $$1;
    }

    public Optional<int[]> getIntArray(int $$0) {
        Tag tag = this.getNullable($$0);
        if (tag instanceof IntArrayTag) {
            IntArrayTag $$1 = (IntArrayTag)tag;
            return Optional.of($$1.getAsIntArray());
        }
        return Optional.empty();
    }

    public Optional<long[]> getLongArray(int $$0) {
        Tag tag = this.getNullable($$0);
        if (tag instanceof LongArrayTag) {
            LongArrayTag $$1 = (LongArrayTag)tag;
            return Optional.of($$1.getAsLongArray());
        }
        return Optional.empty();
    }

    public Optional<Double> getDouble(int $$0) {
        return this.getOptional($$0).flatMap(Tag::asDouble);
    }

    public double getDoubleOr(int $$0, double $$1) {
        Tag tag = this.getNullable($$0);
        if (tag instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)tag;
            return $$2.doubleValue();
        }
        return $$1;
    }

    public Optional<Float> getFloat(int $$0) {
        return this.getOptional($$0).flatMap(Tag::asFloat);
    }

    public float getFloatOr(int $$0, float $$1) {
        Tag tag = this.getNullable($$0);
        if (tag instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)tag;
            return $$2.floatValue();
        }
        return $$1;
    }

    public Optional<String> getString(int $$0) {
        return this.getOptional($$0).flatMap(Tag::asString);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String getStringOr(int $$0, String $$1) {
        Tag $$2 = this.getNullable($$0);
        if (!($$2 instanceof StringTag)) return $$1;
        StringTag stringTag = (StringTag)$$2;
        try {
            String string = stringTag.value();
            return string;
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    private @Nullable Tag getNullable(int $$0) {
        return $$0 >= 0 && $$0 < this.list.size() ? this.list.get($$0) : null;
    }

    private Optional<Tag> getOptional(int $$0) {
        return Optional.ofNullable(this.getNullable($$0));
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public Tag get(int $$0) {
        return this.list.get($$0);
    }

    @Override
    public Tag set(int $$0, Tag $$1) {
        return this.list.set($$0, $$1);
    }

    @Override
    public void add(int $$0, Tag $$1) {
        this.list.add($$0, $$1);
    }

    @Override
    public boolean setTag(int $$0, Tag $$1) {
        this.list.set($$0, $$1);
        return true;
    }

    @Override
    public boolean addTag(int $$0, Tag $$1) {
        this.list.add($$0, $$1);
        return true;
    }

    @Override
    public ListTag copy() {
        ArrayList<Tag> $$0 = new ArrayList<Tag>(this.list.size());
        for (Tag $$1 : this.list) {
            $$0.add($$1.copy());
        }
        return new ListTag($$0);
    }

    @Override
    public Optional<ListTag> asList() {
        return Optional.of(this);
    }

    @Override
    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        return $$0 instanceof ListTag && Objects.equals(this.list, ((ListTag)$$0).list);
    }

    @Override
    public int hashCode() {
        return this.list.hashCode();
    }

    @Override
    public Stream<Tag> stream() {
        return super.stream();
    }

    public Stream<CompoundTag> compoundStream() {
        return this.stream().mapMulti(($$0, $$1) -> {
            if ($$0 instanceof CompoundTag) {
                CompoundTag $$2 = (CompoundTag)$$0;
                $$1.accept($$2);
            }
        });
    }

    @Override
    public void accept(TagVisitor $$0) {
        $$0.visitList(this);
    }

    @Override
    public void clear() {
        this.list.clear();
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor $$0) {
        byte $$1 = this.identifyRawElementType();
        switch ($$0.visitList(TagTypes.getType($$1), this.list.size())) {
            case HALT: {
                return StreamTagVisitor.ValueResult.HALT;
            }
            case BREAK: {
                return $$0.visitContainerEnd();
            }
        }
        block13: for (int $$2 = 0; $$2 < this.list.size(); ++$$2) {
            Tag $$3 = ListTag.wrapIfNeeded($$1, this.list.get($$2));
            switch ($$0.visitElement($$3.getType(), $$2)) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case SKIP: {
                    continue block13;
                }
                case BREAK: {
                    return $$0.visitContainerEnd();
                }
                default: {
                    switch ($$3.accept($$0)) {
                        case HALT: {
                            return StreamTagVisitor.ValueResult.HALT;
                        }
                        case BREAK: {
                            return $$0.visitContainerEnd();
                        }
                    }
                }
            }
        }
        return $$0.visitContainerEnd();
    }

    @Override
    public /* synthetic */ Object remove(int n) {
        return this.remove(n);
    }

    @Override
    public /* synthetic */ void add(int n, Object object) {
        this.add(n, (Tag)object);
    }

    @Override
    public /* synthetic */ Object set(int n, Object object) {
        return this.set(n, (Tag)object);
    }

    @Override
    public /* synthetic */ Object get(int n) {
        return this.get(n);
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

