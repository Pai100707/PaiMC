/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 */
package net.minecraft.server;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Queue;
import net.minecraft.util.ArrayListDeque;

public class SuppressedExceptionCollector {
    private static final int LATEST_ENTRY_COUNT = 8;
    private final Queue<LongEntry> latestEntries = new ArrayListDeque<LongEntry>();
    private final Object2IntLinkedOpenHashMap<ShortEntry> entryCounts = new Object2IntLinkedOpenHashMap();

    private static long currentTimeMs() {
        return System.currentTimeMillis();
    }

    public synchronized void addEntry(String $$0, Throwable $$1) {
        long $$2 = SuppressedExceptionCollector.currentTimeMs();
        String $$3 = $$1.getMessage();
        this.latestEntries.add(new LongEntry($$2, $$0, $$1.getClass(), $$3));
        while (this.latestEntries.size() > 8) {
            this.latestEntries.remove();
        }
        ShortEntry $$4 = new ShortEntry($$0, $$1.getClass());
        int $$5 = this.entryCounts.getInt((Object)$$4);
        this.entryCounts.putAndMoveToFirst((Object)$$4, $$5 + 1);
    }

    public synchronized String dump() {
        long $$0 = SuppressedExceptionCollector.currentTimeMs();
        StringBuilder $$1 = new StringBuilder();
        if (!this.latestEntries.isEmpty()) {
            $$1.append("\n\t\tLatest entries:\n");
            for (LongEntry $$2 : this.latestEntries) {
                $$1.append("\t\t\t").append($$2.location).append(":").append($$2.cls).append(": ").append($$2.message).append(" (").append($$0 - $$2.timestampMs).append("ms ago)").append("\n");
            }
        }
        if (!this.entryCounts.isEmpty()) {
            if ($$1.isEmpty()) {
                $$1.append("\n");
            }
            $$1.append("\t\tEntry counts:\n");
            for (Object2IntMap.Entry $$3 : Object2IntMaps.fastIterable(this.entryCounts)) {
                $$1.append("\t\t\t").append(((ShortEntry)$$3.getKey()).location).append(":").append(((ShortEntry)$$3.getKey()).cls).append(" x ").append($$3.getIntValue()).append("\n");
            }
        }
        if ($$1.isEmpty()) {
            return "~~NONE~~";
        }
        return $$1.toString();
    }

    static final class LongEntry
    extends Record {
        final long timestampMs;
        final String location;
        final Class<? extends Throwable> cls;
        final String message;

        LongEntry(long $$0, String $$1, Class<? extends Throwable> $$2, String $$3) {
            this.timestampMs = $$0;
            this.location = $$1;
            this.cls = $$2;
            this.message = $$3;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LongEntry.class, "timestampMs;location;cls;message", "timestampMs", "location", "cls", "message"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LongEntry.class, "timestampMs;location;cls;message", "timestampMs", "location", "cls", "message"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LongEntry.class, "timestampMs;location;cls;message", "timestampMs", "location", "cls", "message"}, this, $$0);
        }

        public long timestampMs() {
            return this.timestampMs;
        }

        public String location() {
            return this.location;
        }

        public Class<? extends Throwable> cls() {
            return this.cls;
        }

        public String message() {
            return this.message;
        }
    }

    static final class ShortEntry
    extends Record {
        final String location;
        final Class<? extends Throwable> cls;

        ShortEntry(String $$0, Class<? extends Throwable> $$1) {
            this.location = $$0;
            this.cls = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ShortEntry.class, "location;cls", "location", "cls"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ShortEntry.class, "location;cls", "location", "cls"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ShortEntry.class, "location;cls", "location", "cls"}, this, $$0);
        }

        public String location() {
            return this.location;
        }

        public Class<? extends Throwable> cls() {
            return this.cls;
        }
    }
}

