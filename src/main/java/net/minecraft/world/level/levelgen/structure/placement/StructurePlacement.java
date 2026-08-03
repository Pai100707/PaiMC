package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products.P5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public abstract class StructurePlacement {
   public static final Codec<StructurePlacement> CODEC = BuiltInRegistries.STRUCTURE_PLACEMENT
      .byNameCodec()
      .dispatch(StructurePlacement::type, StructurePlacementType::codec);
   private static final int HIGHLY_ARBITRARY_RANDOM_SALT = 10387320;
   private final Vec3i locateOffset;
   private final StructurePlacement.FrequencyReductionMethod frequencyReductionMethod;
   private final float frequency;
   private final int salt;
   private final Optional<StructurePlacement.ExclusionZone> exclusionZone;

   protected static <S extends StructurePlacement> P5<Mu<S>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>> placementCodec(
      Instance<S> $$0
   ) {
      return $$0.group(
         Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(StructurePlacement::locateOffset),
         StructurePlacement.FrequencyReductionMethod.CODEC
            .optionalFieldOf("frequency_reduction_method", StructurePlacement.FrequencyReductionMethod.DEFAULT)
            .forGetter(StructurePlacement::frequencyReductionMethod),
         Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter(StructurePlacement::frequency),
         ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(StructurePlacement::salt),
         StructurePlacement.ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(StructurePlacement::exclusionZone)
      );
   }

   protected StructurePlacement(Vec3i $$0, StructurePlacement.FrequencyReductionMethod $$1, float $$2, int $$3, Optional<StructurePlacement.ExclusionZone> $$4) {
      this.locateOffset = $$0;
      this.frequencyReductionMethod = $$1;
      this.frequency = $$2;
      this.salt = $$3;
      this.exclusionZone = $$4;
   }

   protected Vec3i locateOffset() {
      return this.locateOffset;
   }

   protected StructurePlacement.FrequencyReductionMethod frequencyReductionMethod() {
      return this.frequencyReductionMethod;
   }

   protected float frequency() {
      return this.frequency;
   }

   protected int salt() {
      return this.salt;
   }

   protected Optional<StructurePlacement.ExclusionZone> exclusionZone() {
      return this.exclusionZone;
   }

   public boolean isStructureChunk(ChunkGeneratorStructureState $$0, int $$1, int $$2) {
      return this.isPlacementChunk($$0, $$1, $$2)
         && this.applyAdditionalChunkRestrictions($$1, $$2, $$0.getLevelSeed())
         && this.applyInteractionsWithOtherStructures($$0, $$1, $$2);
   }

   public boolean applyAdditionalChunkRestrictions(int $$0, int $$1, long $$2) {
      return !(this.frequency < 1.0F) || this.frequencyReductionMethod.shouldGenerate($$2, this.salt, $$0, $$1, this.frequency);
   }

   public boolean applyInteractionsWithOtherStructures(ChunkGeneratorStructureState $$0, int $$1, int $$2) {
      return !this.exclusionZone.isPresent() || !this.exclusionZone.get().isPlacementForbidden($$0, $$1, $$2);
   }

   protected abstract boolean isPlacementChunk(ChunkGeneratorStructureState var1, int var2, int var3);

   public BlockPos getLocatePos(net.minecraft.world.level.ChunkPos $$0) {
      return new BlockPos($$0.getMinBlockX(), 0, $$0.getMinBlockZ()).offset(this.locateOffset());
   }

   public abstract StructurePlacementType<?> type();

   private static boolean probabilityReducer(long $$0, int $$1, int $$2, int $$3, float $$4) {
      WorldgenRandom $$5 = new WorldgenRandom(new LegacyRandomSource(0L));
      $$5.setLargeFeatureWithSalt($$0, $$1, $$2, $$3);
      return $$5.nextFloat() < $$4;
   }

   private static boolean legacyProbabilityReducerWithDouble(long $$0, int $$1, int $$2, int $$3, float $$4) {
      WorldgenRandom $$5 = new WorldgenRandom(new LegacyRandomSource(0L));
      $$5.setLargeFeatureSeed($$0, $$2, $$3);
      return $$5.nextDouble() < $$4;
   }

   private static boolean legacyArbitrarySaltProbabilityReducer(long $$0, int $$1, int $$2, int $$3, float $$4) {
      WorldgenRandom $$5 = new WorldgenRandom(new LegacyRandomSource(0L));
      $$5.setLargeFeatureWithSalt($$0, $$2, $$3, 10387320);
      return $$5.nextFloat() < $$4;
   }

   private static boolean legacyPillagerOutpostReducer(long $$0, int $$1, int $$2, int $$3, float $$4) {
      int $$5 = $$2 >> 4;
      int $$6 = $$3 >> 4;
      WorldgenRandom $$7 = new WorldgenRandom(new LegacyRandomSource(0L));
      $$7.setSeed($$5 ^ $$6 << 4 ^ $$0);
      $$7.nextInt();
      return $$7.nextInt((int)(1.0F / $$4)) == 0;
   }

   @Deprecated
   public record ExclusionZone(Holder<StructureSet> otherSet, int chunkCount) {
      public static final Codec<StructurePlacement.ExclusionZone> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               RegistryFileCodec.create(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC, false)
                  .fieldOf("other_set")
                  .forGetter(StructurePlacement.ExclusionZone::otherSet),
               Codec.intRange(1, 16).fieldOf("chunk_count").forGetter(StructurePlacement.ExclusionZone::chunkCount)
            )
            .apply($$0, StructurePlacement.ExclusionZone::new)
      );

      boolean isPlacementForbidden(ChunkGeneratorStructureState $$0, int $$1, int $$2) {
         return $$0.hasStructureChunkInRange(this.otherSet, $$1, $$2, this.chunkCount);
      }
   }

   @FunctionalInterface
   public interface FrequencyReducer {
      boolean shouldGenerate(long var1, int var3, int var4, int var5, float var6);
   }

   public static enum FrequencyReductionMethod implements StringRepresentable {
      DEFAULT("default", StructurePlacement::probabilityReducer),
      LEGACY_TYPE_1("legacy_type_1", StructurePlacement::legacyPillagerOutpostReducer),
      LEGACY_TYPE_2("legacy_type_2", StructurePlacement::legacyArbitrarySaltProbabilityReducer),
      LEGACY_TYPE_3("legacy_type_3", StructurePlacement::legacyProbabilityReducerWithDouble);

      public static final Codec<StructurePlacement.FrequencyReductionMethod> CODEC = StringRepresentable.fromEnum(
         StructurePlacement.FrequencyReductionMethod::values
      );
      private final String name;
      private final StructurePlacement.FrequencyReducer reducer;

      private FrequencyReductionMethod(final String $$0, final StructurePlacement.FrequencyReducer $$1) {
         this.name = $$0;
         this.reducer = $$1;
      }

      public boolean shouldGenerate(long $$0, int $$1, int $$2, int $$3, float $$4) {
         return this.reducer.shouldGenerate($$0, $$1, $$2, $$3, $$4);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
