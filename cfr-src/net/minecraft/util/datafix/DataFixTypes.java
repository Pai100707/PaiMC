/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.fixes.References;

public enum DataFixTypes {
    LEVEL(References.LEVEL),
    LEVEL_SUMMARY(References.LIGHTWEIGHT_LEVEL),
    PLAYER(References.PLAYER),
    CHUNK(References.CHUNK),
    HOTBAR(References.HOTBAR),
    OPTIONS(References.OPTIONS),
    STRUCTURE(References.STRUCTURE),
    STATS(References.STATS),
    SAVED_DATA_COMMAND_STORAGE(References.SAVED_DATA_COMMAND_STORAGE),
    SAVED_DATA_FORCED_CHUNKS(References.SAVED_DATA_TICKETS),
    SAVED_DATA_MAP_DATA(References.SAVED_DATA_MAP_DATA),
    SAVED_DATA_MAP_INDEX(References.SAVED_DATA_MAP_INDEX),
    SAVED_DATA_RAIDS(References.SAVED_DATA_RAIDS),
    SAVED_DATA_RANDOM_SEQUENCES(References.SAVED_DATA_RANDOM_SEQUENCES),
    SAVED_DATA_SCOREBOARD(References.SAVED_DATA_SCOREBOARD),
    SAVED_DATA_STOPWATCHES(References.SAVED_DATA_STOPWATCHES),
    SAVED_DATA_STRUCTURE_FEATURE_INDICES(References.SAVED_DATA_STRUCTURE_FEATURE_INDICES),
    SAVED_DATA_WORLD_BORDER(References.SAVED_DATA_WORLD_BORDER),
    ADVANCEMENTS(References.ADVANCEMENTS),
    POI_CHUNK(References.POI_CHUNK),
    WORLD_GEN_SETTINGS(References.WORLD_GEN_SETTINGS),
    ENTITY_CHUNK(References.ENTITY_CHUNK),
    DEBUG_PROFILE(References.DEBUG_PROFILE);

    public static final Set<DSL.TypeReference> TYPES_FOR_LEVEL_LIST;
    private final DSL.TypeReference type;

    private DataFixTypes(DSL.TypeReference $$0) {
        this.type = $$0;
    }

    static int currentVersion() {
        return SharedConstants.getCurrentVersion().dataVersion().version();
    }

    public <A> Codec<A> wrapCodec(final Codec<A> $$0, final DataFixer $$1, final int $$2) {
        return new Codec<A>(){

            public <T> DataResult<T> encode(A $$02, DynamicOps<T> $$12, T $$22) {
                return $$0.encode($$02, $$12, $$22).flatMap($$1 -> $$12.mergeToMap($$1, $$12.createString("DataVersion"), $$12.createInt(DataFixTypes.currentVersion())));
            }

            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> $$02, T $$12) {
                int $$22 = $$02.get($$12, "DataVersion").flatMap(arg_0 -> $$02.getNumberValue(arg_0)).map(Number::intValue).result().orElse($$2);
                Dynamic $$3 = new Dynamic($$02, $$02.remove($$12, "DataVersion"));
                Dynamic $$4 = DataFixTypes.this.updateToCurrentVersion($$1, $$3, $$22);
                return $$0.decode($$4);
            }
        };
    }

    public <T> Dynamic<T> update(DataFixer $$0, Dynamic<T> $$1, int $$2, int $$3) {
        return $$0.update(this.type, $$1, $$2, $$3);
    }

    public <T> Dynamic<T> updateToCurrentVersion(DataFixer $$0, Dynamic<T> $$1, int $$2) {
        return this.update($$0, $$1, $$2, DataFixTypes.currentVersion());
    }

    public CompoundTag update(DataFixer $$0, CompoundTag $$1, int $$2, int $$3) {
        return (CompoundTag)this.update($$0, new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)$$1), $$2, $$3).getValue();
    }

    public CompoundTag updateToCurrentVersion(DataFixer $$0, CompoundTag $$1, int $$2) {
        return this.update($$0, $$1, $$2, DataFixTypes.currentVersion());
    }

    static {
        TYPES_FOR_LEVEL_LIST = Set.of(DataFixTypes.LEVEL_SUMMARY.type);
    }
}

