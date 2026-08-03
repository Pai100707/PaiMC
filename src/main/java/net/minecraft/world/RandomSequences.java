package net.minecraft.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class RandomSequences extends SavedData {
   public static final Codec<RandomSequences> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Codec.INT.fieldOf("salt").forGetter(RandomSequences::salt),
            Codec.BOOL.optionalFieldOf("include_world_seed", true).forGetter(RandomSequences::includeWorldSeed),
            Codec.BOOL.optionalFieldOf("include_sequence_id", true).forGetter(RandomSequences::includeSequenceId),
            Codec.unboundedMap(Identifier.CODEC, RandomSequence.CODEC).fieldOf("sequences").forGetter($$0x -> $$0x.sequences)
         )
         .apply($$0, RandomSequences::new)
   );
   public static final SavedDataType<RandomSequences> TYPE = new SavedDataType(
      "random_sequences", RandomSequences::new, CODEC, DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES
   );
   private int salt;
   private boolean includeWorldSeed = true;
   private boolean includeSequenceId = true;
   private final Map<Identifier, RandomSequence> sequences = new Object2ObjectOpenHashMap();

   public RandomSequences() {
   }

   private RandomSequences(int $$0, boolean $$1, boolean $$2, Map<Identifier, RandomSequence> $$3) {
      this.salt = $$0;
      this.includeWorldSeed = $$1;
      this.includeSequenceId = $$2;
      this.sequences.putAll($$3);
   }

   public RandomSource get(Identifier $$0, long $$1) {
      RandomSource $$2 = this.sequences.computeIfAbsent($$0, $$1x -> this.createSequence($$1x, $$1)).random();
      return new RandomSequences.DirtyMarkingRandomSource($$2);
   }

   private RandomSequence createSequence(Identifier $$0, long $$1) {
      return this.createSequence($$0, $$1, this.salt, this.includeWorldSeed, this.includeSequenceId);
   }

   private RandomSequence createSequence(Identifier $$0, long $$1, int $$2, boolean $$3, boolean $$4) {
      long $$5 = ($$3 ? $$1 : 0L) ^ $$2;
      return new RandomSequence($$5, $$4 ? Optional.of($$0) : Optional.empty());
   }

   public void forAllSequences(BiConsumer<Identifier, RandomSequence> $$0) {
      this.sequences.forEach($$0);
   }

   public void setSeedDefaults(int $$0, boolean $$1, boolean $$2) {
      this.salt = $$0;
      this.includeWorldSeed = $$1;
      this.includeSequenceId = $$2;
   }

   public int clear() {
      int $$0 = this.sequences.size();
      this.sequences.clear();
      return $$0;
   }

   public void reset(Identifier $$0, long $$1) {
      this.sequences.put($$0, this.createSequence($$0, $$1));
   }

   public void reset(Identifier $$0, long $$1, int $$2, boolean $$3, boolean $$4) {
      this.sequences.put($$0, this.createSequence($$0, $$1, $$2, $$3, $$4));
   }

   private int salt() {
      return this.salt;
   }

   private boolean includeWorldSeed() {
      return this.includeWorldSeed;
   }

   private boolean includeSequenceId() {
      return this.includeSequenceId;
   }

   class DirtyMarkingRandomSource implements RandomSource {
      private final RandomSource random;

      DirtyMarkingRandomSource(final RandomSource $$0) {
         this.random = $$0;
      }

      public RandomSource fork() {
         RandomSequences.this.setDirty();
         return this.random.fork();
      }

      public PositionalRandomFactory forkPositional() {
         RandomSequences.this.setDirty();
         return this.random.forkPositional();
      }

      public void setSeed(long $$0) {
         RandomSequences.this.setDirty();
         this.random.setSeed($$0);
      }

      public int nextInt() {
         RandomSequences.this.setDirty();
         return this.random.nextInt();
      }

      public int nextInt(int $$0) {
         RandomSequences.this.setDirty();
         return this.random.nextInt($$0);
      }

      public long nextLong() {
         RandomSequences.this.setDirty();
         return this.random.nextLong();
      }

      public boolean nextBoolean() {
         RandomSequences.this.setDirty();
         return this.random.nextBoolean();
      }

      public float nextFloat() {
         RandomSequences.this.setDirty();
         return this.random.nextFloat();
      }

      public double nextDouble() {
         RandomSequences.this.setDirty();
         return this.random.nextDouble();
      }

      public double nextGaussian() {
         RandomSequences.this.setDirty();
         return this.random.nextGaussian();
      }

      @Override
      public boolean equals(Object $$0) {
         if (this == $$0) {
            return true;
         } else {
            return $$0 instanceof RandomSequences.DirtyMarkingRandomSource $$1 ? this.random.equals($$1.random) : false;
         }
      }
   }
}
