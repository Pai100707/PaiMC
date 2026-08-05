/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.rcon;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NetworkDataOutputStream {
    private final ByteArrayOutputStream outputStream;
    private final DataOutputStream dataOutputStream;

    public NetworkDataOutputStream(int $$0) {
        this.outputStream = new ByteArrayOutputStream($$0);
        this.dataOutputStream = new DataOutputStream(this.outputStream);
    }

    public void writeBytes(byte[] $$0) throws IOException {
        this.dataOutputStream.write($$0, 0, $$0.length);
    }

    public void writeString(String $$0) throws IOException {
        this.dataOutputStream.write($$0.getBytes(StandardCharsets.UTF_8));
        this.dataOutputStream.write(0);
    }

    public void write(int $$0) throws IOException {
        this.dataOutputStream.write($$0);
    }

    public void writeShort(short $$0) throws IOException {
        this.dataOutputStream.writeShort(Short.reverseBytes($$0));
    }

    public void writeInt(int $$0) throws IOException {
        this.dataOutputStream.writeInt(Integer.reverseBytes($$0));
    }

    public void writeFloat(float $$0) throws IOException {
        this.dataOutputStream.writeInt(Integer.reverseBytes(Float.floatToIntBits($$0)));
    }

    public byte[] toByteArray() {
        return this.outputStream.toByteArray();
    }

    public void reset() {
        this.outputStream.reset();
    }
}

