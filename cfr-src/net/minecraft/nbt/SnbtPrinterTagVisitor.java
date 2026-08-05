/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 */
package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagVisitor;
import net.minecraft.util.Util;

public class SnbtPrinterTagVisitor
implements TagVisitor {
    private static final Map<String, List<String>> KEY_ORDER = Util.make(Maps.newHashMap(), $$0 -> {
        $$0.put("{}", Lists.newArrayList((Object[])new String[]{"DataVersion", "author", "size", "data", "entities", "palette", "palettes"}));
        $$0.put("{}.data.[].{}", Lists.newArrayList((Object[])new String[]{"pos", "state", "nbt"}));
        $$0.put("{}.entities.[].{}", Lists.newArrayList((Object[])new String[]{"blockPos", "pos"}));
    });
    private static final Set<String> NO_INDENTATION = Sets.newHashSet((Object[])new String[]{"{}.size.[]", "{}.data.[].{}", "{}.palette.[].{}", "{}.entities.[].{}"});
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String NAME_VALUE_SEPARATOR = String.valueOf(':');
    private static final String ELEMENT_SEPARATOR = String.valueOf(',');
    private static final String LIST_OPEN = "[";
    private static final String LIST_CLOSE = "]";
    private static final String LIST_TYPE_SEPARATOR = ";";
    private static final String ELEMENT_SPACING = " ";
    private static final String STRUCT_OPEN = "{";
    private static final String STRUCT_CLOSE = "}";
    private static final String NEWLINE = "\n";
    private final String indentation;
    private final int depth;
    private final List<String> path;
    private String result = "";

    public SnbtPrinterTagVisitor() {
        this("    ", 0, Lists.newArrayList());
    }

    public SnbtPrinterTagVisitor(String $$0, int $$1, List<String> $$2) {
        this.indentation = $$0;
        this.depth = $$1;
        this.path = $$2;
    }

    public String visit(Tag $$0) {
        $$0.accept(this);
        return this.result;
    }

    @Override
    public void visitString(StringTag $$0) {
        this.result = StringTag.quoteAndEscape($$0.value());
    }

    @Override
    public void visitByte(ByteTag $$0) {
        this.result = $$0.value() + "b";
    }

    @Override
    public void visitShort(ShortTag $$0) {
        this.result = $$0.value() + "s";
    }

    @Override
    public void visitInt(IntTag $$0) {
        this.result = String.valueOf($$0.value());
    }

    @Override
    public void visitLong(LongTag $$0) {
        this.result = $$0.value() + "L";
    }

    @Override
    public void visitFloat(FloatTag $$0) {
        this.result = $$0.value() + "f";
    }

    @Override
    public void visitDouble(DoubleTag $$0) {
        this.result = $$0.value() + "d";
    }

    @Override
    public void visitByteArray(ByteArrayTag $$0) {
        StringBuilder $$1 = new StringBuilder(LIST_OPEN).append("B").append(LIST_TYPE_SEPARATOR);
        byte[] $$2 = $$0.getAsByteArray();
        for (int $$3 = 0; $$3 < $$2.length; ++$$3) {
            $$1.append(ELEMENT_SPACING).append($$2[$$3]).append("B");
            if ($$3 == $$2.length - 1) continue;
            $$1.append(ELEMENT_SEPARATOR);
        }
        $$1.append(LIST_CLOSE);
        this.result = $$1.toString();
    }

    @Override
    public void visitIntArray(IntArrayTag $$0) {
        StringBuilder $$1 = new StringBuilder(LIST_OPEN).append("I").append(LIST_TYPE_SEPARATOR);
        int[] $$2 = $$0.getAsIntArray();
        for (int $$3 = 0; $$3 < $$2.length; ++$$3) {
            $$1.append(ELEMENT_SPACING).append($$2[$$3]);
            if ($$3 == $$2.length - 1) continue;
            $$1.append(ELEMENT_SEPARATOR);
        }
        $$1.append(LIST_CLOSE);
        this.result = $$1.toString();
    }

    @Override
    public void visitLongArray(LongArrayTag $$0) {
        String $$1 = "L";
        StringBuilder $$2 = new StringBuilder(LIST_OPEN).append("L").append(LIST_TYPE_SEPARATOR);
        long[] $$3 = $$0.getAsLongArray();
        for (int $$4 = 0; $$4 < $$3.length; ++$$4) {
            $$2.append(ELEMENT_SPACING).append($$3[$$4]).append("L");
            if ($$4 == $$3.length - 1) continue;
            $$2.append(ELEMENT_SEPARATOR);
        }
        $$2.append(LIST_CLOSE);
        this.result = $$2.toString();
    }

    @Override
    public void visitList(ListTag $$0) {
        String $$2;
        if ($$0.isEmpty()) {
            this.result = "[]";
            return;
        }
        StringBuilder $$1 = new StringBuilder(LIST_OPEN);
        this.pushPath("[]");
        String string = $$2 = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
        if (!$$2.isEmpty()) {
            $$1.append(NEWLINE);
        }
        for (int $$3 = 0; $$3 < $$0.size(); ++$$3) {
            $$1.append(Strings.repeat((String)$$2, (int)(this.depth + 1)));
            $$1.append(new SnbtPrinterTagVisitor($$2, this.depth + 1, this.path).visit($$0.get($$3)));
            if ($$3 == $$0.size() - 1) continue;
            $$1.append(ELEMENT_SEPARATOR).append($$2.isEmpty() ? ELEMENT_SPACING : NEWLINE);
        }
        if (!$$2.isEmpty()) {
            $$1.append(NEWLINE).append(Strings.repeat((String)$$2, (int)this.depth));
        }
        $$1.append(LIST_CLOSE);
        this.result = $$1.toString();
        this.popPath();
    }

    @Override
    public void visitCompound(CompoundTag $$0) {
        String $$2;
        if ($$0.isEmpty()) {
            this.result = "{}";
            return;
        }
        StringBuilder $$1 = new StringBuilder(STRUCT_OPEN);
        this.pushPath("{}");
        String string = $$2 = NO_INDENTATION.contains(this.pathString()) ? "" : this.indentation;
        if (!$$2.isEmpty()) {
            $$1.append(NEWLINE);
        }
        List<String> $$3 = this.getKeys($$0);
        Iterator $$4 = $$3.iterator();
        while ($$4.hasNext()) {
            String $$5 = (String)$$4.next();
            Tag $$6 = $$0.get($$5);
            this.pushPath($$5);
            $$1.append(Strings.repeat((String)$$2, (int)(this.depth + 1))).append(SnbtPrinterTagVisitor.handleEscapePretty($$5)).append(NAME_VALUE_SEPARATOR).append(ELEMENT_SPACING).append(new SnbtPrinterTagVisitor($$2, this.depth + 1, this.path).visit($$6));
            this.popPath();
            if (!$$4.hasNext()) continue;
            $$1.append(ELEMENT_SEPARATOR).append($$2.isEmpty() ? ELEMENT_SPACING : NEWLINE);
        }
        if (!$$2.isEmpty()) {
            $$1.append(NEWLINE).append(Strings.repeat((String)$$2, (int)this.depth));
        }
        $$1.append(STRUCT_CLOSE);
        this.result = $$1.toString();
        this.popPath();
    }

    private void popPath() {
        this.path.remove(this.path.size() - 1);
    }

    private void pushPath(String $$0) {
        this.path.add($$0);
    }

    protected List<String> getKeys(CompoundTag $$0) {
        HashSet $$1 = Sets.newHashSet($$0.keySet());
        ArrayList $$2 = Lists.newArrayList();
        List<String> $$3 = KEY_ORDER.get(this.pathString());
        if ($$3 != null) {
            for (String $$4 : $$3) {
                if (!$$1.remove($$4)) continue;
                $$2.add($$4);
            }
            if (!$$1.isEmpty()) {
                $$1.stream().sorted().forEach($$2::add);
            }
        } else {
            $$2.addAll($$1);
            Collections.sort($$2);
        }
        return $$2;
    }

    public String pathString() {
        return String.join((CharSequence)".", this.path);
    }

    protected static String handleEscapePretty(String $$0) {
        if (SIMPLE_VALUE.matcher($$0).matches()) {
            return $$0;
        }
        return StringTag.quoteAndEscape($$0);
    }

    @Override
    public void visitEnd(EndTag $$0) {
    }
}

