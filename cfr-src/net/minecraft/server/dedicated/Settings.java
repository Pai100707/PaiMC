/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.RegistryAccess;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class Settings<T extends Settings<T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final Properties properties;

    public Settings(Properties $$0) {
        this.properties = $$0;
    }

    public static Properties loadFromFile(Path $$0) {
        Properties properties;
        block16: {
            InputStream $$1 = Files.newInputStream($$0, new OpenOption[0]);
            try {
                CharsetDecoder $$2 = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
                Properties $$3 = new Properties();
                $$3.load(new InputStreamReader($$1, $$2));
                properties = $$3;
                if ($$1 == null) break block16;
            }
            catch (Throwable $$2) {
                try {
                    if ($$1 != null) {
                        try {
                            $$1.close();
                        }
                        catch (Throwable $$3) {
                            $$2.addSuppressed($$3);
                        }
                    }
                    throw $$2;
                }
                catch (CharacterCodingException $$4) {
                    Properties properties2;
                    block17: {
                        LOGGER.info("Failed to load properties as UTF-8 from file {}, trying ISO_8859_1", (Object)$$0);
                        BufferedReader $$5 = Files.newBufferedReader($$0, StandardCharsets.ISO_8859_1);
                        try {
                            Properties $$6 = new Properties();
                            $$6.load($$5);
                            properties2 = $$6;
                            if ($$5 == null) break block17;
                        }
                        catch (Throwable throwable) {
                            try {
                                if ($$5 != null) {
                                    try {
                                        ((Reader)$$5).close();
                                    }
                                    catch (Throwable throwable2) {
                                        throwable.addSuppressed(throwable2);
                                    }
                                }
                                throw throwable;
                            }
                            catch (IOException $$7) {
                                LOGGER.error("Failed to load properties from file: {}", (Object)$$0, (Object)$$7);
                                return new Properties();
                            }
                        }
                        ((Reader)$$5).close();
                    }
                    return properties2;
                }
            }
            $$1.close();
        }
        return properties;
    }

    public void store(Path $$0) {
        try (BufferedWriter $$1 = Files.newBufferedWriter($$0, StandardCharsets.UTF_8, new OpenOption[0]);){
            this.properties.store($$1, "Minecraft server properties");
        }
        catch (IOException $$2) {
            LOGGER.error("Failed to store properties to file: {}", (Object)$$0);
        }
    }

    private static <V extends Number> Function<String, @Nullable V> wrapNumberDeserializer(Function<String, V> $$0) {
        return $$1 -> {
            try {
                return (Number)$$0.apply((String)$$1);
            }
            catch (NumberFormatException $$2) {
                return null;
            }
        };
    }

    protected static <V> Function<String, @Nullable V> dispatchNumberOrString(IntFunction<@Nullable V> $$0, Function<String, @Nullable V> $$1) {
        return $$2 -> {
            try {
                return $$0.apply(Integer.parseInt($$2));
            }
            catch (NumberFormatException $$3) {
                return $$1.apply((String)$$2);
            }
        };
    }

    private @Nullable String getStringRaw(String $$0) {
        return (String)this.properties.get($$0);
    }

    protected <V> @Nullable V getLegacy(String $$0, Function<String, V> $$1) {
        String $$2 = this.getStringRaw($$0);
        if ($$2 == null) {
            return null;
        }
        this.properties.remove($$0);
        return $$1.apply($$2);
    }

    protected <V> V get(String $$0, Function<String, @Nullable V> $$1, Function<V, String> $$2, V $$3) {
        String $$4 = this.getStringRaw($$0);
        Object $$5 = MoreObjects.firstNonNull($$4 != null ? $$1.apply($$4) : null, $$3);
        this.properties.put($$0, $$2.apply($$5));
        return (V)$$5;
    }

    protected <V> MutableValue<V> getMutable(String $$0, Function<String, @Nullable V> $$1, Function<V, String> $$2, V $$3) {
        String $$4 = this.getStringRaw($$0);
        Object $$5 = MoreObjects.firstNonNull($$4 != null ? $$1.apply($$4) : null, $$3);
        this.properties.put($$0, $$2.apply($$5));
        return new MutableValue<Object>($$0, $$5, $$2);
    }

    protected <V> V get(String $$0, Function<String, @Nullable V> $$1, UnaryOperator<V> $$22, Function<V, String> $$3, V $$4) {
        return (V)this.get($$0, $$2 -> {
            Object $$3 = $$1.apply((String)$$2);
            return $$3 != null ? $$22.apply($$3) : null;
        }, $$3, $$4);
    }

    protected <V> V get(String $$0, Function<String, V> $$1, V $$2) {
        return (V)this.get($$0, $$1, Objects::toString, $$2);
    }

    protected <V> MutableValue<V> getMutable(String $$0, Function<String, V> $$1, V $$2) {
        return this.getMutable($$0, $$1, Objects::toString, $$2);
    }

    protected String get(String $$0, String $$1) {
        return this.get($$0, Function.identity(), Function.identity(), $$1);
    }

    protected @Nullable String getLegacyString(String $$0) {
        return (String)this.getLegacy($$0, Function.identity());
    }

    protected int get(String $$0, int $$1) {
        return this.get($$0, Settings.wrapNumberDeserializer(Integer::parseInt), Integer.valueOf($$1));
    }

    protected MutableValue<Integer> getMutable(String $$0, int $$1) {
        return this.getMutable($$0, Settings.wrapNumberDeserializer(Integer::parseInt), $$1);
    }

    protected MutableValue<String> getMutable(String $$0, String $$1) {
        return this.getMutable($$0, String::new, $$1);
    }

    protected int get(String $$0, UnaryOperator<Integer> $$1, int $$2) {
        return this.get($$0, Settings.wrapNumberDeserializer(Integer::parseInt), $$1, Objects::toString, $$2);
    }

    protected long get(String $$0, long $$1) {
        return this.get($$0, Settings.wrapNumberDeserializer(Long::parseLong), $$1);
    }

    protected boolean get(String $$0, boolean $$1) {
        return this.get($$0, Boolean::valueOf, $$1);
    }

    protected MutableValue<Boolean> getMutable(String $$0, boolean $$1) {
        return this.getMutable($$0, Boolean::valueOf, $$1);
    }

    protected @Nullable Boolean getLegacyBoolean(String $$0) {
        return this.getLegacy($$0, Boolean::valueOf);
    }

    protected Properties cloneProperties() {
        Properties $$0 = new Properties();
        $$0.putAll((Map<?, ?>)this.properties);
        return $$0;
    }

    protected abstract T reload(RegistryAccess var1, Properties var2);

    public class MutableValue<V>
    implements Supplier<V> {
        private final String key;
        private final V value;
        private final Function<V, String> serializer;

        MutableValue(String $$1, V $$2, Function<V, String> $$3) {
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

