/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.MessageToByteEncoder
 */
package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Deflater;
import net.minecraft.network.VarInt;

public class CompressionEncoder
extends MessageToByteEncoder<ByteBuf> {
    private final byte[] encodeBuf = new byte[8192];
    private final Deflater deflater;
    private int threshold;

    public CompressionEncoder(int $$0) {
        this.threshold = $$0;
        this.deflater = new Deflater();
    }

    protected void encode(ChannelHandlerContext $$0, ByteBuf $$1, ByteBuf $$2) {
        int $$3 = $$1.readableBytes();
        if ($$3 > 0x800000) {
            throw new IllegalArgumentException("Packet too big (is " + $$3 + ", should be less than 8388608)");
        }
        if ($$3 < this.threshold) {
            VarInt.write($$2, 0);
            $$2.writeBytes($$1);
        } else {
            byte[] $$4 = new byte[$$3];
            $$1.readBytes($$4);
            VarInt.write($$2, $$4.length);
            this.deflater.setInput($$4, 0, $$3);
            this.deflater.finish();
            while (!this.deflater.finished()) {
                int $$5 = this.deflater.deflate(this.encodeBuf);
                $$2.writeBytes(this.encodeBuf, 0, $$5);
            }
            this.deflater.reset();
        }
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(int $$0) {
        this.threshold = $$0;
    }

    protected /* synthetic */ void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        this.encode(channelHandlerContext, (ByteBuf)object, byteBuf);
    }
}

