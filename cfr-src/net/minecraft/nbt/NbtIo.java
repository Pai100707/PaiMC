/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.util.DelegateDataOutput;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class NbtIo {
    private static final OpenOption[] SYNC_OUTPUT_OPTIONS = new OpenOption[]{StandardOpenOption.SYNC, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

    public static CompoundTag readCompressed(Path $$0, NbtAccounter $$1) throws IOException {
        try (InputStream $$2 = Files.newInputStream($$0, new OpenOption[0]);){
            CompoundTag compoundTag;
            try (FastBufferedInputStream $$3 = new FastBufferedInputStream($$2);){
                compoundTag = NbtIo.readCompressed($$3, $$1);
            }
            return compoundTag;
        }
    }

    private static DataInputStream createDecompressorStream(InputStream $$0) throws IOException {
        return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream($$0)));
    }

    private static DataOutputStream createCompressorStream(OutputStream $$0) throws IOException {
        return new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream($$0)));
    }

    public static CompoundTag readCompressed(InputStream $$0, NbtAccounter $$1) throws IOException {
        try (DataInputStream $$2 = NbtIo.createDecompressorStream($$0);){
            CompoundTag compoundTag = NbtIo.read($$2, $$1);
            return compoundTag;
        }
    }

    public static void parseCompressed(Path $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
        try (InputStream $$3 = Files.newInputStream($$0, new OpenOption[0]);
             FastBufferedInputStream $$4 = new FastBufferedInputStream($$3);){
            NbtIo.parseCompressed($$4, $$1, $$2);
        }
    }

    public static void parseCompressed(InputStream $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
        try (DataInputStream $$3 = NbtIo.createDecompressorStream($$0);){
            NbtIo.parse($$3, $$1, $$2);
        }
    }

    public static void writeCompressed(CompoundTag $$0, Path $$1) throws IOException {
        try (OutputStream $$2 = Files.newOutputStream($$1, SYNC_OUTPUT_OPTIONS);
             BufferedOutputStream $$3 = new BufferedOutputStream($$2);){
            NbtIo.writeCompressed($$0, $$3);
        }
    }

    public static void writeCompressed(CompoundTag $$0, OutputStream $$1) throws IOException {
        try (DataOutputStream $$2 = NbtIo.createCompressorStream($$1);){
            NbtIo.write($$0, $$2);
        }
    }

    public static void write(CompoundTag $$0, Path $$1) throws IOException {
        try (OutputStream $$2 = Files.newOutputStream($$1, SYNC_OUTPUT_OPTIONS);
             BufferedOutputStream $$3 = new BufferedOutputStream($$2);
             DataOutputStream $$4 = new DataOutputStream($$3);){
            NbtIo.write($$0, $$4);
        }
    }

    public static @Nullable CompoundTag read(Path $$0) throws IOException {
        if (!Files.exists($$0, new LinkOption[0])) {
            return null;
        }
        try (InputStream $$1 = Files.newInputStream($$0, new OpenOption[0]);){
            CompoundTag compoundTag;
            try (DataInputStream $$2 = new DataInputStream($$1);){
                compoundTag = NbtIo.read($$2, NbtAccounter.unlimitedHeap());
            }
            return compoundTag;
        }
    }

    public static CompoundTag read(DataInput $$0) throws IOException {
        return NbtIo.read($$0, NbtAccounter.unlimitedHeap());
    }

    public static CompoundTag read(DataInput $$0, NbtAccounter $$1) throws IOException {
        Tag $$2 = NbtIo.readUnnamedTag($$0, $$1);
        if ($$2 instanceof CompoundTag) {
            return (CompoundTag)$$2;
        }
        throw new IOException("Root tag must be a named compound tag");
    }

    public static void write(CompoundTag $$0, DataOutput $$1) throws IOException {
        NbtIo.writeUnnamedTagWithFallback($$0, $$1);
    }

    public static void parse(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
        TagType<?> $$3 = TagTypes.getType($$0.readByte());
        if ($$3 == EndTag.TYPE) {
            if ($$1.visitRootEntry(EndTag.TYPE) == StreamTagVisitor.ValueResult.CONTINUE) {
                $$1.visitEnd();
            }
            return;
        }
        switch ($$1.visitRootEntry($$3)) {
            case HALT: {
                break;
            }
            case BREAK: {
                StringTag.skipString($$0);
                $$3.skip($$0, $$2);
                break;
            }
            case CONTINUE: {
                StringTag.skipString($$0);
                $$3.parse($$0, $$1, $$2);
            }
        }
    }

    public static Tag readAnyTag(DataInput $$0, NbtAccounter $$1) throws IOException {
        byte $$2 = $$0.readByte();
        if ($$2 == 0) {
            return EndTag.INSTANCE;
        }
        return NbtIo.readTagSafe($$0, $$1, $$2);
    }

    public static void writeAnyTag(Tag $$0, DataOutput $$1) throws IOException {
        $$1.writeByte($$0.getId());
        if ($$0.getId() == 0) {
            return;
        }
        $$0.write($$1);
    }

    public static void writeUnnamedTag(Tag $$0, DataOutput $$1) throws IOException {
        $$1.writeByte($$0.getId());
        if ($$0.getId() == 0) {
            return;
        }
        $$1.writeUTF("");
        $$0.write($$1);
    }

    public static void writeUnnamedTagWithFallback(Tag $$0, DataOutput $$1) throws IOException {
        NbtIo.writeUnnamedTag($$0, new StringFallbackDataOutput($$1));
    }

    @VisibleForTesting
    public static Tag readUnnamedTag(DataInput $$0, NbtAccounter $$1) throws IOException {
        byte $$2 = $$0.readByte();
        if ($$2 == 0) {
            return EndTag.INSTANCE;
        }
        StringTag.skipString($$0);
        return NbtIo.readTagSafe($$0, $$1, $$2);
    }

    private static Tag readTagSafe(DataInput $$0, NbtAccounter $$1, byte $$2) {
        try {
            return TagTypes.getType($$2).load($$0, $$1);
        }
        catch (IOException $$3) {
            CrashReport $$4 = CrashReport.forThrowable($$3, "Loading NBT data");
            CrashReportCategory $$5 = $$4.addCategory("NBT Tag");
            $$5.setDetail("Tag type", $$2);
            throw new ReportedNbtException($$4);
        }
    }

    public static class StringFallbackDataOutput
    extends DelegateDataOutput {
        public StringFallbackDataOutput(DataOutput $$0) {
            super($$0);
        }

        @Override
        public void writeUTF(String $$0) throws IOException {
            try {
                super.writeUTF($$0);
            }
            catch (UTFDataFormatException $$1) {
                Util.logAndPauseIfInIde("Failed to write NBT String", $$1);
                super.writeUTF("");
            }
        }
    }
}

