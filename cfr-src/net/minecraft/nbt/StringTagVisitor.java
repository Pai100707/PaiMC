/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.util.ArrayList;
import java.util.Map;
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

public class StringTagVisitor
implements TagVisitor {
    private static final Pattern UNQUOTED_KEY_MATCH = Pattern.compile("[A-Za-z._]+[A-Za-z0-9._+-]*");
    private final StringBuilder builder = new StringBuilder();

    public String build() {
        return this.builder.toString();
    }

    @Override
    public void visitString(StringTag $$0) {
        this.builder.append(StringTag.quoteAndEscape($$0.value()));
    }

    @Override
    public void visitByte(ByteTag $$0) {
        this.builder.append($$0.value()).append('b');
    }

    @Override
    public void visitShort(ShortTag $$0) {
        this.builder.append($$0.value()).append('s');
    }

    @Override
    public void visitInt(IntTag $$0) {
        this.builder.append($$0.value());
    }

    @Override
    public void visitLong(LongTag $$0) {
        this.builder.append($$0.value()).append('L');
    }

    @Override
    public void visitFloat(FloatTag $$0) {
        this.builder.append($$0.value()).append('f');
    }

    @Override
    public void visitDouble(DoubleTag $$0) {
        this.builder.append($$0.value()).append('d');
    }

    @Override
    public void visitByteArray(ByteArrayTag $$0) {
        this.builder.append("[B;");
        byte[] $$1 = $$0.getAsByteArray();
        for (int $$2 = 0; $$2 < $$1.length; ++$$2) {
            if ($$2 != 0) {
                this.builder.append(',');
            }
            this.builder.append($$1[$$2]).append('B');
        }
        this.builder.append(']');
    }

    @Override
    public void visitIntArray(IntArrayTag $$0) {
        this.builder.append("[I;");
        int[] $$1 = $$0.getAsIntArray();
        for (int $$2 = 0; $$2 < $$1.length; ++$$2) {
            if ($$2 != 0) {
                this.builder.append(',');
            }
            this.builder.append($$1[$$2]);
        }
        this.builder.append(']');
    }

    @Override
    public void visitLongArray(LongArrayTag $$0) {
        this.builder.append("[L;");
        long[] $$1 = $$0.getAsLongArray();
        for (int $$2 = 0; $$2 < $$1.length; ++$$2) {
            if ($$2 != 0) {
                this.builder.append(',');
            }
            this.builder.append($$1[$$2]).append('L');
        }
        this.builder.append(']');
    }

    @Override
    public void visitList(ListTag $$0) {
        this.builder.append('[');
        for (int $$1 = 0; $$1 < $$0.size(); ++$$1) {
            if ($$1 != 0) {
                this.builder.append(',');
            }
            $$0.get($$1).accept(this);
        }
        this.builder.append(']');
    }

    @Override
    public void visitCompound(CompoundTag $$0) {
        this.builder.append('{');
        ArrayList<Map.Entry<String, Tag>> $$1 = new ArrayList<Map.Entry<String, Tag>>($$0.entrySet());
        $$1.sort(Map.Entry.comparingByKey());
        for (int $$2 = 0; $$2 < $$1.size(); ++$$2) {
            Map.Entry $$3 = (Map.Entry)$$1.get($$2);
            if ($$2 != 0) {
                this.builder.append(',');
            }
            this.handleKeyEscape((String)$$3.getKey());
            this.builder.append(':');
            ((Tag)$$3.getValue()).accept(this);
        }
        this.builder.append('}');
    }

    private void handleKeyEscape(String $$0) {
        if (!$$0.equalsIgnoreCase("true") && !$$0.equalsIgnoreCase("false") && UNQUOTED_KEY_MATCH.matcher($$0).matches()) {
            this.builder.append($$0);
        } else {
            StringTag.quoteAndEscape($$0, this.builder);
        }
    }

    @Override
    public void visitEnd(EndTag $$0) {
        this.builder.append("END");
    }
}

