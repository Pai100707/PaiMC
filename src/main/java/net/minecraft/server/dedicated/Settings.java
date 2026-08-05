package net.minecraft.server.dedicated;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.RegistryAccess;
import org.slf4j.Logger;

public abstract class Settings<T extends Settings<T>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final Properties properties;

   public Settings(Properties $$0) {
      this.properties = $$0;
   }

   public static Properties loadFromFile(Path $$0) {
      try {
         try {
            Properties var13;
            try (InputStream $$1 = Files.newInputStream($$0)) {
               CharsetDecoder $$2 = StandardCharsets.UTF_8
                  .newDecoder()
                  .onMalformedInput(CodingErrorAction.REPORT)
                  .onUnmappableCharacter(CodingErrorAction.REPORT);
               Properties $$3 = new Properties();
               $$3.load(new InputStreamReader($$1, $$2));
               var13 = $$3;
            }

            return var13;
         } catch (CharacterCodingException var9) {
            LOGGER.info("Failed to load properties as UTF-8 from file {}, trying ISO_8859_1", $$0);

            Properties var4;
            try (Reader $$5 = Files.newBufferedReader($$0, StandardCharsets.ISO_8859_1)) {
               Properties $$6 = new Properties();
               $$6.load($$5);
               var4 = $$6;
            }

            return var4;
         }
      } catch (IOException var10) {
         LOGGER.error("Failed to load properties from file: {}", $$0, var10);
         return new Properties();
      }
   }

   public void store(Path $$0) {
      try (Writer $$1 = Files.newBufferedWriter($$0, StandardCharsets.UTF_8)) {
         this.properties.store($$1, "Minecraft server properties");
      } catch (IOException var7) {
         LOGGER.error("Failed to store properties to file: {}", $$0);
      }
   }

   private static <V extends Number> Function<String, V> wrapNumberDeserializer(Function<String, V> $$0) {
      return $$1 -> {
         try {
            return $$0.apply($$1);
         } catch (NumberFormatException var3) {
            return null;
         }
      };
   }

   protected static <V> Function<String, V> dispatchNumberOrString(IntFunction<V> $$0, Function<String, V> $$1) {
      return $$2 -> {
         try {
            return $$0.apply(Integer.parseInt($$2));
         } catch (NumberFormatException var4) {
            return $$1.apply($$2);
         }
      };
   }

   
   private String getStringRaw(String $$0) {
      return (String)this.properties.get($$0);
   }

   
   protected <V> V getLegacy(String $$0, Function<String, V> $$1) {
      String $$2 = this.getStringRaw($$0);
      if ($$2 == null) {
         return null;
      } else {
         this.properties.remove($$0);
         return $$1.apply($$2);
      }
   }

   protected <V> V get(String $$0, Function<String, V> $$1, Function<V, String> $$2, V $$3) {
      String $$4 = this.getStringRaw($$0);
      V $$5 = (V)MoreObjects.firstNonNull($$4 != null ? $$1.apply($$4) : null, $$3);
      this.properties.put($$0, $$2.apply($$5));
      return $$5;
   }

   protected <V> Settings<T>.MutableValue<V> getMutable(String $$0, Function<String, V> $$1, Function<V, String> $$2, V $$3) {
      String $$4 = this.getStringRaw($$0);
      V $$5 = (V)MoreObjects.firstNonNull($$4 != null ? $$1.apply($$4) : null, $$3);
      this.properties.put($$0, $$2.apply($$5));
      return new Settings.MutableValue<>($$0, $$5, $$2);
   }

   protected <V> V get(String $$0, Function<String, V> $$1, UnaryOperator<V> $$2, Function<V, String> $$3, V $$4) {
      return this.get($$0, $$2x -> {
         V $$3x = $$1.apply($$2x);
         return $$3x != null ? $$2.apply($$3x) : null;
      }, $$3, $$4);
   }

   protected <V> V get(String $$0, Function<String, V> $$1, V $$2) {
      return this.get($$0, $$1, Objects::toString, $$2);
   }

   protected <V> Settings<T>.MutableValue<V> getMutable(String $$0, Function<String, V> $$1, V $$2) {
      return this.getMutable($$0, $$1, Objects::toString, $$2);
   }

   protected String get(String $$0, String $$1) {
      return this.get($$0, Function.identity(), Function.identity(), $$1);
   }

   
   protected String getLegacyString(String $$0) {
      return this.getLegacy($$0, Function.identity());
   }

   protected int get(String $$0, int $$1) {
      return this.get($$0, wrapNumberDeserializer(Integer::parseInt), Integer.valueOf($$1));
   }

   protected Settings<T>.MutableValue<Integer> getMutable(String $$0, int $$1) {
      return this.getMutable($$0, wrapNumberDeserializer(Integer::parseInt), $$1);
   }

   protected Settings<T>.MutableValue<String> getMutable(String $$0, String $$1) {
      return this.getMutable($$0, String::new, $$1);
   }

   protected int get(String $$0, UnaryOperator<Integer> $$1, int $$2) {
      return this.get($$0, wrapNumberDeserializer(Integer::parseInt), $$1, Objects::toString, $$2);
   }

   protected long get(String $$0, long $$1) {
      return this.get($$0, wrapNumberDeserializer(Long::parseLong), $$1);
   }

   protected boolean get(String $$0, boolean $$1) {
      return this.get($$0, Boolean::valueOf, $$1);
   }

   protected Settings<T>.MutableValue<Boolean> getMutable(String $$0, boolean $$1) {
      return this.getMutable($$0, Boolean::valueOf, $$1);
   }

   
   protected Boolean getLegacyBoolean(String $$0) {
      return this.getLegacy($$0, Boolean::valueOf);
   }

   protected Properties cloneProperties() {
      Properties $$0 = new Properties();
      $$0.putAll(this.properties);
      return $$0;
   }

   protected abstract T reload(RegistryAccess var1, Properties var2);

   public class MutableValue<V> implements Supplier<V> {
      private final String key;
      private final V value;
      private final Function<V, String> serializer;

      MutableValue(final String $$1, final V $$2, final Function<V, String> $$3) {
         this.key = $$1;
         this.value = $$2;
         this.serializer = $$3;
      }

      @Override
      public V get() {
         return this.value;
      }

      public T update(RegistryAccess $$0, V $$1) {
         Properties $$2 = Settings.this.cloneProperties();
         $$2.put(this.key, this.serializer.apply($$1));
         return Settings.this.reload($$0, $$2);
      }
   }
}
