package net.minecraft.util.eventlog;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import org.jspecify.annotations.Nullable;

public interface JsonEventLogReader<T> extends Closeable {
   static <T> JsonEventLogReader<T> create(final Codec<T> $$0, Reader $$1) {
      final JsonReader $$2 = new JsonReader($$1);
      $$2.setStrictness(Strictness.LENIENT);
      return new JsonEventLogReader<T>() {
         @Nullable
         @Override
         public T next() throws IOException {
            try {
               if (!$$2.hasNext()) {
                  return null;
               } else {
                  JsonElement $$0x = JsonParser.parseReader($$2);
                  return (T)$$0.parse(JsonOps.INSTANCE, $$0x).getOrThrow(IOException::new);
               }
            } catch (JsonParseException var2) {
               throw new IOException(var2);
            } catch (EOFException var3) {
               return null;
            }
         }

         @Override
         public void close() throws IOException {
            $$2.close();
         }
      };
   }

   @Nullable
   T next() throws IOException;
}
