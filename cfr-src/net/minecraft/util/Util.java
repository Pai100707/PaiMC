/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Function
 *  com.google.common.base.Ticker
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.util.concurrent.ListeningExecutorService
 *  com.google.common.util.concurrent.MoreExecutors
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectLists
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ReferenceImmutableList
 *  it.unimi.dsi.fastutil.objects.ReferenceList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.base.Function;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceImmutableList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.CharPredicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.ReportType;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SuppressForbidden;
import net.minecraft.TracingExecutor;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SingleKeyCache;
import net.minecraft.util.TimeSource;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Util {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_MAX_THREADS = 255;
    private static final int DEFAULT_SAFE_FILE_OPERATION_RETRIES = 10;
    private static final String MAX_THREADS_SYSTEM_PROPERTY = "max.bg.threads";
    private static final TracingExecutor BACKGROUND_EXECUTOR = Util.makeExecutor("Main");
    private static final TracingExecutor IO_POOL = Util.makeIoExecutor("IO-Worker-", false);
    private static final TracingExecutor DOWNLOAD_POOL = Util.makeIoExecutor("Download-", true);
    private static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
    public static final int LINEAR_LOOKUP_THRESHOLD = 8;
    private static final Set<String> ALLOWED_UNTRUSTED_LINK_PROTOCOLS = Set.of("http", "https");
    public static final long NANOS_PER_MILLI = 1000000L;
    public static TimeSource.NanoTimeSource timeSource = System::nanoTime;
    public static final Ticker TICKER = new Ticker(){

        public long read() {
            return timeSource.getAsLong();
        }
    };
    public static final UUID NIL_UUID = new UUID(0L, 0L);
    public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders().stream().filter($$0 -> $$0.getScheme().equalsIgnoreCase("jar")).findFirst().orElseThrow(() -> new IllegalStateException("No jar file system provider found"));
    private static Consumer<String> thePauser = $$0 -> {};

    public static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <T> Collector<T, ?, List<T>> toMutableList() {
        return Collectors.toCollection(Lists::newArrayList);
    }

    public static <T extends Comparable<T>> String getPropertyName(Property<T> $$0, Object $$1) {
        return $$0.getName((Comparable)$$1);
    }

    public static String makeDescriptionId(String $$0, @Nullable Identifier $$1) {
        if ($$1 == null) {
            return $$0 + ".unregistered_sadface";
        }
        return $$0 + "." + $$1.getNamespace() + "." + $$1.getPath().replace('/', '.');
    }

    public static long getMillis() {
        return Util.getNanos() / 1000000L;
    }

    public static long getNanos() {
        return timeSource.getAsLong();
    }

    public static long getEpochMillis() {
        return Instant.now().toEpochMilli();
    }

    public static String getFilenameFormattedDateTime() {
        return FILENAME_DATE_TIME_FORMATTER.format(ZonedDateTime.now());
    }

    private static TracingExecutor makeExecutor(final String $$0) {
        ForkJoinPool $$4;
        int $$1 = Util.maxAllowedExecutorThreads();
        if ($$1 <= 0) {
            ListeningExecutorService $$22 = MoreExecutors.newDirectExecutorService();
        } else {
            AtomicInteger $$3 = new AtomicInteger(1);
            $$4 = new ForkJoinPool($$1, $$2 -> {
                final String $$3 = "Worker-" + $$0 + "-" + $$3.getAndIncrement();
                ForkJoinWorkerThread $$4 = new ForkJoinWorkerThread($$2){

                    @Override
                    protected void onStart() {
                        TracyClient.setThreadName((String)$$3, (int)$$0.hashCode());
                        super.onStart();
                    }

                    @Override
                    protected void onTermination(@Nullable Throwable $$02) {
                        if ($$02 != null) {
                            LOGGER.warn("{} died", (Object)this.getName(), (Object)$$02);
                        } else {
                            LOGGER.debug("{} shutdown", (Object)this.getName());
                        }
                        super.onTermination($$02);
                    }
                };
                $$4.setName($$3);
                return $$4;
            }, Util::onThreadException, true);
        }
        return new TracingExecutor($$4);
    }

    public static int maxAllowedExecutorThreads() {
        return Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, Util.getMaxThreads());
    }

    private static int getMaxThreads() {
        String $$0 = System.getProperty(MAX_THREADS_SYSTEM_PROPERTY);
        if ($$0 != null) {
            try {
                int $$1 = Integer.parseInt($$0);
                if ($$1 >= 1 && $$1 <= 255) {
                    return $$1;
                }
                LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{MAX_THREADS_SYSTEM_PROPERTY, $$0, 255});
            }
            catch (NumberFormatException $$2) {
                LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{MAX_THREADS_SYSTEM_PROPERTY, $$0, 255});
            }
        }
        return 255;
    }

    public static TracingExecutor backgroundExecutor() {
        return BACKGROUND_EXECUTOR;
    }

    public static TracingExecutor ioPool() {
        return IO_POOL;
    }

    public static TracingExecutor nonCriticalIoPool() {
        return DOWNLOAD_POOL;
    }

    public static void shutdownExecutors() {
        BACKGROUND_EXECUTOR.shutdownAndAwait(3L, TimeUnit.SECONDS);
        IO_POOL.shutdownAndAwait(3L, TimeUnit.SECONDS);
    }

    private static TracingExecutor makeIoExecutor(String $$0, boolean $$1) {
        AtomicInteger $$2 = new AtomicInteger(1);
        return new TracingExecutor(Executors.newCachedThreadPool($$3 -> {
            Thread $$4 = new Thread($$3);
            String $$5 = $$0 + $$2.getAndIncrement();
            TracyClient.setThreadName((String)$$5, (int)$$0.hashCode());
            $$4.setName($$5);
            $$4.setDaemon($$1);
            $$4.setUncaughtExceptionHandler(Util::onThreadException);
            return $$4;
        }));
    }

    public static void throwAsRuntime(Throwable $$0) {
        throw $$0 instanceof RuntimeException ? (RuntimeException)$$0 : new RuntimeException($$0);
    }

    private static void onThreadException(Thread $$0, Throwable $$1) {
        Util.pauseInIde($$1);
        if ($$1 instanceof CompletionException) {
            $$1 = $$1.getCause();
        }
        if ($$1 instanceof ReportedException) {
            ReportedException $$2 = (ReportedException)$$1;
            Bootstrap.realStdoutPrintln($$2.getReport().getFriendlyReport(ReportType.CRASH));
            System.exit(-1);
        }
        LOGGER.error("Caught exception in thread {}", (Object)$$0, (Object)$$1);
    }

    public static @Nullable Type<?> fetchChoiceType(DSL.TypeReference $$0, String $$1) {
        if (!SharedConstants.CHECK_DATA_FIXER_SCHEMA) {
            return null;
        }
        return Util.doFetchChoiceType($$0, $$1);
    }

    private static @Nullable Type<?> doFetchChoiceType(DSL.TypeReference $$0, String $$1) {
        Type $$2;
        block2: {
            $$2 = null;
            try {
                $$2 = DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey((int)SharedConstants.getCurrentVersion().dataVersion().version())).getChoiceType($$0, $$1);
            }
            catch (IllegalArgumentException $$3) {
                LOGGER.error("No data fixer registered for {}", (Object)$$1);
                if (!SharedConstants.IS_RUNNING_IN_IDE) break block2;
                throw $$3;
            }
        }
        return $$2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void runNamed(Runnable $$0, String $$1) {
        block16: {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                Thread $$2 = Thread.currentThread();
                String $$3 = $$2.getName();
                $$2.setName($$1);
                try (Zone $$4 = TracyClient.beginZone((String)$$1, (boolean)SharedConstants.IS_RUNNING_IN_IDE);){
                    $$0.run();
                    break block16;
                }
                finally {
                    $$2.setName($$3);
                }
            }
            try (Zone $$5 = TracyClient.beginZone((String)$$1, (boolean)SharedConstants.IS_RUNNING_IN_IDE);){
                $$0.run();
            }
        }
    }

    public static <T> String getRegisteredName(Registry<T> $$0, T $$1) {
        Identifier $$2 = $$0.getKey($$1);
        if ($$2 == null) {
            return "[unregistered]";
        }
        return $$2.toString();
    }

    public static <T> Predicate<T> allOf() {
        return $$0 -> true;
    }

    public static <T> Predicate<T> allOf(Predicate<? super T> $$0) {
        return $$0;
    }

    public static <T> Predicate<T> allOf(Predicate<? super T> $$0, Predicate<? super T> $$1) {
        return $$2 -> $$0.test($$2) && $$1.test($$2);
    }

    public static <T> Predicate<T> allOf(Predicate<? super T> $$0, Predicate<? super T> $$1, Predicate<? super T> $$2) {
        return $$3 -> $$0.test($$3) && $$1.test($$3) && $$2.test($$3);
    }

    public static <T> Predicate<T> allOf(Predicate<? super T> $$0, Predicate<? super T> $$1, Predicate<? super T> $$2, Predicate<? super T> $$3) {
        return $$4 -> $$0.test($$4) && $$1.test($$4) && $$2.test($$4) && $$3.test($$4);
    }

    public static <T> Predicate<T> allOf(Predicate<? super T> $$0, Predicate<? super T> $$1, Predicate<? super T> $$2, Predicate<? super T> $$3, Predicate<? super T> $$4) {
        return $$5 -> $$0.test($$5) && $$1.test($$5) && $$2.test($$5) && $$3.test($$5) && $$4.test($$5);
    }

    @SafeVarargs
    public static <T> Predicate<T> allOf(Predicate<? super T> ... $$0) {
        return $$1 -> {
            for (Predicate $$2 : $$0) {
                if ($$2.test($$1)) continue;
                return false;
            }
            return true;
        };
    }

    public static <T> Predicate<T> allOf(List<? extends Predicate<? super T>> $$0) {
        return switch ($$0.size()) {
            case 0 -> Util.allOf();
            case 1 -> Util.allOf($$0.get(0));
            case 2 -> Util.allOf($$0.get(0), $$0.get(1));
            case 3 -> Util.allOf($$0.get(0), $$0.get(1), $$0.get(2));
            case 4 -> Util.allOf($$0.get(0), $$0.get(1), $$0.get(2), $$0.get(3));
            case 5 -> Util.allOf($$0.get(0), $$0.get(1), $$0.get(2), $$0.get(3), $$0.get(4));
            default -> {
                Predicate[] $$1 = (Predicate[])$$0.toArray(Predicate[]::new);
                yield Util.allOf($$1);
            }
        };
    }

    public static <T> Predicate<T> anyOf() {
        return $$0 -> false;
    }

    public static <T> Predicate<T> anyOf(Predicate<? super T> $$0) {
        return $$0;
    }

    public static <T> Predicate<T> anyOf(Predicate<? super T> $$0, Predicate<? super T> $$1) {
        return $$2 -> $$0.test($$2) || $$1.test($$2);
    }

    public static <T> Predicate<T> anyOf(Predicate<? super T> $$0, Predicate<? super T> $$1, Predicate<? super T> $$2) {
        return $$3 -> $$0.test($$3) || $$1.test($$3) || $$2.test($$3);
    }

    public static <T> Predicate<T> anyOf(Predicate<? super T> $$0, Predicate<? super T> $$1, Predicate<? super T> $$2, Predicate<? super T> $$3) {
        return $$4 -> $$0.test($$4) || $$1.test($$4) || $$2.test($$4) || $$3.test($$4);
    }

    public static <T> Predicate<T> anyOf(Predicate<? super T> $$0, Predicate<? super T> $$1, Predicate<? super T> $$2, Predicate<? super T> $$3, Predicate<? super T> $$4) {
        return $$5 -> $$0.test($$5) || $$1.test($$5) || $$2.test($$5) || $$3.test($$5) || $$4.test($$5);
    }

    @SafeVarargs
    public static <T> Predicate<T> anyOf(Predicate<? super T> ... $$0) {
        return $$1 -> {
            for (Predicate $$2 : $$0) {
                if (!$$2.test($$1)) continue;
                return true;
            }
            return false;
        };
    }

    public static <T> Predicate<T> anyOf(List<? extends Predicate<? super T>> $$0) {
        return switch ($$0.size()) {
            case 0 -> Util.anyOf();
            case 1 -> Util.anyOf($$0.get(0));
            case 2 -> Util.anyOf($$0.get(0), $$0.get(1));
            case 3 -> Util.anyOf($$0.get(0), $$0.get(1), $$0.get(2));
            case 4 -> Util.anyOf($$0.get(0), $$0.get(1), $$0.get(2), $$0.get(3));
            case 5 -> Util.anyOf($$0.get(0), $$0.get(1), $$0.get(2), $$0.get(3), $$0.get(4));
            default -> {
                Predicate[] $$1 = (Predicate[])$$0.toArray(Predicate[]::new);
                yield Util.anyOf($$1);
            }
        };
    }

    public static <T> boolean isSymmetrical(int $$0, int $$1, List<T> $$2) {
        if ($$0 == 1) {
            return true;
        }
        int $$3 = $$0 / 2;
        for (int $$4 = 0; $$4 < $$1; ++$$4) {
            for (int $$5 = 0; $$5 < $$3; ++$$5) {
                T $$8;
                int $$6 = $$0 - 1 - $$5;
                T $$7 = $$2.get($$5 + $$4 * $$0);
                if ($$7.equals($$8 = $$2.get($$6 + $$4 * $$0))) continue;
                return false;
            }
        }
        return true;
    }

    public static int growByHalf(int $$0, int $$1) {
        return (int)Math.max(Math.min((long)$$0 + (long)($$0 >> 1), 0x7FFFFFF7L), (long)$$1);
    }

    @SuppressForbidden(reason="Intentional use of default locale for user-visible date")
    public static DateTimeFormatter localizedDateFormatter(FormatStyle $$0) {
        return DateTimeFormatter.ofLocalizedDateTime($$0);
    }

    public static OS getPlatform() {
        String $$0 = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if ($$0.contains("win")) {
            return OS.WINDOWS;
        }
        if ($$0.contains("mac")) {
            return OS.OSX;
        }
        if ($$0.contains("solaris")) {
            return OS.SOLARIS;
        }
        if ($$0.contains("sunos")) {
            return OS.SOLARIS;
        }
        if ($$0.contains("linux")) {
            return OS.LINUX;
        }
        if ($$0.contains("unix")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }

    public static boolean isAarch64() {
        String $$0 = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        return $$0.equals("aarch64");
    }

    public static URI parseAndValidateUntrustedUri(String $$0) throws URISyntaxException {
        URI $$1 = new URI($$0);
        String $$2 = $$1.getScheme();
        if ($$2 == null) {
            throw new URISyntaxException($$0, "Missing protocol in URI: " + $$0);
        }
        String $$3 = $$2.toLowerCase(Locale.ROOT);
        if (!ALLOWED_UNTRUSTED_LINK_PROTOCOLS.contains($$3)) {
            throw new URISyntaxException($$0, "Unsupported protocol in URI: " + $$0);
        }
        return $$1;
    }

    public static <T> T findNextInIterable(Iterable<T> $$0, @Nullable T $$1) {
        Iterator<T> $$2 = $$0.iterator();
        T $$3 = $$2.next();
        if ($$1 != null) {
            T $$4 = $$3;
            while (true) {
                if ($$4 == $$1) {
                    if (!$$2.hasNext()) break;
                    return $$2.next();
                }
                if (!$$2.hasNext()) continue;
                $$4 = $$2.next();
            }
        }
        return $$3;
    }

    public static <T> T findPreviousInIterable(Iterable<T> $$0, @Nullable T $$1) {
        Iterator<T> $$2 = $$0.iterator();
        T $$3 = null;
        while ($$2.hasNext()) {
            T $$4 = $$2.next();
            if ($$4 == $$1) {
                if ($$3 != null) break;
                $$3 = (T)($$2.hasNext() ? Iterators.getLast($$2) : $$1);
                break;
            }
            $$3 = $$4;
        }
        return $$3;
    }

    public static <T> T make(Supplier<T> $$0) {
        return $$0.get();
    }

    public static <T> T make(T $$0, Consumer<? super T> $$1) {
        $$1.accept($$0);
        return $$0;
    }

    public static <K extends Enum<K>, V> Map<K, V> makeEnumMap(Class<K> $$0, java.util.function.Function<K, V> $$1) {
        EnumMap<Enum, V> $$2 = new EnumMap<Enum, V>($$0);
        for (Enum $$3 : (Enum[])$$0.getEnumConstants()) {
            $$2.put($$3, $$1.apply($$3));
        }
        return $$2;
    }

    public static <K, V1, V2> Map<K, V2> mapValues(Map<K, V1> $$0, java.util.function.Function<? super V1, V2> $$12) {
        return $$0.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, $$1 -> $$12.apply((Object)$$1.getValue())));
    }

    public static <K, V1, V2> Map<K, V2> mapValuesLazy(Map<K, V1> $$0, Function<V1, V2> $$1) {
        return Maps.transformValues($$0, $$1);
    }

    public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<V>> $$0) {
        if ($$0.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }
        if ($$0.size() == 1) {
            return $$0.getFirst().thenApply(ObjectLists::singleton);
        }
        CompletableFuture<Void> $$12 = CompletableFuture.allOf($$0.toArray(new CompletableFuture[0]));
        return $$12.thenApply($$1 -> $$0.stream().map(CompletableFuture::join).toList());
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFast(List<? extends CompletableFuture<? extends V>> $$0) {
        CompletableFuture $$1 = new CompletableFuture();
        return Util.fallibleSequence($$0, $$1::completeExceptionally).applyToEither((CompletionStage)$$1, java.util.function.Function.identity());
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFastAndCancel(List<? extends CompletableFuture<? extends V>> $$0) {
        CompletableFuture $$1 = new CompletableFuture();
        return Util.fallibleSequence($$0, $$2 -> {
            if ($$1.completeExceptionally((Throwable)$$2)) {
                for (CompletableFuture $$3 : $$0) {
                    $$3.cancel(true);
                }
            }
        }).applyToEither((CompletionStage)$$1, java.util.function.Function.identity());
    }

    private static <V> CompletableFuture<List<V>> fallibleSequence(List<? extends CompletableFuture<? extends V>> $$0, Consumer<Throwable> $$12) {
        ObjectArrayList $$2 = new ObjectArrayList();
        $$2.size($$0.size());
        CompletableFuture[] $$32 = new CompletableFuture[$$0.size()];
        for (int $$42 = 0; $$42 < $$0.size(); ++$$42) {
            int $$5 = $$42;
            $$32[$$42] = $$0.get($$42).whenComplete(($$3, $$4) -> {
                if ($$4 != null) {
                    $$12.accept((Throwable)$$4);
                } else {
                    $$2.set($$5, $$3);
                }
            });
        }
        return CompletableFuture.allOf($$32).thenApply($$1 -> $$2);
    }

    public static <T> Optional<T> ifElse(Optional<T> $$0, Consumer<T> $$1, Runnable $$2) {
        if ($$0.isPresent()) {
            $$1.accept($$0.get());
        } else {
            $$2.run();
        }
        return $$0;
    }

    public static <T> Supplier<T> name(final Supplier<T> $$0, Supplier<String> $$1) {
        if (SharedConstants.DEBUG_NAMED_RUNNABLES) {
            final String $$2 = $$1.get();
            return new Supplier<T>(){

                @Override
                public T get() {
                    return $$0.get();
                }

                public String toString() {
                    return $$2;
                }
            };
        }
        return $$0;
    }

    public static Runnable name(final Runnable $$0, Supplier<String> $$1) {
        if (SharedConstants.DEBUG_NAMED_RUNNABLES) {
            final String $$2 = $$1.get();
            return new Runnable(){

                @Override
                public void run() {
                    $$0.run();
                }

                public String toString() {
                    return $$2;
                }
            };
        }
        return $$0;
    }

    public static void logAndPauseIfInIde(String $$0) {
        LOGGER.error($$0);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            Util.doPause($$0);
        }
    }

    public static void logAndPauseIfInIde(String $$0, Throwable $$1) {
        LOGGER.error($$0, $$1);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            Util.doPause($$0);
        }
    }

    public static <T extends Throwable> T pauseInIde(T $$0) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", $$0);
            Util.doPause($$0.getMessage());
        }
        return $$0;
    }

    public static void setPause(Consumer<String> $$0) {
        thePauser = $$0;
    }

    private static void doPause(String $$0) {
        boolean $$2;
        Instant $$1 = Instant.now();
        LOGGER.warn("Did you remember to set a breakpoint here?");
        boolean bl = $$2 = Duration.between($$1, Instant.now()).toMillis() > 500L;
        if (!$$2) {
            thePauser.accept($$0);
        }
    }

    public static String describeError(Throwable $$0) {
        if ($$0.getCause() != null) {
            return Util.describeError($$0.getCause());
        }
        if ($$0.getMessage() != null) {
            return $$0.getMessage();
        }
        return $$0.toString();
    }

    public static <T> T getRandom(T[] $$0, RandomSource $$1) {
        return $$0[$$1.nextInt($$0.length)];
    }

    public static int getRandom(int[] $$0, RandomSource $$1) {
        return $$0[$$1.nextInt($$0.length)];
    }

    public static <T> T getRandom(List<T> $$0, RandomSource $$1) {
        return $$0.get($$1.nextInt($$0.size()));
    }

    public static <T> Optional<T> getRandomSafe(List<T> $$0, RandomSource $$1) {
        if ($$0.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Util.getRandom($$0, $$1));
    }

    private static BooleanSupplier createRenamer(final Path $$0, final Path $$1) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.move($$0, $$1, new CopyOption[0]);
                    return true;
                }
                catch (IOException $$02) {
                    LOGGER.error("Failed to rename", (Throwable)$$02);
                    return false;
                }
            }

            public String toString() {
                return "rename " + String.valueOf($$0) + " to " + String.valueOf($$1);
            }
        };
    }

    private static BooleanSupplier createDeleter(final Path $$0) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.deleteIfExists($$0);
                    return true;
                }
                catch (IOException $$02) {
                    LOGGER.warn("Failed to delete", (Throwable)$$02);
                    return false;
                }
            }

            public String toString() {
                return "delete old " + String.valueOf($$0);
            }
        };
    }

    private static BooleanSupplier createFileDeletedCheck(final Path $$0) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return !Files.exists($$0, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + String.valueOf($$0) + " is deleted";
            }
        };
    }

    private static BooleanSupplier createFileCreatedCheck(final Path $$0) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return Files.isRegularFile($$0, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + String.valueOf($$0) + " is present";
            }
        };
    }

    private static boolean executeInSequence(BooleanSupplier ... $$0) {
        for (BooleanSupplier $$1 : $$0) {
            if ($$1.getAsBoolean()) continue;
            LOGGER.warn("Failed to execute {}", (Object)$$1);
            return false;
        }
        return true;
    }

    private static boolean runWithRetries(int $$0, String $$1, BooleanSupplier ... $$2) {
        for (int $$3 = 0; $$3 < $$0; ++$$3) {
            if (Util.executeInSequence($$2)) {
                return true;
            }
            LOGGER.error("Failed to {}, retrying {}/{}", new Object[]{$$1, $$3, $$0});
        }
        LOGGER.error("Failed to {}, aborting, progress might be lost", (Object)$$1);
        return false;
    }

    public static void safeReplaceFile(Path $$0, Path $$1, Path $$2) {
        Util.safeReplaceOrMoveFile($$0, $$1, $$2, false);
    }

    public static boolean safeReplaceOrMoveFile(Path $$0, Path $$1, Path $$2, boolean $$3) {
        if (Files.exists($$0, new LinkOption[0]) && !Util.runWithRetries(10, "create backup " + String.valueOf($$2), Util.createDeleter($$2), Util.createRenamer($$0, $$2), Util.createFileCreatedCheck($$2))) {
            return false;
        }
        if (!Util.runWithRetries(10, "remove old " + String.valueOf($$0), Util.createDeleter($$0), Util.createFileDeletedCheck($$0))) {
            return false;
        }
        if (!Util.runWithRetries(10, "replace " + String.valueOf($$0) + " with " + String.valueOf($$1), Util.createRenamer($$1, $$0), Util.createFileCreatedCheck($$0)) && !$$3) {
            Util.runWithRetries(10, "restore " + String.valueOf($$0) + " from " + String.valueOf($$2), Util.createRenamer($$2, $$0), Util.createFileCreatedCheck($$0));
            return false;
        }
        return true;
    }

    public static int offsetByCodepoints(String $$0, int $$1, int $$2) {
        int $$3 = $$0.length();
        if ($$2 >= 0) {
            for (int $$4 = 0; $$1 < $$3 && $$4 < $$2; ++$$4) {
                if (!Character.isHighSurrogate($$0.charAt($$1++)) || $$1 >= $$3 || !Character.isLowSurrogate($$0.charAt($$1))) continue;
                ++$$1;
            }
        } else {
            for (int $$5 = $$2; $$1 > 0 && $$5 < 0; ++$$5) {
                if (!Character.isLowSurrogate($$0.charAt(--$$1)) || $$1 <= 0 || !Character.isHighSurrogate($$0.charAt($$1 - 1))) continue;
                --$$1;
            }
        }
        return $$1;
    }

    public static Consumer<String> prefix(String $$0, Consumer<String> $$1) {
        return $$2 -> $$1.accept($$0 + $$2);
    }

    public static DataResult<int[]> fixedSize(IntStream $$0, int $$1) {
        int[] $$2 = $$0.limit($$1 + 1).toArray();
        if ($$2.length != $$1) {
            Supplier<String> $$3 = () -> "Input is not a list of " + $$1 + " ints";
            if ($$2.length >= $$1) {
                return DataResult.error($$3, (Object)Arrays.copyOf($$2, $$1));
            }
            return DataResult.error($$3);
        }
        return DataResult.success((Object)$$2);
    }

    public static DataResult<long[]> fixedSize(LongStream $$0, int $$1) {
        long[] $$2 = $$0.limit($$1 + 1).toArray();
        if ($$2.length != $$1) {
            Supplier<String> $$3 = () -> "Input is not a list of " + $$1 + " longs";
            if ($$2.length >= $$1) {
                return DataResult.error($$3, (Object)Arrays.copyOf($$2, $$1));
            }
            return DataResult.error($$3);
        }
        return DataResult.success((Object)$$2);
    }

    public static <T> DataResult<List<T>> fixedSize(List<T> $$0, int $$1) {
        if ($$0.size() != $$1) {
            Supplier<String> $$2 = () -> "Input is not a list of " + $$1 + " elements";
            if ($$0.size() >= $$1) {
                return DataResult.error($$2, $$0.subList(0, $$1));
            }
            return DataResult.error($$2);
        }
        return DataResult.success($$0);
    }

    public static void startTimerHackThread() {
        Thread $$0 = new Thread("Timer hack thread"){

            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(Integer.MAX_VALUE);
                    }
                }
                catch (InterruptedException $$0) {
                    LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                    return;
                }
            }
        };
        $$0.setDaemon(true);
        $$0.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        $$0.start();
    }

    public static void copyBetweenDirs(Path $$0, Path $$1, Path $$2) throws IOException {
        Path $$3 = $$0.relativize($$2);
        Path $$4 = $$1.resolve($$3);
        Files.copy($$2, $$4, new CopyOption[0]);
    }

    public static String sanitizeName(String $$0, CharPredicate $$12) {
        return $$0.toLowerCase(Locale.ROOT).chars().mapToObj($$1 -> $$12.test((char)$$1) ? Character.toString((char)$$1) : "_").collect(Collectors.joining());
    }

    public static <K, V> SingleKeyCache<K, V> singleKeyCache(java.util.function.Function<K, V> $$0) {
        return new SingleKeyCache<K, V>($$0);
    }

    public static <T, R> java.util.function.Function<T, R> memoize(final java.util.function.Function<T, R> $$0) {
        return new java.util.function.Function<T, R>(){
            private final Map<T, R> cache = new ConcurrentHashMap();

            @Override
            public R apply(T $$02) {
                return this.cache.computeIfAbsent($$02, $$0);
            }

            public String toString() {
                return "memoize/1[function=" + String.valueOf($$0) + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> $$0) {
        return new BiFunction<T, U, R>(){
            private final Map<Pair<T, U>, R> cache = new ConcurrentHashMap();

            @Override
            public R apply(T $$02, U $$12) {
                return this.cache.computeIfAbsent(Pair.of($$02, $$12), $$1 -> $$0.apply($$1.getFirst(), $$1.getSecond()));
            }

            public String toString() {
                return "memoize/2[function=" + String.valueOf($$0) + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T> List<T> toShuffledList(Stream<T> $$0, RandomSource $$1) {
        ObjectArrayList $$2 = (ObjectArrayList)$$0.collect(ObjectArrayList.toList());
        Util.shuffle($$2, $$1);
        return $$2;
    }

    public static IntArrayList toShuffledList(IntStream $$0, RandomSource $$1) {
        int $$3;
        IntArrayList $$2 = IntArrayList.wrap((int[])$$0.toArray());
        for (int $$4 = $$3 = $$2.size(); $$4 > 1; --$$4) {
            int $$5 = $$1.nextInt($$4);
            $$2.set($$4 - 1, $$2.set($$5, $$2.getInt($$4 - 1)));
        }
        return $$2;
    }

    public static <T> List<T> shuffledCopy(T[] $$0, RandomSource $$1) {
        ObjectArrayList $$2 = new ObjectArrayList((Object[])$$0);
        Util.shuffle($$2, $$1);
        return $$2;
    }

    public static <T> List<T> shuffledCopy(ObjectArrayList<T> $$0, RandomSource $$1) {
        ObjectArrayList $$2 = new ObjectArrayList($$0);
        Util.shuffle($$2, $$1);
        return $$2;
    }

    public static <T> void shuffle(List<T> $$0, RandomSource $$1) {
        int $$2;
        for (int $$3 = $$2 = $$0.size(); $$3 > 1; --$$3) {
            int $$4 = $$1.nextInt($$3);
            $$0.set($$3 - 1, $$0.set($$4, $$0.get($$3 - 1)));
        }
    }

    public static <T> CompletableFuture<T> blockUntilDone(java.util.function.Function<Executor, CompletableFuture<T>> $$0) {
        return Util.blockUntilDone($$0, CompletableFuture::isDone);
    }

    public static <T> T blockUntilDone(java.util.function.Function<Executor, T> $$0, Predicate<T> $$1) {
        int $$6;
        LinkedBlockingQueue $$2 = new LinkedBlockingQueue();
        T $$3 = $$0.apply($$2::add);
        while (!$$1.test($$3)) {
            try {
                Runnable $$4 = (Runnable)$$2.poll(100L, TimeUnit.MILLISECONDS);
                if ($$4 == null) continue;
                $$4.run();
            }
            catch (InterruptedException $$5) {
                LOGGER.warn("Interrupted wait");
                break;
            }
        }
        if (($$6 = $$2.size()) > 0) {
            LOGGER.warn("Tasks left in queue: {}", (Object)$$6);
        }
        return $$3;
    }

    public static <T> ToIntFunction<T> createIndexLookup(List<T> $$0) {
        int $$1 = $$0.size();
        if ($$1 < 8) {
            return $$0::indexOf;
        }
        Object2IntOpenHashMap $$2 = new Object2IntOpenHashMap($$1);
        $$2.defaultReturnValue(-1);
        for (int $$3 = 0; $$3 < $$1; ++$$3) {
            $$2.put($$0.get($$3), $$3);
        }
        return $$2;
    }

    public static <T> ToIntFunction<T> createIndexIdentityLookup(List<T> $$0) {
        int $$1 = $$0.size();
        if ($$1 < 8) {
            ReferenceImmutableList $$2 = new ReferenceImmutableList($$0);
            return arg_0 -> ((ReferenceList)$$2).indexOf(arg_0);
        }
        Reference2IntOpenHashMap $$3 = new Reference2IntOpenHashMap($$1);
        $$3.defaultReturnValue(-1);
        for (int $$4 = 0; $$4 < $$1; ++$$4) {
            $$3.put($$0.get($$4), $$4);
        }
        return $$3;
    }

    public static <A, B> Typed<B> writeAndReadTypedOrThrow(Typed<A> $$0, Type<B> $$1, UnaryOperator<Dynamic<?>> $$2) {
        Dynamic $$3 = (Dynamic)$$0.write().getOrThrow();
        return Util.readTypedOrThrow($$1, (Dynamic)$$2.apply($$3), true);
    }

    public static <T> Typed<T> readTypedOrThrow(Type<T> $$0, Dynamic<?> $$1) {
        return Util.readTypedOrThrow($$0, $$1, false);
    }

    public static <T> Typed<T> readTypedOrThrow(Type<T> $$0, Dynamic<?> $$1, boolean $$2) {
        DataResult $$3 = $$0.readTyped($$1).map(Pair::getFirst);
        try {
            if ($$2) {
                return (Typed)$$3.getPartialOrThrow(IllegalStateException::new);
            }
            return (Typed)$$3.getOrThrow(IllegalStateException::new);
        }
        catch (IllegalStateException $$4) {
            CrashReport $$5 = CrashReport.forThrowable($$4, "Reading type");
            CrashReportCategory $$6 = $$5.addCategory("Info");
            $$6.setDetail("Data", $$1);
            $$6.setDetail("Type", $$0);
            throw new ReportedException($$5);
        }
    }

    public static <T> List<T> copyAndAdd(List<T> $$0, T $$1) {
        return ImmutableList.builderWithExpectedSize((int)($$0.size() + 1)).addAll($$0).add($$1).build();
    }

    public static <T> List<T> copyAndAdd(T $$0, List<T> $$1) {
        return ImmutableList.builderWithExpectedSize((int)($$1.size() + 1)).add($$0).addAll($$1).build();
    }

    public static <K, V> Map<K, V> copyAndPut(Map<K, V> $$0, K $$1, V $$2) {
        return ImmutableMap.builderWithExpectedSize((int)($$0.size() + 1)).putAll($$0).put($$1, $$2).buildKeepingLast();
    }

    public static enum OS {
        LINUX("linux"),
        SOLARIS("solaris"),
        WINDOWS("windows"){

            @Override
            protected String[] getOpenUriArguments(URI $$0) {
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", $$0.toString()};
            }
        }
        ,
        OSX("mac"){

            @Override
            protected String[] getOpenUriArguments(URI $$0) {
                return new String[]{"open", $$0.toString()};
            }
        }
        ,
        UNKNOWN("unknown");

        private final String telemetryName;

        OS(String $$0) {
            this.telemetryName = $$0;
        }

        public void openUri(URI $$0) {
            try {
                Process $$1 = Runtime.getRuntime().exec(this.getOpenUriArguments($$0));
                $$1.getInputStream().close();
                $$1.getErrorStream().close();
                $$1.getOutputStream().close();
            }
            catch (IOException $$2) {
                LOGGER.error("Couldn't open location '{}'", (Object)$$0, (Object)$$2);
            }
        }

        public void openFile(File $$0) {
            this.openUri($$0.toURI());
        }

        public void openPath(Path $$0) {
            this.openUri($$0.toUri());
        }

        protected String[] getOpenUriArguments(URI $$0) {
            String $$1 = $$0.toString();
            if ("file".equals($$0.getScheme())) {
                $$1 = $$1.replace("file:", "file://");
            }
            return new String[]{"xdg-open", $$1};
        }

        public void openUri(String $$0) {
            try {
                this.openUri(new URI($$0));
            }
            catch (IllegalArgumentException | URISyntaxException $$1) {
                LOGGER.error("Couldn't open uri '{}'", (Object)$$0, (Object)$$1);
            }
        }

        public String telemetryName() {
            return this.telemetryName;
        }
    }
}

