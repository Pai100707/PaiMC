/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class LpVec3 {
    private static final int DATA_BITS = 15;
    private static final int DATA_BITS_MASK = Short.MAX_VALUE;
    private static final double MAX_QUANTIZED_VALUE = 32766.0;
    private static final int SCALE_BITS = 2;
    private static final int SCALE_BITS_MASK = 3;
    private static final int CONTINUATION_FLAG = 4;
    private static final int X_OFFSET = 3;
    private static final int Y_OFFSET = 18;
    private static final int Z_OFFSET = 33;
    public static final double ABS_MAX_VALUE = 1.7179869183E10;
    public static final double ABS_MIN_VALUE = 3.051944088384301E-5;

    public static boolean hasContinuationBit(int $$0) {
        return ($$0 & 4) == 4;
    }

    public static Vec3 read(ByteBuf $$0) {
        short $$1 = $$0.readUnsignedByte();
        if ($$1 == 0) {
            return Vec3.ZERO;
        }
        short $$2 = $$0.readUnsignedByte();
        long $$3 = $$0.readUnsignedInt();
        long $$4 = $$3 << 16 | (long)($$2 << 8) | (long)$$1;
        long $$5 = $$1 & 3;
        if (LpVec3.hasContinuationBit($$1)) {
            $$5 |= ((long)VarInt.read($$0) & 0xFFFFFFFFL) << 2;
        }
        return new Vec3(LpVec3.unpack($$4 >> 3) * (double)$$5, LpVec3.unpack($$4 >> 18) * (double)$$5, LpVec3.unpack($$4 >> 33) * (double)$$5);
    }

    public static void write(ByteBuf $$0, Vec3 $$1) {
        double $$4;
        double $$3;
        double $$2 = LpVec3.sanitize($$1.x);
        double $$5 = Mth.absMax($$2, Mth.absMax($$3 = LpVec3.sanitize($$1.y), $$4 = LpVec3.sanitize($$1.z)));
        if ($$5 < 3.051944088384301E-5) {
            $$0.writeByte(0);
            return;
        }
        long $$6 = Mth.ceilLong($$5);
        boolean $$7 = ($$6 & 3L) != $$6;
        long $$8 = $$7 ? $$6 & 3L | 4L : $$6;
        long $$9 = LpVec3.pack($$2 / (double)$$6) << 3;
        long $$10 = LpVec3.pack($$3 / (double)$$6) << 18;
        long $$11 = LpVec3.pack($$4 / (double)$$6) << 33;
        long $$12 = $$8 | $$9 | $$10 | $$11;
        $$0.writeByte((int)((byte)$$12));
        $$0.writeByte((int)((byte)($$12 >> 8)));
        $$0.writeInt((int)($$12 >> 16));
        if ($$7) {
            VarInt.write($$0, (int)($$6 >> 2));
        }
    }

    private static double sanitize(double $$0) {
        return Double.isNaN($$0) ? 0.0 : Math.clamp($$0, -1.7179869183E10, 1.7179869183E10);
    }

    private static long pack(double $$0) {
        return Math.round(($$0 * 0.5 + 0.5) * 32766.0);
    }

    private static double unpack(long $$0) {
        return Math.min((double)($$0 & 0x7FFFL), 32766.0) * 2.0 / 32766.0 - 1.0;
    }
}

