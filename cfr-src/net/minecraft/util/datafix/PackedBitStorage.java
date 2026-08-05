/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.Validate
 */
package net.minecraft.util.datafix;

import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;

public class PackedBitStorage {
    private static final int BIT_TO_LONG_SHIFT = 6;
    private final long[] data;
    private final int bits;
    private final long mask;
    private final int size;

    public PackedBitStorage(int $$0, int $$1) {
        this($$0, $$1, new long[Mth.roundToward($$1 * $$0, 64) / 64]);
    }

    public PackedBitStorage(int $$0, int $$1, long[] $$2) {
        Validate.inclusiveBetween((long)1L, (long)32L, (long)$$0);
        this.size = $$1;
        this.bits = $$0;
        this.data = $$2;
        this.mask = (1L << $$0) - 1L;
        int $$3 = Mth.roundToward($$1 * $$0, 64) / 64;
        if ($$2.length != $$3) {
            throw new IllegalArgumentException("Invalid length given for storage, got: " + $$2.length + " but expected: " + $$3);
        }
    }

    public void set(int $$0, int $$1) {
        Validate.inclusiveBetween((long)0L, (long)(this.size - 1), (long)$$0);
        Validate.inclusiveBetween((long)0L, (long)this.mask, (long)$$1);
        int $$2 = $$0 * this.bits;
        int $$3 = $$2 >> 6;
        int $$4 = ($$0 + 1) * this.bits - 1 >> 6;
        int $$5 = $$2 ^ $$3 << 6;
        this.data[$$3] = this.data[$$3] & (this.mask << $$5 ^ 0xFFFFFFFFFFFFFFFFL) | ((long)$$1 & this.mask) << $$5;
        if ($$3 != $$4) {
            int $$6 = 64 - $$5;
            int $$7 = this.bits - $$6;
            this.data[$$4] = this.data[$$4] >>> $$7 << $$7 | ((long)$$1 & this.mask) >> $$6;
        }
    }

    public int get(int $$0) {
        Validate.inclusiveBetween((long)0L, (long)(this.size - 1), (long)$$0);
        int $$1 = $$0 * this.bits;
        int $$2 = $$1 >> 6;
        int $$3 = ($$0 + 1) * this.bits - 1 >> 6;
        int $$4 = $$1 ^ $$2 << 6;
        if ($$2 == $$3) {
            return (int)(this.data[$$2] >>> $$4 & this.mask);
        }
        int $$5 = 64 - $$4;
        return (int)((this.data[$$2] >>> $$4 | this.data[$$3] << $$5) & this.mask);
    }

    public long[] getRaw() {
        return this.data;
    }

    public int getBits() {
        return this.bits;
    }
}

