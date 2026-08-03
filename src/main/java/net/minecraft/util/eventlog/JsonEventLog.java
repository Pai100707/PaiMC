package net.minecraft.util.eventlog;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.Nullable;

public class JsonEventLog<T> implements Closeable {
   private static final Gson GSON = new Gson();
   private final Codec<T> codec;
   final FileChannel channel;
   private final AtomicInteger referenceCount = new AtomicInteger(1);

   public JsonEventLog(Codec<T> $$0, FileChannel $$1) {
      this.codec = $$0;
      this.channel = $$1;
   }

   public static <T> JsonEventLog<T> open(Codec<T> $$0, Path $$1) throws IOException {
      FileChannel $$2 = FileChannel.open($$1, StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
      return new JsonEventLog<>($$0, $$2);
   }

   public void write(T $$0) throws IOException {
      JsonElement $$1 = (JsonElement)this.codec.encodeStart(JsonOps.INSTANCE, $$0).getOrThrow(IOException::new);
      this.channel.position(this.channel.size());
      Writer $$2 = Channels.newWriter(this.channel, StandardCharsets.UTF_8);
      GSON.toJson($$1, GSON.newJsonWriter($$2));
      $$2.write(10);
      $$2.flush();
   }

   public JsonEventLogReader<T> openReader() throws IOException {
      if (this.referenceCount.get() <= 0) {
         throw new IOException("Event log has already been closed");
      } else {
         this.referenceCount.incrementAndGet();
         final JsonEventLogReader<T> $$0 = JsonEventLogReader.create(this.codec, Channels.newReader(this.channel, StandardCharsets.UTF_8));
         return new JsonEventLogReader<T>() {
            private volatile long position;

            @Nullable
            @Override
            public T next() throws IOException {
               Object var1;
               try {
                  JsonEventLog.this.channel.position(this.position);
                  var1 = $$0.next();
               } finally {
                  this.position = JsonEventLog.this.channel.position();
               }

               return (T)var1;
            }

            @Override
            public void close() throws IOException {
               JsonEventLog.this.releaseReference();
            }
         };
      }
   }

   @Override
   public void close() throws IOException {
      this.releaseReference();
   }

   void releaseReference() throws IOException {
      if (this.referenceCount.decrementAndGet() <= 0) {
         this.channel.close();
      }
   }
}
