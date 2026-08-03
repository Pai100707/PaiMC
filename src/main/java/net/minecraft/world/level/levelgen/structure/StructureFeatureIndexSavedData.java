package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class StructureFeatureIndexSavedData extends SavedData {
   private final LongSet all;
   private final LongSet remaining;
   private static final Codec<LongSet> LONG_SET = Codec.LONG_STREAM.xmap(LongOpenHashSet::toSet, LongCollection::longStream);
   public static final Codec<StructureFeatureIndexSavedData> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(LONG_SET.fieldOf("All").forGetter($$0x -> $$0x.all), LONG_SET.fieldOf("Remaining").forGetter($$0x -> $$0x.remaining))
         .apply($$0, StructureFeatureIndexSavedData::new)
   );

   public static SavedDataType<StructureFeatureIndexSavedData> type(String $$0) {
      return new SavedDataType<>($$0, StructureFeatureIndexSavedData::new, CODEC, DataFixTypes.SAVED_DATA_STRUCTURE_FEATURE_INDICES);
   }

   private StructureFeatureIndexSavedData(LongSet $$0, LongSet $$1) {
      this.all = $$0;
      this.remaining = $$1;
   }

   public StructureFeatureIndexSavedData() {
      this(new LongOpenHashSet(), new LongOpenHashSet());
   }

   public void addIndex(long $$0) {
      this.all.add($$0);
      this.remaining.add($$0);
      this.setDirty();
   }

   public boolean hasStartIndex(long $$0) {
      return this.all.contains($$0);
   }

   public boolean hasUnhandledIndex(long $$0) {
      return this.remaining.contains($$0);
   }

   public void removeIndex(long $$0) {
      if (this.remaining.remove($$0)) {
         this.setDirty();
      }
   }

   public LongSet getAll() {
      return this.all;
   }
}
