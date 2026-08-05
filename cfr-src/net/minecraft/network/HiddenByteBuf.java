/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufUtil
 *  io.netty.util.ReferenceCounted
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCounted;

public record HiddenByteBuf(ByteBuf contents) implements ReferenceCounted
{
    public HiddenByteBuf(ByteBuf $$0) {
        this.contents = ByteBufUtil.ensureAccessible((ByteBuf)$$0);
    }

    public static Object pack(Object $$0) {
        if ($$0 instanceof ByteBuf) {
            ByteBuf $$1 = (ByteBuf)$$0;
            return new HiddenByteBuf($$1);
        }
        return $$0;
    }

    public static Object unpack(Object $$0) {
        if ($$0 instanceof HiddenByteBuf) {
            HiddenByteBuf $$1 = (HiddenByteBuf)$$0;
            return ByteBufUtil.ensureAccessible((ByteBuf)$$1.contents);
        }
        return $$0;
    }

    public int refCnt() {
        return this.contents.refCnt();
    }

    public HiddenByteBuf retain() {
        this.contents.retain();
        return this;
    }

    public HiddenByteBuf retain(int $$0) {
        this.contents.retain($$0);
        return this;
    }

    public HiddenByteBuf touch() {
        this.contents.touch();
        return this;
    }

    public HiddenByteBuf touch(Object $$0) {
        this.contents.touch($$0);
        return this;
    }

    public boolean release() {
        return this.contents.release();
    }

    public boolean release(int $$0) {
        return this.contents.release($$0);
    }

    public /* synthetic */ ReferenceCounted touch(Object object) {
        return this.touch(object);
    }

    public /* synthetic */ ReferenceCounted touch() {
        return this.touch();
    }

    public /* synthetic */ ReferenceCounted retain(int n) {
        return this.retain(n);
    }

    public /* synthetic */ ReferenceCounted retain() {
        return this.retain();
    }
}

