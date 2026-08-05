/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.io.IOException;
import java.io.InputStream;

public class FastBufferedInputStream
extends InputStream {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private final InputStream in;
    private final byte[] buffer;
    private int limit;
    private int position;

    public FastBufferedInputStream(InputStream $$0) {
        this($$0, 8192);
    }

    public FastBufferedInputStream(InputStream $$0, int $$1) {
        this.in = $$0;
        this.buffer = new byte[$$1];
    }

    @Override
    public int read() throws IOException {
        if (this.position >= this.limit) {
            this.fill();
            if (this.position >= this.limit) {
                return -1;
            }
        }
        return Byte.toUnsignedInt(this.buffer[this.position++]);
    }

    @Override
    public int read(byte[] $$0, int $$1, int $$2) throws IOException {
        int $$3 = this.bytesInBuffer();
        if ($$3 <= 0) {
            if ($$2 >= this.buffer.length) {
                return this.in.read($$0, $$1, $$2);
            }
            this.fill();
            $$3 = this.bytesInBuffer();
            if ($$3 <= 0) {
                return -1;
            }
        }
        if ($$2 > $$3) {
            $$2 = $$3;
        }
        System.arraycopy(this.buffer, this.position, $$0, $$1, $$2);
        this.position += $$2;
        return $$2;
    }

    @Override
    public long skip(long $$0) throws IOException {
        if ($$0 <= 0L) {
            return 0L;
        }
        long $$1 = this.bytesInBuffer();
        if ($$1 <= 0L) {
            return this.in.skip($$0);
        }
        if ($$0 > $$1) {
            $$0 = $$1;
        }
        this.position = (int)((long)this.position + $$0);
        return $$0;
    }

    @Override
    public int available() throws IOException {
        return this.bytesInBuffer() + this.in.available();
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }

    private int bytesInBuffer() {
        return this.limit - this.position;
    }

    private void fill() throws IOException {
        this.limit = 0;
        this.position = 0;
        int $$0 = this.in.read(this.buffer, 0, this.buffer.length);
        if ($$0 > 0) {
            this.limit = $$0;
        }
    }
}

