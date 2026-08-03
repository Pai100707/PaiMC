package net.minecraft.util.datafix;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DSL.TypeReference;
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

   public static final Set<TypeReference> TYPES_FOR_LEVEL_LIST;
   private final TypeReference type;

   private DataFixTypes(final TypeReference $$0) {
      this.type = $$0;
   }

   static int currentVersion() {
      return SharedConstants.getCurrentVersion().dataVersion().version();
   }

   public <A> Codec<A> wrapCodec(final Codec<A> $$0, final DataFixer $$1, final int $$2) {
      return new Codec<A>() {
         public <T> DataResult<T> encode(A $$0x, DynamicOps<T> $$1x, T $$2x) {
            return $$0.encode($$0, $$1, $$2)
               .flatMap($$1xxx -> $$1.mergeToMap($$1xxx, $$1.createString("DataVersion"), $$1.createInt(DataFixTypes.currentVersion())));
         }

         public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> $$0x, T $$1x) {
            int $$2x = $$0.get($$1, "DataVersion").flatMap($$0::getNumberValue).map(Number::intValue).result().orElse($$2);
            Dynamic<T> $$3 = new Dynamic($$0, $$0.remove($$1, "DataVersion"));
            Dynamic<T> $$4 = DataFixTypes.this.updateToCurrentVersion($$1, $$3, $$2x);
            return $$0.decode($$4);
         }
      };
   }

   public <T> Dynamic<T> update(DataFixer $$0, Dynamic<T> $$1, int $$2, int $$3) {
      return $$0.update(this.type, $$1, $$2, $$3);
   }

   public <T> Dynamic<T> updateToCurrentVersion(DataFixer $$0, Dynamic<T> $$1, int $$2) {
      return this.update($$0, $$1, $$2, currentVersion());
   }

   public CompoundTag update(DataFixer $$0, CompoundTag $$1, int $$2, int $$3) {
      return (CompoundTag)this.update($$0, new Dynamic(NbtOps.INSTANCE, $$1), $$2, $$3).getValue();
   }

   public CompoundTag updateToCurrentVersion(DataFixer $$0, CompoundTag $$1, int $$2) {
      return this.update($$0, $$1, $$2, currentVersion());
   }

   static {
      TYPES_FOR_LEVEL_LIST = Set.of(LEVEL_SUMMARY.type);
   }
}
