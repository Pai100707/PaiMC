/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.Validate
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import java.util.function.IntConsumer;
import net.minecraft.util.BitStorage;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.Nullable;

public class SimpleBitStorage
implements BitStorage {
    private static final int[] MAGIC = new int[]{-1, -1, 0, Integer.MIN_VALUE, 0, 0, 0x55555555, 0x55555555, 0, Integer.MIN_VALUE, 0, 1, 0x33333333, 0x33333333, 0, 0x2AAAAAAA, 0x2AAAAAAA, 0, 0x24924924, 0x24924924, 0, Integer.MIN_VALUE, 0, 2, 0x1C71C71C, 0x1C71C71C, 0, 0x19999999, 0x19999999, 0, 390451572, 390451572, 0, 0x15555555, 0x15555555, 0, 0x13B13B13, 0x13B13B13, 0, 306783378, 306783378, 0, 0x11111111, 0x11111111, 0, Integer.MIN_VALUE, 0, 3, 0xF0F0F0F, 0xF0F0F0F, 0, 0xE38E38E, 0xE38E38E, 0, 226050910, 226050910, 0, 0xCCCCCCC, 0xCCCCCCC, 0, 0xC30C30C, 0xC30C30C, 0, 195225786, 195225786, 0, 186737708, 186737708, 0, 0xAAAAAAA, 0xAAAAAAA, 0, 171798691, 171798691, 0, 0x9D89D89, 0x9D89D89, 0, 159072862, 159072862, 0, 0x9249249, 0x9249249, 0, 148102320, 148102320, 0, 0x8888888, 0x8888888, 0, 138547332, 138547332, 0, Integer.MIN_VALUE, 0, 4, 130150524, 130150524, 0, 0x7878787, 0x7878787, 0, 0x7507507, 0x7507507, 0, 0x71C71C7, 0x71C71C7, 0, 116080197, 116080197, 0, 113025455, 113025455, 0, 0x6906906, 0x6906906, 0, 0x6666666, 0x6666666, 0, 104755299, 104755299, 0, 0x6186186, 0x6186186, 0, 99882960, 99882960, 0, 97612893, 97612893, 0, 0x5B05B05, 0x5B05B05, 0, 93368854, 93368854, 0, 91382282, 91382282, 0, 0x5555555, 0x5555555, 0, 87652393, 87652393, 0, 85899345, 85899345, 0, 0x5050505, 0x5050505, 0, 0x4EC4EC4, 0x4EC4EC4, 0, 81037118, 81037118, 0, 79536431, 79536431, 0, 78090314, 78090314, 0, 0x4924924, 0x4924924, 0, 75350303, 75350303, 0, 74051160, 74051160, 0, 72796055, 72796055, 0, 0x4444444, 0x4444444, 0, 70409299, 70409299, 0, 69273666, 69273666, 0, 0x4104104, 0x4104104, 0, Integer.MIN_VALUE, 0, 5};
    private final long[] data;
    private final int bits;
    private final long mask;
    private final int size;
    private final int valuesPerLong;
    private final int divideMul;
    private final int divideAdd;
    private final int divideShift;

    public SimpleBitStorage(int $$0, int $$1, int[] $$2) {
        this($$0, $$1);
        int $$4;
        int $$3 = 0;
        for ($$4 = 0; $$4 <= $$1 - this.valuesPerLong; $$4 += this.valuesPerLong) {
            long $$5 = 0L;
            for (int $$6 = this.valuesPerLong - 1; $$6 >= 0; --$$6) {
                $$5 <<= $$0;
                $$5 |= (long)$$2[$$4 + $$6] & this.mask;
            }
            this.data[$$3++] = $$5;
        }
        int $$7 = $$1 - $$4;
        if ($$7 > 0) {
            long $$8 = 0L;
            for (int $$9 = $$7 - 1; $$9 >= 0; --$$9) {
                $$8 <<= $$0;
                $$8 |= (long)$$2[$$4 + $$9] & this.mask;
            }
            this.data[$$3] = $$8;
        }
    }

    public SimpleBitStorage(int $$0, int $$1) {
        this($$0, $$1, (long[])null);
    }

    public SimpleBitStorage(int $$0, int $$1, long @Nullable [] $$2) {
        Validate.inclusiveBetween((long)1L, (long)32L, (long)$$0);
        this.size = $$1;
        this.bits = $$0;
        this.mask = (1L << $$0) - 1L;
        this.valuesPerLong = (char)(64 / $$0);
        int $$3 = 3 * (this.valuesPerLong - 1);
        this.divideMul = MAGIC[$$3 + 0];
        this.divideAdd = MAGIC[$$3 + 1];
        this.divideShift = MAGIC[$$3 + 2];
        int $$4 = ($$1 + this.valuesPerLong - 1) / this.valuesPerLong;
        if ($$2 != null) {
            if ($$2.length != $$4) {
                throw new InitializationException("Invalid length given for storage, got: " + $$2.length + " but expected: " + $$4);
            }
            this.data = $$2;
        } else {
            this.data = new long[$$4];
        }
    }

    private int cellIndex(int $$0) {
        long $$1 = Integer.toUnsignedLong(this.divideMul);
        long $$2 = Integer.toUnsignedLong(this.divideAdd);
        return (int)((long)$$0 * $$1 + $$2 >> 32 >> this.divideShift);
    }

    @Override
    public int getAndSet(int $$0, int $$1) {
        Validate.inclusiveBetween((long)0L, (long)(this.size - 1), (long)$$0);
        Validate.inclusiveBetween((long)0L, (long)this.mask, (long)$$1);
        int $$2 = this.cellIndex($$0);
        long $$3 = this.data[$$2];
        int $$4 = ($$0 - $$2 * this.valuesPerLong) * this.bits;
        int $$5 = (int)($$3 >> $$4 & this.mask);
        this.data[$$2] = $$3 & (this.mask << $$4 ^ 0xFFFFFFFFFFFFFFFFL) | ((long)$$1 & this.mask) << $$4;
        return $$5;
    }

    @Override
    public void set(int $$0, int $$1) {
        Validate.inclusiveBetween((long)0L, (long)(this.size - 1), (long)$$0);
        Validate.inclusiveBetween((long)0L, (long)this.mask, (long)$$1);
        int $$2 = this.cellIndex($$0);
        long $$3 = this.data[$$2];
        int $$4 = ($$0 - $$2 * this.valuesPerLong) * this.bits;
        this.data[$$2] = $$3 & (this.mask << $$4 ^ 0xFFFFFFFFFFFFFFFFL) | ((long)$$1 & this.mask) << $$4;
    }

    @Override
    public int get(int $$0) {
        Validate.inclusiveBetween((long)0L, (long)(this.size - 1), (long)$$0);
        int $$1 = this.cellIndex($$0);
        long $$2 = this.data[$$1];
        int $$3 = ($$0 - $$1 * this.valuesPerLong) * this.bits;
        return (int)($$2 >> $$3 & this.mask);
    }

    @Override
    public long[] getRaw() {
        return this.data;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public int getBits() {
        return this.bits;
    }

    @Override
    public void getAll(IntConsumer $$0) {
        int $$1 = 0;
        for (long $$2 : this.data) {
            for (int $$3 = 0; $$3 < this.valuesPerLong; ++$$3) {
                $$0.accept((int)($$2 & this.mask));
                $$2 >>= this.bits;
                if (++$$1 < this.size) continue;
                return;
            }
        }
    }

    @Override
    public void unpack(int[] $$0) {
        int $$1 = this.data.length;
        int $$2 = 0;
        for (int $$3 = 0; $$3 < $$1 - 1; ++$$3) {
            long $$4 = this.data[$$3];
            for (int $$5 = 0; $$5 < this.valuesPerLong; ++$$5) {
                $$0[$$2 + $$5] = (int)($$4 & this.mask);
                $$4 >>= this.bits;
            }
            $$2 += this.valuesPerLong;
        }
        int $$6 = this.size - $$2;
        if ($$6 > 0) {
            long $$7 = this.data[$$1 - 1];
            for (int $$8 = 0; $$8 < $$6; ++$$8) {
                $$0[$$2 + $$8] = (int)($$7 & this.mask);
                $$7 >>= this.bits;
            }
        }
    }

    @Override
    public BitStorage copy() {
        return new SimpleBitStorage(this.bits, this.size, (long[])this.data.clone());
    }

    public static class InitializationException
    extends RuntimeException {
        InitializationException(String $$0) {
            super($$0);
        }
    }
}

