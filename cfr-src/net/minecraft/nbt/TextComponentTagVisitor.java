/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
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
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;

public class TextComponentTagVisitor
implements TagVisitor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int INLINE_LIST_THRESHOLD = 8;
    private static final int MAX_DEPTH = 64;
    private static final int MAX_LENGTH = 128;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_KEY = ChatFormatting.AQUA;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_STRING = ChatFormatting.GREEN;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER = ChatFormatting.GOLD;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER_TYPE = ChatFormatting.RED;
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String LIST_OPEN = "[";
    private static final String LIST_CLOSE = "]";
    private static final String LIST_TYPE_SEPARATOR = ";";
    private static final String ELEMENT_SPACING = " ";
    private static final String STRUCT_OPEN = "{";
    private static final String STRUCT_CLOSE = "}";
    private static final String NEWLINE = "\n";
    private static final String NAME_VALUE_SEPARATOR = ": ";
    private static final String ELEMENT_SEPARATOR = String.valueOf(',');
    private static final String WRAPPED_ELEMENT_SEPARATOR = ELEMENT_SEPARATOR + "\n";
    private static final String SPACED_ELEMENT_SEPARATOR = ELEMENT_SEPARATOR + " ";
    private static final Component FOLDED = Component.literal("<...>").withStyle(ChatFormatting.GRAY);
    private static final Component BYTE_TYPE = Component.literal("b").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final Component SHORT_TYPE = Component.literal("s").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final Component INT_TYPE = Component.literal("I").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final Component LONG_TYPE = Component.literal("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final Component FLOAT_TYPE = Component.literal("f").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final Component DOUBLE_TYPE = Component.literal("d").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final Component BYTE_ARRAY_TYPE = Component.literal("B").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private final String indentation;
    private int indentDepth;
    private int depth;
    private final MutableComponent result = Component.empty();

    public TextComponentTagVisitor(String $$0) {
        this.indentation = $$0;
    }

    public Component visit(Tag $$0) {
        $$0.accept(this);
        return this.result;
    }

    @Override
    public void visitString(StringTag $$0) {
        String $$1 = StringTag.quoteAndEscape($$0.value());
        String $$2 = $$1.substring(0, 1);
        MutableComponent $$3 = Component.literal($$1.substring(1, $$1.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_STRING);
        this.result.append($$2).append($$3).append($$2);
    }

    @Override
    public void visitByte(ByteTag $$0) {
        this.result.append(Component.literal(String.valueOf($$0.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(BYTE_TYPE);
    }

    @Override
    public void visitShort(ShortTag $$0) {
        this.result.append(Component.literal(String.valueOf($$0.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(SHORT_TYPE);
    }

    @Override
    public void visitInt(IntTag $$0) {
        this.result.append(Component.literal(String.valueOf($$0.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
    }

    @Override
    public void visitLong(LongTag $$0) {
        this.result.append(Component.literal(String.valueOf($$0.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(LONG_TYPE);
    }

    @Override
    public void visitFloat(FloatTag $$0) {
        this.result.append(Component.literal(String.valueOf($$0.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(FLOAT_TYPE);
    }

    @Override
    public void visitDouble(DoubleTag $$0) {
        this.result.append(Component.literal(String.valueOf($$0.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(DOUBLE_TYPE);
    }

    @Override
    public void visitByteArray(ByteArrayTag $$0) {
        this.result.append(LIST_OPEN).append(BYTE_ARRAY_TYPE).append(LIST_TYPE_SEPARATOR);
        byte[] $$1 = $$0.getAsByteArray();
        for (int $$2 = 0; $$2 < $$1.length && $$2 < 128; ++$$2) {
            MutableComponent $$3 = Component.literal(String.valueOf($$1[$$2])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            this.result.append(ELEMENT_SPACING).append($$3).append(BYTE_ARRAY_TYPE);
            if ($$2 == $$1.length - 1) continue;
            this.result.append(ELEMENT_SEPARATOR);
        }
        if ($$1.length > 128) {
            this.result.append(FOLDED);
        }
        this.result.append(LIST_CLOSE);
    }

    @Override
    public void visitIntArray(IntArrayTag $$0) {
        this.result.append(LIST_OPEN).append(INT_TYPE).append(LIST_TYPE_SEPARATOR);
        int[] $$1 = $$0.getAsIntArray();
        for (int $$2 = 0; $$2 < $$1.length && $$2 < 128; ++$$2) {
            this.result.append(ELEMENT_SPACING).append(Component.literal(String.valueOf($$1[$$2])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
            if ($$2 == $$1.length - 1) continue;
            this.result.append(ELEMENT_SEPARATOR);
        }
        if ($$1.length > 128) {
            this.result.append(FOLDED);
        }
        this.result.append(LIST_CLOSE);
    }

    @Override
    public void visitLongArray(LongArrayTag $$0) {
        this.result.append(LIST_OPEN).append(LONG_TYPE).append(LIST_TYPE_SEPARATOR);
        long[] $$1 = $$0.getAsLongArray();
        for (int $$2 = 0; $$2 < $$1.length && $$2 < 128; ++$$2) {
            MutableComponent $$3 = Component.literal(String.valueOf($$1[$$2])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            this.result.append(ELEMENT_SPACING).append($$3).append(LONG_TYPE);
            if ($$2 == $$1.length - 1) continue;
            this.result.append(ELEMENT_SEPARATOR);
        }
        if ($$1.length > 128) {
            this.result.append(FOLDED);
        }
        this.result.append(LIST_CLOSE);
    }

    private static boolean shouldWrapListElements(ListTag $$0) {
        if ($$0.size() >= 8) {
            return false;
        }
        for (Tag $$1 : $$0) {
            if ($$1 instanceof NumericTag) continue;
            return true;
        }
        return false;
    }

    @Override
    public void visitList(ListTag $$0) {
        if ($$0.isEmpty()) {
            this.result.append("[]");
            return;
        }
        if (this.depth >= 64) {
            this.result.append(LIST_OPEN).append(FOLDED).append(LIST_CLOSE);
            return;
        }
        if (!TextComponentTagVisitor.shouldWrapListElements($$0)) {
            this.result.append(LIST_OPEN);
            for (int $$1 = 0; $$1 < $$0.size(); ++$$1) {
                if ($$1 != 0) {
                    this.result.append(SPACED_ELEMENT_SEPARATOR);
                }
                this.appendSubTag($$0.get($$1), false);
            }
            this.result.append(LIST_CLOSE);
            return;
        }
        this.result.append(LIST_OPEN);
        if (!this.indentation.isEmpty()) {
            this.result.append(NEWLINE);
        }
        String $$2 = Strings.repeat((String)this.indentation, (int)(this.indentDepth + 1));
        for (int $$3 = 0; $$3 < $$0.size() && $$3 < 128; ++$$3) {
            this.result.append($$2);
            this.appendSubTag($$0.get($$3), true);
            if ($$3 == $$0.size() - 1) continue;
            this.result.append(this.indentation.isEmpty() ? SPACED_ELEMENT_SEPARATOR : WRAPPED_ELEMENT_SEPARATOR);
        }
        if ($$0.size() > 128) {
            this.result.append($$2).append(FOLDED);
        }
        if (!this.indentation.isEmpty()) {
            this.result.append(NEWLINE + Strings.repeat((String)this.indentation, (int)this.indentDepth));
        }
        this.result.append(LIST_CLOSE);
    }

    @Override
    public void visitCompound(CompoundTag $$0) {
        if ($$0.isEmpty()) {
            this.result.append("{}");
            return;
        }
        if (this.depth >= 64) {
            this.result.append(STRUCT_OPEN).append(FOLDED).append(STRUCT_CLOSE);
            return;
        }
        this.result.append(STRUCT_OPEN);
        Collection<String> $$1 = $$0.keySet();
        if (LOGGER.isDebugEnabled()) {
            ArrayList $$2 = Lists.newArrayList($$0.keySet());
            Collections.sort($$2);
            $$1 = $$2;
        }
        if (!this.indentation.isEmpty()) {
            this.result.append(NEWLINE);
        }
        String $$3 = Strings.repeat((String)this.indentation, (int)(this.indentDepth + 1));
        Iterator<String> $$4 = $$1.iterator();
        while ($$4.hasNext()) {
            String $$5 = $$4.next();
            this.result.append($$3).append(TextComponentTagVisitor.handleEscapePretty($$5)).append(NAME_VALUE_SEPARATOR);
            this.appendSubTag($$0.get($$5), true);
            if (!$$4.hasNext()) continue;
            this.result.append(this.indentation.isEmpty() ? SPACED_ELEMENT_SEPARATOR : WRAPPED_ELEMENT_SEPARATOR);
        }
        if (!this.indentation.isEmpty()) {
            this.result.append(NEWLINE + Strings.repeat((String)this.indentation, (int)this.indentDepth));
        }
        this.result.append(STRUCT_CLOSE);
    }

    private void appendSubTag(Tag $$0, boolean $$1) {
        if ($$1) {
            ++this.indentDepth;
        }
        ++this.depth;
        try {
            $$0.accept(this);
        }
        finally {
            if ($$1) {
                --this.indentDepth;
            }
            --this.depth;
        }
    }

    protected static Component handleEscapePretty(String $$0) {
        if (SIMPLE_VALUE.matcher($$0).matches()) {
            return Component.literal($$0).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        }
        String $$1 = StringTag.quoteAndEscape($$0);
        String $$2 = $$1.substring(0, 1);
        MutableComponent $$3 = Component.literal($$1.substring(1, $$1.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        return Component.literal($$2).append($$3).append($$2);
    }

    @Override
    public void visitEnd(EndTag $$0) {
    }
}

