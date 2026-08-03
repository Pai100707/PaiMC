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
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.util.DelegateDataOutput;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class NbtIo {
   private static final OpenOption[] SYNC_OUTPUT_OPTIONS = new OpenOption[]{
      StandardOpenOption.SYNC, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
   };

   public static net.minecraft.nbt.CompoundTag readCompressed(Path $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
      net.minecraft.nbt.CompoundTag var4;
      try (
         InputStream $$2 = Files.newInputStream($$0);
         InputStream $$3 = new FastBufferedInputStream($$2);
      ) {
         var4 = readCompressed($$3, $$1);
      }

      return var4;
   }

   private static DataInputStream createDecompressorStream(InputStream $$0) throws IOException {
      return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream($$0)));
   }

   private static DataOutputStream createCompressorStream(OutputStream $$0) throws IOException {
      return new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream($$0)));
   }

   public static net.minecraft.nbt.CompoundTag readCompressed(InputStream $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
      net.minecraft.nbt.CompoundTag var3;
      try (DataInputStream $$2 = createDecompressorStream($$0)) {
         var3 = read($$2, $$1);
      }

      return var3;
   }

   public static void parseCompressed(Path $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
      try (
         InputStream $$3 = Files.newInputStream($$0);
         InputStream $$4 = new FastBufferedInputStream($$3);
      ) {
         parseCompressed($$4, $$1, $$2);
      }
   }

   public static void parseCompressed(InputStream $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
      try (DataInputStream $$3 = createDecompressorStream($$0)) {
         parse($$3, $$1, $$2);
      }
   }

   public static void writeCompressed(net.minecraft.nbt.CompoundTag $$0, Path $$1) throws IOException {
      try (
         OutputStream $$2 = Files.newOutputStream($$1, SYNC_OUTPUT_OPTIONS);
         OutputStream $$3 = new BufferedOutputStream($$2);
      ) {
         writeCompressed($$0, $$3);
      }
   }

   public static void writeCompressed(net.minecraft.nbt.CompoundTag $$0, OutputStream $$1) throws IOException {
      try (DataOutputStream $$2 = createCompressorStream($$1)) {
         write($$0, $$2);
      }
   }

   public static void write(net.minecraft.nbt.CompoundTag $$0, Path $$1) throws IOException {
      try (
         OutputStream $$2 = Files.newOutputStream($$1, SYNC_OUTPUT_OPTIONS);
         OutputStream $$3 = new BufferedOutputStream($$2);
         DataOutputStream $$4 = new DataOutputStream($$3);
      ) {
         write($$0, $$4);
      }
   }

   @Nullable
   public static net.minecraft.nbt.CompoundTag read(Path $$0) throws IOException {
      if (!Files.exists($$0)) {
         return null;
      } else {
         net.minecraft.nbt.CompoundTag var3;
         try (
            InputStream $$1 = Files.newInputStream($$0);
            DataInputStream $$2 = new DataInputStream($$1);
         ) {
            var3 = read($$2, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
         }

         return var3;
      }
   }

   public static net.minecraft.nbt.CompoundTag read(DataInput $$0) throws IOException {
      return read($$0, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
   }

   public static net.minecraft.nbt.CompoundTag read(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
      net.minecraft.nbt.Tag $$2 = readUnnamedTag($$0, $$1);
      if ($$2 instanceof net.minecraft.nbt.CompoundTag) {
         return (net.minecraft.nbt.CompoundTag)$$2;
      } else {
         throw new IOException("Root tag must be a named compound tag");
      }
   }

   public static void write(net.minecraft.nbt.CompoundTag $$0, DataOutput $$1) throws IOException {
      writeUnnamedTagWithFallback($$0, $$1);
   }

   public static void parse(DataInput $$0, net.minecraft.nbt.StreamTagVisitor $$1, net.minecraft.nbt.NbtAccounter $$2) throws IOException {
      net.minecraft.nbt.TagType<?> $$3 = net.minecraft.nbt.TagTypes.getType($$0.readByte());
      if ($$3 == net.minecraft.nbt.EndTag.TYPE) {
         if ($$1.visitRootEntry(net.minecraft.nbt.EndTag.TYPE) == net.minecraft.nbt.StreamTagVisitor.ValueResult.CONTINUE) {
            $$1.visitEnd();
         }
      } else {
         switch ($$1.visitRootEntry($$3)) {
            case HALT:
            default:
               break;
            case BREAK:
               net.minecraft.nbt.StringTag.skipString($$0);
               $$3.skip($$0, $$2);
               break;
            case CONTINUE:
               net.minecraft.nbt.StringTag.skipString($$0);
               $$3.parse($$0, $$1, $$2);
         }
      }
   }

   public static net.minecraft.nbt.Tag readAnyTag(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
      byte $$2 = $$0.readByte();
      return (net.minecraft.nbt.Tag)($$2 == 0 ? net.minecraft.nbt.EndTag.INSTANCE : readTagSafe($$0, $$1, $$2));
   }

   public static void writeAnyTag(net.minecraft.nbt.Tag $$0, DataOutput $$1) throws IOException {
      $$1.writeByte($$0.getId());
      if ($$0.getId() != 0) {
         $$0.write($$1);
      }
   }

   public static void writeUnnamedTag(net.minecraft.nbt.Tag $$0, DataOutput $$1) throws IOException {
      $$1.writeByte($$0.getId());
      if ($$0.getId() != 0) {
         $$1.writeUTF("");
         $$0.write($$1);
      }
   }

   public static void writeUnnamedTagWithFallback(net.minecraft.nbt.Tag $$0, DataOutput $$1) throws IOException {
      writeUnnamedTag($$0, new net.minecraft.nbt.NbtIo.StringFallbackDataOutput($$1));
   }

   @VisibleForTesting
   public static net.minecraft.nbt.Tag readUnnamedTag(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1) throws IOException {
      byte $$2 = $$0.readByte();
      if ($$2 == 0) {
         return net.minecraft.nbt.EndTag.INSTANCE;
      } else {
         net.minecraft.nbt.StringTag.skipString($$0);
         return readTagSafe($$0, $$1, $$2);
      }
   }

   private static net.minecraft.nbt.Tag readTagSafe(DataInput $$0, net.minecraft.nbt.NbtAccounter $$1, byte $$2) {
      try {
         return net.minecraft.nbt.TagTypes.getType($$2).load($$0, $$1);
      } catch (IOException var6) {
         CrashReport $$4 = CrashReport.forThrowable(var6, "Loading NBT data");
         CrashReportCategory $$5 = $$4.addCategory("NBT Tag");
         $$5.setDetail("Tag type", $$2);
         throw new net.minecraft.nbt.ReportedNbtException($$4);
      }
   }

   public static class StringFallbackDataOutput extends DelegateDataOutput {
      public StringFallbackDataOutput(DataOutput $$0) {
         super($$0);
      }

      public void writeUTF(String $$0) throws IOException {
         try {
            super.writeUTF($$0);
         } catch (UTFDataFormatException var3) {
            Util.logAndPauseIfInIde("Failed to write NBT String", var3);
            super.writeUTF("");
         }
      }
   }
}
