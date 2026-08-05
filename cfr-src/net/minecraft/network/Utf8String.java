/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufUtil
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.nio.charset.StandardCharsets;
import net.minecraft.network.VarInt;

public class Utf8String {
    public static String read(ByteBuf $$0, int $$1) {
        int $$2 = ByteBufUtil.utf8MaxBytes((int)$$1);
        int $$3 = VarInt.read($$0);
        if ($$3 > $$2) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + $$3 + " > " + $$2 + ")");
        }
        if ($$3 < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        }
        int $$4 = $$0.readableBytes();
        if ($$3 > $$4) {
            throw new DecoderException("Not enough bytes in buffer, expected " + $$3 + ", but got " + $$4);
        }
        String $$5 = $$0.toString($$0.readerIndex(), $$3, StandardCharsets.UTF_8);
        $$0.readerIndex($$0.readerIndex() + $$3);
        if ($$5.length() > $$1) {
            throw new DecoderException("The received string length is longer than maximum allowed (" + $$5.length() + " > " + $$1 + ")");
        }
        return $$5;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void write(ByteBuf $$0, CharSequence $$1, int $$2) {
        if ($$1.length() > $$2) {
            throw new EncoderException("String too big (was " + $$1.length() + " characters, max " + $$2 + ")");
        }
        int $$3 = ByteBufUtil.utf8MaxBytes((CharSequence)$$1);
        ByteBuf $$4 = $$0.alloc().buffer($$3);
        try {
            int $$5 = ByteBufUtil.writeUtf8((ByteBuf)$$4, (CharSequence)$$1);
            int $$6 = ByteBufUtil.utf8MaxBytes((int)$$2);
            if ($$5 > $$6) {
                throw new EncoderException("String too big (was " + $$5 + " bytes encoded, max " + $$6 + ")");
            }
            VarInt.write($$0, $$5);
            $$0.writeBytes($$4);
        }
        finally {
            $$4.release();
        }
    }
}

