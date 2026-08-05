/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.nbt.NbtAccounterException;

public class NbtAccounter {
    public static final int DEFAULT_NBT_QUOTA = 0x200000;
    public static final int UNCOMPRESSED_NBT_QUOTA = 0x6400000;
    private static final int MAX_STACK_DEPTH = 512;
    private final long quota;
    private long usage;
    private final int maxDepth;
    private int depth;

    public NbtAccounter(long $$0, int $$1) {
        this.quota = $$0;
        this.maxDepth = $$1;
    }

    public static NbtAccounter create(long $$0) {
        return new NbtAccounter($$0, 512);
    }

    public static NbtAccounter defaultQuota() {
        return new NbtAccounter(0x200000L, 512);
    }

    public static NbtAccounter uncompressedQuota() {
        return new NbtAccounter(0x6400000L, 512);
    }

    public static NbtAccounter unlimitedHeap() {
        return new NbtAccounter(Long.MAX_VALUE, 512);
    }

    public void accountBytes(long $$0, long $$1) {
        this.accountBytes($$0 * $$1);
    }

    public void accountBytes(long $$0) {
        if ($$0 < 0L) {
            throw new IllegalArgumentException("Tried to account NBT tag with negative size: " + $$0);
        }
        if (this.usage + $$0 > this.quota) {
            throw new NbtAccounterException("Tried to read NBT tag that was too big; tried to allocate: " + this.usage + " + " + $$0 + " bytes where max allowed: " + this.quota);
        }
        this.usage += $$0;
    }

    public void pushDepth() {
        if (this.depth >= this.maxDepth) {
            throw new NbtAccounterException("Tried to read NBT tag with too high complexity, depth > " + this.maxDepth);
        }
        ++this.depth;
    }

    public void popDepth() {
        if (this.depth <= 0) {
            throw new NbtAccounterException("NBT-Accounter tried to pop stack-depth at top-level");
        }
        --this.depth;
    }

    @VisibleForTesting
    public long getUsage() {
        return this.usage;
    }

    @VisibleForTesting
    public int getDepth() {
        return this.depth;
    }
}

