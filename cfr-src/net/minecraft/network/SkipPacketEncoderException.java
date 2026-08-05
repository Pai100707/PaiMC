/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.handler.codec.EncoderException
 */
package net.minecraft.network;

import io.netty.handler.codec.EncoderException;
import net.minecraft.network.SkipPacketException;
import net.minecraft.network.codec.IdDispatchCodec;

public class SkipPacketEncoderException
extends EncoderException
implements IdDispatchCodec.DontDecorateException,
SkipPacketException {
    public SkipPacketEncoderException(String $$0) {
        super($$0);
    }

    public SkipPacketEncoderException(Throwable $$0) {
        super($$0);
    }
}

