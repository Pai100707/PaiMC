/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Stopwatch;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

public class Stopwatches
extends SavedData {
    private static final Codec<Stopwatches> CODEC = Codec.unboundedMap(Identifier.CODEC, (Codec)Codec.LONG).fieldOf("stopwatches").codec().xmap(Stopwatches::unpack, Stopwatches::pack);
    public static final SavedDataType<Stopwatches> TYPE = new SavedDataType<Stopwatches>("stopwatches", Stopwatches::new, CODEC, DataFixTypes.SAVED_DATA_STOPWATCHES);
    private final Map<Identifier, Stopwatch> stopwatches = new Object2ObjectOpenHashMap();

    private Stopwatches() {
    }

    private static Stopwatches unpack(Map<Identifier, Long> $$0) {
        Stopwatches $$1 = new Stopwatches();
        long $$22 = Stopwatches.currentTime();
        $$0.forEach(($$2, $$3) -> $$0.stopwatches.put((Identifier)$$2, new Stopwatch($$22, (long)$$3)));
        return $$1;
    }

    private Map<Identifier, Long> pack() {
        long $$0 = Stopwatches.currentTime();
        TreeMap<Identifier, Long> $$1 = new TreeMap<Identifier, Long>();
        this.stopwatches.forEach(($$2, $$3) -> $$1.put((Identifier)$$2, $$3.elapsedMilliseconds($$0)));
        return $$1;
    }

    public @Nullable Stopwatch get(Identifier $$0) {
        return this.stopwatches.get($$0);
    }

    public boolean add(Identifier $$0, Stopwatch $$1) {
        if (this.stopwatches.putIfAbsent($$0, $$1) == null) {
            this.setDirty();
            return true;
        }
        return false;
    }

    public boolean update(Identifier $$0, UnaryOperator<Stopwatch> $$12) {
        if (this.stopwatches.computeIfPresent($$0, ($$1, $$2) -> (Stopwatch)$$12.apply((Stopwatch)$$2)) != null) {
            this.setDirty();
            return true;
        }
        return false;
    }

    public boolean remove(Identifier $$0) {
        boolean $$1;
        boolean bl = $$1 = this.stopwatches.remove($$0) != null;
        if ($$1) {
            this.setDirty();
        }
        return $$1;
    }

    @Override
    public boolean isDirty() {
        return super.isDirty() || !this.stopwatches.isEmpty();
    }

    public List<Identifier> ids() {
        return List.copyOf(this.stopwatches.keySet());
    }

    public static long currentTime() {
        return Util.getMillis();
    }
}

